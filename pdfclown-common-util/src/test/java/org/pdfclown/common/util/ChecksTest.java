/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

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

import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
public class ChecksTest extends BaseTest {
  @Test
  public void _checkBetween() {
    final int intValue = 42;
    {
      assertEquals(intValue, Checks.checkBetween(intValue, 40, 42));
    }

    final String exceptionMessage = "Doesn't fit";
    final String argumentName = "myIntParam";
    {
      assertEquals(intValue, Checks.checkBetween(intValue, 40, 42, argumentName, exceptionMessage));
    }
    {
      var exception = assertThrows(IllegalArgumentException.class, () -> {
        Checks.checkBetween(intValue, 40, 41, argumentName, exceptionMessage);
      });
      assertEquals("myIntParam (42): Doesn't fit (should be between 40 and 41)",
          exception.getMessage());
    }
  }

  @Test
  public void _checkEqual() {
    final int intValue = 42;
    {
      assertEquals(intValue, Checks.checkEqual(intValue, 42));
    }
    final String exceptionMessage = "I don't think so";
    final String argumentName = "myIntParam";
    {
      assertEquals(intValue, Checks.checkEqual(intValue, 42, argumentName, exceptionMessage));
    }
    {
      var exception = assertThrows(IllegalArgumentException.class, () -> {
        Checks.checkEqual(intValue, 1, argumentName, exceptionMessage);
      });
      assertEquals("myIntParam (42): I don't think so (should be 1)", exception.getMessage());
    }
  }

  @Test
  public void _checkType() {
    final String StringValue = "My value";
    final String ArgumentName = "myParam";
    {
      assertEquals(StringValue, Checks.checkType(StringValue, ArgumentName, String.class));
    }
    {
      var exception = assertThrows(IllegalArgumentException.class, () -> {
        Checks.checkType(StringValue, ArgumentName, Boolean.class);
      });
      assertEquals("myParam (\"My value\"): MUST be Boolean", exception.getMessage());
    }
    {
      var exception = assertThrows(IllegalArgumentException.class, () -> {
        Checks.checkType(StringValue, ArgumentName, Boolean.class, Integer.class, Range.class);
      });
      assertEquals(
          "myParam (\"My value\"): MUST be one of { Boolean, Integer, "
              + "org.pdfclown.common.util.Range }",
          exception.getMessage());
    }
  }
}
