package com.breskul.bibernate.persistence;

import com.breskul.bibernate.util.EntityUtil;
import jakarta.persistence.EntityTransaction;
import java.util.Arrays;
import java.util.Optional;
import com.breskul.bibernate.action.Action;
import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.transaction.Transaction;
import com.breskul.bibernate.transaction.TransactionStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.PriorityQueue;
import java.util.Queue;
import javax.sql.DataSource;
import org.slf4j.Logger;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;
import org.slf4j.Logger;

// TODO: javadoc
public class Session implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(Session.class);

  private final GenericDao genericDao;
  private final PersistenceContext persistenceContext;
  private final Queue<Action> actionQueue = new PriorityQueue<>();
  private final Connection connection;
  private Transaction transaction;

  public Session(DataSource dataSource) throws SQLException {
    connection = dataSource.getConnection();
    connection.setAutoCommit(true);
    genericDao = new GenericDao(connection);
    persistenceContext = new PersistenceContext();
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
   * @param entity  entity instance
   */
  public <T> void persist(T entity) {
    T savedEntity = genericDao.save(entity);
    persistenceContext.put(savedEntity);
  }

  //TODO implement
  public boolean isOpen() {
    return true;
  }

  //TODO: write tests
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
  }

  private void performDirtyChecking() {
    persistenceContext.getEntityKeys().stream()
        .filter(this::hasChanged)
        .forEach(this::flushChanges);
  }

  private <T> boolean hasChanged(EntityKey<T> entityKey) {
    Object[] currentEntityState = EntityUtil.getEntityColumnValues(
        persistenceContext.getEntity(entityKey));
    Object[] initialEntityState = persistenceContext.getEntitySnapshot(entityKey);
    return !Arrays.equals(currentEntityState, initialEntityState);
  }

  private <T> void flushChanges(EntityKey<T> entityKey) {
    log.trace("Found not flushed changes in the cache");
    T updatedEntity = persistenceContext.getEntity(entityKey);
    genericDao.executeUpdate(entityKey, EntityUtil.getEntityColumnValues(updatedEntity));
  }
}
