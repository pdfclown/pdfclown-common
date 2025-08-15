/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PolyNull.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.annot;

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
 * <pre class="lang-java"><code>
 * &#64;NullMarked
 * class Class{@code <T>} {
 *   &#64;PolyNull &#64;Nullable T cast(&#64;PolyNull &#64;Nullable Object obj) {
 *     . . .
 *   }
 * }</code></pre>
 * <p>
 * equals (if only nullness information could be used during overload resolution) to
 * </p>
 * <pre class="lang-java"><code>
 * &#64;NullMarked
 * class Class{@code <T>} {
 *   // this method as before
 *   &#64;Nullable T cast(&#64;Nullable Object obj) {
 *     . . .
 *   }
 *
 *   // but also this "overload"!
 *   T cast(Object obj) {
 *     . . .
 *   }
 * }</code></pre>
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
