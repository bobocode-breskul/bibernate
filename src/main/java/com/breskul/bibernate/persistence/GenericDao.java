package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.util.EntityUtil.composeSelectBlockFromColumns;
import static com.breskul.bibernate.util.EntityUtil.findEntityIdField;
import static com.breskul.bibernate.util.EntityUtil.getClassColumnFields;
import static com.breskul.bibernate.util.EntityUtil.getClassEntityFields;
import static com.breskul.bibernate.util.EntityUtil.getCollectionInstance;
import static com.breskul.bibernate.util.EntityUtil.getEntityCollectionElementType;
import static com.breskul.bibernate.util.EntityUtil.getEntityTableName;
import static com.breskul.bibernate.util.EntityUtil.getJoinColumnName;
import static com.breskul.bibernate.util.EntityUtil.isSimpleColumn;
import static com.breskul.bibernate.util.EntityUtil.resolveColumnName;
import static com.breskul.bibernate.util.EntityUtil.validateColumnName;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.generate;

import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.OneToMany;
import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.exception.EntityIdIsNullException;
import com.breskul.bibernate.exception.EntityQueryException;
import com.breskul.bibernate.util.EntityUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;


public class GenericDao {

  // TODO: change to select '*'
  private static final String SELECT_BY_FIELD_VALUE_QUERY = "SELECT %s FROM %s WHERE %s = ?";
  private static final String SELECT_BY_ID_QUERY = "SELECT %s FROM %s WHERE %s = ?";
  private static final String UPDATE_SQL = "UPDATE %s SET %s WHERE %s = ?;";
  private static final String INSERT_ENTITY_QUERY = "INSERT INTO %s (%s) VALUES (%s);";
  private static final String DELETE_ENTITY_QUERY = "DELETE FROM %s WHERE %s = ?;";

  private static final Logger log = LoggerFactory.getLogger(GenericDao.class);
  private final DataSource dataSource;
  PersistenceContext context;

  public GenericDao(DataSource dataSource, PersistenceContext context) {
    this.dataSource = dataSource;
    this.context = context;
  }

  /**
   * Find by primary key. Search for an entity of the specified class and primary key. If the entity
   * instance is contained in the persistence context, it is returned from there.
   *
   * @param cls – entity class
   * @param id  - primary key
   * @return the found entity instance or null if the entity does not exist
   */
  public <T> T findById(Class<T> cls, Object id) {
    Field idField = findEntityIdField(cls);
    String idColumnName = resolveColumnName(idField);
    checkEntityIdType(cls, id);
    T cachedEntity = context.getEntity(cls, id);
    if (cachedEntity != null) {
      return cachedEntity;
    }
    List<T> searchResult = innerFindAllByFieldValue(cls, idColumnName, id);
    return searchResult.isEmpty() ? null : searchResult.get(0);
  }

  /**
   * Find by primary key. Search for entities of the specified class filtered by column. If the
   * entities contained in the persistence context, they returned from there.
   *
   * @param cls         – entity class
   * @param columnName  - column name
   * @param columnValue - column value
   * @return the found entities instance or empty list if such entities do not exist
   */
  public <T> List<T> findAllByColumn(Class<T> cls, String columnName, Object columnValue) {
    validateColumnName(cls, columnName);
    return innerFindAllByFieldValue(cls, columnName, columnValue);
  }


