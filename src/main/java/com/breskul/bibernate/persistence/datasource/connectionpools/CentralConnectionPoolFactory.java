package com.breskul.bibernate.persistence.datasource.connectionpools;

/**
 * CentralConnectionPoolFactory serves as a factory class for creating instances
 * of {@link ConnectionPoolFactory} based on a given type. It supports creating
 * connection pool factories for various implementations like Apache DBCP, HikariCP,
 * and C3P0.
 */
public class CentralConnectionPoolFactory {

  /**
   * Returns an instance of {@link ConnectionPoolFactory} based on the specified type.
   * This method facilitates the dynamic selection of connection pool implementations
   * at runtime, allowing clients to choose between different connection pooling strategies.
   * <p>
   * Supported types include:
   * <ul>
   *   <li>"Apache" - Returns an instance of {@link ApacheDBCP}.</li>
   *   <li>"HikariCP" - Returns an instance of {@link HikariCP}.</li>
   *   <li>"c3p0" - Returns an instance of {@link C3P0}.</li>
   * </ul>
   * If an unsupported type is provided, an {@link IllegalArgumentException} is thrown.
   * </p>
   * @param type The type of connection pool factory to create. This is a case-sensitive string.
   * @return An instance of {@link ConnectionPoolFactory} corresponding to the specified type.
   * @throws IllegalArgumentException If the specified type is not supported.
   */
  public static ConnectionPoolFactory getConnectionPoolFactory(String type) {
    return switch (type) {
      case "Apache" -> new ApacheDBCP();
      case "HikariCP" -> new HikariCP();
      case "c3p0" -> new C3P0();
      default -> throw new IllegalArgumentException("Invalid connection pool type: " + type);
    };
  }
}
