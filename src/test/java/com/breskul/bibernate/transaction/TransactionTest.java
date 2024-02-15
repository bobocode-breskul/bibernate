package com.breskul.bibernate.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.persistence.Session;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionTest {

  @Mock
  private Session session;
  @Mock
  private Connection connection;

  @InjectMocks
  private Transaction transaction;

  @Test
  void givenSessionIsOpen_whenBegin_thenShouldBeginTransaction() throws SQLException {
    when(session.isOpen()).thenReturn(true);

    transaction.begin();

    assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.ACTIVE);
    verify(connection, times(1)).setAutoCommit(false);
  }

  @Test
  void givenSessionIsClosed_whenBegin_thenShouldThrowIllegalStateException() {
    when(session.isOpen()).thenReturn(false);

    assertThatThrownBy(() -> transaction.begin())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot operate Transaction on closed session");
  }

  @Test
  void givenSessionIsOpenButConnectionThrowsError_whenBegin_thenShouldThrowBibernateException()
      throws SQLException {
    when(session.isOpen()).thenReturn(true);
    doThrow(new SQLException()).when(connection).setAutoCommit(false);

    assertThatThrownBy(() -> transaction.begin())
        .isInstanceOf(BibernateException.class)
        .hasMessage("Error occurred during opening transaction");

    verify(connection, times(1)).setAutoCommit(true);
  }

  @Test
  void givenSessionIsOpenAndTransactionActive_whenCommit_thenShouldCommitTransaction()
      throws SQLException {
    when(session.isOpen()).thenReturn(true);

    transaction.begin();
    transaction.commit();

    assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMMITTED);
    verify(connection, times(1)).setAutoCommit(true);
  }

  @Test
  void givenSessionIsClosedAndTransactionActive_whenCommit_thenShouldThrowIllegalStateException() {
    when(session.isOpen()).thenReturn(true).thenReturn(false);

    transaction.begin();

    assertThatThrownBy(() -> transaction.commit())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot operate Transaction on closed session");
  }

  @Test
  void givenSessionIsOpenAndTransactionNotActive_whenCommit_thenShouldThrowIllegalStateException() {
    when(session.isOpen()).thenReturn(true);

    assertThatThrownBy(() -> transaction.commit())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Transaction is not active");
  }

  @Test
  void givenSessionIsOpenButConnectionThrowsError_whenCommit_thenShouldThrowBibernateException()
      throws SQLException {
    when(session.isOpen()).thenReturn(true);

    transaction.begin();

    doThrow(new SQLException()).when(connection).commit();

    assertThatThrownBy(() -> transaction.commit())
        .isInstanceOf(BibernateException.class)
        .hasMessage("Error occurred during committing transaction");

    assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.FAILED_COMMIT);
  }

  @Test
  void givenSessionIsOpenButConnectionThrowsErrorOnAutocommit_whenCommit_thenShouldThrowBibernateException()
      throws SQLException {
    when(session.isOpen()).thenReturn(true);

    transaction.begin();

    doThrow(new SQLException()).when(connection).setAutoCommit(true);

    assertThatThrownBy(() -> transaction.commit())
        .isInstanceOf(BibernateException.class)
        .hasMessage("Could not set autocommit to true");
  }

  @Test
  void givenSessionIsOpenAndTransactionActive_whenRollback_thenShouldRollbackTransaction()
      throws SQLException {
    when(session.isOpen()).thenReturn(true);

    transaction.begin();
    transaction.rollback();

    assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.ROLLED_BACK);
    verify(connection, times(1)).setAutoCommit(true);
  }

  @Test
  void givenSessionIsClosedAndTransactionActive_whenRollback_thenShouldThrowIllegalStateException() {
    when(session.isOpen()).thenReturn(true).thenReturn(false);

    transaction.begin();

    assertThatThrownBy(() -> transaction.rollback())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot operate Transaction on closed session");
  }

  @Test
  void givenSessionIsOpenAndTransactionNotActive_whenRollback_thenShouldThrowIllegalStateException() {
    when(session.isOpen()).thenReturn(true);

    assertThatThrownBy(() -> transaction.rollback())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot rollback transaction with status NOT_ACTIVE");
  }

  @Test
  void givenSessionIsOpenButConnectionThrowsError_whenRollback_thenShouldThrowBibernateException()
      throws SQLException {
    when(session.isOpen()).thenReturn(true);

    transaction.begin();

    doThrow(new SQLException()).when(connection).rollback();

    assertThatThrownBy(() -> transaction.rollback())
        .isInstanceOf(BibernateException.class)
        .hasMessage("Error occurred while transaction rollback");

    assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.FAILED_ROLLBACK);
  }
}