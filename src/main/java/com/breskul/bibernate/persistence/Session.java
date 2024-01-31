package com.breskul.bibernate.persistence;

import com.breskul.bibernate.action.Action;
import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.transaction.Transaction;
import com.breskul.bibernate.transaction.TransactionStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.PriorityQueue;
import java.util.Queue;
import javax.sql.DataSource;
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
  public void close() throws Exception {
    // TODO implement
    throw new NotImplementedException("not implemented yet");
  }
}
