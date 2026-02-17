/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (LogManager.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.system;

import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Objects.objTo;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.jspecify.annotations.Nullable;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;

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
   * Logging profile.
   */
  public enum Profile {
    /**
     * CLI application.
     */
    CLI
  }

  /**
   * Appender for assertion-related logs.
   */
  public static final String APPENDER_NAME__ASSERT = "Assert";

  /**
   * Marker for log entries excluded from console.
   */
  public static final org.slf4j.Marker MARKER__VERBOSE = MarkerFactory.getMarker("VERBOSE");

  private static @Nullable Level defaultLevel;
  private static boolean levelOverridden;

  /**
   * System property specifying the root log level applied during testing.
   * <p>
   * The value of this property corresponds to the name of a {@link Level} constant (for example,
   * {@link Level#DEBUG DEBUG}).
   * </p>
   *
   * @apiNote For example, to apply {@link Level#TRACE TRACE} level (Maven build system):
   *          <pre class="lang-shell"><code>
   * mvn verify ... -Dlog.level=TRACE</code></pre>
   */
  public static final String SYSTEM_PROPERTY__LOG_LEVEL = "log.level";
  static {
    Level systemLevel = objTo(System.getProperty(SYSTEM_PROPERTY__LOG_LEVEL), Level::valueOf);
    if (systemLevel != null) {
      setLevel(systemLevel);
      levelOverridden = true;
    }
  }

  /**
   * Applies a logging profile.
   */
  public static void applyProfile(Profile profile) {
    switch (profile) {
      case CLI:
        setLevel(Level.INFO);
        break;
      default:
        throw unexpected("profile", profile);
    }
  }

  /**
   * Binds the loggers associated to a package to an existing log appender.
   *
   * @param packageType
   *          A type belonging to the intended package.
   * @param appenderName
   *          Name of the log appender to bind.
   */
  public static void bindLogger(Class<?> packageType, String appenderName) {
    var logContext = getContext();
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

  /**
   * Root logging level.
   */
  public static Level getLevel() {
    return Level.valueOf(getContext().getConfiguration().getRootLogger().getLevel().name());
  }

  /**
   * Whether the {@linkplain #getLevel() root logging level} was set via
   * {@link #SYSTEM_PROPERTY__LOG_LEVEL}.
   * <p>
   * As a consequence, calls to {@link #setLevel(Level)} are ineffective — this is useful to
   * manually force logging to a certain level, without being subsequently altered by existing
   * automated configurations.
   * </p>
   */
  public static boolean isLevelOverridden() {
    return levelOverridden;
  }

  /**
   * Sets the {@linkplain #getLevel() root logging level}.
   * <p>
   * NOTE: If level setting was {@linkplain #isLevelOverridden() overridden}, calls to this setter
   * are ignored.
   * </p>
   *
   * @param value
   *          ({@code null}, to restore the default level)
   */
  public static void setLevel(@Nullable Level value) {
    if (levelOverridden)
      return;
    else if (value == null) {
      if (defaultLevel == null)
        // NOP
        return;

      value = defaultLevel;
    }
    var implLevel = org.apache.logging.log4j.Level.valueOf(value.name());
    var logContext = getContext();
    var rootLogger = logContext.getConfiguration().getRootLogger();
    if (defaultLevel == null) {
      defaultLevel = Level.valueOf(rootLogger.getLevel().name());
    }
    rootLogger.setLevel(implLevel);
    logContext.updateLoggers();
  }

  /**
   * Gets the logger context.
   */
  private static LoggerContext getContext() {
    return (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
  }

  private LogManager() {
  }
}
