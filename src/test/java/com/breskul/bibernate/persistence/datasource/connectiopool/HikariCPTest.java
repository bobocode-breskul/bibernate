package com.breskul.bibernate.persistence.datasource.connectiopool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.breskul.bibernate.persistence.datasource.DataSourceProperties;
import com.breskul.bibernate.persistence.datasource.connectionpools.HikariCP;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

public class HikariCPTest {

  @Test
  void testCreateDataSource() {
    DataSourceProperties mockProperties = mock(DataSourceProperties.class);
    when(mockProperties.url()).thenReturn("jdbc:h2:mem:testdb");
    when(mockProperties.username()).thenReturn("sa");
    when(mockProperties.password()).thenReturn("");

    HikariCP hikariCP = new HikariCP();

    // Act
    DataSource dataSource = hikariCP.createDataSource(mockProperties);

    // Assert
    assertNotNull(dataSource);
    assertInstanceOf(HikariDataSource.class, dataSource);
    HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
    assertEquals("jdbc:h2:mem:testdb", hikariDataSource.getJdbcUrl());
    assertEquals("sa", hikariDataSource.getUsername());
    assertEquals("", hikariDataSource.getPassword());
  }
}
