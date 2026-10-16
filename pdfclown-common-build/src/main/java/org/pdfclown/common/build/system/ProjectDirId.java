/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ProjectDirId.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.system;

/**
 * Identifier of a base directory within the test environment.
 *
 * @author Stefano Chizzolini
 */
public enum ProjectDirId {
  /**
   * Root directory of the project.
   */
  ROOT,
  /**
   * Base directory of the project.
   */
  BASE,
  /**
   * Test output directory ({@code target/test-output} in Maven's Standard Directory Layout).
   */
  TEST_OUTPUT,
  /**
   * Test build directory ({@code target/test-classes} in Maven's Standard Directory Layout).
   */
  TEST_TARGET,
  /**
   * Directory of shared test resources (for example, {@code %ROOT%/src/test/shared-resources} in
   * Maven, where {@code %ROOT%} is the {@linkplain #ROOT root directory of the project}).
   *
   * @apiNote Useful when packaging shared resources in test artifacts is inconvenient, because they
   *          are large and consumed only locally to the project.
   */
  TEST_RESOURCE_SHARED,
  /**
   * Source directory of test resources ({@code src/test/resources} in Maven's Standard Directory
   * Layout).
   */
  TEST_RESOURCE_SOURCE,
  /**
   * Source directory of test types ({@code src/test/java} in Maven's Standard Directory Layout).
   */
  TEST_TYPE_SOURCE,
  /**
   * Production build directory ({@code target/classes} in Maven's Standard Directory Layout).
   */
  MAIN_TARGET,
  /**
   * Source directory of production resources ({@code src/main/resources} in Maven's Standard
   * Directory Layout).
   */
  MAIN_RESOURCE_SOURCE,
  /**
   * Source directory of production types ({@code src/main/java} in Maven's Standard Directory
   * Layout).
   */
  MAIN_TYPE_SOURCE
}
