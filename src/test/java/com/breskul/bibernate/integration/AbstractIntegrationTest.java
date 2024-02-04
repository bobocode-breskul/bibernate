package com.breskul.bibernate.integration;

import com.breskul.bibernate.persistence.GenericDao;
import com.breskul.bibernate.persistence.PersistenceContext;
import com.breskul.bibernate.persistence.datasource.BibernateDataSource;
import com.breskul.bibernate.persistence.datasource.DataSourceProperties;
import com.breskul.bibernate.persistence.datasource.propertyreader.ApplicationPropertiesReader;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractIntegrationTest {
  protected static DataSource dataSource;
  protected GenericDao genericDao;

  @BeforeAll
  public static void beforeAll() {
    DataSourceProperties properties = ApplicationPropertiesReader.getInstance().readProperty();
    dataSource = new BibernateDataSource(properties);
  }

  @BeforeEach
  public void setup() {
    this.genericDao = new GenericDao(dataSource, new PersistenceContext());
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


}


