package com.breskul.bibernate.action;

import com.breskul.bibernate.persistence.GenericDao;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAction implements Action {

  private final GenericDao dao;
  private final Object entity;

  @Override
  public void execute() {
    dao.delete(entity);
  }

  @Override
  public int priority() {
    return 3;
  }
}
