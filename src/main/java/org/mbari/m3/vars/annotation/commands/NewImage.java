package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.model.Image;

/**
 * @author Brian Schlining
 * @since 2017-05-17T11:09:00
 */
public class NewImage implements NewObjectNotification<Image> {

    private final Image image;

    public NewImage(Image image) {
        this.image = image;
    }

    @Override
    public Image get() {
        return image;
    }
}
