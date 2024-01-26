package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.util.EntityUtil.composeSelectBlockFromColumns;
import static com.breskul.bibernate.util.EntityUtil.findEntityIdField;
import static com.breskul.bibernate.util.EntityUtil.getClassColumnFields;
import static com.breskul.bibernate.util.EntityUtil.getColumnName;
import static com.breskul.bibernate.util.EntityUtil.getEntityId;
import static com.breskul.bibernate.util.EntityUtil.getEntityIdType;
import static com.breskul.bibernate.util.EntityUtil.getEntityTableName;
import static com.breskul.bibernate.util.EntityUtil.getJoinColumnName;
import static com.breskul.bibernate.util.EntityUtil.isPrimitiveColumn;
import static com.breskul.bibernate.util.EntityUtil.resolveColumnName;
import static com.breskul.bibernate.util.EntityUtil.validateIsEntity;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

public class GenericDao {

    // TODO: change to select '*'
    public static final String SELECT_BY_FIELD_VALUE_QUERY = "SELECT %s FROM %s WHERE %s = ?";

    private final DataSource dataSource;
    public GenericDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    Map<String, RequestCache> requestCacheStorage = new ConcurrentHashMap<>();


    public <T> T findById(Class<T> cls, Object id) {

        Field idField = findEntityIdField(cls);
        String idColumnName = resolveColumnName(idField);
      String requestId = UUID.randomUUID().toString();
        List<T> searchResult = innerFindByFieldValue(cls, idColumnName, id, requestId);
        requestCacheStorage.remove(requestId);
        return searchResult.isEmpty() ? null : searchResult.get(0);
    }

  public <T> List<T> findByFieldValue(Class<T> cls, String fieldName, Object fieldValue) {
    validateIsEntity(cls);
    String requestId = UUID.randomUUID().toString();
    var result = innerFindByFieldValue(cls, fieldName, fieldValue, requestId);
    requestCacheStorage.remove(requestId);
    return result;
  }

  private  <T> List<T> innerFindByFieldValue(Class<T> cls, String fieldName, Object fieldValue,
    String requestId) {
    RequestCache<T> requestCache = requestCacheStorage.getOrDefault(requestId, new RequestCache<T>());

    String tableName = getEntityTableName(cls);
    List<Field> columnFields = getClassColumnFields(cls);

    String sql = SELECT_BY_FIELD_VALUE_QUERY.formatted(composeSelectBlockFromColumns(columnFields),
      tableName, fieldName);
    List<T> result = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, fieldValue);
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        T entity = mapResult(resultSet, cls, requestId);
        result.add(entity);
        requestCache.addEntity(getEntityId(entity), entity);
      }
      requestCacheStorage.put(requestId, requestCache);
    } catch (SQLException e) {
      throw new EntityQueryException(
        "Could not read entity data from database for entity [%s] by field [%s]=%s"
          .formatted(cls, fieldName, fieldValue), e);
    }
    return result;
  }



    // todo add logic for relation annotations - @OneToMany, @ManyToOne, @ManyToMany
    private  <T> T mapResult(ResultSet resultSet, Class<T> cls, String requestId) {
      List<Field> columnFields = getClassColumnFields(cls);
      try {
        T entity = cls.getConstructor().newInstance();
        for (Field field : columnFields) {
            field.setAccessible(true);
          if (isPrimitiveColumn(field)) {
            String columnName = getColumnName(field);
            field.set(entity, resultSet.getObject(columnName));
          } else if (field.isAnnotationPresent(ManyToOne.class)) {
            String joinColumnName = getJoinColumnName(field);
            Field idField = findEntityIdField(cls);
            var id = idField.getType().cast(resultSet.getObject(joinColumnName));
            var relatedEntity = requestCacheStorage.get(requestId).getEntity(id);
            if (relatedEntity == null) {
              String idColumnName = resolveColumnName(idField);
              relatedEntity = innerFindByFieldValue(field.getType(), idColumnName, id, requestId);
            }
            field.set(entity, relatedEntity);
          } else if (field.isAnnotationPresent(OneToMany.class)) {
            // todo implement
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


    private static class RequestCache<T> {
      final Map<Object, T> idToEntityMap = new HashMap<>();

      void addEntity(Object id, T entity) {
        idToEntityMap.put(id, entity);
      }

      T getEntity(Object id) {
        return idToEntityMap.get(id);
      }
    }
}
