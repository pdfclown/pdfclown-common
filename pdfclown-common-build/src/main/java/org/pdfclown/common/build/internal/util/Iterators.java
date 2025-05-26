/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Iterators.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Iteration utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Iterators {
  /**
   * Creates an iterator of the ancestors hierarchy of the given type.
   * <p>
   * The ancestors are ordered according to the given comparator.
   * </p>
   */
  @SuppressWarnings("rawtypes")
  public static Iterator<Class> ancestors(final Class type, final Comparator<Class> comparator) {
    return ancestors(type, comparator, Set.of(), false);
  }

  /**
   * Creates an iterator of the ancestors hierarchy of the given type.
   * <p>
   * The ancestors are ordered according to the given comparator.
   * </p>
   *
   * @param stoppers
   *          Types at which to stop ancestor hierarchy traversal.
   * @param stopperExclusive
   *          Whether stopped types are excluded from iterated ancestors.
   */
  @SuppressWarnings("rawtypes")
  public static Iterator<Class> ancestors(final Class type, final Comparator<Class> comparator,
      Set<Class> stoppers, boolean stopperExclusive) {
    return new Iterator<>() {
      final Iterator<Class> base;
      {
        Set<Class> superTypes = new TreeSet<>(comparator);
        Class superType = type;
        //noinspection StatementWithEmptyBody
        while ((superType = superType.getSuperclass()) != null
            && collect(superType, superTypes, stoppers, stopperExclusive)) {
          // NOOP
        }
        base = superTypes.iterator();
      }

      @Override
      public boolean hasNext() {
        return base.hasNext();
      }

      /**
       * @return Its super type, then each of its compatible, directly implemented interface.
       */
      @Override
      @SuppressWarnings("null")
      public Class next() {
        return base.next();
      }

      /**
       * Recursively collects the given type and its interfaces until stopped.
       * <p>
       * If {@code type} is contained in {@code stoppers}, this operation stops; in such case,
       * {@code type} is collected only if not {@code stopperExclusive}, while its interfaces are
       * ignored.
       * </p>
       *
       * @param superTypes
       *          Target collection.
       * @param stoppers
       *          Types at which to stop ancestor hierarchy traversal.
       * @param stopperExclusive
       *          Whether stopped types are excluded from iterated ancestors.
       * @return Whether this operation stopped.
       */
      private boolean collect(Class type, Set<Class> superTypes, Set<Class> stoppers,
          boolean stopperExclusive) {
        boolean ret = !stoppers.contains(type);
        if ((ret || !stopperExclusive) && superTypes.add(type)) {
          if (ret) {
            for (var e : type.getInterfaces()) {
              collect(e, superTypes, stoppers, stopperExclusive);
            }
          }
        }
        return ret;
      }
    };
  }

  private Iterators() {
  }
}
