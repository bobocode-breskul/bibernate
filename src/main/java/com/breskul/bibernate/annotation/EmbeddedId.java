package com.breskul.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Applied to a persistent field or property of an entity
 * class or mapped superclass to denote a composite primary
 * key that is an embeddable class. The embeddable class
 * must be annotated as {@link Embeddable}.
 *
 * <p> There must be only one <code>EmbeddedId</code> annotation and
 * no <code>Id</code> annotation when the <code>EmbeddedId</code> annotation is used.
 *
 * <p> The {@code AttributeOverride} annotation may be used to override
 * the column mappings declared within the embeddable class.
 *
 * <p> The {@code MapsId} annotation may be used in conjunction
 * with the <code>EmbeddedId</code> annotation to specify a derived
 * primary key.
 *
 * <p> If the entity has a derived primary key, the
 * <code>AttributeOverride</code> annotation may only be used to
 * override those attributes of the embedded id that do not correspond
 * to the relationship to the parent entity.
 *
 * <p> Relationship mappings defined within an embedded id class are not supported.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EmbeddedId {

    // TODO: add attributes
}
