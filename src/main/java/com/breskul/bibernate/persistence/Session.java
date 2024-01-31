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
        this.persistenceContext = new PersistenceContext();
        this.genericDao = new GenericDao(dataSource, persistenceContext);

    }

  // TODO add test
  // TODO add javadoc
  public <T> T findById(Class<T> entityClass, Object id) {
    return genericDao.findById(entityClass, id);
  }

  @Override
  public void close() throws Exception {
    // TODO implement
    throw new NotImplementedException("not implemented yet");
  }
}
