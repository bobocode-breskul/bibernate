package com.breskul.bibernate.persistence;

import org.postgresql.ds.PGSimpleDataSource;

// TODO: javadoc
public class Persistence {

  // TODO: read properties for data source
  public static SessionFactory createSessionFactory() {
    // TODO: initialize data source
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setUrl("jdbc:postgresql://localhost:5432/postgres");
    dataSource.setUser("postgres");
    dataSource.setPassword("admin");
    return new SessionFactory(dataSource);
  }
}
