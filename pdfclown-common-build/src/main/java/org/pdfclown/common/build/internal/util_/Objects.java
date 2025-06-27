/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Objects.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Exceptions.wrongArg;
import static org.pdfclown.common.build.internal.util_.Strings.CURLY_BRACE_CLOSE;
import static org.pdfclown.common.build.internal.util_.Strings.CURLY_BRACE_OPEN;
import static org.pdfclown.common.build.internal.util_.Strings.DOT;
import static org.pdfclown.common.build.internal.util_.Strings.DQUOTE;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;
import static org.pdfclown.common.build.internal.util_.Strings.S;
import static org.pdfclown.common.build.internal.util_.Strings.SQUOTE;
import static org.pdfclown.common.build.internal.util_.Strings.indexFound;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.JavaUnicodeEscaper;
import org.apache.commons.text.translate.LookupTranslator;
import org.apache.commons.text.translate.OctalUnescaper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.util_.annot.PolyNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Objects {
  private static final Logger log = LoggerFactory.getLogger(Objects.class);

  /**
   * Empty object array.
   */
  public static final Object[] OBJ_ARRAY__EMPTY = new Object[0];

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

  private static final String STRING__NULL = "null";

  /**
   * Gets whether the given object matches the other one according to the given predicate.
   * <p>
   * NOTE: This method is redundant; it's intended as a placeholder in case the implementer expects
   * further objects to be added later.
   * </p>
   */
  public static <@Nullable T, @Nullable U> boolean any(T obj, BiPredicate<T, U> predicate,
      U other1) {
    return predicate.test(obj, other1);
  }

  /**
   * Gets whether the given object matches any of the others according to the given predicate.
   */
  public static <@Nullable T, @Nullable U> boolean any(T obj, BiPredicate<T, U> predicate, U other1,
      U other2) {
    return predicate.test(obj, other1)
        || predicate.test(obj, other2);
  }

  /**
   * Gets whether the given object matches any of the others according to the given predicate.
   *
   * @implNote Because of the limited expressiveness of varargs, in order to force the caller to
   *           specify at least two other objects we have to declare two corresponding parameters
   *           ({@code other1} and {@code other2}) in the signature — despite its inherent ugliness,
   *           this is the standard way Java API itself deals with such cases.
   */
  @SafeVarargs
  public static <@Nullable T, @Nullable U> boolean any(T obj, BiPredicate<T, U> predicate, U other1,
      U other2, U... others) {
    if (predicate.test(obj, other1)
        || predicate.test(obj, other2))
      return true;
    for (var other : others) {
      if (predicate.test(obj, other))
        return true;
    }
    return false;
  }

  /**
   * Gets whether the given object matches any of the others according to the given predicate.
   */
  public static <@Nullable T, @Nullable U> boolean any(T obj, BiPredicate<T, U> predicate, U other1,
      U other2, U other3) {
    return predicate.test(obj, other1)
        || predicate.test(obj, other2)
        || predicate.test(obj, other3);
  }

  /**
   * Gets whether the given object matches any of the others according to the given predicate.
   */
  public static <@Nullable T, @Nullable U> boolean any(T obj, BiPredicate<T, U> predicate, U other1,
      U other2, U other3, U other4) {
    return predicate.test(obj, other1)
        || predicate.test(obj, other2)
        || predicate.test(obj, other3)
        || predicate.test(obj, other4);
  }

  /**
   * Gets whether the given object matches any of the others according to the given predicate.
   */
  public static <@Nullable T, @Nullable U> boolean any(T obj, BiPredicate<T, U> predicate, U other1,
      U other2, U other3, U other4, U other5) {
    return predicate.test(obj, other1)
        || predicate.test(obj, other2)
        || predicate.test(obj, other3)
        || predicate.test(obj, other4)
        || predicate.test(obj, other5);
  }

  /**
   * Gets the type corresponding to the given object.
   * <p>
   * Same as {@link #typeOf(Object) typeOf(..)}, unless {@code obj} is {@link Class} (in such case,
   * returns itself).
   * </p>
   */
  public static @PolyNull @Nullable Class<?> asType(@PolyNull @Nullable Object obj) {
    return obj != null ? (obj instanceof Class ? (Class<?>) obj : obj.getClass()) : null;
  }

  /**
   * Gets the integer representation of the given boolean value.
   *
   * @return {@code 1}, if {@code value} is {@code true}, otherwise {@code 0}.
   */
  public static int boolToInt(boolean value) {
    return Boolean.compare(value, false);
  }

  /**
   * Quietly closes the given object.
   *
   * @return {@code obj}.
   * @implNote A warning is logged if this operation fails.
   */
  public static <@Nullable T extends AutoCloseable> T closeQuiet(T obj) {
    if (obj != null) {
      try {
        obj.close();
      } catch (Exception ex) {
        log.warn("Close FAILED", ex);
      }
    }
    return obj;
  }

  /**
   * Gets whether the two resolved elements are equal.
   *
   * @param <R>
   *          Reference (ie, unresolved element) type.
   * @param <T>
   *          Object (ie, resolved element) type.
   * @param o1
   *          Resolved element 1.
   * @param o2
   *          Resolved element 2.
   * @param baseRefType
   *          Base unresolved element type.
   * @param resolver
   *          Element resolver.
   * @param raw
   *          Whether comparison is done on resolved elements only; otherwise, unresolved elements
   *          are required to be of the same type in order to be resolved.
   */
  @SuppressWarnings({ "unchecked", "null" })
  public static <R, @Nullable T> boolean deepEquals(T o1, T o2, Class<R> baseRefType,
      Function<? super R, T> resolver, boolean raw) {
    if (o1 == o2)
      return true;
    else if (o1 == null || o2 == null)
      return false;
    else if (o1 instanceof Map && o2 instanceof Map)
      return deepEqualsMap((Map<?, R>) o1, (Map<?, R>) o2, baseRefType, resolver, raw);
    else if (o1 instanceof List && o2 instanceof List)
      return deepEqualsList((List<R>) o1, (List<R>) o2, baseRefType, resolver, raw);
    else if (o1 instanceof Collection && o2 instanceof Collection)
      return deepEqualsCollection((Collection<R>) o1, (Collection<R>) o2, baseRefType, resolver,
          raw);
    else if (o1.equals(o2))
      return true;
    else
      return false;
  }

  /**
   * Gets whether the two unresolved elements are equal.
   *
   * @param <R>
   *          Reference (ie, unresolved element) type.
   * @param <T>
   *          Object (ie, resolved element) type.
   * @param ref1
   *          Unresolved element 1.
   * @param ref2
   *          Unresolved element 2.
   * @param baseRefType
   *          Base unresolved element type.
   * @param resolver
   *          Element resolver.
   * @param raw
   *          Whether comparison is done on resolved elements only; otherwise, unresolved elements
   *          are required to be of the same type in order to be resolved.
   */
  public static <R, T> boolean deepEqualsRef(@Nullable R ref1, @Nullable R ref2,
      Class<R> baseRefType, Function<? super @Nullable R, @Nullable T> resolver, boolean raw) {
    if (ref1 == ref2)
      return true;
    else if (!raw && !isSameType(ref1, ref2))
      return false;

    return deepEquals(resolver.apply(ref1), resolver.apply(ref2), baseRefType, resolver, raw);
  }

  /**
   * (Same as {@link java.util.Objects#equals(Object, Object)}, but applies
   * {@link String#equalsIgnoreCase(String)} instead of {@link Object#equals(Object)})
   */
  public static boolean equalsIgnoreCase(@Nullable String s1, @Nullable String s2) {
    //noinspection StringEquality
    return (s1 == s2) || (s1 != null && s1.equalsIgnoreCase(s2));
  }

  /**
   * Gets whether the given object is equal to or contains the other one.
   *
   * <p>
   * Containment is verified via {@link Collection#contains(Object)} and
   * {@link Map#containsValue(Object)}.
   * </p>
   */
  public static boolean equalsOrContains(@Nullable Object obj, @Nullable Object other) {
    return java.util.Objects.equals(obj, other)
        || (obj instanceof Collection<?> && ((Collection<?>) obj).contains(other))
        || (obj instanceof Map<?, ?> && ((Map<?, ?>) obj).containsValue(other));
  }

  /**
   * Gets whether the given object is equal to the other one or undefined.
   */
  public static boolean equalsOrNull(@Nullable Object obj, @Nullable Object other) {
    return obj == null || java.util.Objects.equals(obj, other);
  }

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

  /**
   * Applies an action to the given object.
   *
   * @param action
   *          Action applied if {@code obj} is defined.
   */
  public static <T> void ifPresent(@Nullable T obj, Consumer<? super T> action) {
    if (obj != null) {
      action.accept(obj);
    }
  }

  /**
   * Initializes the given class.
   *
   * @return Whether the initialization succeeded.
   * @implNote This is the unchecked equivalent of {@link Class#forName(String)}
   */
  public static boolean init(Class<?> type) {
    return init(type.getName());
  }

  /**
   * Initializes the given class.
   *
   * @return Whether the initialization succeeded.
   * @implNote This is the unchecked equivalent of {@link Class#forName(String)}
   */
  public static boolean init(String typeName) {
    try {
      Class.forName(typeName);
      return true;
    } catch (ClassNotFoundException ex) {
      return false;
    }
  }

  /**
   * Initializes the given class.
   *
   * @throws RuntimeException
   *           if class initialization fails.
   * @implNote This is the unchecked equivalent of {@link Class#forName(String)}
   */
  public static void initOrThrow(Class<?> type) {
    initOrThrow(type.getName());
  }

  /**
   * Initializes the given class.
   *
   * @throws RuntimeException
   *           if class initialization fails.
   * @implNote This is the unchecked equivalent of {@link Class#forName(String)}
   */
  public static void initOrThrow(String typeName) {
    try {
      Class.forName(typeName);
    } catch (ClassNotFoundException ex) {
      throw runtime(ex);
    }
  }

  /**
   * Gets whether the given object is an instance of the given type.
   */
  public static boolean isInstanceOf(@Nullable Object o, @Nullable Class<?> c) {
    return c != null && c.isInstance(o);
  }

  /**
   * Gets whether the given string is a literal null ({@code "null"}).
   *
   * @see #objFromLiteralString(String)
   */
  public static boolean isLiteralNull(@Nullable String s) {
    return STRING__NULL.equals(s);
  }

  /**
   * Gets whether the objects are the same instance, or both null.
   */
  public static boolean isSame(@Nullable Object o1, @Nullable Object o2) {
    return o1 == o2;
  }

  /**
   * Gets whether the given objects belong to exactly the same type.
   */
  public static boolean isSameType(@Nullable Object o1, @Nullable Object o2) {
    return typeOf(o1) == typeOf(o2);
  }

  /**
   * Gets the locale corresponding to the given language tag.
   *
   * @throws IllegalArgumentException
   *           if {@code languageTag} is non-conformant.
   */
  public static @PolyNull @Nullable Locale locale(@PolyNull @Nullable String languageTag) {
    if (languageTag == null)
      return null;

    var ret = Locale.forLanguageTag(languageTag);
    try {
      if (!ret.getISO3Language().isEmpty())
        return ret;
    } catch (MissingResourceException e) {
      /* FALLTHRU */
    }
    throw wrongArg("languageTag", languageTag);
  }

  /**
   * Normalizes the given locale.
   *
   * @return {@linkplain Locale#getDefault() Default} if {@code locale} is undefined.
   */
  public static Locale localeNorm(@Nullable Locale locale) {
    return requireNonNullElseGet(locale, Locale::getDefault);
  }

  /**
   * Asserts the given object is non-null.
   * <p>
   * This is a shorthand to explicit non-null assertion, useful to confirm expected state to
   * compiler when null check is redundant (contrary to
   * {@link java.util.Objects#requireNonNull(Object)}, which enforces null check).
   * </p>
   * <p>
   * NOTE: Depending on the compiler's nullness policies' strictness, such method may be unnecessary
   * and more conveniently replaced by <code>@SuppressWarnings("null")</code> (see also
   * <a href="https://github.com/jspecify/jspecify/issues/29">JSpecify issue #29</a> for a broader
   * discussion on the topic).
   * </p>
   * <p>
   * NOTE: Despite this method is accidentally named like {@link java.util.Objects#nonNull(Object)},
   * there is no risk of clash, as their use contexts never overlap (BTW, the latter represents one
   * of those ugly naming inconsistencies of the Java API, as its complement is
   * {@link java.util.Objects#isNull(Object)}).
   * </p>
   *
   * @return {@code obj}
   * @see java.util.Objects#requireNonNull(Object)
   */
  public static <T> @NonNull T nonNull(@Nullable T obj) {
    assert obj != null;
    return obj;
  }

  /**
   * Casts the given object to a target type.
   * <p>
   * The purpose of this method is to allow a safe cast without pre-check.
   * </p>
   *
   * @return {@code null} if {@code obj} is assignment-incompatible with {@code type}.
   */
  @SuppressWarnings("unchecked")
  public static <T, R extends T> @Nullable R objCast(@Nullable T obj, Class<R> type) {
    return type.isInstance(obj) ? (R) obj : null;
  }

  /**
   * Applies an action to the given object.
   *
   * @param action
   *          Action applied if {@code obj} is defined.
   * @return {@code obj}
   */
  public static <T> @Nullable T objDo(@Nullable T obj, Consumer<? super T> action) {
    if (obj != null) {
      action.accept(obj);
    }
    return obj;
  }

  /**
   * Returns the given object, if not null; otherwise, the supplied default.
   * <p>
   * NOTE: This method doesn't enforce its result to be non-null; if you need such enforcement, use
   * {@link java.util.Objects#requireNonNullElseGet(Object, Supplier)} instead.
   * </p>
   *
   * @param <T>
   *          Object type.
   * @param obj
   *          Object to evaluate.
   * @param defaultSupplier
   *          Object supplier if {@code obj} is undefined.
   * @see java.util.Objects#requireNonNull(Object)
   * @see java.util.Objects#requireNonNullElse(Object, Object)
   * @see java.util.Objects#requireNonNullElseGet(Object, Supplier)
   */
  public static <T> @Nullable T objElseGet(@Nullable T obj,
      Supplier<? extends @Nullable T> defaultSupplier) {
    return obj != null ? obj : defaultSupplier.get();
  }

  /**
   * Gets the object corresponding to the given literal string.
   * <p>
   * NOTE: This method is complementary to {@link #objToLiteralString(Object)} only for primitive
   * type representations.
   * </p>
   *
   * @return Depending on {@code s} format:
   *         <ul>
   *         <li>{@code "null"}, or undefined ({@code null}), or invalid (any string not comprised
   *         in known formats) — {@code null} (use {@link #isLiteralNull(String)} to tell literal
   *         null apart)</li>
   *         <li>boolean literal ({@code true}, {@code false}) — {@link Boolean}</li>
   *         <li>numeric value — see {@link NumberUtils#createNumber(String)}</li>
   *         <li>single-quoted character — {@link Character}</li>
   *         <li>single- or double-quoted string — {@link String}, unescaped</li>
   *         </ul>
   */
  public static @Nullable Object objFromLiteralString(@Nullable String s) {
    // Undefined, or null literal?
    if (s == null || (s = s.trim()).equals(STRING__NULL))
      return null;

    if (s.length() >= 2) {
      char c = s.charAt(0);
      switch (c) {
        case '"':
        case '\'':
          // Quoted literal?
          if (s.charAt(s.length() - 1) == c) {
            // Character literal without escape?
            if (c == SQUOTE && s.length() == 3)
              return Character.valueOf(s.charAt(1));
            // Character literal with escape?
            else if (c == SQUOTE && s.length() == 4 && s.charAt(1) == '\\')
              return Character.valueOf(s.charAt(2));
            // String literal.
            else
              return LITERAL_STRING_UNESCAPE.translate(s.substring(1, s.length() - 1));
          }
      }
    }

    s = s.toLowerCase();
    // Boolean literal?
    if (s.equals("true"))
      return Boolean.TRUE;
    else if (s.equals("false"))
      return Boolean.FALSE;

    try {
      // Numeric literal.
      return NumberUtils.createNumber(s);
    } catch (NumberFormatException ex) {
      // Invalid literal.
      return null;
    }
  }

  /**
   * Maps the given object.
   *
   * @param <T>
   *          Object type.
   * @param <R>
   *          Result type.
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   */
  public static <T, R> @Nullable R objTo(@Nullable T obj,
      Function<? super @NonNull T, ? extends @Nullable R> mapper) {
    return obj != null ? mapper.apply(obj) : null;
  }

  /**
   * Maps the given object.
   *
   * @param <T>
   *          Object type.
   * @param <R>
   *          Result type.
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   * @param defaultResult
   *          Result if {@code obj} or {@code mapper}'s result are undefined.
   */
  public static <T, R> R objToElse(@Nullable T obj,
      Function<? super @NonNull T, ? extends @Nullable R> mapper, R defaultResult) {
    R ret;
    return (ret = objTo(obj, mapper)) != null ? ret : defaultResult;
  }

  /**
   * Maps the given object.
   *
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   * @param defaultSupplier
   *          Result supplier if {@code obj} or {@code mapper}'s result are undefined.
   */
  public static <T, @Nullable R> R objToElseGet(@Nullable T obj,
      Function<? super @NonNull T, ? extends R> mapper, Supplier<? extends R> defaultSupplier) {
    R ret;
    return obj != null && (ret = mapper.apply(obj)) != null ? ret : defaultSupplier.get();
  }

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
      return STRING__NULL;
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

  /**
   * Maps the given object to its string representation, ensuring its qualification with simple type
   * name.
   *
   * @param obj
   *          Object to map.
   * @see #objToString(Object)
   * @implNote If {@link #sqn(Object) SQN} is missing from the original {@link Object#toString()
   *           toString()}, the latter is wrapped in curly braces and prepended by the former
   *           (<code>%SQN%{%toString()%}</code>).
   */
  public static String objToQualifiedString(Object obj) {
    String objString = obj.toString();
    return objString.contains(sqn(obj)) ? objString
        : fqn(obj) + CURLY_BRACE_OPEN + objString + CURLY_BRACE_CLOSE;
  }

  /**
   * Maps the given object to its string representation.
   * <p>
   * NOTE: Contrary to the standard {@link java.util.Objects#toString(Object)} function, this one
   * returns {@code null} as-is.
   * </p>
   *
   * @param obj
   *          Object to map.
   * @see java.util.Objects#toString(Object, String)
   */
  public static @PolyNull @Nullable String objToString(@PolyNull @Nullable Object obj) {
    return obj != null ? obj.toString() : null;
  }

  /**
   * Wraps the given object in a null-aware container.
   */
  public static <T> Optional<T> opt(@Nullable T obj) {
    return Optional.ofNullable(obj);
  }

  /**
   * (see {@link #requireState(Object, String)})
   */
  public static <T> @NonNull T requireState(@Nullable T obj) {
    return requireState(obj, "State UNDEFINED");
  }

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

  /**
   * Gets the given fully qualified name split into package and simple name parts.
   *
   * @param fqn
   *          Fully qualified name.
   * @return Two-part string array, where the first item is empty if {@code fqn} has no package.
   */
  @SuppressWarnings("null")
  public static @NonNull String @NonNull [] splitFqn(String fqn) {
    int pos = fqn.lastIndexOf(DOT);
    return pos >= 0
        ? new String[] { fqn.substring(0, pos), fqn.substring(pos + 1) }
        : new String[] { EMPTY, fqn };
  }

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

  /**
   * Gets the string representation of the given object, along with its features.
   *
   * @param features
   *          Features (null values are ignored).
   */
  public static String toStringWithFeatures(Object obj, @Nullable Object... features) {
    var b = new StringBuilder(sqn(obj)).append(" (");
    for (int i = 0; i < features.length; i++) {
      if (features[i] == null) {
        continue;
      }

      if (i > 0) {
        b.append(" ");
      }
      b.append(features[i]);
    }
    return b.append(")").toString();
  }

  /**
   * Gets the string representation of the given object, along with its features.
   *
   * @param f1
   *          Feature (null value is ignored).
   */
  public static String toStringWithFeatures(Object obj, @Nullable Object f1) {
    var b = new StringBuilder(sqn(obj)).append(" ");
    if (f1 instanceof Collection || f1 instanceof Map) {
      b.append(f1);
    } else {
      b.append("(");
      if (f1 != null) {
        b.append(f1);
      }
      b.append(")");
    }
    return b.toString();
  }

  /**
   * Gets the string representation of the given object, along with its features.
   *
   * @param f1
   *          Feature (null value is ignored).
   * @param f2
   *          Feature (null value is ignored).
   */
  public static String toStringWithFeatures(Object obj, @Nullable Object f1, @Nullable Object f2) {
    var b = new StringBuilder(sqn(obj)).append(" (");
    var filled = false;
    if (f1 != null) {
      filled = true;

      b.append(f1);
    }
    if (f2 != null) {
      if (filled) {
        b.append(" ");
      }

      b.append(f2);
    }
    return b.append(")").toString();
  }

  /**
   * Gets the string representation of the given object, along with its features.
   *
   * @param f1
   *          Feature (null value is ignored).
   * @param f2
   *          Feature (null value is ignored).
   * @param f3
   *          Feature (null value is ignored).
   */
  public static String toStringWithFeatures(Object obj, @Nullable Object f1, @Nullable Object f2,
      @Nullable Object f3) {
    var b = new StringBuilder(sqn(obj)).append(" (");
    var filled = false;
    if (f1 != null) {
      filled = true;

      b.append(f1);
    }
    if (f2 != null) {
      if (filled) {
        b.append(" ");
      } else {
        filled = true;
      }

      b.append(f2);
    }
    if (f3 != null) {
      if (filled) {
        b.append(" ");
      }

      b.append(f3);
    }
    return b.append(")").toString();
  }

  /**
   * Gets the string representation of the given object, along with its properties.
   *
   * @param properties
   *          Properties (key-value pairs; keys MUST be non-null).
   */
  public static String toStringWithProperties(Object obj, @Nullable Object... properties) {
    var b = new StringBuilder(sqn(obj)).append(" {");
    for (int i = 0; i < properties.length;) {
      if (i > 0) {
        b.append(", ");
      }
      b.append(requireNonNull(properties[i++])).append(": ").append(properties[i++]);
    }
    return b.append("}").toString();
  }

  /**
   * Gets the string representation of the given object, along with its properties.
   */
  public static String toStringWithProperties(Object obj, String k1, @Nullable Object v1) {
    return sqn(obj) + " {"
        + requireNonNull(k1) + ": " + v1
        + "}";
  }

  /**
   * Gets the string representation of the given object, along with its properties.
   */
  public static String toStringWithProperties(Object obj, String k1, @Nullable Object v1, String k2,
      @Nullable Object v2) {
    return sqn(obj) + " {"
        + requireNonNull(k1) + ": " + v1 + ", "
        + requireNonNull(k2) + ": " + v2
        + "}";
  }

  /**
   * Gets the string representation of the given object, along with its properties.
   */
  public static String toStringWithProperties(Object obj, String k1, @Nullable Object v1, String k2,
      @Nullable Object v2, String k3, @Nullable Object v3) {
    return sqn(obj) + " {"
        + requireNonNull(k1) + ": " + v1 + ", "
        + requireNonNull(k2) + ": " + v2 + ", "
        + requireNonNull(k3) + ": " + v3
        + "}";
  }

  /**
   * Tries the given operation, returning its result, if not null; otherwise, the given default.
   *
   * @param operation
   *          Operation.
   * @param defaultObj
   *          Result in case {@code operation} fails or its result is undefined.
   */
  public static <@Nullable T> T tryOrElse(Supplier<? extends T> operation, T defaultObj) {
    try {
      T ret = operation.get();
      if (ret != null)
        return ret;
    } catch (Exception ex) {
      /* NOOP */
    }
    return defaultObj;
  }

  /**
   * Quietly runs the given object.
   *
   * @implNote A warning is logged if this operation fails.
   */
  public static void tryQuiet(Runnable obj) {
    try {
      obj.run();
    } catch (Throwable ex) {
      log.warn("Execution FAILED", ex);
    }
  }

  /**
   * Gets the type corresponding to the given fully-qualified name.
   *
   * @return {@code null}, if no class matched {@code name}, or the latter is undefined.
   * @implNote {@code name} nullability was introduced to simplify method referencing in lambda
   *           expressions, avoiding NPE issues in case of nullable input.
   */
  public static @Nullable Class<?> type(@Nullable String name) {
    if (name != null) {
      try {
        return Class.forName(name);
      } catch (ClassNotFoundException ex) {
        // NOOP
      }
    }
    return null;
  }

  /**
   * Gets the type of the given object.
   */
  public static @PolyNull @Nullable Class<?> typeOf(@PolyNull @Nullable Object obj) {
    return obj != null ? obj.getClass() : null;
  }

  private static <@Nullable R, @Nullable T> boolean deepEqualsCollection(Collection<R> c1,
      Collection<R> c2, Class<R> baseRefType, Function<? super R, T> resolver, boolean raw) {
    if (c1.size() != c2.size())
      return false;

    /*
     * TODO: Unordered comparison is painfully inefficient (O = c1.size * c2.size / 2), needs
     * sorting via custom comparator.
     */
    List<T> oc2 = c2.stream().map(resolver).collect(Collectors.toList());
    for (R r1 : c1) {
      T o1 = resolver.apply(r1);
      Iterator<T> itr2 = oc2.iterator();
      if (itr2.hasNext()) {
        T o2 = itr2.next();
        if (!deepEquals(o1, o2, baseRefType, resolver, raw))
          return false;

        itr2.remove();
      }
    }
    return true;
  }

  private static <@Nullable R, @Nullable T> boolean deepEqualsList(List<R> l1, List<R> l2,
      Class<R> baseRefType, Function<? super R, T> resolver, boolean raw) {
    if (l1.size() != l2.size())
      return false;

    for (int i = 0, size = l1.size(); i < size; i++) {
      if (!deepEqualsRef(l1.get(i), l2.get(i), baseRefType, resolver, raw))
        return false;
    }
    return true;
  }

  private static <@Nullable R, @Nullable T> boolean deepEqualsMap(Map<?, R> m1, Map<?, R> m2,
      Class<R> baseRefType, Function<? super R, T> resolver, boolean raw) {
    if (m1.size() != m2.size())
      return false;

    for (Object key : m1.keySet()) {
      if (!deepEqualsRef(m1.get(key), m2.get(key), baseRefType, resolver, raw))
        return false;
    }
    return true;
  }

  private static String fqn(@Nullable Object obj, boolean dotted) {
    return fqn(objTo(asType(obj), Class::getName), dotted);
  }

  private static String fqn(@Nullable String typeName, boolean dotted) {
    return typeName != null
        ? dotted ? typeName.replace('$', DOT) : typeName
        : STRING__NULL;
  }

  private static String sqn(@Nullable Object obj, boolean dotted) {
    return sqn(fqn(obj, false), dotted);
  }

  private static String sqn(@Nullable String typeName, boolean dotted) {
    return fqn(objTo(typeName, $ -> indexFound($.indexOf(DOT))
        ? $.substring($.lastIndexOf(DOT) + 1)
        : typeName), dotted);
  }

  private Objects() {
  }
}
