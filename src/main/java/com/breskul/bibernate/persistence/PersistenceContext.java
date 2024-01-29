package com.breskul.bibernate.persistence;

import com.breskul.bibernate.util.EntityUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//TODO add docs
//TODO add logs

public class PersistenceContext {

  private final Map<EntityKey, Object> firstLevelCache = new ConcurrentHashMap<>(); //TODO Think what better HashMap vs ConcurrentHashMap
  private final Map<EntityKey, Object[]> entitySnapshots = new ConcurrentHashMap<>(); // for dirty checking

  // persist
  // todo: docs
  // todo: tests
  public <T> T manageEntity(T entity) {
//    manage(entity);
    var key = EntityKey.valueOf(entity);
    if (firstLevelCache.containsKey(key)) {
      return (T) firstLevelCache.get(key);
    }
    firstLevelCache.put(EntityKey.valueOf(entity), entity);
    return entity;
  }

  private <T> void manage(T entity) {
    // todo: implement manage (persist)
  }

  // todo: docs
  // todo: tests
  public <T> T findEntity(Class<T> cls, Object id) {
    return cls.cast(firstLevelCache.computeIfAbsent(new EntityKey(cls, id), this::find));
  }

  private <T> T find(EntityKey entityKey) {
    // todo: implement find by id
    return null;
  }

  // todo: docs
  // todo: tests
//  @SuppressWarnings()
  public <T> T mergeEntity(T entity) {
    // todo: check for id
    var key = EntityKey.valueOf(entity);
    if (firstLevelCache.containsKey(key)) {
      T cachedEntity = (T) firstLevelCache.get(key);
      if (!isDirty(entity)) {
        merge(entity, cachedEntity);
      }
      return cachedEntity;
    }
    T newEntity = find(key);
    firstLevelCache.put(key, newEntity);
    return newEntity;
  }

  private <T> void merge(T entity, T cachedEntity) {
    // todo: merge field from entity to cachedEntity using reflection
  }

  private <T> boolean isDirty(T entity) {
    // todo: implement
    return false;
  }

  // todo: docs
  // todo: test
  public <T> boolean contains(T entity) {
    var key = EntityKey.valueOf(entity);
    return firstLevelCache.containsKey(key);
  }

  public void clear() {
    firstLevelCache.clear();
    entitySnapshots.clear();
  }

}
