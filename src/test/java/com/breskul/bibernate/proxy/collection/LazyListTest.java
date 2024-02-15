package com.breskul.bibernate.proxy.collection;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
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