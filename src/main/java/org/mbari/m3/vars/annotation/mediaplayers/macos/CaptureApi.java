package org.mbari.m3.vars.annotation.mediaplayers.macos;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author Brian Schlining
 * @since 2018-04-25T10:42:00
 */
public enum CaptureApi {
    NONE("None", SelectableNoopImageCaptureService::getInstance),
    BLACKMAGIC_DESIGN("Blackmagic Design", BMImageCaptureService::getInstance),
    AVFOUNDATION("AVFoundation", AVFImageCaptureService::getInstance);

    private final String name;
    private final Supplier<SelectableImageCaptureService> factory;

    CaptureApi(String name, Supplier<SelectableImageCaptureService> factory) {
        this.name = name;
        this.factory = factory;
    }

    public String getName() {
        return name;
    }

    public SelectableImageCaptureService getImageCaptureService() {
        return factory.get();
    }

    public static CaptureApi findByName(String name) {
        return Arrays.stream(values())
                .filter(captureApi -> captureApi.name.equals(name))
                .findFirst()
                .orElse(NONE);
    }
}
