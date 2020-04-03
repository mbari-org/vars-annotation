package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;

import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.MediaChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the life-cycle o the connection to remote apps that could support
 * localization
 */
public class LocalizationLifecycleController {

    private final UIToolBox toolBox;
    private volatile LocalizationController controller;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final long THREAD_SLEEP_MILLIS = 1000L;
    private volatile int threadCounter = 0;

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

    private synchronized void manageControllerLifecycle(Media media) {
        // Manage lifecylce in it's own thread as it needs to block for a second to allow
        // ZeroMQ to spin down/up.
        Runnable runner = () -> {
            if (controller != null) {
                controller.close();
                controller = null;
                // IT takes some time for ZeroMQ to shutdown/start
                try {
                    Thread.sleep(THREAD_SLEEP_MILLIS);
                } catch (InterruptedException e) {
                    log.warn("Localization lifecycle thread was interrupted");
                }
            }
            if (media != null) {
                if (media.getUri().getScheme().equalsIgnoreCase("http")) {
                    LocalizationSettings settings = LocalizationPrefs.load(toolBox.getAppConfig());
                    if (settings.isEnabled()) {
                        log.debug("Creating a LocalizationController for " + media.getUri());
                        controller = new LocalizationController(settings, toolBox);
                        try {
                            Thread.sleep(THREAD_SLEEP_MILLIS);
                        } catch (InterruptedException e) {
                            log.warn("Localization lifecycle thread was interrupted");
                        }
                    }
                }
            }
        };
        new Thread(runner,
                getClass().getSimpleName() + "-" + threadCounter++).start();
    }
}
