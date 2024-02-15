package com.breskul.bibernate.persistence;

import com.breskul.bibernate.config.PropertiesConfiguration;
import com.breskul.bibernate.ddl.TableCreationService;
import com.breskul.bibernate.metadata.EntitiesMetadataPersistence;
import com.breskul.bibernate.persistence.datasource.BibernateDataSource;
import com.breskul.bibernate.persistence.datasource.DataSourceProperties;
import com.breskul.bibernate.persistence.datasource.connectionpools.CentralConnectionPoolFactory;
import com.breskul.bibernate.persistence.datasource.propertyreader.ApplicationPropertiesReader;
import com.breskul.bibernate.util.EntityUtil;

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
   * Creates a {@link SessionFactory} instance for managing database sessions.
   * This method leverages the application's properties to configure and initialize
   * the data source, and subsequently, the session factory.
   * <p>
   * The process involves reading the {@link DataSourceProperties} using the
   * {@link ApplicationPropertiesReader}, determining the appropriate connection pool
   * factory based on the type of data source specified in the properties, and then
   * creating a data source instance. Finally, a new {@link SessionFactory} is instantiated
   * with the created data source.
   * </p>
   * @return A newly created {@link SessionFactory} instance ready for use in creating
   *         and managing database sessions.
   */
  public static SessionFactory createSessionFactory() {
    DataSourceProperties dataSourceProperties = ApplicationPropertiesReader.getInstance().readProperty();
    var factory = CentralConnectionPoolFactory.getConnectionPoolFactory(dataSourceProperties.type());
    var dataSource = factory.createDataSource(dataSourceProperties);
    EntitiesMetadataPersistence entitiesMetadataPersistence = EntitiesMetadataPersistence.createInstance(
        EntityUtil::getAllEntitiesClasses);
    TableCreationService tableCreationService = new TableCreationService(dataSource, entitiesMetadataPersistence);
    tableCreationService.processDdl();

    return new SessionFactory(dataSource);
  }
}

