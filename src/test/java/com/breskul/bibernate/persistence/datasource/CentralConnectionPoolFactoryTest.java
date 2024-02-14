package com.breskul.bibernate.persistence.datasource;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.breskul.bibernate.persistence.datasource.BibernateDataSource;
import com.breskul.bibernate.persistence.datasource.connectionpools.ApacheDBCP;
import com.breskul.bibernate.persistence.datasource.connectionpools.C3P0;
import com.breskul.bibernate.persistence.datasource.connectionpools.CentralConnectionPoolFactory;
import com.breskul.bibernate.persistence.datasource.connectionpools.ConnectionPoolFactory;
import com.breskul.bibernate.persistence.datasource.connectionpools.HikariCP;
import org.junit.jupiter.api.Test;

public class CentralConnectionPoolFactoryTest {
  @Test
  public void testGetConnectionPoolFactoryWithApache() {
    ConnectionPoolFactory factory = CentralConnectionPoolFactory.getConnectionPoolFactory("Apache");
    assertInstanceOf(ApacheDBCP.class, factory, "Factory should be an instance of ApacheDBCP");
  }

  @Test
  public void testGetConnectionPoolFactoryWithHikariCP() {
    ConnectionPoolFactory factory = CentralConnectionPoolFactory.getConnectionPoolFactory("HikariCP");
    assertInstanceOf(HikariCP.class, factory, "Factory should be an instance of HikariCP");
  }

  @Test
  public void testGetConnectionPoolFactoryWithC3P0() {
    ConnectionPoolFactory factory = CentralConnectionPoolFactory.getConnectionPoolFactory("c3p0");
    assertInstanceOf(C3P0.class, factory, "Factory should be an instance of C3P0");
  }

  @Test
  public void testGetConnectionPoolFactoryWithUnsupportedTypeReturnsBibernateDataSource() {
    ConnectionPoolFactory factory = CentralConnectionPoolFactory.getConnectionPoolFactory("Unsupported");
    assertInstanceOf(BibernateDataSource.class, factory,
        "Factory should return a BibernateDataSource for unsupported types");
  }
}
