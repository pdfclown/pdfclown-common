/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ProjectPathResolverProvider.java) is part of pdfclown-common-build module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.spi;

import java.nio.file.Path;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.system.ProjectPathResolver;
import org.pdfclown.common.util.spi.ServiceProvider;

/**
 * {@link ProjectPathResolver} provider.
 *
 * @author Stefano Chizzolini
 */
public interface ProjectPathResolverProvider extends ServiceProvider {
  /**
   * Gets the {@link ProjectPathResolver} corresponding to the project type.
   *
   * @param baseDir
   *          Base project directory.
   * @return {@code null}, if this provider doesn't support the project type at {@code baseDir}.
   */
  @Nullable
  ProjectPathResolver getResolver(Path baseDir);
}
