package com.breskul.bibernate.annotation;

import static com.breskul.bibernate.annotation.FetchType.EAGER;
import static com.breskul.bibernate.annotation.FetchType.LAZY;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Specifies a single-valued association to another entity class that
 * has many-to-one multiplicity. It is not normally necessary to
 * specify the target entity explicitly since it can usually be
 * inferred from the type of the object being referenced.  If the
 * relationship is bidirectional, the non-owning
 * <code>OneToMany</code> entity side must used the
 * <code>mappedBy</code> element to specify the relationship field or
 * property of the entity that is the owner of the relationship.
 *
 * <p> The <code>ManyToOne</code> annotation may be used within an
 * embeddable class to specify a relationship from the embeddable
 * class to an entity class. If the relationship is bidirectional, the
 * non-owning <code>OneToMany</code> entity side must use the <code>mappedBy</code>
 * element of the <code>OneToMany</code> annotation to specify the
 * relationship field or property of the embeddable field or property
 * on the owning side of the relationship. The dot (".") notation
 * syntax must be used in the <code>mappedBy</code> element to indicate the
 * relationship attribute within the embedded attribute.  The value of
 * each identifier used with the dot notation is the name of the
 * respective embedded field or property.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToOne {
  FetchType fetch() default EAGER;
    // TODO: add attributes
}
