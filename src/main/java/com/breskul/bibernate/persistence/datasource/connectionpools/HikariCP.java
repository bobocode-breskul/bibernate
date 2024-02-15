package com.breskul.bibernate.persistence.datasource.connectionpools;

import com.breskul.bibernate.persistence.datasource.PersistenceProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * HikariCP is an implementation of the {@link ConnectionPoolFactory} interface,
 * specialized in creating and configuring instances of HikariCP's {@link HikariDataSource}.
 * This class leverages {@link PersistenceProperties} to configure the data source
 * with the necessary details such as JDBC URL, username, and password for database connectivity.
 */
public class HikariCP implements ConnectionPoolFactory {

  /**
   * Creates a {@link HikariDataSource} instance configured with the provided
   * {@link PersistenceProperties}.
   * <p>
   * By setting up the {@link HikariConfig} with the database connection details
   * specified in {@link PersistenceProperties}, this method prepares a HikariDataSource
   * for efficient and reliable database connection pooling. The HikariCP library is known
   * for its performance and simplicity, making it an excellent choice for database connectivity.
   * </p>
   * @param properties The {@link PersistenceProperties} containing the configuration
   *                   details such as JDBC URL, username, and password.
   * @return A {@link javax.sql.DataSource} instance, specifically a {@link HikariDataSource},
   *         configured and ready for use in database operations.
   */
  @Override
  public javax.sql.DataSource createDataSource(PersistenceProperties properties) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(properties.url());
    config.setUsername(properties.username());
    config.setPassword(properties.password());
    return new HikariDataSource(config);
  }
}
