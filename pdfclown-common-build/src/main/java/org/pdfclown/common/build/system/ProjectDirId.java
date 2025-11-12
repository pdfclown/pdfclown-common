/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

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
   * Base directory of the project.
   */
  BASE,
  /**
   * Base directory of test output (typically, {@code "target/test-output"}).
   */
  TEST_OUTPUT,
  /**
   * Test build directory (typically, {@code "target/test-classes"}).
   */
  TEST_TARGET,
  /**
   * Source directory of test resources (typically, {@code "src/test/resources"}).
   */
  TEST_RESOURCE_SOURCE,
  /**
   * Source directory of test types (typically, {@code "src/test/java"}).
   */
  TEST_TYPE_SOURCE,
  /**
   * Main build directory (typically, {@code "target/classes"}).
   */
  MAIN_TARGET,
  /**
   * Source directory of main resources (typically, {@code "src/main/resources"}).
   */
  MAIN_RESOURCE_SOURCE,
  /**
   * Source directory of main types (typically, {@code "src/main/java"}).
   */
  MAIN_TYPE_SOURCE
}