  /**
   * Perform an internal search for entities of the specified class filtered by a field value.
   *
   * @param <T>        the type parameter
   * @param cls        the entity class
   * @param fieldName  the field name to filter by
   * @param fieldValue the field value to filter by
   * @return the list of found entities or an empty list if no entities match the search criteria
   * @throws EntityQueryException if an error occurs during the search
   */
  private <T> List<T> innerFindAllByFieldValue(Class<T> cls, String fieldName, Object fieldValue) {
    String tableName = getEntityTableName(cls);
    List<Field> columnFields = getClassColumnFields(cls);

    String sql = SELECT_BY_FIELD_VALUE_QUERY.formatted(composeSelectBlockFromColumns(columnFields),
        tableName, fieldName);

    log.info("Bibernate: " + sql);  // todo make this print depend on property.
    List<T> result = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, fieldValue);
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        T entity = mapResult(resultSet, cls);
        result.add(entity);
      }
    } catch (SQLException e) {
      throw new EntityQueryException(
          "Could not read entity data from database for entity [%s] by field [%s]=%s"
              .formatted(cls, fieldName, fieldValue), e);
    }
    return result;
  }

  /**
   * Saves a given entity. Use the returned instance for further operations as the save operation
   * might have changed the entity instance completely.
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
    String saveSQL = INSERT_ENTITY_QUERY.formatted(tableName,
        composeSelectBlockFromColumns(columnFields), questionMarks);

    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(saveSQL,
            Statement.RETURN_GENERATED_KEYS)) {
      for (int i = 0; i < columnFields.size(); i++) {
        Field field = columnFields.get(i);
        field.setAccessible(true);
        statement.setObject(i + 1, field.get(entity));
      }
      log.trace("Save entity: [{}]", saveSQL);
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

  /**
   * Deletes an entity from the database using its ID. This method finds the entity's ID field,
   * constructs a DELETE SQL query, and executes it. The entity must not be null and must have a
   * non-null ID.
   *
   * @param <T>    the type of the entity
   * @param entity the entity to be deleted
   * @throws EntityQueryException     if there's an error during deletion or if the entity or its ID
   *                                  is null
   * @throws IllegalArgumentException if the entity is null
   */
  public <T> void delete(T entity) {
    requireNonNull(entity, "Entity should not be null.");
    if (!context.contains(entity)) {
      throw new EntityQueryException(
          "Entity [%s] could not be deleted because not found in the persistent context.".formatted(
              entity));
    }
    Class<?> cls = entity.getClass();
    String tableName = getEntityTableName(cls);
    Field idField = findEntityIdField(cls);
    idField.setAccessible(true);
    String deleteSql = DELETE_ENTITY_QUERY.formatted(tableName, idField.getName());
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(deleteSql)) {
      Object idObject = idField.get(entity);
      if (idObject == null) {
        throw new EntityIdIsNullException("Entity ID is null for [%s]".formatted(entity));
      }
      statement.setObject(1, idObject);
      log.trace("Delete entity: [{}]", deleteSql);
      int result = statement.executeUpdate();
      if (result != 1) {
        throw new EntityQueryException(
            "Could not delete entity to database for entity [%s]"
                .formatted(entity));
      }
      context.delete(entity);
    } catch (SQLException e) {
      throw new EntityQueryException(
          "Could not delete entity from the database for entity [%s]"
              .formatted(entity), e);
    } catch (IllegalAccessException e) {
      throw new EntityQueryException(
          "Could not get field [%s] from entity [%s]"
              .formatted(idField.getType().getSimpleName(), entity), e);
    }
  }

  // todo add logic for relation annotations - @OneToMany, @ManyToOne, @ManyToMany
  // todo add logic for relation annotations - @ManyToMany, @OneToOne

  /**
   * Maps the results from a ResultSet object to an entity object of the specified class and add the
   * entity to context. Recursively fetch all related entities and add them to context. Returns the
   * mapped entity object.
   *
   * @param resultSet - The ResultSet object containing the data to be mapped
   * @param cls       - The class of the entity object
   * @param <T>       - The type parameter representing the entity class
   * @return The mapped entity object
   * @throws EntityQueryException if there is an error during the mapping process
   */
  private <T> T mapResult(ResultSet resultSet, Class<T> cls) {
    List<Field> columnFields = getClassEntityFields(cls);

    try {
      T entity = cls.getConstructor().newInstance();
      for (Field field : columnFields) {
        field.setAccessible(true);

        if (isSimpleColumn(field)) {
          String columnName = resolveColumnName(field);
          field.set(entity, resultSet.getObject(columnName));
        }
      }
      context.manageEntity(entity);

      for (Field field : columnFields) {
        field.setAccessible(true);
        if (field.isAnnotationPresent(ManyToOne.class)) {
          mapManyToOneRelationship(resultSet, cls, field, entity);
        } else if (field.isAnnotationPresent(OneToMany.class)) {
          mapOneToManyRelationship(resultSet, cls, field, entity);
        }
      }
      return entity;
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

  private <T> void mapManyToOneRelationship(ResultSet resultSet, Class<T> cls, Field field,
      T entity) throws SQLException, IllegalAccessException {
    String joinColumnName = resolveColumnName(field);
    Field relatedEntityIdField = findEntityIdField(cls);
    Object relatedEntityId = relatedEntityIdField.getType()
        .cast(resultSet.getObject(joinColumnName));
    String relatedEntityIdColumnName = resolveColumnName(relatedEntityIdField);
    var relatedEntity = fetchRelatedEntity(field, relatedEntityIdColumnName, relatedEntityId);
    field.set(entity, relatedEntity);
  }

  private <T> void mapOneToManyRelationship(ResultSet resultSet, Class<T> cls, Field field,
      T entity) throws IllegalAccessException, SQLException {
    Class<?> relatedEntityType = getEntityCollectionElementType(field);
    String joinColumnName = getJoinColumnName(relatedEntityType, cls);
    Object id = extractIdFromResultSet(cls, resultSet);
    List<?> relatedEntities = innerFindAllByFieldValue(relatedEntityType, joinColumnName, id);
    Collection<Object> collection = getCollectionInstance(field);
    collection.addAll(relatedEntities);
    field.set(entity, collection);
  }

  private Object fetchRelatedEntity(Field field, String columnName, Object id) {
    var relatedEntity = context.getEntity(field.getType(), id);
    if (relatedEntity == null) {
      relatedEntity = innerFindAllByFieldValue(field.getType(), columnName, id).get(0);
      relatedEntity = context.manageEntity(relatedEntity);
    }
    return relatedEntity;
  }

  private Object extractIdFromResultSet(Class<?> cls, ResultSet resultSet) throws SQLException {
    Field idField = findEntityIdField(cls);
    String idColumnName = resolveColumnName(idField);
    return resultSet.getObject(idColumnName);
  }

  public <T> int executeUpdate(EntityKey<T> entityKey, Object... parameters) {
    String updateSql = prepareUpdateQuery(entityKey);
    log.trace("Update entity: [{}]", updateSql);
    try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
      setParameters(preparedStatement, entityKey.id(), parameters);
      return preparedStatement.executeUpdate();
    } catch (SQLException e) {
      throw new BibernateException("Failed to execute update query: [%s] with parameters %s"
          .formatted(updateSql, Arrays.toString(parameters)), e);
    }
  }

  public <T> String prepareUpdateQuery(EntityKey<T> entityKey) {
    Class<T> entityClass = entityKey.entityClass();
    String setUpdatedColumnsSql = EntityUtil.getEntityColumnNames(entityClass).stream()
        .map("%s = ?"::formatted)
        .collect(Collectors.joining(", "));
    String tableName = EntityUtil.getEntityTableName(entityClass);
    String primaryKeyName = EntityUtil.findEntityIdFieldName(entityClass);

    return UPDATE_SQL.formatted(tableName, setUpdatedColumnsSql, primaryKeyName);
  }

  public <T> void setParameters(PreparedStatement preparedStatement,
      Object primaryKey,
      Object... params) throws SQLException {
    validatePrimaryKey(primaryKey);

    int parameterIndex = 1;
    for (Object parameter : params) {
      preparedStatement.setObject(parameterIndex, parameter);
      parameterIndex++;
    }

    preparedStatement.setObject(parameterIndex, primaryKey);
  }

  private void validatePrimaryKey(Object primaryKey) {
    if (primaryKey == null) {
      throw new BibernateException("Primary key value must be passed for update query");
    }
  }

  private void checkEntityIdType(Class<?> entityClass, Object id) {
    Class<?> entityIdType = findEntityIdField(entityClass).getType();
    if (!entityIdType.equals(id.getClass())) {
      throw new BibernateException(
          "Mismatched types: Expected ID of type %s  but received ID of type %s".formatted(
              entityIdType.getSimpleName(), id.getClass().getSimpleName()));
    }
  }

  private Connection getConnection() {
    try {
      return dataSource.getConnection();
    } catch (SQLException e) {
      throw new BibernateException("Failed to acquire connection", e);
    }
  }
}
