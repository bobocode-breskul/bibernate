package com.breskul.bibernate.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;

import com.breskul.bibernate.data.Person;
import com.breskul.bibernate.persistence.EntityKey;
import com.breskul.bibernate.util.EntityUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class GenericDaoIntegrationTest extends AbstractIntegrationTest {

  @SneakyThrows
  @Test
  void given_EntityWithoutRelations_when_ExecuteUpdate_then_OneRowUpdated() {
    //given
    Person person = new Person(1L, "John", "Doe");
    persistenceContext.put(person);
    EntityKey<Person> entityKey = EntityKey.valueOf(person);
    Object[] parameters = EntityUtil.getEntityColumnValues(person);
    ArgumentCaptor<String> updateSql = ArgumentCaptor.forClass(String.class);

    //when
    int result = genericDao.executeUpdate(entityKey, parameters);

    // then
    String expectedSql = "UPDATE persons SET id = ?, first_name = ?, last_name = ? WHERE id = ?;";
    verify(connection, atMostOnce()).prepareStatement(updateSql.capture());
    assertEquals(expectedSql, updateSql.getValue());
    assertEquals(1, result);
  }



}
