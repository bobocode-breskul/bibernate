package com.breskul.bibernate.util;

import static com.breskul.bibernate.util.ReflectionUtil.createEntityInstance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.breskul.bibernate.exception.ReflectAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReflectionUtilTest {

  @Test
  @Order(1)
  @DisplayName("When valid input class with public no-args constructor then object creation success")
  void whenCreateEntityInstanceWithValidInput_ValidSuccess() {
    // data
    final Class<String> cls = String.class;
    // given
    // when
    String result = createEntityInstance(cls);
    // verify
    assertThat(result)
        .as("Result object is not 'null'")
        .isNotNull()
        .as("Result object has the expected class type")
        .isInstanceOf(String.class);
  }

  @Test
  @DisplayName("Throw ReflectAccessException when abstract class is passed as argument")
  @Order(2)
  void whenCreateEntityInstanceOnAbstractClass_ThenExceptionIsThrown() {
    assertThatThrownBy(() -> createEntityInstance(AbstractClass.class))
        .isInstanceOf(ReflectAccessException.class)
        .hasMessage("Entity [%s] should be non-abstract class".formatted(AbstractClass.class));
  }

  @Test
  @DisplayName("Throw ReflectAccessException when class with private no-args constructor is "
      + "passed as argument")
  @Order(3)
  void whenCreateEntityInstanceOnClassWithPrivateNoArgsConstructor_ThenExceptionIsThrown() {
    assertThatThrownBy(() -> createEntityInstance(ClassWithoutPublicConstructor.class))
        .isInstanceOf(ReflectAccessException.class)
        .hasMessage("Entity [%s] should have public no-args constructor".formatted(
            ClassWithoutPublicConstructor.class));
  }

  /**
   * This test validates the createEntityInstance method for a class without no-args constructor. It
   * expects an ReflectAccessException to be thrown.
   */
  @Test
  @DisplayName("Throw ReflectAccessException when class without no-args constructor is "
      + "passed as argument")
  @Order(4)
  void whenCreateEntityInstanceOnClassWithMultiArgsConstructor_ThenExceptionIsThrown() {
    assertThatThrownBy(() -> createEntityInstance(ClassWithoutConstructor.class))
        .isInstanceOf(ReflectAccessException.class)
        .hasMessage("Entity [%s] should have public no-args constructor".formatted(
            ClassWithoutConstructor.class));
  }

  @Test
  @DisplayName("Throw ReflectAccessException when no-args constructor throws exception")
  void whenCreateEntityInstanceOnClassWithConstructorWithException_ThenExceptionIsThrown() {
    assertThatThrownBy(() -> createEntityInstance(ClassWithExceptionInNoArgsConstructor.class))
        .isInstanceOf(ReflectAccessException.class)
        .hasMessage("Could not create instance of target entity [%s]".formatted(
            ClassWithExceptionInNoArgsConstructor.class));
  }
}

/**
 * Abstract Class for testing purpose
 */
abstract class AbstractClass {

  public AbstractClass() {
  }
}

/**
 * Class without public Constructor for testing purpose
 */
class ClassWithoutPublicConstructor {

  private ClassWithoutPublicConstructor() {
  }
}

/**
 * Class without Constructor for testing purpose
 */
class ClassWithoutConstructor {

  private ClassWithoutConstructor(int number) {
  }
}

class ClassWithExceptionInNoArgsConstructor {

  public ClassWithExceptionInNoArgsConstructor() {
    throw new NullPointerException("TEST");
  }
}