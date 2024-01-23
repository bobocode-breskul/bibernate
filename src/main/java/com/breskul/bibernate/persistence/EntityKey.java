package com.breskul.bibernate.persistence;

public class EntityKey {
    private final Class<?> entityClass;

    private final Object id;

    public EntityKey(Class<?> entityClass, Object id) {
        this.entityClass = entityClass;
        this.id = id;
    }

    public static <T> EntityKey valueOf(T entity) {
        Object id = null; //todo impl getId
        var entityType = entity.getClass();
        return new EntityKey(entityType, id);
    }
}
