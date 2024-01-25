package com.breskul.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Specifies a column for joining an entity association or element
 * collection.  If the <code>JoinColumn</code> annotation itself is
 * defaulted, a single join column is assumed and the default values
 * apply.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JoinColumn {

    // TODO: add attributes
}
