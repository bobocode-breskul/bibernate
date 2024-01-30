package com.breskul.bibernate.persistence;

import com.breskul.bibernate.config.PropertiesConfiguration;
import com.breskul.bibernate.exception.BibernateException;
import java.util.Objects;

/**
 * The Persistence class provides a convenient way to create a Bibernate SessionFactory based on configuration properties. It retrieves
 * connection details from a properties file using {@link PropertiesConfiguration} and initializes a {@link BibernateDataSource}
 * accordingly. The default behavior is to use a generic DataSource, but you can specify a custom JDBC driver class if needed.
 * <p>
 * Usage Example:
 * <pre>
 *   {@code
 *  SessionFactory sessionFactory = Persistence.createSessionFactory();
 * }
 * </pre>
 */
public class Persistence {

  private static final String CONNECTION_URL = "bibernate.connection.url";
  private static final String CONNECTION_USERNAME = "bibernate.connection.username";
  private static final String CONNECTION_PASSWORD = "bibernate.connection.password";
  private static final String CONNECTION_DRIVER_CLASS = "bibernate.connection.driver_class";

  /**
   * Creates a Hibernate SessionFactory based on configuration properties.
   *
   * @return The created Hibernate SessionFactory.
   * @throws BibernateException If there is an issue with the DataSource or loading the JDBC driver class.
   * @see BibernateDataSource
   * @see SessionFactory
   * @see PropertiesConfiguration
   */
  public static SessionFactory createSessionFactory() {
    String url = Objects.requireNonNull(PropertiesConfiguration.getProperty(CONNECTION_URL));
    String username = Objects.requireNonNull(PropertiesConfiguration.getProperty(CONNECTION_USERNAME));
    String password = Objects.requireNonNull(PropertiesConfiguration.getProperty(CONNECTION_PASSWORD));
    String driverClass = PropertiesConfiguration.getPropertyOrDefault(CONNECTION_DRIVER_CLASS, null);

    BibernateDataSource dataSource;
    if (driverClass == null) {
      dataSource = new BibernateDataSource();
    } else {
      dataSource = new BibernateDataSource(driverClass);
    }
    dataSource.setUrl(url);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    return new SessionFactory(dataSource);
  }
}
