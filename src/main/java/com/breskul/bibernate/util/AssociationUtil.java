package com.breskul.bibernate.util;

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

// todo: docs
public class AssociationUtil {

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

  // todo: docs
  // todo: logging
  public static Collection<Object> getCollectionInstance(Field collectionField,
      Collection<?> source) {
    var collectionClass = collectionField.getType();

    if (collectionClass.isAssignableFrom(List.class)) {
      return new ArrayList<>(source);
    }

    if (collectionClass.isAssignableFrom(Set.class)) {
      return new HashSet<>(source);
    }

    throw new AssociationException(
        "Entity association collection is not supported. Collection: [%s]".formatted(
            collectionClass));
  }

  // todo: docs
  // todo: logging
  public static Collection<Object> getLazyCollectionInstance(Field collectionField,
      Supplier<Collection<?>> delegateSupplier) {
    var collectionClass = collectionField.getType();

    if (collectionClass.isAssignableFrom(List.class)) {
      return new LazyList<>(delegateSupplier);
    }

    if (collectionClass.isAssignableFrom(Set.class)) {
      return new LazySet<>(delegateSupplier);
    }

    throw new AssociationException(
        "Entity association collection is not supported. Collection: [%s]".formatted(
            collectionClass));
  }

  // todo: docs
  // todo: logging
  public static Object getLazyObjectProxy(Field field, Supplier<?> delegateSupplier) {
    Class<?> objectType = field.getType();
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
    } catch (InvocationTargetException e) {
      throw new AssociationException(
          "Could not create proxy instance of target entity [%s]".formatted(objectType), e);
    } catch (InstantiationException e) {
      throw new AssociationException(
          "Proxied entity [%s] should be non-abstract class".formatted(objectType), e);
    } catch (IllegalAccessException e) {
      throw new AssociationException(
          "Proxied entity [%s] should have public no-args constructor".formatted(objectType), e);
    } catch (NoSuchMethodException e) {
      throw new AssociationException(
          "Proxied entity [%s] should have constructor without parameters".formatted(objectType), e);
    }
  }
}
