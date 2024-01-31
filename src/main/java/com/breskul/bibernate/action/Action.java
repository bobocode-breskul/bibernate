package com.breskul.bibernate.action;

public interface Action {

  void execute();

  int priority();
}
