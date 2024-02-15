package com.breskul.bibernate.proxy.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;


/**
 * LazyList represents a lazily initialized list that delegates its operations to another list. The
 * delegate list is supplied by a supplier, which allows for lazy initialization.
 *
 * @param <T> the type of elements in the list
 */
public class LazyList<T> implements List<T> {

  private final Supplier<Collection<? extends T>> delegateSupplier;
  private List<T> delegate;

  public LazyList(Supplier<Collection<? extends T>> delegateSupplier) {
    this.delegateSupplier = delegateSupplier;
  }

  private List<T> getDelegateList() {
    if (delegate == null) {
      delegate = new ArrayList<>(delegateSupplier.get());
    }
    return delegate;
  }

  @Override
  public int size() {
    return getDelegateList().size();
  }

  @Override
  public boolean isEmpty() {
    return getDelegateList().isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return getDelegateList().contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return getDelegateList().iterator();
  }

  @Override
  public Object[] toArray() {
    return getDelegateList().toArray();
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    return getDelegateList().toArray(a);
  }

  @Override
  public boolean add(T t) {
    return getDelegateList().add(t);
  }

  @Override
  public boolean remove(Object o) {
    return getDelegateList().remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return getDelegateList().containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    return getDelegateList().addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    return getDelegateList().addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return getDelegateList().removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return getDelegateList().retainAll(c);
  }

  @Override
  public void replaceAll(UnaryOperator<T> operator) {
    getDelegateList().replaceAll(operator);
  }

  @Override
  public void sort(Comparator<? super T> c) {
    getDelegateList().sort(c);
  }

  @Override
  public void clear() {
    getDelegateList().clear();
  }

  @Override
  public boolean equals(Object o) {
    return getDelegateList().equals(o);
  }

  @Override
  public int hashCode() {
    return getDelegateList().hashCode();
  }

  @Override
  public T get(int index) {
    return getDelegateList().get(index);
  }

  @Override
  public T set(int index, T element) {
    return getDelegateList().set(index, element);
  }

  @Override
  public void add(int index, T element) {
    getDelegateList().add(index, element);
  }

  @Override
  public T remove(int index) {
    return getDelegateList().remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return getDelegateList().indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return getDelegateList().lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator() {
    return getDelegateList().listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return getDelegateList().listIterator(index);
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return getDelegateList().subList(fromIndex, toIndex);
  }

  @Override
  public Spliterator<T> spliterator() {
    return getDelegateList().spliterator();
  }

  @Override
  public <T1> T1[] toArray(IntFunction<T1[]> generator) {
    return getDelegateList().toArray(generator);
  }

  @Override
  public boolean removeIf(Predicate<? super T> filter) {
    return getDelegateList().removeIf(filter);
  }

  @Override
  public Stream<T> stream() {
    return getDelegateList().stream();
  }

  @Override
  public Stream<T> parallelStream() {
    return getDelegateList().parallelStream();
  }

  @Override
  public void forEach(Consumer<? super T> action) {
    getDelegateList().forEach(action);
  }

  @Override
  public String toString() {
    return getDelegateList().toString();
  }
}
