package com.breskul.bibernate.exception;

/**
 * The {@code BiQLException} class represents exceptions that may occur during the processing of Bi Query Language (BiQL) queries. It
 * extends {@code BibernateException}, indicating that it is a specific type of exception within a broader custom ORM framework or library
 * handling database interactions. This exception class is designed to encapsulate errors related to the parsing, validation, and conversion
 * of BiQL to SQL, providing more detailed context about issues encountered during these operations.
 */
public class BiQLException extends BibernateException {

  /**
   * Constructs a new {@code BiQLException} with the specified detail message. The message explains the reason for the exception and can be
   * used to provide meaningful feedback to the user or developer.
   *
   * @param message the detailed message explaining the reason for the exception
   */
  public BiQLException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code BiQLException} with the specified detail message and cause. This constructor is useful for wrapping lower-level
   * exceptions that contribute to this exception and providing additional context about the error, such as an underlying SQL exception that
   * led to the failure of BQL processing.
   *
   * @param message the detailed message explaining the reason for the exception
   * @param cause   the cause of the exception (which is saved for later retrieval by the {@code Throwable.getCause()} method)
   */
  public BiQLException(String message, Throwable cause) {
    super(message, cause);
  }
}
