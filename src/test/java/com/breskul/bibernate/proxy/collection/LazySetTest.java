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

class LazySetTest {

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
    LazySet<Object> objects = new LazySet<>(spyListSupplier);
    Assertions.assertThat(objects).isNotNull();
  }

  @Test
  void testSize_callsDelegate() {
    LazySet<?> lazyList = new LazySet<>(spyListSupplier);

    lazyList.size();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testIsEmpty_callsDelegate() {
    LazySet<?> lazyList = new LazySet<>(spyListSupplier);

    lazyList.isEmpty();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testContains_callsDelegate() {
    LazySet<?> lazyList = new LazySet<>(spyListSupplier);

    lazyList.contains(0);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testIterator_callsDelegate() {
    LazySet<?> lazyList = new LazySet<>(spyListSupplier);

    lazyList.iterator();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testToArrayNoArgs_callsDelegate() {
    LazySet<?> lazyList = new LazySet<>(spyListSupplier);

    lazyList.toArray();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testToArrayArrayArgs_callsDelegate() {
    LazySet<?> lazyList = new LazySet<>(spyListSupplier);

    lazyList.toArray(new Object[]{});

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testAdd_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.add(5);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testRemove_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.remove(new Object());

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testRemoveObject_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.add(new Object());

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testContainsAll_callsDelegate() {
    LazySet<?> lazyList = new LazySet<>(spyListSupplier);

    lazyList.containsAll(Collections.emptyList());

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testAddAll_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.addAll(List.of(1));

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testRemoveAll_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.removeAll(List.of(1));

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testRetainAll_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.retainAll(List.of(1));

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testClear_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.clear();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testEquals_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.equals(List.of());

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testHashCode_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.hashCode();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testRemoveIndex_callsDelegate() {
    LazySet<?> lazyList = new LazySet<>(spyListSupplier);

    lazyList.remove(0);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testSpliterator_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.spliterator();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testToArrayGenerator_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.toArray(Object[]::new);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testRemoveIf_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.removeIf(Objects::isNull);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testStream_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.stream();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testParallelStream_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.parallelStream();

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testForEach_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.forEach(Object::toString);

    verify(spyListSupplier, times(1)).get();
  }

  @Test
  void testToString_callsDelegate() {
    LazySet<Object> lazyList = new LazySet<>(spyListSupplier);

    lazyList.toString();

    verify(spyListSupplier, times(1)).get();
  }
}