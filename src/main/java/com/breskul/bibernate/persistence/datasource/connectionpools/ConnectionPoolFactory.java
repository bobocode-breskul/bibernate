package com.breskul.bibernate.persistence.datasource.connectionpools;

import com.breskul.bibernate.persistence.datasource.DataSourceProperties;

public interface ConnectionPoolFactory {
  javax.sql.DataSource createDataSource(DataSourceProperties properties);
}
