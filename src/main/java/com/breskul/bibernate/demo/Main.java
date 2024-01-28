package com.breskul.bibernate.demo;

import com.breskul.bibernate.demo.entity.Person;
import com.breskul.bibernate.demo.entity.Photo;
import com.breskul.bibernate.demo.entity.PhotoComment;
import com.breskul.bibernate.persistence.Persistence;
import com.breskul.bibernate.persistence.Session;
import com.breskul.bibernate.persistence.SessionFactory;

public class Main {

    public static void main(String[] args) {
        SessionFactory sessionFactory = Persistence.createSessionFactory();
        try (Session session = sessionFactory.openSession()) {
//            Photo person = session.findById(Photo.class, 1L);
            PhotoComment comment = session.findById(PhotoComment.class, 1L);
            System.out.println(comment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
