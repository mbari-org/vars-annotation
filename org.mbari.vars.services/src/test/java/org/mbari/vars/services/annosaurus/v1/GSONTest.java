package org.mbari.vars.services.annosaurus.v1;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mbari.vars.services.TestConstants;
import org.mbari.vars.services.model.Annotation;


/**
 * @author Brian Schlining
 * @since 2017-08-07T14:57:00
 */
public class GSONTest {

    @Test
    public void fromJson() {
        Annotation annotation = TestConstants.newTestAnnotation();
        assertTrue(annotation.getImages() != null);
        assertTrue(annotation.getImages().size() == 2);
    }
}
