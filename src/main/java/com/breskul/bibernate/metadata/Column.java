package com.breskul.bibernate.metadata;

/**
 * Represents a column metadata.
 */
public class Column {
  private Integer length;
  private Integer precision;
  private Integer scale;
  private String name;
  private boolean nullable = true;
  private boolean unique;
  private String sqlTypeName;
  private boolean isPrimaryKey;
  private Class<?> javaType;

  public Integer getLength() {
    return length;
  }

  public void setLength(Integer length) {
    this.length = length;
  }

  public Integer getPrecision() {
    return precision;
  }

  public void setPrecision(Integer precision) {
    this.precision = precision;
  }

  public Integer getScale() {
    return scale;
  }

  public void setScale(Integer scale) {
    this.scale = scale;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public boolean isUnique() {
    return unique;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public String getSqlTypeName() {
    return sqlTypeName;
  }

  public void setSqlTypeName(String sqlTypeName) {
    this.sqlTypeName = sqlTypeName;
  }

  public boolean isPrimaryKey() {
    return isPrimaryKey;
  }

  public void setPrimaryKey(boolean primaryKey) {
    isPrimaryKey = primaryKey;
  }

  public Class<?> getJavaType() {
    return javaType;
  }

  public void setJavaType(Class<?> javaType) {
    this.javaType = javaType;
  }
}
