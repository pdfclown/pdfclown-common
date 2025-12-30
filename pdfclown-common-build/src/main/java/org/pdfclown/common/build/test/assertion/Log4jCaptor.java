/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Log4jCaptor.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static org.pdfclown.common.util.Objects.objTo;
import static org.pdfclown.common.util.Objects.sqnd;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.jspecify.annotations.Nullable;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

/**
 * {@link LogCaptor} for log4j.
 *
 * @author Stefano Chizzolini
 */
class Log4jCaptor extends LogCaptor {
  private static class CaptureAppender extends AbstractAppender {
    private final List<LoggingEvent> events = new ArrayList<>();

    CaptureAppender() {
      super(sqnd(CaptureAppender.class), null, null, true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
      events.add(new Capture(level(event.getLevel()), event.getMessage().getFormattedMessage(),
          event.getMessage().getThrowable()));
    }

    public List<LoggingEvent> getEvents() {
      return events;
    }

    public void reset() {
      events.clear();
    }
  }

  private static Level level(org.apache.logging.log4j.Level nativeLevel) {
    return Level.valueOf(nativeLevel.name());
  }

  private static org.apache.logging.log4j.@Nullable Level nativeLevel(@Nullable Level level) {
    return objTo(level, $ -> org.apache.logging.log4j.Level.valueOf($.name()));
  }

  private final CaptureAppender appender = new CaptureAppender();
  private final LoggerConfig logConfig;

  /**
   * New instance capturing events of a logger.
   */
  Log4jCaptor(String loggerName) {
    Configuration config = ((LoggerContext) LogManager.getContext(false)).getConfiguration();
    logConfig = config.getLoggerConfig(loggerName);
  }

  @Override
  public List<LoggingEvent> getEvents() {
    return appender.getEvents();
  }

  @Override
  public void reset() {
    appender.reset();
  }

  @Override
  public void start() {
    appender.start();
  }

  @Override
  public void stop() {
    appender.stop();
  }

  @Override
  protected void attach() {
    if (!logConfig.getAppenders().containsKey(appender.getName())) {
      logConfig.addAppender(appender, nativeLevel(getLevel()), null);
    }
  }

  @Override
  protected boolean detach() {
    if (!logConfig.getAppenders().containsKey(appender.getName()))
      return false;

    logConfig.removeAppender(appender.getName());
    return true;
  }

  @Override
  protected void onLevelChanged() {
    /*
     * NOTE: In order not to alter the observed state, the logger level is left as-is, only the
     * capturing appender is redefined.
     */
    detach();
    attach();
  }
}
