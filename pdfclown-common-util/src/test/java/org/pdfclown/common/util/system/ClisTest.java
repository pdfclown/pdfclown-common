/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ClisTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.system;

import static java.util.Arrays.asList;
import static org.pdfclown.common.build.test.assertion.Verifiers.VERIFIER__COMBINATION;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class ClisTest extends BaseTest {
  @Test
  void parseArgs() {
    VERIFIER__COMBINATION.verify(
        (argsString) -> Clis.parseArgs(argsString),
        List.of("argsString"),
        // argsString
        asList(
            // Quoted arguments embracing whitespace.
            "val1 val2 \"val3 (dquote)\" 'val4 (squote)'",
            // Quoted arguments with inner escaping.
            "val1 \"val2 (\\\"dquote\\\")\" 'val3 (\\'squote\\')'",
            // Quoted argument continuing on missing closing quote.
            "val1 \"val2 (\\\"dquote\\\") 'val3 (\\'squote\\')'",
            // Quoted argument continuing on contiguous quoted chunks without whitespace in between.
            "val1 \"val2 (\\\"dquote\\\")\"'val3 (\\'squote\\')'",
            // Multi-line arguments.
            "\"Multi-\nLine\nArgument\" \"val2\"",
            // Escaped backslashes.
            "val1 C:\\\\Some\\\\Random\\\\Path\\\\"));
  }
}