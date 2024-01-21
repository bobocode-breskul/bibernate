package com.breskul.bibernate.persistence;


import com.breskul.bibernate.exception.EntityQueryException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EntityMapper<T> {

    private final Class<T> entityCls;
    private final List<Field> columnFields;

    public EntityMapper(Class<T> entityCls, List<Field> columnFields) {
        this.entityCls = entityCls;
        this.columnFields = columnFields;
    }

    public T mapResult(ResultSet resultSet) {
        try {
            T t = entityCls.getConstructor().newInstance();
            for (int i = 0; i < columnFields.size(); i++) {
                Field field = columnFields.get(i);
                field.setAccessible(true);
                field.set(t, resultSet.getObject(i + 1));
            }
            return t;
        } catch (IllegalAccessException e) {
            throw new EntityQueryException("Entity [%s] should have public no-args constructor".formatted(entityCls), e);
        } catch (IllegalArgumentException | NoSuchMethodException e) {
            throw new EntityQueryException("Entity [%s] should have constructor without parameters".formatted(entityCls), e);
        } catch (InstantiationException e) {
            throw new EntityQueryException("Entity [%s] should be non-abstract class".formatted(entityCls), e);
        } catch (InvocationTargetException e) {
            throw new EntityQueryException("Could not create instance of target entity [%s]".formatted(entityCls), e);
        } catch (SQLException e) {
            throw new EntityQueryException("Could not read single row data from database for entity [%s]"
                    .formatted(entityCls), e);
        }
    }

    public Class<T> getEntityCls() {
        return entityCls;
    }
}
