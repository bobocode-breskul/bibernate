package com.breskul.bibernate.exception;

/**
 * Represents duplicate entity exception when entity already exists in persistence context
 */
public class DuplicateEntityException extends RuntimeException {

  public <T> DuplicateEntityException(T entity) {
    super("Entity already exist in persistence context ".concat(entity.toString()));
  }

}
