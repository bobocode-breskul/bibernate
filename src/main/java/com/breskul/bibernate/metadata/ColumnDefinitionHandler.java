package com.breskul.bibernate.metadata;

import com.breskul.bibernate.metadata.dto.DataType;
import java.lang.reflect.Field;

/**
 * Represents an interface for resolving the sql data type from an entity field definition.
 */
public interface ColumnDefinitionHandler {

  /**
   * Resolves the SQL data type from an entity field definition.
   *
   * @param field the field whose data type needs to be resolved
   * @return the resolved sql data type
   */
  DataType resolveDataType(Field field);
}
