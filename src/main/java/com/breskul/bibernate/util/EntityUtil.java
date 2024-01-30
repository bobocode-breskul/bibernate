package com.breskul.bibernate.util;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.JoinColumn;
import com.breskul.bibernate.annotation.ManyToMany;
import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.OneToMany;
import com.breskul.bibernate.annotation.OneToOne;
import com.breskul.bibernate.annotation.Table;
import com.breskul.bibernate.exception.EntityParseException;
import com.breskul.bibernate.persistence.Test;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityUtil {

  // TODO: javadoc
  public static void validateIsEntity(Class<?> cls) {
    if (!cls.isAnnotationPresent(Entity.class)) {
      throw new EntityParseException("Class should be marked with 'Entity' annotation");
    }
  }


  public static void validateColumnName(Class<?> cls, String columnName) {
    validateIsEntity(cls);
    var column = getClassColumnFields(cls).stream()
      .filter(field -> resolveColumnName(field).equals(columnName))
      .findFirst();
    if (column.isEmpty()) {
      throw new IllegalArgumentException("Entity [%s] does not have a column: [%s]."
        .formatted(cls, columnName));
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
      .filter(field -> !isCollectionEntityField(field))
      .toList();
  }

  public static boolean isCollectionEntityField(Field field) {
    return field.isAnnotationPresent(OneToMany.class)
      || field.isAnnotationPresent(ManyToMany.class);
  }

  public static List<Field> getClassEntityFields(Class<?> cls) {
    return Arrays.stream(cls.getDeclaredFields())
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

  public static Field findEntityIdField(Class<?> cls) {
    validateIsEntity(cls);
    return findEntityIdField(getClassColumnFields(cls));
  }

  // TODO: javadoc
  public static String composeSelectBlockFromColumns(List<Field> columnNames) {
    return columnNames.stream()
        .map(EntityUtil::resolveColumnName)
        .collect(Collectors.joining(", "));
  }

  // TODO: javadoc
  public static String resolveColumnName(Field field) {
    if (field.isAnnotationPresent(Column.class)) {
      var columnName = field.getAnnotation(Column.class).name();
      return columnName.isBlank() ? field.getName() : columnName;
    } else if (field.isAnnotationPresent(JoinColumn.class)) {
      var columnName = field.getAnnotation(JoinColumn.class).name();
      return columnName.isBlank() ? field.getName() + "_id" : columnName;
    }
    return field.getName();
  }

  public static String getJoinColumnName(Class<?> entityType, Class<?> joinedEntity) {
    var joinField = Arrays.stream(entityType.getDeclaredFields())
      .filter(field -> field.getType().equals(joinedEntity))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("Can't find related entity [%s] field in [%s]."
        .formatted(joinedEntity, entityType)));

    return resolveColumnName(joinField);
  }

  public static Object getEntityId(Object entity) {
    var idField = findEntityIdField(List.of(entity.getClass().getDeclaredFields()));
    return readFieldValue(entity, idField);
  }

  public static boolean isPrimitiveColumn(Field field) {
    return field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class);
  }

  public static Class<?> getEntityCollectionElementType(Field field) {
    var parameterizedType = (ParameterizedType) field.getGenericType();
    var typeArguments = parameterizedType.getActualTypeArguments();
    var actualTypeArgument = typeArguments[0];
    return (Class<?>) actualTypeArgument;
  }

  public static Collection<Object> getCollectionInstance(Field collectionField) {
    var collectionClass = collectionField.getType();

    if (collectionClass.isAssignableFrom(List.class)) {
      return new ArrayList<>();
    }

    if (collectionClass.isAssignableFrom(Set.class)) {
      return new HashSet<>();
    }

    throw new IllegalArgumentException("Unsupported collection: " + collectionClass); // change exception and more clear msg?
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

  private EntityUtil() {
  }
}
