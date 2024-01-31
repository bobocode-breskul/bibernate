package com.breskul.bibernate.persistence;

import jakarta.persistence.EntityTransaction;
import java.util.PriorityQueue;
import java.util.Queue;
import javax.sql.DataSource;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;

// TODO: javadoc
public class Session implements AutoCloseable {

  private final GenericDao genericDao;
  private final PersistenceContext persistenceContext;
  private final Queue<Action> actionQueue = new PriorityQueue<>();
  private EntityTransaction transaction;

  public Session(DataSource dataSource) {
    this.genericDao = new GenericDao(dataSource);
    this.persistenceContext = new PersistenceContext();
  }

  // TODO add test
  // TODO add javadoc
  public <T> T findById(Class<T> entityClass, Object id) {
    return genericDao.findById(entityClass, id);
  }

  // TODO add test

  /**
   * Make an instance managed and persistent.
   * @param entity  entity instance
   * @throws @code EntityExistsException if the entity already exists.
   * (If the entity already exists, the <code>EntityExistsException</code> may
   * be thrown when the persist operation is invoked, or the
   * <code>EntityExistsException</code> or another <code>PersistenceException</code> may be
   * thrown at flush or commit time.)
   * @throws @code IllegalArgumentException if the instance is not an
   *         entity
   * @throws @code TransactionRequiredException if there is no transaction when
   *         invoked on a container-managed entity manager of that is of type
   *         <code>PersistenceContextType.TRANSACTION</code>
   */
  public <T> void persist(T entity) {
    T savedEntity = genericDao.save(entity);
    persistenceContext.manageEntity(savedEntity);
  }

  @Override
  public void close() throws Exception {
    // TODO implement
    throw new NotImplementedException("not implemented yet");
  }
}
