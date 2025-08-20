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

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.build.internal.util_.Strings.strNormToNull;
import static org.pdfclown.common.build.internal.util_.xml.Xmls.xml;
import static org.pdfclown.common.build.internal.util_.xml.Xmls.xpath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.function.Failable;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.util_.xml.Xmls;
import org.pdfclown.common.build.internal.util_.xml.Xmls.XPath;

/**
 * Build utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Builds {
  private static final Map<Path, String> projectArtifactIds = new HashMap<>();

  private static final String NS_PREFIX__POM = "pom";

  private static final XPath.Expression XPATH__ARTIFACT_ID = xpath(XPath.Namespaces.of()
      .register(NS_PREFIX__POM, Xmls.NS__POM))
          .compile(NS_PREFIX__POM + ":project/" + NS_PREFIX__POM + ":artifactId");

  /**
   * Gets the artifact ID the given path belongs to.
   *
   * @param path
   *          A position within the subproject.
   * @return {@code null}, if {@code path} is outside any project.
   * @implNote Currently supports Maven only.
   */
  public static @Nullable String artifactId(Path path) {
    Path dir = Files.isDirectory(path) ? path : path.getParent();
    while (dir != null) {
      if (projectArtifactIds.containsKey(dir))
        return projectArtifactIds.get(dir);

      var pomFile = dir.resolve("pom.xml");
      if (Files.exists(pomFile))
        return projectArtifactIds.computeIfAbsent(dir, Failable.asFunction(
            $k -> requireNonNull(strNormToNull(XPATH__ARTIFACT_ID.nodeValue(xml(pomFile))),
                () -> "`artifactId` NOT FOUND in " + pomFile)));

      dir = dir.getParent();
    }
    return null;
  }

  private Builds() {
  }
}
