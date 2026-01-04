/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Objects.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util;

import java.util.regex.Pattern;

/**
 * Object utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Objects {
  private static final String REGEX__JAVA_IDENTIFIER =
      "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
  private static final String REGEX__CLASS_FQN =
      REGEX__JAVA_IDENTIFIER + "(\\." + REGEX__JAVA_IDENTIFIER + ")*";
  private static final String REGEX__HEX = "[0-9a-fA-F]+";

  public static final String PATTERN_GROUP__CLASS_FQN = "fqcn";

  /**
   * Pattern of the default implementation of {@link Object#toString()}.
   * <p>
   * Use {@link #PATTERN_GROUP__CLASS_FQN} to get the fully-qualified class name matched.
   * </p>
   */
  public static final Pattern PATTERN__TO_STRING__DEFAULT = Pattern.compile(
      "(?<%s>%s)@%s".formatted(PATTERN_GROUP__CLASS_FQN, REGEX__CLASS_FQN, REGEX__HEX));

  private Objects() {
  }
}
