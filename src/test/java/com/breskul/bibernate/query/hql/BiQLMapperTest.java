package com.breskul.bibernate.query.hql;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.breskul.bibernate.data.Note;
import com.breskul.bibernate.data.Person;
import com.breskul.bibernate.exception.BiQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class BiQLMapperTest {

  @Test
  void givenNullBql_whenBqlToSql_thenShouldThrowBiQLException() {
    assertThatThrownBy(() -> BiQLMapper.bqlToSql(null, Person.class))
        .isInstanceOf(BiQLException.class)
        .hasMessage("BiQL should not be null or empty");
  }

  @Test
  void givenEmptyBql_whenBqlToSql_thenShouldThrowBiQLException() {
    assertThatThrownBy(() -> BiQLMapper.bqlToSql("", Person.class))
        .isInstanceOf(BiQLException.class)
        .hasMessage("BiQL should not be null or empty");
  }

  @Test
  void givenValidBqlButContainsDifferentEntityClass_whenBqlToSql_thenShouldThrowBiQLException() {
    assertThatThrownBy(() -> BiQLMapper.bqlToSql("from Person", Note.class))
        .isInstanceOf(BiQLException.class)
        .hasMessage(
            "BiQL does not contain entity with type %s".formatted(Note.class.getSimpleName()));
  }

  @Test
  void givenNullEntityClass_whenBqlToSql_thenShouldThrowBiQLException() {
    assertThatThrownBy(() -> BiQLMapper.bqlToSql("from Person", null))
        .isInstanceOf(BiQLException.class)
        .hasMessage("EntityClass should not be null");
  }

  @Test
  void givenAsteriksInBiQL_whenBqlToSql_thenShouldThrowBiQLException() {
    assertThatThrownBy(() -> BiQLMapper.bqlToSql("select * from Person", Person.class))
        .isInstanceOf(BiQLException.class)
        .hasMessage("BiQL has incorrect structure");
  }

  @ParameterizedTest
  @ValueSource(strings = {"select id, age from Person p", "select p.id, p.age from Person",
      "p from Person where p.age = 4"})
  void givenInvalidBQGL_whenBqlToSql_thenShouldThrowBiQLException(String bql) {
    assertThatThrownBy(() -> BiQLMapper.bqlToSql(bql, Person.class))
        .isInstanceOf(BiQLException.class)
        .hasMessage("BiQL has incorrect structure");
  }

  @ParameterizedTest
  @CsvSource({
      "'from Person', 'select * from persons'",
      "'from Person p', 'select * from persons p'",
      "'select p from Person p', 'select * from persons p'",
      "'select p.firstName, p.id from Person p', 'select p.first_name, p.id from persons p'",
      "'select firstName, age from Person', 'select first_name, age from persons'"
  })
  void givenValidBql_whenBqlToSql_thenShouldGenerateValidSql(String bql, String expected) {
    //when
    String actual = BiQLMapper.bqlToSql(bql, Person.class);
    //then
    assertThat(actual).isEqualTo(expected);
  }
}