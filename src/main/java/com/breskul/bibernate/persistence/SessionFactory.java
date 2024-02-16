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

  /**
   * Opens a new session for interacting with the database. This session provides
   * various methods for executing queries, managing transactions, and performing
   * CRUD operations on entities.
   * <p>
   * The session is configured based on the provided {@code dataSource}, {@code dialect},
   * and {@code showSql} settings. The {@code dialect} determines how SQL queries are
   * generated specific to the database in use, and {@code showSql} configures whether
   * SQL queries executed by the session should be logged.
   *
   * @return A new {@link Session} instance for database interaction.
   * @throws SQLException If there is a problem obtaining a connection from the
   *                      {@code dataSource} or initializing the session.
   */
  public Session openSession() throws SQLException {
    return new Session(dataSource, dialect, showSql);
  }
}
