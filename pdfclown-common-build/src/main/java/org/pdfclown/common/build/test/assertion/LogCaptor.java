/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (LogCaptor.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.build.internal.util_.Objects.OBJ_ARRAY__EMPTY;
import static org.pdfclown.common.build.internal.util_.Objects.fqn;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

/**
 * {@linkplain org.junit.jupiter.api.extension.Extension JUnit extension} for capturing log entries.
 *
 * @author Stefano Chizzolini
 * @see Matchers#matchesEvent(Level, String, Class)
 */
public abstract class LogCaptor
    implements AutoCloseable, AfterAllCallback, BeforeAllCallback, AfterEachCallback {
  /**
   * Log entry capture.
   *
   * @author Stefano Chizzolini
   */
  protected static class Capture implements LoggingEvent {
    private final Level level;
    private final String message;
    private final @Nullable Throwable throwable;

    protected Capture(Level level, String message, @Nullable Throwable throwable) {
      this.level = requireNonNull(level, "`level`");
      this.message = requireNonNull(message, "`message`");
      this.throwable = throwable;
    }

    @Override
    public Object[] getArgumentArray() {
      return OBJ_ARRAY__EMPTY;
    }

    @Override
    public List<Object> getArguments() {
      return List.of();
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() {
      return List.of();
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
    public List<Marker> getMarkers() {
      return List.of();
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

  /**
   * New instance capturing root logger events.
   */
  public static LogCaptor of() {
    return of(EMPTY);
  }

  /**
   * New instance capturing events of the given logger.
   */
  public static LogCaptor of(Class<?> loggerClass) {
    return of(loggerClass.getName());
  }

  /**
   * New instance capturing events of the given logger.
   */
  public static LogCaptor of(String loggerName) {
    String implName = fqn(LoggerFactory.getILoggerFactory()).toLowerCase();
    // Log4J?
    if (implName.contains("log4j"))
      return new Log4jCaptor(loggerName);
    /*
     * TODO: after the initial release, when `org.pdfclown.common.util.spi.ServiceProvider` is
     * available, define a service (in `org.pdfclown.common.build.spi`) to discover other logging
     * adapters.
     */
    // Unknown.
    else
      throw new UnsupportedOperationException("Logging implementation NOT supported: " + implName);
  }

  private @Nullable Level level;

  @Override
  public void afterAll(ExtensionContext context) {
    close();
  }

  @Override
  public void afterEach(ExtensionContext context) {
    reset();
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    attach();
    start();
  }

  @Override
  public void close() {
    stop();
    detach();
  }

  /**
   * Filters the captured events by the given predicate.
   */
  public Stream<LoggingEvent> event(Predicate<? super LoggingEvent> predicate) {
    return getEvents().stream().filter(predicate);
  }

  /**
   * Gets the event captured at the given position.
   */
  public LoggingEvent event(int index) {
    return getEvents().get(index);
  }

  /**
   * Gets the captured events transformed according to the given mapper.
   */
  public <R> Stream<R> eventAs(Function<? super LoggingEvent, ? extends R> mapper) {
    return getEvents().stream().map(mapper);
  }

  /**
   * Count of the captured events.
   */
  public int eventCount() {
    return getEvents().size();
  }

  /**
   * Captured events.
   */
  public abstract List<LoggingEvent> getEvents();

  /**
   * Capture threshold.
   */
  public @Nullable Level getLevel() {
    return level;
  }

  /**
   * Whether no event was captured.
   */
  public boolean noEvent() {
    return getEvents().isEmpty();
  }

  /**
   * Resets the captured events.
   */
  public abstract void reset();

  /**
   * Sets {@link #getLevel() level}.
   */
  public LogCaptor setLevel(@Nullable Level value) {
    if (level != value) {
      level = value;

      onLevelChanged();
    }
    return this;
  }

  /**
   * Enables the event capture.
   */
  public abstract void start();

  /**
   * Disables the event capture.
   */
  public abstract void stop();

  /**
   * Attaches this captor to the logger.
   */
  protected abstract void attach();

  /**
   * Detaches this captor from the logger.
   */
  protected abstract boolean detach();

  /**
   * Notifies that {@link #getLevel() level} has changed.
   */
  protected abstract void onLevelChanged();
}
