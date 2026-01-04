/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (AbstractProjectDirResolver.java) is part of pdfclown-common-build module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.system;

import static java.nio.file.Files.isRegularFile;
import static org.pdfclown.common.util.Conditions.requireDirectory;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.io.Files.normal;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Project directory resolver.
 *
 * @author Stefano Chizzolini
 */
public abstract class ProjectPathResolver {
  /**
   * Gets the path resolver for a project.
   *
   * @param baseDir
   *          Project base directory.
   */
  public static ProjectPathResolver of(Path baseDir) throws FileNotFoundException {
    requireDirectory(baseDir);
    /*
     * TODO: implement `org.pdfclown.common.util.spi.ServiceProvider` to support additional project
     * types
     */
    if (isRegularFile(baseDir.resolve("pom.xml")))
      return new MavenPathResolver(baseDir);
    else
      throw wrongArg("baseDir", baseDir, "Project type UNKNOWN");
  }

  private final Map<ProjectDirId, Path> base = new HashMap<>();

  public ProjectPathResolver() {
    this(Path.of(EMPTY));
  }

  public ProjectPathResolver(Path baseDir) {
    base.put(ProjectDirId.BASE, normal(baseDir));
  }

  public Path resolve(ProjectDirId id) {
    return base.computeIfAbsent(id, $k -> base.get(ProjectDirId.BASE).resolve(relativePath($k)));
  }

  public Path resolve(ProjectDirId id, String sub) {
    return resolve(id).resolve(sub);
  }

  /**
   * Gets the path associated to an ID, relative to the {@linkplain ProjectDirId#BASE base directory
   * of the project}.
   */
  protected abstract String relativePath(ProjectDirId id);
}
