package com.breskul.bibernate.metadata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public class ForeignKey {
  private String tableName;
  private String fieldName;
  private String relatedTableName;
  private String constraintId;

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getRelatedTableName() {
    return relatedTableName;
  }

  public void setRelatedTableName(String relatedTableName) {
    this.relatedTableName = relatedTableName;
  }

  public String getConstraintId() {
    return constraintId;
  }

  public void setConstraintId(String constraintId) {
    this.constraintId = constraintId;
  }
}
