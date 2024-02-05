package com.breskul.bibernate.integration;

import com.breskul.bibernate.data.Person;
import com.breskul.bibernate.persistence.Session;
import org.junit.jupiter.api.Test;

public class SimpleIntegrationTest extends AbstractIntegrationTest {

  @Test
  public void test() {
    try (Session session = new Session(dataSource)) {
      Person person = session.findById(Person.class, 1L);;
      System.out.println(person);
    }

  }

  @Test
  public void test2() {
    try (Session session = new Session(dataSource)) {
      Person person = session.findById(Person.class, 1L);;
      System.out.println(person);
    }

  }

}
