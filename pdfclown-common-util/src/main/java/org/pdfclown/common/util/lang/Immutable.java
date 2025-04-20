/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (Immutable.java) is part of pdfclown-common-util module in pdfClown Common project (this
  Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.util.lang;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated type is
 * <a href="https://en.wikipedia.org/wiki/Immutable_object">strongly immutable</a>.
 * <h2>Requirements</h2>
 * <ul>
 * <li>class:
 * <ul>
 * <li>the annotated class is {@code final}</li>
 * <li>the annotated class complies with the requirements of {@linkplain WeakImmutable weak
 * immutability}</li>
 * </ul>
 * </li>
 * <li>interface:
 * <ul>
 * <li>the annotated interface has no member with state-altering semantics (setters or other methods
 * with side effects); any semantic detail delegated by parent interfaces must be settled to
 * immutability</li>
 * <li>all the parent interfaces of the annotated interface are themselves immutable (or weakly
 * immutable, if limited to semantic details delegated to derived types)</li>
 * </ul>
 * <p>
 * NOTE: Strong immutability at interface level is obviously limited to contract: it is
 * implementers' responsibility to honor its semantics (and annotate derived types accordingly).
 * </p>
 * </li>
 * </ul>
 *
 * @author Stefano Chizzolini
 * @implNote Because of the <a href="https://github.com/google/guava/issues/2960">notorious mess</a>
 *           around defunct JSR 305, to avoid bloating dependencies with yet another idiosyncratic
 *           sub-standard set of annotations, the minimalist set in this package is meant to
 *           temporarily substitute them for documentation purposes, until the industry settles on a
 *           clean common standard.
 */
@Documented
@Retention(CLASS)
@Target({ TYPE, TYPE_USE })
public @interface Immutable {
}
