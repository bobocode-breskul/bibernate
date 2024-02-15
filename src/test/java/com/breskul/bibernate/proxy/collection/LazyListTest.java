package com.breskul.bibernate.proxy.collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class LazyListTest {

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

}