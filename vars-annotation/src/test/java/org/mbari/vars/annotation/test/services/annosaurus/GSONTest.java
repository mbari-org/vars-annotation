package org.mbari.vars.annotation.test.services.annosaurus;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.test.services.TestConstants;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Brian Schlining
 * @since 2017-08-07T14:57:00
 */
public class GSONTest {

    @org.junit.jupiter.api.Test
    public void fromJson() {
        Annotation annotation = TestConstants.newTestAnnotation();
        assertNotNull(annotation.getImages());
        assertEquals(2, annotation.getImages().size());

        var assoc0 = annotation.getAssociations().getFirst();
        assertNotNull(assoc0.getLastUpdatedTime());
    }

   
}
