package com.breskul.bibernate.exception;

/**
 * Thrown to indicate that an operation was attempted on an entity that is not managed. This typically occurs when an entity is expected to
 * be in a managed state for a particular operation (e.g., update, delete), but the entity is not present in the current persistence
 * context.
 */
public class EntityIsNotManagedException extends RuntimeException {

  public EntityIsNotManagedException(String message) {
    super(message);
  }
}
