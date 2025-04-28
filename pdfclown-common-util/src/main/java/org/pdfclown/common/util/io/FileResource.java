/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (FileResource.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static org.pdfclown.common.util.io.Files.urlOf;

import java.net.URL;
import java.nio.file.Path;

/**
 * Plain filesystem resource.
 *
 * @author Stefano Chizzolini
 */
public class FileResource extends AbstractResource implements PathResource {
  private final Path path;

  FileResource(String name, Path path) {
    super(name);

    this.path = path;
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public URL getUrl() {
    return urlOf(path);
  }
}
