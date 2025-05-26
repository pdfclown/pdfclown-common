/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TestUnit.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.pdfclown.common.build.internal.util.Objects.sqn;
import static org.pdfclown.common.build.internal.util.Strings.SLASH;
import static org.pdfclown.common.build.internal.util.io.Files.resetDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.Matcher;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.pdfclown.common.build.test.assertion.LogExtension;
import org.pdfclown.common.build.test.assertion.LogInterceptor;
import org.pdfclown.common.build.test.assertion.TestEnvironment;
import org.pdfclown.common.build.util.io.ResourceNames;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

/**
 * Automated testing unit.
 * <p>
 * This test environment assumes:
 * </p>
 * <ul>
 * <li>the filesystem is {@linkplain MavenBaseDirResolver organized according to Maven} (otherwise,
 * a custom filesystem mapping can be specified passing a {@link BaseDirResolver} via
 * {@linkplain #TestUnit(BaseDirResolver) constructor})</li>
 * <li>tests are executed directly from plain filesystem directory (no jar embedding)</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class TestUnit implements TestEnvironment {
  /**
   * Base directory resolver.
   *
   * @author Stefano Chizzolini
   */
  public interface BaseDirResolver {
    /**
     * Main types' source base directory.
     */
    Path getMainTypeSrcPath();

    /**
     * Test output's base directory.
     */
    Path getOutputPath();

    /**
     * Test resources' source base directory.
     */
    Path getResourceSrcPath();

    /**
     * Test types' source base directory.
     */
    Path getTypeSrcPath();
  }

  /**
   * Filesystem mapping for <a href=
   * "https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html">Maven's
   * Standard Directory Layout</a>.
   *
   * @author Stefano Chizzolini
   */
  public static class MavenBaseDirResolver implements BaseDirResolver {
    private static final Path MAIN_TYPE_SRC_PATH = Path.of("src", "main", "java")
        .toAbsolutePath();
    private static final Path OUTPUT_PATH = Path.of("target", "test-output")
        .toAbsolutePath();
    private static final Path RESOURCE_SRC_PATH = Path.of("src", "test", "resources")
        .toAbsolutePath();
    private static final Path TYPE_SRC_PATH = Path.of("src", "test", "java")
        .toAbsolutePath();

    @Override
    public Path getMainTypeSrcPath() {
      return MAIN_TYPE_SRC_PATH;
    }

    @Override
    public Path getOutputPath() {
      return OUTPUT_PATH;
    }

    @Override
    public Path getResourceSrcPath() {
      return RESOURCE_SRC_PATH;
    }

    @Override
    public Path getTypeSrcPath() {
      return TYPE_SRC_PATH;
    }
  }

  @RegisterExtension
  static LogInterceptor logInterceptor = new LogExtension();

  private static final BaseDirResolver BASE_DIR_RESOLVER__MAVEN = new MavenBaseDirResolver();

  private final BaseDirResolver baseDirResolver;
  private boolean outputDirInitialized;
  /**
   * Test resources' target base directory.
   */
  private final Path resourceDir;
  {
    try {
      /*
       * NOTE: The classic way to retrieve the base directory would be via
       * `getClass().getResource("/")`, but, unfortunately, it works in non-modular (ie, non-JPMS)
       * projects only; the alternate solution applied here seems to reliably work in modular as
       * well as non-modular projects.
       */
      resourceDir = Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
    } catch (URISyntaxException ex) {
      throw new RuntimeException(ex) /* Should NEVER happen */;
    }
  }

  protected TestUnit() {
    this(BASE_DIR_RESOLVER__MAVEN);
  }

  protected TestUnit(BaseDirResolver baseDirResolver) {
    this.baseDirResolver = requireNonNull(baseDirResolver, "`baseDirResolver`");
  }

  /**
   * @implNote Method sealed, as it relies on {@link #dirPath(DirId)}.
   */
  @Override
  public final File dir(DirId id) {
    return TestEnvironment.super.dir(id);
  }

  @Override
  public Path dirPath(DirId id) {
    switch (id) {
      case OUTPUT:
        return baseDirResolver.getOutputPath();
      case RESOURCE:
        return resourceDir;
      case RESOURCE_SRC:
        return baseDirResolver.getResourceSrcPath();
      case TYPE_SRC:
        return baseDirResolver.getTypeSrcPath();
      case MAIN_TYPE_SRC:
        return baseDirResolver.getMainTypeSrcPath();
      default:
        throw new UnsupportedOperationException("Unexpected `id`: " + id);
    }
  }

  /**
   * Qualifies the given simple resource name prepending the simple name of this class.
   * <p>
   * Useful for referencing resources specific to this test unit.
   * </p>
   *
   * @see ResourceNames#localName(String, Class)
   */
  public String localName(String simpleName) {
    return ResourceNames.localName(simpleName, getClass());
  }

  /**
   * @implNote Method sealed, as it relies on {@link #outputPath(String)}.
   */
  @Override
  public final File outputFile(String name) {
    return TestEnvironment.super.outputFile(name);
  }

  @Override
  public synchronized Path outputPath(String name) {
    if (!outputDirInitialized) {
      /*
       * NOTE: The output directory of this test environment is initialized on demand as most unit
       * tests do not use it (it would be a waste if the tests which don't generate filesystem
       * output spawned empty directories all around!).
       *
       * The initialization flag is immediately set in order not to enter an infinite loop.
       */
      try {
        outputDirInitialized = true;
        resetDir(outputFile(EMPTY));
      } catch (Exception ex) {
        /*
         * NOTE: We catch any exception to ensure the initialization flag is reverted.
         */
        outputDirInitialized = false;
        throw (ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex));
      }
    }

    return ResourceNames.path(
        ResourceNames.isAbsolute(name) ? name : sqn(this) + SLASH + name,
        dirPath(DirId.OUTPUT), getClass());
  }

  /**
   * @implNote Method sealed, as it relies on {@link #resourcePath(String)}.
   */
  @Override
  public final File resourceFile(String name) {
    return TestEnvironment.super.resourceFile(name);
  }

  @Override
  public Path resourcePath(String name) {
    return ResourceNames.path(name, dirPath(DirId.RESOURCE), getClass());
  }

  /**
   * @implNote Method sealed, as it relies on {@link #resourceSrcPath(String)}.
   */
  @Override
  public final File resourceSrcFile(String name) {
    return TestEnvironment.super.resourceSrcFile(name);
  }

  @Override
  public Path resourceSrcPath(String name) {
    return ResourceNames.path(name, dirPath(DirId.RESOURCE_SRC), getClass());
  }

  /**
   * @implNote Method sealed, as it relies on {@link #typeSrcPath(Class)}.
   */
  @Override
  public final File typeSrcFile(Class<?> type) {
    return TestEnvironment.super.typeSrcFile(type);
  }

  @Override
  public Path typeSrcPath(Class<?> type) {
    Path ret;
    var filename = sqn(requireNonNull(type, "`type`")) + ".java";
    if (Files.exists(ret = ResourceNames.path(filename, dirPath(DirId.TYPE_SRC), type)))
      return ret;
    else if (Files.exists(ret = ResourceNames.path(filename, dirPath(DirId.MAIN_TYPE_SRC), type)))
      return ret;
    else
      throw new RuntimeException(new FileNotFoundException(
          String.format("Source file corresponding to `%s` NOT FOUND (search paths: %s, %s)",
              type.getName(), dirPath(DirId.TYPE_SRC), dirPath(DirId.MAIN_TYPE_SRC))));
  }

  /**
   * Asserts that log events matching the given criteria occurred, then resets logged events.
   */
  protected LoggingEvent assertLogged(Level level, Matcher<String> message) {
    var ret = assertLoggedAlso(level, message);
    resetLog();
    return ret;
  }

  /**
   * Asserts that log events matching the given criteria occurred.
   */
  protected LoggingEvent assertLoggedAlso(Level level, Matcher<String> message) {
    return logInterceptor.assertLogged(level, message);
  }

  /**
   * Asserts that no log event occurred.
   */
  protected void assertNotLogged() {
    assertNotLogged(null, null);
  }

  /**
   * Asserts that no log event matching the given criteria occurred, then resets logged events.
   */
  protected void assertNotLogged(@Nullable Level level, @Nullable Matcher<String> message) {
    assertNotLoggedAlso(level, message);
    resetLog();
  }

  /**
   * Asserts that no log event matching the given criteria occurred.
   */
  protected void assertNotLoggedAlso(@Nullable Level level, @Nullable Matcher<String> message) {
    logInterceptor.assertNotLogged(level, message);
  }

  /**
   * Resets logged events.
   */
  protected void resetLog() {
    logInterceptor.reset();
  }
}
