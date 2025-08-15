/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (LazyNonNull.java) is part of pdfclown-common-build module in pdfClown Common project
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
 * Indicates that once the annotated field (or variable) becomes non-null, it never becomes null
 * again.
 * <p>
 * Similar to <a href=
 * "https://checkerframework.org/api/org/checkerframework/checker/nullness/qual/MonotonicNonNull.html">MonotonicNonNull</a>.
 * </p>
 * <p>
 * <span class="important">IMPORTANT: In case of field, its assignment typically occurs in the
 * accessor; as a consequence, <i>the annotated field should NEVER be accessed directly until its
 * accessor method is called</i>.</span>
 * </p>
 * <p>
 * Useful for lazy field initialization — e.g. [*]:
 * </p>
 * <pre class="lang-java"><code>
 * &#64;NullMarked
 * class MyClass {
 *   &#64;LazyNonNull &#64;Nullable Object object;
 *
 *   MyClass() {
 *     . . .
 *   }
 *
 *   public Object getObject() {
 *     if (object == null) {
 *       object = . . .;
 *     }
 *     return object;
 *   }
 *
 *   public void setObject(Object value) {
 *     object = requireNonNull(value);
 *   }
 * }</code></pre>
 * <p>
 * <span class="important">[*] IMPORTANT: This annotation is for documentation purposes only; since
 * static analyzers don't recognize its semantics, <i>it MUST be accompanied by
 * {@link Nullable @Nullable}</i>.</span>
 * </p>
 *
 * @author Stefano Chizzolini
 * @see Derived
 * @see InitNonNull
 * @see Nullable
 * @see <a href="https://github.com/jspecify/jspecify/issues/431">JSpecify issue 431 — What about
 *      lazy initialization/setters</a>
 */
@Documented
@Retention(CLASS)
@Target(TYPE_USE)
public @interface LazyNonNull {
}
