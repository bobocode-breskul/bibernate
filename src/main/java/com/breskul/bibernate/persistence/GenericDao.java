package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.util.EntityUtil.composeSelectBlockFromColumns;
import static com.breskul.bibernate.util.EntityUtil.findEntityIdField;
import static com.breskul.bibernate.util.EntityUtil.getClassColumnFields;
import static com.breskul.bibernate.util.EntityUtil.getEntityTableName;
import static com.breskul.bibernate.util.EntityUtil.resolveColumnName;
import static com.breskul.bibernate.util.EntityUtil.validateIsEntity;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.generate;

import com.breskul.bibernate.exception.EntityQueryException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

public class GenericDao {

  // TODO: change to select '*'
  private static final String SELECT_BY_ID_QUERY = "SELECT %s FROM %s WHERE %s = ?";
  private static final String INSERT_ENTITY_QUERY = "INSERT INTO %s (%s) VALUES (%s);";

  private final Connection connection;

  public GenericDao(Connection connection) {
    this.connection = connection;
  }


  public <T> T findById(Class<T> cls, Object id) {
    validateIsEntity(cls);

    String tableName = getEntityTableName(cls);
    List<Field> columnFields = getClassColumnFields(cls);
    Field idField = findEntityIdField(columnFields);
    String idColumnName = resolveColumnName(idField);

    String sql = SELECT_BY_ID_QUERY.formatted(composeSelectBlockFromColumns(columnFields),
        tableName, idColumnName);

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, id);
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return mapResult(resultSet, cls);
      }
    } catch (SQLException e) {
      throw new EntityQueryException(
          "Could not read entity data from database for entity [%s] with id [%s]"
              .formatted(cls, id), e);
    }
    return null;
  }

  /**
   * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
   * entity instance completely.
   *
   * @param entity must not be {@literal null}.
   * @return the saved entity; will never be {@literal null}.
   * @throws NullPointerException in case the given {@literal entity} is {@literal null}.
   * @throws EntityQueryException If an error occurs during the save operation.
   */
  public <T> T save(T entity) {
    requireNonNull(entity, "Entity should not be null.");
    Class<?> cls = entity.getClass();
    String tableName = getEntityTableName(cls);
    Field idField = findEntityIdField(cls);
    List<Field> columnFields = getClassColumnFields(cls, field -> !field.equals(idField));

    String questionMarks = generate(() -> "?")
        .limit(columnFields.size())
        .collect(Collectors.joining(", "));
    String sql = INSERT_ENTITY_QUERY.formatted(tableName,
        composeSelectBlockFromColumns(columnFields), questionMarks);

    try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      for (int i = 0; i < columnFields.size(); i++) {
        Field field = columnFields.get(i);
        field.setAccessible(true);
        statement.setObject(i + 1, field.get(entity));
      }
      int result = statement.executeUpdate();
      if (result != 1) {
        throw new EntityQueryException(
            "Could not save entity to database for entity [%s]"
                .formatted(entity));
      }
      ResultSet generatedKeys = statement.getGeneratedKeys();
      generatedKeys.next();
      Object idValue = idField.getType().cast(generatedKeys.getObject(1));
      idField.setAccessible(true);
      idField.set(entity, idValue);
    } catch (SQLException e) {
      throw new EntityQueryException(
          "Could not save entity to database for entity [%s]"
              .formatted(entity), e);
    } catch (IllegalAccessException e) {
      throw new EntityQueryException(
          "Could not set id to entity [%s]"
              .formatted(entity), e);
    } catch (ClassCastException e) {
      throw new EntityQueryException(
          "Could not cast id value to type [%s] for entity [%s]"
              .formatted(idField.getType().getSimpleName(), entity), e);
    }
    return entity;
  }

  // todo add logic for relation annotations - @OneToMany, @ManyToOne, @ManyToMany
  private <T> T mapResult(ResultSet resultSet, Class<T> cls) {
    // todo: change it to have both field and it's column name (from @Column annotation)
    List<Field> columnFields = getClassColumnFields(cls);
    try {
      T t = cls.getConstructor().newInstance();
      for (int i = 0; i < columnFields.size(); i++) {
        Field field = columnFields.get(i);
        field.setAccessible(true);
        // todo: take data from resultSet by column name (taken from @Column annotation)
        field.set(t, resultSet.getObject(i + 1));
      }
      return t;
    } catch (IllegalAccessException e) {
      throw new EntityQueryException(
          "Entity [%s] should have public no-args constructor".formatted(cls), e);
    } catch (IllegalArgumentException | NoSuchMethodException e) {
      throw new EntityQueryException(
          "Entity [%s] should have constructor without parameters".formatted(cls), e);
    } catch (InstantiationException e) {
      throw new EntityQueryException("Entity [%s] should be non-abstract class".formatted(cls), e);
    } catch (InvocationTargetException e) {
      throw new EntityQueryException(
          "Could not create instance of target entity [%s]".formatted(cls), e);
    } catch (SQLException e) {
      throw new EntityQueryException("Could not read single row data from database for entity [%s]"
          .formatted(cls), e);
    }
  }
}
