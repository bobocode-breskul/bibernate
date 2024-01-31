package com.breskul.bibernate.annotation;


/**
 * Enumeration representing the different fetch types for entity associations.
 *
 * The FetchType enumeration provides two fetch types:
 * - EAGER: This fetch type specifies that the associated entity or collection should be eagerly
 * fetched when the owning entity is fetched.
 * - LAZY: This fetch type specifies that the associated entity or collection should be lazily
 * fetched when the owning entity is fetched.
 */
public enum FetchType {
  EAGER,
  LAZY
}
