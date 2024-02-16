package com.breskul.bibernate.persistence.context.snapshot;

/**
 * A snapshot of an entity property, representing a single column of an entity. This record contains the name of the column and its
 * corresponding value.
 */
public record EntityPropertySnapshot(String columnName, Object columnValue) {

  /**
   * Constructs a new {@code EntityPropertySnapshot} with the specified column name and value.
   *
   * @param columnName  The name of the column.
   * @param columnValue The value of the column.
   * @return A new {@code EntityPropertySnapshot} instance.
   */
  public static EntityPropertySnapshot of(String columnName, Object columnValue) {
    return new EntityPropertySnapshot(columnName, columnValue);
  }
}
