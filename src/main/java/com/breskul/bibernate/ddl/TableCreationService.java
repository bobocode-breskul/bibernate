package com.breskul.bibernate.ddl;

import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.config.PropertiesConfiguration;
import com.breskul.bibernate.metadata.Column;
import com.breskul.bibernate.metadata.EntitiesMetadataPersistence;
import com.breskul.bibernate.metadata.Table;
import com.breskul.bibernate.metadata.dto.ForeignKey;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;


/**
 * The TableCreationService class is responsible for creating, dropping, and adding foreign keys to database tables.
 */
public class TableCreationService {
  private static final Logger logger = LoggerFactory.getLogger(TableCreationService.class);
  public static final String CREATE_TABLES_PROPERTY_NAME = "bibernate.ddl.create_tables";
  private static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS %s CASCADE";
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
  private final EntitiesMetadataPersistence entitiesMetadataPersistence;



  public TableCreationService(DataSource dataSource, EntitiesMetadataPersistence entitiesMetadataPersistence) {
    this.dataSource = dataSource;
    this.entitiesMetadataPersistence = entitiesMetadataPersistence;
  }

  /**
   * Processes Data Definition Language (DDL) statements to create tables and add foreign keys if specified by the configuration.
   * The process involves dropping all existing tables, creating new tables, and adding foreign keys if necessary.
   *
   * @see TableCreationService#dropAllTables()
   * @see TableCreationService#createTables()
   * @see TableCreationService#addForeignKeys()
   * @see PropertiesConfiguration#getPropertyOrDefault(String, String)
   */
  public void processDdl(){
      dropAllTables();
      createTables();
      addForeignKeys();
  }

  private void dropAllTables() {
    try(var connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      for (Table table : entitiesMetadataPersistence.getTables()) {
        String dropSql = DROP_TABLE_SQL.formatted(table.getFullName());
        logger.info("Bibernate: " + dropSql);
        statement.execute(dropSql);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void createTables() {
    try (var connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
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
    try (var connection = dataSource.getConnection();
      Statement statement = connection.createStatement()) {
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
    return ADD_FOREIGN_KEY_SQL.formatted(fk.getTableName(), fk.getConstraintName(), fk.getFieldName(),
            fk.getRelatedTableName());

  }
  private String generateCreateTableSql(Table table) {
    StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS %s(".formatted(table.getFullName()));
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
