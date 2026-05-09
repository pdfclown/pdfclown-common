/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Systems.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.system;

import org.jspecify.annotations.Nullable;

/**
 * System utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Systems {
  /**
   * {@jada.reuseDoc} Gets the boolean corresponding to a {@linkplain System#getProperty(String)
   * system property}.
   * <p>
   * Contrary to {@link Boolean#getBoolean(String)}, this method takes into account also the
   * behavior of CLI flags (for example, {@code -Dmyflag}), whose empty string represents
   * {@code true}.
   * </p>
   * {@jada.reuseDoc END} {@jada.reuseDoc :params}
   *
   * @param key
   *          System property name.
   * @return {@code true}, if the property value is empty or equals {@code "true"}
   *         (case-insensitive).{@jada.reuseDoc END}
   */
  public static boolean getBooleanProperty(String key) {
    return parsePropertyBoolean(System.getProperty(key));
  }

  /**
   * {@jada.doc} Gets the boolean corresponding to a {@linkplain System#getProperty(String) system
   * property}.
   * <p>
   * Contrary to {@link Boolean#getBoolean(String)}, this method takes into account also the
   * behavior of CLI flags (for example, {@code -Dmyflag}), whose empty string represents
   * {@code true}.
   * </p>
   * {@jada.doc END}
   *
   * @param defaultValue
   *          Value to return if the property is undefined. {@jada.doc params}
   * @param key
   *          System property name.
   * @return {@code true}, if the property value is empty or equals {@code "true"}
   *         (case-insensitive).{@jada.doc END}
   */
  public static boolean getBooleanProperty(String key, boolean defaultValue) {
    return parsePropertyBoolean(System.getProperty(key, Boolean.toString(defaultValue)));
  }

  /**
   * Gets the boolean corresponding to a system property value.
   * <p>
   * Contrary to {@link Boolean#parseBoolean(String)}, this method takes into account also the
   * behavior of CLI flags (for example, {@code -Dmyflag}), whose empty string represents
   * {@code true}.
   * </p>
   *
   * @param value
   *          System property value.
   * @return {@code true}, if {@code value} is empty or equals {@code "true"} (case-insensitive).
   */
  public static boolean parsePropertyBoolean(@Nullable String value) {
    return value != null && (value.isEmpty() || value.equalsIgnoreCase("true"));
  }

  private Systems() {
  }
}
