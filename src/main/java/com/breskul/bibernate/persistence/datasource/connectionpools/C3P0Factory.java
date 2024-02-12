package com.breskul.bibernate.persistence.datasource.connectionpools;

import com.breskul.bibernate.persistence.datasource.DataSourceProperties;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0Factory implements ConnectionPoolFactory {
  @Override
  public javax.sql.DataSource createDataSource(DataSourceProperties properties) {
    ComboPooledDataSource cpds = new ComboPooledDataSource();
    cpds.setJdbcUrl(properties.url());
    cpds.setUser(properties.username());
    cpds.setPassword(properties.password());
    return cpds;
  }
}
