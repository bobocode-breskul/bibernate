package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.ddl.TableCreationService.CREATE_TABLES_PROPERTY_NAME;

import com.breskul.bibernate.config.PropertiesConfiguration;
import com.breskul.bibernate.ddl.TableCreationService;
import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.metadata.EntitiesMetadataPersistence;
import com.breskul.bibernate.persistence.datasource.BibernateDataSource;
import com.breskul.bibernate.persistence.datasource.PersistenceProperties;
import com.breskul.bibernate.persistence.datasource.connectionpools.CentralConnectionPoolFactory;
import com.breskul.bibernate.persistence.datasource.propertyreader.ApplicationPropertiesReader;
import com.breskul.bibernate.persistence.dialect.Dialect;
import com.breskul.bibernate.util.EntityUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
   * Creates a {@link SessionFactory} instance for managing database sessions. This method leverages the application's properties to
   * configure and initialize the data source, and subsequently, the session factory.
   * <p>
   * The process involves reading the {@link PersistenceProperties} using the {@link ApplicationPropertiesReader}, determining the
   * appropriate connection pool factory based on the type of data source specified in the properties, and then creating a data source
   * instance. Finally, a new {@link SessionFactory} is instantiated with the created data source.
   * </p>
   *
   * @return A newly created {@link SessionFactory} instance ready for use in creating and managing database sessions.
   */
  public static SessionFactory createSessionFactory() {
    PersistenceProperties persistenceProperties = ApplicationPropertiesReader.getInstance().readProperty();
    var factory = CentralConnectionPoolFactory.getConnectionPoolFactory(persistenceProperties.type());
    var dataSource = factory.createDataSource(persistenceProperties);

    EntitiesMetadataPersistence entitiesMetadataPersistence = EntitiesMetadataPersistence.createInstance(
        EntityUtil::getAllEntitiesClasses);
    boolean createTables = Boolean.parseBoolean(
        PropertiesConfiguration.getPropertyOrDefault(CREATE_TABLES_PROPERTY_NAME, "false"));
    if (createTables) {
      TableCreationService tableCreationService = new TableCreationService(dataSource, entitiesMetadataPersistence,
          persistenceProperties.showSql());
      tableCreationService.processDdl();
    }

    Dialect dialect = getDialectInstance(persistenceProperties);
    return new SessionFactory(dataSource, dialect, persistenceProperties.showSql());
  }

  private static Dialect getDialectInstance(PersistenceProperties persistenceProperties) {
    String dialectClassName = persistenceProperties.dialectClass();
    if (dialectClassName == null) {
      return null;
    }
    try {
      Class<?> dialectClass = Class.forName(dialectClassName);
      Constructor<?> declaredConstructor = dialectClass.getDeclaredConstructor();
      Object instance = declaredConstructor.newInstance();
      if (instance instanceof Dialect dialect) {
        return dialect;
      }
      throw new BibernateException("Provided dialect class '%s' is not a instance of Dialect interface".formatted(dialectClassName));
    } catch (ClassNotFoundException e) {
      throw new BibernateException(
          "Provided dialect class '%s' is not found in classPath".formatted(dialectClassName), e);
    } catch (NoSuchMethodException e) {
      throw new BibernateException(
          "Provided dialect class '%s' is not have required default constructor".formatted(
              dialectClassName), e);
    } catch (InvocationTargetException e) {
      throw new BibernateException(
          "Creation of dialect class '%s' failed due to exception inside of constructor".formatted(
              dialectClassName), e);
    } catch (InstantiationException e) {
      throw new BibernateException(
          "Default constructor of dialect class '%s' represents an abstract class".formatted(
              dialectClassName), e);
    } catch (IllegalAccessException e) {
      throw new BibernateException(
          "Creation of dialect class '%s' failed due to parameter issue".formatted(
              dialectClassName), e);
    }
  }
}

