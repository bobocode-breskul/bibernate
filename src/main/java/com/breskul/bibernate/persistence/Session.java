package com.breskul.bibernate.persistence;

import static java.util.Comparator.comparing;

import com.breskul.bibernate.action.Action;
import com.breskul.bibernate.action.DeleteAction;
import com.breskul.bibernate.action.InsertAction;
import com.breskul.bibernate.action.UpdateAction;
import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.transaction.Transaction;
import com.breskul.bibernate.transaction.TransactionStatus;
import com.breskul.bibernate.util.EntityUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import javax.sql.DataSource;
import org.slf4j.Logger;

// TODO: javadoc
public class Session implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(Session.class);

  private final GenericDao genericDao;
  private final PersistenceContext persistenceContext;
  private final Queue<Action> actionQueue = new PriorityQueue<>(comparing(Action::priority));
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
    verifyIsSessionOpen();
    return Optional.ofNullable(persistenceContext.getEntity(entityClass, id))
        .orElseGet(() -> find(EntityKey.of(entityClass, id)));
  }

  private <T> T find(EntityKey<? extends T> entityKey) {
    verifyIsSessionOpen();
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
    verifyIsSessionOpen();
    actionQueue.offer(new InsertAction(genericDao, entity));
    persistenceContext.put(entity);
  }

  /**
   * Check if current session is open
   */
  public boolean isOpen() {
    return sessionStatus;
  }

  //TODO: write tests
  /**
   * Returns session transaction. If session does not have it or transaction was
   * completed or rolled back then creates new {@link Transaction}
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
      log.trace("Using current transaction");
    }

    return transaction;
  }

  public <T> void delete(T entity) {
    verifyIsSessionOpen();
    actionQueue.offer(new DeleteAction(genericDao, entity));
    persistenceContext.delete(entity);
  }

  @Override
  public void close() {
    performDirtyChecking();
    persistenceContext.clear();
    actionQueue.clear();
    sessionStatus = false;
  }

  public void flush() {
    verifyIsSessionOpen();
    performDirtyChecking();
    log.trace("Flushing session action queue");
    while (!actionQueue.isEmpty()) {
      actionQueue.poll().execute();
    }
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
    actionQueue.offer(
        new UpdateAction<>(genericDao, entityKey, EntityUtil.getEntityColumnValues(updatedEntity)));
  }

  private void verifyIsSessionOpen() {
    if (!sessionStatus) {
      throw new IllegalStateException("Session is closed");
    }
  }
}
