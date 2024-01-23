package com.breskul.bibernate.util;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.Table;
import com.breskul.bibernate.exception.EntityParseException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityUtil {

  // TODO: javadoc
  public static void validateIsEntity(Class<?> cls) {
    if (!cls.isAnnotationPresent(Entity.class)) {
      throw new EntityParseException("Class should be marked with 'Entity' annotation");
    }
  }

  public static String getEntityTableName(Class<?> cls) {
    return Optional.ofNullable(cls.getAnnotation(Table.class))
        .map(Table::name)
        .orElseThrow(() -> new EntityParseException(
            "Class should be marked with 'Table' annotation and it should not be empty"));
  }

  // TODO: javadoc
  public static List<Field> getClassColumnFields(Class<?> cls) {
    return Arrays.stream(cls.getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(
            Id.class))
        .toList();
  }

  // TODO: javadoc
  public static Field findEntityIdField(List<Field> fields) {
    List<Field> idFields = fields.stream()
        .filter(field -> field.isAnnotationPresent(Id.class))
        .toList();
    if (idFields.isEmpty()) {
      throw new EntityParseException("Entity should define ID column marked with 'Id' annotation.");
    }
    if (idFields.size() > 1) {
      throw new EntityParseException("Only one field should be marked with 'Id' annotation.");
    }
    return idFields.get(0);
  }

  // TODO: javadoc
  public static String composeSelectBlockFromColumns(List<Field> columnNames) {
    return columnNames.stream()
        .map(EntityUtil::getFieldColumnName)
        .collect(Collectors.joining(", "));
  }

  // TODO: javadoc
  public static String resolveColumnName(Field idField) {
    if (idField.isAnnotationPresent(Column.class)) {
      return idField.getAnnotation(Column.class).name();
    }
    return idField.getName();
  }

  private static String getFieldColumnName(Field field) {
    if (field.isAnnotationPresent(Column.class)) {
      return field.getAnnotation(Column.class).name();
    } else if (field.isAnnotationPresent(Id.class)) {
      return getIdColumnName(field);
    }
    throw new IllegalArgumentException(
        "Field should have annotation of type [Column.class, Id.class]");
  }

  private EntityUtil() {
  }
}
