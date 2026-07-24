/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Tests.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test;

import static org.pdfclown.common.build.internal.temp.util.Objects.anyThat;

import java.lang.StackWalker.StackFrame;
import java.lang.reflect.AccessibleObject;
import java.util.Optional;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.params.ParameterizedTest;
import org.pdfclown.common.util.reflect.Reflects;

/**
 * Test utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Tests {
  /**
   * Gets the stack frame of the currently executing test.
   * <p>
   * Test detection is based on JUnit 5 annotations (see <b>test method</b> definition in
   * {@link Test @Test}).
   * </p>
   *
   * @apiNote Useful to detect which test is currently executing.
   */
  public static Optional<StackFrame> testFrame() {
    return Reflects.stackFrame($ -> Reflects.method($)
        .map($$ -> anyThat($$, AccessibleObject::isAnnotationPresent,
            Test.class,
            RepeatedTest.class,
            ParameterizedTest.class,
            TestFactory.class,
            TestTemplate.class))
        .orElse(false));
  }

  private Tests() {
  }
}
