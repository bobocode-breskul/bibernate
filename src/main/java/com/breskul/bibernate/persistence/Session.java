package com.breskul.bibernate.persistence;

import static java.util.Comparator.comparing;

import com.breskul.bibernate.action.Action;
import com.breskul.bibernate.action.DeleteAction;
import com.breskul.bibernate.action.InsertAction;
import com.breskul.bibernate.action.UpdateAction;
import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.persistence.context.PersistenceContext;
import com.breskul.bibernate.persistence.context.snapshot.EntityPropertySnapshot;
import com.breskul.bibernate.persistence.context.snapshot.EntityRelationSnapshot;
import com.breskul.bibernate.transaction.Transaction;
import com.breskul.bibernate.transaction.TransactionStatus;
import com.breskul.bibernate.util.EntityUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.slf4j.Logger;


/**
 * Represents a session for managing database operations and entity persistence. Provides methods
 * for entity management, persistence, transaction handling, and session closure.
 * <p>
 * This class encapsulates a set of operations related to database interactions within a single unit
 * of work. It allows developers to perform database operations such as finding entities by ID,
 * merging entity changes, managing entity state, persisting new entities, and handling
 * transactions.
 * <p>
 * Additionally, the session class manages the persistence context, which stores first-level cached
 * entities and their snapshots. It also maintains an action queue to track operations performed
 * within the session.
 */
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

  /**
   * Find entry by id for specified entity class
   *
   * @param entityClass represents table
   * @param id          is used search
   * @param <T>         represent type of entity
   * @return object of entity class
   */
  public <T> T findById(Class<T> entityClass, Object id) {
    verifyIsSessionOpen();
    Objects.requireNonNull(id, "Required id to load load entity, pleas provide not null value");
    return Optional.ofNullable(persistenceContext.getEntity(entityClass, id))
        .orElseGet(() -> find(EntityKey.of(entityClass, id)));
  }

  private <T> T find(EntityKey<? extends T> entityKey) {
    verifyIsSessionOpen();
    T entity = genericDao.findById(entityKey.entityClass(), entityKey.id());
    if (entity != null) {
      persistenceContext.put(entity);
    }
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
    verifyIsSessionOpen();
    new InsertAction(genericDao, entity).execute();
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
      log.trace("Using current transaction");
    }

    return transaction;
  }

  /**
   * Creates delete action and put it in action queue
   *
   * @param entity represents entity that will be deleted
   * @param <T> represents type of entry
   */
  public <T> void delete(T entity) {
    verifyIsSessionOpen();
    actionQueue.offer(new DeleteAction(genericDao, entity));
    persistenceContext.delete(entity);
  }

  /**
   * Closes the session, performing necessary operations such as dirty checking, clearing the
   * persistence context, clearing the action queue, and updating the session status.
   */
  @Override
  public void close() {
    performDirtyChecking();
    persistenceContext.clear();
    actionQueue.clear();
    sessionStatus = false;
  }

  /**
   *  Flushes session action queue
   */
  public void flush() {
    verifyIsSessionOpen();
    performDirtyChecking();
    log.trace("Flushing session action queue");
    while (!actionQueue.isEmpty()) {
      actionQueue.poll().execute();
    }
  }

  /**
   * Performs dirty checking on entities in the persistence context and flushes any changes found.
   */
  private void performDirtyChecking() {
    log.trace("Executing dirty checking...");
    persistenceContext.getEntityKeys().stream()
        .filter(persistenceContext::isDirty)
        .forEach(this::flushChanges);
  }

  /**
   * Flushes changes for the specified entity found in the persistence context.
   *
   * @param entityKey The key representing the entity.
   * @param <T>       The type of the entity.
   */
  private <T> void flushChanges(EntityKey<T> entityKey) {
    log.debug("Found not flushed changes in the cache");
    T updatedEntity = persistenceContext.getEntity(entityKey);
    Object[] parameters = EntityUtil.getEntityColumnValues(updatedEntity);
    if (EntityUtil.isDynamicUpdate(entityKey.entityClass())) {
      parameters = prepareDynamicParameters(entityKey, updatedEntity);
    }
    actionQueue.offer(new UpdateAction<>(genericDao, entityKey, parameters));
  }

  /**
   * Prepares dynamic parameters for the entity update query based on differences between the
   * current and snapshot states.
   *
   * @param entityKey     The key representing the entity.
   * @param updatedEntity The updated entity.
   * @param <T>           The type of the entity.
   * @return An array of objects representing the dynamic parameters.
   */
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

  private void verifyIsSessionOpen() {
    if (!sessionStatus) {
      throw new IllegalStateException("Session is closed");
    }
  }
}
