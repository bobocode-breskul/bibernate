package com.breskul.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Specifies the mapped column for a persistent property or field. If no <code>Column</code>
 * annotation is specified, the default values apply.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

  // TODO: add attributes

  String name() default "";


  boolean unique() default false;

  /**
   * (Optional) Whether the database column is nullable.
   */
  boolean nullable() default true;


  /**
   * (Optional) The SQL fragment that is used when
   * generating the DDL for the column.
   * <p> Defaults to the generated SQL to create a
   * column of the inferred type.
   */
  String columnDefinition() default "";

  /**
   * (Optional) The column length. (Applies only if a
   * string-valued column is used.)
   */
  int length() default 255;

  /**
   * (Optional) The precision for a decimal (exact numeric)
   * column. (Applies only if a decimal column is used.)
   * Value must be set by developer if used when generating
   * the DDL for the column.
   */
  int precision() default 0;

  /**
   * (Optional) The scale for a decimal (exact numeric) column.
   * (Applies only if a decimal column is used.)
   */
  int scale() default 0;

  /**
   * (Optional) Name of field handler which will resolve sql type name for this field
   * @see com.breskul.bibernate.metadata.ColumnDefinitionHandler
   */
  String fieldHandler() default "";
}
