package com.breskul.bibernate.persistence;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceContext {

    private final Map<EntityKey, Object> firstLevelCache = new ConcurrentHashMap<>();
    private final Map<EntityKey, Object[]> entitySnapshots = new ConcurrentHashMap<>(); // for dirty checking

}
