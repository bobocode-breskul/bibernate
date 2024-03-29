package com.breskul.bibernate.transaction;

/**
 * Represents status of {@link Transaction}
 */
public enum TransactionStatus {
  /**
   * The transaction has not yet been started.
   */
  NOT_ACTIVE,
  /**
   * The transaction has been started, but not yet completed.
   */
  ACTIVE,
  /**
   * The transaction has been completed successfully.
   */
  COMMITTED,
  /**
   * The transaction has been rolled back.
   */
  ROLLED_BACK,
  /**
   * The transaction attempted to commit, but failed.
   */
  FAILED_COMMIT,
  /**
   * The transaction attempted to rollback, but failed.
   */
  FAILED_ROLLBACK;

  public boolean canRollback() {
    return this == ACTIVE || this == FAILED_COMMIT;
  }
}
