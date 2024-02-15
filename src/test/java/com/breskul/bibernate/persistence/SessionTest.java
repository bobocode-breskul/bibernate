package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.transaction.TransactionStatus.NOT_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.breskul.bibernate.transaction.Transaction;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionTest {

  @Mock
  private DataSource dataSource;
  @Mock
  private Connection connection;

  private Session session;

  @BeforeEach
  void setUp() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);

    session = new Session(dataSource);
  }

  @Test
  void givenSessionWithOutTransaction_whenGetTransaction_thenShouldReturnNewTransaction() {
    Transaction transaction = session.getTransaction();

    assertThat(transaction.getStatus()).isEqualTo(NOT_ACTIVE);
  }

  @Test
  void givenSessionWithTransaction_whenGetTransaction_thenShouldReturnSameTransaction() {
    Transaction transaction = session.getTransaction();
    Transaction transaction1 = session.getTransaction();

    assertThat(transaction).isSameAs(transaction1);
  }

  @Test
  void givenSessionWithCommitedTransaction_whenGetTransaction_thenShouldReturnNewTransaction() {
    Transaction transaction = session.getTransaction();
    transaction.begin();
    transaction.commit();

    Transaction transaction1 = session.getTransaction();

    assertThat(transaction).isNotSameAs(transaction1);
    assertThat(transaction1.getStatus()).isEqualTo(NOT_ACTIVE);
  }
}