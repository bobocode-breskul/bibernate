package com.breskul.bibernate.persistence.datasource.connectiopool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.breskul.bibernate.persistence.datasource.DataSourceProperties;
import com.breskul.bibernate.persistence.datasource.connectionpools.C3P0;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.junit.jupiter.api.Test;

public class C3P0Test {

  @Test
  void testCreateDataSource() {
    DataSourceProperties mockProperties = mock(DataSourceProperties.class);
    when(mockProperties.url()).thenReturn("jdbc:testdb:url");
    when(mockProperties.username()).thenReturn("testUser");
    when(mockProperties.password()).thenReturn("testPass");

    C3P0 c3P0 = new C3P0();

    javax.sql.DataSource dataSource = c3P0.createDataSource(mockProperties);


    assertNotNull(dataSource, "DataSource should not be null");
    assertInstanceOf(ComboPooledDataSource.class, dataSource,
        "DataSource should be an instance of BasicDataSource");
    ComboPooledDataSource comboPooledDataSource = (ComboPooledDataSource) dataSource;
    assertEquals("jdbc:testdb:url", comboPooledDataSource.getJdbcUrl(), "URL should match the mock property");
    assertEquals("testUser", comboPooledDataSource.getUser(), "Username should match the mock property");
    assertEquals("testPass", comboPooledDataSource.getPassword(), "Password should match the mock property");
  }
}
