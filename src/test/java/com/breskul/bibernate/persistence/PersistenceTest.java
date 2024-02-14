package com.breskul.bibernate.persistence;

import com.breskul.bibernate.persistence.datasource.DataSourceProperties;
import com.breskul.bibernate.persistence.datasource.connectionpools.CentralConnectionPoolFactory;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
public class PersistenceTest {

  @Test
  public
  void testCreateSessionFactory() {
    DataSourceProperties properties = mock(DataSourceProperties.class);
    when(properties.type()).thenReturn("HikariCP");
    when(properties.url()).thenReturn("jdbc:h2:mem:test");
    when(properties.username()).thenReturn("sa");
    when(properties.password()).thenReturn("");
    when(properties.driverClass()).thenReturn("org.h2.Driver");
    DataSource dataSource = CentralConnectionPoolFactory.getConnectionPoolFactory(properties.type()).createDataSource(properties);
    assertNotNull(dataSource);
  }
}
