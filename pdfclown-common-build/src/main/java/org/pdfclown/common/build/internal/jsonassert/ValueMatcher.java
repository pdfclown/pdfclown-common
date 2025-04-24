/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (ValueMatcher.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.build.internal.jsonassert;

/**
 * Represents a value matcher that can compare two objects for equality.
 *
 * @param <T>
 *          the object type to compare
 */
public interface ValueMatcher<T> {
  /**
   * Compares the two provided objects whether they are equal.
   *
   * @param o1
   *          the first object to check
   * @param o2
   *          the object to check the first against
   * @return true if the objects are equal, false otherwise
   */
  boolean equal(T o1, T o2);
}
