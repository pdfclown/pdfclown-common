/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SystemsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.system;

import static java.util.Arrays.asList;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class SystemsTest extends BaseTest {
  @Test
  void getBooleanProperty() {
    final var key = "myProperty";

    combinationVerifier.verify(
        (value) -> {
          if (value != null) {
            System.setProperty(key, value);
          } else {
            System.clearProperty(key);
          }
          return Systems.getBooleanProperty(key);
        },
        List.of("value"),
        // value
        asList(
            null,
            "false",
            "something else",
            "",
            "true",
            "True"));
  }
}