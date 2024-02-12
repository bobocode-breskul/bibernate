package com.breskul.bibernate.persistence.datasource.connectionpools;

import com.breskul.bibernate.persistence.datasource.DataSourceProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class HikariCPFactory implements ConnectionPoolFactory {
  @Override
  public javax.sql.DataSource createDataSource(DataSourceProperties properties) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(properties.url());
    config.setUsername(properties.username());
    config.setPassword(properties.password());
    return new HikariDataSource(config);
  }
}
