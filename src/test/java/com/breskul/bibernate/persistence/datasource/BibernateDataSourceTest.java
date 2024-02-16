package com.breskul.bibernate.persistence.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.breskul.bibernate.persistence.datasource.connectionpools.C3P0;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class BibernateDataSourceTest {

  @Test
  void given_persistenceProperties_when_propertiesValid_then_successfullyCreateDataSource() {
    PersistenceProperties properties = new PersistenceProperties("jdbc:h2:file:~/tmp/test", "sa", "", null, null, null, true);

    new BibernateDataSource().createDataSource(properties);

    assertThatNoException();
  }

  @Test
  void given_persistenceProperties_when_urlNotValid_then_successfullyCreateDataSource() {
    PersistenceProperties properties = new PersistenceProperties("jdbc:myOwn:file:~/tmp/test", "sa", "", null, null, null, true);

    assertThatThrownBy(() -> new BibernateDataSource().createDataSource(properties))
        .hasMessage("Failed to get driver instance for jdbcUrl=jdbc:myOwn:file:~/tmp/test");
  }

  @Test
  void given_persistenceProperties_when_withValidCustomDriverClass_then_successfullyCreateDataSource() {
    PersistenceProperties properties = new PersistenceProperties("jdbc:h2:file:~/tmp/test", "sa", "", "org.h2.Driver", null, null, true);

    new BibernateDataSource().createDataSource(properties);

    assertThatNoException();
  }

  @Test
  void given_persistenceProperties_when_withInvalidCustomDriverClass_then_successfullyCreateDataSource() {
    PersistenceProperties properties = new PersistenceProperties("jdbc:h2:file:~/tmp/test", "sa", "", "org.h3.Driver", null, null, true);

    assertThatThrownBy(() -> new BibernateDataSource().createDataSource(properties))
        .hasMessage("Error loading the Driver class: org.h3.Driver");
  }

  @SneakyThrows
  @Test
  void given_persistenceProperties_when_propertiesValid_then_allParentMethodsWorkAsExpected() {
    PersistenceProperties properties = new PersistenceProperties("jdbc:h2:file:~/tmp/test", "sa", "", null, null, null, true);

    AbstractDataSource dataSource = new BibernateDataSource().createDataSource(properties);

    assertThat(dataSource.getLoginTimeout()).isEqualTo(0);
    assertThat(dataSource.isWrapperFor(BibernateDataSource.class)).isEqualTo(true);
    assertThat(dataSource.isWrapperFor(C3P0.class)).isEqualTo(false);
    assertThat(dataSource.unwrap(BibernateDataSource.class).getClass()).isEqualTo(BibernateDataSource.class);

    assertThatThrownBy(() -> dataSource.setLoginTimeout(0))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("setLoginTimeout");
    assertThatThrownBy(dataSource::getLogWriter)
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("getLogWriter");
    assertThatThrownBy(() -> dataSource.setLogWriter(null))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("setLogWriter");
    assertThatThrownBy(dataSource::getParentLogger)
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("getParentLogger");
  }
}