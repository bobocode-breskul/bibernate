package com.breskul.bibernate.persistence.context;

import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.persistence.EntityKey;
import com.breskul.bibernate.persistence.context.snapshot.EntityPropertySnapshot;
import com.breskul.bibernate.persistence.context.snapshot.EntityRelationSnapshot;
import com.breskul.bibernate.util.EntityUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

/**
 * A persistence context that manages first-level caching of entities and their snapshots. This
 * class provides methods for retrieving, caching, and managing entity snapshots.
 */
public class PersistenceContext {

  private static final Logger log = LoggerFactory.getLogger(PersistenceContext.class);

  /**
   * The first-level cache for storing entities managed by session.
   */
  private final Map<EntityKey<?>, Object> firstLevelCache = new ConcurrentHashMap<>();

  /**
   * Map to store snapshots of entity properties.
   */
  private final Map<EntityKey<?>, List<EntityPropertySnapshot>> entitySnapshots = new ConcurrentHashMap<>();

  /**
   * Map to store snapshots of to-one relations.
   */
  private final Map<EntityKey<?>, List<EntityRelationSnapshot>> toOneRelationSnapshots = new ConcurrentHashMap<>();


  /**
   * Retrieves the entity associated with the specified entity class and ID.
   *
   * @param entityClass The class of the entity.
   * @param id          The ID of the entity.
   * @param <T>         The type of the entity.
   * @return The entity associated with the specified class and ID, or {@code null} if not found.
   */
  public <T> T getEntity(Class<T> entityClass, Object id) {
    return getEntity(EntityKey.of(entityClass, id));
  }

  /**
   * Retrieves the entity associated with the specified entity key.
   *
   * @param key The key representing the entity.
   * @param <T> The type of the entity.
   * @return The entity associated with the specified key, or {@code null} if not found.
   */
  public <T> T getEntity(EntityKey<T> key) {
    return key.entityClass().cast(firstLevelCache.get(key));
  }

  /**
   * Retrieves the set of entity keys stored in the first-level cache.
   *
   * @return The set of entity keys.
   */
  public Set<EntityKey<?>> getEntityKeys() {
    return firstLevelCache.keySet();
  }

  /**
   * Retrieves the snapshot of entity properties associated with the specified entity key.
   *
   * @param entityKey The key representing the entity.
   * @param <T>       The type of the entity.
   * @return The snapshot of entity properties, or an empty list if not found.
   */

  public <T> Object[] getEntitySnapshot(EntityKey<T> entityKey) {
    return entitySnapshots.get(entityKey).stream()
        .map(EntityPropertySnapshot::columnValue)
        .toArray();
  }

  /**
   * Retrieves the snapshot of entity properties associated with the specified entity key, including
   * property names.
   *
   * @param entityKey The key representing the entity.
   * @param <T>       The type of the entity.
   * @return The snapshot of entity properties, or an empty list if not found.
   */
  public <T> List<EntityPropertySnapshot> getEntityPropertySnapshot(EntityKey<T> entityKey) {
    return entitySnapshots.get(entityKey);
  }

  /**
   * Retrieves the snapshot of to-one relations associated with the specified entity key.
   *
   * @param entityKey The key representing the entity.
   * @param <T>       The type of the entity.
   * @return The snapshot of to-one relations, or an empty list if not found.
   */
  public <T> List<EntityRelationSnapshot> getToOneRelationSnapshot(
      EntityKey<T> entityKey) {
    return Optional.ofNullable(toOneRelationSnapshots.get(entityKey))
        .orElse(Collections.emptyList());
  }

  /**
   * Puts the specified entity into the first-level cache and takes its initial state snapshot
   *
   * @param entity The entity to be cached.
   * @param <T>    The type of the entity.
   * @return The cached entity.
   */
  public <T> T put(T entity) {
    log.debug("Caching entity: {}", entity);
    firstLevelCache.putIfAbsent(EntityKey.valueOf(entity), entity);
    takeSimpleSnapshot(entity);
    return entity;
  }

