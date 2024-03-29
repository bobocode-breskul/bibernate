package com.breskul.bibernate.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.breskul.bibernate.data.AccountPerson;
import com.breskul.bibernate.data.CustomPerson;
import com.breskul.bibernate.data.DynamicPerson;
import com.breskul.bibernate.data.Note;
import com.breskul.bibernate.data.Person;
import com.breskul.bibernate.exception.BiQLException;
import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.persistence.Persistence;
import com.breskul.bibernate.persistence.Session;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SessionIntegrationTest extends AbstractIntegrationTest {

  private static final AtomicLong ids = new AtomicLong();

  private Session session;
  private Person person;

  @BeforeEach
  void before() throws SQLException {
    this.session = Persistence.createSessionFactory().openSession();
    person = prepareRandomPerson();
  }

  @Test
  @DisplayName("Find person by bql when it exist in DB")
  void givenPersonInDb_whenFindByBqlFromDb_thenReturnExpectedPerson() {
    List<Person> result =
        session.executeBiQLQuery("from Person where id = %s".formatted(person.getId()),
            Person.class);

    assertThat(result).hasSize(1);

    assertThat(result.get(0).getId()).isEqualTo(person.getId());
    assertThat(result.get(0).getFirstName()).isEqualTo(person.getFirstName());
    assertThat(result.get(0).getLastName()).isEqualTo(person.getLastName());
  }

  @Test
  @DisplayName("Find persons firstname and lastname by bql when it exist in DB")
  void givenPersonInDb_whenFindFirstNameLastNameByBqlFromDb_thenReturnExpectedPerson() {
    List<Person> result =
        session.executeBiQLQuery(
            "select p.firstName, p.lastName from Person p where p.id = %s".formatted(
                person.getId()), Person.class);

    assertThat(result).hasSize(1);

    assertThat(result.get(0).getFirstName()).isEqualTo(person.getFirstName());
    assertThat(result.get(0).getLastName()).isEqualTo(person.getLastName());
  }

  @Test
  @DisplayName("Find person by sql when it exist in DB")
  void givenPersonInDb_whenFindBySqlFromDb_thenReturnExpectedPerson() {
    List<Person> result = session.executeNativeQuery(
        "select * from persons where id = %s".formatted(person.getId()), Person.class);

    assertThat(result).hasSize(1);

    assertThat(result.get(0).getId()).isEqualTo(person.getId());
    assertThat(result.get(0).getFirstName()).isEqualTo(person.getFirstName());
    assertThat(result.get(0).getLastName()).isEqualTo(person.getLastName());
  }

  @Test
  @DisplayName("Find persons firstname and lastname by sql when it exist in DB")
  void givenPersonInDb_whenFindFirstNameLastNameBySqlFromDb_thenReturnExpectedPerson() {
    List<Person> result =
        session.executeNativeQuery(
            "select p.first_name, p.last_name from persons p where p.id = %s".formatted(
                person.getId()), Person.class);

    assertThat(result).hasSize(1);

    assertThat(result.get(0).getId()).isNull();
    assertThat(result.get(0).getFirstName()).isEqualTo(person.getFirstName());
    assertThat(result.get(0).getLastName()).isEqualTo(person.getLastName());
  }

  @Test
  @DisplayName("Given invalid query when execute native query then should throw BiQLException")
  void givenPersonInDb_whenInvalidSqlFromDb_thenThrowBiQLException() {
    String invalidQuery = "select p.first_name, p.last_name from persons p where order by";

    assertThatThrownBy(() -> session.executeNativeQuery(invalidQuery, Person.class))
        .isInstanceOf(BiQLException.class)
        .hasMessage("Could not execute native query [%s] for entity [%s]".formatted(invalidQuery,
            Person.class));
  }

  @Test
  void givenPersonInDb_whenSessionClose_thenShouldNotFlushUnflushedChanges() throws SQLException {
    Person createdPerson = prepareRandomPerson();

    Person retrievedPerson = session.findById(Person.class, createdPerson.getId());

    assertThat(retrievedPerson.getId()).isEqualTo(createdPerson.getId());

    session.delete(createdPerson);
    session.close();

    session = Persistence.createSessionFactory().openSession();

    Person unFlushedPerson = session.findById(Person.class, createdPerson.getId());
    assertThat(unFlushedPerson.getId()).isEqualTo(createdPerson.getId());
    assertThat(unFlushedPerson.getFirstName()).isEqualTo(createdPerson.getFirstName());
    assertThat(unFlushedPerson.getLastName()).isEqualTo(createdPerson.getLastName());
  }

  @Test
  void givenPersonInDb_WhenFindPersonAndUpdateNameAndFlush_thenDirtyCheckingFlushesChanges()
      throws SQLException {
    //given
    Person createdPerson = prepareRandomPerson();
    Person person = session.findById(Person.class, createdPerson.getId());

    //when
    person.setFirstName("Mike");
    session.flush();

    //then
    session = Persistence.createSessionFactory().openSession();
    Person updatedPerson = session.findById(Person.class, createdPerson.getId());
    assertThat(updatedPerson.getFirstName()).isEqualTo("Mike");
  }

  @Test
  void givenPersonWithNoteInDb_WhenFindNoteAndUpdatePersonAndFlush_thenDirtyCheckingFlushesChangesForRelation()
      throws SQLException {
    //given
    Person createdPerson = prepareRandomPerson();
    Person newPerson = prepareRandomPerson();
    Note createdNote = prepareRandomNote(person);
    Note note = session.findById(Note.class, createdNote.getId());

    //when
    note.setPerson(newPerson);
    session.flush();

    //then
    session = Persistence.createSessionFactory().openSession();
    Note updatedNote = session.findById(Note.class, createdNote.getId());
    assertThat(updatedNote.getPerson().getId()).isNotEqualTo(createdPerson.getId());
  }

  @Test
  void givenPersonInDb_WhenFindPersonAndUpdateId_thenThrowException()
      throws SQLException {
    //given
    Person createdPerson = prepareRandomPerson();
    Person person = session.findById(Person.class, createdPerson.getId());

    //when
    person.setId(1L);

    //then
    assertThatThrownBy(() -> session.flush())
        .isInstanceOf(BibernateException.class)
        .hasMessage("identifier of an instance of %s was altered from %s to %s".formatted(Person.class.getName(), createdPerson.getId(), 1));
  }

  @SneakyThrows
  @Test
  void given_DynamicUpdateEntityInDb_WhenFindPersonAndUpdateNameAndFlush_thenDirtyCheckingFlushesChangesWithDynamicQuery() {
    // given
    DynamicPerson dynamicPerson = new DynamicPerson(ids.incrementAndGet(), "John", "Doe");
    createDynamicPerson(dynamicPerson);
    DynamicPerson person = session.findById(DynamicPerson.class, dynamicPerson.getId());

    // when
    person.setFirstName("Mike");
    session.flush();

    // then
    session = Persistence.createSessionFactory().openSession();
    DynamicPerson updatedPerson = session.findById(DynamicPerson.class, dynamicPerson.getId());
    assertThat(updatedPerson.getFirstName()).isEqualTo("Mike");
  }

  @Test
  void givenPersonWithNoteInDb1_whenExecuteNativeQuery_thenReturnPersonWithNote() {
    prepareRandomNote(person);

    List<CustomPerson> result =
        session.executeNativeQuery(
            "select * from persons left join notes ON notes.person_id=persons.id order by id asc;",
            CustomPerson.class);

    assertThat(result).hasSizeGreaterThan(0);

    assertThat(result.get(0).getFirstName()).isEqualTo(person.getFirstName());
    assertThat(result.get(0).getLastName()).isEqualTo(person.getLastName());
    assertThat(result.get(0).getNotes()).hasSizeGreaterThan(0);
  }

  @Test
  void givenPersonWithAccount_whenExecuteNativeQuery_thenReturnPersonWithNote() {
    prepareRandomNote(person);

    List<AccountPerson> result =
        session.executeNativeQuery(
            "select * from persons left join account ON account.person_id=persons.id order by id asc;",
            AccountPerson.class);

    assertThat(result).hasSizeGreaterThan(0);

    assertThat(result.get(0).getFirstName()).isEqualTo(person.getFirstName());
    assertThat(result.get(0).getLastName()).isEqualTo(person.getLastName());
    assertThat(result.get(0).getAccount()).isNotNull();
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

  private Note prepareRandomNote(Person person) {
    long id = ids.incrementAndGet();
    Note note = new Note(id, "hello", "hello", person);
    createNote(note);
    return note;
  }
}
