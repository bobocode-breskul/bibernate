package com.breskul.bibernate.persistence;

import com.breskul.bibernate.persistence.dialect.Dialect;
import java.sql.SQLException;
import javax.sql.DataSource;

// TODO: javadoc
public class SessionFactory {

  private final DataSource dataSource;
  private final Dialect dialect;

  protected SessionFactory(DataSource dataSource, Dialect dialect) {
    this.dataSource = dataSource;
    this.dialect = dialect;
  }

  // TODO: javaDoc
  public Session openSession() throws SQLException {
    return new Session(dataSource, dialect);
  }
}
