/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Aggregations.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_;

import static java.util.Objects.requireNonNull;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.util_.annot.Immutable;

/**
 * Aggregation (ie, collection or map) utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Aggregations {
  /**
   * Map-backed delegation set, safe for concurrent modifications.
   * <p>
   * See {@link FailSafeSet} for further information.
   * </p>
   *
   * @param <E>
   * @author Stefano Chizzolini
   */
  private static class FailSafeMapBasedSet<E> extends FailSafeSet<E> {
    private final Map<E, Boolean> base;

    public FailSafeMapBasedSet(Map<E, Boolean> base) {
      super(base.keySet());

      this.base = requireNonNull(base);
    }

    @Override
    public boolean add(E e) {
      return base.put(e, Boolean.TRUE) == null;
    }
  }

  /**
   * Delegation set, safe for concurrent modifications.
   * <p>
   * The iterator is isolated from the backing map, so any modification occurring to the latter
   * during iteration doesn't cause {@link ConcurrentModificationException}. This addresses common
   * cases such as observer deregistration during observers iteration.
   * </p>
   *
   * @param <E>
   * @author Stefano Chizzolini
   */
  private static class FailSafeSet<E> extends AbstractSet<E> {
    private final Set<E> base;

    public FailSafeSet(Set<E> base) {
      this.base = requireNonNull(base);
    }

    @Override
    public boolean add(E e) {
      return base.add(e);
    }

    @Override
    public void clear() {
      base.clear();
    }

    @Override
    public boolean contains(@Nullable Object o) {
      return base.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      return base.containsAll(c);
    }

    @Override
    public boolean equals(@Nullable Object o) {
      return super.equals(o) || base.equals(o);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
      base.forEach(action);
    }

    @Override
    public boolean isEmpty() {
      return base.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
      return new Iterator<>() {
        int i;
        @SuppressWarnings({ "unchecked" })
        final E[] items = base.toArray((E[]) new Object[base.size()]);

        @Override
        public boolean hasNext() {
          return i < items.length;
        }

        @Override
        public E next() {
          return items[i++];
        }
      };
    }

    @Override
    public Stream<E> parallelStream() {
      return base.parallelStream();
    }

    @Override
    public boolean remove(@Nullable Object o) {
      return base.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      return base.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
      return base.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      return base.retainAll(c);
    }

    @Override
    public int size() {
      return base.size();
    }

    @Override
    public Spliterator<E> spliterator() {
      return base.spliterator();
    }

    @Override
    public Stream<E> stream() {
      return base.stream();
    }

    @Override
    public Object[] toArray() {
      return base.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
      return base.toArray(a);
    }

    @Override
    public String toString() {
      return base.toString();
    }
  }

  /**
   * Extended mutable list.
   *
   * @author Stefano Chizzolini
   */
  private static class XtArrayList<E> extends ArrayList<E> implements XtList<E> {
    private static final long serialVersionUID = 1L;

    public XtArrayList() {
      super();
    }

    @SuppressWarnings("unused")
    public XtArrayList(Collection<? extends E> c) {
      super(c);
    }

    public XtArrayList(int initialCapacity) {
      super(initialCapacity);
    }
  }

  /**
   * Extended mutable map.
   *
   * @author Stefano Chizzolini
   */
  @SuppressWarnings("null")
  private static class XtHashMap<K, V> extends HashMap<K, V> implements XtMap<K, V> {
    private static final long serialVersionUID = 1L;

    public XtHashMap() {
      super();
    }

    @SuppressWarnings("unused")
    public XtHashMap(Map<? extends K, ? extends V> m) {
      super(m);
    }

    public XtHashMap(int initialCapacity) {
      super(initialCapacity);
    }
  }

  /**
   * Extended mutable set.
   *
   * @author Stefano Chizzolini
   */
  private static class XtHashSet<E> extends HashSet<E> implements XtSet<E> {
    private static final long serialVersionUID = 1L;

    public XtHashSet() {
      super();
    }

    @SuppressWarnings("unused")
    public XtHashSet(Collection<? extends E> c) {
      super(c);
    }

    public XtHashSet(int initialCapacity) {
      super(initialCapacity);
    }
  }

  /**
   * Inserts an array of elements in the given list.
   *
   * @param target
   *          Target list.
   * @param index
   *          Insertion position.
   * @param a
   *          Array of elements to insert.
   * @return Whether the given list changed as a result of the call.
   * @see Collections#addAll( Collection, Object[])
   */
  public static <E> boolean addAll(List<E> target, int index, E[] a) {
    if (a.length == 0)
      return false;

    for (E e : a) {
      target.add(index++, e);
    }
    return true;
  }

  /**
   * Gets the Cartesian product of the given lists.
   *
   * @return Sequential stream.
   */
  public static Stream<List<Object>> cartesianProduct(List<List<?>> lists) {
    return cartesianProduct(lists, 0);
  }

  /**
   * {@linkplain #peek(List, int) Relaxed getter} which, in case of undefined element,
   * {@linkplain #place(List, int, Object) sets} it with the provided one.
   *
   * @param target
   *          Target list.
   * @param index
   *          Index of the element to return.
   * @param provider
   *          Element provider.
   * @return Element at {@code index}, possibly provided by {@code provider} if undefined.
   */
  public static <E> @Nullable E computeIfAbsent(List<E> target, int index,
      Function<Integer, ? extends E> provider) {
    var ret = peek(target, index);
    if (ret == null) {
      ret = provider.apply(index);
      if (ret != null) {
        place(target, index, ret);
      }
    }
    return ret;
  }

  /**
   * Gets whether the given collection contains any of the elements.
   */
  @SafeVarargs
  public static <E> boolean containsAny(Collection<E> c, E... ee) {
    for (E e : ee) {
      if (c.contains(e))
        return true;
    }
    return false;
  }

  /**
   * @see #containsAny(Collection, Object...)
   */
  public static <E> boolean containsAny(Collection<E> c, E e1, E e2) {
    return c.contains(e1)
        || c.contains(e2);
  }

  /**
   * @see #containsAny(Collection, Object...)
   */
  public static <E> boolean containsAny(Collection<E> c, E e1, E e2, E e3) {
    return c.contains(e1)
        || c.contains(e2)
        || c.contains(e3);
  }

  /**
   * Gets whether the given map contains any of the keys.
   *
   * @param <K>
   *          Key type.
   * @param <V>
   *          Value type.
   */
  @SafeVarargs
  public static <K, V> boolean containsAnyKey(Map<K, V> m, K... kk) {
    for (K k : kk) {
      if (m.containsKey(k))
        return true;
    }
    return false;
  }

  /**
   * @see #containsAnyKey(Map, Object...)
   */
  public static <K, V> boolean containsAnyKey(Map<K, V> m, K k1, K k2) {
    return m.containsKey(k1)
        || m.containsKey(k2);
  }

  /**
   * @see #containsAnyKey(Map, Object...)
   */
  public static <K, V> boolean containsAnyKey(Map<K, V> m, K k1, K k2, K k3) {
    return m.containsKey(k1)
        || m.containsKey(k2)
        || m.containsKey(k3);
  }

  /**
   * Counts the elements within an iterable.
   */
  public static <E> int count(Iterable<E> iterable) {
    /*
     * NOTE: Apparently, direct access to `iterable.spliterator().getExactSizeIfKnown()` doesn't
     * work (returns `-1`, whilst encapsulating the same `Spliterator` in a stream returns the
     * correct count!... does it count the actual iterations??), so we have to involve streams,
     * alas.
     */
    return (int) StreamSupport.stream(iterable.spliterator(), false).count();
  }

  /**
   * @return {@code null}, if {@code c} is empty.
   */
  public static <T extends Collection<?>> @Nullable T emptyToNull(@Nullable T c) {
    return isFilled(c) ? c : null;
  }

  /**
   * @return {@code null}, if {@code m} is empty.
   */
  public static <T extends Map<?, ?>> @Nullable T emptyToNull(@Nullable T m) {
    return isFilled(m) ? m : null;
  }

  /**
   * Creates a new entry.
   *
   * @see Map#entry( Object, Object)
   */
  public static <K, V> Map.@Immutable Entry<K, V> entry(@Nullable K key, @Nullable V value) {
    return new AbstractMap.SimpleImmutableEntry<>(key, value);
  }

  /**
   * Gets whether the given iterable objects are equal.
   *
   * @implSpec Intrinsic {@link #equals(Object)} implementation of the given objects is ignored, as
   *           their iterators are evaluated only.
   */
  public static <E> boolean equals(Iterable<E> i1, Iterable<E> i2) {
    if (i1 == i2)
      return true;

    Iterator<E> itr1 = i1.iterator();
    Iterator<E> itr2 = i2.iterator();
    while (itr1.hasNext()) {
      if (!itr2.hasNext() || !Objects.equals(itr1.next(), itr2.next()))
        return false;
    }
    return !itr2.hasNext();
  }

  /**
   * Creates a set backed by a {@link HashSet}, safe for concurrent modifications.
   */
  public static <E> Set<E> failSafeSet() {
    return failSafeSet(new HashSet<>());
  }

  /**
   * Wraps the give map inside a set that is safe for concurrent modifications.
   *
   * @param m
   *          Base map.
   */
  public static <E> Set<E> failSafeSet(Map<E, Boolean> m) {
    return new FailSafeMapBasedSet<>(m);
  }

  /**
   * Wraps the give set inside a set that is safe for concurrent modifications.
   *
   * @param s
   *          Base set.
   */
  public static <E> Set<E> failSafeSet(Set<E> s) {
    return new FailSafeSet<>(s);
  }

  /**
   * Creates a set backed by a {@link WeakHashMap}, safe for concurrent modifications.
   */
  public static <E> Set<E> failSafeWeakSet() {
    return failSafeSet(new WeakHashMap<>());
  }

  /**
   * Performs an action for each element of the given list until all elements have been processed or
   * the action throws an exception (relayed to the caller).
   * <p>
   * The behavior of this method is unspecified if the action performs side effects that modify the
   * underlying source of elements, unless an overriding class has specified a concurrent
   * modification policy.
   * </p>
   *
   * @param action
   *          The action to be performed for each element.
   */
  public static <E> void forEach(List<E> target, ObjIntConsumer<E> action) {
    for (int i = 0, l = target.size(); i < l; i++) {
      action.accept(target.get(i), i);
    }
  }

  /**
   * Gets the key associated to the given value.
   * <p>
   * NOTE: This implementation is the most simple and inefficient, iterating the whole map (O(n)
   * complexity). Use it for occasional calls only.
   * </p>
   */
  public static <K, V> @Nullable K getKey(Map<K, V> map, @Nullable V value) {
    for (Map.Entry<K, V> entry : map.entrySet()) {
      if (Objects.equals(entry.getValue(), value))
        return entry.getKey();
    }
    return null;
  }

  /**
   * Creates a new hash set populated with the given collections.
   */
  @SafeVarargs
  public static <E> HashSet<E> hashSet(@NonNull Collection<E>... cc) {
    var ret = new HashSet<E>();
    for (var c : cc) {
      ret.addAll(c);
    }
    return ret;
  }

  /**
   * Creates a new hash set populated with the given elements.
   */
  @SafeVarargs
  public static <E> HashSet<E> hashSet(E... ee) {
    var ret = new HashSet<E>(ee.length);
    Collections.addAll(ret, ee);
    return ret;
  }

  /**
   * Whether the given collection is empty.
   */
  public static boolean isEmpty(@Nullable Collection<?> c) {
    return c == null || c.isEmpty();
  }

  /**
   * Whether the given map is empty.
   */
  public static boolean isEmpty(@Nullable Map<?, ?> m) {
    return m == null || m.isEmpty();
  }

  /**
   * Whether the given array is empty.
   */
  public static <T> boolean isEmpty(T @Nullable [] a) {
    return a == null || a.length == 0;
  }

  /**
   * Whether the given collection is not empty.
   */
  public static boolean isFilled(@Nullable Collection<?> c) {
    return !isEmpty(c);
  }

  /**
   * Whether the given map is not empty.
   */
  public static boolean isFilled(@Nullable Map<?, ?> m) {
    return !isEmpty(m);
  }

  /**
   * Whether the given array is not empty.
   */
  public static <T> boolean isFilled(T @Nullable [] a) {
    return !isEmpty(a);
  }

  /**
   * Creates a new mutable list.
   */
  public static <E> XtList<E> list() {
    return new XtArrayList<>();
  }

  /**
   * Creates a new mutable list populated with the given elements.
   */
  @SafeVarargs
  public static <E> XtList<E> list(E... ee) {
    var ret = new XtArrayList<E>();
    Collections.addAll(ret, ee);
    return ret;
  }

  /**
   * Creates a new mutable list of the given type.
   */
  public static <E> XtList<E> listOf(Class<E> type) {
    return list();
  }

  /**
   * Creates a new mutable map.
   */
  public static <K, V> XtMap<K, V> map() {
    return new XtHashMap<>();
  }

  /**
   * Creates a new mutable map populated with the given entries.
   */
  @SafeVarargs
  public static <K, V> XtMap<K, V> map(Map.Entry<K, V>... ee) {
    var ret = new XtHashMap<K, V>();
    for (var e : ee) {
      ret.put(e.getKey(), e.getValue());
    }
    return ret;
  }

  /**
   * Creates a new mutable map of the given types.
   */
  public static <K, V> XtMap<K, V> mapOf(Class<K> keyType, Class<V> valueType) {
    return map();
  }

  /**
   * @return Empty, if {@code c} is undefined.
   */
  @SuppressWarnings("unchecked")
  public static <T extends List<?>> @NonNull T nullToEmpty(@Nullable T c) {
    return c != null ? c : (T) Collections.emptyList();
  }

  /**
   * @return Empty, if {@code c} is undefined.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Set<?>> @NonNull T nullToEmpty(@Nullable T c) {
    return c != null ? c : (T) Collections.emptySet();
  }

  /**
   * @return Empty, if {@code m} is undefined.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Map<?, ?>> @NonNull T nullToEmpty(@Nullable T m) {
    return m != null ? m : (T) Collections.emptyMap();
  }

  /**
   * Gets an element in the given list without throwing {@link IndexOutOfBoundsException}.
   *
   * @param target
   *          Target list.
   * @param index
   *          Index of the element to return.
   * @return Element at {@code index}, or {@code null} if {@code target} is undefined or index is
   *         out of bounds.
   */
  public static <E> @Nullable E peek(@Nullable List<E> target, int index) {
    return target != null && index >= 0 && index < target.size() ? target.get(index) : null;
  }

  /**
   * Sets an element in the given list at an unrestricted position.
   * <p>
   * If {@code index} is below the lower- or above the upper-bound, this method makes room to the
   * new element accordingly (possibly inserting {@code null} elements in the intermediate
   * positions), instead of throwing {@link IndexOutOfBoundsException} — think the list as a limited
   * view on an infinite sequence whose external elements are all {@code null}.
   * </p>
   *
   * @param target
   *          Target list.
   * @param index
   *          Index of the element to replace.
   * @param e
   *          New element to be stored at {@code index}.
   * @return Element previously at {@code index}.
   */
  public static <E> E place(List<E> target, int index, E e) {
    if (index < 0) {
      size(target, target.size() - index, true);
      index = 0;
    } else if (index >= target.size()) {
      size(target, index + 1, false);
    }
    return target.set(index, e);
  }

  /**
   * Removes an element in the given list without throwing {@link IndexOutOfBoundsException}.
   * <p>
   * If {@code index} is out of bounds, nothing happens.
   * </p>
   *
   * @param index
   *          Index of the element to remove.
   * @return Removed element; {@code null}, if {@code target} is undefined or {@code index} is out
   *         of bounds.
   */
  public static <E> @Nullable E poll(@Nullable List<E> target, int index) {
    return target != null && index >= 0 && index < target.size() ? target.remove(index) : null;
  }

  /**
   * Creates a new mutable set.
   */
  public static <E> XtSet<E> set() {
    return new XtHashSet<>();
  }

  /**
   * Creates a new mutable set.
   */
  public static <E> XtSet<E> set(Class<E> elementType) {
    return new XtHashSet<>();
  }

  /**
   * Creates a new mutable set populated with the given elements.
   */
  @SafeVarargs
  public static <E> XtSet<E> set(E... ee) {
    var ret = new XtHashSet<E>(ee.length);
    Collections.addAll(ret, ee);
    return ret;
  }

  /**
   * Sets the {@link List#size() size} of the given list, shrinking or expanding it with the given
   * element.
   *
   * @param target
   *          Target list.
   * @param value
   *          New size.
   * @param lowerPadding
   *          Whether, on shrink, existing elements are removed from the beginning rather than the
   *          end of the list, and, on expansion, {@code element}s are inserted at the beginning
   *          rather than appended at the end of the list.
   * @param element
   *          Filling element.
   * @return {@code target}.
   */
  public static <E> List<@Nullable E> size(List<@Nullable E> target, int value,
      boolean lowerPadding, @Nullable E element) {
    int size = target.size();
    while (value > size) {
      if (lowerPadding) {
        target.add(0, element);
      } else {
        target.add(element);
      }
      size++;
    }
    while (value < size) {
      size--;
      if (lowerPadding) {
        target.remove(0);
      } else {
        target.remove(size);
      }
    }
    return target;
  }

  /**
   * Sets the {@link List#size() size} of the given list, shrinking or expanding it with
   * {@code null} elements.
   *
   * @param target
   *          Target list.
   * @param value
   *          New size.
   * @return {@code target}.
   */
  public static <E> List<E> size(List<E> target, int value) {
    return size(target, value, false);
  }

  /**
   * Sets the {@link List#size() size} of the given list, shrinking or expanding it with
   * {@code null} elements.
   *
   * @param target
   *          Target list.
   * @param value
   *          New size.
   * @param lowerPadding
   *          Whether, on shrink, existing elements are removed from the beginning rather than the
   *          end of the list, and, on expansion, {@code null} elements are inserted at the
   *          beginning rather than appended at the end of the list.
   * @return {@code target}.
   */
  public static <E> List<E> size(List<E> target, int value, boolean lowerPadding) {
    return size(target, value, lowerPadding, null);
  }

  /**
   * Converts the given iterator into a stream.
   */
  public static <E> Stream<E> stream(Iterator<E> iterator) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
  }

  /**
   * Trims the given list of its trailing {@code null} elements.
   *
   * @param target
   *          Target list.
   * @return {@code target}.
   */
  public static <@Nullable E> List<E> trim(List<E> target) {
    int i = target.size();
    while (i-- > 0 && target.get(i) == null) {
      target.remove(i);
    }
    return target;
  }

  /**
   * Traverses depth-first the given collection of tree nodes.
   *
   * @param <T>
   *          Node type.
   * @param c
   *          Node children.
   * @param childrenGetter
   *          Returns the children of the provided node.
   * @param nodeEvaluator
   *          Returns whether the search is complete and consequently the traversal must end;
   *          otherwise, the traversal continues.
   */
  public static <T> boolean walk(Collection<? extends T> c,
      Function<T, @Nullable Collection<? extends T>> childrenGetter, Predicate<T> nodeEvaluator) {
    for (var e : c) {
      if (nodeEvaluator.test(e))
        return true;

      var children = childrenGetter.apply(e);
      if (children != null) {
        if (walk(children, childrenGetter, nodeEvaluator))
          return true;
      }
    }
    return false;
  }

  private static Stream<List<Object>> cartesianProduct(List<List<?>> lists, int index) {
    if (index == lists.size())
      return Stream.of(new ArrayList<>());

    return lists.get(index).stream()
        .flatMap($ -> cartesianProduct(lists, index + 1)
            .map($$ -> {
              var newList = new ArrayList<>($$);
              newList.add(0, $);
              return newList;
            }));
  }

  private Aggregations() {
  }
}
