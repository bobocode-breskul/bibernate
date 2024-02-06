package com.breskul.bibernate.util;

import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.exception.EntityConstructionException;
import com.breskul.bibernate.exception.ReflectAccessException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;


/**
 * Utility class for performing reflection operations.
 */
public class ReflectionUtil {

  private static final Logger log = LoggerFactory.getLogger(ReflectionUtil.class);

  private ReflectionUtil() {
  }

  /**
   * Creates a new instance of the specified class using reflection.
   *
   * @param cls The class to create an instance of
   * @param <T> The type parameter representing the class
   * @return The created instance of the class
   * @throws ReflectAccessException if there is an error during the instance creation
   */
  public static <T> T createEntityInstance(Class<T> cls) {
    try {
      log.trace("Creating reflective instance of class [{}]", cls);
      return cls.getConstructor().newInstance();
    } catch (InvocationTargetException e) {
      throw new ReflectAccessException(
          "Could not create instance of target entity [%s]".formatted(cls), e);
    } catch (InstantiationException e) {
      throw new ReflectAccessException("Entity [%s] should be non-abstract class".formatted(cls),
          e);
    } catch (IllegalAccessException | NoSuchMethodException e) {
      throw new ReflectAccessException(
          "Entity [%s] should have public no-args constructor".formatted(cls), e);
    }
  }

  /**
   * Writes the given value to the specified field of the provided object using reflection.
   *
   * @param field The field to write the value to
   * @param obj   The object whose field should be written
   * @param value The value to be written to the field
   * @throws ReflectAccessException if there is an error accessing the field or the field is final
   */
  public static void writeFieldValue(Field field, Object obj, Object value) {
    try {
      log.trace("Writing value to field {}.{}.{}", obj.getClass().getPackageName(),
          obj.getClass().getSimpleName(), field.getName());
      field.set(obj, value);
    } catch (IllegalAccessException e) {
      throw new ReflectAccessException(
          "Object field [%s] should be accessible and not final".formatted(field), e);
    } catch (IllegalArgumentException e) {
      throw new EntityConstructionException(
          "Mismatched types: Expected value of type %s but received value of type %s".formatted(
              field.getType().getSimpleName(), value.getClass().getSimpleName()), e);
    }
  }


  /**
   * Reads the value of a field on the given obj object.
   *
   * @param obj   - The obj object
   * @param field - The field to read the value from
   * @return The value of the field
   * @throws ReflectAccessException if failed to access the field due to illegal access
   */
  public static Object readFieldValue(Object obj, Field field) {
    try {
      log.trace("Reading value of field {}.{}.{}", obj.getClass().getPackageName(),
          obj.getClass().getSimpleName(), field.getType());
      field.setAccessible(true);
      return field.get(obj);
    } catch (IllegalAccessException e) {
      throw new ReflectAccessException(
          "Failed to access field '" + field.getName() + "' of obj type '" + obj.getClass()
              .getName() + "': Illegal access", e);
    }
  }
}
