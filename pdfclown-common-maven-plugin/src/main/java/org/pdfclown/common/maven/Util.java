/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (Util.java) is part of pdfclown-common-maven-plugin module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.maven;

import static org.pdfclown.common.maven.util.MavenModels.pluginQName;

import org.apache.maven.project.MavenProject;

/**
 * @author Stefano Chizzolini
 */
class Util {
  static final String PLUGIN_KEY__PDFCLOWN_COMMON =
      "org.pdfclown:pdfclown-common-maven-plugin";

  static final String PLUGIN_KEY__JAR =
      "org.apache.maven.plugins:maven-jar-plugin";

  static final String PLUGIN_KEY__SOURCE =
      "org.apache.maven.plugins:maven-source-plugin";

  static String pluginTag(MavenProject project) {
    return pluginQName(project.getPlugin(PLUGIN_KEY__PDFCLOWN_COMMON));
  }
}
