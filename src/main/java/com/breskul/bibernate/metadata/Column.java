package com.breskul.bibernate.metadata;

import java.sql.JDBCType;
import lombok.Data;

@Data
public class Column {
  private Integer length;
  private Integer precision;
  private Integer scale;
  private String name;
  private boolean nullable = true;
  private boolean unique;
  private String sqlTypeName;
  private boolean isPrimaryKey;
}
