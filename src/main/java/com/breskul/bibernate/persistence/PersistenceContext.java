package com.breskul.bibernate.persistence;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceContext {

    private final Map<EntityKey, Object> firstLevelCache = new ConcurrentHashMap<>();
    private final Map<EntityKey, Object[]> entitySnapshots = new ConcurrentHashMap<>(); // for dirty checking
    @SuppressWarnings("unchecked")
    public <T> T manageEntity(T entity) {
        var key = EntityKey.valueOf(entity);
        var cachedEntity = firstLevelCache.get(key);
        if (cachedEntity != null) {
            return (T) cachedEntity;
        } else {
            return addEntity(entity);
        }
    }

    //todo impl
    public <T> T getEntity(EntityKey key) {
        Object entity = firstLevelCache.get(key);
        return null;
    }

    public <T> T addEntity(T entity) {
        var key = EntityKey.valueOf(entity);
        firstLevelCache.put(key, entity);
        return entity;
    }

    public <T> boolean contains(T entity) {
        var key = EntityKey.valueOf(entity);
        return firstLevelCache.containsKey(key);
    }

    public void clear() {
        firstLevelCache.clear();
        entitySnapshots.clear();
    }

}
