package com.breskul.bibernate.proxy.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

/**
 * A class that provides lazy initialization and interception for a given object.
 *
 * @param <T> The type of the object to be lazily initialized and intercepted.
 */
public class LazyInterceptor<T> {

  private T object;
  private final Supplier<T> supplier;


  public LazyInterceptor(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  /**
   * Intercepts a method call on a lazily initialized object and initialize real object when lazy
   * object relationType accessed.
   *
   * @param method The method being intercepted.
   * @param args   The arguments passed to the method.
   * @return The result of invoking the method on the lazily initialized object.
   * @throws InvocationTargetException If the intercepted method throws an exception.
   * @throws IllegalAccessException    If the intercepted method cannot be accessed.
   */
  @RuntimeType
  public Object intercept(@Origin Method method, @AllArguments Object[] args)
      throws InvocationTargetException, IllegalAccessException {
    if (object == null) {
      object = supplier.get();
    }
    return method.invoke(object, args);
  }

}
