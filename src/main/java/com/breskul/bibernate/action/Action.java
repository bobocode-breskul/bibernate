package com.breskul.bibernate.action;

import com.breskul.bibernate.persistence.Session;

/**
 * Represents SQL query being queued in {@link Session}. Implementation of this interface are used
 * to perform insert, update and remove operations asynchronously.
 */
public interface Action {

  /**
   * Calls SQL query execution
   */
  void execute();

  /**
   * Used to prioritize actions and execute them in order after Session flush is called
   *
   * @return action priority
   */
  int priority();
}
