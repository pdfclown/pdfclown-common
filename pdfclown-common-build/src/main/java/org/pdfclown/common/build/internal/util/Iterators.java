/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (Iterators.java) is part of pdfclown-common-build module in pdfClown Common project
  (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
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
      Iterator<Class> base;
      {
        Set<Class> superTypes = new TreeSet<>(comparator);
        Class superType = type;
        while ((superType = superType.getSuperclass()) != null
            && collect(superType, superTypes, stoppers, stopperExclusive)) {
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
       * @param type
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
