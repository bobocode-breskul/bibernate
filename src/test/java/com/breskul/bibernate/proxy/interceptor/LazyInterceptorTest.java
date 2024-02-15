package com.breskul.bibernate.proxy.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class LazyInterceptorTest {

  @Test
  @SneakyThrows
  void whenInterceptWithArguments_thenInterceptedMethodInvokedCorrectly() {
    // Arrange
    Supplier<TestObject> supplier = () -> new TestObject(42);
    Method addValueMethod = TestObject.class.getMethod("addValue", int.class);

    // Act
    LazyInterceptor<TestObject> interceptor = new LazyInterceptor<>(supplier);
    Object result = interceptor.intercept(addValueMethod, new Object[]{10});

    // Assert
    assertThat(result).isEqualTo(52);
  }

  @Test
  @SneakyThrows
  void whenInterceptWithoutNullField_thenNullPointerExceptionIsThrown() {
    // Arrange
    Supplier<TestObject> supplier = () -> new TestObject(37);
    LazyInterceptor<TestObject> interceptor = new LazyInterceptor<>(supplier);

    // Act
    assertThatThrownBy(() -> interceptor.intercept(null, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void whenInterceptWithMethodThrowsException_thenExpectedExceptionIsThrown() {
    // Arrange
    Supplier<TestObject> supplier = () -> {
      throw new IllegalStateException("Supplier Exception");
    };
    LazyInterceptor<TestObject> interceptor = new LazyInterceptor<>(supplier);

    // Assert
    assertThatThrownBy(() -> interceptor.intercept(null, null))
        .isInstanceOf(IllegalStateException.class).hasMessage("Supplier Exception");
  }

  private static class TestObject {

    private int value;

    public TestObject(int value) {
      this.value = value;
    }

    public int addValue(int addend) {
      this.value += addend;
      return this.value;
    }
  }
}