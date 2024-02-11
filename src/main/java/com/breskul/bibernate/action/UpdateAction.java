package com.breskul.bibernate.action;

import com.breskul.bibernate.persistence.EntityKey;
import com.breskul.bibernate.persistence.GenericDao;
import lombok.RequiredArgsConstructor;

/**
 * Represents Update action that will be used in Action queue
 */
@RequiredArgsConstructor
public class UpdateAction<T> implements Action {

  private final GenericDao dao;
  private final EntityKey<T> entityKey;
  private final Object[] parameters;

  /**
   * Executes update action in action queue
   */
  @Override
  public void execute() {
    dao.executeUpdate(entityKey, parameters);
  }

  /**
   * Represents priority in action queue
   *
   * @return returns priority
   */
  @Override
  public int priority() {
    return 2;
  }
}
