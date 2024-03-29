package com.breskul.bibernate.persistence.datasource.propertyreader;

import com.breskul.bibernate.config.PropertiesConfiguration;
import com.breskul.bibernate.persistence.datasource.PersistenceProperties;
import java.util.Objects;

/**
 * The ApplicationPropertiesReader class is responsible for reading application properties related to database connections and providing
 * them as DataSourceProperties.
 * <p>
 * This class follows the Singleton pattern to ensure there is only one instance in the application, providing a single point for reading
 * application properties.
 */
public class ApplicationPropertiesReader implements PropertyReader {

  private static final String CONNECTION_URL = "bibernate.connection.url";
  private static final String CONNECTION_USERNAME = "bibernate.connection.username";
  private static final String CONNECTION_PASSWORD = "bibernate.connection.password";
  private static final String CONNECTION_DRIVER_CLASS = "bibernate.connection.driver_class";
  private static final String DIALECT_CLASS = "bibernate.dialect";
  private static final String DATASOURCE_TYPE = "bibernate.datasource.type";
  private static final String SHOW_SQL = "bibernate.show_sql";

  private static final ApplicationPropertiesReader INSTANCE = new ApplicationPropertiesReader();

  // Private constructor to prevent instantiation
  private ApplicationPropertiesReader() {
  }

  /**
   * Get the singleton instance of ApplicationPropertiesReader.
   *
   * @return The singleton instance of ApplicationPropertiesReader.
   */
  public static ApplicationPropertiesReader getInstance() {
    return INSTANCE;
  }

  /**
   * Reads application properties related to database connections and returns them as DataSourceProperties.
   *
   * @return DataSourceProperties containing the connection URL, username, password, and driver class.
   * @throws NullPointerException If any of the required properties is null.
   * @see PersistenceProperties
   */
  @Override
  public PersistenceProperties readProperty() {
    String url = Objects.requireNonNull(PropertiesConfiguration.getProperty(CONNECTION_URL), "Connection URL cannot be null");
    String username = Objects.requireNonNull(PropertiesConfiguration.getProperty(CONNECTION_USERNAME),
        "Connection username cannot be null");
    String password = Objects.requireNonNull(PropertiesConfiguration.getProperty(CONNECTION_PASSWORD),
        "Connection password cannot be null");
    String type = Objects.requireNonNull(PropertiesConfiguration.getProperty(DATASOURCE_TYPE), "Datasource type cannot be null");
    String driverClass = PropertiesConfiguration.getPropertyOrDefault(CONNECTION_DRIVER_CLASS, null);
    String dialectClass = PropertiesConfiguration.getPropertyOrDefault(DIALECT_CLASS, null);
    boolean showSql = Boolean.parseBoolean(PropertiesConfiguration.getPropertyOrDefault(SHOW_SQL, "").toLowerCase());

    return new PersistenceProperties(url, username, password, driverClass, type, dialectClass, showSql);
  }
}

