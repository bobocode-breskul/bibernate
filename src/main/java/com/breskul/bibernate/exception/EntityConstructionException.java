package com.breskul.bibernate.exception;

/**
 * Exception thrown when there is an error constructing of an entity.
 */
public class EntityConstructionException extends RuntimeException {

  public EntityConstructionException(String message) {
    super(message);
  }

  public EntityConstructionException(String message, Throwable cause) {
    super(message, cause);
  }
}
