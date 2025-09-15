/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Systems.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_.system;

/**
 * System utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Systems {
  /**
   * Gets the boolean {@linkplain System#getProperty(String) system property} indicated by the key.
   * <p>
   * Contrary to {@link Boolean#getBoolean(String)}, this method takes into account also the
   * behavior of CLI flags (for example, {@code -Dmyflag}), whose empty string represents
   * {@code true}.
   * </p>
   *
   * @return {@code true}, if {@code value} is empty or equals {@code "true"} (case-insensitive).
   */
  public static boolean getBoolProperty(String key) {
    var value = System.getProperty(key);
    return value != null && (value.isEmpty() || value.equalsIgnoreCase("true"));
  }

  private Systems() {
  }
}
