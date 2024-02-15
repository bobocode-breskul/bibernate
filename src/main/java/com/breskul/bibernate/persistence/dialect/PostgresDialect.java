package com.breskul.bibernate.persistence.dialect;

import com.breskul.bibernate.persistence.LockType;

public class PostgresDialect implements Dialect {

  @Override
  public String getLockClause(LockType lockType) {
    if (lockType == null) {
      return "";
    }
    return switch (lockType) {
      case PESSIMISTIC_READ -> "FOR SHARE";
      case PESSIMISTIC_WRITE -> "FOR UPDATE";
    };
  }
}
