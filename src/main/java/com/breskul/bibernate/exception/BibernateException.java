package com.breskul.bibernate.exception;

public class BibernateException extends RuntimeException {

  public BibernateException(String message) {
    super(message);
  }

  public BibernateException(String message, Exception ex) {
    super(message, ex);
  }

}
