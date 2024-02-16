package com.breskul.bibernate.persistence.dialect;

import com.breskul.bibernate.persistence.LockType;

public class InvalidDialectExceptionInConstructor implements Dialect {

  public InvalidDialectExceptionInConstructor() {
    throw new RuntimeException("Exception in constructor");
  }

  @Override
  public String getLockClause(LockType lockType) {
    return null;
  }
}