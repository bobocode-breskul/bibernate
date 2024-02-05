package com.breskul.bibernate.persistence;

import com.breskul.bibernate.util.EntityUtil;
import com.breskul.bibernate.util.Pair;
import com.breskul.bibernate.util.Triple;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//TODO add docs
//TODO add logs

public class PersistenceContext {

  private final Map<EntityKey<?>, Object> firstLevelCache = new ConcurrentHashMap<>();
  private final Map<EntityKey<?>, List<Pair<String, Object>>> entitySnapshots = new ConcurrentHashMap<>();
  private final Map<EntityKey<?>, List<Triple<? extends Class<?>, String, Object>>> toOneRelationSnapshots = new ConcurrentHashMap<>();

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
    return entitySnapshots.get(entityKey).stream()
        .map(Pair::right)
        .toArray();
  }

  public <T> List<Pair<String, Object>> getEntitySnapshotWithColumnName(EntityKey<T> entityKey) {
    return entitySnapshots.get(entityKey);
  }

  public <T> List<Triple<? extends Class<?>, String, Object>> getToOneRelationSnapshot(
      EntityKey<T> entityKey) {
    return Optional.ofNullable(toOneRelationSnapshots.get(entityKey))
        .orElse(Collections.emptyList());
  }

  public <T> T put(T entity) {
    firstLevelCache.putIfAbsent(EntityKey.valueOf(entity), entity);
    takeSimpleSnapshot(entity);
    return entity;
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


  public <T> void takeSimpleSnapshot(T entity) {
    EntityKey<T> entityKey = EntityKey.valueOf(entity);

    if (!entitySnapshots.containsKey(entityKey)) {
      List<Pair<String, Object>> values = EntityUtil.getEntitySimpleColumnValues(entity);
      entitySnapshots.put(entityKey, values);
    }
  }

  public <T> void takeToOneRelationSnapshot(T entity) {
    EntityKey<T> entityKey = EntityKey.valueOf(entity);

    if (!toOneRelationSnapshots.containsKey(entityKey)) {
      List<Triple<? extends Class<?>, String, Object>> entityToOneRelationValues =
          EntityUtil.getEntityToOneRelationValues(entity);
      toOneRelationSnapshots.put(entityKey, entityToOneRelationValues);
    }
  }
}

