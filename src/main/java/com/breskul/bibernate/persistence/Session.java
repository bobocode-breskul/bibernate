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
