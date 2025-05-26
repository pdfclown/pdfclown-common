/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Immutable.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.lang;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated type is immutable.
 * <p>
 * Immutability is about the <i>stability of the object state, referenced objects inclusive</i>; it
 * is stricter than {@linkplain ReadOnly view unmodifiability} and {@linkplain Unmodifiable
 * unmodifiability}.
 * </p>
 * <h2>Requirements</h2>
 * <ul>
 * <li>class:
 * <ul>
 * <li>the annotated class has only unmodifiable fields, whose types are themselves effectively
 * immutable (eg, defensive copy of arrays and other mutable objects)</li>
 * <li>the annotated class is {@code final} (<b>strong immutability</b>), or not (<b>weak
 * immutability</b>)</li>
 * </ul>
 * </li>
 * <li>interface:
 * <ul>
 * <li>the annotated interface has no member with mutation semantics (setters or other methods with
 * side effects), and its getters return types which are themselves immutable</li>
 * <li>the parent interfaces of the annotated interface are themselves immutable</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author Stefano Chizzolini
 * @see ReadOnly
 * @see Unmodifiable
 */
@Documented
@Retention(CLASS)
@Target(TYPE)
public @interface Immutable {
}
