package com.breskul.bibernate.util;

import static com.breskul.bibernate.util.ReflectionUtil.createEntityInstance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.breskul.bibernate.exception.ReflectAccessException;
import java.lang.reflect.Field;
import lombok.Setter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReflectionUtilTest {

  private static final String TEST_CLASS_STRING_FIELD = "stringFieldName";
  private static final String TEST_CLASS_INTEGER_FIELD = "integerFieldName";
  private static final String TEST_STRING_VALUE = "test value";

  @Test
  @Order(1)
  @DisplayName("When valid input class with public no-args constructor then object creation success")
  void whenCreateEntityInstanceWithValidInput_thenSuccess() {
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
  @DisplayName("Throw ReflectAccessException when no-args constructor throws exception")
  @Order(2)
  void whenCreateEntityInstanceOnClassWithConstructorWithException_thenExceptionIsThrown() {
    assertThatThrownBy(() -> createEntityInstance(ClassWithExceptionInNoArgsConstructor.class))
        .isInstanceOf(ReflectAccessException.class)
        .hasMessage("Could not create instance of target entity [%s]".formatted(
            ClassWithExceptionInNoArgsConstructor.class));
  }

  @Test
  @DisplayName("Throw ReflectAccessException when abstract class is passed as argument")
  @Order(3)
  void whenCreateEntityInstanceOnAbstractClass_thenExceptionIsThrown() {
    assertThatThrownBy(() -> createEntityInstance(AbstractClass.class))
        .isInstanceOf(ReflectAccessException.class)
        .hasMessage("Entity [%s] should be non-abstract class".formatted(AbstractClass.class));
  }

  @Test
  @DisplayName("Throw ReflectAccessException when class with private no-args constructor is "
      + "passed as argument")
  @Order(4)
  void whenCreateEntityInstanceOnClassWithPrivateNoArgsConstructor_thenExceptionIsThrown() {
    assertThatThrownBy(() -> createEntityInstance(ClassWithoutPublicNoArgsConstructor.class))
        .isInstanceOf(ReflectAccessException.class)
        .hasMessage("Entity [%s] should have public no-args constructor".formatted(
            ClassWithoutPublicNoArgsConstructor.class));
  }

  @Test
  @DisplayName("Throw ReflectAccessException when class without no-args constructor is "
      + "passed as argument")
  @Order(5)
  void whenCreateEntityInstanceOnClassWithMultiArgsConstructor_thenExceptionIsThrown() {
    assertThatThrownBy(() -> createEntityInstance(ClassWithoutNoArgsConstructor.class))
        .isInstanceOf(ReflectAccessException.class)
        .hasMessage("Entity [%s] should have public no-args constructor".formatted(
            ClassWithoutNoArgsConstructor.class));
  }

  @Test
  @DisplayName("When write field value with valid type then success")
  @Order(6)
  @SneakyThrows
  void whenWriteFieldValueWithValidType_thenSuccess() {
    // data
    ClassWithSinglePublicField inputClass = new ClassWithSinglePublicField();
    Field inputField = inputClass.getClass().getDeclaredField(TEST_CLASS_STRING_FIELD);
    String inputValidValue = TEST_STRING_VALUE;
    // given
    inputField.setAccessible(true);
    // when
    ReflectionUtil.writeFieldValue(inputField, inputClass, inputValidValue);
    // verify
    assertThat(inputClass.stringFieldName).isEqualTo(inputValidValue);
  }

  @Test
  @DisplayName("Throw ReflectAccessException when writing field is not accessible")
  @Order(7)
  @SneakyThrows
  void whenWriteFieldValueWithInaccessibleField_thenThrowException() {
    // data
    ClassWithSinglePublicField inputClass = new ClassWithSinglePublicField();
    Field inputField = inputClass.getClass().getDeclaredField(TEST_CLASS_STRING_FIELD);
    String inputValidValue = "valid value";
    // given
    inputField.setAccessible(false);
    // when
    assertThatThrownBy(
        () -> ReflectionUtil.writeFieldValue(inputField, inputClass, inputValidValue))
        .isInstanceOf(ReflectAccessException.class)
        .hasMessage("Object field [%s] should be accessible and not final".formatted(inputField));
  }

  @Test
  @DisplayName("Throw ReflectAccessException when writing value has different type than field")
  @Order(8)
  @SneakyThrows
  void whenWriteFieldValueWithWrongValueType_thenThrowException() {
    // data
    ClassWithSinglePublicField inputClass = new ClassWithSinglePublicField();
    Field inputField = inputClass.getClass().getDeclaredField(TEST_CLASS_STRING_FIELD);
    Integer inputInvalidValue = 12345;
    // given
    inputField.setAccessible(true);
    // when
    assertThatThrownBy(
        () -> ReflectionUtil.writeFieldValue(inputField, inputClass, inputInvalidValue))
        .isInstanceOf(ReflectAccessException.class)
        .hasMessage(
            "Mismatched types: Expected value of type [%s] but received value of type [%s]".formatted(
                inputField.getType().getSimpleName(),
                inputInvalidValue.getClass().getSimpleName()));
  }

  @Test
  @DisplayName("Throw ReflectAccessException when writing value to field that not belongs "
      + "to provided class object")
  @Order(9)
  @SneakyThrows
  void whenWriteFieldValueWithFieldAndObjectAreDifferent_thenThrowException() {
    // data
    ClassWithSinglePublicField inputClass = new ClassWithSinglePublicField();
    Field inputWrongField = ClassWithSingleOtherPublicField.class.getDeclaredField(
        TEST_CLASS_INTEGER_FIELD);
    Integer inputValidValue = 12345;
    // given
    inputWrongField.setAccessible(true);
    // when
    assertThatThrownBy(
        () -> ReflectionUtil.writeFieldValue(inputWrongField, inputClass, inputValidValue))
        .isInstanceOf(ReflectAccessException.class)
        .hasMessage("Mismatched field owner object: field [%s]; object class [%s]".formatted(
            inputWrongField, inputClass.getClass().getName()));
  }

  @Test
  @DisplayName("When read valid class field value then return expected object")
  @Order(10)
  void whenReadFieldValue_thenSuccess() throws NoSuchFieldException {
    // data
    ClassWithSinglePublicField testClass = new ClassWithSinglePublicField();
    testClass.setStringFieldName(TEST_STRING_VALUE);
    Field testField = testClass.getClass().getDeclaredField(TEST_CLASS_STRING_FIELD);
    // when
    Object fieldValue = ReflectionUtil.readFieldValue(testClass, testField);
    // verify
    assertThat(fieldValue)
        .as("Result object is not 'null'")
        .isNotNull()
        .as("Result object has correct value")
        .isEqualTo(TEST_STRING_VALUE);
  }

  @Test
  @DisplayName("Throw ReflectAccessException when object field value is inaccessible")
  @Order(11)
  @SneakyThrows
  void whenReadFieldValueIsInaccessible_thenThrowException() {
    // data
    String testObject = TEST_STRING_VALUE;
    Field inaccessibleField = String.class.getDeclaredField("value");
    // when
    assertThatThrownBy(() -> ReflectionUtil.readFieldValue(testObject, inaccessibleField))
        .isInstanceOf(ReflectAccessException.class)
        .hasMessage("Failed to access field '" + inaccessibleField.getName() + "' of obj type '"
            + testObject.getClass().getName() + "': Illegal access");

  }

  @Test
  @DisplayName("Throw ReflectAccessException when reading value has different type than field")
  @Order(12)
  @SneakyThrows
  void whenReadFieldValueWithFieldAndObjectAreDifferent_thenThrowException() {
    // data
    ClassWithSinglePublicField inputClass = new ClassWithSinglePublicField();
    Field inputWrongField = ClassWithSingleOtherPublicField.class.getDeclaredField(
        TEST_CLASS_INTEGER_FIELD);
    // when
    assertThatThrownBy(() -> ReflectionUtil.readFieldValue(inputClass, inputWrongField))
        .isInstanceOf(ReflectAccessException.class)
        .hasMessage("Mismatched field owner object: field [%s]; object class [%s]".formatted(
            inputWrongField, inputClass.getClass().getName()));
  }

  /**
   * Abstract class for testing purpose
   */
  private static abstract class AbstractClass {

    public AbstractClass() {
    }
  }

  /**
   * Class without public no-args constructor for testing purpose
   */
  private static class ClassWithoutPublicNoArgsConstructor {

    private ClassWithoutPublicNoArgsConstructor() {
    }
  }

  /**
   * Class without no-args constructor for testing purpose
   */
  private static class ClassWithoutNoArgsConstructor {

    private ClassWithoutNoArgsConstructor(int number) {
    }
  }

  /**
   * Class with an exception thrown in its no-args constructor for testing purpose.
   */
  private static class ClassWithExceptionInNoArgsConstructor {

    public ClassWithExceptionInNoArgsConstructor() {
      throw new NullPointerException("TEST");
    }
  }

  /**
   * Class with single {@code String} public field for testing purpose
   */
  @Setter
  private static class ClassWithSinglePublicField {

    private String stringFieldName;
  }

  /**
   * CLass with single {@code Integer} public field for testing purpose
   */
  private static class ClassWithSingleOtherPublicField {

    private Integer integerFieldName;
  }
}