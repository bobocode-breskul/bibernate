package com.breskul.bibernate.persistence.dialect;

import com.breskul.bibernate.persistence.LockType;

public class InvalidDialectMissingConstructor implements Dialect {

  // Missing default constructor
  public InvalidDialectMissingConstructor(String someParameter) {
    // Constructor with a parameter
  }

  @Override
  public String getLockClause(LockType lockType) {
    return null;
  }
}