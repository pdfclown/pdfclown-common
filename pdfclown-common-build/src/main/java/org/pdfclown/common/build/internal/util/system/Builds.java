/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Builds.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util.system;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.function.Failable;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.util.xml.Xmls;

/**
 * Build utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Builds {
  private static final Map<Path, String> projectArtifactIds = new HashMap<>();

  /**
   * Gets the artifact ID the given path belongs to.
   * <p>
   * NOTE: Currently supports Maven only.
   * </p>
   *
   * @param path
   *          Position within the subproject.
   * @return {@code null}, if {@code path} is outside any project.
   */
  public static @Nullable String artifactId(Path path) {
    Path dir = Files.isDirectory(path) ? path : path.getParent();
    while (dir != null) {
      if (projectArtifactIds.containsKey(dir))
        return projectArtifactIds.get(dir);

      var pomFile = dir.resolve("pom.xml");
      if (Files.exists(pomFile))
        return projectArtifactIds.computeIfAbsent(dir, Failable.asFunction(
            $k -> Xmls.filterNodeValue("project/artifactId", Xmls.xml(pomFile.toFile()))));

      dir = dir.getParent();
    }
    return null;
  }

  private Builds() {
  }
}
