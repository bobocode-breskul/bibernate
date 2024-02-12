package com.breskul.bibernate.persistence.datasource.connectionpools;

public class CentralConnectionPoolFactory {
  public static ConnectionPoolFactory getConnectionPoolFactory(String type) {
    return switch (type) {
      case "Apache" -> new ApacheDBCPFactory();
      case "HikariCP" -> new HikariCPFactory();
      case "c3p0" -> new C3P0Factory();
      default -> throw new IllegalArgumentException("Invalid connection pool type: " + type);
    };
  }
}
