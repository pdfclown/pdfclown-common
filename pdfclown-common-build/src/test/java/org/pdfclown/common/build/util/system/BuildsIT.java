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

import static java.nio.file.Files.isDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.pdfclown.common.util.Chars.UNDERSCORE;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Objects.sqn;
import static org.pdfclown.common.util.system.Processes.execute;
import static org.pdfclown.common.util.system.Processes.osCommand;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.__test.BaseIT;
import org.pdfclown.common.build.system.ProjectDirId;
import org.pdfclown.common.build.system.ProjectPathResolver;
import org.pdfclown.common.util.Ref;

/**
 * @author Stefano Chizzolini
 */
class BuildsIT extends BaseIT {
  @Test
  void classpath() throws IOException, InterruptedException {
    var projectBaseDir = getEnv().resourcePath(UNDERSCORE + sqn(this));

    compileProject(projectBaseDir);

    var projectPaths = ProjectPathResolver.of(projectBaseDir);

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
  void classpath__failure_baseDir() {
    var throwable = assertThrows(FileNotFoundException.class,
        () -> Builds.classpath(Path.of("gibberish"), null));
    assertThat(throwable.getMessage(), is("\"gibberish\" MISSING"));
  }

  @Test
  void classpath__failure_scope() throws IOException, InterruptedException {
    var projectBaseDir = getEnv().resourcePath(UNDERSCORE + sqn(this));

    compileProject(projectBaseDir);

    var throwable = assertThrows(RuntimeException.class, () -> Builds.classpath(
        projectBaseDir, "gibberish"));
    assertThat(throwable.getMessage(), containsStringIgnoringCase("invalid scope"));
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

  /**
   * Compiles the given project in order to make its classes available on classpath.
   */
  private void compileProject(Path baseDir) throws IOException, InterruptedException {
    var paths = ProjectPathResolver.of(baseDir);
    if (!isDirectory(paths.resolve(ProjectDirId.MAIN_TARGET))) {
      var out = new Ref<String>();
      if (execute(osCommand(Builds.mavenExecutable()
          + " compile -q -Dspotless.apply.skip"), baseDir, out) != 0)
        throw runtime("""
            Compilation of {} FAILED:
            {}""", baseDir, out.get());
    }
  }
}
