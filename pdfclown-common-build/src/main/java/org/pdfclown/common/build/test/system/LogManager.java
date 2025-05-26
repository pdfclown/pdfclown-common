/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (LogManager.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.system;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * Log manager exposing common logging utilities.
 * <p>
 * NOTE: Any entity herein referenced is defined in "{@code /log4j2.xml}" resource.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public final class LogManager {
  /**
   * Appender for assertion-related logs.
   */
  public static final String APPENDER_NAME__ASSERT = "Assert";

  /**
   * Binds the loggers associated to the given package to an existing log appender.
   *
   * @param packageType
   *          A type belonging to the intended package.
   * @param appenderName
   *          Name of the log appender to bind.
   */
  public static void bindLogger(Class<?> packageType, String appenderName) {
    var logContext = (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
    Configuration logConfig = logContext.getConfiguration();
    Appender appender = logConfig.getAppender(appenderName);
    var loggerConfig = LoggerConfig.newBuilder()
        .withConfig(logConfig)
        .withIncludeLocation("true")
        .withLoggerName(packageType.getPackageName())
        .withRefs(new AppenderRef[] {
            AppenderRef.createAppenderRef(appender.getName(), null, null) })
        .build();
    loggerConfig.addAppender(appender, null, null);
    logConfig.addLogger(loggerConfig.getName(), loggerConfig);
    logContext.updateLoggers();
  }

  private LogManager() {
  }
}
