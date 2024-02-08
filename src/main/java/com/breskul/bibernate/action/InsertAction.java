package com.breskul.bibernate.action;

import com.breskul.bibernate.persistence.GenericDao;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InsertAction implements Action {

  private final GenericDao dao;
  private final Object entity;

  @Override
  public void execute() {
    dao.save(entity);
  }

  @Override
  public int priority() {
    return 1;
  }
}
