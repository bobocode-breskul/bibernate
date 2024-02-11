package com.breskul.bibernate.action;

import com.breskul.bibernate.persistence.GenericDao;
import lombok.RequiredArgsConstructor;

/**
 * Represents Delete action that will be used in Action queue
 */
@RequiredArgsConstructor
public class DeleteAction implements Action {

  private final GenericDao dao;
  private final Object entity;

  /**
   * Executes delete action in action queue
   */
  @Override
  public void execute() {
    dao.delete(entity);
  }

  /**
   * Represents priority in action queue
   *
   * @return returns priority
   */
  @Override
  public int priority() {
    return 3;
  }
}
