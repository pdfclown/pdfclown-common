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

import static org.pdfclown.common.build.internal.util.Strings.DOT;
import static org.pdfclown.common.build.internal.util.Strings.DQUOTE;
import static org.pdfclown.common.build.internal.util.Strings.EMPTY;
import static org.pdfclown.common.build.internal.util.Strings.S;
import static org.pdfclown.common.build.internal.util.Strings.SQUOTE;
import static org.pdfclown.common.build.internal.util.Strings.indexFound;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.JavaUnicodeEscaper;
import org.apache.commons.text.translate.LookupTranslator;
import org.apache.commons.text.translate.OctalUnescaper;
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

  // SourceFQN: org.pdfclown.common.util.Objects.LITERAL_STRING_ESCAPE
  /**
   * Literal string escape filter.
   * <p>
   * Differently from {@link StringEscapeUtils#escapeJava(String)}, this translator doesn't escape
   * Unicode characters.
   * </p>
   *
   * @see #LITERAL_STRING_UNESCAPE
   * @see StringEscapeUtils#ESCAPE_JAVA
   */
  private static final CharSequenceTranslator LITERAL_STRING_ESCAPE = new AggregateTranslator(
      new LookupTranslator(Map.of(
          "\"", "\\\"",
          "\\", "\\\\")),
      new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE),
      JavaUnicodeEscaper.below(32));

  // SourceFQN: org.pdfclown.common.util.Objects.LITERAL_STRING_UNESCAPE
  /**
   * Literal string unescape filter.
   * <p>
   * Differently from {@link StringEscapeUtils#unescapeJava(String)}, this translator doesn't
   * unescape Unicode characters.
   * </p>
   *
   * @see #LITERAL_STRING_ESCAPE
   * @see StringEscapeUtils#UNESCAPE_JAVA
   */
  private static final CharSequenceTranslator LITERAL_STRING_UNESCAPE = new AggregateTranslator(
      new OctalUnescaper(), // .between('\1', '\377'),
      new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE),
      new LookupTranslator(Map.of(
          "\\\\", "\\",
          "\\\"", "\"",
          "\\'", "'",
          "\\", EMPTY)));

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

  // SourceFQN: org.pdfclown.common.util.Objects.fqnd(..)
  /**
   * Ensures the given type name has inner-class separators ({@code $}) replaced with dots.
   * <p>
   * No syntactic check is applied to {@code typeName}.
   * </p>
   *
   * @see #fqn(Object)
   * @see #sqn(Object)
   */
  public static String fqnd(@Nullable String typeName) {
    return fqn(typeName, true);
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
   * Maps the given object to its literal string representation (that is, inclusive of markers such
   * as quotes).
   *
   * @return Depending on {@code obj} type:
   *         <ul>
   *         <li>{@code null} — {@code "null"} (like
   *         {@link java.util.Objects#toString(Object)})</li>
   *         <li>{@link Boolean}, {@link Number} — {@link Object#toString()}, as-is</li>
   *         <li>{@link Character} — {@link Object#toString()}, wrapped with single quotes</li>
   *         <li>any other type — {@link Object#toString()}, escaped and wrapped with double
   *         quotes</li>
   *         </ul>
   */
  public static String objToLiteralString(@Nullable Object obj) {
    if (obj == null)
      return "null";
    else if (obj instanceof Float)
      /*
       * NOTE: Literal float MUST be marked by suffix to override default double type.
       */
      return obj + "F";
    else if (obj instanceof Long)
      /*
       * NOTE: Literal long MUST be marked by suffix to override default integer type.
       */
      return obj + "L";
    else if (obj instanceof Number || obj instanceof Boolean)
      return obj.toString();
    else if (obj instanceof Character)
      return S + SQUOTE + ((Character) obj == SQUOTE ? "\\" : EMPTY) + obj + SQUOTE;
    else
      return S + DQUOTE + LITERAL_STRING_ESCAPE.translate(obj.toString()) + DQUOTE;
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
   * Gets the qualified simple type name of the given object, that is the simple class name
   * qualified with its outer class (for example, {@code MyOuterClass$MyInnerClass}), if present
   * (otherwise, behaves like {@link Class#getSimpleName()}).
   *
   * @see #fqn(Object)
   */
  public static String sqn(@Nullable Object obj) {
    return sqn(obj, false);
  }

  // SourceFQN: org.pdfclown.common.util.Objects.sqn(..)
  /**
   * Gets the qualified simple type name from the given type name, that is the simple class name
   * qualified with its outer class (for example, {@code MyOuterClass$MyInnerClass}), if present
   * (otherwise, behaves like {@link Class#getSimpleName()}).
   * <p>
   * No syntactic check is applied to {@code typeName}.
   * </p>
   *
   * @see #fqn(Object)
   */
  public static String sqn(@Nullable String typeName) {
    return sqn(typeName, false);
  }

  // SourceFQN: org.pdfclown.common.util.Objects.sqnd(..)
  /**
   * Gets the qualified simple type name of the given object, replacing inner-class separators
   * ({@code $}) with dots, that is the simple class name qualified with its outer class (for
   * example, {@code MyOuterClass.MyInnerClass}), if present (otherwise, behaves like
   * {@link Class#getSimpleName()}).
   *
   * @see #sqn(Object)
   * @see #fqn(Object)
   */
  public static String sqnd(@Nullable Object obj) {
    return sqn(obj, true);
  }

  // SourceFQN: org.pdfclown.common.util.Objects.sqnd(..)
  /**
   * Gets the qualified simple type name from the given type name, replacing inner-class separators
   * ({@code $}) with dots, that is the simple class name qualified with its outer class (for
   * example, {@code MyOuterClass.MyInnerClass}), if present (otherwise, behaves like
   * {@link Class#getSimpleName()}).
   * <p>
   * No syntactic check is applied to {@code typeName}.
   * </p>
   *
   * @see #sqn(Object)
   * @see #fqn(Object)
   */
  public static String sqnd(@Nullable String typeName) {
    return sqn(typeName, true);
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
  private static String fqn(@Nullable String typeName, boolean dotted) {
    return typeName != null
        ? dotted ? typeName.replace('$', DOT) : typeName
        : "null";
  }

  // SourceFQN: org.pdfclown.common.util.Objects.sqn(..)
  private static String sqn(@Nullable Object obj, boolean dotted) {
    return sqn(fqn(obj, false), dotted);
  }

  // SourceFQN: org.pdfclown.common.util.Objects.sqn(..)
  private static String sqn(@Nullable String typeName, boolean dotted) {
    return fqn(objTo(typeName, $ -> indexFound($.indexOf(DOT))
        ? $.substring($.lastIndexOf(DOT) + 1)
        : typeName), dotted);
  }

  private Objects() {
  }
}
