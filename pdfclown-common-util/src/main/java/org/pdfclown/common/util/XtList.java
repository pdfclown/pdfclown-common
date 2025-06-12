/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (XtList.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import org.jspecify.annotations.Nullable;

/**
 * Extended list.
 *
 * @param <E>
 *          Element type.
 * @author Stefano Chizzolini
 */
public interface XtList<E> extends List<E>, XtCollection<E> {
  @Override
  default boolean add(E e) {
    add(size(), e);
    return true;
  }

  @Override
  default boolean addAll(Collection<? extends E> c) {
    return addAll(size(), c);
  }

  @Override
  default boolean addAll(int index, Collection<? extends E> c) {
    for (E e : c) {
      add(index++, e);
    }
    return !c.isEmpty();
  }

  /**
   * {@linkplain #peek(int) Relaxed getter} which, in case of undefined element,
   * {@linkplain #place(int, Object) sets} it with the provided one.
   *
   * @param index
   *          Index of the element to return.
   * @param provider
   *          Element provider.
   * @return Element at {@code index}, possibly provided by {@code provider} if undefined.
   */
  default @Nullable E computeIfAbsent(int index, Function<Integer, ? extends E> provider) {
    return Aggregations.computeIfAbsent(this, index, provider);
  }

  /**
   * Performs the given action for each element of this list until all elements have been processed
   * or the action throws an exception (relayed to the caller).
   * <p>
   * The behavior of this method is unspecified if the action performs side effects that modify the
   * underlying source of elements, unless an overriding class has specified a concurrent
   * modification policy.
   * </p>
   *
   * @param action
   *          The action to be performed for each element.
   */
  default void forEach(ObjIntConsumer<E> action) {
    Aggregations.forEach(this, action);
  }

  /**
   * First element.
   *
   * @throws IndexOutOfBoundsException
   *           if this list is empty.
   * @see Deque#getFirst()
   */
  default E getFirst() {
    return get(0);
  }

  /**
   * Last element.
   *
   * @throws IndexOutOfBoundsException
   *           if this list is empty.
   * @see Deque#getLast()
   */
  default E getLast() {
    return get(size() - 1);
  }

  @Override
  default boolean isEmpty() {
    return XtCollection.super.isEmpty();
  }

  /**
   * Relaxed {@link #get(int) get(..)} (doesn't throw {@link IndexOutOfBoundsException}).
   *
   * @param index
   *          Index of the element to return.
   * @return Element at {@code index}, or {@code null}, if index is out of bounds.
   */
  default @Nullable E peek(int index) {
    return Aggregations.peek(this, index);
  }

  /**
   * Relaxed {@link #getFirst()} (doesn't throw {@link IndexOutOfBoundsException}).
   *
   * @return {@code null}, if this list is empty.
   * @see Deque#peekFirst()
   */
  default @Nullable E peekFirst() {
    return isFilled() ? getFirst() : null;
  }

  /**
   * Relaxed {@link #getLast()} (doesn't throw {@link IndexOutOfBoundsException}).
   *
   * @return {@code null}, if this list is empty.
   * @see Deque#peekLast()
   */
  default @Nullable E peekLast() {
    return isFilled() ? getLast() : null;
  }

  /**
   * Relaxed {@link List#set(int, Object) set(..)}.
   * <p>
   * If {@code index} is below the lower or above the upper bound, this method makes room to the new
   * element accordingly (possibly inserting {@code null} elements in the intermediate positions),
   * instead of throwing {@link IndexOutOfBoundsException} — think the list as a bounded view on a
   * boundless sequence whose external elements are all {@code null}.
   * </p>
   *
   * @param index
   *          Index of the element to replace.
   * @param e
   *          New element to be stored at {@code index}.
   * @return Replaced element.
   */
  default E place(int index, E e) {
    return Aggregations.place(this, index, e);
  }

  /**
   * Relaxed {@link #remove(int) remove(..)}.
   * <p>
   * If {@code index} is out of bounds, nothing happens.
   * </p>
   *
   * @param index
   *          Index of the element to remove.
   * @return Removed element, or {@code null}, if {@code index} is out of bounds.
   */
  default @Nullable E poll(int index) {
    return Aggregations.poll(this, index);
  }

  /**
   * Relaxed {@link #removeFirst()} (doesn't throw {@link IndexOutOfBoundsException}).
   *
   * @return {@code null}, if this list is empty.
   * @see Deque#pollFirst()
   */
  default @Nullable E pollFirst() {
    return isFilled() ? removeFirst() : null;
  }

  /**
   * Relaxed {@link #removeLast()} (doesn't throw {@link IndexOutOfBoundsException}).
   *
   * @return {@code null}, if this list is empty.
   * @see Deque#pollLast()
   */
  default @Nullable E pollLast() {
    return isFilled() ? removeLast() : null;
  }

  @Override
  default boolean remove(@Nullable Object o) {
    int index = indexOf(o);
    if (index < 0)
      return false;

    remove(index);
    return true;
  }

  @Override
  default boolean removeAll(Collection<?> c) {
    return XtCollection.super.removeAll(c);
  }

  /**
   * Removes the first element.
   *
   * @return Removed element.
   * @throws IndexOutOfBoundsException
   *           if this list is empty.
   * @see Deque#removeFirst()
   */
  default E removeFirst() {
    return remove(0);
  }

  /**
   * Removes the last element.
   *
   * @return Removed element.
   * @throws IndexOutOfBoundsException
   *           if this list is empty.
   * @see Deque#removeLast()
   */
  default E removeLast() {
    return remove(size() - 1);
  }

  @Override
  default XtList<E> with(E e) {
    return (XtList<E>) XtCollection.super.with(e);
  }

  /**
   * Fluent {@link List#add(int, Object) add(..)}.
   *
   * @return This object.
   */
  default XtList<E> with(int index, E e) {
    add(index, e);
    return this;
  }

  @Override
  default XtList<E> withAll(Collection<? extends E> c) {
    return (XtList<E>) XtCollection.super.withAll(c);
  }

  /**
   * Fluent {@link List#set(int, Object) set(..)}.
   *
   * @return This object.
   */
  default XtList<E> withElse(int index, E e) {
    set(index, e);
    return this;
  }

  /**
   * Fluent {@link #place(int, Object) place(..)}.
   *
   * @return This object.
   */
  default XtList<E> withElseSafe(int index, E e) {
    place(index, e);
    return this;
  }

  @Override
  default XtList<E> without(E e) {
    return (XtList<E>) XtCollection.super.without(e);
  }

  /**
   * Fluent {@link List#remove(int) remove(..)}.
   *
   * @return This object.
   */
  default XtList<E> without(int index) {
    remove(index);
    return this;
  }

  @Override
  default XtList<E> withoutAll(Collection<?> c) {
    return (XtList<E>) XtCollection.super.withoutAll(c);
  }

  /**
   * Fluent {@link #poll(int) poll(..)}.
   *
   * @return This object.
   */
  default XtList<E> withoutSafe(int index) {
    poll(index);
    return this;
  }
}
