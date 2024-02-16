package com.breskul.bibernate.persistence.datasource.connectionpools;

import com.breskul.bibernate.persistence.datasource.PersistenceProperties;

/**
 * The ConnectionPoolFactory interface defines a method for creating data sources. This interface is used to abstract the creation of
 * {@link javax.sql.DataSource} instances, allowing for the configuration of data sources through {@link PersistenceProperties}.
 */
public interface ConnectionPoolFactory {

  /**
   * Creates and configures a {@link javax.sql.DataSource} instance using the provided {@link PersistenceProperties}. This method allows for
   * the flexible configuration of data sources, enabling the adjustment of settings such as URL, username, password, and connection pool
   * properties.
   *
   * @param properties The {@link PersistenceProperties} used to configure the data source.
   * @return A configured {@link javax.sql.DataSource} instance ready for use.
   */
  javax.sql.DataSource createDataSource(PersistenceProperties properties);
}
