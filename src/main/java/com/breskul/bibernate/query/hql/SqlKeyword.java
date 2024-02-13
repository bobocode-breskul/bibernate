package com.breskul.bibernate.query.hql;

/**
 * Enumerates common SQL keywords used in query construction.
 * This enumeration provides a type-safe way to reference SQL keywords within the application,
 * ensuring consistency and reducing the likelihood of typos in query strings.
 */
public enum SqlKeyword {
  /** Represents the 'SELECT' keyword used in SQL queries to specify the columns to be returned. */
  SELECT,
  /** Represents the 'FROM' keyword used in SQL queries to specify the table from which to retrieve data. */
  FROM,
  /** Represents the 'WHERE' keyword used in SQL queries to specify conditions that must be met for a row to be included in the result set. */
  WHERE
}
