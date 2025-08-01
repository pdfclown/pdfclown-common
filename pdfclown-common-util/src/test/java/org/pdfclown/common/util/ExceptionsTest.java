/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ExceptionsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.pdfclown.common.util.Objects.nonNull;

import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class ExceptionsTest extends BaseTest {
  @Test
  void wrongArg() {
    IllegalArgumentException exception = Exceptions.wrongArg("myArg", 99, "{}! Maybe {}{} or {}",
        "that's it", "not so good", ',', "relevant", new NullPointerException());
    assertEquals("`myArg` (99): that's it! Maybe not so good, or relevant", exception.getMessage());
    assertEquals(NullPointerException.class, nonNull(exception.getCause()).getClass());
  }
}
