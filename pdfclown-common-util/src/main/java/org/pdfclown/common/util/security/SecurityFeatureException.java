/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SecurityFeatureException.java) is part of pdfclown-common-util module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.security;

import java.io.Serial;
import java.security.GeneralSecurityException;

/**
 * Thrown to indicate that a security feature failed to be activated, either because of lack of
 * support or due to issues occurred during its activation, as possibly specified by its
 * {@linkplain #getCause() cause}.
 *
 * @author Stefano Chizzolini
 */
public class SecurityFeatureException extends GeneralSecurityException {
  @Serial
  private static final long serialVersionUID = 1L;

  public SecurityFeatureException(String msg) {
    super(msg);
  }

  public SecurityFeatureException(String message, Throwable cause) {
    super(message, cause);
  }

  public SecurityFeatureException(Throwable cause) {
    super(cause);
  }
}
