package com.breskul.bibernate.proxy.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

// todo: docs
public class LazyInterceptor<T> {
  private T object;
  private final Supplier<T> supplier;


  public LazyInterceptor(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  @RuntimeType
  public Object intercept(@Origin Method method, @AllArguments Object[] args)
      throws InvocationTargetException, IllegalAccessException {
    if (object == null) {
      object = supplier.get();
    }
    return method.invoke(object, args);
  }

}
