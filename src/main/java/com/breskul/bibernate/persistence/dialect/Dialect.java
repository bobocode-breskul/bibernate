package com.breskul.bibernate.persistence.dialect;

import com.breskul.bibernate.persistence.LockType;

public interface Dialect {

  String getLockClause(LockType lockType);
}
