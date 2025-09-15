/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (InitNonNull.java) is part of pdfclown-common-build module in pdfClown Common project
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
 * Indicates that the annotated field becomes non-null as soon as the object initialization phase
 * ends.
 * <p>
 * Similar to <a href=
 * "https://checkerframework.org/api/org/checkerframework/checker/nullness/qual/MonotonicNonNull.html">MonotonicNonNull</a>.
 * </p>
 * <p>
 * Useful for field initialization outside the constructor (that is, subroutines called by the
 * constructor, mandatory subclass delegation or automated initialization) — for example [*]:
 * </p>
 * <pre class="lang-java"><code>
 * &#64;NullMarked
 * class MyClass {
 *   &#64;InitNonNull Object object;
 *
 *   MyClass() {
 *     . . .
 *     load();
 *   }
 *
 *   private void load() {
 *     object = . . .;
 *   }
 * }</code></pre>
 * <p>
 * [*] NOTE: This annotation is for documentation purposes only; since static analyzers don't
 * recognize its semantics, corresponding nullness warnings have to be suppressed.
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
