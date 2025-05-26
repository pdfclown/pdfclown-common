/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Objects.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util;

import static org.pdfclown.common.build.internal.util.Strings.DQUOTE;
import static org.pdfclown.common.build.internal.util.Strings.S;
import static org.pdfclown.common.build.internal.util.Strings.SQUOTE;

import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Object utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Objects {
  private static final String REGEX__JAVA_IDENTIFIER =
      "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
  private static final String REGEX__CLASS_FQN =
      REGEX__JAVA_IDENTIFIER + "(\\." + REGEX__JAVA_IDENTIFIER + ")*";
  private static final String REGEX__HEX = "[0-9a-fA-F]+";

  // SourceFQN: org.pdfclown.common.util.Objects#OBJ_ARRAY__EMPTY
  public static final Object[] OBJ_ARRAY__EMPTY = new Object[0];

  public static final String PATTERN_GROUP__CLASS_FQN = "fqcn";

  /**
   * Pattern of the default implementation of {@link Object#toString()}.
   * <p>
   * Use {@link #PATTERN_GROUP__CLASS_FQN} to catch the associated FQCN on match.
   * </p>
   */
  public static final Pattern PATTERN__TO_STRING__DEFAULT = Pattern.compile(
      String.format(Locale.ROOT, "(?<%s>%s)@%s", PATTERN_GROUP__CLASS_FQN, REGEX__CLASS_FQN,
          REGEX__HEX));

  // SourceFQN: org.pdfclown.common.util.Objects.asType(..)
  /**
   * Gets the type corresponding to the given object.
   * <p>
   * Same as {@link #typeOf(Object) typeOf(..)}, unless {@code obj} is {@link Class} (in such case,
   * returns itself).
   * </p>
   */
  public static @Nullable Class<?> asType(@Nullable Object obj) {
    return obj != null ? (obj instanceof Class ? (Class<?>) obj : obj.getClass()) : null;
  }

  // SourceFQN: org.pdfclown.common.util.Objects.fqn(..)
  /**
   * Gets the fully qualified type name of the given object.
   *
   * @return
   *         <ul>
   *         <li>{@code obj.getName()}, if {@code obj} is a {@link Class}</li>
   *         <li>{@code obj.getClass().getName()}, if {@code obj} is not a {@code Class}</li>
   *         <li>{@code "null"}, if {@code obj} is undefined</li>
   *         </ul>
   * @see #sqn(Object)
   */
  public static String fqn(@Nullable Object obj) {
    return fqn(obj, false);
  }

  // SourceFQN: org.pdfclown.common.util.Objects.fqnd(..)
  /**
   * Gets the fully qualified type name of the given object, replacing inner-class separators
   * ({@code $}) with dots.
   *
   * @see #fqn(Object)
   * @see #sqn(Object)
   */
  public static String fqnd(@Nullable Object obj) {
    return fqn(obj, true);
  }

  // SourceFQN: org.pdfclown.common.util.Objects.objTo(..)
  /**
   * Maps the given object.
   *
   * @param <T>
   *          Object type.
   * @param <R>
   *          Return type.
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   */
  public static <T, @Nullable R> R objTo(@Nullable T obj,
      Function<? super @NonNull T, ? extends R> mapper) {
    return obj != null ? mapper.apply(obj) : null;
  }

  // SourceFQN: org.pdfclown.common.util.Objects.objToLiteralString(..)
  /**
   * Maps the given object to its literal string representation (ie, inclusive of markers such as
   * quotes).
   *
   * @param obj
   *          Object to map.
   */
  public static String objToLiteralString(@Nullable Object obj) {
    if (obj instanceof CharSequence)
      return S + DQUOTE + obj + DQUOTE;
    else if (obj instanceof Character)
      return S + SQUOTE + obj + SQUOTE;
    else
      return String.valueOf(obj);
  }

  // SourceFQN: org.pdfclown.common.util.Objects.requireState(..)
  /**
   * (see {@link #requireState(Object, String)})
   */
  public static <T> @NonNull T requireState(@Nullable T obj) {
    return requireState(obj, "State UNDEFINED");
  }

  // SourceFQN: org.pdfclown.common.util.Objects.requireState(..)
  /**
   * Checks that the given object reference is not null.
   * <p>
   * This method is the state counterpart of {@link java.util.Objects#requireNonNull(Object)}:
   * contrary to the latter (which is primarily for input validation), it is designed for doing
   * <b>output validation</b> in accessors, as demonstrated below:
   * </p>
   * <pre>
   *public Bar getBar() {
   *  return requireState((Bar)getExternalResource("bar"));
   *}</pre>
   * <p>
   * Useful in case an accessor is expected to return a non-null reference, but depends on external
   * state beyond its control, or on internal state available in specific phases of its containing
   * object: if the caller tries to access it in a wrong moment, an {@link IllegalStateException} is
   * thrown.
   * </p>
   *
   * @return {@code obj}
   * @throws IllegalStateException
   *           if {@code obj} is {@code null}.
   * @see java.util.Objects#requireNonNull(Object)
   */
  public static <T> @NonNull T requireState(@Nullable T obj, String message) {
    if (obj == null)
      throw new IllegalStateException(message);

    return obj;
  }

  // SourceFQN: org.pdfclown.common.util.Objects.sqn(..)
  /**
   * Gets the qualified simple type name of the given object, ie the simple class name qualified
   * with its outer class (eg, {@code MyOuterClass$MyInnerClass}), if present (otherwise, behaves
   * like {@link Class#getSimpleName()}).
   *
   * @see #fqn(Object)
   */
  public static String sqn(@Nullable Object obj) {
    return sqn(obj, false);
  }

  // SourceFQN: org.pdfclown.common.util.Objects.sqnd(..)
  /**
   * Gets the qualified simple type name of the given object, replacing inner-class separators
   * ({@code $}) with dots, ie the simple class name qualified with its outer class (eg,
   * {@code MyOuterClass.MyInnerClass}), if present (otherwise, behaves like
   * {@link Class#getSimpleName()}).
   *
   * @see #sqn(Object)
   * @see #fqn(Object)
   */
  public static String sqnd(@Nullable Object obj) {
    return sqn(obj, true);
  }

  // SourceFQN: org.pdfclown.common.util.Objects.typeOf(..)
  /**
   * Gets the type of the given object.
   */
  public static @Nullable Class<?> typeOf(@Nullable Object obj) {
    return obj != null ? obj.getClass() : null;
  }

  // SourceFQN: org.pdfclown.common.util.Objects.fqn(..)
  private static String fqn(@Nullable Object obj, boolean dotted) {
    return fqn(objTo(asType(obj), Class::getName), dotted);
  }

  // SourceFQN: org.pdfclown.common.util.Objects.fqn(..)
  private static String fqn(@Nullable String s, boolean dotted) {
    return s != null
        ? s.replace('$', dotted ? '.' : '$')
        : "null";
  }

  // SourceFQN: org.pdfclown.common.util.Objects.sqn(..)
  private static String sqn(@Nullable Object obj, boolean dotted) {
    return sqn(fqn(obj, false), dotted);
  }

  // SourceFQN: org.pdfclown.common.util.Objects.sqn(..)
  private static String sqn(@Nullable String s, boolean dotted) {
    return fqn(objTo(s, $ -> $.substring($.lastIndexOf('.') + 1)), dotted);
  }

  private Objects() {
  }
}
