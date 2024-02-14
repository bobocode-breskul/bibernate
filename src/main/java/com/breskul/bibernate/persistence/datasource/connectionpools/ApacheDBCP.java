package com.breskul.bibernate.persistence.datasource.connectionpools;

import com.breskul.bibernate.persistence.datasource.DataSourceProperties;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * ApacheDBCPFactory is an implementation of the {@link ConnectionPoolFactory} interface
 * that creates and configures instances of Apache Commons DBCP's {@link BasicDataSource}.
 * This class utilizes the provided {@link DataSourceProperties} to set up the data source
 * connection details such as the URL, username, and password.
 */
public class ApacheDBCP implements ConnectionPoolFactory {

  /**
   * Creates a {@link BasicDataSource} instance configured with the specified
   * {@link DataSourceProperties}.
   * <p>
   * This method sets up the connection properties for a BasicDataSource, including
   * the database URL, username, and password. It is designed to provide a ready-to-use
   * data source based on Apache Commons DBCP.
   * </p>
   * @param properties The {@link DataSourceProperties} containing the configuration
   *                   details for the data source, such as the database URL, username,
   *                   and password.
   * @return A {@link javax.sql.DataSource} instance configured according to the provided
   *         properties, ready for database interaction.
   */
  @Override
  public javax.sql.DataSource createDataSource(DataSourceProperties properties) {
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setUrl(properties.url());
    dataSource.setUsername(properties.username());
    dataSource.setPassword(properties.password());
    return dataSource;
  }
}
