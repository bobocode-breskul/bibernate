package com.breskul.bibernate.exception;

public class DuplicateEntityException extends RuntimeException {

  public <T> DuplicateEntityException(T entity) {
    super("Entity already exist in persistence context ".concat(entity.toString()));
  }

}
