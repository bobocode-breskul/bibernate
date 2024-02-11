package com.breskul.bibernate.ddl;

import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.metadata.Column;
import com.breskul.bibernate.metadata.EntitiesMetadataPersistence;
import com.breskul.bibernate.metadata.Table;
import com.breskul.bibernate.metadata.dto.ForeignKey;
import com.breskul.bibernate.persistence.datasource.propertyreader.ApplicationPropertiesReader;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;

public class TableCreationService {
  private static final Logger logger = LoggerFactory.getLogger(TableCreationService.class);
  private static final String DROP_TABLE_SQL = "drop table if exists %s cascade";
  private static final String UNIQUE = "UNIQUE";
  private static final String NOT_NULL = "NOT NULL";
  private static final String PRIMARY_KEY = "PRIMARY KEY";
  String ADD_FOREIGN_KEY_SQL = """
        ALTER TABLE IF EXISTS %s
        ADD CONSTRAINT %s 
        FOREIGN KEY (%s)
        REFERENCES %s
        """;

  private final DataSource dataSource;
  private final ApplicationPropertiesReader propertiesReader;
  private final EntitiesMetadataPersistence entitiesMetadataPersistence;



  public TableCreationService(DataSource dataSource, ApplicationPropertiesReader propertiesReader,
      EntitiesMetadataPersistence entitiesMetadataPersistence) {
    this.dataSource = dataSource;
    this.propertiesReader = propertiesReader;
    this.entitiesMetadataPersistence = entitiesMetadataPersistence;
  }

  public void processDdl(){
    dropAllTables();
    createTables();
    addForeignKeys();
    // todo: read property and decide if DLL generation is required
    // todo: drop and create tables for found entities
  }

  public void dropAllTables() {
    try(var connection = dataSource.getConnection()) {
      Statement statement = connection.createStatement();
      for (Table table : entitiesMetadataPersistence.getTables()) {
        String dropSql = DROP_TABLE_SQL.formatted(table.getFullName());
        logger.info("Bibernate: " + dropSql);
        statement.execute(dropSql);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  public void createTables() {
    try (var connection = dataSource.getConnection()) {
      Statement statement = connection.createStatement();
      for (Table table : entitiesMetadataPersistence.getTables()) {
        String createQuery = generateCreateTableSql(table);
        logger.info("Bibernate: " + createQuery);
        statement.executeUpdate(createQuery);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void addForeignKeys() {
    try (var connection = dataSource.getConnection()) {
      Statement statement = connection.createStatement();
      for (Table table : entitiesMetadataPersistence.getTables()) {
        Set<ForeignKey> foreignKeys = table.getForeignKeys();
        if (!foreignKeys.isEmpty()) {
          for (ForeignKey fk: foreignKeys) {
            String createQuery = generateFKeySQL(fk);
            logger.info("Bibernate: " + createQuery);
            statement.executeUpdate(createQuery);
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private String generateFKeySQL(ForeignKey fk) {
    return ADD_FOREIGN_KEY_SQL.formatted(fk.getTableName(), fk.getConstraintId(), fk.getFieldName(),
            fk.getRelatedTableName());

  }
  private String generateCreateTableSql(Table table) {
    StringBuilder sql = new StringBuilder("create table %s(".formatted(table.getFullName()));
    for (Column column: table.getColumns()) {
      sql.append(System.lineSeparator());
      sql.append("\t%s %s".formatted(column.getName(), column.getSqlTypeName()));
      if (column.isUnique()) {
        sql.append(" " + UNIQUE);
      }
      if (!column.isNullable()) {
        sql.append(" " + NOT_NULL);
      }
      if (column.isPrimaryKey()) {
        sql.append(" " + PRIMARY_KEY);
      }
      sql.append(",");
    }

    sql.replace(sql.length() - 1, sql.length(), System.lineSeparator()); // change last "," on separate sign.
    sql.append(");");


    return sql.toString();
  }

}
