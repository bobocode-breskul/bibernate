package com.breskul.bibernate.exception;


/**
 * Exception thrown when there is an error accessing entity fields or using reflection for
 * entity/objects creation.
 */
public class ReflectAccessException extends RuntimeException {
  public ReflectAccessException(String message, Throwable cause) {
    super(message, cause);
  }
}
