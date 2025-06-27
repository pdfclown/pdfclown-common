/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Unmodifiable.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_.annot;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated type or type use is unmodifiable.
 * <p>
 * Unmodifiability is about the <i>stability of the object state, referenced objects exclusive</i>;
 * it is stricter than {@linkplain ReadOnly view unmodifiability} and looser than
 * {@linkplain Immutable immutability}.
 * </p>
 * <h2>Requirements</h2>
 * <ul>
 * <li>class:
 * <ul>
 * <li>the annotated class has only unmodifiable fields, whose types may be mutable</li>
 * <li>the annotated class is {@code final} (<b>strong unmodifiability</b>), or not (<b>weak
 * unmodifiability</b>)</li>
 * </ul>
 * </li>
 * <li>interface:
 * <ul>
 * <li>the annotated interface has no member with mutation semantics (setters or other methods with
 * side effects), whilst its getters may return types which are mutable</li>
 * <li>the parent interfaces of the annotated interface are themselves unmodifiable</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author Stefano Chizzolini
 * @see Immutable
 * @see ReadOnly
 */
@Documented
@Retention(CLASS)
@Target({ TYPE, TYPE_USE })
public @interface Unmodifiable {
}
