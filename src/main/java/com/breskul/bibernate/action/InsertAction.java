package com.breskul.bibernate.action;

import com.breskul.bibernate.persistence.GenericDao;
import lombok.RequiredArgsConstructor;

/**
 * Represents Insert action that will be used in Action queue
 */
@RequiredArgsConstructor
public class InsertAction implements Action {

  private final GenericDao dao;
  private final Object entity;

  /**
   * Executes insert action in action queue
   */
  @Override
  public void execute() {
    dao.save(entity);
  }

  /**
   * Represents priority in action queue
   *
   * @return returns priority
   */
  @Override
  public int priority() {
    return 1;
  }
}
