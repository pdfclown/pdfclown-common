/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Tuple.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: Copyright 2003-2026 The Apache Software Foundation

  SPDX-License-Identifier: Apache-2.0

  Source: https://github.com/apache/groovy/blob/7831e9cf9e66f20c1b7fdd8f7dfff480db7cbe18/src/main/java/groovy/lang/Tuple.java

  Changes:
  - factory methods reduced to 9 elements maximum
  - constructor visibility reduced to package
  - equality comparison simplified
 */
package org.pdfclown.common.build.util;

import static java.util.Objects.requireNonNull;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Unmodifiable;

/**
 * Tuple.
 *
 * @param <E>
 *          Element type.
 * @author Stefano Chizzolini (adaptation to pdfclown-common-build)
 */
@Unmodifiable
public class Tuple<E extends @Nullable Object> extends AbstractList<E>
    implements Comparable<Tuple<E>>, RandomAccess {
  @SafeVarargs
  @SuppressWarnings("varargs")
  public static <E extends @Nullable Object> Tuple<E> tuple(E... elements) {
    return new Tuple<>(elements);
  }

  public static <E1 extends @Nullable Object, E2 extends @Nullable Object> //
      Tuple2<E1, E2> tuple(E1 e1, E2 e2) {
    return new Tuple2<>(e1, e2);
  }

  public static <E1 extends @Nullable Object, E2 extends @Nullable Object,
      E3 extends @Nullable Object> //
      Tuple3<E1, E2, E3> tuple(E1 e1, E2 e2, E3 e3) {
    return new Tuple3<>(e1, e2, e3);
  }

  public static <E1 extends @Nullable Object, E2 extends @Nullable Object,
      E3 extends @Nullable Object, E4 extends @Nullable Object> //
      Tuple4<E1, E2, E3, E4> tuple(E1 e1, E2 e2, E3 e3, E4 e4) {
    return new Tuple4<>(e1, e2, e3, e4);
  }

  public static <E1 extends @Nullable Object, E2 extends @Nullable Object,
      E3 extends @Nullable Object, E4 extends @Nullable Object, E5 extends @Nullable Object> //
      Tuple5<E1, E2, E3, E4, E5> tuple(E1 e1, E2 e2, E3 e3, E4 e4, E5 e5) {
    return new Tuple5<>(e1, e2, e3, e4, e5);
  }

  public static <E1 extends @Nullable Object, E2 extends @Nullable Object,
      E3 extends @Nullable Object, E4 extends @Nullable Object, E5 extends @Nullable Object,
      E6 extends @Nullable Object> //
      Tuple6<E1, E2, E3, E4, E5, E6> tuple(E1 e1, E2 e2, E3 e3, E4 e4, E5 e5, E6 e6) {
    return new Tuple6<>(e1, e2, e3, e4, e5, e6);
  }

  public static <E1 extends @Nullable Object, E2 extends @Nullable Object,
      E3 extends @Nullable Object, E4 extends @Nullable Object, E5 extends @Nullable Object,
      E6 extends @Nullable Object, E7 extends @Nullable Object> //
      Tuple7<E1, E2, E3, E4, E5, E6, E7> tuple(E1 e1, E2 e2, E3 e3, E4 e4, E5 e5, E6 e6, E7 e7) {
    return new Tuple7<>(e1, e2, e3, e4, e5, e6, e7);
  }

  public static <E1 extends @Nullable Object, E2 extends @Nullable Object,
      E3 extends @Nullable Object, E4 extends @Nullable Object, E5 extends @Nullable Object,
      E6 extends @Nullable Object, E7 extends @Nullable Object, E8 extends @Nullable Object> //
      Tuple8<E1, E2, E3, E4, E5, E6, E7, E8> tuple(E1 e1, E2 e2, E3 e3, E4 e4, E5 e5, E6 e6, E7 e7,
          E8 e8) {
    return new Tuple8<>(e1, e2, e3, e4, e5, e6, e7, e8);
  }

  public static <E1 extends @Nullable Object, E2 extends @Nullable Object,
      E3 extends @Nullable Object, E4 extends @Nullable Object, E5 extends @Nullable Object,
      E6 extends @Nullable Object, E7 extends @Nullable Object, E8 extends @Nullable Object,
      E9 extends @Nullable Object> //
      Tuple9<E1, E2, E3, E4, E5, E6, E7, E8, E9> tuple(E1 e1, E2 e2, E3 e3, E4 e4, E5 e5, E6 e6,
          E7 e7, E8 e8, E9 e9) {
    return new Tuple9<>(e1, e2, e3, e4, e5, e6, e7, e8, e9);
  }

  @SuppressWarnings("unchecked")
  private static <T extends @Nullable Object> int compare(T t1, T t2) {
    return t1 == null && t2 == null ? 0
        : t1 == null ? 1
        : t2 == null ? -1
        : t1 instanceof Comparable ? ((Comparable<T>) t1).compareTo(t2)
        : Objects.equals(t1, t2) ? 0 : 1;
  }

  private final E[] elements;

  @SafeVarargs
  @SuppressWarnings("varargs")
  Tuple(E... elements) {
    this.elements = requireNonNull(elements, "`elements`");
  }

  @Override
  public int compareTo(Tuple<E> other) {
    int thisSize = this.size();
    int otherSize = other.size();
    int i = 0;

    for (int n = Math.min(thisSize, otherSize); i < n; ++i) {
      int result = compare(this.get(i), other.get(i));
      if (result != 0) {
        return result;
      }
    }

    return Integer.compare(thisSize, otherSize);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Tuple)) {
      return false;
    } else {
      var that = (Tuple<E>) o;
      int size = this.size();
      if (size != that.size()) {
        return false;
      } else {
        for (int i = 0; i < size; ++i) {
          if (!Objects.equals(this.get(i), that.get(i)))
            return false;
        }
        return true;
      }
    }
  }

  @Override
  public E get(int index) {
    return elements[index];
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(elements);
  }

  @Override
  public int size() {
    return elements.length;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<E> subList(int fromIndex, int toIndex) {
    int size = toIndex - fromIndex;
    E[] newContent = (E[]) (new Object[size]);
    System.arraycopy(elements, fromIndex, newContent, 0, size);
    return new Tuple<>(newContent);
  }

  /**
   * @see #subList(int, int)
   */
  public Tuple<E> subTuple(int fromIndex, int toIndex) {
    return (Tuple<E>) subList(fromIndex, toIndex);
  }

  @Override
  @SuppressWarnings("unchecked")
  public E[] toArray() {
    int size = size();
    E[] copy = (E[]) (new Object[size]);
    System.arraycopy(elements, 0, copy, 0, size);
    return copy;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a) {
    int size = size();
    if (a.length < size) {
      a = (T[]) (new Object[size]);
    }
    System.arraycopy(elements, 0, a, 0, size);
    Arrays.fill(a, size, a.length, null);
    return a;
  }
}
