package com.breskul.bibernate.query.hql;

import java.util.List;

public class Query {
  private final String sql;

  public Query(String sql) {
    this.sql = sql;
  }

  public List<Object> getResultList() {
    return null;
  }

  public Object getSingleResult() {
    return null;
  }
}
