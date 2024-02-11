package com.breskul.bibernate.util;

import com.breskul.bibernate.config.LoggerFactory;
import com.breskul.bibernate.exception.AssociationException;
import com.breskul.bibernate.proxy.collection.LazyList;
import com.breskul.bibernate.proxy.collection.LazySet;
import com.breskul.bibernate.proxy.interceptor.LazyInterceptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.NamingStrategy.Suffixing.BaseNameResolver.ForFixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;


/**
 * Utility class for handling associations between entities.
 */
public class AssociationUtil {

  private static final Logger log = LoggerFactory.getLogger(AssociationUtil.class);

  private static final String PROXY_NAMING_PART = "BibernateProxy";

  private AssociationUtil() {
  }

  /**
   * Retrieves an instance of the Collection class based on the given Field representing a
   * collection.
   *
   * @param collectionField - The Field representing the collection
   * @return An instance of the Collection class
   * @throws IllegalArgumentException if the collection type is unsupported
   */
  public static Collection<Object> getCollectionInstance(Field collectionField) {
    return getCollectionInstance(collectionField, Collections.emptyList());
  }


  /**
   * Retrieves an instance of the Collection class based on the given Field representing a
   * collection.
   *
   * @param collectionField - The Field representing the collection
   * @param source          - The source collection used to initialize the new collection instance
   * @return An instance of the Collection class
   * @throws AssociationException if the collection type is unsupported
   */
  public static Collection<Object> getCollectionInstance(Field collectionField,
      Collection<?> source) {
    var collectionClass = collectionField.getType();

    if (collectionClass.isAssignableFrom(List.class)) {
      log.trace("Generating List collection for [{}.{}.{}] field",
          collectionField.getDeclaringClass().getPackageName(),
          collectionField.getDeclaringClass().getSimpleName(), collectionField.getName());
      return new ArrayList<>(source);
    }

    if (collectionClass.isAssignableFrom(Set.class)) {
      log.trace("Generating Set collection for [{}.{}.{}] field",
          collectionField.getDeclaringClass().getPackageName(),
          collectionField.getDeclaringClass().getSimpleName(), collectionField.getName());
      return new HashSet<>(source);
    }

    throw new AssociationException(
        "Entity association collection is not supported. Collection: [%s]".formatted(
            collectionClass));
  }


  /**
   * Retrieves a lazy instance of a Collection based on the given Field representing the
   * collection.
   *
   * @param collectionField  The Field representing the collection.
   * @param delegateSupplier The Supplier providing the delegate collection for lazy
   *                         initialization.
   * @return An instance of the Collection class.
   * @throws AssociationException if the collection type is unsupported.
   */
  public static Collection<Object> getLazyCollectionInstance(Field collectionField,
      Supplier<Collection<?>> delegateSupplier) {
    var collectionClass = collectionField.getType();

    if (collectionClass.isAssignableFrom(List.class)) {
      log.trace("Generating lazy List collection for [{}.{}.{}] field",
          collectionField.getDeclaringClass().getPackageName(),
          collectionField.getDeclaringClass().getSimpleName(), collectionField.getName());
      return new LazyList<>(delegateSupplier);
    }

    if (collectionClass.isAssignableFrom(Set.class)) {
      log.trace("Generating lazy Set collection for [{}.{}.{}] field",
          collectionField.getDeclaringClass().getPackageName(),
          collectionField.getDeclaringClass().getSimpleName(), collectionField.getName());
      return new LazySet<>(delegateSupplier);
    }

    throw new AssociationException(
        "Entity association collection is not supported. Collection: [%s]".formatted(
            collectionClass));
  }

  /**
   * Retrieves a lazy proxied instance of an object based on the given Field and Supplier.
   *
   * @param field            The Field representing the object.
   * @param delegateSupplier The Supplier providing the delegate object for lazy initialization.
   * @return a proxy instance of the object class.
   * @throws AssociationException If the object type is unsupported or if there are errors in the
   *                              proxy creation process.
   */
  public static Object getLazyObjectProxy(Field field, Supplier<?> delegateSupplier) {
    Class<?> objectType = field.getType();
    log.trace("Generating proxy object for [{}.{}.{}] field of type [{}]",
        field.getDeclaringClass().getPackageName(), field.getDeclaringClass().getSimpleName(),
        field.getName(), field.getType());
    try {
      return new ByteBuddy()
          .with(new NamingStrategy.SuffixingRandom(PROXY_NAMING_PART,
              new ForFixedValue(objectType.getName())))
          .subclass(objectType)
          .method(ElementMatchers.any())
          .intercept(
              MethodDelegation.to(new LazyInterceptor<>(delegateSupplier)))
          .make()
          .load(objectType.getClassLoader())
          .getLoaded()
          .getConstructor()
          .newInstance();
    } catch (InvocationTargetException | InstantiationException e) {
      throw new AssociationException(
          "Could not create proxy instance of target entity [%s]".formatted(objectType), e);
    } catch (IllegalAccessException | NoSuchMethodException e) {
      throw new AssociationException(
          "Proxied entity [%s] should have public no-args constructor".formatted(objectType), e);
    }
  }
}
