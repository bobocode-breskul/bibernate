package com.breskul.bibernate.util;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.Table;
import com.breskul.bibernate.exception.EntityParseException;
import com.breskul.bibernate.exception.EntityQueryException;
import com.breskul.bibernate.persistence.EntityMapper;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sql.DataSource;

public class EntityUtil {

  public static <T> T doQuery(String sql, Object id, EntityMapper<T> entityMapper,
      DataSource dataSource) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, id);
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return entityMapper.mapResult(resultSet);
      }
    } catch (SQLException e) {
      throw new EntityQueryException(
          "Could not read entity data from database for entity [%s] with id [%s]"
              .formatted(entityMapper.getEntityCls(), id), e);
    }
    return null;
  }

  public static <T> void validateIsEntity(Class<T> cls) {
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

  public static List<Field> getClassColumnFields(Class<?> cls) {
    return Arrays.stream(cls.getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(
            Id.class))
        .toList();
  }

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

  public static String composeSelectBlockFromColumns(List<Field> columnNames) {
    return columnNames.stream()
        .map(EntityUtil::getFieldColumnName)
        .collect(Collectors.joining(", "));
  }

  public static String getIdColumnName(Field idField) {
    if (idField.isAnnotationPresent(Column.class)) {
      return idField.getAnnotation(Column.class).name();
    }
    return idField.getName();
  }

  public static Object getEntityId(Object entity) {
    var idField = findEntityIdField(List.of(entity.getClass().getDeclaredFields()));
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
