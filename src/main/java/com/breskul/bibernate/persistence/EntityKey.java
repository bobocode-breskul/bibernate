package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.util.EntityUtil.getEntityId;

public record EntityKey<T>(Class<T> entityClass, Object id) {

  public static <T> EntityKey<T> of(Class<T> entityClass, Object id) {
    return new EntityKey<>(entityClass, id);
  }

  @SuppressWarnings("unchecked")
  public static <T> EntityKey<T> valueOf(T entity) {
    Object id = getEntityId(entity);
    Class<T> entityClass = (Class<T>) entity.getClass();
    return new EntityKey<>(entityClass, id);
  }
}
