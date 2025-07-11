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
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Exceptions.unsupported;
import static org.pdfclown.common.build.internal.util_.Objects.sqn;
import static org.pdfclown.common.build.internal.util_.Objects.typeOf;
import static org.pdfclown.common.build.internal.util_.Strings.SLASH;
import static org.pdfclown.common.build.internal.util_.io.Files.resetDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.pdfclown.common.build.internal.util_.annot.LazyNonNull;
import org.pdfclown.common.build.test.assertion.Test;
import org.pdfclown.common.build.test.assertion.TestEnvironment;
import org.pdfclown.common.build.util.io.ResourceNames;

/**
 * Automated testing unit.
 * <p>
 * Assumptions:
 * </p>
 * <ul>
 * <li>the filesystem is {@linkplain MavenBaseDirResolver organized according to Maven} (otherwise,
 * a custom filesystem mapping can be specified overriding {@link #__createEnv()} and passing a
 * {@link BaseDirResolver} via {@linkplain TestUnit.Environment#Environment(BaseDirResolver)
 * constructor})</li>
 * <li>tests are executed directly from plain filesystem directory (no jar embedding)</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class TestUnit implements Test {
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
   * {@link TestUnit} environment.
   *
   * @author Stefano Chizzolini
   */
  public class Environment implements TestEnvironment {
    private final BaseDirResolver baseDirResolver;

    /**
     * Test resources' target base directory.
     */
    private final Path resourceDir;
    {
      try {
        /*
         * NOTE: The classic way to retrieve the base directory would be via
         * `getClass().getResource("/")`, but, unfortunately, it works in non-modular (i.e.,
         * non-JPMS) projects only; the alternate solution applied here seems to reliably work in
         * modular as well as non-modular projects.
         */
        resourceDir = Path.of(typeOf(TestUnit.this).getProtectionDomain().getCodeSource()
            .getLocation().toURI());
      } catch (URISyntaxException ex) {
        throw runtime(ex) /* Should NEVER happen */;
      }
    }

    private boolean outputDirInitialized;

    public Environment() {
      this(BASE_DIR_RESOLVER__MAVEN);
    }

    public Environment(BaseDirResolver baseDirResolver) {
      this.baseDirResolver = baseDirResolver;
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
          throw unsupported("Unexpected `id`: " + id);
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
      return ResourceNames.localName(simpleName, typeOf(TestUnit.this));
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
          throw runtime(ex);
        }
      }

      return ResourceNames.path(
          ResourceNames.isAbsolute(name) ? name : sqn(TestUnit.this) + SLASH + name,
          dirPath(DirId.OUTPUT), typeOf(TestUnit.this));
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
      return ResourceNames.path(name, dirPath(DirId.RESOURCE), typeOf(TestUnit.this));
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
      return ResourceNames.path(name, dirPath(DirId.RESOURCE_SRC), typeOf(TestUnit.this));
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
      Class<?> topLevelType = requireNonNull(type, "`type`");
      while (topLevelType.getEnclosingClass() != null) {
        topLevelType = topLevelType.getEnclosingClass();
      }
      Path ret;
      var filename = topLevelType.getSimpleName() + ".java";
      if (Files.exists(ret = ResourceNames.path(filename, dirPath(DirId.TYPE_SRC), topLevelType)))
        return ret;
      else if (Files.exists(ret = ResourceNames.path(filename, dirPath(DirId.MAIN_TYPE_SRC),
          topLevelType)))
        return ret;
      else
        throw runtime(new FileNotFoundException(String.format(
            "Source file corresponding to `%s` NOT FOUND (search paths: %s, %s)",
            type.getName(), dirPath(DirId.TYPE_SRC), dirPath(DirId.MAIN_TYPE_SRC))));
    }
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

  private static final BaseDirResolver BASE_DIR_RESOLVER__MAVEN = new MavenBaseDirResolver();

  private @LazyNonNull @Nullable Environment env;

  protected TestUnit() {
  }

  @Override
  public synchronized Environment getEnv() {
    if (this.env == null) {
      this.env = __createEnv();
    }
    return this.env;
  }

  protected Environment __createEnv() {
    return new Environment();
  }
}
