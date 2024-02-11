package com.breskul.bibernate.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;

import com.breskul.bibernate.exception.AssociationException;
import com.breskul.bibernate.proxy.collection.LazyList;
import com.breskul.bibernate.proxy.collection.LazySet;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AssociationUtilTest {

  private static final String LIST_FIELD_NAME = "listField";
  private static final String SET_FIELD_NAME = "setField";
  private static final String INTEGER_FIELD_NAME = "integerField";
  private static final String PROXY_FIELD_NAME = "proxyField";
  private static final List<String> SAMPLE_DATA_LIST = List.of("sample_data_1", "sample_data_2");
  private static final String LAZY_DELEGATE_FIELD = "delegate";
  private static final Pattern NAME_ENDS_WITH_8_CHARS_PATTERN = Pattern.compile(".+\\$\\w{8}$");

  @Test
  @DisplayName("When field type is 'List' then created collection is empty 'ArrayList'")
  @Order(1)
  @SneakyThrows
  void whenGetCollectionInstanceWithValidField_thenResultCollectionIsArrayList() {
    // data
    Field inputListField = EntityCollectionHolder.class.getDeclaredField(LIST_FIELD_NAME);
    // when
    Collection<Object> resultCollection = AssociationUtil.getCollectionInstance(inputListField);
    // verify
    assertThat(resultCollection)
        .as("Result is correct type")
        .isInstanceOf(ArrayList.class)
        .as("Result collection is empty")
        .isEmpty();
  }

  @Test
  @DisplayName("When field type is 'Set' then created collection is empty 'HashSet'")
  @Order(2)
  @SneakyThrows
  void whenGetCollectionInstanceWithValidField_thenResultCollectionIsHashSet() {
    // data
    Field inputListField = EntityCollectionHolder.class.getDeclaredField(SET_FIELD_NAME);
    // when
    Collection<Object> resultCollection = AssociationUtil.getCollectionInstance(inputListField);
    // verify
    assertThat(resultCollection)
        .as("Result is correct type")
        .isInstanceOf(HashSet.class)
        .as("Result collection is empty")
        .isEmpty();
  }

  @Test
  @DisplayName("Throw AssociationException when field type is invalid ('Integer')")
  @Order(3)
  @SneakyThrows
  void whenGetCollectionInstanceWithInvalidField_thenThrowException() {
    // data
    Field inputIntegerField = EntityCollectionHolder.class.getDeclaredField(INTEGER_FIELD_NAME);
    // when
    assertThatThrownBy(() -> AssociationUtil.getCollectionInstance(inputIntegerField))
        .isInstanceOf(AssociationException.class)
        .hasMessage("Entity association collection is not supported. Collection: [%s]".formatted(
            inputIntegerField.getType()));
  }

  @Test
  @DisplayName("When field type is 'List' and source collection has data then "
      + "created collection is 'ArrayList'")
  @Order(4)
  @SneakyThrows
  void whenGetCollectionInstanceWithValidSourceList_thenResultCollectionIsArrayList() {
    // data
    Field inputListField = EntityCollectionHolder.class.getDeclaredField(LIST_FIELD_NAME);
    // when
    Collection<Object> resultCollection = AssociationUtil.getCollectionInstance(inputListField,
        SAMPLE_DATA_LIST);
    // verify
    assertThat(resultCollection).isInstanceOf(ArrayList.class);
  }

  @Test
  @DisplayName("When field type is 'List' and source collection has data then created "
      + "'ArrayList' collection has the same data")
  @Order(5)
  @SneakyThrows
  void whenGetCollectionInstanceWithValidSourceList_thenResultCollectionHasCorrectData() {
    // data
    Field inputListField = EntityCollectionHolder.class.getDeclaredField(LIST_FIELD_NAME);
    List<String> inputSourceCollection = SAMPLE_DATA_LIST;
    // when
    Collection<Object> resultCollection = AssociationUtil.getCollectionInstance(inputListField,
        inputSourceCollection);
    // verify
    assertThat(resultCollection).containsExactlyElementsOf(inputSourceCollection);
  }

  @Test
  @DisplayName("When field type is 'Set' and source collection has data then "
      + "created collection is 'HashSet'")
  @Order(6)
  @SneakyThrows
  void whenGetCollectionInstanceWithValidSourceList_thenResultCollectionIsHashSet() {
    // data
    Field inputSetField = EntityCollectionHolder.class.getDeclaredField(SET_FIELD_NAME);
    // when
    Collection<Object> resultCollection = AssociationUtil.getCollectionInstance(inputSetField,
        SAMPLE_DATA_LIST);
    // verify
    assertThat(resultCollection).isInstanceOf(HashSet.class);
  }

  @Test
  @DisplayName("When field type is 'Set' and source collection has data then created "
      + "'HashSet' collection has the same data")
  @Order(7)
  @SneakyThrows
  void whenGetCollectionInstanceWithValidSourceSet_thenResultCollectionHasCorrectData() {
    // data
    Field inputSetField = EntityCollectionHolder.class.getDeclaredField(SET_FIELD_NAME);
    List<String> inputSourceCollection = SAMPLE_DATA_LIST;
    // when
    Collection<Object> resultCollection = AssociationUtil.getCollectionInstance(inputSetField,
        inputSourceCollection);
    // verify
    assertThat(resultCollection).containsExactlyElementsOf(inputSourceCollection);
  }

  @Test
  @DisplayName("Throw AssociationException when generating lazy collection with invalid field type "
      + "('Integer') and source collection has data")
  @Order(8)
  @SneakyThrows
  void whenGetCollectionInstanceWithInvalidFieldAndValidSourceList_thenThrowException() {
    // data
    Field inputIntegerField = EntityCollectionHolder.class.getDeclaredField(INTEGER_FIELD_NAME);
    // when
    assertThatThrownBy(
        () -> AssociationUtil.getCollectionInstance(inputIntegerField, SAMPLE_DATA_LIST))
        .isInstanceOf(AssociationException.class)
        .hasMessage("Entity association collection is not supported. Collection: [%s]".formatted(
            inputIntegerField.getType()));
  }

  @Test
  @DisplayName("When field type is 'List' and delegate collection has data then "
      + "created collection is 'LazyList'")
  @Order(9)
  @SneakyThrows
  void whenGetLazyCollectionInstanceWithValidDelegateList_thenResultCollectionIsLazyList() {
    // data
    Field inputListField = EntityCollectionHolder.class.getDeclaredField(LIST_FIELD_NAME);
    // when
    Collection<Object> resultCollection = AssociationUtil.getLazyCollectionInstance(inputListField,
        () -> SAMPLE_DATA_LIST);
    // verify
    assertThat(resultCollection).isInstanceOf(LazyList.class);
  }

  @Test
  @DisplayName("When field type is 'List' and delegate collection has data then "
      + "created 'LazyList' is not initialized")
  @Order(10)
  @SneakyThrows
  void whenGetLazyCollectionInstanceWithValidDelegateList_thenResultLazyListIsNotInitialized() {
    // data
    Field inputListField = EntityCollectionHolder.class.getDeclaredField(LIST_FIELD_NAME);
    // when
    Collection<Object> resultCollection = AssociationUtil.getLazyCollectionInstance(inputListField,
        () -> SAMPLE_DATA_LIST);
    // verify
    Field lazyDelegateField = LazyList.class.getDeclaredField(LAZY_DELEGATE_FIELD);
    lazyDelegateField.setAccessible(true);
    assertThat(lazyDelegateField.get(resultCollection)).isNull();
  }

  @Test
  @DisplayName("When field type is 'List' and delegate collection has data then "
      + "'LazyList' collection has the same data")
  @Order(11)
  @SneakyThrows
  void whenGetLazyCollectionInstanceWithValidDelegateList_thenResultCollectionHasCorrectData() {
    // data
    Field inputListField = EntityCollectionHolder.class.getDeclaredField(LIST_FIELD_NAME);
    // when
    Collection<Object> resultCollection = AssociationUtil.getLazyCollectionInstance(inputListField,
        () -> SAMPLE_DATA_LIST);
    // verify
    assertThat(resultCollection).containsExactlyElementsOf(SAMPLE_DATA_LIST);
  }

  @Test
  @DisplayName("When field type is 'Set' and delegate collection has data then "
      + "created collection is 'LazySet'")
  @Order(12)
  @SneakyThrows
  void whenGetLazyCollectionInstanceWithValidDelegateSet_thenResultCollectionIsLazySet() {
    // data
    Field inputListField = EntityCollectionHolder.class.getDeclaredField(SET_FIELD_NAME);
    // when
    Collection<Object> resultCollection = AssociationUtil.getLazyCollectionInstance(inputListField,
        () -> SAMPLE_DATA_LIST);
    // verify
    assertThat(resultCollection).isInstanceOf(LazySet.class);
  }

  @Test
  @DisplayName("When field type is 'Set' and delegate collection has data then "
      + "created 'LazySet' is not initialized")
  @Order(13)
  @SneakyThrows
  void whenGetLazyCollectionInstanceWithValidDelegateSet_thenResultLazySetIsNotInitialized() {
    // data
    Field inputListField = EntityCollectionHolder.class.getDeclaredField(SET_FIELD_NAME);
    // when
    Collection<Object> resultCollection = AssociationUtil.getLazyCollectionInstance(inputListField,
        () -> SAMPLE_DATA_LIST);
    // verify
    Field lazyDelegateField = LazySet.class.getDeclaredField(LAZY_DELEGATE_FIELD);
    lazyDelegateField.setAccessible(true);
    assertThat(lazyDelegateField.get(resultCollection)).isNull();
  }

  @Test
  @DisplayName("When field type is 'Set' and delegate collection has data then "
      + "'LazySet' collection has the same data")
  @Order(14)
  @SneakyThrows
  void whenGetLazyCollectionInstanceWithValidDelegateSet_thenResultCollectionHasCorrectData() {
    // data
    Field inputListField = EntityCollectionHolder.class.getDeclaredField(SET_FIELD_NAME);
    // when
    Collection<Object> resultCollection = AssociationUtil.getLazyCollectionInstance(inputListField,
        () -> SAMPLE_DATA_LIST);
    // verify
    assertThat(resultCollection).containsExactlyElementsOf(SAMPLE_DATA_LIST);
  }

  @Test
  @DisplayName("Throw AssociationException when generating lazy collection with invalid field type "
      + "('Integer') and source collection has data provided")
  @Order(15)
  @SneakyThrows
  void whenGetLazyCollectionInstanceWithInvalidFieldAndValidSourceList_thenThrowException() {
    // data
    Field inputIntegerField = EntityCollectionHolder.class.getDeclaredField(INTEGER_FIELD_NAME);
    // when
    assertThatThrownBy(
        () -> AssociationUtil.getLazyCollectionInstance(inputIntegerField, () -> SAMPLE_DATA_LIST))
        .isInstanceOf(AssociationException.class)
        .hasMessage("Entity association collection is not supported. Collection: [%s]".formatted(
            inputIntegerField.getType()));
  }

  @Test
  @DisplayName("When field type is valid object class then result proxy class is same "
      + "as field class")
  @Order(16)
  @SneakyThrows
  void whenGetLazyObjectProxyWithValidField_thenResultProxyClassHasCorrectClass() {
    // data
    Field inputProxyField = EntityObjectHolder.class.getDeclaredField(PROXY_FIELD_NAME);
    ProxyEntity proxyDelegateObject = getDelegateObject();
    // when
    Object resultObject = AssociationUtil.getLazyObjectProxy(inputProxyField,
        () -> proxyDelegateObject);
    // verify
    assertThat(resultObject).isInstanceOf(ProxyEntity.class);
  }

  @Test
  @DisplayName("When field type is valid object class then result proxy class has correct name")
  @Order(17)
  @SneakyThrows
  void whenGetLazyObjectProxyWithValidField_thenResultProxyClassHasCorrectName() {
    // data
    Field inputProxyField = EntityObjectHolder.class.getDeclaredField(PROXY_FIELD_NAME);
    ProxyEntity proxyDelegateObject = getDelegateObject();
    // when
    String resultName = AssociationUtil.getLazyObjectProxy(inputProxyField,
        () -> proxyDelegateObject).getClass().getName();
    // verify
    assertThat(resultName)
        .as("Proxy name has delegate full name ending with 'BibernateProxy'")
        .startsWith(inputProxyField.getType().getName() + "$BibernateProxy$")
        .as("Proxy name ends with 8 random alphanumeric characters")
        .matches(NAME_ENDS_WITH_8_CHARS_PATTERN);
  }

  @Test
  @DisplayName("When field type is valid object class then result proxy delegate "
      + "object is not initialized")
  @Order(18)
  @SneakyThrows
  void whenGetLazyObjectProxyWithValidField_thenResultProxyDelegateObjectIsNotInitialized() {
    // data
    Field inputProxyField = EntityObjectHolder.class.getDeclaredField(PROXY_FIELD_NAME);
    Supplier<?> mockedDelegateSupplier = Mockito.mock(Supplier.class);
    // when
    Object resultObject = AssociationUtil.getLazyObjectProxy(inputProxyField,
        mockedDelegateSupplier);
    // verify
    assertThat(resultObject).isNotNull();
    then(mockedDelegateSupplier).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("When field type is valid object class then result proxy has correct field values")
  @Order(19)
  @SneakyThrows
  void whenGetLazyObjectProxyWithValidField_thenResultProxyHasCorrectFieldValues() {
    // data
    Field inputProxyField = EntityObjectHolder.class.getDeclaredField(PROXY_FIELD_NAME);
    ProxyEntity proxyDelegateObject = getDelegateObject();
    // when
    ProxyEntity resultObject = (ProxyEntity) AssociationUtil.getLazyObjectProxy(
        inputProxyField, () -> proxyDelegateObject);
    // verify
    assertThat(resultObject.getStringField())
        .isEqualTo(proxyDelegateObject.getStringField());
    assertThat(resultObject.getIntegerField())
        .isEqualTo(proxyDelegateObject.getIntegerField());
  }

  @Test
  @DisplayName("Throw AssociationException when proxy delegate no-args constructor "
      + "throws exception")
  @Order(20)
  @SneakyThrows
  void whenGetLazyObjectProxyOnDelegateClassWithConstructorWithException_thenExceptionIsThrown() {
    // data
    Field inputProxyField = EntityWithProxyConstructorException.class.getDeclaredField(
        PROXY_FIELD_NAME);
    // when
    Assertions.assertThatThrownBy(
            () -> AssociationUtil.getLazyObjectProxy(inputProxyField, Object::new))
        .isInstanceOf(AssociationException.class)
        .hasMessage("Could not create proxy instance of target entity [%s]"
            .formatted(ClassWithExceptionInNoArgsConstructor.class));
  }

  @Test
  @DisplayName("Throw AssociationException when proxy delegate class no-args "
      + "constructor is private")
  @Order(21)
  @SneakyThrows
  void whenGetLazyObjectProxyOnDelegateClassWithPrivateNoArgsConstructor_thenExceptionIsThrown() {
    // data
    Field inputProxyField = EntityWithProxyPrivateNoArgsConstructorClass.class.getDeclaredField(
        PROXY_FIELD_NAME);
    // when
    Assertions.assertThatThrownBy(
            () -> AssociationUtil.getLazyObjectProxy(inputProxyField, Object::new))
        .isInstanceOf(AssociationException.class)
        .hasMessage("Proxied entity [%s] should have public no-args constructor"
            .formatted(ClassWithPrivateNoArgsConstructor.class));
  }

  @Test
  @DisplayName("Throw AssociationException when proxy delegate class has only "
      + "multi-args constructor")
  @Order(22)
  @SneakyThrows
  void whenGetLazyObjectProxyOnDelegateClassWithMultiArgsConstructor_thenExceptionIsThrown() {
    // data
    Field inputProxyField = EntityWithProxyWithoutNoArgsConstructor.class.getDeclaredField(
        PROXY_FIELD_NAME);
    // when
    Assertions.assertThatThrownBy(
            () -> AssociationUtil.getLazyObjectProxy(inputProxyField, Object::new))
        .isInstanceOf(AssociationException.class)
        .hasMessage("Proxied entity [%s] should have public no-args constructor"
            .formatted(ClassWithoutNoArgsConstructor.class));
  }

  private ProxyEntity getDelegateObject() {
    return new ProxyEntity("string", 100);
  }

  /**
   * Class with different fields for testing purpose
   */
  private static class EntityCollectionHolder {

    private List<String> listField;
    private Set<String> setField;
    private Integer integerField;
  }

  /**
   * Class with single object field for testing purpose
   */
  private static class EntityObjectHolder {

    private ProxyEntity proxyField;
  }

  /**
   * Represents a test field class with a string field and an integer field. Should be public to
   * make proxy for it.
   */
  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  public static class ProxyEntity {

    private String stringField;
    private Integer integerField;
  }

  /**
   * Class with object field for class that throws exception in no-args constructor for testing
   * purpose
   */
  private static class EntityWithProxyConstructorException {

    private ClassWithExceptionInNoArgsConstructor proxyField;
  }

  /**
   * Class with an exception thrown in its no-args constructor for testing purpose. Should be public
   * to make proxy for it.
   */
  public static class ClassWithExceptionInNoArgsConstructor {

    public ClassWithExceptionInNoArgsConstructor() {
      throw new NullPointerException("TEST");
    }
  }

  /**
   * Class with object field for class with private no-args constructor.
   */
  private static class EntityWithProxyPrivateNoArgsConstructorClass {

    private ClassWithPrivateNoArgsConstructor proxyField;
  }

  /**
   * Class with private no-args constructor for testing purpose. Should be public to make proxy
   * for it.
   */
  public static class ClassWithPrivateNoArgsConstructor {

    private ClassWithPrivateNoArgsConstructor() {
    }
  }

  /**
   * Class with object field for class without no-args constructor.
   */
  private static class EntityWithProxyWithoutNoArgsConstructor {

    private ClassWithoutNoArgsConstructor proxyField;
  }

  /**
   * Class without no-args constructor for testing purpose. Should be public to make proxy for it.
   */
  public static class ClassWithoutNoArgsConstructor {

    private ClassWithoutNoArgsConstructor(int number) {
    }
  }
}