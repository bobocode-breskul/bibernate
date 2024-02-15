package com.breskul.bibernate.proxy.collection;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LazyListTest {

  private Supplier<Collection<?>> spyListSupplier;

  @BeforeEach
  void beforeMethod() {
    // Mockito cannot spy on lambdas, so real anonymous class is necessary
    spyListSupplier = spy(new Supplier<Collection<?>>() {
      @Override
      public Collection<?> get() {
        return new ArrayList<>(List.of(1, 2, 3));
      }
    });
  }

  @Test
  void testLazyListBasicOperations() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test basic operations
    assertEquals(3, lazyList.size());
    assertFalse(lazyList.isEmpty());
    assertTrue(lazyList.contains("Two"));
    assertEquals(2, lazyList.indexOf("Three"));
    assertEquals(1, lazyList.lastIndexOf("Two"));

    // Test iterator
    int count = 0;
    for (String item : lazyList) {
      assertNotNull(item);
      count++;
    }
    assertEquals(3, count);

    // Test toArray
    Object[] array = lazyList.toArray();
    assertEquals(3, array.length);

    // Test equals and hashCode
    LazyList<String> sameLazyList = new LazyList<>(supplier);
    assertEquals(lazyList, sameLazyList);
    assertEquals(lazyList.hashCode(), sameLazyList.hashCode());
  }

  @Test
  void testLazyListAddOperations() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test add
    assertTrue(lazyList.add("Four"));
    assertEquals(4, lazyList.size());
    assertTrue(lazyList.contains("Four"));

    // Test addAll
    List<String> newList = Arrays.asList("Five", "Six");
    assertTrue(lazyList.addAll(newList));
    assertEquals(6, lazyList.size());
    assertTrue(lazyList.contains("Five"));
    assertTrue(lazyList.contains("Six"));

    // Test addAll at specific index
    assertTrue(lazyList.addAll(2, Arrays.asList("Seven", "Eight")));
    assertEquals(8, lazyList.size());
    assertEquals("Seven", lazyList.get(2));
    assertEquals("Eight", lazyList.get(3));
  }

  @Test
  void testLazyListRemoveOperations() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test remove
    assertTrue(lazyList.remove("Two"));
    assertEquals(2, lazyList.size());
    assertFalse(lazyList.contains("Two"));

    // Test removeAll
    assertTrue(lazyList.removeAll(Arrays.asList("One", "Three")));
    assertEquals(0, lazyList.size());
    assertTrue(lazyList.isEmpty());

    // Test removeIf
    lazyList.addAll(Arrays.asList("One", "Two", "Three", "Four", "Five"));
    assertTrue(lazyList.removeIf(s -> s.length() == 3));
    assertEquals(3, lazyList.size());
    assertFalse(lazyList.contains("Two"));
    assertFalse(lazyList.contains("One"));
  }

  @Test
  void testLazyListRetainAll() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test retainAll
    assertTrue(lazyList.retainAll(Arrays.asList("Two", "Three")));
    assertEquals(2, lazyList.size());
    assertTrue(lazyList.contains("Two"));
    assertTrue(lazyList.contains("Three"));
    assertFalse(lazyList.contains("One"));
  }

  @Test
  void testLazyListReplaceAndSort() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test replaceAll
    lazyList.replaceAll(String::toUpperCase);
    assertEquals("ONE", lazyList.get(0));
    assertEquals("TWO", lazyList.get(1));
    assertEquals("THREE", lazyList.get(2));

    // Test sort
    lazyList.sort(Comparator.reverseOrder());
    assertEquals("TWO", lazyList.get(0));
    assertEquals("THREE", lazyList.get(1));
    assertEquals("ONE", lazyList.get(2));
  }

  @Test
  void testLazyListSubListAndSpliterator() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three",
        "Four");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test subList
    List<String> subList = lazyList.subList(1, 4);
    assertEquals(3, subList.size());
    assertEquals("Two", subList.get(0));
    assertEquals("Three", subList.get(1));
    assertEquals("Four", subList.get(2));

    // Test spliterator
    Spliterator<String> spliterator = lazyList.spliterator();
    assertNotNull(spliterator);
  }

  @Test
  void testLazyListStreamAndForEach() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test stream
    Stream<String> stream = lazyList.stream();
    assertNotNull(stream);
    List<String> collected = stream.collect(Collectors.toList());
    assertEquals(Arrays.asList("One", "Two", "Three"), collected);

    // Test parallelStream
    Stream<String> parallelStream = lazyList.parallelStream();
    assertNotNull(parallelStream);
    List<String> parallelCollected = parallelStream.collect(Collectors.toList());
    assertEquals(Arrays.asList("One", "Two", "Three"), parallelCollected);

    // Test forEach
    List<String> forEachResult = new ArrayList<>();
    lazyList.forEach(forEachResult::add);
    assertEquals(Arrays.asList("One", "Two", "Three"), forEachResult);
  }

  @Test
  void testLazyListToString() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test toString
    assertEquals("[One, Two, Three]", lazyList.toString());
  }

  @Test
  void testLazyListClear() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test clear
    lazyList.clear();
    assertEquals(0, lazyList.size());
    assertTrue(lazyList.isEmpty());
  }

  @Test
  void testLazyListToArrayWithGenerator() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test toArray with generator
    String[] array = lazyList.toArray(String[]::new);
    assertNotNull(array);
    assertArrayEquals(new String[]{"One", "Two", "Three"}, array);
  }

  @Test
  void testLazyListContainsAll() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test containsAll
    List<String> subList = Arrays.asList("Two", "Three");
    assertTrue(lazyList.containsAll(subList));
  }

  @Test
  void testLazyListSet() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test set
    String replacedElement = lazyList.set(1, "NewTwo");
    assertEquals("Two", replacedElement);
    assertEquals("NewTwo", lazyList.get(1));
  }

  @Test
  void testLazyListAddAtIndex() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test add at index
    lazyList.add(1, "NewElement");
    assertEquals(4, lazyList.size());
    assertEquals("NewElement", lazyList.get(1));
  }

  @Test
  void testLazyListRemoveAtIndex() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test remove at index
    String removedElement = lazyList.remove(1);
    assertEquals("Two", removedElement);
    assertEquals(2, lazyList.size());
    assertEquals("Three", lazyList.get(1));
  }

  @Test
  void testLazyListListIterator() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test listIterator
    ListIterator<String> iterator = lazyList.listIterator();
    assertNotNull(iterator);
    assertTrue(iterator.hasNext());
    assertEquals("One", iterator.next());
  }

  @Test
  void testLazyListListIteratorWithIndex() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test listIterator with index
    ListIterator<String> iterator = lazyList.listIterator(1);
    assertNotNull(iterator);
    assertTrue(iterator.hasNext());
    assertEquals("Two", iterator.next());
  }

  @Test
  void testLazyListToArrayWithIntFunction() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test toArray with IntFunction
    String[] array = lazyList.toArray(String[]::new);
    assertNotNull(array);
    assertArrayEquals(new String[]{"One", "Two", "Three"}, array);
  }

  @Test
  void testLazyListToArrayWithArray() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazyList<String> lazyList = new LazyList<>(supplier);

    // Test toArray with array
    String[] array = lazyList.toArray(new String[0]);
    assertNotNull(array);
    assertArrayEquals(new String[]{"One", "Two", "Three"}, array);
  }

  @Test
  void testConstructor_createsCollection() {
    LazyList<Object> objects = new LazyList<>(spyListSupplier);
    Assertions.assertThat(objects).isNotNull();
  }

  @Test
  void testSize_callsDelegate() {
    LazyList<?> lazyList = new LazyList<>(spyListSupplier);

    lazyList.size();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testIsEmpty_callsDelegate() {
    LazyList<?> lazyList = new LazyList<>(spyListSupplier);

    lazyList.isEmpty();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testContains_callsDelegate() {
    LazyList<?> lazyList = new LazyList<>(spyListSupplier);

    lazyList.contains(0);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testIterator_callsDelegate() {
    LazyList<?> lazyList = new LazyList<>(spyListSupplier);

    lazyList.iterator();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testToArrayNoArgs_callsDelegate() {
    LazyList<?> lazyList = new LazyList<>(spyListSupplier);

    lazyList.toArray();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testToArrayArrayArgs_callsDelegate() {
    LazyList<?> lazyList = new LazyList<>(spyListSupplier);

    lazyList.toArray(new Object[]{});

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testAdd_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.add(5);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testRemove_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.remove(new Object());

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testRemoveObject_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.add(new Object());

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testContainsAll_callsDelegate() {
    LazyList<?> lazyList = new LazyList<>(spyListSupplier);

    lazyList.containsAll(Collections.emptyList());

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testAddAll_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.addAll(List.of(1));

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testAddAllWithIndex_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.addAll(0, List.of(1));

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testRemoveAll_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.removeAll(List.of(1));

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testRetainAll_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.retainAll(List.of(1));

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testReplaceAll_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.replaceAll(UnaryOperator.identity());

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testSort_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.sort(Comparator.comparing(Object::toString));

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testClear_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.clear();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testEquals_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.equals(List.of());

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testHashCode_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.hashCode();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testGetWithIndex_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.get(0);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testSetWithIndex_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.set(0, 0);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testAddWithIndex_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.add(0, 0);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testRemoveIndex_callsDelegate() {
    LazyList<?> lazyList = new LazyList<>(spyListSupplier);

    lazyList.remove(0);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testIndexOf_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.indexOf(0);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testLastIndexOf_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.lastIndexOf(0);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testListIterator_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.listIterator();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testListIteratorWithIndex_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.listIterator(0);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testSubList_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.subList(0, 1);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testSpliterator_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.spliterator();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testToArrayGenerator_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.toArray(Object[]::new);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testRemoveIf_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.removeIf(Objects::isNull);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testStream_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.stream();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testParallelStream_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.parallelStream();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testForEach_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.forEach(Object::toString);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testToString_callsDelegate() {
    LazyList<Object> lazyList = new LazyList<>(spyListSupplier);

    lazyList.toString();

    verify(spyListSupplier, times(1)).get();
  }
}