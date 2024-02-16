package com.breskul.bibernate.annotation;

import static com.breskul.bibernate.annotation.FetchType.LAZY;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Specifies a many-valued association with one-to-many multiplicity.
 *
 * <p> If the collection is defined using generics to specify the
 * element type, the associated target entity type need not be specified; otherwise the target entity class must be specified. If the
 * relationship is bidirectional, the
 * <code>mappedBy</code> element must be used to specify the relationship field or
 * property of the entity that is the owner of the relationship.
 *
 * <p> The <code>OneToMany</code> annotation may be used within an embeddable class
 * contained within an entity class to specify a relationship to a collection of entities. If the relationship is bidirectional, the
 * <code> mappedBy</code> element must be used to specify the relationship field or
 * property of the entity that is the owner of the relationship.
 * <p>
 * When the collection is a <code>java.util.Map</code>, the <code>cascade</code> element and the
 * <code>orphanRemoval</code> element apply to the map value.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToMany {

  /**
   * (Optional) Whether the association should be lazily
   * loaded or must be eagerly fetched. The EAGER
   * strategy is a requirement on the persistence provider runtime that
   * the associated entity must be eagerly fetched. The LAZY
   * strategy is a hint to the persistence provider runtime.
   */
  FetchType fetch() default LAZY;
}
