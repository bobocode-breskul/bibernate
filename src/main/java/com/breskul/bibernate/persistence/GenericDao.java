package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.util.EntityUtil.composeSelectBlockFromColumns;
import static com.breskul.bibernate.util.EntityUtil.findEntityIdField;
import static com.breskul.bibernate.util.EntityUtil.getClassColumnFields;
import static com.breskul.bibernate.util.EntityUtil.getEntityTableName;
import static com.breskul.bibernate.util.EntityUtil.getIdColumnName;
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
}
