/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (ValueMatcherException.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

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
