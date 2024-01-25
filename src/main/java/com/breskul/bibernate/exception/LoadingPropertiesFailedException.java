package com.breskul.bibernate.exception;

import com.breskul.bibernate.config.PropertiesConfiguration;

/**
 * Exception thrown to indicate that loading properties in loadProperties() method was failed.
 *
 * @see PropertiesConfiguration
 */
public class LoadingPropertiesFailedException extends RuntimeException {

  public LoadingPropertiesFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
