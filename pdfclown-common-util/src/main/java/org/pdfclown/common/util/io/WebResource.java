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
  public String asString() {
    return uri.toString();
  }

  /**
   * @implNote Marked as final to enforce equivalence symmetry.
   */
  @Override
  public final boolean equals(Object o) {
    return this == o || (o instanceof WebResource that
        && this.uri.equals(that.uri));
  }

  @Override
  public URI getUri() {
    return uri;
  }

  /**
   * @implNote Marked as final to enforce equivalence symmetry.
   */
  @Override
  public final int hashCode() {
    return uri.hashCode();
  }
}
