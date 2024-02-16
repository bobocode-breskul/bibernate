package com.breskul.bibernate.exception;


/**
 * Exception thrown when an entity association operation fails. For example, loading entity associated collection or parent record.
 */
public class AssociationException extends RuntimeException {

  public AssociationException(String message) {
    super(message);
  }

  public AssociationException(String message, Throwable cause) {
    super(message, cause);
  }
}
