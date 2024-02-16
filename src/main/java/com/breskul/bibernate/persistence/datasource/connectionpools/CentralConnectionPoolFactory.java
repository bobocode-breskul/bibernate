package com.breskul.bibernate.persistence.datasource.connectionpools;

import com.breskul.bibernate.persistence.datasource.BibernateDataSource;

/**
 * CentralConnectionPoolFactory serves as a factory class for creating instances of {@link ConnectionPoolFactory} based on a given type. It
 * supports creating connection pool factories for various implementations like Apache DBCP, HikariCP, and C3P0.
 */
public class CentralConnectionPoolFactory {

  /**
   * Retrieves an instance of {@link ConnectionPoolFactory} based on the specified type. This method allows for the dynamic selection of
   * connection pool implementations at runtime, offering flexibility in choosing the underlying connection pooling mechanism. Supported
   * types and their corresponding returned instances include:
   * <ul>
   *   <li>"Apache" - Returns an instance of Apache DBCP connection pool.</li>
   *   <li>"HikariCP" - Returns an instance of HikariCP connection pool.</li>
   *   <li>"c3p0" - Returns an instance of c3p0 connection pool.</li>
   * </ul>
   * For any other type, this method defaults to returning an instance of
   * BibernateDataSource, providing a fallback option for database connectivity.
   *
   * @param type The type of connection pool factory to create, specified as a case-sensitive string.
   * @return An instance of {@link ConnectionPoolFactory} corresponding to the specified type, or a BibernateDataSource as the default
   * default option.
   */
  public static ConnectionPoolFactory getConnectionPoolFactory(String type) {
    return switch (type) {
      case "Apache" -> new ApacheDBCP();
      case "HikariCP" -> new HikariCP();
      case "c3p0" -> new C3P0();
      default -> new BibernateDataSource();
    };
  }
}