  /**
   * Checks if the specified entity exists in the first-level cache.
   *
   * @param entity The entity to be checked.
   * @param <T>    The type of the entity.
   * @return {@code true} if the entity exists in the cache, {@code false} otherwise.
   */
  public <T> boolean contains(T entity) {
    return firstLevelCache.containsKey(EntityKey.valueOf(entity));
  }

  /**
   * Checks if the entity associated with the specified key has changed by comparing the initial
   * snapshot and the current state in the first-level cache.
   *
   * @param entityKey The key representing the entity.
   * @return {@code true} if the entity has changed, {@code false} otherwise.
   */
  public boolean isDirty(EntityKey<?> entityKey) {
    return hasChanged(entityKey);
  }

  public <T> void delete(T entity) {
    EntityKey<T> key = EntityKey.valueOf(entity);
    firstLevelCache.remove(key);
    entitySnapshots.remove(key);
    toOneRelationSnapshots.remove(key);
  }

  /**
   * Clears the first-level cache and entity snapshots.
   */
  public void clear() {
    log.info("Clearing first-level cache and entity snapshots");
    firstLevelCache.clear();
    entitySnapshots.clear();
    toOneRelationSnapshots.clear();
  }

  /**
   * Takes a snapshot of the entity's columns without any relations.
   *
   * @param entity The entity to take a snapshot of.
   * @param <T>    The type of the entity.
   */
  public <T> void takeSimpleSnapshot(T entity) {
    EntityKey<T> entityKey = EntityKey.valueOf(entity);

    if (!entitySnapshots.containsKey(entityKey)) {
      List<EntityPropertySnapshot> values = EntityUtil.getEntitySimpleColumnValues(entity);
      entitySnapshots.put(entityKey, values);
    }
    log.debug("Simple snapshot taken for entity: {}", entity);
  }

  /**
   * Takes a snapshot of to-one relations of the specified entity.
   *
   * @param entity The entity to take a snapshot of.
   * @param <T>    The type of the entity.
   */
  public <T> void takeToOneRelationSnapshot(T entity) {
    EntityKey<T> entityKey = EntityKey.valueOf(entity);

    if (!toOneRelationSnapshots.containsKey(entityKey)) {
      List<EntityRelationSnapshot> entityToOneRelationValues =
          EntityUtil.getEntityToOneRelationValues(entity);
      toOneRelationSnapshots.put(entityKey, entityToOneRelationValues);
    }
    log.debug("Snapshot of to-one relations taken for entity: {}", entity);
  }

  /**
   * Checks if the entity associated with the specified key has changed by comparing the initial
   * snapshot and the current state in the first-level cache.
   *
   * @param entityKey The key representing the entity.
   * @param <T>       The type of the entity.
   * @return {@code true} if the entity has changed, {@code false} otherwise.
   */
  private <T> boolean hasChanged(EntityKey<T> entityKey) {
    Object[] currentEntityState = EntityUtil.getEntitySimpleColumnValues(
        getEntity(entityKey)).stream().map(EntityPropertySnapshot::columnValue).toArray();
    Object[] initialEntityState = getEntitySnapshot(entityKey);

    return !Arrays.equals(currentEntityState, initialEntityState)
        || isToOneRelationChanged(entityKey);
  }

  /**
   * Checks if the to-one relations of the entity associated with the specified key have changed.
   *
   * @param entityKey The key representing the entity.
   * @param <T>       The type of the entity.
   * @return {@code true} if the to-one relations have changed, {@code false} otherwise.
   */
  private <T> boolean isToOneRelationChanged(EntityKey<T> entityKey) {
    if (!EntityUtil.hasToOneRelations(entityKey.entityClass())) {
      return false;
    }

    List<EntityRelationSnapshot> currentToOneRelationState =
        EntityUtil.getEntityToOneRelationValues(getEntity(entityKey));
    List<EntityRelationSnapshot> entityToOneRelationSnapshot = getToOneRelationSnapshot(entityKey);
    return !currentToOneRelationState.equals(entityToOneRelationSnapshot);
  }
}

