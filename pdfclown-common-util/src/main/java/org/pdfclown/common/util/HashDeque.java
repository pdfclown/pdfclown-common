/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (HashDeque.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.TODO;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

/**
 * A deque which ensures element uniqueness, possibly extended across the whole life of the deque
 * (ie, once an element is removed, it cannot be reinserted anymore).
 *
 * @param <E>
 *          Element type.
 * @author Stefano Chizzolini
 */
public class HashDeque<E> extends ArrayDeque<E> {
  private static final long serialVersionUID = 1L;

  private final Set<E> base = new HashSet<>();

  private final boolean tracked;

  /**
   */
  public HashDeque(boolean tracked) {
    this.tracked = tracked;
  }

  /**
   */
  public HashDeque(boolean tracked, Collection<? extends E> c) {
    this(tracked);

    addAll(c);
  }

  @Override
  public boolean add(E e) {
    if (!base.add(e))
      return false;

    super.addLast(e);
    return true;
  }

  @Override
  public final boolean addAll(Collection<? extends E> c) {
    int oldSize = size();
    for (var e : c) {
      add(e);
    }
    return size() != oldSize;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Insertion occurs only if {@code e} has not already inserted.
   * </p>
   */
  @Override
  public final void addFirst(E e) {
    offerFirst(e);
  }

  @Override
  public final void addLast(E e) {
    add(e);
  }

  @Override
  public final void clear() {
    clear(!tracked);
  }

  /**
   * Removes all the elements from this deque.
   *
   * @param reset
   *          Whether element tracking is reset.
   */
  public void clear(boolean reset) {
    super.clear();
    if (reset) {
      base.clear();
    }
  }

  @Override
  public Iterator<E> descendingIterator() {
    throw TODO();
  }

  /**
   * Whether elements are tracked after they are removed, making their insertion unique across the
   * whole life of this instance (or until {@linkplain #clear(boolean) reset}); otherwise, they are
   * forgotten as soon as they are removed, making them unique only as long as they are contained in
   * this deque, like a {@link Set}.
   */
  public boolean isTracked() {
    return tracked;
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<>() {
      final Iterator<E> base = HashDeque.super.iterator();
      E next;

      @Override
      public boolean hasNext() {
        return base.hasNext();
      }

      @Override
      public E next() {
        return next = base.next();
      }

      @Override
      public void remove() {
        Iterator.super.remove();
        if (!tracked) {
          HashDeque.this.base.remove(next);
        }
      }
    };
  }

  @Override
  public final boolean offer(E e) {
    return super.offer(e);
  }

  @Override
  public boolean offerFirst(E e) {
    if (!base.add(e))
      return false;

    super.addFirst(e);
    return true;
  }

  @Override
  public final boolean offerLast(E e) {
    return add(e);
  }

  @Override
  public final E poll() {
    return super.poll();
  }

  @Override
  public @Nullable E pollFirst() {
    var ret = super.pollFirst();
    if (!tracked) {
      base.remove(ret);
    }
    return ret;
  }

  @Override
  public @Nullable E pollLast() {
    var ret = super.pollLast();
    if (!tracked) {
      base.remove(ret);
    }
    return ret;
  }

  @Override
  public final E pop() {
    return super.pop();
  }

  @Override
  public final void push(E e) {
    super.push(e);
  }

  @Override
  public final E remove() {
    return super.remove();
  }

  @Override
  public final boolean remove(@Nullable Object o) {
    return super.remove(o);
  }

  @Override
  public final boolean removeAll(Collection<?> c) {
    int oldSize = size();
    for (var o : c) {
      remove(o);
    }
    return size() != oldSize;
  }

  @Override
  public final E removeFirst() {
    return super.removeFirst();
  }

  @Override
  public boolean removeFirstOccurrence(@Nullable Object o) {
    var ret = super.removeFirstOccurrence(o);
    if (ret && !tracked) {
      //noinspection SuspiciousMethodCalls
      base.remove(o);
    }
    return ret;
  }

  @Override
  public final boolean removeIf(Predicate<? super E> filter) {
    int oldSize = size();
    /*
     * NOTE: Cannot delegate to super.removeIf(..) as we have to track removal via custom iterator.
     */
    //noinspection Java8CollectionRemoveIf
    for (var itr = iterator(); itr.hasNext();) {
      var e = itr.next();
      if (filter.test(e)) {
        itr.remove();
      }
    }
    return size() != oldSize;
  }

  @Override
  public final E removeLast() {
    return super.removeLast();
  }

  /**
   * @implNote By definition, this deque contains only unique occurrences, so this method is
   *           equivalent to {@link #removeFirstOccurrence(Object)}.
   */
  @Override
  public final boolean removeLastOccurrence(@Nullable Object o) {
    return removeFirstOccurrence(o);
  }

  @Override
  public final boolean retainAll(Collection<?> c) {
    requireNonNull(c);
    return removeIf($ -> !c.contains($));
  }

  @Override
  public Spliterator<E> spliterator() {
    throw TODO();
  }
}
