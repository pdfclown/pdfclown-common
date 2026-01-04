/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ReleaseManagerTest.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.release;

import static java.util.Arrays.asList;
import static java.util.List.of;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.pdfclown.common.build.__test.BaseTest;
import org.pdfclown.common.build.meta.SemVer1;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.util.annot.InitNonNull;

/**
 * @author Stefano Chizzolini
 */
class ReleaseManagerTest extends BaseTest {
  @SuppressWarnings("NotNullFieldNotInitialized")
  static @InitNonNull MockedStatic<ReleaseManager> releaseManagerMock;

  @AfterAll
  static void onAllAfter() {
    releaseManagerMock.close();
  }

  @BeforeAll
  static void onAllBefore() {
    releaseManagerMock = mockStatic(ReleaseManager.class, CALLS_REAL_METHODS);
    releaseManagerMock
        .when(ReleaseManager::checkOS)
        .thenAnswer($ -> null /*
                               * Isolates tests from actual test environment (otherwise, on non-Unix
                               * systems they would fail)
                               */);
  }

  Stream<Arguments> _create__devVersion() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] releaseVersion[0]: "0.1.0"
            "0.1.1-SNAPSHOT",
            // [2] releaseVersion[1]: "0.1.5"
            "0.1.6-SNAPSHOT",
            // [3] releaseVersion[2]: "1.0.0"
            "1.0.1-SNAPSHOT",
            // [4] releaseVersion[3]: "1.0.9"
            "1.0.10-SNAPSHOT",
            // [5] releaseVersion[4]: "1.0.0-beta"
            "1.0.0-beta-1-SNAPSHOT",
            // [6] releaseVersion[5]: "1.0.0-beta1"
            "1.0.0-beta2-SNAPSHOT",
            // [7] releaseVersion[6]: "1.0.0-beta-3"
            "1.0.0-beta-4-SNAPSHOT",
            // [8] releaseVersion[7]: "2.0.0-rc"
            "2.0.0-rc-1-SNAPSHOT",
            // [9] releaseVersion[8]: "2.0.0-rc-1"
            "2.0.0-rc-2-SNAPSHOT",
            // [10] releaseVersion[9]: "2.0.0-rc9"
            "2.0.0-rc10-SNAPSHOT"),
        // releaseVersion
        of(
            "0.1.0",
            "0.1.5",
            "1.0.0",
            "1.0.9",
            "1.0.0-beta",
            "1.0.0-beta1",
            "1.0.0-beta-3",
            "2.0.0-rc",
            "2.0.0-rc-1",
            "2.0.0-rc9"));
  }

  @ParameterizedTest
  @MethodSource
  void _create__devVersion(Expected<String> expected, String releaseVersion) {
    assertParameterizedOf(
        () -> new ReleaseManager(getEnv().outputPath(EMPTY), SemVer1.of(releaseVersion))
            .getDevVersion(),
        expected,
        () -> new ExpectedGeneration(releaseVersion));
  }
}