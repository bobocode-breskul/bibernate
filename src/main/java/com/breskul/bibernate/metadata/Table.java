package com.breskul.bibernate.metadata;

import com.breskul.bibernate.metadata.Column;
import com.breskul.bibernate.metadata.dto.ForeignKey;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
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

  public Set<Column> getColumns() {
    return new LinkedHashSet<>(columns.values());
  }

  public void addForeignKey(ForeignKey key) {
    foreignKeys.put(key.getFieldName(), key);
  }

  public Set<ForeignKey> getForeignKeys() {
    return new LinkedHashSet<>(foreignKeys.values());
  }


//  private KeyValue idValue;
//  private PrimaryKey primaryKey;
//  private final Map<ForeignKeyKey, ForeignKey> foreignKeys = new LinkedHashMap<>();
//  private final Map<String, Index> indexes = new LinkedHashMap<>();
//  private final Map<String,UniqueKey> uniqueKeys = new LinkedHashMap<>();
}
