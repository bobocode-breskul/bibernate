package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.util.EntityUtil.composeSelectBlockFromColumns;
import static com.breskul.bibernate.util.EntityUtil.findEntityIdField;
import static com.breskul.bibernate.util.EntityUtil.getClassColumnFields;
import static com.breskul.bibernate.util.EntityUtil.getClassEntityFields;
import static com.breskul.bibernate.util.EntityUtil.getCollectionInstance;
import static com.breskul.bibernate.util.EntityUtil.getEntityCollectionElementType;
import static com.breskul.bibernate.util.EntityUtil.getEntityId;
import static com.breskul.bibernate.util.EntityUtil.getEntityTableName;
import static com.breskul.bibernate.util.EntityUtil.getJoinColumnName;
import static com.breskul.bibernate.util.EntityUtil.isPrimitiveColumn;
import static com.breskul.bibernate.util.EntityUtil.resolveColumnName;
import static com.breskul.bibernate.util.EntityUtil.validateIsEntity;

import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.OneToMany;
import com.breskul.bibernate.collection.LazyList;
import com.breskul.bibernate.exception.EntityQueryException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import lombok.EqualsAndHashCode;

public class GenericDao {

  PersistenceContext context ;

    // TODO: change to select '*'
    public static final String SELECT_BY_FIELD_VALUE_QUERY = "SELECT %s FROM %s WHERE %s = ?";

    private final DataSource dataSource;
    public GenericDao(DataSource dataSource, PersistenceContext context) {
        this.dataSource = dataSource;
      this.context = context;
    }

    Map<String, RequestCache> requestCacheStorage = new ConcurrentHashMap<>();


  public <T> T findById(Class<T> cls, Object id) {
    Field idField = findEntityIdField(cls);
    String idColumnName = resolveColumnName(idField);
    String requestId = UUID.randomUUID().toString();
    List<T> searchResult = innerFindAllByFieldValue(cls, idColumnName, id, requestId);
    requestCacheStorage.remove(requestId);
    return searchResult.isEmpty() ? null : searchResult.get(0);
  }

  public <T> List<T> findAllByField(Class<T> cls, String fieldName, Object fieldValue) {
    validateIsEntity(cls);
    String requestId = UUID.randomUUID().toString();
    var result = innerFindAllByFieldValue(cls, fieldName, fieldValue, requestId);
    requestCacheStorage.remove(requestId);
    return result;
  }

  private  <T> List<T> innerFindAllByFieldValue(Class<T> cls, String fieldName, Object fieldValue,
    String requestId) {
    requestCacheStorage.putIfAbsent(requestId, new RequestCache());

    String tableName = getEntityTableName(cls);
    List<Field> columnFields = getClassColumnFields(cls);

    String sql = SELECT_BY_FIELD_VALUE_QUERY.formatted(composeSelectBlockFromColumns(columnFields),
      tableName, fieldName);

    System.out.println(sql);
    List<T> result = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, fieldValue);
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        T entity = mapResult(resultSet, cls, requestId);
        result.add(entity);
      }
    } catch (SQLException e) {
      throw new EntityQueryException(
        "Could not read entity data from database for entity [%s] by field [%s]=%s"
          .formatted(cls, fieldName, fieldValue), e);
    }
    return result;
  }



    // todo add logic for relation annotations - @OneToMany, @ManyToOne, @ManyToMany
    private  <T> T mapResult(ResultSet resultSet, Class<T> cls, String requestId) {
      List<Field> columnFields = getClassEntityFields(cls);

      try {
        Field idField = findEntityIdField(cls);
        String idName = resolveColumnName(idField);
        Object id = idField.getType().cast(resultSet.getObject(idName));
        requestCacheStorage.get(requestId).addRequest(RequestKey.of(cls, idName, id));

        T entity = cls.getConstructor().newInstance();
        for (Field field : columnFields) {
            field.setAccessible(true);

         // todo round all links
          if (isPrimitiveColumn(field)) {
            String columnName = resolveColumnName(field);
            field.set(entity, resultSet.getObject(columnName));
          } else if (field.isAnnotationPresent(ManyToOne.class)) {
            String joinColumnName = resolveColumnName(field);
            Field relatedEntityIdField = findEntityIdField(cls);
            Object relatedEntityId = relatedEntityIdField.getType().cast(resultSet.getObject(joinColumnName));
            String relatedEntityIdColumnName = resolveColumnName(relatedEntityIdField);
            boolean isDuplicatedRequest = requestCacheStorage.get(requestId)
              .isDuplicatedRequest(RequestKey.of(field.getType(), relatedEntityIdColumnName, relatedEntityId));
            if (isDuplicatedRequest) {
              continue; // stops infinity recursion.
            }

            var relatedEntity = fetchRelatedEntity(field, relatedEntityIdColumnName, relatedEntityId, requestId);
            field.set(entity, relatedEntity);
          } else if (field.isAnnotationPresent(OneToMany.class)) {
            // todo merge to context fetched entities and set merged entities.
            Class<?> relatedEntityType = getEntityCollectionElementType(field);
            String joinColumnName = getJoinColumnName(relatedEntityType, cls);

            var relatedEntities = innerFindAllByFieldValue(
              relatedEntityType, joinColumnName, id, requestId
            );
            field.set(entity, relatedEntities);
          }
        }
        return entity;
      } catch (IllegalAccessException e) {
          throw new EntityQueryException("Entity [%s] should have public no-args constructor".formatted(cls), e);
      } catch (IllegalArgumentException | NoSuchMethodException e) {
          throw new EntityQueryException("Entity [%s] should have constructor without parameters".formatted(cls), e);
      } catch (InstantiationException e) {
          throw new EntityQueryException("Entity [%s] should be non-abstract class".formatted(cls), e);
      } catch (InvocationTargetException e) {
          throw new EntityQueryException("Could not create instance of target entity [%s]".formatted(cls), e);
      } catch (SQLException e) {
          throw new EntityQueryException("Could not read single row data from database for entity [%s]"
            .formatted(cls), e);
      }
    }

    private Object fetchRelatedEntity(Field field, String columnName, Object id, String requestId) {
      var relatedEntity = context.findEntity(field.getType(), id);
      if (relatedEntity == null) {
        requestCacheStorage.get(requestId)
          .addRequest(RequestKey.of(field.getType(), columnName, id));
        relatedEntity = innerFindAllByFieldValue(field.getType(), columnName, id, requestId).get(0);
        context.manageEntity(relatedEntity);
      }
      return relatedEntity;
    }

    private Object extractIdFromResultSet(Class<?> cls, ResultSet resultSet) throws SQLException {
      Field idField = findEntityIdField(cls);
      String idColumnName = resolveColumnName(idField);
      return resultSet.getObject(idColumnName);
    }

    private static class RequestCache {
      final Set<RequestKey> requests = new HashSet<>();

      boolean isDuplicatedRequest(RequestKey key) {
        return !requests.add(key);
      }

      void addRequest(RequestKey key) {
        requests.add(key);
      }
    }

    @EqualsAndHashCode
    private static class RequestKey {
      Class<?> entityType;
      String fieldName;
      Object entityId;

      public RequestKey(Class<?> entityType, String fieldName, Object id) {
        this.entityType = entityType;
        this.fieldName = fieldName;
        this.entityId = id;
      }


      static RequestKey of(Class<?> entityType, String searchField, Object fieldValue) {
        return new RequestKey(entityType, searchField, fieldValue);
      }
    }
}
