package com.breskul.bibernate.action;

import com.breskul.bibernate.persistence.EntityKey;
import com.breskul.bibernate.persistence.GenericDao;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateAction<T> implements Action {

  private final GenericDao dao;
  private final EntityKey<T> entityKey;
  private final Object[] parameters;

  @Override
  public void execute() {
    dao.executeUpdate(entityKey, parameters);
  }

  @Override
  public int priority() {
    return 2;
  }
}
