/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TestUnitTest.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.__test.BaseTest;
import org.pdfclown.common.build.release.ReleaseManager;
import org.pdfclown.common.build.system.ProjectDirId;
import org.pdfclown.common.build.test.assertion.CombinationVerifier;
import org.pdfclown.common.build.test.assertion.TestEnvironment;
import org.pdfclown.common.build.test.assertion.Verifiers;

/**
 * @author Stefano Chizzolini
 */
class TestUnitTest extends BaseTest {
  static class SampleTest extends TestUnit {
  }

  @Nested
  class TestEnvironmentTest {
    private static final CombinationVerifier pathVerifier = Verifiers.pathVerifier(
        combinationVerifier, PROJECT_DIR);

    @Test
    void localName() {
      combinationVerifier.verify(
          (name) -> sampleTest.getEnv().localName(name),
          List.of("name"),
          // name
          NAMES);
    }

    @Test
    void outputPath() {
      pathVerifier.verify(
          (name) -> sampleTest.getEnv().outputPath(name),
          List.of("name"),
          // name
          NAMES);
    }

    @Test
    void resourcePath() {
      pathVerifier.verify(
          (name) -> sampleTest.getEnv().resourcePath(name),
          List.of("name"),
          // name
          NAMES);
    }

    @Test
    void resourceSrcPath() {
      pathVerifier.verify(
          (name) -> sampleTest.getEnv().resourceSrcPath(name),
          List.of("name"),
          // name
          NAMES);
    }

    @Test
    void typeSrcPath() {
      pathVerifier.verify(
          (type) -> sampleTest.getEnv().typeSrcPath(type),
          List.of("type"),
          // type
          asList(
              TestEnvironment.class,
              TestUnitTest.class,
              ReleaseManager.class,
              String.class));
    }
  }

  static final SampleTest sampleTest = new SampleTest();

  static final Path PROJECT_DIR = sampleTest.getEnv().dir(ProjectDirId.BASE);

  public static final List<String> NAMES = asList(
      // Absolute root
      "/",
      // Absolute name
      "/my/absolute/resource.html",
      // Relative root
      "",
      // Relative name
      "my/relative/resource.html");

  @Test
  @DisplayName("getTestLabel!")
  void getTestLabel_() {
    assertThat(getTestLabel(), is("getTestLabel!"));
  }

  @Test
  @DisplayName("getTestName!")
  void getTestName_() {
    assertThat(getTestName(), is("getTestName_"));
  }
}