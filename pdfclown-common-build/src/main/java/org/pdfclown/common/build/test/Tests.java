/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Tests.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test;

import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.params.ParameterizedTest;
import org.pdfclown.common.build.internal.util_.reflect.Reflects;

/**
 * Test utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Tests {
  /**
   * Gets the stack frame of the currently executing test.
   * <p>
   * Useful to detect which test is currently executing.
   * </p>
   * <p>
   * Test detection is based on JUnit 5 annotations (see <b>test method</b> definition in
   * {@link Test @Test}).
   * </p>
   */
  public static Optional<StackFrame> testFrame() {
    return Reflects.stackFrame($ -> {
      Method m = Reflects.method($);
      return m.isAnnotationPresent(Test.class)
          || m.isAnnotationPresent(RepeatedTest.class)
          || m.isAnnotationPresent(ParameterizedTest.class)
          || m.isAnnotationPresent(TestFactory.class)
          || m.isAnnotationPresent(TestTemplate.class);
    });
  }

  private Tests() {
  }
}
