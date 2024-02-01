package com.breskul.bibernate.util;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.Table;
import com.breskul.bibernate.exception.EntityParseException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EntityUtil {

  // TODO: javadoc
  public static void validateIsEntity(Class<?> cls) {
    if (!cls.isAnnotationPresent(Entity.class)) {
      throw new EntityParseException("Class should be marked with 'Entity' annotation");
    }
  }

  // TODO: implement get default table name by class name
  public static String getEntityTableName(Class<?> cls) {
    return Optional.ofNullable(cls.getAnnotation(Table.class))
        .map(Table::name)
        .orElseGet(cls::getName);
  }

  // TODO: javadoc
  public static List<Field> getClassColumnFields(Class<?> cls) {
    return Arrays.stream(cls.getDeclaredFields())
        .toList();
  }

  public static List<Field> getClassColumnFields(Class<?> cls, Predicate<Field> fieldPredicate) {
    return Arrays.stream(cls.getDeclaredFields())
        .filter(fieldPredicate)
        .toList();
  }

  // TODO: javadoc
  public static Field findEntityIdField(Class<?> entityClass) {
    List<Field> idFields = Arrays.stream(entityClass.getDeclaredFields())
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

  public static <T> String findEntityIdFieldName(Class<T> entityClass) {
    return findEntityIdField(entityClass).getName();
  }


  // TODO: javadoc
  public static String composeSelectBlockFromColumns(List<Field> columnNames) {
    return columnNames.stream()
        .map(EntityUtil::resolveColumnName)
        .collect(Collectors.joining(", "));
  }

  // TODO: javadoc
  public static String resolveColumnName(Field idField) {
    if (idField.isAnnotationPresent(Column.class)) {
      return idField.getAnnotation(Column.class).name();
    }
    return idField.getName();
  }

  public static Object getEntityId(Object entity) {
    var idField = findEntityIdField(entity.getClass());
    return readFieldValue(entity, idField);
  }

  public static Object readFieldValue(Object entity, Field idField) {
    try {
      idField.setAccessible(true);
      return idField.get(entity);
    } catch (IllegalAccessException e) {
      throw new EntityParseException(
          "Failed to access field '" + idField.getName() + "' of entity type '" + entity.getClass()
              .getName() + "': Illegal access");
    }
  }

  public static <T> List<Field> getEntityColumns(Class<? extends T> entityClass) {
    return Arrays.stream(entityClass.getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(Column.class))
        .collect(Collectors.toList());
  }

  public static <T> Object[] getEntityColumnValues(T entity) {
    return getEntityColumns(entity.getClass()).stream()
        .map(field -> readFieldValue(entity, field))
        .toArray();
  }

  public static <T> List<String> getEntityColumnNames(Class<? extends T> entityClass) {
    return Arrays.stream(entityClass.getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(Column.class))
        .map(field -> field.getAnnotation(Column.class).name())
        .collect(Collectors.toList());
  }

  private EntityUtil() {
  }
}
