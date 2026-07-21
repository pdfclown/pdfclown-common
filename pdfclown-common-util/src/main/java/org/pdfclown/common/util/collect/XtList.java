/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (XtList.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.collect;

import static org.pdfclown.common.util.Exceptions.missing;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
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
public interface XtList<E extends @Nullable Object> extends List<E>, XtCollection<E> {
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

  @Override
  default XtList<E> and(E e) {
    return (XtList<E>) XtCollection.super.and(e);
  }

  /**
   * Fluent {@link #add(int, Object) add}.
   *
   * @return Self.
   */
  default XtList<E> and(int index, E e) {
    add(index, e);
    return this;
  }

  @Override
  default XtList<E> andAll(Collection<? extends E> c) {
    return (XtList<E>) XtCollection.super.andAll(c);
  }

  /**
   * Fluent {@link #place(int, Object) place}.
   *
   * @return Self.
   */
  default XtList<E> anyway(int index, E e) {
    place(index, e);
    return this;
  }

  @Override
  default XtList<E> but(E e) {
    return (XtList<E>) XtCollection.super.but(e);
  }

  /**
   * Fluent {@link #remove(int) remove}.
   *
   * @return Self.
   */
  default XtList<E> but(int index) {
    remove(index);
    return this;
  }

  @Override
  default XtList<E> butAll(Collection<?> c) {
    return (XtList<E>) XtCollection.super.butAll(c);
  }

  /**
   * Fluent {@link #removeFirst() removeFirst}.
   *
   * @return Self.
   */
  default XtList<E> butFirst() {
    removeFirst();
    return this;
  }

  /**
   * Fluent {@link #removeLast() removeLast}.
   *
   * @return Self.
   */
  default XtList<E> butLast() {
    removeLast();
    return this;
  }

  /**
   * Fluent {@link #poll(int) poll}.
   *
   * @return Self.
   */
  default XtList<E> butMaybe(int index) {
    poll(index);
    return this;
  }

  /**
   * {@linkplain #peek(int) Safe getter} which, in case of undefined element,
   * {@linkplain #place(int, Object) sets} it with the provided one.
   *
   * @return Element at {@code index}, possibly provided by {@code provider} if undefined.
   */
  default E computeIfAbsent(int index, Function<Integer, ? extends E> provider) {
    return Collectives.computeIfAbsent(this, index, provider);
  }

  /**
   * Replaces all the elements of this list with the given one.
   */
  default XtList<E> fill(E e) {
    Collections.fill(this, e);
    return this;
  }

  /**
   * Performs the action for each element of this list until all elements have been processed or the
   * action throws an exception (relayed to the caller).
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
    Collectives.forEach(this, action);
  }

  /**
   * First element.
   *
   * @throws NoSuchElementException
   *           if this list is empty.
   */
  default E getFirst() {
    if (isEmpty())
      throw missing();

    return get(0);
  }

  /**
   * Last element.
   *
   * @throws NoSuchElementException
   *           if this list is empty.
   */
  default E getLast() {
    if (isEmpty())
      throw missing();

    return get(size() - 1);
  }

  /**
   * Fluent {@link #set(int, Object) set}.
   *
   * @return Self.
   */
  default XtList<E> instead(int index, E e) {
    set(index, e);
    return this;
  }

  @Override
  default boolean isEmpty() {
    return XtCollection.super.isEmpty();
  }

  @Override
  default XtList<E> none() {
    return (XtList<E>) XtCollection.super.none();
  }

  /**
   * Safe {@link #get(int) get}.
   *
   * @param index
   *          Index of the element to return.
   * @return Element at {@code index}, or {@code null}, if index is out of bounds.
   */
  default @Nullable E peek(int index) {
    return Collectives.peek(this, index);
  }

  /**
   * Safe {@link #getFirst() getFirst}.
   *
   * @return {@code null}, if this list is empty.
   */
  default @Nullable E peekFirst() {
    return isEmpty() ? null : getFirst();
  }

  /**
   * Safe {@link #getLast() getLast}.
   *
   * @return {@code null}, if this list is empty.
   */
  default @Nullable E peekLast() {
    return isEmpty() ? null : getLast();
  }

  /**
   * Sets an element in the list at an unrestricted position.
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
   * @see #set(int, Object)
   */
  default @Nullable E place(int index, E e) {
    return Collectives.place(this, index, e);
  }

  /**
   * Safe {@link #remove(int) remove}.
   *
   * @param index
   *          Index of the element to remove.
   * @return Removed element, or {@code null}, if {@code index} is out of bounds.
   */
  default @Nullable E poll(int index) {
    return Collectives.poll(this, index);
  }

  /**
   * Safe {@link #removeFirst() removeFirst}.
   *
   * @return Removed element, or {@code null}, if this list is empty.
   */
  default @Nullable E pollFirst() {
    return isEmpty() ? null : removeFirst();
  }

  /**
   * Safe {@link #removeLast() removeLast}.
   *
   * @return Removed element, or {@code null}, if this list is empty.
   */
  default @Nullable E pollLast() {
    return isEmpty() ? null : removeLast();
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
   * @throws NoSuchElementException
   *           if this list is empty.
   */
  default E removeFirst() {
    if (isEmpty())
      throw missing();

    return remove(0);
  }

  /**
   * Removes the last element.
   *
   * @return Removed element.
   * @throws NoSuchElementException
   *           if this list is empty.
   */
  default E removeLast() {
    if (isEmpty())
      throw missing();

    return remove(size() - 1);
  }

  /**
   * Sets the {@link List#size() size} of this list, shrinking or expanding it with the given
   * element.
   *
   * @param value
   *          New size.
   * @param e
   *          Filling element.
   * @return Self.
   */
  default XtList<E> size(int value, E e) {
    return Collectives.size(this, value, e);
  }
}
