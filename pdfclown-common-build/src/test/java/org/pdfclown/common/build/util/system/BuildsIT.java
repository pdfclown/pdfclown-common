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
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.pdfclown.common.build.internal.util_.Chars.LF;
import static org.pdfclown.common.build.internal.util_.Chars.UNDERSCORE;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Objects.sqn;
import static org.pdfclown.common.build.internal.util_.system.Processes.execute;
import static org.pdfclown.common.build.internal.util_.system.Processes.osCommand;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.__test.BaseIT;
import org.pdfclown.common.build.internal.util_.Ref;
import org.pdfclown.common.build.system.ProjectDirId;
import org.pdfclown.common.build.system.ProjectPathResolver;

/**
 * @author Stefano Chizzolini
 */
class BuildsIT extends BaseIT {
  @Test
  void classpath() throws IOException, InterruptedException {
    var projectPaths = ProjectPathResolver.of(getEnv().resourcePath(UNDERSCORE + sqn(this)));
    var projectBaseDir = projectPaths.resolve(ProjectDirId.BASE);

    // Compile the sample project, in order to make its classes available on classpath!
    {
      var out = new Ref<String>();
      if (execute(osCommand(Builds.mavenExecutable()
          + " compile -q -Dspotless.apply.skip"), projectBaseDir, out) != 0)
        throw runtime("Compilation of " + projectBaseDir + " FAILED" + LF + out.get());
    }

    // Default (test) scope.
    List<Path> testClasspath = Builds.classpath(projectBaseDir, null);

    assertThat(testClasspath.size(), is(not(0)));
    assertThat(testClasspath.get(0), is(projectPaths.resolve(ProjectDirId.MAIN_TARGET)));
    assertThat("In test classpath, test dependency 'junit' SHOULD exist",
        testClasspath.stream().anyMatch($ -> $.toString().contains("junit")), is(true));
    assertThat("In test classpath, runtime dependency 'classgraph' SHOULD exist",
        testClasspath.stream().anyMatch($ -> $.toString().contains("classgraph")), is(true));

    // Runtime scope.
    List<Path> runtimeClasspath = Builds.classpath(projectBaseDir, "runtime");

    assertThat(runtimeClasspath.size(), is(not(0)));
    assertThat("Runtime dependencies SHOULD be less than test ones",
        runtimeClasspath.size() < testClasspath.size(), is(true));
    assertThat("Runtime dependencies SHOULD contain the same main build directory as the "
        + "test ones", runtimeClasspath.get(0).equals(testClasspath.get(0)), is(true));
    assertThat("In runtime classpath, test dependency 'junit' SHOULD NOT exist",
        runtimeClasspath.stream().anyMatch($ -> $.toString().contains("junit")), is(false));
    assertThat("In runtime classpath, runtime dependency 'classgraph' SHOULD exist",
        runtimeClasspath.stream().anyMatch($ -> $.toString().contains("classgraph")), is(true));
  }

  @Test
  void classpath__failure() {
    {
      var throwable = assertThrows(RuntimeException.class,
          () -> Builds.classpath(Path.of("gibberish"), null));
      assertThat(throwable.getCause(), isA(FileNotFoundException.class));
      assertThat(throwable.getCause().getMessage(), endsWith("gibberish/pom.xml\" MISSING"));
    }
    {
      var throwable = assertThrows(RuntimeException.class, () -> Builds.classpath(
          getEnv().dir(ProjectDirId.BASE), "gibberish"));
      assertThat(throwable.getMessage(), containsStringIgnoringCase("invalid scope"));
    }
  }

  @Test
  void mavenExecutable() {
    assertThat(Builds.mavenExecutable(), is(not(nullValue())));
  }

  @Test
  void mavenHome() {
    assertThat(Builds.mavenHome(), is(not(nullValue())));
  }

  @Test
  @SuppressWarnings("DataFlowIssue" /* assertThat(..) supports nullable actual */)
  void projectArtifactId() {
    assertThat(Builds.projectArtifactId(getEnv().dir(ProjectDirId.MAIN_TYPE_SOURCE)),
        is("pdfclown-common-build"));
  }
}
