/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (LogMarker.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import org.slf4j.MarkerFactory;

/**
 * @author Stefano Chizzolini
 */
public final class LogMarker {
  /**
   * Marker for assertion log entries excluded from console.
   */
  @SuppressWarnings("null")
  public static final org.slf4j.Marker VERBOSE = MarkerFactory.getMarker("VERBOSE");

  private LogMarker() {
  }
}
