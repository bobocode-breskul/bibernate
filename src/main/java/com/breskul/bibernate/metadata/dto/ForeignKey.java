package com.breskul.bibernate.metadata.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ForeignKey {
  private String tableName;
  private String fieldName;
  private String relatedTableName;
  private String constraintName;

  public String getTableName() {
    return tableName;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getRelatedTableName() {
    return relatedTableName;
  }

  public String getConstraintName() {
    return constraintName;
  }

}
