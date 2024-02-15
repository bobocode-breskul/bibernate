package com.breskul.bibernate.proxy.collection;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class LazySetTest {

  @Test
  void testLazySetBasicOperations() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazySet<String> lazySet = new LazySet<>(supplier);

    // Test basic operations
    assertEquals(3, lazySet.size());
    assertFalse(lazySet.isEmpty());
    assertTrue(lazySet.contains("Two"));

    // Test iterator
    int count = 0;
    for (String item : lazySet) {
      assertNotNull(item);
      count++;
    }
    assertEquals(3, count);

    // Test toArray
    Object[] array = lazySet.toArray();
    assertEquals(3, array.length);

    // Test add
    assertTrue(lazySet.add("Four"));
    assertEquals(4, lazySet.size());
    assertTrue(lazySet.contains("Four"));

    // Test remove
    assertTrue(lazySet.remove("Two"));
    assertEquals(3, lazySet.size());
    assertFalse(lazySet.contains("Two"));

    // Test addAll
    List<String> newList = Arrays.asList("Five", "Six");
    assertTrue(lazySet.addAll(newList));
    assertEquals(5, lazySet.size());
    assertTrue(lazySet.contains("Five"));
    assertTrue(lazySet.contains("Six"));

    // Test removeAll
    assertTrue(lazySet.removeAll(Arrays.asList("Four", "Six")));
    assertEquals(3, lazySet.size());

    // Test clear
    lazySet.clear();
    assertEquals(0, lazySet.size());
    assertTrue(lazySet.isEmpty());
  }

  @Test
  void testLazySetRetainAll() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazySet<String> lazySet = new LazySet<>(supplier);

    // Test retainAll
    assertTrue(lazySet.retainAll(Arrays.asList("Two", "Three")));
    assertEquals(2, lazySet.size());
    assertTrue(lazySet.contains("Two"));
    assertTrue(lazySet.contains("Three"));
    assertFalse(lazySet.contains("One"));
  }

  @Test
  void testLazySetRemoveIf() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three",
        "Four", "Five");
    LazySet<String> lazySet = new LazySet<>(supplier);

    // Test removeIf
    assertTrue(lazySet.removeIf(s -> s.length() == 3));
    assertEquals(3, lazySet.size());
    assertFalse(lazySet.contains("Two"));
    assertFalse(lazySet.contains("One"));
  }

  @Test
  void testLazySetStreamAndForEach() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazySet<String> lazySet = new LazySet<>(supplier);

    // Test stream
    Stream<String> stream = lazySet.stream();
    assertNotNull(stream);
    List<String> collected = stream.collect(Collectors.toList());
    assertEquals(Arrays.asList("One", "Two", "Three"), collected);

    // Test parallelStream
    Stream<String> parallelStream = lazySet.parallelStream();
    assertNotNull(parallelStream);
    List<String> parallelCollected = parallelStream.collect(Collectors.toList());
    assertEquals(Arrays.asList("One", "Two", "Three"), parallelCollected);

    // Test forEach
    List<String> forEachResult = new ArrayList<>();
    lazySet.forEach(forEachResult::add);
    assertEquals(Arrays.asList("One", "Two", "Three"), forEachResult);
  }

  @Test
  void testLazySetToString() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazySet<String> lazySet = new LazySet<>(supplier);

    // Test toString
    assertEquals("[One, Two, Three]", lazySet.toString());
  }

  @Test
  void testLazySetEqualsAndHashCode() {
    Supplier<Collection<? extends String>> supplier1 = () -> Arrays.asList("One", "Two", "Three");
    Supplier<Collection<? extends String>> supplier2 = () -> Arrays.asList("One", "Two", "Three");
    LazySet<String> lazySet1 = new LazySet<>(supplier1);
    LazySet<String> lazySet2 = new LazySet<>(supplier2);

    // Test equals and hashCode
    assertEquals(lazySet1, lazySet2);
    assertEquals(lazySet1.hashCode(), lazySet2.hashCode());
  }

  @Test
  void testLazySetSpliteratorAndToArrayWithGenerator() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazySet<String> lazySet = new LazySet<>(supplier);

    // Test spliterator
    Spliterator<String> spliterator = lazySet.spliterator();
    assertNotNull(spliterator);

    // Test toArray with generator
    String[] array = lazySet.toArray(String[]::new);
    assertNotNull(array);
    assertArrayEquals(new String[]{"One", "Two", "Three"}, array);
  }

  @Test
  void testLazySetContainsAll() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazySet<String> lazySet = new LazySet<>(supplier);

    // Test containsAll
    List<String> subList = Arrays.asList("Two", "Three");
    assertTrue(lazySet.containsAll(subList));
  }

  @Test
  void testLazySetToArrayWithArray() {
    Supplier<Collection<? extends String>> supplier = () -> Arrays.asList("One", "Two", "Three");
    LazySet<String> lazySet = new LazySet<>(supplier);

    // Test toArray with array
    String[] array = lazySet.toArray(new String[0]);
    assertNotNull(array);
    assertArrayEquals(new String[]{"One", "Two", "Three"}, array);
  }
}