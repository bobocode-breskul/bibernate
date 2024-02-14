package com.breskul.bibernate.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.breskul.bibernate.data.Person;
import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.persistence.Persistence;
import com.breskul.bibernate.persistence.Session;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CrudIntegrationTest extends AbstractIntegrationTest {

  private static final AtomicLong ids = new AtomicLong();
  private Session session;

  @BeforeEach
  void before() throws SQLException {
    this.session = Persistence.createSessionFactory().openSession();
  }

  @Test
  @DisplayName("Find person by primary key when it exist in DB")
  void givenPersonInDb_whenFindByIdFromDb_thenReturnExpectedPerson() {
    Person expected = prepareRandomPerson();

    Person result = session.findById(Person.class, expected.getId());

    assertThat(result.getId()).isEqualTo(expected.getId());
    assertThat(result.getFirstName()).isEqualTo(expected.getFirstName());
    assertThat(result.getLastName()).isEqualTo(expected.getLastName());
  }

  @Test
  @DisplayName("Select person by primary key twice when it exist in DB and return same instance of person from context")
  void givenPerson_whenItRequestedSecondTimeFromDB_thenReturnSamePerson() {
    Person expected = prepareRandomPerson();

    Person firstRequestResult = session.findById(Person.class, expected.getId());
    Person secondRequestResult = session.findById(Person.class, expected.getId());

    assertThat(firstRequestResult).isSameAs(secondRequestResult);
  }

  @Test
  @DisplayName("Find person by primary key but send wrong type of id to the method")
  void givenPersonInDb_whenFindByIdFromDbWithWrongIdType_thenReturnExpectedPerson() {
    Person expected = prepareRandomPerson();

    assertThatThrownBy(() -> session.findById(Person.class, expected.getId().toString()))
        .isInstanceOf(BibernateException.class)
        .hasMessage("Mismatched types: Expected ID of type %s but received ID of type %s".formatted(Long.class.getSimpleName(),
            String.class.getSimpleName()));
  }

  @Test
  @DisplayName("Find person by primary key when it does not exist in db")
  void givenNotExistedId_whenRequestByThisID_thenReturnNull() {
    Person result = session.findById(Person.class, Long.MAX_VALUE);

    assertThat(result).isNull();
  }

  private Person prepareRandomPerson() {
    long id = ids.incrementAndGet();
    Person person = new Person();
    person.setId(id);
    person.setFirstName("Mykola" + id);
    person.setLastName("Filimonov" + id);
    createPerson(person);
    return person;
  }

}
