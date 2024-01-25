package com.breskul.bibernate.exception;

import com.breskul.bibernate.config.PropertiesConfiguration;

/**
 * Exception thrown to indicate that property value is invalid.
 * <p>
 * For example: if propertyName would be a string the method getPropertyAsInt from
 * PropertiesConfiguration throw InvalidPropertyValueException.
 *
 * @see PropertiesConfiguration
 */
public class InvalidPropertyValueException extends RuntimeException {

  public InvalidPropertyValueException(String message, Throwable cause) {
    super(message, cause);
  }
}
