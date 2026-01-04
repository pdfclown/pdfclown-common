/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ChecksTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings("CodeBlock2Expr")
class ConditionsTest extends BaseTest {
  @Test
  void requireEqual__failure() {
    final int value = 42;

    var exception = assertThrows(IllegalArgumentException.class, () -> {
      Conditions.requireEqual(value, 41, "myIntParam");
    });
    assertEquals("`myIntParam` (42): MUST be 41", exception.getMessage());
  }

  @Test
  void requireEqual__success() {
    final int value = 42;

    assertEquals(value, Conditions.requireEqual(value, 42));
  }

  @Test
  void requireRange__failure() {
    final int value = 42;

    var exception = assertThrows(IllegalArgumentException.class, () -> {
      Conditions.requireRange(value, 40, 41, "myIntParam");
    });
    assertEquals("`myIntParam` (42): MUST be between 40 and 41", exception.getMessage());
  }

  @Test
  void requireRange__success() {
    final int value = 42;

    assertEquals(value, Conditions.requireRange(value, 40, 42));
  }

  @Test
  void requireType_Class__failure() {
    Number value = 1L;

    var exception = assertThrows(IllegalArgumentException.class, () -> {
      Conditions.requireType(value, Boolean.class);
    });
    assertEquals("`value` (1L): MUST be Boolean", exception.getMessage());
  }

  @Test
  void requireType_Class__success() {
    Number value = 1L;

    @SuppressWarnings("DataFlowIssue" /* null-safe */)
    long result = Conditions.requireType(value, Long.class);
    assertEquals(value, result);
  }

  @Test
  void requireType_Collection__failure() {
    Number value = 1L;

    var exception = assertThrows(IllegalArgumentException.class, () -> {
      Conditions.requireType(value, List.of(Boolean.class, Integer.class, Range.class),
          "myParam");
    });
    assertEquals(
        "`myParam` (1L): MUST be one of { Boolean, Integer, org.pdfclown.common.util.Range }",
        exception.getMessage());
  }

  @Test
  void requireType_Collection__success() {
    Number value = 1L;

    Number result = Conditions.requireType(value, List.of(Boolean.class, Long.class,
        Range.class), "myParam");
    assertEquals(value, result);
  }
}
