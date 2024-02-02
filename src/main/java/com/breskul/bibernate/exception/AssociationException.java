package com.breskul.bibernate.exception;

// todo: docs
public class AssociationException extends RuntimeException {
  public AssociationException(String message) {
    super(message);
  }

  public AssociationException(String message, Throwable cause) {
    super(message, cause);
  }
}
