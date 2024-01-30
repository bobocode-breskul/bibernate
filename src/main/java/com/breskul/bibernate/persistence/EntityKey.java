package com.breskul.bibernate.persistence;

import static com.breskul.bibernate.util.EntityUtil.getEntityId;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// todo: docs
@EqualsAndHashCode(of = {"entityClass", "id"})
@Getter
@Setter
@ToString
public class EntityKey<T> {

  private final Class<T> entityClass;
  private final Object id;

  private EntityKey(Class<T> entityClass, Object id) {
    this.entityClass = entityClass;
    this.id = id;
  }

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
