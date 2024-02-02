package com.breskul.bibernate.demo;

import com.breskul.bibernate.demo.entity.Person;
import com.breskul.bibernate.persistence.Persistence;
import com.breskul.bibernate.persistence.Session;
import com.breskul.bibernate.persistence.SessionFactory;

public class Main {

  public static void main(String[] args) {
    SessionFactory sessionFactory = Persistence.createSessionFactory();
    try (Session session = sessionFactory.openSession()) {
      Person person = session.findById(Person.class, 3022L);
      System.out.println(person);
      person.setAge(40);
      System.out.println(person);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
