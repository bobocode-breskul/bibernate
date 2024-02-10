package com.breskul.bibernate.persistence.datasource;

public record PersistenceProperties(String url, String username, String password, String driverClass, String dialectClass) {

}
