package com.breskul.bibernate.exception;

/**
 * Represents an exception that occurs during entity parsing.
 */
public class EntityParseException extends RuntimeException {

  public EntityParseException(String message) {
    super(message);
  }
}
