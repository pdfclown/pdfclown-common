/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ReleaseManagerTest.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.release;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pdfclown.common.build.__test.BaseTest;
import org.pdfclown.common.util.annot.InitNonNull;

/**
 * @author Stefano Chizzolini
 */
class ReleaseManagerTest extends BaseTest {
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

  @Test
  void new__devVersion() {
    combinationVerifier.verify(
        (releaseVersion) -> new ReleaseManager(getEnv().outputPath(EMPTY), releaseVersion)
            .getDevVersion(),
        List.of("releaseVersion"),
        // releaseVersion
        asList(
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
}