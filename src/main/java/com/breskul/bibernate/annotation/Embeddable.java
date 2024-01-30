package com.breskul.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Specifies a class whose instances are stored as an intrinsic
 * part of an owning entity and share the identity of the entity.
 * Each of the persistent properties or fields of the embedded
 * object is mapped to the database table for the entity.
 *
 * <p> Note that the {@code Transient} annotation may be used to
 * designate the non-persistent state of an embeddable class.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Embeddable {

  // TODO: add attributes
}
