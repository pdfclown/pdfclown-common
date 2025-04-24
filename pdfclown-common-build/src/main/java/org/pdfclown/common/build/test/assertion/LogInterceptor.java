/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (LogInterceptor.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import org.hamcrest.Matcher;
import org.jspecify.annotations.Nullable;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

/**
 * Intercepts log activity to test assertions over log events.
 *
 * @author Stefano Chizzolini
 */
public interface LogInterceptor {
  /**
   * Asserts that log events matching the given criteria occurred.
   *
   * @param level
   *          Matching level ({@code null} matches any). Any level at least of the same priority is
   *          matched.
   * @param message
   *          Matching message ({@code null} matches any).
   */
  public LoggingEvent assertLogged(@Nullable Level level, @Nullable Matcher<String> message);

  /**
   * Asserts that no log event matching the given criteria occurred.
   *
   * @param level
   *          Matching level ({@code null} matches any). Any level at least of the same priority is
   *          matched.
   * @param message
   *          Matching message ({@code null} matches any).
   */
  public void assertNotLogged(@Nullable Level level, @Nullable Matcher<String> message);

  /**
   * Cleans up the stored log events.
   */
  public void reset();
}
