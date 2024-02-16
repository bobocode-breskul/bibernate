package com.breskul.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Specifies the primary table for the annotated entity.
 *
 * <p> If no <code>Table</code> annotation is specified for an entity
 * class, the default values apply.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {

  /**
   * The name of the table.
   * <p> Defaults to the entity name.
   */
  String name();

  /**
   * (Optional) The catalog of the table.
   * <p> Defaults to the default catalog.
   */
  String catalog() default "";

  /**
   * (Optional) The schema of the table.
   * <p> Defaults to the default schema for user.
   */
  String schema() default "";
}
