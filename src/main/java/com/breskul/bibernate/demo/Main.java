package com.breskul.bibernate.demo;

import com.breskul.bibernate.demo.entity.Photo;
import com.breskul.bibernate.demo.entity.PhotoComment;
import com.breskul.bibernate.persistence.Persistence;
import com.breskul.bibernate.persistence.Session;
import com.breskul.bibernate.persistence.SessionFactory;

public class Main {

  public static void main(String[] args) {
    SessionFactory sessionFactory = Persistence.createSessionFactory();
    try (Session session = sessionFactory.openSession();) {
      Photo photo = session.findById(Photo.class, 1L);
      System.out.println(photo);
      System.out.println(photo.getComments());
      photo.setDescription("Updated6");
      PhotoComment comment = photo.getComments().get(0);
      comment.setText("New text3");
      photo.removeComment(comment);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
