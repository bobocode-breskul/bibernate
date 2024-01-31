package com.breskul.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Specifies a many-valued association with many-to-many multiplicity.
 *
 * <p> Every many-to-many association has two sides, the owning side
 * and the non-owning, or inverse, side.  The join table is specified
 * on the owning side. If the association is bidirectional, either
 * side may be designated as the owning side.  If the relationship is
 * bidirectional, the non-owning side must use the <code>mappedBy</code> element of
 * the <code>ManyToMany</code> annotation to specify the relationship field or
 * property of the owning side.
 *
 * <p> The join table for the relationship, if not defaulted, is
 * specified on the owning side.
 *
 * <p> The <code>ManyToMany</code> annotation may be used within an
 * embeddable class contained within an entity class to specify a
 * relationship to a collection of entities. If the relationship is
 * bidirectional and the entity containing the embeddable class is the
 * owner of the relationship, the non-owning side must use the
 * <code>mappedBy</code> element of the <code>ManyToMany</code>
 * annotation to specify the relationship field or property of the
 * embeddable class. The dot (".") notation syntax must be used in the
 * <code>mappedBy</code> element to indicate the relationship
 * attribute within the embedded attribute.  The value of each
 * identifier used with the dot notation is the name of the respective
 * embedded field or property.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToMany {

  // TODO: add attributes
}
