package com.breskul.bibernate.annotation;


/**
 * Enumeration representing the different fetch types for entity associations.
 */
public enum FetchType {
  /**
   * Specifies that the associated entity or collection should be eagerly
   * fetched when the owning entity is fetched.
   */
  EAGER,
  /**
   * Specifies that the associated entity or collection should be lazily
   * fetched when the owning entity is fetched.
   */
  LAZY
}
