package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.util.EntityUtil.composeSelectBlockFromColumns;
import static com.breskul.bibernate.util.EntityUtil.findEntityIdField;
import static com.breskul.bibernate.util.EntityUtil.getClassColumnFields;
import static com.breskul.bibernate.util.EntityUtil.getEntityTableName;
import static com.breskul.bibernate.util.EntityUtil.resolveColumnName;
import static com.breskul.bibernate.util.EntityUtil.validateIsEntity;

import com.breskul.bibernate.exception.EntityQueryException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

public class GenericDao {

    // TODO: change to select '*'
    public static final String SELECT_BY_ID_QUERY = "SELECT %s FROM %s WHERE %s = ?";

    private final DataSource dataSource;
    public GenericDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public <T> T findById(Class<T> cls, Object id) {
        validateIsEntity(cls);

        String tableName = getEntityTableName(cls);
        List<Field> columnFields = getClassColumnFields(cls);
        Field idField = findEntityIdField(columnFields);
        String idColumnName = resolveColumnName(idField);

        String sql = SELECT_BY_ID_QUERY.formatted(composeSelectBlockFromColumns(columnFields),
                tableName, idColumnName);

        try (Connection connection = dataSource.getConnection();
          PreparedStatement statement = connection.prepareStatement(sql)) {
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

    // todo add logic for relation annotations - @OneToMany, @ManyToOne, @ManyToMany
    private  <T> T mapResult(ResultSet resultSet, Class<T> cls) {
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
}
