package com.breskul.bibernate.persistence.datasource;

import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.persistence.datasource.connectionpools.ConnectionPoolFactory;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * The BibernateDataSource class represents a data source for Bibernate connections. It provides flexibility to work with either the default
 * DriverManager-based mechanism for finding the appropriate Driver class or a specified custom Driver class.
 */
public class BibernateDataSource extends AbstractDataSource implements ConnectionPoolFactory {

  protected final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

  private String url;
  private String username;
  private String password;
  private Driver delegateDriver;

  /**
   * Default constructor for BibernateDataSource. This constructor is used when a specific Driver class is not specified. It relies on
   * DriverManager to find the appropriate Driver class based on the connection URL.
   */
  public BibernateDataSource() {
  }

  /**
   * Constructor for BibernateDataSource with a specified driver class. Use this constructor when you need to use a specific or custom
   * Driver class. The provided driver class must be registered. DriverManager is used to find the Driver class by matching it with the
   * given driver class name.
   *
   * @param properties The properties containing the connection URL, username, password, and driver class.
   * @throws BibernateException If the Driver with the specified name is not found or if there is an issue loading the class.
   */

  @Override
  public BibernateDataSource createDataSource(PersistenceProperties properties) {
    this.url = properties.url();
    this.username = properties.username();
    this.password = properties.password();
    this.delegateDriver = Objects.requireNonNull(prepareDriver(properties), "Driver class name cannot be null");
    return this;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return getConnection(this.username, this.password);
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    Properties properties = new Properties();
    properties.setProperty("user", username);
    properties.setProperty("password", password);
    return delegateDriver.connect(this.url, properties);
  }

  private Driver prepareDriver(PersistenceProperties properties) {
    String driverClassName = properties.driverClass();
    if (driverClassName == null) {
      return findDriverByUrl(this.url);
    }
    try {
      Class<?> aClass = Class.forName(driverClassName);
      Optional<Driver> driver = DriverManager.drivers().filter(aClass::isInstance).findFirst();
      return driver.orElseThrow(() ->
          new BibernateException("Driver with name %s not found".formatted(driverClassName)));
    } catch (ClassNotFoundException e) {
      throw new BibernateException("Error loading the Driver class: " + driverClassName, e);
    }
  }

  private Driver findDriverByUrl(String url) {
    Objects.requireNonNull(url);
    try {
      return DriverManager.getDriver(url);
    } catch (SQLException e) {
      throw new BibernateException(String.format("Failed to get driver instance for jdbcUrl=%s", url), e);
    }
  }
}
