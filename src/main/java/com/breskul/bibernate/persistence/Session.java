package com.breskul.bibernate.persistence;

import jakarta.persistence.EntityTransaction;

import javax.sql.DataSource;
import java.util.PriorityQueue;
import java.util.Queue;

public class Session implements AutoCloseable {

    private final GenericDao genericDao;
    private final PersistenceContext persistenceContext;
    private final Queue<Action> actionQueue = new PriorityQueue<>();
    private EntityTransaction transaction;

    public Session(DataSource dataSource) {
        this.genericDao = new GenericDao(dataSource);
        this.persistenceContext = new PersistenceContext();

    }

    public <T> T findById(Class<T> entityClass, Object id) {
        return genericDao.findById(entityClass, id);
    }

    @Override
    public void close() throws Exception {

    }
}
