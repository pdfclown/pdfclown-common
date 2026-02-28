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
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.build.test.assertion.match.PathEndsWith.pathEndsWith;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.__test.BaseTest;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.build.test.assertion.Assertions.Failure;
import org.pdfclown.common.build.util.io.ResourceNamesTest;

/**
 * @author Stefano Chizzolini
 */
class TestUnitTest extends BaseTest {
  static class SampleTest extends TestUnit {
  }

  static SampleTest sampleTest = new SampleTest();

  static Stream<Arguments> subName() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] name[0]: "/"
            new Failure("ArgumentException", "`name` (\"/\"): MUST NOT be root"),
            // [2] name[1]: "\\"
            new Failure("ArgumentException", "`name` (\"\\\\\"): MUST NOT be root"),
            // [3] name[2]: "/my/absolute/resource"
            "/my/absolute/TestUnitTest$SampleTest_resource",
            // [4] name[3]: "/my/absolute/resource/"
            "/my/absolute/resource/TestUnitTest$SampleTest_",
            // [5] name[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            "/my/other/deep/absolute/resource/TestUnitTest$SampleTest_",
            // [6] name[5]: ""
            new Failure("ArgumentException", "`name` (\"\"): MUST NOT be root"),
            // [7] name[6]: "my/relative/resource"
            "my/relative/TestUnitTest$SampleTest_resource",
            // [8] name[7]: "my/relative/resource/"
            "my/relative/resource/TestUnitTest$SampleTest_",
            // [9] name[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            "my/other/deep/relative/resource/TestUnitTest$SampleTest_"),
        // name
        ResourceNamesTest.NAMES);
  }

  @Test
  void getTestResourcePath() {
    assertThat(sampleTest.getTestResourcePath(""), pathEndsWith(Path.of(
        "pdfclown-common-build/target/test-classes/org/pdfclown/common/build/test/"
            + "_TestUnitTest$SampleTest")));
    assertThat(sampleTest.getTestResourcePath("myfile.txt"), pathEndsWith(Path.of(
        "pdfclown-common-build/target/test-classes/org/pdfclown/common/build/test/"
            + "_TestUnitTest$SampleTest/myfile.txt")));
  }

  @ParameterizedTest
  @MethodSource
  void subName(Expected<String> expected, String name) {
    assertParameterizedOf(
        () -> sampleTest.subName(name),
        expected,
        () -> new ExpectedGeneration(name));
  }
}