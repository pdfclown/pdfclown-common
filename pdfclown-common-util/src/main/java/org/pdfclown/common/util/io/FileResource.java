/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FileResource.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Objects.isSameType;
import static org.pdfclown.common.util.net.Uris.uri;

import java.net.URI;
import java.nio.file.Path;
import org.pdfclown.common.util.annot.Immutable;

/**
 * Plain filesystem resource.
 *
 * @author Stefano Chizzolini
 */
@Immutable
public class FileResource extends AbstractResource implements PathResource {
  private final Path path;

  FileResource(String name, Path path) {
    super(name);

    this.path = requireNonNull(path, "`path`");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    else if (!isSameType(this, o))
      return false;

    var that = (FileResource) o;
    return this.path.equals(that.path);
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public URI getUri() {
    return uri(path);
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }

  @Override
  public String toString() {
    return path.toString();
  }
}
