package com.breskul.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Specifies a single-valued association to another entity that has
 * one-to-one multiplicity. It is not normally necessary to specify
 * the associated target entity explicitly since it can usually be
 * inferred from the type of the object being referenced.  If the relationship is
 * bidirectional, the non-owning side must use the <code>mappedBy</code> element of
 * the <code>OneToOne</code> annotation to specify the relationship field or
 * property of the owning side.
 *
 * <p> The <code>OneToOne</code> annotation may be used within an
 * embeddable class to specify a relationship from the embeddable
 * class to an entity class. If the relationship is bidirectional and
 * the entity containing the embeddable class is on the owning side of
 * the relationship, the non-owning side must use the
 * <code>mappedBy</code> element of the <code>OneToOne</code>
 * annotation to specify the relationship field or property of the
 * embeddable class. The dot (".") notation syntax must be used in the
 * <code>mappedBy</code> element to indicate the relationship attribute within the
 * embedded attribute.  The value of each identifier used with the dot
 * notation is the name of the respective embedded field or property.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToOne {

  // TODO: add attributes
}
