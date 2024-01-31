package com.breskul.bibernate.transaction;


import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.persistence.Session;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;

//TODO: write tests
public class Transaction {

  private static final Logger log = LoggerFactory.getLogger(Transaction.class);

  private final Session session;
  private final Connection connection;
  private TransactionStatus status;

  public Transaction(Session session, Connection connection) {
    this.session = session;
    this.connection = connection;
    this.status = TransactionStatus.NOT_ACTIVE;
  }

  /**
   * Start a resource transaction.
   *
   * @throws IllegalStateException if <code>isActive()</code> is true or when session is closed
   */
  public void begin() {
    try {
      validateIfSessionOpenAndTransactionActive();
      connection.setAutoCommit(false);
      status = TransactionStatus.ACTIVE;
      log.trace("Begin transaction");
    } catch (SQLException e) {
      setAutoCommitTrue();
      throw new BibernateException("Error occurred during opening transaction", e);
    }
  }

  /**
   * Commit the current resource transaction, writing any unflushed changes to the database.
   *
   * @throws IllegalStateException if <code>isActive()</code> is false
   * @throws BibernateException    if the commit fails
   */
  public void commit() {
    try {
      validateIfSessionOpenAndTransactionActive();
      connection.commit();
      connection.setAutoCommit(true);
      status = TransactionStatus.COMMITTED;
      log.trace("Transaction committed");
    } catch (SQLException ex) {
      status = TransactionStatus.FAILED_COMMIT;
      throw new BibernateException("Error occurred during committing transaction", ex);
    }
  }

  /**
   * Roll back the current resource transaction.
   *
   * @throws IllegalStateException if <code>isActive()</code> is false
   * @throws BibernateException    if an unexpected error condition is encountered
   */
  public void rollback() {
    try {
      validateIfCanRollback();
      connection.rollback();
      setAutoCommitTrue();
      status = TransactionStatus.ROLLED_BACK;
      log.trace("Transaction rolled back");
    } catch (SQLException ex) {
      status = TransactionStatus.FAILED_ROLLBACK;
      setAutoCommitTrue();
      throw new BibernateException("Error occurred while transaction rollback", ex);
    }
  }

  public TransactionStatus getStatus() {
    return status;
  }

  private void validateIfSessionOpenAndTransactionActive() throws SQLException {
    if (!session.isOpen()) {
      throw new IllegalStateException("Cannot begin Transaction on closed session");
    }
    if (status != TransactionStatus.ACTIVE) {
      throw new IllegalStateException("Cannot commit not active transaction");
    }
  }

  private void validateIfCanRollback() throws SQLException {
    if (!session.isOpen()) {
      throw new IllegalStateException("Cannot begin Transaction on closed session");
    }
    if (!status.canRollback()) {
      throw new IllegalStateException(
          "Cannot rollback transaction with status %s".formatted(status));
    }
  }

  private void setAutoCommitTrue() {
    try {
      connection.setAutoCommit(true);
    } catch (SQLException ex) {
      throw new BibernateException("Could not set autocommit to true", ex);
    }
  }
}
