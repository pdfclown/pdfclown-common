/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Idempotent.java) is part of pdfclown-common-util module in pdfClown Common project
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

/**
 * Indicates that the annotated method is
 * <a href="https://en.wikipedia.org/wiki/Idempotence">idempotent</a>.
 * <p>
 * This annotation implies that the method can be called repeatedly without changing its result
 * beyond the initial call (non-idempotent methods, on the contrary, require callers to keep track
 * whether they had already been called or not).
 * </p>
 * <p>
 * <span class="important">IMPORTANT: In order for a method to be annotated, its implementation must
 * call only idempotent methods itself.</span>
 * </p>
 *
 * @author Stefano Chizzolini
 */
@Documented
@Retention(CLASS)
@Target(METHOD)
public @interface Idempotent {

}
