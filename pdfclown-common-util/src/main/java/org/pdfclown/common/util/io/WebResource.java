/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (WebResource.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Objects.isSameType;

import java.net.URI;
import org.pdfclown.common.util.annot.Immutable;

/**
 * Generic web resource.
 *
 * @author Stefano Chizzolini
 */
@Immutable
public class WebResource extends AbstractResource {
  private final URI uri;

  WebResource(String name, URI uri) {
    super(name);

    this.uri = requireNonNull(uri, "`uri`");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    else if (!isSameType(this, o))
      return false;

    var that = (WebResource) o;
    return this.uri.equals(that.uri);
  }

  @Override
  public URI getUri() {
    return uri;
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }

  @Override
  public String toString() {
    return uri.toString();
  }
}
