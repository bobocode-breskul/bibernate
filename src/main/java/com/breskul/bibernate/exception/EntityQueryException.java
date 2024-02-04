package com.breskul.bibernate.exception;

public class EntityQueryException extends RuntimeException {

  public EntityQueryException(String message, Throwable cause) {
    super(message, cause);
  }

  public EntityQueryException(String message) {
    super(message);
  }
}
