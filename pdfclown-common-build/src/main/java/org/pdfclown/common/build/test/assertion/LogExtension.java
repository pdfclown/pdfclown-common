/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (LogExtension.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.pdfclown.common.build.internal.util.Objects.OBJ_ARRAY__EMPTY;
import static org.pdfclown.common.build.internal.util.Strings.EMPTY;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.hamcrest.Matcher;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

/**
 * Provides logging interception to test assertions over logged events.
 *
 * @author Stefano Chizzolini
 * @apiNote This extension can be wired up into a test class via a static field annotated with
 *          {@link RegisterExtension}, eg: <pre>
 *{@code @}RegisterExtension
 *static LogInterceptor logInterceptor = new LogExtension();</pre>
 *          <p>
 *          NOTE: Instance fields are not suitable for the purpose since they are outside the
 *          required class-level JUnit life-cycle.
 *          </p>
 */
public class LogExtension
    implements LogInterceptor, AfterAllCallback, BeforeAllCallback, BeforeEachCallback {
  private static class InterceptionAppender extends AbstractAppender {
    private final List<LogEvent> events = new ArrayList<>();

    public InterceptionAppender() {
      super("Interception", null, null, true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
      events.add(event.toImmutable());
    }

    public void reset() {
      events.clear();
    }
  }

  private static class LoggedEvent implements LoggingEvent {
    public static LoggedEvent of(LogEvent nativeEvent) {
      return new LoggedEvent(
          Level.valueOf(nativeEvent.getLevel().name()),
          nativeEvent.getMessage().getFormattedMessage(),
          nativeEvent.getMessage().getThrowable());
    }

    private final Level level;
    private final String message;
    private final @Nullable Throwable throwable;

    public LoggedEvent(Level level, String message, @Nullable Throwable throwable) {
      this.level = requireNonNull(level, "`level`");
      this.message = requireNonNull(message, "`message`");
      this.throwable = throwable;
    }

    @Override
    public Object[] getArgumentArray() {
      return OBJ_ARRAY__EMPTY;
    }

    @Override
    public Level getLevel() {
      return level;
    }

    @Override
    public String getLoggerName() {
      return EMPTY;
    }

    @Override
    public @Nullable Marker getMarker() {
      return null;
    }

    @Override
    public String getMessage() {
      return message;
    }

    @Override
    public String getThreadName() {
      return EMPTY;
    }

    @Override
    public @Nullable Throwable getThrowable() {
      return throwable;
    }

    @Override
    public long getTimeStamp() {
      return 0;
    }
  }

  @SuppressWarnings("NotNullFieldNotInitialized")
  private /* @InitNonNull */ InterceptionAppender appender;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private /* @InitNonNull */ LoggerConfig rootLoggerConfig;

  @Override
  public void afterAll(ExtensionContext context) {
    rootLoggerConfig.removeAppender(appender.getName());
  }

  @Override
  public LoggingEvent assertLogged(@Nullable Level level,
      @Nullable Matcher<String> messageMatcher) {
    var nativeLevel = level != null ? org.apache.logging.log4j.Level.valueOf(level.name()) : null;
    for (LogEvent event : appender.events) {
      if ((nativeLevel == null || nativeLevel.compareTo(event.getLevel()) >= 0)
          && (messageMatcher == null || messageMatcher.matches(event.getMessage()
              .getFormattedMessage())))
        return LoggedEvent.of(event);
    }
    fail(String.format("Event match MISSING where level is %s and message is %s",
        level != null ? level : "ANY", messageMatcher != null ? messageMatcher : "ANY"));
    return null /* NOTE: Unreachable but required by the compiler. */;
  }

  @Override
  public void assertNotLogged(@Nullable Level level, @Nullable Matcher<String> messageMatcher) {
    try {
      assertLogged(level, messageMatcher);
    } catch (AssertionError ex) {
      return /* NOOP: Missing log is just the condition we are looking for here. */;
    }
    fail(String.format("Event match FOUND where level is %s and message is %s",
        level != null ? level : "ANY", messageMatcher != null ? messageMatcher : "ANY"));
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    rootLoggerConfig = ((LoggerContext) LogManager.getContext(false)).getConfiguration()
        .getLoggerConfig(EMPTY);

    // Inject the log interception appender!
    rootLoggerConfig.addAppender(appender = new InterceptionAppender(),
        org.apache.logging.log4j.Level.DEBUG, null);
    appender.start();
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    reset() /* Ensures each test sees its own log events only */;
  }

  @Override
  public void reset() {
    appender.reset();
  }
}
