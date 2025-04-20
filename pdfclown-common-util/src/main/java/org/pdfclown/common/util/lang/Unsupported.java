/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (Unsupported.java) is part of pdfclown-common-util module in pdfClown Common project
  (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
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
