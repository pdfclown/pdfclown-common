/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Systems.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.temp.util.system;

import java.util.function.Function;
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
    return parseBooleanProperty(System.getProperty(key));
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
    return parseBooleanProperty(System.getProperty(key, Boolean.toString(defaultValue)));
  }

  /**
   * Gets the domain value corresponding to a system property name.
   * {@jada.reuseDoc #parseProperty(*)}
   * <p>
   * This method supports three-state logic: absence ({@code null}, mapped to
   * {@code absentDefault}), implicit presence (empty string, mapped to {@code presentDefault}), and
   * explicit presence (non-empty string, mapped via {@code parser}); in the latter case, if the
   * parsing fails with an exception and {@code value} equals {@code "true"}, {@code presentDefault}
   * is returned (such condition addresses the edge case of flag-like, implicit-valued parameters
   * which are mapped to {@code "true"} in some execution environments like Maven).
   * </p>
   * {@jada.reuseDoc END}
   *
   * @param key
   *          System property name. {@jada.reuseDoc #parseProperty(*):params}
   * @param absentDefault
   *          Domain value to return in case {@code value} is undefined (missing property).
   * @param presentDefault
   *          Domain value to return in case {@code value} is empty (default property).
   * @param parser
   *          Domain value parser. Evaluates non-empty {@code value} and throws an exception if
   *          invalid.
   * @param <T>
   *          Domain value type. {@jada.reuseDoc END}
   */
  public static <T extends @Nullable Object> T getProperty(String key,
      T absentDefault, T presentDefault, Function<String, T> parser) {
    return parseProperty(System.getProperty(key), absentDefault, presentDefault, parser);
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
  public static boolean parseBooleanProperty(@Nullable String value) {
    return parseProperty(value, false, true, $ -> $.equalsIgnoreCase("true"));
  }

  /**
   * Gets the domain value corresponding to a system property value. {@jada.doc}
   * <p>
   * This method supports three-state logic: absence ({@code null}, mapped to
   * {@code absentDefault}), implicit presence (empty string, mapped to {@code presentDefault}), and
   * explicit presence (non-empty string, mapped via {@code parser}); in the latter case, if the
   * parsing fails with an exception and {@code value} equals {@code "true"}, {@code presentDefault}
   * is returned (such condition addresses the edge case of flag-like, implicit-valued parameters
   * which are mapped to {@code "true"} in some execution environments like Maven).
   * </p>
   * {@jada.doc END}
   *
   * @param value
   *          System property value. {@jada.doc params}
   * @param absentDefault
   *          Domain value to return in case {@code value} is undefined (missing property).
   * @param presentDefault
   *          Domain value to return in case {@code value} is empty (default property).
   * @param parser
   *          Domain value parser. Evaluates non-empty {@code value} and throws an exception if
   *          invalid.
   * @param <T>
   *          Domain value type. {@jada.doc END}
   */
  public static <T extends @Nullable Object> T parseProperty(@Nullable String value,
      T absentDefault, T presentDefault, Function<String, T> parser) {
    if (value == null)
      return absentDefault;
    else if (value.isEmpty())
      return presentDefault;
    else
      try {
        return parser.apply(value);
      } catch (Exception ex) {
        if (value.equals("true"))
          return presentDefault;
        else
          throw ex;
      }
  }

  private Systems() {
  }
}
