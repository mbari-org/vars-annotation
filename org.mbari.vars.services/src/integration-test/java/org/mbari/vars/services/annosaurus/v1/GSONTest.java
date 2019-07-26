package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.ui.DemoConstants;



/**
 * @author Brian Schlining
 * @since 2017-08-07T14:57:00
 */
public class GSONTest {

    @Test
    public void fromJson() {
        Annotation annotation = DemoConstants.newTestAnnotation();
        assertTrue(annotation.getImages() != null);
        assertTrue(annotation.getImages().size() == 2);
    }
}
