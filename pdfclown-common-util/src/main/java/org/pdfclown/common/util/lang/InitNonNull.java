/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (InitNonNull.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.lang;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.jspecify.annotations.Nullable;

/**
 * Indicates that the annotated field becomes non-null as soon as the object initialization phase
 * ends.
 * <p>
 * Similar to <a href=
 * "https://checkerframework.org/api/org/checkerframework/checker/nullness/qual/MonotonicNonNull.html">MonotonicNonNull</a>.
 * </p>
 * <p>
 * Useful for field initialization outside the constructor (ie, subroutines called by the
 * constructor, mandatory subclass delegation or automated initialization) — eg [*]:
 * </p>
 * <pre>
 *{@code @}NullMarked
 *class MyClass {
 *  {@code @}InitNonNull Object object;
 *
 *  {@code @}SuppressWarnings("null")
 *  MyClass() {
 *    . . .
 *    load();
 *  }
 *
 *  private void load() {
 *    object = . . .;
 *  }
 *}</pre>
 * <p>
 * [*] NOTE: This annotation is for documentation purposes only; since static analyzers don't
 * recognize its semantics, corresponding nullness warnings on construction have to be suppressed.
 * </p>
 * <p>
 * DO NOT use this annotation for mandatory fields which are expected to be manually set from
 * outside the class through explicit setter invocations: mark them as {@link Nullable @Nullable}
 * instead; their respective setter and getter shall be non-null.
 * </p>
 *
 * @author Stefano Chizzolini
 * @see LazyNonNull
 * @see Nullable
 */
@Documented
@Retention(CLASS)
@Target(TYPE_USE)
public @interface InitNonNull {
}
