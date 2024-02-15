package com.breskul.bibernate.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.breskul.bibernate.data.DynamicPerson;
import com.breskul.bibernate.data.Note;
import com.breskul.bibernate.data.Person;
import com.breskul.bibernate.exception.BibernateException;
import com.breskul.bibernate.exception.EntityQueryException;
import com.breskul.bibernate.persistence.EntityKey;
import com.breskul.bibernate.util.EntityUtil;
import java.sql.SQLException;
import java.util.Arrays;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class GenericDaoIntegrationTest extends AbstractIntegrationTest {

  @SneakyThrows
  @Order(1)
  @DisplayName("When execute update for simple entity then one row updated")
  @Test
  void given_Entity_when_ExecuteUpdate_then_OneRowUpdated() {
    //given
    Person person = new Person(1L, "John", "Doe");
    createPerson(person);
    persistenceContext.put(person);
    EntityKey<Person> entityKey = EntityKey.valueOf(person);
    Object[] parameters = EntityUtil.getEntityColumnValues(person);
    ArgumentCaptor<String> updateSql = ArgumentCaptor.forClass(String.class);

    //when
    int result = genericDao.executeUpdate(entityKey, parameters);

    // then
    String expectedSql = "UPDATE persons SET id = ?, first_name = ?, last_name = ? WHERE id = ?;";
    verify(connection, times(2)).prepareStatement(updateSql.capture());
    assertEquals(expectedSql, updateSql.getValue());
    assertEquals(1, result);
  }


  @SneakyThrows
  @Order(2)
  @DisplayName("When execute update for entity with relations then one row updated")
  @Test
  void given_EntityWithRelations_when_ExecuteUpdate_then_OneRowUpdated() {
    //given
    Person person = new Person(1L, "John", "Doe");
    Note note = new Note(1L, "hello", "hello", person);
    createPerson(person);
    createNote(note);
    persistenceContext.put(person);
    persistenceContext.put(note);
    persistenceContext.takeToOneRelationSnapshot(note);
    EntityKey<Note> entityKey = EntityKey.valueOf(note);
    Object[] parameters = EntityUtil.getEntityColumnValues(note);
    ArgumentCaptor<String> updateSql = ArgumentCaptor.forClass(String.class);

    //when
    int result = genericDao.executeUpdate(entityKey, parameters);

    // then
    String expectedSql = "UPDATE notes SET id = ?, title = ?, body = ?, person_id = ? WHERE id = ?;";
    verify(connection, times(3)).prepareStatement(updateSql.capture());
    assertEquals(expectedSql, updateSql.getValue());
    assertEquals(1, result);
  }

  @SneakyThrows
  @Order(3)
  @DisplayName("When execute update for @DynamicUpdate annotated entity then one row updated and dynamic query generated")
  @Test
  void given_DynamicUpdateEntity_when_ExecuteUpdate_then_OneRowUpdated() {
    //given
    DynamicPerson person = new DynamicPerson(1L, "John", "Doe");
    createDynamicPerson(person);
    persistenceContext.put(person);
    person.setFirstName("Mike");
    EntityKey<DynamicPerson> entityKey = EntityKey.valueOf(person);
    ArgumentCaptor<String> updateSql = ArgumentCaptor.forClass(String.class);

    //when
    int result = genericDao.executeUpdate(entityKey, "Mike");

    // then
    String expectedSql = "UPDATE persons SET first_name = ? WHERE id = ?;";
    verify(connection, times(2)).prepareStatement(updateSql.capture());
    assertEquals(expectedSql, updateSql.getValue());
    assertEquals(1, result);
  }

  @SneakyThrows
  @Order(4)
  @DisplayName("When execute update and SQLException thrown then EntityQueryException thrown with parameters and sql query")
  @Test
  void given_Entity_when_ExecuteUpdateThrowsSqlException_then_EntityQueryExceptionThrown() {
    //given
    Person person = new Person(1L, "John", "Doe");
    persistenceContext.put(person);
    EntityKey<Person> entityKey = EntityKey.valueOf(person);
    Object[] parameters = EntityUtil.getEntityColumnValues(person);
    doThrow(SQLException.class).when(connection).prepareStatement(any());
    String expectedSql = "UPDATE persons SET id = ?, first_name = ?, last_name = ? WHERE id = ?;";

    //when
    //then
    assertThatThrownBy(() -> genericDao.executeUpdate(entityKey, parameters))
        .isInstanceOf(EntityQueryException.class)
        .hasMessage("Failed to execute update query: [%s] with parameters %s".formatted(expectedSql,
            Arrays.toString(parameters)));

  }

  @SneakyThrows
  @Order(5)
  @DisplayName("When execute update entity with null id then throw validation exception")
  @Test
  void given_EntityWithNullId_when_ExecuteUpdate_then_ThrowException() {
    //given
    Person person = new Person("John", "Doe");
    persistenceContext.put(person);
    EntityKey<Person> entityKey = EntityKey.valueOf(person);
    Object[] parameters = EntityUtil.getEntityColumnValues(person);

    //when
    //then
    assertThatThrownBy(() -> genericDao.executeUpdate(entityKey, parameters))
        .isInstanceOf(BibernateException.class)
        .hasMessage("Primary key value must be passed for update query");
  }
}
