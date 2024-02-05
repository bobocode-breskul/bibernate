package com.breskul.bibernate.persistence;

import com.breskul.bibernate.util.EntityUtil;
import com.breskul.bibernate.util.Pair;
import com.breskul.bibernate.util.Triple;
import jakarta.persistence.EntityTransaction;
import java.util.Arrays;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: javadoc
public class Session implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(Session.class);
  private final GenericDao genericDao;
  private final PersistenceContext persistenceContext;
  private final Queue<Action> actionQueue = new PriorityQueue<>();
  private EntityTransaction transaction; // todo: get rid of jakarta api

  public Session(DataSource dataSource) {
    this.persistenceContext = new PersistenceContext();
    this.genericDao = new GenericDao(dataSource, persistenceContext);

  }

  public <T> T findById(Class<T> entityClass, Object id) {
    return Optional.ofNullable(persistenceContext.getEntity(entityClass, id))
        .orElseGet(() -> find(EntityKey.of(entityClass, id)));
  }

  private <T> T find(EntityKey<? extends T> entityKey) {
    T entity = genericDao.findById(entityKey.entityClass(), entityKey.id());
    persistenceContext.put(entity);
    return entity;
  }

  // todo: docs
  // todo: tests
  public <T> T mergeEntity(T entity) {
    var key = EntityKey.valueOf(entity);
    if (persistenceContext.contains(key)) {
      T cachedEntity = persistenceContext.getEntity(key);
      if (!persistenceContext.isDirty(entity)) {
        merge(entity, cachedEntity);
      }
      return cachedEntity;
    }
    T newEntity = find(key);
    persistenceContext.put(newEntity);
    return newEntity;
  }

  private <T> T merge(T entity, T cachedEntity) {
    // todo: update
    return entity;
  }

  public <T> void manageEntity(T entity) {
    persistenceContext.put(entity);
  }

  // TODO add test

  /**
   * Make an instance managed and persistent.
   *
   * @param entity entity instance
   */
  public <T> void persist(T entity) {
    T savedEntity = genericDao.save(entity);
    persistenceContext.put(savedEntity);
  }

  @Override
  public void close() {
    performDirtyChecking();
    // todo: transaction commit/rollback
    persistenceContext.clear();
    actionQueue.clear();
  }

  private void performDirtyChecking() {
    persistenceContext.getEntityKeys().stream()
        .filter(this::hasChanged)
        .peek(entityKey -> log.trace("Found not flushed changes in the cache"))
        .forEach(this::flushChanges);
  }

  private <T> boolean hasChanged(EntityKey<T> entityKey) {
    Object[] currentEntityState = EntityUtil.getEntitySimpleColumnValues(
        persistenceContext.getEntity(entityKey)).stream().map(Pair::right).toArray();
    Object[] initialEntityState = persistenceContext.getEntitySnapshot(entityKey);

    return !Arrays.equals(currentEntityState, initialEntityState) || isToOneRelationChanged(
        entityKey);
  }

  private <T> boolean isToOneRelationChanged(EntityKey<T> entityKey) {
    if (EntityUtil.hasToOneRelations(entityKey.entityClass())) {
      var currentToOneRelationState =
          EntityUtil.getEntityToOneRelationValues(persistenceContext.getEntity(entityKey));
      var entityToOneRelationSnapshot = persistenceContext.getToOneRelationSnapshot(entityKey);
      return !currentToOneRelationState.equals(entityToOneRelationSnapshot);
    }
    return false;
  }

  // TODO: reformat code
  // TODO: write javadoc for all changes
  // TODO: cover all changes with tests
  private <T> void flushChanges(EntityKey<T> entityKey) {
    T updatedEntity = persistenceContext.getEntity(entityKey);
    Object[] parameters = EntityUtil.getEntityColumnValues(updatedEntity);
    if (EntityUtil.isDynamicUpdate(entityKey.entityClass())) {
      parameters = prepareDynamicParameters(entityKey, updatedEntity);
    }
    genericDao.executeUpdate(entityKey, parameters);
  }

  private <T> Object[] prepareDynamicParameters(EntityKey<T> entityKey, T updatedEntity) {
    // simple columns
    var entitySnapshot = persistenceContext.getEntitySnapshotWithColumnName(entityKey);
    var currentEntity = EntityUtil.getEntitySimpleColumnValues(updatedEntity);
    currentEntity.removeAll(entitySnapshot);
    Stream<Object> simpleColumnParams = currentEntity.stream().map(Pair::right);

    // toOne relation columns
    var entityToOneRelationSnapshot = persistenceContext.getToOneRelationSnapshot(entityKey);
    var currentToOneRelationState = EntityUtil.getEntityToOneRelationValues(updatedEntity);
    currentToOneRelationState.removeAll(entityToOneRelationSnapshot);
    Stream<Object> toOneRelationParams = currentToOneRelationState.stream().map(Triple::third);
    return Stream.concat(simpleColumnParams, toOneRelationParams).toArray();
  }
}
