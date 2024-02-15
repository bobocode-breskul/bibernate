package com.breskul.bibernate.persistence.dialect;

import static org.assertj.core.api.Assertions.assertThat;

import com.breskul.bibernate.persistence.LockType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class H2DialectTest {

  private H2Dialect dialect;

  @BeforeEach
  public void setUp() {
    dialect = new H2Dialect();
  }

  @Test
  void givenNullLock_thenReturnEmptyString() {
    String lockClause = dialect.getLockClause(null);

    assertThat(lockClause).isEmpty();
  }

  @Test
  void givenReadLock_thenReturnEmptyString() {
    String lockClause = dialect.getLockClause(LockType.PESSIMISTIC_READ);

    assertThat(lockClause).isEmpty();
  }

  @Test
  void givenWriteLock_thenReturnEmptyString() {
    String lockClause = dialect.getLockClause(LockType.PESSIMISTIC_WRITE);

    assertThat(lockClause).isEqualTo("FOR UPDATE");
  }
}