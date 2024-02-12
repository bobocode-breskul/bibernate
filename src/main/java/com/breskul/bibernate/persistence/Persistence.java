package com.breskul.bibernate.persistence;

import com.breskul.bibernate.config.PropertiesConfiguration;
import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.persistence.datasource.BibernateDataSource;
import com.breskul.bibernate.persistence.datasource.DataSourceProperties;
import com.breskul.bibernate.persistence.datasource.connectionpools.CentralConnectionPoolFactory;
import com.breskul.bibernate.persistence.datasource.connectionpools.ConnectionPoolFactory;
import com.breskul.bibernate.persistence.datasource.propertyreader.ApplicationPropertiesReader;
import java.sql.Connection;

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
    DataSourceProperties dataSourceProperties = ApplicationPropertiesReader.getInstance().readProperty();
//    BibernateDataSource dataSource = new BibernateDataSource(dataSourceProperties);
    ConnectionPoolFactory factory = CentralConnectionPoolFactory.getConnectionPoolFactory(dataSourceProperties.type());
    var dataSource = factory.createDataSource(dataSourceProperties);
    return new SessionFactory(dataSource);
  }
}
