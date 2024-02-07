package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.util.AssociationUtil.getCollectionInstance;
import static com.breskul.bibernate.util.AssociationUtil.getLazyCollectionInstance;
import static com.breskul.bibernate.util.AssociationUtil.getLazyObjectProxy;
import static com.breskul.bibernate.util.EntityUtil.composeSelectBlockFromColumns;
import static com.breskul.bibernate.util.EntityUtil.findEntityIdField;
import static com.breskul.bibernate.util.EntityUtil.findEntityIdFieldName;
import static com.breskul.bibernate.util.EntityUtil.getClassColumnFields;
import static com.breskul.bibernate.util.EntityUtil.getClassEntityFields;
import static com.breskul.bibernate.util.EntityUtil.getEntityCollectionElementType;
import static com.breskul.bibernate.util.EntityUtil.getEntityId;
import static com.breskul.bibernate.util.EntityUtil.getEntityTableName;
import static com.breskul.bibernate.util.EntityUtil.getJoinColumnName;
import static com.breskul.bibernate.util.EntityUtil.isSimpleColumn;
import static com.breskul.bibernate.util.EntityUtil.resolveColumnName;
import static com.breskul.bibernate.util.EntityUtil.validateColumnName;
import static com.breskul.bibernate.util.ReflectionUtil.createEntityInstance;
import static com.breskul.bibernate.util.ReflectionUtil.writeFieldValue;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.generate;

import com.breskul.bibernate.annotation.FetchType;
import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.OneToMany;
import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.exception.EntityIdIsNullException;
import com.breskul.bibernate.exception.EntityIsNotManagedException;
import com.breskul.bibernate.exception.EntityQueryException;
import com.breskul.bibernate.persistence.context.PersistenceContext;
import com.breskul.bibernate.persistence.context.snapshot.EntityPropertySnapshot;
import com.breskul.bibernate.persistence.context.snapshot.EntityRelationSnapshot;
import com.breskul.bibernate.util.EntityUtil;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;


/**
 * Provides generic data access operations for entities, including retrieval by primary key or
 * column value, saving entities, and executing updates. Facilitates efficient and consistent
 * database interactions across various entity types.
 */
public class GenericDao {

  private static final Logger log = LoggerFactory.getLogger(GenericDao.class);

  // TODO: change to select '*'
  private static final String SELECT_BY_FIELD_VALUE_QUERY = "SELECT %s FROM %s WHERE %s = ?";
  private static final String UPDATE_SQL = "UPDATE %s SET %s WHERE %s = ?;";
  private static final String INSERT_ENTITY_QUERY = "INSERT INTO %s (%s) VALUES (%s);";
  private static final String DELETE_ENTITY_QUERY = "DELETE FROM %s WHERE %s = ?;";

  private final Connection connection;
  private final PersistenceContext context;

