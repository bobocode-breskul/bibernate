package com.breskul.bibernate.metadata;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.metadata.dto.DataType;
import java.lang.reflect.Field;

public abstract class ColumnDefinitionHandler {
  DataType getDataType(Field field) {
    var columnAnnotation = field.getAnnotation(Column.class);
    if (columnAnnotation != null && !columnAnnotation.columnDefinition().isBlank()) {
      var colDefinition = columnAnnotation.columnDefinition();
      return new DataType(colDefinition);
    }
    return this.resolveDataType(field);
  }

  abstract DataType resolveDataType(Field field);
}
