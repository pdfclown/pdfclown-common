/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Test.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.pdfclown.common.build.internal.temp.util.Conditions.requireState;
import static org.pdfclown.common.util.Chars.HASH;

/**
 * Test unit.
 *
 * @author Stefano Chizzolini
 */
public interface Test {
  /**
   * Test environment.
   */
  TestEnvironment getEnv();

  /**
   * Name of the current test.
   * <p>
   * Corresponds to its {@linkplain org.junit.jupiter.api.TestInfo#getTestMethod() method name}.
   * </p>
   */
  String getTestName();

  /**
   * Qualified name of the current test.
   *
   * @return ({@code this.getClass().getSimpleName() + '#' + this.}{@link #getTestName()})
   * @apiNote Useful for referencing the test, such as to select specific tests for execution (see
   *          {@code -Dtest} and {@code -Dit.test} Maven CLI arguments).
   */
  default String getTestQName() {
    return getClass().getSimpleName() + HASH
        + requireState(stripToNull(getTestName()), "`testName`");
  }
}
