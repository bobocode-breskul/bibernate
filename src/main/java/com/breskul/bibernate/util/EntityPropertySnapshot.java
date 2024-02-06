package com.breskul.bibernate.util;

public record EntityPropertySnapshot(String columnName, Object columnValue) {

  public static EntityPropertySnapshot of(String columnName, Object columnValue) {
    return new EntityPropertySnapshot(columnName, columnValue);
  }
}
