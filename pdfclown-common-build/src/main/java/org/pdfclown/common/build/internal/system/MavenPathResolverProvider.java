/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (MavenPathResolverProvider.java) is part of pdfclown-common-build module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.system;

import static java.nio.file.Files.isRegularFile;
import static org.pdfclown.common.build.system.MavenPathResolver.FILENAME__POM;

import java.nio.file.Path;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.spi.ProjectPathResolverProvider;
import org.pdfclown.common.build.system.MavenPathResolver;
import org.pdfclown.common.build.system.ProjectPathResolver;

/**
 * {@link MavenPathResolver} provider.
 *
 * @author Stefano Chizzolini
 */
public class MavenPathResolverProvider implements ProjectPathResolverProvider {
  @Override
  public int getPriority() {
    return 0;
  }

  @Override
  public @Nullable ProjectPathResolver getResolver(Path baseDir) {
    return isRegularFile(baseDir.resolve(FILENAME__POM)) ? new MavenPathResolver(baseDir) : null;
  }

  @Override
  public boolean isAvailable() {
    return true;
  }
}
