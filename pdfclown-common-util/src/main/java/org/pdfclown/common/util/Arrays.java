/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Arrays.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Array utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Arrays {
  /**
   * Returns an array of the given elements.
   *
   * @apiNote This is just syntactic sugar to spare users from explicitly instantiating a reference
   *          array (for example, <code>arr(MyClass.class)</code> instead of
   *          <code>new Class[] { MyClass.class }</code>).
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public static <T> T[] arr(T... ee) {
    return ee;
  }

  /**
   * Creates a new array from the given iterator.
   */
  public static Object[] from(Iterator<?> itr) {
    return from(itr, (Object[]) Array.newInstance(Object.class, 0));
  }

  /**
   * Creates a new array from the given iterator, like {@link Collection#toArray(Object[])}.
   *
   * @param a
   *          Target array.
   */
  public static <E, T> T[] from(Iterator<E> itr, T[] a) {
    var l = new ArrayList<E>();
    itr.forEachRemaining(l::add);
    return l.toArray(a);
  }

  private Arrays() {
  }
}
