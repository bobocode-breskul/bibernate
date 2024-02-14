package com.breskul.bibernate.integration;

import com.breskul.bibernate.data.Person;
import com.breskul.bibernate.persistence.datasource.BibernateDataSource;
import com.breskul.bibernate.persistence.datasource.DataSourceProperties;
import com.breskul.bibernate.persistence.datasource.propertyreader.ApplicationPropertiesReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractIntegrationTest {

  private static final String PERSON_INSERT_SQL = "insert into persons (id, first_name, last_name) values (?, ?, ?)";
  protected static DataSource dataSource;
  protected Connection connection;

  @BeforeAll
  public static void beforeAll() {
    DataSourceProperties properties = ApplicationPropertiesReader.getInstance().readProperty();
    dataSource = new BibernateDataSource().createDataSource(properties);
  }

  @SneakyThrows
  @BeforeEach
  public void setup() {
    this.connection = dataSource.getConnection();
    this.connection.setAutoCommit(true);
  }


  @AfterEach
  void tearDown() {
    dropTables();
  }

  @SneakyThrows
  private void dropTables() {
    try (PreparedStatement preparedStatement = dataSource.getConnection()
        .prepareStatement("DROP ALL OBJECTS")) {
      preparedStatement.execute();
    }
  }

  @SneakyThrows
  public void createPerson(Person person) {
    try (PreparedStatement preparedStatement = connection.prepareStatement(PERSON_INSERT_SQL)){
      preparedStatement.setObject(1, person.getId());
      preparedStatement.setObject(2, person.getFirstName());
      preparedStatement.setObject(3, person.getLastName());
      preparedStatement.execute();
    }
  }

}


