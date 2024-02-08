package com.breskul.bibernate.query.hql;

import static com.breskul.bibernate.util.EntityUtil.getClassEntityFields;
import static com.breskul.bibernate.util.EntityUtil.isSimpleColumn;
import static com.breskul.bibernate.util.EntityUtil.resolveColumnName;
import static com.breskul.bibernate.util.ReflectionUtil.createEntityInstance;
import static com.breskul.bibernate.util.ReflectionUtil.writeFieldValue;

import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.OneToMany;
import com.breskul.bibernate.exception.EntityQueryException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TypedQuery<T> {
  private final String hql;

  private final Class<T> entityClass;

  private final Connection connection;

  public TypedQuery(String hql, Class<T> entityClass, Connection connection) {
    this.hql = hql;
    this.entityClass = entityClass;
    this.connection = connection;
  }

  public List<T> getResultList() {
    String sql = hqlToSql(hql);
    List<T> result = new ArrayList<>();
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        T entity = mapResult(resultSet, entityClass);
        result.add(entity);
      }
    } catch (SQLException e) {
//      throw new EntityQueryException(
//          "Could not read entity data from database for entity [%s] by field [%s]=%s"
//              .formatted(cls, fieldName, fieldValue), e);
    }
    return result;
  }

  // todo : implement method
  private String hqlToSql(String hql) {
    // from Person p where age > 30 and p.id < 3
    // select p from Person p where age > 30 and p.id < 3
    return null;
  }

  public T getSingleResult() {
    return getResultList().get(0);
  }

  private T mapResult(ResultSet resultSet, Class<T> cls) {
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
//      context.manageEntity(entity);

      for (Field field : columnFields) {
        field.setAccessible(true);
        if (field.isAnnotationPresent(ManyToOne.class)) {
//          mapManyToOneRelationship(resultSet, field, entity);
        } else if (field.isAnnotationPresent(OneToMany.class)) {
//          mapOneToManyRelationship(resultSet, cls, field, entity);
        }
      }
      return entity;
    } catch (SQLException e) {
      throw new EntityQueryException("Could not read single row data from database for entity [%s]"
          .formatted(cls), e);
    }
  }
}
