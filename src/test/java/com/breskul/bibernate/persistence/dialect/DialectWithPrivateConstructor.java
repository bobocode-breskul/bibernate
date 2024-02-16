package com.breskul.bibernate.persistence.dialect;

import com.breskul.bibernate.persistence.LockType;

public class DialectWithPrivateConstructor implements Dialect {

  private DialectWithPrivateConstructor() {
  }

  @Override
  public String getLockClause(LockType lockType) {
    return null;
  }
}