package com.breskul.bibernate.persistence;

import com.breskul.bibernate.persistence.dialect.Dialect;
import java.sql.SQLException;
import javax.sql.DataSource;

public class SessionFactory {

  private final DataSource dataSource;
  private final Dialect dialect;
  private final boolean showSql;

  protected SessionFactory(DataSource dataSource, Dialect dialect, boolean showSql) {
    this.dataSource = dataSource;
    this.dialect = dialect;
    this.showSql = showSql;
  }

  public Session openSession() throws SQLException {
    return new Session(dataSource, dialect, showSql);
  }
}
