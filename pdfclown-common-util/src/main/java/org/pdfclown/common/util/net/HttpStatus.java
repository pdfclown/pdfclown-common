/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (HttpStatus.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.net;

/**
 * HTTP status codes supplemental to {@link java.net.HttpURLConnection}{@code .HTTP_*}.
 *
 * @author Stefano Chizzolini
 */
public final class HttpStatus {
  /**
   * Temporary Redirect.
   */
  public static final int HTTP_REDIRECT_TEMP = 307;
  /**
   * Permanent Redirect.
   */
  public static final int HTTP_REDIRECT_PERM = 308;

  private HttpStatus() {
  }
}
