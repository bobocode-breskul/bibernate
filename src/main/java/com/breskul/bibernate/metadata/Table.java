package com.breskul.bibernate.metadata;

import com.breskul.bibernate.metadata.dto.ForeignKey;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Table metadata.
 */
public class Table {

  private String catalog;
  private String schema;
  private String name;
  private String fullName;
  private Map<String, ForeignKey> foreignKeys = new LinkedHashMap<>();
  /**
   * contains all columns, including the primary key
   */
  private Map<String, Column> columns = new LinkedHashMap<>();

  /**
   * Returns table columns.
   *
   * @return all table columns.
   */
  public Set<Column> getColumns() {
    return new LinkedHashSet<>(columns.values());
  }

  /**
   * Retrieves the Column object corresponding to the given column name.
   *
   * @param columnName the name of the column
   * @return the Column object corresponding to the given column name, null if no match is found
   */
  public Column getColumn(String columnName) {
    return columns.get(columnName);
  }

  /**
   * Adds a {@code ForeignKey} to the table.
   *
   * @param key the foreign key to be added
   */
  public void addForeignKey(ForeignKey key) {
    foreignKeys.put(key.getFieldName(), key);
  }

  /**
   * Retrieves the {@code ForeignKey} object corresponding by join column field name.
   *
   * @param columnName the name of join column
   * @return the {@code ForeignKey} object corresponding to the given column, null if no match is found
   */
  public ForeignKey getForeignKey(String columnName) {
    return foreignKeys.get(columnName);
  }


  /**
   * Returns the set of foreign keys associated with the table.
   *
   * @return the set of foreign keys
   */
  public Set<ForeignKey> getForeignKeys() {
    return new LinkedHashSet<>(foreignKeys.values());
  }

  public String getCatalog() {
    return catalog;
  }

  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public void setColumns(Map<String, Column> columns) {
    this.columns = columns;
  }
}
