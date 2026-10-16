/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (MavenDirResolver.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.system;

import static java.nio.file.Files.isRegularFile;
import static org.pdfclown.common.build.internal.temp.util.Objects.nonNull;
import static org.pdfclown.common.build.internal.temp.util.Strings.EMPTY;
import static org.pdfclown.common.build.util.system.Builds.projectRootDir;

import java.nio.file.Path;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.spi.ProjectPathResolverProvider;

/**
 * Filesystem mapping for <a href=
 * "https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html">Maven's
 * Standard Directory Layout</a>.
 *
 * @author Stefano Chizzolini
 */
public class MavenPathResolver extends ProjectPathResolver {
  /**
   * {@link MavenPathResolver} provider.
   *
   * @author Stefano Chizzolini
   */
  public static class Provider implements ProjectPathResolverProvider {
    @Override
    public int getPriority() {
      return 0;
    }

    @Override
    public @Nullable ProjectPathResolver getResolver(Path baseDir) {
      return isRegularFile(baseDir.resolve("pom.xml")) ? new MavenPathResolver(baseDir) : null;
    }

    @Override
    public boolean isAvailable() {
      return true;
    }
  }

  /**
   * Maven project filename.
   */
  public static final String FILENAME__POM = "pom.xml";

  public MavenPathResolver(Path baseDir) {
    super(baseDir);
  }

  @Override
  protected String relativePath(ProjectDirId id) {
    return switch (id) {
      case BASE -> EMPTY;
      case MAIN_RESOURCE_SOURCE -> "src/main/resources";
      case MAIN_TARGET -> "target/classes";
      case MAIN_TYPE_SOURCE -> "src/main/java";
      /*
       * TODO: The call to `projectRootDir` causes a circular dependence with
       * `org.pdfclown.common.build.util.system.Builds`; at the moment this benign design flaw is
       * not worth the pain of refactoring, but should be fixed in the future as the library
       * matures.
       */
      case ROOT -> nonNull(projectRootDir(resolve(ProjectDirId.BASE))).toString();
      case TEST_OUTPUT -> "target/test-output";
      case TEST_RESOURCE_SHARED -> resolve(ProjectDirId.ROOT, "src/test/shared-resources")
          .toString();
      case TEST_RESOURCE_SOURCE -> "src/test/resources";
      case TEST_TARGET -> "target/test-classes";
      case TEST_TYPE_SOURCE -> "src/test/java";
    };
  }
}
