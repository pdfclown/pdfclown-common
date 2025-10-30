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

import static java.nio.file.Files.exists;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Exceptions.unexpected;
import static org.pdfclown.common.build.internal.util_.Objects.asTopLevelType;
import static org.pdfclown.common.build.internal.util_.Objects.sqn;
import static org.pdfclown.common.build.internal.util_.ParamMessage.ARG;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;
import static org.pdfclown.common.build.internal.util_.io.Files.FILE_EXTENSION__JAVA;
import static org.pdfclown.common.build.internal.util_.io.Files.normal;
import static org.pdfclown.common.build.internal.util_.io.Files.resetDirectory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.pdfclown.common.build.internal.util_.annot.InitNonNull;
import org.pdfclown.common.build.internal.util_.annot.LazyNonNull;
import org.pdfclown.common.build.test.assertion.Test;
import org.pdfclown.common.build.test.assertion.TestEnvironment;
import org.pdfclown.common.build.test.assertion.TestEnvironment.DirId;
import org.pdfclown.common.build.util.io.ResourceNames;

/**
 * Automated testing unit.
 * <p>
 * Assumptions:
 * </p>
 * <ul>
 * <li>the filesystem is {@linkplain MavenDirResolver organized according to Maven} (otherwise, a
 * custom filesystem mapping can be specified overriding {@link #__createEnv()} and passing a
 * {@link DirResolver} via {@linkplain TestUnit.Environment#Environment(DirResolver)
 * constructor})</li>
 * <li>tests are executed directly from plain filesystem directory (no jar embedding)</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class TestUnit implements Test {
  /**
   * {@link DirResolver} base implementation.
   *
   * @author Stefano Chizzolini
   */
  public abstract static class AbstractDirResolver implements DirResolver {
    private final Map<DirId, Path> base = new HashMap<>();

    public AbstractDirResolver() {
      this(Path.of(EMPTY));
    }

    public AbstractDirResolver(Path baseDir) {
      base.put(DirId.BASE, normal(baseDir));
    }

    @Override
    public Path resolve(DirId id) {
      return base.computeIfAbsent(id, $k -> base.get(DirId.BASE).resolve(relativePath($k)));
    }

    /**
     * Gets the path associated to an ID, relative to the {@linkplain DirId#BASE base directory of
     * the project}.
     */
    protected abstract String relativePath(DirId id);
  }

  /**
   * Project directory resolver.
   *
   * @author Stefano Chizzolini
   */
  @FunctionalInterface
  public interface DirResolver {
    Path resolve(DirId id);
  }

  /**
   * {@link TestUnit} environment.
   *
   * @author Stefano Chizzolini
   */
  public class Environment implements TestEnvironment {
    private final DirResolver dirResolver;

    private boolean outputDirInitialized;

    public Environment() {
      this(new MavenDirResolver());
    }

    public Environment(DirResolver dirResolver) {
      this.dirResolver = dirResolver;
    }

    @Override
    public Path dir(DirId id) {
      return dirResolver.resolve(id);
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
          resetDirectory(outputPath(EMPTY));
        } catch (Exception ex) {
          /*
           * NOTE: We catch any exception to ensure the initialization flag is reverted.
           */
          outputDirInitialized = false;
          throw runtime(ex);
        }
      }

      return ResourceNames.path(ResourceNames.isAbs(name) ? name
          : ResourceNames.based(ResourceNames.name(sqn(TestUnit.this), name), TestUnit.this),
          dir(DirId.OUTPUT));
    }

    @Override
    public Path resourcePath(String name) {
      return ResourceNames.path(ResourceNames.based(name, TestUnit.this), dir(DirId.TARGET));
    }

    @Override
    public Path resourceSrcPath(String name) {
      return ResourceNames.path(ResourceNames.based(name, TestUnit.this),
          dir(DirId.RESOURCE_SOURCE));
    }

    @Override
    public Path typeSrcPath(Class<?> type) {
      var name = ResourceNames.based(
          requireNonNull(asTopLevelType(type), "`type`").getSimpleName() + FILE_EXTENSION__JAVA,
          type);
      Path ret;
      if (exists(ret = ResourceNames.path(name, dir(DirId.TYPE_SOURCE)))
          || exists(ret = ResourceNames.path(name, dir(DirId.MAIN_TYPE_SOURCE))))
        return ret;
      else
        throw runtime("Source file corresponding to " + ARG + " NOT FOUND "
            + "(search paths: " + ARG + ", " + ARG + ")", type, dir(DirId.TYPE_SOURCE),
            dir(DirId.MAIN_TYPE_SOURCE));
    }
  }

  /**
   * Filesystem mapping for <a href=
   * "https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html">Maven's
   * Standard Directory Layout</a>.
   *
   * @author Stefano Chizzolini
   */
  public static class MavenDirResolver extends AbstractDirResolver {
    public MavenDirResolver() {
    }

    public MavenDirResolver(Path baseDir) {
      super(baseDir);
    }

    @Override
    protected String relativePath(DirId id) {
      switch (id) {
        case BASE:
          return EMPTY;
        case MAIN_TARGET:
          return "target/classes";
        case MAIN_TYPE_SOURCE:
          return "src/main/java";
        case OUTPUT:
          return "target/test-output";
        case TARGET:
          return "target/test-classes";
        case TYPE_SOURCE:
          return "src/test/java";
        case RESOURCE_SOURCE:
          return "src/test/resources";
        default:
          throw unexpected(id);
      }
    }
  }

  private @LazyNonNull @Nullable Environment env;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull TestInfo testInfo;

  protected TestUnit() {
  }

  @Override
  public synchronized Environment getEnv() {
    if (this.env == null) {
      this.env = __createEnv();
    }
    return this.env;
  }

  /**
   * Name of the current test method.
   */
  public String getTestMethodName() {
    return testInfo.getTestMethod().orElseThrow().getName();
  }

  /**
   * Name of the current test.
   * <p>
   * Corresponds to JUnit's display name.
   * </p>
   */
  public String getTestName() {
    return testInfo.getDisplayName();
  }

  protected Environment __createEnv() {
    return new Environment();
  }

  @BeforeEach
  void onEachBefore(TestInfo testInfo) {
    this.testInfo = testInfo;
  }
}
