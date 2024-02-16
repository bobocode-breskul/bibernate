package com.breskul.bibernate.integration;

import com.breskul.bibernate.data.DynamicPerson;
import com.breskul.bibernate.data.Note;
import com.breskul.bibernate.data.Person;
import com.breskul.bibernate.persistence.GenericDao;
import com.breskul.bibernate.persistence.context.PersistenceContext;
import com.breskul.bibernate.persistence.datasource.BibernateDataSource;
import com.breskul.bibernate.persistence.datasource.PersistenceProperties;
import com.breskul.bibernate.persistence.datasource.propertyreader.ApplicationPropertiesReader;
import com.breskul.bibernate.persistence.dialect.H2Dialect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

public abstract class AbstractIntegrationTest {

  private static final String PERSON_INSERT_SQL = "insert into persons (id, first_name, last_name) values (?, ?, ?)";
  private static final String NOTE_INSERT_SQL = "insert into notes (id, title, body, person_id) values (?, ?, ?, ?)";
  protected static DataSource dataSource;
  protected Connection connection;
  protected PersistenceContext persistenceContext;
  protected GenericDao genericDao;

  @BeforeAll
  public static void beforeAll() {
    PersistenceProperties properties = ApplicationPropertiesReader.getInstance().readProperty();
    dataSource = new BibernateDataSource().createDataSource(properties);
  }

  @SneakyThrows
  @BeforeEach
  public void setup() {
    this.connection = Mockito.spy(dataSource.getConnection());
    this.connection.setAutoCommit(true);
    this.persistenceContext = new PersistenceContext();
    this.genericDao = new GenericDao(connection, persistenceContext, new H2Dialect(), true);
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
    try (PreparedStatement preparedStatement = connection.prepareStatement(PERSON_INSERT_SQL)) {
      preparedStatement.setObject(1, person.getId());
      preparedStatement.setObject(2, person.getFirstName());
      preparedStatement.setObject(3, person.getLastName());
      preparedStatement.execute();


    }
  }
  @SneakyThrows
  public void createDynamicPerson(DynamicPerson person) {
    try (PreparedStatement preparedStatement = connection.prepareStatement(PERSON_INSERT_SQL)) {
      preparedStatement.setObject(1, person.getId());
      preparedStatement.setObject(2, person.getFirstName());
      preparedStatement.setObject(3, person.getLastName());
      preparedStatement.execute();


    }
  }

  @SneakyThrows
  public void createNote(Note note) {
    try (PreparedStatement preparedStatement = connection.prepareStatement(NOTE_INSERT_SQL)) {
      preparedStatement.setObject(1, note.getId());
      preparedStatement.setObject(2, note.getTitle());
      preparedStatement.setObject(3, note.getBody());
      preparedStatement.setObject(4, note.getPerson().getId());
      preparedStatement.execute();
    }
  }

}


