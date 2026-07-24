/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Initializer.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.annot;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.jspecify.annotations.Nullable;

/**
 * Indicates that the annotated method provides delayed initialization to non-null fields.
 *
 * @apiNote Useful for field initialization outside the constructor — for example [*]:
 *          <pre class="lang-java"><code>
 * &#64;NullMarked
 * class MyClass {
 *   Object object;
 *
 *   MyClass() {
 *     . . .
 *   }
 *
 *   &#64;Initializer
 *   public void init() {
 *     object = . . .;
 *   }
 * }</code></pre>
 *          <p>
 *          [*] NOTE: Since the support to this annotation by static analyzers is uneven, its
 *          semantics may be ignored; consequently, corresponding nullness warnings have to be
 *          suppressed.
 *          </p>
 * @author Stefano Chizzolini
 * @see MonotonicNonNull
 * @see Nullable
 */
@Documented
@Retention(CLASS)
@Target(METHOD)
public @interface Initializer {
}
