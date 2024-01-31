package com.breskul.bibernate.persistence;

import com.breskul.bibernate.transaction.Transaction;
import com.breskul.bibernate.transaction.TransactionStatus;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;

// TODO: javadoc
public class Session implements AutoCloseable {

  private final GenericDao genericDao;
  private final PersistenceContext persistenceContext;
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

  //TODO add implementation
  public boolean isOpen() {
    return true;
  }

  public Transaction getTransaction() {
    if (transaction == null) {
      transaction = new Transaction(this, connection);
    } else if (
        transaction.getStatus() == TransactionStatus.COMMITTED ||
            transaction.getStatus() == TransactionStatus.ROLLED_BACK) {
      transaction = new Transaction(this, connection);
    }

    return transaction;
  }

  @Override
  public void close() throws Exception {
    // TODO implement
    throw new NotImplementedException("not implemented yet");
  }
}
