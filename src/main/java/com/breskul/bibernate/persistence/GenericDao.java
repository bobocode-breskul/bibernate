package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.util.EntityUtil.composeSelectBlockFromColumns;
import static com.breskul.bibernate.util.EntityUtil.doQuery;
import static com.breskul.bibernate.util.EntityUtil.findEntityIdField;
import static com.breskul.bibernate.util.EntityUtil.getClassColumnFields;
import static com.breskul.bibernate.util.EntityUtil.getEntityTableName;
import static com.breskul.bibernate.util.EntityUtil.getIdColumnName;
import static com.breskul.bibernate.util.EntityUtil.validateIsEntity;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.List;

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

        String sql = SELECT_BY_ID_QUERY.formatted(composeSelectBlockFromColumns(columnFields),
                tableName, getIdColumnName(idField));

        return doQuery(sql, id, new EntityMapper<>(cls, columnFields), dataSource);
    }
}
