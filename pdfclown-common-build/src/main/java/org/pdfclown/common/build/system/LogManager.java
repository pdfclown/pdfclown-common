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

import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.pdfclown.common.build.internal.temp.util.Conditions.requireState;
import static org.pdfclown.common.build.internal.temp.util.function.Functions.toOrNull;
import static org.pdfclown.common.build.system.BootstrapLog.info;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Exceptions.wrongState;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;
import org.jspecify.annotations.Nullable;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;

/**
 * Log manager.
 * <p>
 * Provides facilities to automate log configuration in testing environments.
 * </p>
 *
 * @implNote Any logging object herein referenced is defined in
 *           {@value AutoDiscoveryConfigurationFactory#RESOURCE_NAME__CONFIG_FRAGMENT} resource.
 * @author Stefano Chizzolini
 */
public final class LogManager {
  /**
   * Log4j2 plugin for configuration auto-discovery.
   * <p>
   * The configuration is composed picking fragments at {@value #RESOURCE_NAME__CONFIG_FRAGMENT} and
   * conventional Log4j2 user files ({@code log4j2-test.xml}, {@code log4j2.xml}, in order of
   * precedence).
   * </p>
   *
   * @author Stefano Chizzolini
   */
  @Plugin(name = "AutoDiscoveryConfigurationFactory", category = ConfigurationFactory.CATEGORY)
  @Order(-1 /* NOTE: Low value to get priority over default configuration files lookup */)
  public static class AutoDiscoveryConfigurationFactory extends ConfigurationFactory {
    /**
     * Resource name of log configuration fragments.
     */
    public static final String RESOURCE_NAME__CONFIG_FRAGMENT =
        "META-INF/org/pdfclown/common/build/system/log4j2-fragment.xml";

    @Override
    public @Nullable Configuration getConfiguration(LoggerContext ctx, ConfigurationSource source) {
      return getConfiguration(ctx, source.toString(), (URI) null);
    }

    @Override
    @SuppressWarnings("ReferenceEquality")
    public @Nullable Configuration getConfiguration(LoggerContext ctx, String name,
        @Nullable URI configLocation) {
      info(getClass(), "Log configuration discovery BEGIN");

      try {
        var configs = new ArrayList<AbstractConfiguration>();
        var xmlFactory = new XmlConfigurationFactory();

        // 1. Log4j2 configuration fragments.
        {
          Enumeration<URL> fragments = Thread.currentThread().getContextClassLoader()
              .getResources(RESOURCE_NAME__CONFIG_FRAGMENT);
          while (fragments.hasMoreElements()) {
            URL url = fragments.nextElement();
            try (InputStream in = url.openStream()) {
              info(getClass(), "Log configuration fragment FOUND: '%s'", url);

              configs.add((AbstractConfiguration) xmlFactory.getConfiguration(ctx,
                  new ConfigurationSource(in, url)));
            }
          }
        }

        // 2. Conventional Log4j2 user file.
        for (String conventionalConfigResourceName : new String[] {
            "log4j2-test.xml" /*
                               * NOTE: `log4j2-test.xml` takes precedence over `log4j2.xml`, same as
                               * Log4j2's own convention
                               */,
            "log4j2.xml" }) {
          URL url = Thread.currentThread().getContextClassLoader()
              .getResource(conventionalConfigResourceName);
          if (url == null) {
            continue;
          }

          try (InputStream in = url.openStream()) {
            info(getClass(), "Log configuration full file FOUND: '%s'", url);

            configs.add((AbstractConfiguration) xmlFactory.getConfiguration(ctx,
                new ConfigurationSource(in, url)));
          }
          break;
        }

        info(getClass(), "Log configuration discovery END (%s files found)", configs.size());

        return !configs.isEmpty() ? new CompositeConfiguration(configs) : null;
      } catch (IOException ex) {
        throw wrongState("Log4j2 composite configuration building FAILED", ex);
      }
    }

    @Override
    protected String[] getSupportedTypes() {
      return new String[] { "*" };
    }
  }

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
   * Test log files initializer.
   *
   * @implNote Registered as a {@code org.junit.platform.launcher.LauncherSessionListener} SPI
   *           service for automatic activation on JUnit session startup.
   * @author Stefano Chizzolini
   */
  public static class TestPlatformListener implements LauncherSessionListener {
    @Override
    public void launcherSessionOpened(LauncherSession session) {
      info(getClass(), "Log files initialization BEGIN");

      LoggerContext context = getContext();
      try {
        logFiles = retrieveLogFiles(context.getConfiguration());

        cleanLogFiles(logFiles.values());
      } finally {
        context.stop() /*
                        * Releases any file handle opened while inspecting the configuration, so
                        * later real logging starts clean
                        */;

        info(getClass(), "Log files initialization END");
      }
    }

    private void cleanLogFiles(Collection<Path> logFiles) {
      logFiles.forEach($ -> {
        try {
          Files.deleteIfExists($);

          info(getClass(), "Log file CLEANED: '%s'", $);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }

    private Map<String, Path> retrieveLogFiles(Configuration config) {
      return config.getAppenders().values().stream()
          .filter($ -> {
            if ($ instanceof FileAppender f) {
              info(getClass(), "Log file FOUND: '%s' ('%s' appender)", f.getFileName(),
                  f.getName());

              return true;
            } else
              return false;
          })
          .collect(toUnmodifiableMap(Appender::getName,
              $ -> Path.of(((FileAppender) $).getFileName())));
    }
  }

  private static @Nullable Map<String, Path> logFiles;

  /**
   * Appender for assertion-related logs.
   */
  public static final String APPENDER_NAME__ASSERTION = "Assertion";

  /**
   * Marker for log entries excluded from console.
   */
  public static final org.slf4j.Marker MARKER__VERBOSE = MarkerFactory.getMarker("VERBOSE");

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

  private static @Nullable Level defaultLevel;
  private static boolean levelOverridden;
  static {
    Level systemLevel = toOrNull(System.getProperty(SYSTEM_PROPERTY__LOG_LEVEL), Level::valueOf);
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
      case CLI -> setLevel(Level.INFO);
      default -> throw unexpected(profile);
    }
  }

  /**
   * Root logging level.
   */
  public static Level getLevel() {
    return Level.valueOf(getContext().getConfiguration().getRootLogger().getLevel().name());
  }

  /**
   * Configured log files by appender name.
   */
  public static Map<String, Path> getLogFiles() {
    return requireState(logFiles);
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
   * Logger context.
   */
  private static LoggerContext getContext() {
    return (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
  }

  private LogManager() {
  }
}
