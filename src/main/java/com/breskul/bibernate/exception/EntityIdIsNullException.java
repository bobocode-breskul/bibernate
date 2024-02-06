package com.breskul.bibernate.exception;

/**
 * Exception thrown to indicate that Entity id is null that is not allowed.
 */
public class EntityIdIsNullException extends RuntimeException {

  public EntityIdIsNullException(String message) {
    super(message);
  }
}
