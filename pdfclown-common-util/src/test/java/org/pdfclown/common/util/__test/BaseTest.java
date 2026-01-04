/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BaseTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.__test;

import static org.pdfclown.common.build.test.assertion.Assertions.Argument.arg;
import static org.pdfclown.common.util.Objects.objTo;

import javax.measure.Unit;
import org.pdfclown.common.build.test.Test;
import org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy;

/**
 * Module-specific unit test.
 *
 * @author Stefano Chizzolini
 */
public abstract class BaseTest extends Test {
  private static final ArgumentsStreamStrategy.Converter ARGUMENTS_CONVERTER = ($index, $obj) -> {
    if ($obj instanceof Unit<?> unit) {
      return objTo(unit, $ -> arg($.getName(), $));
    } else
      return $obj;
  };

  protected static ArgumentsStreamStrategy cartesianArgumentsStreamStrategy() {
    return ArgumentsStreamStrategy.cartesian()
        .setConverter(ARGUMENTS_CONVERTER);
  }

  protected static ArgumentsStreamStrategy simpleArgumentsStreamStrategy() {
    return ArgumentsStreamStrategy.simple()
        .setConverter(ARGUMENTS_CONVERTER);
  }
}
