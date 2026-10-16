/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TestUnit.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test;

import static org.pdfclown.common.build.internal.temp.util.Exceptions.runtime;
import static org.pdfclown.common.build.internal.temp.util.Objects.init;
import static org.pdfclown.common.build.internal.temp.util.Objects.toStringWithValues;
import static org.pdfclown.common.build.internal.temp.util.Strings.EMPTY;
import static org.pdfclown.common.build.internal.temp.util.io.Files.resetDirectory;

import java.nio.file.Path;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.pdfclown.common.build.internal.temp.util.annot.MonotonicNonNull;
import org.pdfclown.common.build.system.LogManager;
import org.pdfclown.common.build.system.MavenPathResolver;
import org.pdfclown.common.build.system.ProjectDirId;
import org.pdfclown.common.build.system.ProjectPathResolver;
import org.pdfclown.common.build.test.assertion.Test;
import org.pdfclown.common.build.test.assertion.TestEnvironment;
import org.pdfclown.common.build.test.assertion.Verifier;
import org.pdfclown.common.build.util.system.Builds;

/**
 * Automated testing unit.
 * <p>
 * Assumptions:
 * </p>
 * <ul>
 * <li>the filesystem is {@linkplain MavenPathResolver organized according to Maven} (otherwise, a
 * custom filesystem mapping can be specified overriding {@link #__createEnv()} and passing a
 * {@link ProjectPathResolver} via {@linkplain TestUnit.Environment#Environment(ProjectPathResolver)
 * constructor})</li>
 * <li>tests are executed directly from plain filesystem directory (no jar embedding)</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class TestUnit implements Test {
  /**
   * {@link TestUnit} environment.
   *
   * @author Stefano Chizzolini
   */
  public class Environment extends TestEnvironment {
    private final ProjectPathResolver pathResolver;

    private transient boolean outputDirInitialized;

    public Environment() {
      this(new MavenPathResolver(Builds.projectDir()));
    }

    public Environment(ProjectPathResolver pathResolver) {
      super(TestUnit.this);

      this.pathResolver = pathResolver;
    }

    @Override
    public Path dir(ProjectDirId id) {
      return pathResolver.resolve(id);
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
          resetDirectory(super.outputPath(EMPTY));
        } catch (Exception ex) {
          /*
           * NOTE: We catch any exception to ensure the initialization flag is reverted, so users
           * have a chance to fix it and retry.
           */
          outputDirInitialized = false;
          throw runtime(ex);
        }
      }
      return super.outputPath(name);
    }

    @Override
    public String toString() {
      return toStringWithValues(this, pathResolver);
    }
  }

  static {
    // Trigger test-related configuration loading as soon as any test unit starts:
    // - log management
    init(LogManager.class);
    // - test approvals
    init(Verifier.class);
  }

  private @MonotonicNonNull @Nullable Environment env;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private TestInfo testInfo;

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
   * Label of the current test.
   * <p>
   * Corresponds to its {@linkplain TestInfo#getDisplayName() display name}.
   * </p>
   */
  public String getTestLabel() {
    return testInfo.getDisplayName();
  }

  @Override
  public String getTestName() {
    return testInfo.getTestMethod().orElseThrow().getName();
  }

  protected Environment __createEnv() {
    return new Environment();
  }

  @BeforeEach
  void onEachBefore(TestInfo testInfo) {
    this.testInfo = testInfo;
  }
}
