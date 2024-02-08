package com.breskul.bibernate.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.breskul.bibernate.exception.AssociationException;
import com.breskul.bibernate.proxy.collection.LazyList;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

class AssociationUtilTest {

  private static final String LIST_FIELD_NAME = "listField";
  private static final String SET_FIELD_NAME = "setField";
  private static final String INTEGER_FIELD_NAME = "integerField";
  private static final List<String> SAMPLE_DATA_LIST = List.of("sample_data_1", "sample_data_2");
  private static final String LAZY_DELEGATE_FIELD = "delegate";

  @Test
  @DisplayName("When field type is 'List' then created collection is empty 'ArrayList'")
  @Order(1)
  @SneakyThrows
  void whenGetCollectionInstanceWithValidField_thenResultCollectionIsArrayList() {
    // data
    Field inputListField = SampleEntity.class.getDeclaredField(LIST_FIELD_NAME);
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
    Field inputListField = SampleEntity.class.getDeclaredField(SET_FIELD_NAME);
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
    Field inputIntegerField = SampleEntity.class.getDeclaredField(INTEGER_FIELD_NAME);
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
    Field inputListField = SampleEntity.class.getDeclaredField(LIST_FIELD_NAME);
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
    Field inputListField = SampleEntity.class.getDeclaredField(LIST_FIELD_NAME);
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
    Field inputSetField = SampleEntity.class.getDeclaredField(SET_FIELD_NAME);
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
    Field inputSetField = SampleEntity.class.getDeclaredField(SET_FIELD_NAME);
    List<String> inputSourceCollection = SAMPLE_DATA_LIST;
    // when
    Collection<Object> resultCollection = AssociationUtil.getCollectionInstance(inputSetField,
        inputSourceCollection);
    // verify
    assertThat(resultCollection).containsExactlyElementsOf(inputSourceCollection);
  }

  @Test
  @DisplayName("Throw AssociationException when field type is invalid ('Integer') and "
      + "source collection has data")
  @Order(8)
  @SneakyThrows
  void whenGetCollectionInstanceWithInvalidFieldAndValidSourceList_thenThrowException() {
    // data
    Field inputIntegerField = SampleEntity.class.getDeclaredField(INTEGER_FIELD_NAME);
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
    Field inputListField = SampleEntity.class.getDeclaredField(LIST_FIELD_NAME);
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
    Field inputListField = SampleEntity.class.getDeclaredField(LIST_FIELD_NAME);
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
    Field inputListField = SampleEntity.class.getDeclaredField(LIST_FIELD_NAME);
    // when
    Collection<Object> resultCollection = AssociationUtil.getLazyCollectionInstance(inputListField,
        () -> SAMPLE_DATA_LIST);
    // verify
    assertThat(resultCollection).containsExactlyElementsOf(SAMPLE_DATA_LIST);
  }

  // todo: create test, created collection is LazySet
  // todo: create test, created 'LazySet' is not initialized
  // todo: create test, created 'LazySet' has same data
  // todo: create test, throw exception for invalid field

  // todo: create test cases, getLazyObjectProxy method

  /**
   * Class with different fields for testing purpose
   */
  private static class SampleEntity {

    private List<String> listField;
    private Set<String> setField;
    private Integer integerField;
  }
}