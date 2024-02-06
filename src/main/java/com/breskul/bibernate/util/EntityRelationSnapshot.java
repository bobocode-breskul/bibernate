package com.breskul.bibernate.util;

public record EntityRelationSnapshot(Class<?> relationType, String columnName, Object columnValue) {

  public static EntityRelationSnapshot of(Class<?> relationType, String columnName,
      Object columnValue) {
    return new EntityRelationSnapshot(relationType, columnName, columnValue);
  }

}
