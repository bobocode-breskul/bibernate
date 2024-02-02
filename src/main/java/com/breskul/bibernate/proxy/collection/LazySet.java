package com.breskul.bibernate.proxy.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

// todo: docs
public class LazySet<T> implements Set<T> {
  private final Supplier<Collection<? extends T>> delegateSupplier;

  private Set<T> delegate;

  public LazySet(Supplier<Collection<? extends T>> delegateSupplier) {
    this.delegateSupplier = delegateSupplier;
  }

  private Set<T> getDelegateSet() {
    if (delegate == null) {
      delegate = new HashSet<>(delegateSupplier.get());
    }
    return delegate;
  }

  @Override
  public int size() {
    return getDelegateSet().size();
  }

  @Override
  public boolean isEmpty() {
    return getDelegateSet().isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return getDelegateSet().contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return getDelegateSet().iterator();
  }

  @Override
  public Object[] toArray() {
    return getDelegateSet().toArray();
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    return getDelegateSet().toArray(a);
  }

  @Override
  public boolean add(T t) {
    return getDelegateSet().add(t);
  }

  @Override
  public boolean remove(Object o) {
    return getDelegateSet().remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return getDelegateSet().containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    return getDelegateSet().addAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return getDelegateSet().retainAll(c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return getDelegateSet().removeAll(c);
  }

  @Override
  public void clear() {
    getDelegateSet().clear();
  }

  @Override
  public boolean equals(Object o) {
    return getDelegateSet().equals(o);
  }

  @Override
  public int hashCode() {
    return getDelegateSet().hashCode();
  }

  @Override
  public Spliterator<T> spliterator() {
    return getDelegateSet().spliterator();
  }

  @Override
  public <T1> T1[] toArray(IntFunction<T1[]> generator) {
    return getDelegateSet().toArray(generator);
  }

  @Override
  public boolean removeIf(Predicate<? super T> filter) {
    return getDelegateSet().removeIf(filter);
  }

  @Override
  public Stream<T> stream() {
    return getDelegateSet().stream();
  }

  @Override
  public Stream<T> parallelStream() {
    return getDelegateSet().parallelStream();
  }

  @Override
  public void forEach(Consumer<? super T> action) {
    getDelegateSet().forEach(action);
  }

  @Override
  public String toString() {
    return getDelegateSet().toString();
  }
}
