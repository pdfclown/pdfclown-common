/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BuildsTest.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util.system;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.__test.BaseTest;
import org.pdfclown.common.build.test.assertion.TestEnvironment.DirId;

/**
 * @author Stefano Chizzolini
 */
class BuildsTest extends BaseTest {
  @Test
  void artifactId() {
    //noinspection DataFlowIssue : assertThat(..) supports nullable actual.
    assertThat(Builds.artifactId(getEnv().dir(DirId.MAIN_TYPE_SRC)),
        is("pdfclown-common-build"));
  }
}
