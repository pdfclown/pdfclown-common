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
import static org.pdfclown.common.build.test.assertion.Assertions.Argument.qnamed;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.util.Objects.literal;
import static org.pdfclown.common.util.net.Uris.uri;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.__test.BaseTest;
import org.pdfclown.common.build.release.ReleaseManager;
import org.pdfclown.common.build.system.ProjectDirId;
import org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedConversions;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.build.test.assertion.Assertions.Failure;
import org.pdfclown.common.build.test.assertion.TestEnvironment;

/**
 * @author Stefano Chizzolini
 */
class TestUnitTest extends BaseTest {
  static class SampleTest extends TestUnit {
  }

  @Nested
  class TestEnvironmentTest {
    private static final Pattern PATTERN__PROJECT_PATH = Pattern.compile(
        "\"" + Pattern.quote(PROJECT_DIR.toString()) + ".(.+?)\"");

    static Stream<Arguments> localName() {
      return argumentsStream(
          cartesian(),
          // expected
          asList(
              // [1] name[0]: "/"
              "/",
              // [2] name[1]: "/my/absolute/resource.html"
              "/my/absolute/resource.html",
              // [3] name[2]: ""
              "/org/pdfclown/common/build/test/_TestUnitTest$SampleTest",
              // [4] name[3]: "my/relative/resource.html"
              "/org/pdfclown/common/build/test/_TestUnitTest$SampleTest/my/relative/resource.html"),
          // name
          NAMES);
    }

    static Stream<Arguments> outputPath() {
      return argumentsStream(
          prepareExpectedPathDeserializer(cartesian()),
          // expected
          asList(
              // [1] name[0]: "/"
              "target/test-output",
              // [2] name[1]: "/my/absolute/resource.html"
              "target/test-output/my/absolute/resource.html",
              // [3] name[2]: ""
              "target/test-output/org/pdfclown/common/build/test/_TestUnitTest$SampleTest",
              // [4] name[3]: "my/relative/resource.html"
              "target/test-output/org/pdfclown/common/build/test/_TestUnitTest$SampleTest/my/relative/resource.html"),
          // name
          NAMES);
    }

    static Stream<Arguments> resourcePath() {
      return argumentsStream(
          prepareExpectedPathDeserializer(cartesian()),
          // expected
          asList(
              // [1] name[0]: "/"
              "target/test-classes",
              // [2] name[1]: "/my/absolute/resource.html"
              "target/test-classes/my/absolute/resource.html",
              // [3] name[2]: ""
              "target/test-classes/org/pdfclown/common/build/test/_TestUnitTest$SampleTest",
              // [4] name[3]: "my/relative/resource.html"
              "target/test-classes/org/pdfclown/common/build/test/_TestUnitTest$SampleTest/my/relative/resource.html"),
          // name
          NAMES);
    }

    static Stream<Arguments> resourceSrcPath() {
      return argumentsStream(
          prepareExpectedPathDeserializer(cartesian()),
          // expected
          asList(
              // [1] name[0]: "/"
              "src/test/resources",
              // [2] name[1]: "/my/absolute/resource.html"
              "src/test/resources/my/absolute/resource.html",
              // [3] name[2]: ""
              "src/test/resources/org/pdfclown/common/build/test/_TestUnitTest$SampleTest",
              // [4] name[3]: "my/relative/resource.html"
              "src/test/resources/org/pdfclown/common/build/test/_TestUnitTest$SampleTest/my/relative/resource.html"),
          // name
          NAMES);
    }

    static Stream<Arguments> typeSrcPath() {
      return argumentsStream(
          prepareExpectedPathDeserializer(cartesian()),
          // expected
          asList(
              // [1] type[0]: org.pdfclown.common.build.test.assertion.Test. . .
              "src/main/java/org/pdfclown/common/build/test/assertion/TestEnvironment.java",
              // [2] type[1]: org.pdfclown.common.build.test.TestUnitTest
              "src/test/java/org/pdfclown/common/build/test/TestUnitTest.java",
              // [3] type[2]: org.pdfclown.common.build.release.ReleaseManager
              "src/main/java/org/pdfclown/common/build/release/ReleaseManager.java",
              // [4] type[3]: String
              new Failure("RuntimeException",
                  "Source file corresponding to String NOT FOUND (search paths: \"src/test/java\", \"src/main/java\")")),
          // name
          asList(
              TestEnvironment.class,
              TestUnitTest.class,
              ReleaseManager.class,
              String.class));
    }

    @ParameterizedTest
    @MethodSource
    void localName(Expected<String> expected, String name) {
      assertParameterizedOf(
          () -> sampleTest.getEnv().localName(name),
          expected,
          () -> new ExpectedGeneration<>(name));
    }

    @ParameterizedTest
    @MethodSource
    void outputPath(Expected<Path> expected, String name) {
      assertParameterizedOf(
          () -> sampleTest.getEnv().outputPath(name),
          expected,
          () -> prepareExpectedPathSerializer(new ExpectedGeneration<>(name)));
    }

    @ParameterizedTest
    @MethodSource
    void resourcePath(Expected<Path> expected, String name) {
      assertParameterizedOf(
          () -> sampleTest.getEnv().resourcePath(name),
          expected,
          () -> prepareExpectedPathSerializer(new ExpectedGeneration<>(name)));
    }

    @ParameterizedTest
    @MethodSource
    void resourceSrcPath(Expected<Path> expected, String name) {
      assertParameterizedOf(
          () -> sampleTest.getEnv().resourceSrcPath(name),
          expected,
          () -> prepareExpectedPathSerializer(new ExpectedGeneration<>(name)));
    }

    @ParameterizedTest
    @MethodSource
    void typeSrcPath(Expected<Path> expected, Class<?> type) {
      assertParameterizedOf(
          () -> sampleTest.getEnv().typeSrcPath(type),
          expected,
          () -> prepareExpectedPathSerializer(new ExpectedGeneration<>(type))
              .addFailureMessageNormalizer(RuntimeException.class, $ -> {
                /*
                 * NOTE: Here we normalize paths in the failure message purging their
                 * system-specific project directory in order to make them portable across the
                 * testing systems.
                 */
                var b = new StringBuilder();
                var m = PATTERN__PROJECT_PATH.matcher($);
                while (m.find()) {
                  m.appendReplacement(b, literal(uri(m.group(1))));
                }
                m.appendTail(b);
                return b.toString();
              }));
    }
  }

  public static final List<Named<String>> NAMES = asList(
      qnamed("Absolute root",
          "/"),
      qnamed("Absolute name",
          "/my/absolute/resource.html"),
      qnamed("Relative root",
          ""),
      qnamed("Relative name",
          "my/relative/resource.html"));

  static final SampleTest sampleTest = new SampleTest();

  static final Path PROJECT_DIR = sampleTest.getEnv().dir(ProjectDirId.BASE);

  static ArgumentsStreamStrategy prepareExpectedPathDeserializer(ArgumentsStreamStrategy strategy) {
    return ExpectedConversions.preparePathDeserializer(strategy, PROJECT_DIR);
  }

  static ExpectedGeneration<Path> prepareExpectedPathSerializer(
      ExpectedGeneration<Path> generation) {
    return ExpectedConversions.preparePathSerializer(generation, PROJECT_DIR);
  }

  @Test
  @DisplayName("getTestMethodName!")
  void getTestMethodName_() {
    assertThat(getTestMethodName(), is("getTestMethodName_"));
  }

  @Test
  @DisplayName("getTestName!")
  void getTestName_() {
    assertThat(getTestName(), is("getTestName!"));
  }
}