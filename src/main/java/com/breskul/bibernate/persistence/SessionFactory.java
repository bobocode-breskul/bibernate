package com.breskul.bibernate.persistence;

import javax.sql.DataSource;

// TODO: javadoc
public class SessionFactory {

  private final DataSource dataSource;

  protected SessionFactory(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  // TODO: javaDoc
  public Session openSession() {
    return new Session(dataSource);
  }
}
