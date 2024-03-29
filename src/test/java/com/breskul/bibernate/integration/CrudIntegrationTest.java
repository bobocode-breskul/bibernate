package com.breskul.bibernate.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.breskul.bibernate.data.Person;
import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.persistence.LockType;
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
    Person expected = createRandomPersonInDb();

    Person result = session.findById(Person.class, expected.getId());

    assertThat(result.getId()).isEqualTo(expected.getId());
    assertThat(result.getFirstName()).isEqualTo(expected.getFirstName());
    assertThat(result.getLastName()).isEqualTo(expected.getLastName());
  }

  @Test
  @DisplayName("Find person by primary key when it exist in DB and used read lock")
  void givenPersonInDb_whenFindByIdFromDb_thenReturnExpectedPersonWithReadLock() {
    Person expected = createRandomPersonInDb();

    Person result = session.findById(Person.class, expected.getId(), LockType.PESSIMISTIC_READ);

    assertThat(result.getId()).isEqualTo(expected.getId());
    assertThat(result.getFirstName()).isEqualTo(expected.getFirstName());
    assertThat(result.getLastName()).isEqualTo(expected.getLastName());
  }

  @Test
  @DisplayName("Find person by primary key when it exist in DB and used write lock")
  void givenPersonInDb_whenFindByIdFromDb_thenReturnExpectedPersonWithWriteLock() {
    Person expected = createRandomPersonInDb();

    Person result = session.findById(Person.class, expected.getId(), LockType.PESSIMISTIC_WRITE);

    assertThat(result.getId()).isEqualTo(expected.getId());
    assertThat(result.getFirstName()).isEqualTo(expected.getFirstName());
    assertThat(result.getLastName()).isEqualTo(expected.getLastName());
  }

  @Test
  @DisplayName("Find person by primary key but send wrong type of id to the method")
  void givenPersonInDb_whenFindByIdFromDbWithWrongIdType_thenReturnExpectedPerson() {
    Person expected = createRandomPersonInDb();

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

  @Test
  void givenPerson_whenPersist_thenShouldPersistNewPerson() {
    Person expectedPerson = prepareRandomPerson();

    session.persist(expectedPerson);

    Person resultPerson = session.findById(Person.class, expectedPerson.getId());

    assertThat(resultPerson.getId()).isEqualTo(expectedPerson.getId());
    assertThat(resultPerson.getFirstName()).isEqualTo(expectedPerson.getFirstName());
    assertThat(resultPerson.getLastName()).isEqualTo(expectedPerson.getLastName());
  }

  @Test
  void givenExistedPerson_whenDelete_thenShouldDeletePerson() {
    Person createdPerson = createRandomPersonInDb();

    Person retrievedPerson = session.findById(Person.class, createdPerson.getId());

    assertThat(retrievedPerson.getId()).isEqualTo(createdPerson.getId());

    session.delete(createdPerson);
    session.flush();

    Person deletedPerson = session.findById(Person.class, createdPerson.getId());

    assertNull(deletedPerson);
  }

  private Person createRandomPersonInDb() {
    Person person = prepareRandomPerson();
    createPerson(person);
    return person;
  }

  private Person prepareRandomPerson() {
    long id = ids.incrementAndGet();

    Person person = new Person();
    person.setId(id);
    person.setFirstName("Mykola" + id);
    person.setLastName("Filimonov" + id);
    return person;
  }

}
