package com.breskul.bibernate.demo;

import com.breskul.bibernate.demo.entity.Person;
import com.breskul.bibernate.persistence.LockType;
import com.breskul.bibernate.persistence.Persistence;
import com.breskul.bibernate.persistence.Session;
import com.breskul.bibernate.persistence.SessionFactory;

public class Main {

  public static void main(String[] args) throws InterruptedException {
    SessionFactory sessionFactory = Persistence.createSessionFactory();
    Person person;
    try (Session session = sessionFactory.openSession()) {
      // Create
      person = new Person("Taras", "TEST", 20);
      session.persist(person);

      // Find
      Person foundPerson = session.findById(Person.class, person.getId());
      System.out.println(foundPerson);

      // Update
      foundPerson.setAge(40);
      System.out.println(foundPerson);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    Thread thread1 = new Thread(() -> lockMode(sessionFactory, person.getId()));
    Thread thread2 = new Thread(() -> lockMode2(sessionFactory, person.getId()));
    thread1.start();
    thread2.start();
    thread1.join();
    try (Session session = sessionFactory.openSession()) {
      // Find
      Person foundPerson = session.findById(Person.class, person.getId());
      System.out.println(foundPerson);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void lockMode(SessionFactory sessionFactory, Long id) {
    try (Session session = sessionFactory.openSession()) {
      session.getTransaction().begin();

      // Find
      Person foundPerson = session.findById(Person.class, id, LockType.PESSIMISTIC_WRITE);
      System.out.println(foundPerson);

      Thread.sleep(20000);
      session.getTransaction().commit();
    System.out.println("lockMode ended");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  private static void lockMode2(SessionFactory sessionFactory, Long id) {
    try (Session session = sessionFactory.openSession()) {
      session.getTransaction().begin();

      Thread.sleep(5000);
      // Find
      Person foundPerson = session.findById(Person.class, id);
      System.out.println(foundPerson + "2");
      foundPerson.setAge(20);

      session.getTransaction().commit();
      Session session1 = sessionFactory.openSession();
      foundPerson = session1.findById(Person.class, id);
      System.out.println(foundPerson + "2");
      session1.close();
    System.out.println("lockMode2 ended");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
