package com.breskul.bibernate.demo;

import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.persistence.Persistence;
import com.breskul.bibernate.persistence.Session;
import com.breskul.bibernate.persistence.SessionFactory;
import org.atteo.classindex.ClassIndex;

public class Main {

  public static void main(String[] args) {
    SessionFactory sessionFactory = Persistence.createSessionFactory();
    try (Session session = sessionFactory.openSession()) {
//      Person person = session.findById(Person.class, 3022L);
//      System.out.println(person);
//      person.setAge(40);
//      System.out.println(person);

//      ScanUtils scanUtils = new ScanUtilsImpl();
//      var e = scanUtils.searchClassesByFilter("", cls -> true);


      Iterable<Class<?>> klasses = ClassIndex.getAnnotated(Entity.class);

      klasses.forEach(System.out::println);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
