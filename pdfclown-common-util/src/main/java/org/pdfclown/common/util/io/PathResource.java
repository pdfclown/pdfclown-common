/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PathResource.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import java.nio.file.Path;
import org.pdfclown.common.util.annot.Immutable;

/**
 * Filesystem resource.
 *
 * @author Stefano Chizzolini
 */
@Immutable
public interface PathResource extends Resource {
  /**
   * Resource path.
   */
  Path getPath();
}
