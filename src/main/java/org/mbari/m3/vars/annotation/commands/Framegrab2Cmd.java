package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author Brian Schlining
 * @since 2018-08-13T11:41:00
 */
public class Framegrab2Cmd implements Command {

    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC);

    private volatile Annotation annotationRef;
    private volatile Image imageRef;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void apply(UIToolBox toolBox) {
        
    }

    @Override
    public void unapply(UIToolBox toolBox) {

    }

    @Override
    public String getDescription() {
        return null;
    }
}
