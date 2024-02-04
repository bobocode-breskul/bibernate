package com.breskul.bibernate.persistence.datasource.propertyreader;

import com.breskul.bibernate.persistence.datasource.DataSourceProperties;

/**
 * The PropertyReader interface defines the contract for classes that read application properties related to database connections and
 * provide them as DataSourceProperties.
 * <p>
 * Implementing classes are expected to read the necessary properties and return them encapsulated in a {@link DataSourceProperties}
 * object.
 */
public interface PropertyReader {

  /**
   * Reads application properties related to database connections and returns them as DataSourceProperties.
   *
   * @return DataSourceProperties containing the connection URL, username, password, and driver class.
   * @see DataSourceProperties
   */
  DataSourceProperties readProperty();
}
