/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Javas.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util.lang;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaParserAdapter;
import com.github.javaparser.ParserConfiguration;

/**
 * Internal configuration shared across the module.
 *
 * @author Stefano Chizzolini
 */
public final class Javas {
  public static final JavaParserAdapter PARSER = JavaParserAdapter.of(new JavaParser(
      new ParserConfiguration()
          .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)));

  private Javas() {
  }
}
