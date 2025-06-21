/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ReadOnly.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.annot;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated type or type use allows only read access to its state, like an
 * unmodifiable view.
 * <p>
 * View unmodifiability is about the <i>stability of the object state, referenced objects exclusive,
 * against observer access</i>; it is looser than {@linkplain Unmodifiable proper unmodifiability}
 * and {@linkplain Immutable immutability}.
 * </p>
 * <h2>Requirements</h2>
 * <ul>
 * <li>class:
 * <ul>
 * <li>the annotated class has effectively no member with mutation semantics (any setter or other
 * method with side effects throws {@link UnsupportedOperationException})</li>
 * <li>the annotated class is {@code final} (<b>strong view unmodifiability</b>), or not (<b>weak
 * view unmodifiability</b>)</li>
 * </ul>
 * </li>
 * <li>interface: not applicable (read-only access is an implementation detail)</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 * @see Immutable
 * @see Unmodifiable
 */
@Documented
@Retention(CLASS)
@Target({ TYPE, TYPE_USE })
public @interface ReadOnly {
}
