/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ExceptionsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.pdfclown.common.build.test.assertion.Verifiers.COMBINATION;
import static org.pdfclown.common.util.ConditionsTest.STRING_NAMES;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class ExceptionsTest extends BaseTest {
  static final List<@Nullable String> STRING_VALUES = asList(
      null,
      "my value");

  @Test
  @SuppressWarnings("NullableProblems")
  void unexpected() {
    COMBINATION.verify(
        (name, value, message, arg1, arg2) -> {
          var ret = Exceptions.unexpected(name, value, message, arg1, arg2);
          assertEquals(arg2, ret.getCause());
          return ret;
        },
        List.of("name", "value", "message", "arg1", "arg2"),
        // name
        STRING_NAMES,
        // value
        STRING_VALUES,
        // message
        asList(
            null,
            "MUST be otherwise -- ref: {}"),
        // arg1
        asList(
            null,
            123),
        // arg2
        asList(
            null,
            new NullPointerException()));
  }

  @Test
  @SuppressWarnings("NullableProblems")
  void wrongArg() {
    COMBINATION.verify(
        (name, value, message, arg1, arg2) -> {
          var ret = Exceptions.wrongArg(name, value, message, arg1, arg2);
          assertEquals(arg2, ret.getCause());
          return ret;
        },
        List.of("name", "value", "message", "arg1", "arg2"),
        // name
        STRING_NAMES,
        // value
        STRING_VALUES,
        // message
        asList(
            null,
            "MUST be otherwise -- ref: {}"),
        // arg1
        asList(
            null,
            123),
        // arg2
        asList(
            null,
            new NullPointerException()));
  }
}
