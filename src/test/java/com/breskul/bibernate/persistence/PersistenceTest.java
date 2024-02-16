package com.breskul.bibernate.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.persistence.datasource.PersistenceProperties;
import com.breskul.bibernate.persistence.datasource.connectionpools.CentralConnectionPoolFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PersistenceTest {

  private PersistenceProperties properties;

  @BeforeEach
  void setUp() {
    properties = mock(PersistenceProperties.class);
    when(properties.type()).thenReturn("HikariCP");
    when(properties.url()).thenReturn("jdbc:h2:mem:test");
    when(properties.username()).thenReturn("sa");
    when(properties.password()).thenReturn("");
    when(properties.driverClass()).thenReturn("org.h2.Driver");
  }

  @Test
  public void testCreateSessionFactory() {
    DataSource dataSource = CentralConnectionPoolFactory.getConnectionPoolFactory(properties.type()).createDataSource(properties);
    assertNotNull(dataSource);
  }

  @Test
  public void testGetDialectInstanceWithInvalidDialectClassNotFound() {
    when(properties.dialectClass()).thenReturn(
        "com.breskul.bibernate.persistence.dialect.InvalidDialect");

    assertThatThrownBy(() -> invokePrivateStaticMethod("getDialectInstance", properties))
        .isInstanceOf(InvocationTargetException.class)
        .extracting(Throwable::getCause)
        .isInstanceOf(BibernateException.class)
        .hasToString(
            "com.breskul.bibernate.exception.BibernateException: Provided dialect class 'com.breskul.bibernate.persistence.dialect.InvalidDialect' is not found in classPath");
  }

  @Test
  public void testGetDialectInstanceWithInvalidDialectMissingConstructor() {
    when(properties.dialectClass()).thenReturn(
        "com.breskul.bibernate.persistence.dialect.InvalidDialectMissingConstructor");

    assertThatThrownBy(() -> invokePrivateStaticMethod("getDialectInstance", properties))
        .isInstanceOf(InvocationTargetException.class)
        .extracting(Throwable::getCause)
        .isInstanceOf(BibernateException.class)
        .hasToString(
            "com.breskul.bibernate.exception.BibernateException: Provided dialect class 'com.breskul.bibernate.persistence.dialect.InvalidDialectMissingConstructor' is not have required default constructor");
  }

  @Test
  public void testGetDialectInstanceWithInvalidDialectAbstractClass() {
    when(properties.dialectClass()).thenReturn(
        "com.breskul.bibernate.persistence.dialect.InvalidDialectAbstractClass");

    assertThatThrownBy(() -> invokePrivateStaticMethod("getDialectInstance", properties))
        .isInstanceOf(InvocationTargetException.class)
        .extracting(Throwable::getCause)
        .isInstanceOf(BibernateException.class)
        .hasToString(
            "com.breskul.bibernate.exception.BibernateException: Default constructor of dialect class 'com.breskul.bibernate.persistence.dialect.InvalidDialectAbstractClass' represents an abstract class");
  }

  @Test
  public void testGetDialectInstanceWithInvalidDialectExceptionInConstructor() {
    when(properties.dialectClass()).thenReturn(
        "com.breskul.bibernate.persistence.dialect.InvalidDialectExceptionInConstructor");

    assertThatThrownBy(() -> invokePrivateStaticMethod("getDialectInstance", properties))
        .isInstanceOf(InvocationTargetException.class)
        .extracting(Throwable::getCause)
        .isInstanceOf(BibernateException.class)
        .hasToString(
            "com.breskul.bibernate.exception.BibernateException: Creation of dialect class 'com.breskul.bibernate.persistence.dialect.InvalidDialectExceptionInConstructor' failed due to exception inside of constructor");
  }

  @Test
  public void testGetDialectInstanceWithNotDialectClass() {
    when(properties.dialectClass()).thenReturn(
        "com.breskul.bibernate.persistence.dialect.NotDialectClass");

    assertThatThrownBy(() -> invokePrivateStaticMethod("getDialectInstance", properties))
        .isInstanceOf(InvocationTargetException.class)
        .extracting(Throwable::getCause)
        .isInstanceOf(BibernateException.class)
        .hasToString(
            "com.breskul.bibernate.exception.BibernateException: Provided dialect class 'com.breskul.bibernate.persistence.dialect.NotDialectClass' is not a instance of Dialect interface");
  }

  @Test
  public void testGetDialectInstanceWithDialectWithPrivateConstructor() {
    when(properties.dialectClass()).thenReturn(
        "com.breskul.bibernate.persistence.dialect.DialectWithPrivateConstructor");

    assertThatThrownBy(() -> invokePrivateStaticMethod("getDialectInstance", properties))
        .isInstanceOf(InvocationTargetException.class)
        .extracting(Throwable::getCause)
        .isInstanceOf(BibernateException.class)
        .hasToString(
            "com.breskul.bibernate.exception.BibernateException: Creation of dialect class 'com.breskul.bibernate.persistence.dialect.DialectWithPrivateConstructor' failed due to parameter issue");
  }

  @Test
  public void testGetDialectInstanceWithoutDialect() throws Exception {
    Object dialectInstance = invokePrivateStaticMethod("getDialectInstance", properties);
    assertThat(dialectInstance).isNull();
  }

  private <T> T invokePrivateStaticMethod(String methodName, Object... args) throws Exception {
    Method privateMethod = Persistence.class.getDeclaredMethod(methodName,
        PersistenceProperties.class);
    privateMethod.setAccessible(true);

    return (T) privateMethod.invoke(null, args);
  }
}
