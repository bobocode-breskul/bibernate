package com.breskul.bibernate.persistence.datasource.connectionpools;

import com.breskul.bibernate.persistence.datasource.PersistenceProperties;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * C3P0 is an implementation of the {@link ConnectionPoolFactory} interface that produces configured instances of C3P0's
 * {@link ComboPooledDataSource}. Utilizing {@link PersistenceProperties}, it sets up the data source connection specifics such as the JDBC
 * URL, username, and password.
 */
public class C3P0 implements ConnectionPoolFactory {

  /**
   * Creates a {@link ComboPooledDataSource} instance configured with the settings provided in {@link PersistenceProperties}.
   * <p>
   * This method establishes the JDBC connection properties for a ComboPooledDataSource, including the database URL, username, and password.
   * Aimed at offering a ready-to-use data source leveraging the C3P0 connection pooling library, it ensures efficient database
   * connectivity.
   * </p>
   *
   * @param properties The {@link PersistenceProperties} that hold the configuration details for the data source, such as the JDBC URL,
   *                   username, and password.
   * @return A {@link javax.sql.DataSource} instance that is configured based on the provided properties and is ready for database
   * operations.
   */
  @Override
  public javax.sql.DataSource createDataSource(PersistenceProperties properties) {
    ComboPooledDataSource cpds = new ComboPooledDataSource();
    cpds.setJdbcUrl(properties.url());
    cpds.setUser(properties.username());
    cpds.setPassword(properties.password());
    return cpds;
  }
}
