/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BootstrapLog.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.system;

import static org.pdfclown.common.build.internal.temp.util.function.Functions.tryToElse;
import static org.pdfclown.common.build.system.LogManager.SYSTEM_PROPERTY__LOG_LEVEL;
import static org.pdfclown.common.util.Chars.DOLLAR;
import static org.pdfclown.common.util.Chars.DOT;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.event.Level;

/**
 * Minimalist logger.
 * <p>
 * Writes event messages to {@linkplain System#err std-err}, formatted along with level, time, and
 * caller.
 * </p>
 *
 * @apiNote Useful for logging early system initialization operations before the real logging system
 *          kicks in.
 * @author Stefano Chizzolini
 */
public final class BootstrapLog {
  private static final DateTimeFormatter FORMAT__DATETIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

  private static final Level THRESHOLD = tryToElse(System.getProperty(SYSTEM_PROPERTY__LOG_LEVEL),
      Level::valueOf, Level.INFO);

  public static void debug(Class<?> caller, String msg, Object... args) {
    log(caller, Level.DEBUG, msg, args);
  }

  public static void error(Class<?> caller, String msg, Object... args) {
    log(caller, Level.ERROR, msg, args);
  }

  public static void info(Class<?> caller, String msg, Object... args) {
    log(caller, Level.INFO, msg, args);
  }

  public static void warn(Class<?> caller, String msg, Object... args) {
    log(caller, Level.WARN, msg, args);
  }

  @SuppressWarnings("JavaTimeDefaultTimeZone")
  private static void log(Class<?> caller, Level level, String msg, Object... args) {
    if (level.toInt() < THRESHOLD.toInt())
      return;

    // Message.
    System.err.printf("%s  %-5s %s %s%n", LocalDateTime.now().format(FORMAT__DATETIME), level,
        ClassUtils.getAbbreviatedName(caller.getName(), 1).replace(DOLLAR, DOT),
        args.length > 0 ? String.format(msg, args) : msg);

    // Stack trace.
    if (args.length > 0 && args[args.length - 1] instanceof Throwable t) {
      t.printStackTrace(System.err);
    }
  }

  private BootstrapLog() {
  }
}