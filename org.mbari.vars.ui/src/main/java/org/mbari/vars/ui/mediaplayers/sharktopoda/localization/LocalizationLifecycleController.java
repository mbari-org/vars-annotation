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
    private static final AtomicReference<LocalizationController> controllerRef = new AtomicReference<>();
    private static final Object lock = new byte[]{};

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

    private boolean isFileMedia(Media media) {
        String scheme = media.getUri().getScheme();
        return scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("file");
    }

    private void manageControllerLifecycle(final Media media) {
        // Manage lifecylce in it's own thread as it needs to block for a second to allow
        // ZeroMQ to spin down/up.


//        Runnable runnable = () -> {
            synchronized (controllerRef) {
                LocalizationController controller = controllerRef.get();
                LocalizationSettings settings = LocalizationPrefs.load(toolBox.getAppConfig());
                if (controller != null) {
                    controller.close();
                    controllerRef.set(null);
                    try {
                        Thread.sleep(THREAD_SLEEP_MILLIS);
                    } catch (InterruptedException e) {
                        log.warn("A thread was interrupted while cycling the state of the LocalizationController");
                    }
                }

                var isFile = isFileMedia(media);
                if (media == null || !isFile) {
                    return;
                }

                log.debug("Creating a LocalizationController for " + media.getUri());
                controller = new LocalizationController(settings, toolBox);
                controllerRef.set(controller);
                log.debug("Controller created");
                try {
                    Thread.sleep(THREAD_SLEEP_MILLIS);
                } catch (InterruptedException e) {
                    log.warn("A thread was interrupted while cycling the state of the LocalizationController");
                }
            }

//        };
//
//        toolBox.getExecutorService().submit(runnable);

    }
}
