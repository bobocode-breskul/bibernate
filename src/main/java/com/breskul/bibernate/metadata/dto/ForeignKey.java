package com.breskul.bibernate.metadata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForeignKey {
  String tableName;
  String fieldName;
  String relatedTableName;
  String constraintId;
}
