package com.breskul.bibernate.persistence.datasource.connectionpools;

import com.breskul.bibernate.persistence.datasource.DataSourceProperties;
import org.apache.commons.dbcp2.BasicDataSource;

public class ApacheDBCPFactory implements ConnectionPoolFactory {
  @Override
  public javax.sql.DataSource createDataSource(DataSourceProperties properties) {
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setUrl(properties.url());
    dataSource.setUsername(properties.username());
    dataSource.setPassword(properties.password());
    return dataSource;
  }
}
