/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BuildsTest.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.util.system;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.__test.BaseIT;
import org.pdfclown.common.build.internal.util_.XtIllegalArgumentException;
import org.pdfclown.common.build.test.assertion.TestEnvironment.DirId;

/**
 * @author Stefano Chizzolini
 */
class BuildsIT extends BaseIT {
  @Test
  void classpath() {
    /*
     * NOTE: Sibling project used because pdfclown-common-build is unsuitable for this test, since
     * all its dependencies belong both to the test and to the runtime scopes.
     */
    Path projectDir = Path.of("../pdfclown-common-util").toAbsolutePath().normalize();

    // Default (test) scope.
    List<Path> testClasspath = Builds.classpath(projectDir, null);

    assertThat(testClasspath.size(), is(not(0)));
    assertThat(testClasspath.get(0), is(projectDir.resolve("target/classes")));
    assertThat("In test classpath, test dependency 'junit' SHOULD exist",
        testClasspath.stream().anyMatch($ -> $.toString().contains("junit")), is(true));
    assertThat("In test classpath, runtime dependency 'classgraph' SHOULD exist",
        testClasspath.stream().anyMatch($ -> $.toString().contains("classgraph")), is(true));

    // Runtime scope.
    List<Path> runtimeClasspath = Builds.classpath(projectDir, "runtime");

    assertThat(runtimeClasspath.size(), is(not(0)));
    assertThat("Runtime dependencies SHOULD be less than test ones",
        runtimeClasspath.size() < testClasspath.size(), is(true));
    assertThat("Runtime dependencies SHOULD have contain the same main build directory as the "
        + "test ones", runtimeClasspath.get(0).equals(testClasspath.get(0)), is(true));
    assertThat("In runtime classpath, test dependency 'junit' SHOULD NOT exist",
        runtimeClasspath.stream().anyMatch($ -> $.toString().contains("junit")), is(false));
    assertThat("In runtime classpath, runtime dependency 'classgraph' SHOULD exist",
        runtimeClasspath.stream().anyMatch($ -> $.toString().contains("classgraph")), is(true));
  }

  @Test
  void classpath__failure() {
    {
      var throwable = assertThrows(RuntimeException.class, () -> Builds.classpath(Path.of(""),
          "gibberish"));
      assertThat(throwable.getMessage(), containsStringIgnoringCase("invalid scope"));
    }
    {
      var throwable = assertThrows(XtIllegalArgumentException.class,
          () -> Builds.classpath(Path.of("gibberish"), null));
      assertThat(throwable.getArgName(), is("projectDir"));
    }
  }

  @Test
  void mavenExecutable() {
    //noinspection DataFlowIssue : assertThat(..) supports nullable actual.
    assertThat(Builds.mavenExecutable(), is(not(nullValue())));
  }

  @Test
  void mavenHome() {
    //noinspection DataFlowIssue : assertThat(..) supports nullable actual.
    assertThat(Builds.mavenHome(), is(not(nullValue())));
  }

  @Test
  void projectArtifactId() {
    //noinspection DataFlowIssue : assertThat(..) supports nullable actual.
    assertThat(Builds.projectArtifactId(getEnv().dir(DirId.MAIN_TYPE_SRC)),
        is("pdfclown-common-build"));
  }
}
