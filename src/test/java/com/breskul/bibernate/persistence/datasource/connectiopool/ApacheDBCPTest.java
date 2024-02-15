package com.breskul.bibernate.persistence.datasource.connectiopool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.breskul.bibernate.persistence.datasource.PersistenceProperties;
import com.breskul.bibernate.persistence.datasource.connectionpools.ApacheDBCP;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.Test;

public class ApacheDBCPTest {
  @Test
  void testCreateDataSource() {
    PersistenceProperties mockProperties = mock(PersistenceProperties.class);
    when(mockProperties.url()).thenReturn("jdbc:testdb:url");
    when(mockProperties.username()).thenReturn("testUser");
    when(mockProperties.password()).thenReturn("testPass");

    ApacheDBCP apacheDBCP = new ApacheDBCP();

    javax.sql.DataSource dataSource = apacheDBCP.createDataSource(mockProperties);


    assertNotNull(dataSource, "DataSource should not be null");
    assertInstanceOf(BasicDataSource.class, dataSource,
        "DataSource should be an instance of BasicDataSource");
    BasicDataSource basicDataSource = (BasicDataSource) dataSource;
    assertEquals("jdbc:testdb:url", basicDataSource.getUrl(), "URL should match the mock property");
    assertEquals("testUser", basicDataSource.getUsername(), "Username should match the mock property");
    assertEquals("testPass", basicDataSource.getPassword(), "Password should match the mock property");
  }
}
