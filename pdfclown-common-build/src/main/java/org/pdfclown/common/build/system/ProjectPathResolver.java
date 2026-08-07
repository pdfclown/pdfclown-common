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

import static org.pdfclown.common.build.internal.temp.util.Conditions.requireDirectory;
import static org.pdfclown.common.build.internal.temp.util.Objects.nonNull;
import static org.pdfclown.common.build.internal.temp.util.Objects.toStringWithValues;
import static org.pdfclown.common.build.internal.temp.util.io.Files.normal;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.pdfclown.common.build.internal.temp.util.io.ResourceNames;
import org.pdfclown.common.build.internal.temp.util.spi.ServiceProviderNotFoundException;
import org.pdfclown.common.build.spi.ProjectPathResolverProvider;
import org.pdfclown.common.util.spi.ServiceProvider;

/**
 * Project directory resolver.
 *
 * @author Stefano Chizzolini
 */
public abstract class ProjectPathResolver {
  private static final List<ProjectPathResolverProvider> providers =
      ServiceProvider.discover(ProjectPathResolverProvider.class).toList();

  /**
   * Gets the path resolver for a project.
   *
   * @param baseDir
   *          Project base directory.
   * @throws FileNotFoundException
   *           if {@code baseDir} does not exist.
   */
  public static ProjectPathResolver of(Path baseDir) throws FileNotFoundException {
    requireDirectory(baseDir);

    for (var provider : providers) {
      var ret = provider.getResolver(baseDir);
      if (ret != null)
        return ret;
    }
    throw new ServiceProviderNotFoundException(baseDir, ProjectPathResolverProvider.class,
        "Project type at the given directory UNKNOWN", null);
  }

  private final Map<ProjectDirId, Path> base = new HashMap<>();

  public ProjectPathResolver(Path baseDir) {
    base.put(ProjectDirId.BASE, normal(baseDir));
  }

  public Path resolve(ProjectDirId id) {
    return base.computeIfAbsent(id, $k -> nonNull(base.get(ProjectDirId.BASE))
        .resolve(relativePath($k)));
  }

  public Path resolve(ProjectDirId id, String sub) {
    return resolve(id).resolve(ResourceNames.rel(sub));
  }

  @Override
  public String toString() {
    return toStringWithValues(this, resolve(ProjectDirId.BASE));
  }

  /**
   * Gets the path associated to an ID, relative to the {@linkplain ProjectDirId#BASE base directory
   * of the project}.
   */
  protected abstract String relativePath(ProjectDirId id);
}
