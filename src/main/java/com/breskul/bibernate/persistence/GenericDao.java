package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.util.EntityUtil.composeSelectBlockFromColumns;
import static com.breskul.bibernate.util.EntityUtil.findEntityIdField;
import static com.breskul.bibernate.util.EntityUtil.getClassColumnFields;
import static com.breskul.bibernate.util.EntityUtil.getClassEntityFields;
import static com.breskul.bibernate.util.EntityUtil.getCollectionInstance;
import static com.breskul.bibernate.util.EntityUtil.getEntityCollectionElementType;
import static com.breskul.bibernate.util.EntityUtil.getEntityTableName;
import static com.breskul.bibernate.util.EntityUtil.getJoinColumnName;
import static com.breskul.bibernate.util.EntityUtil.isPrimitiveColumn;
import static com.breskul.bibernate.util.EntityUtil.resolveColumnName;
import static com.breskul.bibernate.util.EntityUtil.validateColumnName;

import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.OneToMany;
import com.breskul.bibernate.exception.EntityQueryException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;

public class GenericDao {

  PersistenceContext context;

  // TODO: change to select '*'
  public static final String SELECT_BY_FIELD_VALUE_QUERY = "SELECT %s FROM %s WHERE %s = ?";

  private final DataSource dataSource;

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
    T cachedEntity = context.findEntity(cls, id);
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

    System.out.println("Bibirnate: " + sql);  // todo make this print depend on property.
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

        if (isPrimitiveColumn(field)) {
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
    var relatedEntity = context.findEntity(field.getType(), id);
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
}
