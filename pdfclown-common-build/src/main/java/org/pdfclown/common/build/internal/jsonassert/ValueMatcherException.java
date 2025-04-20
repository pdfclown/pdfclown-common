/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (ValueMatcherException.java) is part of pdfclown-common-build module in pdfClown Common
  project (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
/*
  SPDX-FileCopyrightText: © 2012-2022 Skyscreamer <https://github.com/skyscreamer>
  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.build.internal.jsonassert;

/**
 * Exception that may be thrown by ValueMatcher subclasses to provide more detail on why matches
 * method failed.
 *
 * @author Duncan Mackinder
 *
 */
public class ValueMatcherException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private final String expected;

  private final String actual;

  /**
   * Create new ValueMatcherException
   *
   * @param message
   *          description of exception
   * @param expected
   *          value expected by ValueMatcher
   * @param actual
   *          value being tested by ValueMatcher
   */
  public ValueMatcherException(String message, String expected, String actual) {
    super(message);
    this.expected = expected;
    this.actual = actual;
  }

  /**
   * Create new ValueMatcherException
   *
   * @param message
   *          description of exception
   * @param cause
   *          cause of ValueMatcherException
   * @param expected
   *          value expected by ValueMatcher
   * @param actual
   *          value being tested by ValueMatcher
   */
  public ValueMatcherException(String message, Throwable cause, String expected, String actual) {
    super(message, cause);
    this.expected = expected;
    this.actual = actual;
  }

  /**
   * @return the actual value
   */
  public String getActual() {
    return actual;
  }

  /**
   * @return the expected value
   */
  public String getExpected() {
    return expected;
  }
}
