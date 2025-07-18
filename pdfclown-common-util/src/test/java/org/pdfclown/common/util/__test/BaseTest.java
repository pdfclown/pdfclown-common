/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BaseTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.__test;

import javax.measure.Unit;
import org.pdfclown.common.build.test.Test;
import org.pdfclown.common.build.test.assertion.Assertions.Argument;
import org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig;

/**
 * Module-specific unit test.
 *
 * @author Stefano Chizzolini
 */
public abstract class BaseTest extends Test {
  private static final ArgumentsStreamConfig.Converter ARGUMENTS_CONVERTER = ($index, $obj) -> {
    if ($obj instanceof Unit) {
      var unit = (Unit<?>) $obj;
      return Argument.of(String.format("%s (%s)", unit, unit.getName()), unit);
    } else
      return $obj;
  };

  protected static ArgumentsStreamConfig cartesianArgumentsStreamConfig() {
    return ArgumentsStreamConfig.cartesian()
        .setConverter(ARGUMENTS_CONVERTER);
  }

  protected static ArgumentsStreamConfig simpleArgumentsStreamConfig() {
    return ArgumentsStreamConfig.simple()
        .setConverter(ARGUMENTS_CONVERTER);
  }
}
