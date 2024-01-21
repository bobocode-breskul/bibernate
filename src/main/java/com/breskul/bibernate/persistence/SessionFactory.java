package com.breskul.bibernate.persistence;

import javax.sql.DataSource;

public class SessionFactory {

    private final DataSource dataSource;

    public SessionFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Session openSession() {
        return new Session(dataSource);
    }
}
