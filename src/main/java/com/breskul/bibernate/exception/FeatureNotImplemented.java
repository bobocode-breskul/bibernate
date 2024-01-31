package com.breskul.bibernate.exception;

public class FeatureNotImplemented extends RuntimeException {

  public FeatureNotImplemented(String featureName) {
    super(featureName);
  }

}
