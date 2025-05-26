/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Unsupported.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.lang;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method is unsupported, throwing
 * {@link UnsupportedOperationException} any time called.
 * <p>
 * Useful to declare in a strong, machine-readable and semantically unambiguous way that an API
 * member is incompatible with the annotated implementation.
 * </p>
 *
 * @author Stefano Chizzolini
 * @apiNote It is strongly recommended to add a {@code throws UnsupportedOperationException}
 *          declaration to the method signature, and to explain in the documentation, using the
 *          {@code @throws UnsupportedOperationException} javadoc tag, the reason for not supporting
 *          an API member. The documentation should also suggest and link to alternate API, if
 *          applicable; an alternate API often has subtly different semantics, so such issues should
 *          be discussed as well.
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Unsupported {
}
