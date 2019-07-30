package org.mbari.m3.vars.annotation.services;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.model.Annotation;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Brian Schlining
 * @since 2018-04-06T12:17:00
 */
public class CombinedMediaAnnotationDecoratorTest {

    private final CombinedMediaAnnotationDecorator decorator =
            new CombinedMediaAnnotationDecorator(Initializer.getToolBox());

    @Test
    public void test01() throws Exception {
        List<Annotation> annotations = decorator.findAnnotations(UUID.fromString("d92bc7c3-8852-4135-a40f-bd6e0e00a650"))
                .get(5, TimeUnit.SECONDS);
        assertTrue("Expected to find annotations", annotations.size() > 0);
    }

    @Test
    public void test02() throws Exception {
        List<Annotation> annotations = decorator.findAllAnnotationsInDeployment("Ventana 4110")
                .get(20, TimeUnit.SECONDS);
        assertTrue("Expected to find annotations", annotations.size() > 0);
    }
}
