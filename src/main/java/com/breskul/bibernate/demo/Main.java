package com.breskul.bibernate.demo;

import com.breskul.bibernate.demo.entity.Person;
import com.breskul.bibernate.persistence.Persistence;
import com.breskul.bibernate.persistence.Session;
import com.breskul.bibernate.persistence.SessionFactory;
import java.util.List;

public class Main {

  public static void main(String[] args) {
    SessionFactory sessionFactory = Persistence.createSessionFactory();
    try (Session session = sessionFactory.openSession()) {
//      TypedQuery<Person> typedQuery = new TypedQuery<>("from Person p where age > 30 and p.id < 4", Person.class, connection);
      List<Person> resultList = session.executeBiQLQuery(
          "select p from Person p where p.firstName in (5, 7, 10)", Person.class);
//      List<Person> sqlQuery = session.executeNativeQuery("select * from persons where id in (5, 7, 10)", Person.class);
      // Create
      Person person = new Person("Taras", "TEST", 20);
      session.persist(person);

      // Find
      Person foundPerson = session.findById(Person.class, person.getId());
      System.out.println(foundPerson);

      // Update
      foundPerson.setAge(40);
      System.out.println(foundPerson);

      // Delete
      session.delete(foundPerson);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
