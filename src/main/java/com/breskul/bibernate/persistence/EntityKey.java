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
public class EntityKey {

  private final Class<?> entityClass;
  private final Object id;

  public EntityKey(Class<?> entityClass, Object id) {
    this.entityClass = entityClass;
    this.id = id;
  }

  public static <T> EntityKey valueOf(T entity) {
    Object id = getEntityId(entity);
    return new EntityKey(entity.getClass(), id);
  }
}
