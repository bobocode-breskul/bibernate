package com.breskul.bibernate.ddl;

import static org.mockito.BDDMockito.then;

import com.breskul.bibernate.metadata.Column;
import com.breskul.bibernate.metadata.EntitiesMetadataPersistence;
import com.breskul.bibernate.metadata.Table;
import com.breskul.bibernate.metadata.dto.ForeignKey;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TableCreationServiceTest {

  public static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS TestCatalog.TestSchema.TestTable CASCADE";
  public static final String CREATE_TABLE_SQL = """
      CREATE TABLE IF NOT EXISTS TestCatalog.TestSchema.TestTable(
      	id INTEGER PRIMARY KEY,
      	name2 VARCHAR UNIQUE NOT NULL
      );""";

  private static final String CONSTRAINT_SQL =""" 
  ALTER TABLE IF EXISTS TestTable
  ADD CONSTRAINT test_parent_fk
  FOREIGN KEY (parent_id)
  REFERENCES ParentTable
  """;
  @Mock
  private DataSource dataSource;

  @Mock
  private EntitiesMetadataPersistence entitiesMetadataPersistence;

  @Mock
  private Connection connection;

  @Mock
  private Statement statement;

  @InjectMocks
  private TableCreationService tableCreationService;

//  drop table if exists TestCatalog.TestSchema.TestTable cascade





  @Test
  public void testProcessDdl() throws SQLException {
    // Arrange
    Mockito.when(entitiesMetadataPersistence.getTables())
        .thenReturn(prepareTable());
    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.createStatement()).thenReturn(statement);
    tableCreationService.processDdl();

    then(statement).should().execute(DROP_TABLE_SQL);
    then(statement).should().executeUpdate(CREATE_TABLE_SQL);
    then(statement).should().executeUpdate(CONSTRAINT_SQL);
    then(statement).shouldHaveNoMoreInteractions();
  }

  private Set<Table> prepareTable() {
    Table table = new Table();
    table.setName("TestTable");
    table.setCatalog("TestCatalog");
    table.setSchema("TestSchema");
    table.setFullName("TestCatalog.TestSchema.TestTable");
    table.setColumns(prepareColumn());
    table.addForeignKey(prepareForeignKey());
    return Set.of(table);
  }

  private ForeignKey prepareForeignKey() {
    return new ForeignKey("TestTable", "parent_id", "ParentTable", "test_parent_fk");
  }


  private Map<String, Column> prepareColumn() {
    Column column1 = new Column();
    column1.setPrimaryKey(true);
    column1.setName("id");
    column1.setSqlTypeName("INTEGER");

    Column column2 = new Column();
    column2.setName("name2");
    column2.setSqlTypeName("VARCHAR");
    column2.setNullable(false);
    column2.setUnique(true);

    Map<String, Column> columnMap = new LinkedHashMap<>();
    columnMap.put("id", column1);
    columnMap.put("name2", column2);
    return columnMap;
  }

}