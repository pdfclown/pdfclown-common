/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (MavenDirResolver.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.system;

import static org.pdfclown.common.util.Strings.EMPTY;

import java.nio.file.Path;

/**
 * Filesystem mapping for <a href=
 * "https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html">Maven's
 * Standard Directory Layout</a>.
 *
 * @author Stefano Chizzolini
 */
public class MavenPathResolver extends ProjectPathResolver {
  public MavenPathResolver() {
  }

  public MavenPathResolver(Path baseDir) {
    super(baseDir);
  }

  @Override
  protected String relativePath(ProjectDirId id) {
    return switch (id) {
      case BASE -> EMPTY;
      case MAIN_TARGET -> "target/classes";
      case MAIN_TYPE_SOURCE -> "src/main/java";
      case MAIN_RESOURCE_SOURCE -> "src/main/resources";
      case TEST_OUTPUT -> "target/test-output";
      case TEST_TARGET -> "target/test-classes";
      case TEST_TYPE_SOURCE -> "src/test/java";
      case TEST_RESOURCE_SOURCE -> "src/test/resources";
    };
  }
}
