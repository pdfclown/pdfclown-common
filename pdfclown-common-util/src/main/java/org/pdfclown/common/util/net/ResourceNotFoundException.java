/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ResourceNotFoundException.java) is part of pdfclown-common-util module
  in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you
  reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block abov
  e this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.net;

import java.io.IOException;
import java.io.Serial;
import java.net.URI;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.ElementNotFoundException;

/**
 * Thrown if an attempt to retrieve a remote resource denoted by a specified URI has failed.
 *
 * @author Stefano Chizzolini
 * @implNote Despite being a highly abstract analogue to {@link java.io.FileNotFoundException}, this
 *           exception doesn't extend {@link IOException} deliberately, on the premise that a
 *           missing resource still represents a successful infrastructural operation, not an I/O
 *           failure per-se.
 */
public class ResourceNotFoundException extends ElementNotFoundException {
  @Serial
  private static final long serialVersionUID = 1L;

  public ResourceNotFoundException(URI uri) {
    this(uri, null, null);
  }

  public ResourceNotFoundException(URI uri, @Nullable String message) {
    this(uri, message, null);
  }

  public ResourceNotFoundException(URI uri, @Nullable String message,
      @Nullable Throwable cause) {
    super(uri, message, cause);
  }

  public ResourceNotFoundException(URI uri, @Nullable Throwable cause) {
    this(uri, null, cause);
  }

  /**
   * Resource location.
   */
  @Override
  public URI getRef() {
    return (URI) super.getRef();
  }
}
