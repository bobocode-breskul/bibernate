package com.breskul.bibernate.demo;

import com.breskul.bibernate.demo.entity.Person;
import com.breskul.bibernate.persistence.Persistence;
import com.breskul.bibernate.persistence.Session;
import com.breskul.bibernate.persistence.SessionFactory;

public class Main {

  public static void main(String[] args) {
    SessionFactory sessionFactory = Persistence.createSessionFactory();
    try (Session session = sessionFactory.openSession()) {
      // Test create
      Person person = new Person("Taras", "TEST", 20);
      session.persist(person);

      // Test find
      Person foundPerson = session.findById(Person.class, person.getId());
      System.out.println(foundPerson);

      // Test update
      foundPerson.setAge(40);
      System.out.println(foundPerson);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
