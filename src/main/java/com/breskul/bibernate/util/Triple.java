package com.breskul.bibernate.util;

public record Triple<F, S, T>(F first, S second, T third) {

  public static <F, S, T> Triple<F, S, T> of(F first, S second, T third) {
    return new Triple<>(first, second, third);
  }

}
