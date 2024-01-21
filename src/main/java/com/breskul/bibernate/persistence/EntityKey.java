package com.breskul.bibernate.persistence;

public class EntityKey {
    private final Class<?> entityClass;

    private final Object id;

    public EntityKey(Class<?> entityClass, Object id) {
        this.entityClass = entityClass;
        this.id = id;
    }
}
