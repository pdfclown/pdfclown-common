/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Version.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.meta;

/*
  TODO: after next release (0.4.0), remove ALL version classes from org.pdfclown.common.build.meta
        and shade them from pdfclown-common-util.
 */
/**
 * <a href="https://en.wikipedia.org/wiki/Software_versioning">Software version</a>.
 *
 * @param <T>
 *          Version type.
 * @author Stefano Chizzolini
 * @implSpec This interface avoids deliberately to extend {@link Comparable} in order to be
 *           applicable also to classes whose implementation is constrained, like {@linkplain Enum
 *           enumerations}; therefore, it is implementers' responsibility to support
 *           {@link Comparable} wherever appropriate.
 */
public interface Version<T extends Version<T>> {
  /**
   * Whether this version represents a normal release, rather than a pre-release.
   */
  boolean isRegular();

  /**
   * Compares this version with the given one for precedence.
   * <p>
   * NOTE: Differently from {@link Comparable#compareTo(Object)}, this method doesn't produce a
   * strict order, as versioning semantics may ignore certain version parts, causing non-equivalent
   * versions to have the same precedence.
   * </p>
   */
  int precedence(T o);
}
