package com.breskul.bibernate.util;

import com.breskul.bibernate.exception.ReflectAccessException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

// todo: docs
public class ReflectionUtil {
  private ReflectionUtil() {}
  // todo: docs
  // todo: logging
  public static <T> T createEntityInstance(Class<T> cls) {
    try {
      return cls.getConstructor().newInstance();
    } catch (InvocationTargetException e) {
      throw new ReflectAccessException(
          "Could not create instance of target entity [%s]".formatted(cls), e);
    } catch (InstantiationException e) {
      throw new ReflectAccessException("Entity [%s] should be non-abstract class".formatted(cls), e);
    } catch (IllegalAccessException e) {
      throw new ReflectAccessException(
          "Entity [%s] should have public no-args constructor".formatted(cls), e);
    } catch (NoSuchMethodException e) {
      throw new ReflectAccessException(
          "Entity [%s] should have constructor without parameters".formatted(cls), e);
    }
  }

  // todo: docs
  // todo: logging
  public static void writeFieldValue(Field field, Object obj, Object value) {
    try {
      field.set(obj, value);
    } catch (IllegalAccessException e) {
      throw new ReflectAccessException(
          "Object field [%s] should be accessible and not final".formatted(field), e);
    }
  }


  /**
   * Reads the value of a field on the given entity object.
   *
   * @param entity  - The entity object
   * @param idField - The field to read the value from
   * @return The value of the field
   * @throws ReflectAccessException if failed to access the field due to illegal access
   */
  public static Object readFieldValue(Object entity, Field idField) {
    try {
      idField.setAccessible(true);
      return idField.get(entity);
    } catch (IllegalAccessException e) {
      throw new ReflectAccessException(
          "Failed to access field '" + idField.getName() + "' of entity type '" + entity.getClass()
              .getName() + "': Illegal access", e);
    }
  }
}
