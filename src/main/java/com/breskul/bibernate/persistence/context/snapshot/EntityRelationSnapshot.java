package com.breskul.bibernate.persistence.context.snapshot;

/**
 * A snapshot of a to-one relation of an entity, representing a single column of an entity. This
 * record contains the type of the relation, the name of the column, and its corresponding value.
 */
public record EntityRelationSnapshot(Class<?> relationType, String columnName, Object columnValue) {

  /**
   * Constructs a new {@code EntityRelationSnapshot} with the specified relation type, column name,
   * and value.
   *
   * @param relationType The type of the relation.
   * @param columnName   The name of the column.
   * @param columnValue  The value of the column.
   * @return A new {@code EntityRelationSnapshot} instance.
   */
  public static EntityRelationSnapshot of(Class<?> relationType, String columnName,
      Object columnValue) {
    return new EntityRelationSnapshot(relationType, columnName, columnValue);
  }

}
