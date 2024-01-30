package com.breskul.bibernate.persistence;

import com.breskul.bibernate.util.EntityUtil;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//TODO add docs
//TODO add logs

public class PersistenceContext {

  private final Map<EntityKey<?>, Object> firstLevelCache = new ConcurrentHashMap<>(); //TODO Think what better HashMap vs ConcurrentHashMap
  private final Map<EntityKey<?>, Object[]> entitySnapshots = new ConcurrentHashMap<>(); // for dirty checking

  public <T> T getEntity(Class<T> entityClass, Object id) {
    return getEntity(EntityKey.of(entityClass, id));
  }

  public <T> T getEntity(EntityKey<T> key) {
    return key.entityClass().cast(firstLevelCache.get(key));
  }

  public Set<EntityKey<?>> getEntityKeys() {
    return firstLevelCache.keySet();
  }

  public <T> Object[] getEntitySnapshot(EntityKey<T> entityKey) {
    return entitySnapshots.get(entityKey);
  }

  public <T> void put(T entity) {
    firstLevelCache.putIfAbsent(EntityKey.valueOf(entity), entity);
    takeSnapshot(entity);
  }

  public <T> boolean contains(T entity) {
    var key = EntityKey.valueOf(entity);
    return firstLevelCache.containsKey(key);
  }

  public <T> boolean isDirty(T entity) {
    return false;
  }

  public void clear() {
    firstLevelCache.clear();
    entitySnapshots.clear();
  }

  private <T> void takeSnapshot(T entity) {
    EntityKey<T> entityKey = EntityKey.valueOf(entity);
    if (!entitySnapshots.containsKey(entityKey)) {
      Object[] values = EntityUtil.getEntityColumnValues(entity);
      entitySnapshots.put(entityKey, values);
    }
  }

}

