package com.breskul.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Specifies a persistent field or property of an entity whose
 * value is an instance of an embeddable class. The embeddable
 * class must be annotated as {@link Embeddable}.
 *
 * <p> The <code>AttributeOverride</code>, <code>AttributeOverrides</code>,
 * <code>AssociationOverride</code>, and <code>AssociationOverrides</code>
 * annotations may be used to override mappings declared or defaulted
 * by the embeddable class.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Embedded {

  // TODO: add attributes
}