  public GenericDao(Connection connection, PersistenceContext context) {
    this.connection = connection;
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

    // todo make this print depend on property.
    log.info("Bibernate: {}", sql);
    List<T> result = new ArrayList<>();
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
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
    String sql = INSERT_ENTITY_QUERY.formatted(tableName,
        composeSelectBlockFromColumns(columnFields), questionMarks);

    try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      for (int i = 0; i < columnFields.size(); i++) {
        Field field = columnFields.get(i);
        field.setAccessible(true);
        statement.setObject(i + 1, field.get(entity));
      }
      log.trace("Save entity: [{}]", sql);
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
      throw new EntityIsNotManagedException(
          "Entity [%s] could not be deleted because not found in the persistent context.".formatted(
              entity));
    }
    Class<?> cls = entity.getClass();
    String tableName = getEntityTableName(cls);
    String deleteSql = DELETE_ENTITY_QUERY.formatted(tableName, findEntityIdFieldName(cls));
    try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
      Object idObject = getEntityId(entity);
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
    }
  }

  // todo add logic for relation annotations - @OneToMany, @ManyToOne, @ManyToMany
  /**
   * Executes an update query for the specified entity key with the given parameters. This method
   * dynamically determines whether to use a dynamic update query based on the entity class.
   *
   * @param <T>        the type of the entity
   * @param entityKey  the entity key representing the entity to update
   * @param parameters the parameters to be used in the update query
   * @return the number of rows affected by the update query
   * @throws BibernateException if an SQL exception occurs while executing the update query
   */
  // TODO: write integration test
  // TODO: 1. given entity without relations, when execute update then one row updated
  // TODO: 3. given entity with one ManyToOne relation, when execute
  public <T> int executeUpdate(EntityKey<T> entityKey, Object... parameters) {
    boolean isDynamicUpdate = EntityUtil.isDynamicUpdate(entityKey.entityClass());
    String updateSql =
        isDynamicUpdate ? prepareDynamicUpdateQuery(entityKey) : prepareUpdateQuery(entityKey);
    log.debug("Update entity: [{}]", updateSql);
    try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
      setParameters(preparedStatement, entityKey.id(), parameters);
      return preparedStatement.executeUpdate();
    } catch (SQLException e) {
      throw new EntityQueryException("Failed to execute update query: [%s] with parameters %s"
          .formatted(updateSql, Arrays.toString(parameters)), e);
    }
  }

  /**
   * Prepares an update query for the specified entity key. This method constructs an update SQL
   * query string to update all columns of the entity.
   *
   * @param <T>       the type of the entity
   * @param entityKey the entity key representing the entity to update
   * @return a standard update SQL query string
   */
  private <T> String prepareUpdateQuery(EntityKey<T> entityKey) {
    Class<T> entityClass = entityKey.entityClass();
    String setUpdatedColumnsSql = EntityUtil.getEntityColumnNames(entityClass).stream()
        .map("%s = ?"::formatted)
        .collect(Collectors.joining(", "));
    String tableName = EntityUtil.getEntityTableName(entityClass);
    String primaryKeyName = EntityUtil.findEntityIdFieldName(entityClass);
    return UPDATE_SQL.formatted(tableName, setUpdatedColumnsSql, primaryKeyName);
  }

  /**
   * Prepares a dynamic update query for the specified entity key. This method constructs an update
   * SQL query string only for columns that were actually updated  based on the differences between
   * the initial and current states of the entity.
   *
   * @param <T>       the type of the entity
   * @param entityKey the entity key representing the entity to update
   * @return a dynamic update SQL query string
   */
  private <T> String prepareDynamicUpdateQuery(EntityKey<T> entityKey) {
    Class<T> entityClass = entityKey.entityClass();

    // Obtain initial and current states for simple columns and to-one relation columns
    List<EntityPropertySnapshot> initialState = context.getEntityPropertySnapshot(entityKey);
    List<EntityPropertySnapshot> currentState =
        EntityUtil.getEntitySimpleColumnValues(context.getEntity(entityKey));
    currentState.removeAll(initialState);

    List<EntityRelationSnapshot> entityToOneRelationSnapshot =
        context.getToOneRelationSnapshot(entityKey);
    List<EntityRelationSnapshot> currentEntityToOneRelationValues =
        EntityUtil.getEntityToOneRelationValues(context.getEntity(entityKey));
    currentEntityToOneRelationValues.removeAll(entityToOneRelationSnapshot);

    // Extract column names for simple columns and to-one relation columns
    Stream<Object> simpleColumns = currentState.stream()
        .map(EntityPropertySnapshot::columnName);
    Stream<Object> toOneRelations = currentEntityToOneRelationValues.stream()
        .map(EntityRelationSnapshot::columnName);
    String setUpdatedColumnsSql = Stream.concat(simpleColumns, toOneRelations)
        .filter(Objects::nonNull)
        .map("%s = ?"::formatted)
        .collect(Collectors.joining(", "));

    // Construct the dynamic update SQL query string
    String tableName = EntityUtil.getEntityTableName(entityClass);
    String primaryKeyName = EntityUtil.findEntityIdFieldName(entityClass);
    return UPDATE_SQL.formatted(tableName, setUpdatedColumnsSql, primaryKeyName);
  }
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
      T entity = createEntityInstance(cls);
      for (Field field : columnFields) {
        field.setAccessible(true);

        if (isSimpleColumn(field)) {
          String columnName = resolveColumnName(field);
          writeFieldValue(field, entity, resultSet.getObject(columnName));
        }
      }
      context.put(entity);

      for (Field field : columnFields) {
        field.setAccessible(true);
        if (field.isAnnotationPresent(ManyToOne.class)) {
          mapManyToOneRelationship(resultSet, field, entity);
        } else if (field.isAnnotationPresent(OneToMany.class)) {
          mapOneToManyRelationship(resultSet, cls, field, entity);
        }
      }

      if (EntityUtil.hasToOneRelations(cls)) {
        context.takeToOneRelationSnapshot(entity);
      }

      return entity;
    } catch (SQLException e) {
      throw new EntityQueryException("Could not read single row data from database for entity [%s]"
          .formatted(cls), e);
    }
  }

  private <T> void mapManyToOneRelationship(ResultSet resultSet, Field field, T entity)
      throws SQLException {
    String joinColumnName = resolveColumnName(field);
    Field relatedEntityIdField = findEntityIdField(field.getType());
    Object relatedEntityId = resultSet.getObject(joinColumnName);
    String relatedEntityIdColumnName = resolveColumnName(relatedEntityIdField);
    writeFieldValue(field, entity,
        createAssocitatedObject(field, relatedEntityIdColumnName, relatedEntityId));
  }

  private <T> void mapOneToManyRelationship(ResultSet resultSet, Class<T> cls, Field field,
      T entity) throws SQLException {
    Class<?> relatedEntityType = getEntityCollectionElementType(field);
    String joinColumnName = getJoinColumnName(relatedEntityType, cls);
    Object id = extractIdFromResultSet(cls, resultSet);
    writeFieldValue(field, entity,
        createAssociatedCollection(field, relatedEntityType, joinColumnName, id));
  }

  private Collection<Object> createAssociatedCollection(Field field, Class<?> relatedEntityType,
      String joinColumnName, Object id) {
    FetchType fetchType = field.getAnnotation(OneToMany.class).fetch();
    log.debug(
        "Resolving [{}] collection for [{}.{}.{}] field by related column [{}] with value [{}]",
        fetchType, field.getDeclaringClass().getPackageName(),
        field.getDeclaringClass().getSimpleName(), field.getName(), joinColumnName, id);
    return switch (fetchType) {
      case EAGER -> getCollectionInstance(field,
          innerFindAllByFieldValue(relatedEntityType, joinColumnName, id));
      case LAZY -> getLazyCollectionInstance(field,
          () -> this.innerFindAllByFieldValue(relatedEntityType, joinColumnName, id));
    };
  }

  private Object createAssocitatedObject(Field field, String relatedEntityIdColumnName,
      Object relatedEntityId) {
    FetchType fetchType = field.getAnnotation(ManyToOne.class).fetch();
    log.debug(
        "Resolving [{}] parent object for [{}.{}.{}] field by related column [{}] with value [{}]",
        fetchType, field.getDeclaringClass().getPackageName(),
        field.getDeclaringClass().getSimpleName(), field.getName(), relatedEntityIdColumnName,
        relatedEntityId);
    return switch (fetchType) {
      case EAGER -> fetchRelatedEntity(field, relatedEntityIdColumnName, relatedEntityId);
      case LAZY -> getLazyObjectProxy(field,
          () -> fetchRelatedEntity(field, relatedEntityIdColumnName, relatedEntityId));
    };
  }

  private Object fetchRelatedEntity(Field field, String columnName, Object id) {
    var relatedEntity = context.getEntity(field.getType(), id);
    if (relatedEntity == null) {
      relatedEntity = innerFindAllByFieldValue(field.getType(), columnName, id).get(0);
      relatedEntity = context.put(relatedEntity);
    }
    return relatedEntity;
  }

  private Object extractIdFromResultSet(Class<?> cls, ResultSet resultSet) throws SQLException {
    Field idField = findEntityIdField(cls);
    String idColumnName = resolveColumnName(idField);
    return resultSet.getObject(idColumnName);
  }

  private <T> void setParameters(PreparedStatement preparedStatement,
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
}
