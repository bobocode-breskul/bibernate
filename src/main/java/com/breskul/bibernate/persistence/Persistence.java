package com.breskul.bibernate.persistence;

import com.breskul.bibernate.config.PropertiesConfiguration;
import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.persistence.datasource.BibernateDataSource;
import com.breskul.bibernate.persistence.datasource.PersistenceProperties;
import com.breskul.bibernate.persistence.datasource.propertyreader.ApplicationPropertiesReader;
import com.breskul.bibernate.persistence.dialect.Dialect;
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
   * Creates a Hibernate SessionFactory based on configuration properties.
   *
   * @return The created Hibernate SessionFactory.
   * @throws BibernateException If there is an issue with the DataSource or loading the JDBC driver class.
   * @see BibernateDataSource
   * @see SessionFactory
   * @see PropertiesConfiguration
   */
  public static SessionFactory createSessionFactory() {
    PersistenceProperties persistenceProperties = ApplicationPropertiesReader.getInstance().readProperty();
    BibernateDataSource dataSource = new BibernateDataSource(persistenceProperties);

    Dialect dialect = getDialectInstance(persistenceProperties);
    return new SessionFactory(dataSource, dialect);
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
      throw new BibernateException("Provided dialect class '%s' is not found in classPath", e);
    } catch (NoSuchMethodException e) {
      throw new BibernateException("Provided dialect class '%s' is not have required default constructor", e);
    } catch (InvocationTargetException e) {
      throw new BibernateException("Creation of dialect class '%s' failed due to exception inside of constructor", e);
    } catch (InstantiationException e) {
      throw new BibernateException("Default constructor of dialect class '%s' represents an abstract class", e);
    } catch (IllegalAccessException e) {
      throw new BibernateException("Creation of dialect class '%s' failed due to parameter issue", e);
    }
  }
}
