package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.util.EntityUtil.findEntityIdFieldName;
import static java.util.Comparator.comparing;

import com.breskul.bibernate.action.Action;
import com.breskul.bibernate.action.DeleteAction;
import com.breskul.bibernate.action.InsertAction;
import com.breskul.bibernate.action.UpdateAction;
import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.exception.EntityIsNotManagedException;
import com.breskul.bibernate.persistence.context.PersistenceContext;
import com.breskul.bibernate.persistence.context.snapshot.EntityPropertySnapshot;
import com.breskul.bibernate.persistence.context.snapshot.EntityRelationSnapshot;
import com.breskul.bibernate.persistence.dialect.Dialect;
import com.breskul.bibernate.query.hql.BiQLMapper;
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
 * Represents a session for managing database operations and entity persistence. Provides methods for entity management, persistence,
 * transaction handling, and session closure.
 * <p>
 * This class encapsulates a set of operations related to database interactions within a single unit of work. It allows developers to
 * perform database operations such as finding entities by ID, merging entity changes, managing entity state, persisting new entities, and
 * handling transactions.
 * <p>
 * Additionally, the session class manages the persistence context, which stores first-level cached entities and their snapshots. It also
 * maintains an action queue to track operations performed within the session.
 */
public class Session implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(Session.class);

  private final GenericDao genericDao;
  private final PersistenceContext persistenceContext;
  private final Queue<Action> actionQueue = new PriorityQueue<>(comparing(Action::priority));
  private final Connection connection;

  private Transaction transaction;
  private boolean sessionStatus;

  public Session(DataSource dataSource, Dialect dialect, boolean showSql) throws SQLException {
    connection = dataSource.getConnection();
    connection.setAutoCommit(true);
    persistenceContext = new PersistenceContext();
    genericDao = new GenericDao(connection, persistenceContext, dialect, showSql);
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
    return findById(entityClass, id, null);
  }

  public <T> T findById(Class<T> entityClass, Object id, LockType lockType) {
    verifyIsSessionOpen();
    Objects.requireNonNull(id, "Required id to load load entity, pleas provide not null value");
    return Optional.ofNullable(persistenceContext.getEntity(entityClass, id))
        .filter(entity -> lockType == null)
        .orElseGet(() -> find(EntityKey.of(entityClass, id), lockType));
  }

  private <T> T find(EntityKey<? extends T> entityKey) {
    return find(entityKey, null);
  }

  private <T> T find(EntityKey<? extends T> entityKey, LockType lockType) {
    verifyIsSessionOpen();
    T entity = genericDao.findById(entityKey.entityClass(), entityKey.id(), lockType);
    if (entity == null) {
      return null;
    }
    T persistEntity = persistenceContext.getEntity(EntityKey.valueOf(entity));
    if (persistEntity != null) {
      return persistEntity;
    }
    persistenceContext.put(entity);
    return entity;
  }

  /**
   * Merges the provided entity with the existing entity in the persistence context or database. If the entity is already present in the
   * persistence context, it copies the changed values from the provided entity to the existing entity. If the entity is not present in the
   * persistence context, it attempts to find the entity in the database and copies the changed values from the provided entity to the found
   * entity. If the entity is not found in the database, it persists a copy of the provided entity. Provided entity object never becomes
   * managed.
   *
   * @param mergeEntity The entity to be merged.
   * @param <T>         The type of the entity.
   * @return The updated managed entity.
   */
  public <T> T mergeEntity(T mergeEntity) {
    var mergeEntityKey = EntityKey.valueOf(mergeEntity);
    if (persistenceContext.contains(mergeEntity)) {
      T cachedEntity = persistenceContext.getEntity(mergeEntityKey);
      if (mergeEntity != cachedEntity) {
        EntityUtil.copyChangedValues(mergeEntity, cachedEntity);
      }
      return cachedEntity;
    }
    T foundEntity = find(mergeEntityKey);
    if (foundEntity != null) {
      EntityUtil.copyChangedValues(mergeEntity, foundEntity);
      return foundEntity;
    }
    T entityInstance = persistCopy(mergeEntity);
    return persistenceContext.getEntity(EntityKey.valueOf(entityInstance));
  }

  /**
   * Makes a copy of the provided entity and persists it. Provided entity object ID value will be changed too.
   *
   * @param mergeEntity The entity to be persisted and copied.
   * @param <T>         The type of the entity.
   * @return The persisted entity copy
   */
  private <T> T persistCopy(T mergeEntity) {
    T entityCopy = EntityUtil.copyEntity(mergeEntity);
    persist(entityCopy);
    EntityUtil.copyEntityId(entityCopy, mergeEntity);
    return entityCopy;
  }

  public <T> void manageEntity(T entity) {
    persistenceContext.put(entity);
  }

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

  /**
   * Returns session transaction. If session does not have it or transaction was completed or rolled back then creates new
   * {@link Transaction}
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
   * @param <T>    represents type of entry
   */
  public <T> void delete(T entity) {
    verifyEntityManaged(entity);
    verifyIsSessionOpen();
    actionQueue.offer(new DeleteAction(genericDao, entity));
    persistenceContext.delete(entity);
  }

  /**
   * Executes a SQL query and returns the results as a list of the specified type.
   *
   * @param <T>         the type of the result list
   * @param sqlString   the SQL query to execute
   * @param resultClass the class of the results
   * @return a list of objects of type T
   */
  public <T> List<T> executeNativeQuery(String sqlString, Class<T> resultClass) {
    return genericDao.executeNativeQuery(sqlString, resultClass);
  }

  /**
   * Converts a BiQL query to SQL and executes it, returning the results as a list of the specified type.
   *
   * @param <T>         the type of the result list
   * @param bglString   the BiQL query string
   * @param resultClass the class of the results
   * @return a list of objects of type T
   */
  public <T> List<T> executeBiQLQuery(String bglString, Class<T> resultClass) {
    return executeNativeQuery(BiQLMapper.bqlToSql(bglString, resultClass), resultClass);
  }

  /**
   * Closes the session, performing necessary operations such as dirty checking, clearing the persistence context, clearing the action
   * queue, and updating the session status.
   */
  @Override
  public void close() {
    persistenceContext.clear();
    actionQueue.clear();

    if (transaction != null && transaction.getStatus().canRollback()) {
      transaction.rollback();
    }

    sessionStatus = false;
  }

  /**
   * Flushes session action queue
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
    checkIdNotAltered(entityKey);
    T updatedEntity = persistenceContext.getEntity(entityKey);
    Object[] parameters = EntityUtil.getEntityColumnValues(updatedEntity);
    if (EntityUtil.isDynamicUpdate(entityKey.entityClass())) {
      parameters = prepareDynamicParameters(entityKey, updatedEntity);
    }
    actionQueue.offer(new UpdateAction<>(genericDao, entityKey, parameters));
  }

  private <T> void checkIdNotAltered(EntityKey<T> entityKey) {
    T currentState = persistenceContext.getEntity(entityKey);
    Object currentIdValue = EntityUtil.getEntityId(currentState);
    Class<T> entityClass = entityKey.entityClass();
    EntityPropertySnapshot idFieldSnapshot =
        persistenceContext.getEntityPropertySnapshotByColumnName(entityKey, findEntityIdFieldName(
            entityClass));
    if (!Objects.equals(currentIdValue, idFieldSnapshot.columnValue())) {
      throw new BibernateException(
          "identifier of an instance of %s was altered from %s to %s".formatted(
              entityClass.getName(), idFieldSnapshot.columnValue(), currentIdValue));
    }
  }

  /**
   * Prepares dynamic parameters for the entity update query based on differences between the current and snapshot states.
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

  private <T> void verifyEntityManaged(T entity) {
    if (!persistenceContext.contains(entity)) {
      throw new EntityIsNotManagedException(
          "Entity [%s] could not be deleted because not found in the persistent context.".formatted(
              entity));
    }
  }
}
