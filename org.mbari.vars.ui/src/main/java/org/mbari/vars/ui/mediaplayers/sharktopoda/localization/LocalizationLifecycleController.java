package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;

import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.MediaChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages the life-cycle o the connection to remote apps that could support
 * localization
 */
public class LocalizationLifecycleController {

    private final UIToolBox toolBox;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final long THREAD_SLEEP_MILLIS = 1000L;
    private volatile int threadCounter = 0;
    private static final AtomicReference<LocalizationController> controllerRef = new AtomicReference<>();
    private static final Lock lock = new ReentrantLock();

    public LocalizationLifecycleController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        EventBus eventBus = toolBox.getEventBus();
        eventBus.toObserverable()
                .ofType(MediaChangedEvent.class)
                .map(MediaChangedEvent::get)
                .subscribe(this::manageControllerLifecycle);
    }

    private void manageControllerLifecycle(Media media) {
        // Manage lifecylce in it's own thread as it needs to block for a second to allow
        // ZeroMQ to spin down/up.

        Runnable runnable = () -> {
            lock.lock();
            boolean pause = false;
            LocalizationController controller = controllerRef.get();
            LocalizationSettings settings = LocalizationPrefs.load(toolBox.getAppConfig());
            if (controller == null) {
                if (media == null) {
                    // Nothing to do. Let's get out of here.
                    return;
                }
                else if (media.getUri().getScheme().equalsIgnoreCase("http") &&
                    settings.isEnabled()) {
                    log.debug("Creating a LocalizationController for " + media.getUri());
                    controller = new LocalizationController(settings, toolBox);
                    controllerRef.set(controller);
                    pause = true;
                }
            }
            else {
                if (media == null) {
                    // What to do?
                }
                else if (!media.getUri().getScheme().equalsIgnoreCase("http")) {
                    controller.close();
                    controllerRef.set(null);
                    pause = true;
                }
            }
            if (pause) {
                try {
                    Thread.sleep(THREAD_SLEEP_MILLIS);
                } catch (InterruptedException e) {
                    log.warn("A thread was interrupted while cycling the state of the LocalizationController");
                }
            }
            lock.unlock();
        };

        toolBox.getExecutorService().submit(runnable);

    }
}
