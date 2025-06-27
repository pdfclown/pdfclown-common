/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PolyNull.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_.annot;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.jspecify.annotations.Nullable;

/**
 * Indicates that <a href="https://github.com/jspecify/jspecify/wiki/polynull">nullness is preserved
 * between annotated inputs and outputs of a method</a>.
 * <p>
 * Arguments marked with this annotation will cause the method to return a non-null result, and vice
 * versa — like if there were two method overloads, e.g. [*]:
 * </p>
 * <pre>
 *{@code @}NullMarked
 *class Class{@code <T>} {
 *  {@code @}PolyNull {@code @}Nullable T cast({@code @}PolyNull {@code @}Nullable Object obj) {
 *    . . .
 *  }
 *}</pre>
 * <p>
 * equals (if only nullness information could be used during overload resolution) to
 * </p>
 * <pre>
 *{@code @}NullMarked
 *class Class{@code <T>} {
 *  // this method as before
 *  {@code @}Nullable T cast({@code @}Nullable Object obj) {
 *    . . .
 *  }
 *
 *  // but also this "overload"!
 *  T cast(Object obj) {
 *    . . .
 *  }
 *}</pre>
 * <p>
 * <span class="important">[*] IMPORTANT: Because of the lack of standardization, this annotation is
 * currently for documentation purposes only; as a consequence, <i>it MUST be accompanied by
 * {@link Nullable @Nullable}</i>.</span>
 * </p>
 *
 * @author Stefano Chizzolini
 */
@Documented
@Retention(CLASS)
@Target(TYPE_USE)
public @interface PolyNull {
}
