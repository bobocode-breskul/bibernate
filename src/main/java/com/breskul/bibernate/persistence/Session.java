package com.breskul.bibernate.persistence;

import com.breskul.bibernate.action.Action;
import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.persistence.context.PersistenceContext;
import com.breskul.bibernate.transaction.Transaction;
import com.breskul.bibernate.transaction.TransactionStatus;
import com.breskul.bibernate.persistence.context.snapshot.EntityPropertySnapshot;
import com.breskul.bibernate.persistence.context.snapshot.EntityRelationSnapshot;
import com.breskul.bibernate.util.EntityUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.slf4j.Logger;

// TODO: javadoc
public class Session implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(Session.class);

  private final GenericDao genericDao;
  private final PersistenceContext persistenceContext;
  private final Queue<Action> actionQueue = new PriorityQueue<>();
  private final Connection connection;

  private Transaction transaction;
  private boolean sessionStatus;

  public Session(DataSource dataSource) throws SQLException {
    connection = dataSource.getConnection();
    connection.setAutoCommit(true);
    persistenceContext = new PersistenceContext();
    genericDao = new GenericDao(connection, persistenceContext);
    sessionStatus = true;
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
      if (!persistenceContext.isDirty(key)) {
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

  /**
   * Check if current session is open
   */
  public boolean isOpen() {
    return sessionStatus;
  }

  //TODO: write tests

  /**
   * Returns session transaction. If session does not have it or transaction was completed or rolled
   * back then creates new {@link Transaction}
   *
   * @return current session status
   */
  public Transaction getTransaction() {
    if (transaction == null) {
      log.trace("Creating new transaction");
      transaction = new Transaction(this, connection);
    } else if (transaction.getStatus() == TransactionStatus.COMMITTED ||
        transaction.getStatus() == TransactionStatus.ROLLED_BACK) {
      log.trace("Creating new transaction");
      transaction = new Transaction(this, connection);
    } else {
      log.trace("using current transaction");
    }

    return transaction;
  }

  @Override
  public void close() {
    performDirtyChecking();
    // todo: transaction commit/rollback
    persistenceContext.clear();
    actionQueue.clear();
    sessionStatus = false;
  }

  private void performDirtyChecking() {
    persistenceContext.getEntityKeys().stream()
        .filter(persistenceContext::isDirty)
        .forEach(this::flushChanges);
  }


  // TODO: reformat code
  // TODO: write javadoc for all changes
  // TODO: cover all changes with tests
  private <T> void flushChanges(EntityKey<T> entityKey) {
    log.debug("Found not flushed changes in the cache");
    T updatedEntity = persistenceContext.getEntity(entityKey);
    Object[] parameters = EntityUtil.getEntityColumnValues(updatedEntity);
    if (EntityUtil.isDynamicUpdate(entityKey.entityClass())) {
      parameters = prepareDynamicParameters(entityKey, updatedEntity);
    }
    genericDao.executeUpdate(entityKey, parameters);
  }


  private <T> Object[] prepareDynamicParameters(EntityKey<T> entityKey, T updatedEntity) {
    // simple columns
    List<EntityPropertySnapshot> entitySnapshot =
        persistenceContext.getEntityPropertySnapshot(entityKey);
    List<EntityPropertySnapshot> currentEntity =
        EntityUtil.getEntitySimpleColumnValues(updatedEntity);
    currentEntity.removeAll(entitySnapshot);
    Stream<Object> simpleColumnParams = currentEntity.stream()
        .map(EntityPropertySnapshot::columnValue);

    // toOne relation columns
    List<EntityRelationSnapshot> entityToOneRelationSnapshot =
        persistenceContext.getToOneRelationSnapshot(entityKey);
    List<EntityRelationSnapshot> currentToOneRelationState =
        EntityUtil.getEntityToOneRelationValues(updatedEntity);
    currentToOneRelationState.removeAll(entityToOneRelationSnapshot);
    Stream<Object> toOneRelationParams = currentToOneRelationState.stream()
        .map(EntityRelationSnapshot::columnValue);
    return Stream.concat(simpleColumnParams, toOneRelationParams).toArray();
  }
}
