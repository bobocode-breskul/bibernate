package com.breskul.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Specifies that a field or property of an entity class is part of the natural id of the entity.
 * This annotation is very useful when the primary key of an entity class is a surrogate key, that
 * is, a {@code GeneratedValue} synthetic identifier, with no domain-model semantics. There should
 * always be some other field or combination of fields which uniquely identifies an instance of the
 * entity from the point of view of the user of the system. This is the <em>natural id</em> of the
 * entity.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NaturalId {

  // TODO: add attributes
}
