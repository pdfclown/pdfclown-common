/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ResourceNotFoundException.java) is part of pdfclown-common-util module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import java.io.IOException;
import java.io.Serial;
import java.net.URI;
import org.jspecify.annotations.Nullable;

/**
 * Signals that an attempt to open a remote resource denoted by a specified URI has failed.
 * <p>
 * This is the remote equivalent of {@link java.io.FileNotFoundException}.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class ResourceNotFoundException extends IOException {
  @Serial
  private static final long serialVersionUID = 1L;

  private final URI uri;

  public ResourceNotFoundException(URI uri) {
    this(uri, null, null);
  }

  public ResourceNotFoundException(URI uri, @Nullable String message) {
    this(uri, message, null);
  }

  public ResourceNotFoundException(URI uri, @Nullable String message, @Nullable Throwable cause) {
    super(message != null ? message : "%s NOT FOUND".formatted(uri), cause);

    this.uri = uri;
  }

  public ResourceNotFoundException(URI uri, @Nullable Throwable cause) {
    this(uri, null, cause);
  }

  /**
   * Resource location.
   */
  public URI getUri() {
    return uri;
  }
}
