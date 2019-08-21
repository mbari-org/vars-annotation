package org.mbari.vars.services.gson;

import com.google.gson.InstanceCreator;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.ImagedMoment;

import java.lang.reflect.Type;

/**
 * @author Brian Schlining
 * @since 2018-07-26T11:24:00
 */
public class AnnotationCreator implements InstanceCreator<ImagedMoment> {
  @Override
  public ImagedMoment createInstance(Type type) {
    return new Annotation();
  }
}
