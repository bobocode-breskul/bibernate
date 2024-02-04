package com.breskul.bibernate.persistence.datasource;

public record DataSourceProperties(String url, String username, String password, String driverClass) {

}
