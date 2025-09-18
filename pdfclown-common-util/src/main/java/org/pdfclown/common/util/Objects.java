/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Objects.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.lang.Math.subtractExact;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;
import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.pdfclown.common.util.Booleans.strToBool;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Strings.BACKSLASH;
import static org.pdfclown.common.util.Strings.COLON;
import static org.pdfclown.common.util.Strings.COMMA;
import static org.pdfclown.common.util.Strings.CURLY_BRACE_CLOSE;
import static org.pdfclown.common.util.Strings.CURLY_BRACE_OPEN;
import static org.pdfclown.common.util.Strings.DOT;
import static org.pdfclown.common.util.Strings.DQUOTE;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.INDEX__NOT_FOUND;
import static org.pdfclown.common.util.Strings.NULL;
import static org.pdfclown.common.util.Strings.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Strings.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.Strings.SPACE;
import static org.pdfclown.common.util.Strings.SQUOTE;
import static org.pdfclown.common.util.reflect.Reflects.stackFrame;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.function.FailableSupplier;
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
import org.pdfclown.common.util.annot.PolyNull;
import org.pdfclown.common.util.annot.Unmodifiable;
import org.pdfclown.common.util.regex.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Objects {
  /**
   * Hierarchical type comparator.
   *
   * @author Stefano Chizzolini
   */
  @SuppressWarnings("rawtypes")
  public static class HierarchicalTypeComparator implements Comparator<Class> {
    /**
     * Additional ordering criteria for {@link HierarchicalTypeComparator}.
     *
     * @author Stefano Chizzolini
     */
    public static class Priorities {
      /**
       * Type comparator based on explicit priorities.
       *
       * @author Stefano Chizzolini
       */
      public static class TypePriorityComparator implements Comparator<Class> {
        private int minPriority;
        private int maxPriority;
        private final Map<Class, Integer> priorities = new HashMap<>();

        private TypePriorityComparator() {
        }

        @Override
        public int compare(Class o1, Class o2) {
          return priorities.getOrDefault(o1, 0) - priorities.getOrDefault(o2, 0);
        }

        /**
         * Gets the priority associated to the type.
         *
         * @return {@code 0}, if no priority is associated to {@code type}.
         */
        public int get(Class<?> type) {
          return getOrDefault(type, 0);
        }

        /**
         * Gets the priority associated to the type.
         *
         * @return {@code defaultValue}, if no priority is associated to {@code type}.
         */
        public int getOrDefault(Class<?> type, int defaultValue) {
          return priorities.getOrDefault(type, defaultValue);
        }

        /**
         * Associates a priority to the type.
         */
        public TypePriorityComparator set(int priority, Class<?> type) {
          if (priority < minPriority) {
            subtractExact(priority, maxPriority) /* Checks underflow */;
            subtractExact(maxPriority, priority) /* Checks overflow */;
            minPriority = priority;
          } else if (priority > maxPriority) {
            subtractExact(minPriority, priority) /* Checks underflow */;
            subtractExact(priority, minPriority) /* Checks overflow */;
            maxPriority = priority;
          }
          priorities.put(type, priority);
          return this;
        }

        /**
         * Associates a priority to the types.
         */
        public TypePriorityComparator set(int priority, Class<?>... types) {
          for (var type : types) {
            set(priority, type);
          }
          return this;
        }

        /**
         * Associates a sequence of priorities to the types.
         */
        public TypePriorityComparator setInOrder(int priority, Class<?>... types) {
          for (var type : types) {
            set(priority++, type);
          }
          return this;
        }
      }

      private static final Comparator<Class> COMPARATOR__INTERFACE_PRIORITY =
          new Comparator<>() {
            @Override
            public int compare(Class o1, Class o2) {
              return interfacePriority(o1) - interfacePriority(o2);
            }

            private int interfacePriority(Class<?> type) {
              return type.isInterface() ? 1 : 0;
            }
          };

      /**
       * Compares types by explicit priority.
       */
      public static TypePriorityComparator explicitPriority() {
        return new TypePriorityComparator();
      }

      /**
       * Compares types prioritizing concrete types over interfaces.
       */
      public static Comparator<Class> interfacePriority() {
        return COMPARATOR__INTERFACE_PRIORITY;
      }
    }

    @SuppressWarnings("unchecked")
    private static final HierarchicalTypeComparator INSTANCE =
        new HierarchicalTypeComparator(
            ($1, $2) -> {
              // Prioritize specialized types over super types!
              if ($1.isAssignableFrom($2))
                return 1;
              else if ($2.isAssignableFrom($1))
                return -1;
              else
                return 0;
            });

    /**
     * Basic hierarchical type comparator.
     */
    public static HierarchicalTypeComparator get() {
      return INSTANCE;
    }

    private final Comparator<Class> base;

    private HierarchicalTypeComparator(Comparator<Class> base) {
      this.base = base;
    }

    @Override
    public int compare(Class o1, Class o2) {
      if (o1 == o2)
        return 0;

      int ret = base.compare(o1, o2);
      if (ret == 0)
        throw unexpected(ret, "unable to decide over type priority between `{}` and `{}`", o1, o2);

      return ret;
    }

    @Override
    public HierarchicalTypeComparator thenComparing(Comparator<? super Class> other) {
      return new HierarchicalTypeComparator(base.thenComparing(other));
    }
  }

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

  private static final Pattern PATTERN__QUALIFIED_TO_STRING =
      Pattern.compile("((?:\\w+[.$])*\\w+)([^\\w.$].*)?");

  private static final Set<Class<?>> BASIC_TYPES = Set.of(
      Boolean.class,
      Byte.class,
      Character.class,
      Double.class,
      Float.class,
      Integer.class,
      Long.class,
      Short.class,
      String.class,
      Void.class);

  private static final Map<Object, Object> proxies = new HashMap<>();

  @SuppressWarnings("rawtypes")
  private static final Map<String, Class> proxyTypes = new HashMap<>();

  /**
   * Gets the ancestors of the type, ordered by {@linkplain HierarchicalTypeComparator#get() default
   * comparator}.
   */
  @SuppressWarnings("rawtypes")
  public static @Unmodifiable Iterable<Class> ancestors(Class type) {
    return ancestors(type, HierarchicalTypeComparator.get());
  }

  /**
   * Gets the ancestors of the type, ordered by {@code comparator}.
   */
  @SuppressWarnings("rawtypes")
  public static @Unmodifiable Iterable<Class> ancestors(Class type,
      HierarchicalTypeComparator comparator) {
    return ancestors(type, comparator, Set.of(), false);
  }

  /**
   * Gets the ancestors of the type, ordered by {@code comparator}.
   *
   * @param stoppers
   *          Types at which to stop ancestor hierarchy traversal.
   * @param stopperExclusive
   *          Whether stopped types are excluded from returned ancestors.
   */
  @SuppressWarnings("rawtypes")
  public static @Unmodifiable Iterable<Class> ancestors(Class type,
      HierarchicalTypeComparator comparator, Set<Class> stoppers, boolean stopperExclusive) {
    var ret = new TreeSet<>(comparator);

    // 1. Interfaces related to `type`.
    for (var e : type.getInterfaces()) {
      collectTypeAndAncestorInterfaces(e, ret, stoppers, stopperExclusive);
    }

    // 2. Ancestor concrete types and related interfaces.
    Class superType = type;
    //noinspection StatementWithEmptyBody
    while ((superType = superType.getSuperclass()) != null
        && collectTypeAndAncestorInterfaces(superType, ret, stoppers, stopperExclusive)) {
      // NOP
    }
    return unmodifiableSet(ret);
  }

  /**
   * Gets whether the object matches the other one according to the predicate.
   * <p>
   * NOTE: This method is redundant; it's intended as a placeholder in case the implementer expects
   * further objects to be added later.
   * </p>
   */
  public static <T, U> boolean any(@Nullable T obj, BiPredicate<@Nullable T, @Nullable U> predicate,
      @Nullable U other1) {
    return predicate.test(obj, other1);
  }

  /**
   * Gets whether the object matches any of the others according to the predicate.
   */
  public static <T, U> boolean any(@Nullable T obj, BiPredicate<@Nullable T, @Nullable U> predicate,
      @Nullable U other1, @Nullable U other2) {
    return predicate.test(obj, other1)
        || predicate.test(obj, other2);
  }

  /**
   * Gets whether the object matches any of the others according to the predicate.
   *
   * @implNote Because of the limited expressiveness of varargs, in order to force the caller to
   *           specify at least two other objects we have to declare two corresponding parameters
   *           ({@code other1} and {@code other2}) in the signature — despite its inherent ugliness,
   *           this is the standard way Java API itself deals with such cases.
   */
  @SafeVarargs
  public static <T, U> boolean any(@Nullable T obj, BiPredicate<@Nullable T, @Nullable U> predicate,
      @Nullable U other1, @Nullable U other2, @Nullable U... others) {
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
   * Gets whether the object matches any of the others according to the predicate.
   */
  public static <T, U> boolean any(@Nullable T obj, BiPredicate<@Nullable T, @Nullable U> predicate,
      @Nullable U other1, @Nullable U other2, @Nullable U other3) {
    return predicate.test(obj, other1)
        || predicate.test(obj, other2)
        || predicate.test(obj, other3);
  }

  /**
   * Gets whether the object matches any of the others according to the predicate.
   */
  public static <T, U> boolean any(@Nullable T obj, BiPredicate<@Nullable T, @Nullable U> predicate,
      @Nullable U other1, @Nullable U other2, @Nullable U other3, @Nullable U other4) {
    return predicate.test(obj, other1)
        || predicate.test(obj, other2)
        || predicate.test(obj, other3)
        || predicate.test(obj, other4);
  }

  /**
   * Gets whether the object matches any of the others according to the predicate.
   */
  public static <T, U> boolean any(@Nullable T obj, BiPredicate<@Nullable T, @Nullable U> predicate,
      @Nullable U other1, @Nullable U other2, @Nullable U other3, @Nullable U other4,
      @Nullable U other5) {
    return predicate.test(obj, other1)
        || predicate.test(obj, other2)
        || predicate.test(obj, other3)
        || predicate.test(obj, other4)
        || predicate.test(obj, other5);
  }

  /**
   * Gets the type corresponding to the object.
   * <p>
   * Same as {@link #typeOf(Object) typeOf(..)}, unless {@code obj} is {@link Class} (in such case,
   * returns itself).
   * </p>
   */
  public static @PolyNull @Nullable Class<?> asType(@PolyNull @Nullable Object obj) {
    return obj != null ? (obj instanceof Class ? (Class<?>) obj : obj.getClass()) : null;
  }

  /**
   * Quietly closes the object.
   *
   * @return {@code obj}
   * @see #quiet(FailableConsumer, Object)
   */
  public static <T extends AutoCloseable> @PolyNull @Nullable T closeQuiet(
      @PolyNull @Nullable T obj) {
    return quiet(AutoCloseable::close, obj);
  }

  /**
   * Gets whether the two resolved elements are equal.
   *
   * @param <R>
   *          Reference (that is, unresolved element) type.
   * @param <T>
   *          Object (that is, resolved element) type.
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
  public static <R, T> boolean deepEquals(@Nullable T o1, @Nullable T o2, Class<R> baseRefType,
      Function<? super R, @Nullable T> resolver, boolean raw) {
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
   *          Reference (that is, unresolved element) type.
   * @param <T>
   *          Object (that is, resolved element) type.
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
   * Gets whether the object is equal to or contains the other one.
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
   * Gets whether the object is equal to the other one or undefined.
   */
  public static boolean equalsOrNull(@Nullable Object obj, @Nullable Object other) {
    return obj == null || java.util.Objects.equals(obj, other);
  }

  /**
   * Gets the fully-qualified class name of the object.
   * <p>
   * Corresponds to the {@linkplain Class#getName() class name}; the class is resolved from
   * {@code obj} through {@link #asType(Object)}.
   * </p>
   *
   * @return {@value Strings#NULL}, if {@code obj} is undefined.
   * @see #fqnd(Object)
   * @see #sqn(Object)
   */
  public static String fqn(@Nullable Object obj) {
    return fqn(obj, false);
  }

  /**
   * Gets the dotted fully-qualified class name of the object.
   * <p>
   * Corresponds to the {@linkplain Class#getName() class name}, replacing inner-class separators
   * ({@code $}) with dots (for example, {@code my.package.MyTopLevel.MyOuterClass.MyInnerClass});
   * the class is resolved from {@code obj} through {@link #asType(Object)}.
   * </p>
   *
   * @return {@value Strings#NULL}, if {@code obj} is undefined.
   * @see #fqn(Object)
   * @see #sqnd(Object)
   */
  public static String fqnd(@Nullable Object obj) {
    return fqn(obj, true);
  }

  /**
   * Ensures the class name has inner-class separators ({@code $}) replaced with dots.
   * <p>
   * No syntactic check is applied to {@code typeName}.
   * </p>
   *
   * @return {@value Strings#NULL}, if {@code typeName} is undefined.
   * @see #sqnd(String)
   */
  public static String fqnd(@Nullable String typeName) {
    return fqn(typeName, true);
  }

  /**
   * Gets the object corresponding to the literal string.
   * <p>
   * NOTE: This method is complementary to {@link #toLiteralString(Object)} only for primitive type
   * representations.
   * </p>
   *
   * @return
   *         <ul>
   *         <li>{@code null} — if {@code s} corresponds to {@value Strings#NULL} or is undefined
   *         ({@code null})</li>
   *         <li>{@link Boolean} — if {@code s} corresponds to a boolean literal ({@code "true"} or
   *         {@code "false"}, case-insensitive)</li>
   *         <li>{@link Number} — if {@code s} corresponds to a
   *         {@linkplain NumberUtils#createNumber(String) numeric value}</li>
   *         <li>{@link Character} — if {@code s} corresponds to a single-quoted character</li>
   *         <li>{@link String} (unescaped) — if {@code s} corresponds to a single- or double-quoted
   *         string</li>
   *         <li>{@link String} (as-is) — otherwise</li>
   *         </ul>
   * @see #toLiteralString(Object)
   */
  public static @Nullable Object fromLiteralString(@Nullable String s) {
    // Undefined, or null literal?
    if (s == null || (s = s.trim()).equals(NULL))
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
              return s.charAt(1);
            // Character literal with escape?
            else if (c == SQUOTE && s.length() == 4 && s.charAt(1) == '\\')
              return s.charAt(2);
            // String literal.
            else
              return LITERAL_STRING_UNESCAPE.translate(s.substring(1, s.length() - 1));
          }
          break;
        default:
          // NOP
      }
    }

    {
      var bool = strToBool(s);
      // Boolean literal?
      if (bool != null)
        return bool;
    }

    try {
      // Numeric literal.
      return NumberUtils.createNumber(s);
    } catch (NumberFormatException ex) {
      // Generic literal.
      return s;
    }
  }

  /**
   * Initializes the class.
   * <p>
   * Contrary to {@link Class#forName(String)}, it is safe from exceptions.
   * </p>
   *
   * @return Whether the initialization succeeded.
   */
  public static boolean init(Class<?> type) {
    return init(type.getName());
  }

  /**
   * Initializes the class.
   * <p>
   * Contrary to {@link Class#forName(String)}, it is safe from exceptions.
   * </p>
   *
   * @return Whether the initialization succeeded.
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
   * Initializes the class.
   * <p>
   * This is the unchecked equivalent of {@link Class#forName(String)}.
   * </p>
   *
   * @throws RuntimeException
   *           if class initialization fails.
   */
  public static void initElseThrow(Class<?> type) {
    initElseThrow(type.getName());
  }

  /**
   * Initializes the class.
   * <p>
   * This is the unchecked equivalent of {@link Class#forName(String)}.
   * </p>
   *
   * @throws RuntimeException
   *           if class initialization fails.
   */
  public static void initElseThrow(String typeName) {
    try {
      Class.forName(typeName);
    } catch (ClassNotFoundException ex) {
      throw runtime(ex);
    }
  }

  /**
   * Gets whether the type is basic.
   * <p>
   * <b>Basic types</b> comprise primitive wrappers and {@link String}.
   * </p>
   */
  public static boolean isBasic(@Nullable Class<?> type) {
    return BASIC_TYPES.contains(type);
  }

  /**
   * Gets whether the object belongs to a basic type.
   * <p>
   * <b>Basic types</b> comprise primitive wrappers and {@link String}.
   * </p>
   */
  public static boolean isBasic(@Nullable Object obj) {
    return isBasic(typeOf(obj));
  }

  /**
   * Gets whether the objects are the same instance, or both null.
   */
  public static boolean isSame(@Nullable Object o1, @Nullable Object o2) {
    return o1 == o2;
  }

  /**
   * Gets whether the objects belong to exactly the same type.
   */
  public static boolean isSameType(@Nullable Object o1, @Nullable Object o2) {
    return typeOf(o1) == typeOf(o2);
  }

  /**
   * Gets the locale corresponding to the language tag.
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
   * Normalizes the locale.
   *
   * @return {@linkplain Locale#getDefault() Default} if {@code locale} is undefined.
   */
  public static Locale localeNorm(@Nullable Locale locale) {
    return requireNonNullElseGet(locale, Locale::getDefault);
  }

  /**
   * Asserts the object is non-null.
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
   * Casts the object to a target type.
   * <p>
   * Contrary to {@link Class#cast(Object)}, this method is safe without assignment compatibility.
   * </p>
   *
   * @return {@code null}, if {@code obj} is assignment-incompatible with {@code type}.
   */
  @SuppressWarnings("unchecked")
  public static <T, R extends T> @Nullable R objCast(@Nullable T obj, Class<R> type) {
    return type.isInstance(obj) ? (R) obj : null;
  }

  /**
   * Applies an operation to the object.
   *
   * @return {@code obj}
   * @see #quiet(FailableConsumer, Object)
   */
  public static <T> @PolyNull @Nullable T objDo(@PolyNull @Nullable T obj, Consumer<? super T> op) {
    if (obj != null) {
      op.accept(obj);
    }
    return obj;
  }

  /**
   * Returns the object, if not null; otherwise, the supplied default.
   * <p>
   * Contrary to {@link java.util.Objects#requireNonNullElseGet(Object, Supplier)}, this method
   * doesn't enforce its result to be non-null.
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
   * Maps the object.
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
   * Maps the object.
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
    return requireNonNullElse(objTo(obj, mapper), defaultResult);
  }

  /**
   * Maps the object.
   * <p>
   * Contrary to {@link java.util.Objects#requireNonNullElseGet(Object, Supplier)}, this method
   * doesn't enforce its result to be non-null.
   * </p>
   *
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   * @param defaultSupplier
   *          Result supplier if {@code obj} or {@code mapper}'s result are undefined.
   */
  public static <T, R> @Nullable R objToElseGet(@Nullable T obj,
      Function<? super @NonNull T, ? extends R> mapper, Supplier<? extends R> defaultSupplier) {
    R ret;
    return obj != null && (ret = mapper.apply(obj)) != null ? ret : defaultSupplier.get();
  }

  /**
   * Wraps the object in a null-aware container.
   */
  public static <T> Optional<T> opt(@Nullable T obj) {
    return Optional.ofNullable(obj);
  }

  /**
   * Quietly applies an operation to the object.
   *
   * @return {@code obj}
   * @see #objDo(Object, Consumer)
   */
  public static <T> @PolyNull @Nullable T quiet(FailableConsumer<T, ?> op,
      @PolyNull @Nullable T obj) {
    return quiet(op, obj, null);
  }

  /**
   * Quietly applies the operation to the object.
   *
   * @param exceptionHandler
   *          Handles the exceptions thrown by {@code op}.
   * @return {@code obj}
   * @see #objDo(Object, Consumer)
   */
  public static <T> @PolyNull @Nullable T quiet(FailableConsumer<T, ?> op,
      @PolyNull @Nullable T obj, @Nullable Consumer<Throwable> exceptionHandler) {
    if (obj != null) {
      try {
        op.accept(obj);
      } catch (Throwable ex) {
        if (exceptionHandler != null) {
          exceptionHandler.accept(ex);
        }
      }
    }
    return obj;
  }

  /**
   * Quietly runs the operation.
   */
  public static void quiet(FailableRunnable<?> op) {
    quiet(op, null);
  }

  /**
   * Quietly runs the operation.
   *
   * @param exceptionHandler
   *          Handles the exceptions thrown by {@code op}.
   */
  public static void quiet(FailableRunnable<?> op, @Nullable Consumer<Throwable> exceptionHandler) {
    try {
      op.run();
    } catch (Throwable ex) {
      if (exceptionHandler != null) {
        exceptionHandler.accept(ex);
      }
    }
  }

  /**
   * Splits the fully-qualified name into package and class name parts.
   *
   * @return Two-part string array, where the first item is empty if {@code typeName} has no
   *         package.
   */
  @SuppressWarnings("null")
  public static String[] splitFqn(String typeName) {
    int pos = typeName.lastIndexOf(DOT);
    return pos >= 0
        ? new String[] { typeName.substring(0, pos), typeName.substring(pos + 1) }
        : new String[] { EMPTY, typeName };
  }

  /**
   * Gets the simply-qualified class name of the object.
   * <p>
   * Corresponds to the {@linkplain Class#getSimpleName() simple class name} qualified with its
   * enclosing classes till the top level (for example,
   * {@code MyTopLevel$MyOuterClass$MyInnerClass}); the class is resolved from {@code obj} through
   * {@link #asType(Object)}.
   * </p>
   *
   * @return {@value Strings#NULL}, if {@code obj} is undefined.
   * @see #sqnd(Object)
   * @see #fqn(Object)
   */
  public static String sqn(@Nullable Object obj) {
    return sqn(obj, false);
  }

  /**
   * Gets the simply-qualified class name from the name.
   * <p>
   * Corresponds to the {@linkplain Class#getSimpleName() simple class name} qualified with its
   * enclosing classes till the top level (for example,
   * {@code MyTopLevel$MyOuterClass$MyInnerClass}).
   * </p>
   * <p>
   * No syntactic check is applied to {@code typeName}.
   * </p>
   *
   * @return {@value Strings#NULL}, if {@code typeName} is undefined.
   * @see #sqnd(String)
   */
  public static String sqn(@Nullable String typeName) {
    return sqn(typeName, false);
  }

  /**
   * Gets the dotted simply-qualified class name of the object.
   * <p>
   * Corresponds to the {@linkplain Class#getSimpleName() simple class name} qualified with its
   * enclosing classes till the top level, replacing inner-class separators ({@code $}) with dots
   * (for example, {@code MyTopLevel.MyOuterClass.MyInnerClass}); the class is resolved from
   * {@code obj} through {@link #asType(Object)}.
   * </p>
   *
   * @return {@value Strings#NULL}, if {@code obj} is undefined.
   * @see #sqn(Object)
   * @see #fqnd(Object)
   */
  public static String sqnd(@Nullable Object obj) {
    return sqn(obj, true);
  }

  /**
   * Gets the dotted simply-qualified class name from the name.
   * <p>
   * Corresponds to the {@linkplain Class#getSimpleName() simple class name} qualified with its
   * enclosing classes till the top level, replacing inner-class separators ({@code $}) with dots
   * (for example, {@code MyTopLevel.MyOuterClass.MyInnerClass}).
   * </p>
   * <p>
   * No syntactic check is applied to {@code typeName}.
   * </p>
   *
   * @return {@value Strings#NULL}, if {@code typeName} is undefined.
   * @see #sqn(String)
   * @see #fqnd(String)
   */
  public static String sqnd(@Nullable String typeName) {
    return sqn(typeName, true);
  }

  /**
   * Maps the object to its literal string representation (that is, inclusive of markers such as
   * quotes).
   *
   * @return
   *         <ul>
   *         <li>{@value Strings#NULL} — if {@code obj} is undefined</li>
   *         <li>{@link Object#toString()}, suffixed with literal qualifier — if {@code obj} is
   *         {@link Float} or {@link Long} (disambiguation against respective default literal types,
   *         {@link Double} or {@link Integer})</li>
   *         <li>{@link Object#toString()}, escaped and wrapped with single quotes — if {@code obj}
   *         is {@link Character}</li>
   *         <li>{@link Object#toString()}, escaped and wrapped with double quotes — if {@code obj}
   *         is {@link String}</li>
   *         <li>{@link Class#getName()} — if {@code obj} is {@link Class}
   *         ({@link Class#getSimpleName()} for common types, under {@code java.lang} package)</li>
   *         <li>{@link Object#toString()}, as-is — otherwise</li>
   *         </ul>
   * @see #fromLiteralString(String)
   */
  public static String toLiteralString(@Nullable Object obj) {
    return toLiteralString(obj, false);
  }

  /**
   * Maps the object to its literal string representation (that is, inclusive of markers such as
   * quotes).
   *
   * @param nonBasicForced
   *          Whether, in case {@code obj} is non-{@linkplain #isBasic(Object) basic}, the resulting
   *          string is treated as {@link String} (that is, escaped and double-quoted).
   * @return
   *         <ul>
   *         <li>{@value Strings#NULL} — if {@code obj} is undefined</li>
   *         <li>{@link Object#toString()}, suffixed with literal qualifier — if {@code obj} is
   *         {@link Float} or {@link Long} (disambiguation against respective default literal types,
   *         {@link Double} or {@link Integer})</li>
   *         <li>{@link Object#toString()}, escaped and wrapped with single quotes — if {@code obj}
   *         is {@link Character}</li>
   *         <li>{@link Object#toString()}, escaped and wrapped with double quotes — if {@code obj}
   *         is {@link String}</li>
   *         <li>{@link Class#getName()} — if {@code obj} is {@link Class}
   *         ({@link Class#getSimpleName()} for common types, under {@code java.lang} package)</li>
   *         <li>{@link Object#toString()}, as-is — otherwise</li>
   *         </ul>
   * @see #fromLiteralString(String)
   */
  public static String toLiteralString(@Nullable Object obj, boolean nonBasicForced) {
    if (obj == null)
      return NULL;
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
      return S + SQUOTE + ((Character) obj == SQUOTE ? S + BACKSLASH : EMPTY) + obj + SQUOTE;
    else if (!(obj instanceof String)) {
      if (obj instanceof Class) {
        /*
         * NOTE: The names of classes belonging to common packages are simplified to reduce noise.
         */
        obj = objTo((Class<?>) obj, $ -> $.getPackageName().startsWith("java.lang")
            ? $.getSimpleName()
            : $.getName());
      } else {
        obj = obj.toString();
      }
      if (!nonBasicForced)
        return (String) obj;
    }
    return S + DQUOTE + LITERAL_STRING_ESCAPE.translate((String) obj) + DQUOTE;
  }

  /**
   * Maps the object to its string representation, ensuring its qualification at least with its
   * {@linkplain Class#getSimpleName() simple class name}.
   *
   * @return
   *         <ul>
   *         <li>{@value Strings#NULL} — if {@code obj} is undefined</li>
   *         <li><code>obj.toString()</code> — if it contains the simple class name of
   *         {@code obj}</li>
   *         <li>{@link #sqnd(Object) sqnd(obj)}<code> + " {" + obj.toString() + "}"</code> —
   *         otherwise</li>
   *         </ul>
   */
  public static String toQualifiedString(@Nullable Object obj) {
    if (obj == null)
      return NULL;

    String objString = obj.toString();
    String sqnd = sqnd(obj);
    return Patterns.match(PATTERN__QUALIFIED_TO_STRING, objString)
        .filter($ -> {
          // Qualification corresponds to simple class name?
          if ($.group(1).equals(obj.getClass().getSimpleName()))
            return true;

          // Qualification corresponds to either simply- or fully-qualified class name?
          var norm = $.group(1).replace('$', DOT);
          return norm.equals(sqnd) || norm.equals(fqnd(obj));
        }).isPresent()
            ? objString
            : sqnd + SPACE + CURLY_BRACE_OPEN + objString + CURLY_BRACE_CLOSE;
  }

  /**
   * Maps the object to its string representation, normalizing its qualification with the
   * {@linkplain #sqnd(Object) dotted simply-qualified class name}.
   *
   * @return Considering the {@linkplain Matcher pattern match} of
   *         {@code obj.}{@link Object#toString() toString()} as formed by two groups (qualification
   *         and attributes):
   *         <ul>
   *         <li>{@value Strings#NULL} — if {@code obj} is undefined</li>
   *         <li><code>group()</code> — if its qualification equals the dotted simply-qualified
   *         class name of {@code obj}</li>
   *         <li><code>sqnd(obj) + group(2)</code> — if its qualification contains the
   *         {@linkplain Class#getSimpleName() simple class name} of {@code obj}</li>
   *         <li><code>sqnd(obj) + " {" + group() + "}"</code> — otherwise</li>
   *         </ul>
   */
  public static String toSqnQualifiedString(@Nullable Object obj) {
    if (obj == null)
      return NULL;

    String objString = obj.toString();
    String sqnd = sqnd(obj);
    return Patterns.match(PATTERN__QUALIFIED_TO_STRING, objString)
        .map($ -> $.group(1).equals(sqnd) ? $.group()
            : sqnd + ($.group(1).endsWith(obj.getClass().getSimpleName())
                ? objToElse(stripToNull($.group(2)), $$ -> S + SPACE + $$, EMPTY)
                : S + SPACE + CURLY_BRACE_OPEN + $.group() + CURLY_BRACE_CLOSE))
        .orElseThrow();
  }

  /**
   * Gets the string representation of the object, along with its features.
   * <p>
   * NOTE: {@code null} features are ignored.
   * </p>
   */
  public static String toStringWithFeatures(Object obj, @Nullable Object... features) {
    var b = new StringBuilder(sqnd(obj)).append(SPACE).append(ROUND_BRACKET_OPEN);
    var filled = false;
    for (var feature : features) {
      if (feature == null) {
        continue;
      }

      if (filled) {
        b.append(SPACE);
      }
      b.append(feature);
      filled = true;
    }
    return b.append(ROUND_BRACKET_CLOSE).toString();
  }

  /**
   * Gets the string representation of the object, along with its features.
   * <p>
   * NOTE: {@code null} feature is ignored.
   * </p>
   */
  public static String toStringWithFeatures(Object obj, @Nullable Object feature) {
    var b = new StringBuilder(sqnd(obj)).append(SPACE);
    if (feature instanceof Collection || feature instanceof Map) {
      b.append(feature);
    } else {
      b.append(ROUND_BRACKET_OPEN);
      if (feature != null) {
        b.append(feature);
      }
      b.append(ROUND_BRACKET_CLOSE);
    }
    return b.toString();
  }

  /**
   * Gets the string representation of the object, along with its features.
   * <p>
   * NOTE: {@code null} features are ignored.
   * </p>
   */
  public static String toStringWithFeatures(Object obj, @Nullable Object feature1,
      @Nullable Object feature2) {
    var b = new StringBuilder(sqnd(obj)).append(SPACE).append(ROUND_BRACKET_OPEN);
    var filled = false;
    if (feature1 != null) {
      filled = true;
      b.append(feature1);
    }
    if (feature2 != null) {
      if (filled) {
        b.append(SPACE);
      }
      b.append(feature2);
    }
    return b.append(ROUND_BRACKET_CLOSE).toString();
  }

  /**
   * Gets the string representation of the object, along with its features.
   * <p>
   * NOTE: {@code null} features are ignored.
   * </p>
   */
  public static String toStringWithFeatures(Object obj, @Nullable Object feature1,
      @Nullable Object feature2, @Nullable Object feature3) {
    var b = new StringBuilder(sqnd(obj)).append(SPACE).append(ROUND_BRACKET_OPEN);
    var filled = false;
    if (feature1 != null) {
      filled = true;
      b.append(feature1);
    }
    if (feature2 != null) {
      if (filled) {
        b.append(SPACE);
      } else {
        filled = true;
      }
      b.append(feature2);
    }
    if (feature3 != null) {
      if (filled) {
        b.append(SPACE);
      }
      b.append(feature3);
    }
    return b.append(ROUND_BRACKET_CLOSE).toString();
  }

  /**
   * Gets the string representation of the object, along with its properties.
   *
   * @param properties
   *          Properties (key-value pairs; keys MUST be non-null).
   */
  public static String toStringWithProperties(Object obj, @Nullable Object... properties) {
    var b = new StringBuilder(sqnd(obj)).append(SPACE).append(CURLY_BRACE_OPEN);
    for (int i = 0; i < properties.length;) {
      if (i > 0) {
        b.append(COMMA).append(SPACE);
      }
      b.append(requireNonNull(properties[i++])).append(COLON).append(SPACE).append(properties[i++]);
    }
    return b.append(CURLY_BRACE_CLOSE).toString();
  }

  /**
   * Gets the string representation of the object, along with its properties.
   */
  public static String toStringWithProperties(Object obj, String k1, @Nullable Object v1) {
    return sqnd(obj) + SPACE + CURLY_BRACE_OPEN
        + requireNonNull(k1) + COLON + SPACE + v1
        + CURLY_BRACE_CLOSE;
  }

  /**
   * Gets the string representation of the object, along with its properties.
   */
  public static String toStringWithProperties(Object obj, String k1, @Nullable Object v1, String k2,
      @Nullable Object v2) {
    return sqnd(obj) + SPACE + CURLY_BRACE_OPEN
        + requireNonNull(k1) + COLON + SPACE + v1 + COMMA + SPACE
        + requireNonNull(k2) + COLON + SPACE + v2
        + CURLY_BRACE_CLOSE;
  }

  /**
   * Gets the string representation of the object, along with its properties.
   */
  public static String toStringWithProperties(Object obj, String k1, @Nullable Object v1, String k2,
      @Nullable Object v2, String k3, @Nullable Object v3) {
    return sqnd(obj) + SPACE + CURLY_BRACE_OPEN
        + requireNonNull(k1) + COLON + SPACE + v1 + COMMA + SPACE
        + requireNonNull(k2) + COLON + SPACE + v2 + COMMA + SPACE
        + requireNonNull(k3) + COLON + SPACE + v3
        + CURLY_BRACE_CLOSE;
  }

  /**
   * Tries the supplier.
   *
   * @return Result of {@code supplier}, or {@code null} if failed.
   */
  public static <R> @Nullable R tryGet(FailableSupplier<? extends @Nullable R, ?> supplier) {
    try {
      return supplier.get();
    } catch (Throwable ex) {
      return null;
    }
  }

  /**
   * Tries the supplier.
   *
   * @param defaultResult
   *          Result in case {@code supplier} fails or its result is undefined.
   * @return Result of {@code supplier}, if not {@code null}; otherwise, {@code defaultResult}.
   */
  public static <R> R tryGetElse(FailableSupplier<? extends @Nullable R, ?> supplier,
      R defaultResult) {
    return requireNonNullElse(tryGet(supplier), defaultResult);
  }

  /**
   * Gets the type corresponding to the fully-qualified name, resolved in the loading context of the
   * current class and initialized.
   *
   * @return {@code null}, if no type matched {@code name}.
   */
  public static @Nullable Class<?> type(String name) {
    return type(name, null);
  }

  /**
   * Gets the type corresponding to the fully-qualified name, resolved in the loading context and
   * initialized.
   *
   * @param loadingHint
   *          Object whose {@link ClassLoader} must be used as loading context ({@code null}, to
   *          resolve with the class loader of the current class).
   * @return {@code null}, if no type matched {@code name}.
   */
  public static @Nullable Class<?> type(String name, @Nullable Object loadingHint) {
    try {
      if (loadingHint != null) {
        @SuppressWarnings("DataFlowIssue" /* @PolyNull */)
        ClassLoader loader = loadingHint instanceof ClassLoader
            ? (ClassLoader) loadingHint
            : asType(loadingHint).getClassLoader();
        return Class.forName(name, true, loader);
      } else
        return Class.forName(name);
    } catch (ClassNotFoundException ex) {
      return null;
    }
  }

  /**
   * Gets the type of the object.
   *
   * @see #asType(Object)
   */
  public static @PolyNull @Nullable Class<?> typeOf(@PolyNull @Nullable Object obj) {
    return obj != null ? obj.getClass() : null;
  }

  /**
   * Cross-casts the object to the caller's {@linkplain ClassLoader class loader}.
   * <p>
   * (see {@linkplain #xcast(Object, Object) main overload} for further information)
   * </p>
   *
   * @param <T>
   *          Target type (useful for final casting, but irrelevant for actual cross-casting).
   * @param obj
   *          Source object.
   */
  public static <T> @Nullable T xcast(Object obj) {
    return xcast(obj, null);
  }

  /**
   * Cross-casts the object to the target {@linkplain ClassLoader class loader}.
   * <p>
   * Split types (that is, binary-incompatible types with same fully-qualified name and different
   * class loaders) are transparently bridged through proxy, providing a convenient alternative to
   * manual reflection.
   * </p>
   * <img src="doc-files/proxy.svg" alt="UML diagram of object proxy">
   *
   * @param <T>
   *          Target type (useful for final casting, but irrelevant for actual cross-casting).
   * @param obj
   *          Source object.
   * @param loadingHint
   *          Object whose class loader must be used as target ({@code null}, for the caller's class
   *          loader).
   */
  public static <T> @Nullable T xcast(Object obj, @Nullable Object loadingHint) {
    return xcast(obj, loadingHint, null, null);
  }

  /**
   * Flattens the object, extracting the source object (proxy base) in case of
   * {@linkplain #xcast(Object, Object) cross-cast proxy}.
   *
   * @see #xcast(Object, Object)
   */
  @SuppressWarnings("unchecked")
  public static <T> T xflat(Object obj) {
    try {
      // Try to extract the source object (proxy base)!
      Field baseField = obj.getClass().getField("proxyBase");
      return (T) baseField.get(obj);
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
        | IllegalAccessException e) {
      // No cross-cast proxy, just a regular object.
      return (T) obj;
    }
  }

  /**
   * Gets whether the object is a cross-instance of the type.
   * <p>
   * A {@linkplain #xcast(Object, Object) cross-instance} is a superset of a regular instance: it
   * encompasses both its own class hierarchy and the class hierarchies of its split types (that is,
   * binary-incompatible types with same fully-qualified name and different {@linkplain ClassLoader
   * class loader}s). With the regular {@code instanceof} operator, an object can relate only to the
   * class hierarchy within its own class loader; on the contrary, this method takes care to
   * cross-cast {@code obj} to the same class loader as {@code type} before evaluating it.
   * </p>
   *
   * @param type
   *          Supertype candidate (the evaluation happens within its class loader).
   * @implNote {@code obj} is {@linkplain #xflat(Object) flattened} to extract the source object
   *           (proxy base), then its type is cross-cast to {@code type}'s class loader, and
   *           evaluated.
   * @see #xcast(Object, Object)
   */
  public static boolean xinstanceof(Object obj, Class<?> type) {
    // Extract the source object!
    Object base = xflat(obj);
    // Cross-cast the source type to target type's class loader!
    Class<?> xbaseType = nonNull(xcast(base.getClass(), type));
    return type.isAssignableFrom(xbaseType);
  }

  /**
   * Recursively collects the type and its interfaces until stopped.
   * <p>
   * If {@code type} is contained in {@code stoppers}, this operation stops; in such case,
   * {@code type} is collected only if not {@code stopperExclusive}, while its interfaces are
   * ignored.
   * </p>
   *
   * @param ancestors
   *          Target collection.
   * @param stoppers
   *          Types at which to stop ancestor hierarchy traversal.
   * @param stopperExclusive
   *          Whether stopped types are excluded from iterated ancestors.
   * @return Whether this operation completed (that is, it wasn't stopped).
   */
  @SuppressWarnings("rawtypes")
  private static boolean collectTypeAndAncestorInterfaces(Class type, Set<Class> ancestors,
      Set<Class> stoppers, boolean stopperExclusive) {
    var ret = !stoppers.contains(type);
    if ((ret || !stopperExclusive) && ancestors.add(type)) {
      if (ret) {
        for (var e : type.getInterfaces()) {
          collectTypeAndAncestorInterfaces(e, ancestors, stoppers, stopperExclusive);
        }
      }
    }
    return ret;
  }

  private static <R, T> boolean deepEqualsCollection(Collection<@Nullable R> c1,
      Collection<@Nullable R> c2, Class<R> baseRefType,
      Function<? super @Nullable R, @Nullable T> resolver, boolean raw) {
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

  private static <R, T> boolean deepEqualsList(List<@Nullable R> l1, List<@Nullable R> l2,
      Class<R> baseRefType, Function<? super @Nullable R, @Nullable T> resolver, boolean raw) {
    if (l1.size() != l2.size())
      return false;

    for (int i = 0, size = l1.size(); i < size; i++) {
      if (!deepEqualsRef(l1.get(i), l2.get(i), baseRefType, resolver, raw))
        return false;
    }
    return true;
  }

  private static <R, T> boolean deepEqualsMap(Map<?, @Nullable R> m1, Map<?, @Nullable R> m2,
      Class<R> baseRefType, Function<? super @Nullable R, @Nullable T> resolver, boolean raw) {
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
        : NULL;
  }

  private static boolean isAutoInstantiable(Class<?> type) {
    try {
      type.getDeclaredConstructor();
      return true;
    } catch (NoSuchMethodException | SecurityException e) {
      return false;
    }
  }

  private static String sqn(@Nullable Object obj, boolean dotted) {
    return sqn(fqn(obj, false), dotted);
  }

  private static String sqn(@Nullable String typeName, boolean dotted) {
    return fqn(objTo(typeName, $ -> {
      int index = $.lastIndexOf(DOT);
      return index != INDEX__NOT_FOUND ? $.substring(index + 1) : $;
    }), dotted);
  }

  /**
   * Cross-casts the object to the target {@linkplain ClassLoader class loader}.
   * <p>
   * (see {@linkplain #xcast(Object, Object) main overload} for further information)
   * </p>
   *
   * @param <T>
   *          Target type (useful for final casting, but irrelevant for actual cross-casting).
   * @param obj
   *          Source object.
   * @param loadingHint
   *          Object whose class loader must be used as target ({@code null}, for the caller's class
   *          loader).
   * @param sourceTypeHint
   *          Suggested source cast type.
   * @param targetTypeHint
   *          Suggested target cast type. Useful whenever {@code obj} may be an instance of a
   *          subclass of an expected type, like a parameter type in a target method (the assumption
   *          is that the target method shall work with the interface of the parameter type only,
   *          without casting to any of its subclasses, so the proxy can implement just that
   *          interface — if that's not the case, such subclasses must be added to the classpath
   *          visible to the target class loader).
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static <T> @Nullable T xcast(@Nullable Object obj, @Nullable Object loadingHint,
      @Nullable Class<?> sourceTypeHint, @Nullable Class<?> targetTypeHint) {
    if (obj == null)
      return null;

    if (loadingHint == null) {
      loadingHint = stackFrame($ -> $.getDeclaringClass() != Objects.class)
          .orElseThrow(() -> runtime("Caller NOT FOUND"))
          .getDeclaringClass();
    }

    if (obj instanceof Class) {
      var type = (Class<?>) obj;
      if (type.isPrimitive())
        return (T) type;
      else if (type.isArray()) {
        Class<?> targetComponentType = type(type.getComponentType().getName(), loadingHint);
        return (T) (targetComponentType == type.getComponentType() ? type
            : Array.newInstance(targetComponentType, 0).getClass());
      } else
        return (T) type(((Class<?>) obj).getName(), loadingHint);
    }

    // Extract the source object in case `obj` is a proxy!
    obj = xflat(obj);

    Class<?> candidateSourceType = obj.getClass();
    // Source object is array?
    if (candidateSourceType.isArray())
      return (T) xcastArray((Object[]) obj, loadingHint, null);
    // Source object is enum?
    else if (candidateSourceType.isEnum())
      return (T) Enum.valueOf((Class<Enum>) (sourceTypeHint != null && sourceTypeHint.isEnum()
          ? sourceTypeHint
          : nonNull(type(candidateSourceType.getName(), loadingHint))), ((Enum<?>) obj).name());
    // Source object is compatible with target?
    else if (candidateSourceType == type(candidateSourceType.getName(), loadingHint))
      return (T) obj;

    // Map the incompatible source object to proxy!
    var ret = (T) proxies.get(obj);
    if (ret != null)
      return ret;

    var proxyType = proxyTypes.get(candidateSourceType.getName());
    if (proxyType == null) {
      final Class<?> sourceType;
      final Class<?> targetType;
      {
        // Source type candidate is unsuitable for proxying?
        if (!isAutoInstantiable(candidateSourceType)) {
          if (sourceTypeHint != null && sourceTypeHint != Object.class
              && isAutoInstantiable(sourceTypeHint)) {
            candidateSourceType = sourceTypeHint;
          } else {
            /*
             * Find source type suitable for proxying!
             *
             * HACK: This ugly block was added to work around the notorious type erasure of return
             * types of generic methods.
             *
             * TODO: See <https://github.com/raphw/byte-buddy/issues/1725> to implement a more
             * robust solution; in particular, this hint
             * (<https://github.com/raphw/byte-buddy/issues/1725#issuecomment-2464707817>):
             *
             * " Simply load a class using `TypeDescription.ForLoadedType.of(...)`. Then navigate
             * the hierarchy as you would with the reflection API where generic types are resolved
             * transparently. To resolve the return type of methods, use `MethodGraph.Compiler`. "
             */
            sourceTypeHint = candidateSourceType;
            while (sourceTypeHint != Object.class) {
              var interfaceTypes = sourceTypeHint.getInterfaces();
              if (interfaceTypes.length > 0) {
                candidateSourceType = interfaceTypes[0];
                break;
              }

              sourceTypeHint = sourceTypeHint.getSuperclass();
            }
          }
        }
        sourceType = candidateSourceType;
        targetType = java.util.Objects.requireNonNullElse(type(sourceType.getName(), loadingHint),
            targetTypeHint);
        if (sourceType == targetType)
          return (T) obj;
      }

      proxyType = proxyTypes.computeIfAbsent(sourceType.getName(),
          $typeKey -> {
            try {
              return new ByteBuddy()
                  .subclass(targetType, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                  .defineField("proxyBase", Object.class, Modifier.PUBLIC + Modifier.FINAL)
                  .defineConstructor(Visibility.PUBLIC)
                  .withParameters(Object.class)
                  .intercept(MethodCall.invoke((!targetType.isInterface() ? targetType
                      : Object.class).getDeclaredConstructor()).onSuper()
                      .andThen(FieldAccessor.ofField("proxyBase").setsArgumentAt(0)))
                  .method(ElementMatchers.any())
                  .intercept(InvocationHandlerAdapter.of(new InvocationHandler() {
                    @Override
                    public @Nullable Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                      // Retrieve the source object associated to this proxy instance!
                      var base = proxy.getClass().getDeclaredField("proxyBase").get(proxy);
                      var baseType = base.getClass();

                      /*
                       * Get the source method corresponding to the invoked proxy method!
                       *
                       * NOTE: (proxy) `method` is binary-incompatible with (source) `base`, so its
                       * argument types must be cross-cast to their base counterparts in order to
                       * find a matching method signature in `baseType`.
                       */
                      Class<?>[] baseParamTypes = xcastArray(method.getParameterTypes(), baseType,
                          null);
                      Method baseMethod = null;
                      try {
                        baseMethod = baseType.getMethod(method.getName(), baseParamTypes);
                      } catch (Exception ex) {
                        /*
                         * NOTE: No matching public method found, so we have to hack through the
                         * non-public interface, hoping for the best.
                         */
                        var type = baseType;
                        do {
                          try {
                            baseMethod = type.getDeclaredMethod(method.getName(), baseParamTypes);
                            break;
                          } catch (Exception ex1) {
                            // NOP
                          }
                        } while ((type = type.getSuperclass()) != null);
                        if (baseMethod == null)
                          throw runtime("Base method NOT FOUND", ex);

                        baseMethod.setAccessible(true);
                      }

                      /*
                       * Delegate the invocation to the source object!
                       *
                       * NOTE: Return value is cross-cast in turn, to ensure any binary-incompatible
                       * type is encapsulated into its own proxy.
                       */
                      Object[] baseArgs = xcastArray(args, baseType, baseParamTypes);
                      return xcast(baseMethod.invoke(base, baseArgs), null,
                          method.getReturnType(), null);
                    }
                  }))
                  .make()
                  .load(targetType.getClassLoader())
                  /*
                   * TODO: Injection sometimes fails (for example, if debugging Jada on a JPMS
                   * project. Remove if unsolvable.
                   */
                  //.load(targetType.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                  .getLoaded();
            } catch (Exception ex) {
              throw runtime(ex);
            }
          });
    }

    try {
      proxies.put(obj, ret = (T) proxyType.getConstructor(Object.class).newInstance(obj));
    } catch (Exception ex) {
      throw runtime(ex);
    }
    return ret;
  }

  /**
   * {@linkplain #xcast(Object, Object) Cross-casts} the object to the target
   * {@linkplain ClassLoader class loader}.
   *
   * @param <T>
   *          Target type (useful for final casting, but irrelevant for actual cross-casting).
   * @param objs
   *          Source objects.
   * @param loadingHint
   *          Object whose class loader must be used as target ({@code null}, for the current class
   *          loader).
   * @param targetTypeHints
   *          Target cast types suggested for the respective item in {@code objs}. Useful whenever
   *          {@code objs} may be instances of subclasses of expected types, like parameter types in
   *          a target method (the assumption is that the target method shall work with the
   *          interface of the parameter type only, without casting to any of its subclasses, so the
   *          proxy can implement just that interface — if that's not the case, such subclasses must
   *          be added to the classpath visible to the target class loader).
   */
  @SuppressWarnings("unchecked")
  private static <T> T @Nullable [] xcastArray(Object @Nullable [] objs,
      @Nullable Object loadingHint, Class<?> @Nullable [] targetTypeHints) {
    if (objs == null || objs.length == 0)
      return (T[]) objs;

    T[] ret = null;
    for (int i = 0; i < objs.length; i++) {
      var obj = objs[i];
      var targetObj = (T) xcast(obj, loadingHint, null,
          targetTypeHints != null ? targetTypeHints[i] : null);
      if (ret != null) {
        ret[i] = targetObj;
      } else if (targetObj != obj) {
        ret = (T[]) Array.newInstance(xcast(objs.getClass().getComponentType(), loadingHint),
            objs.length);
        if (i > 0) {
          i = -1;
        } else {
          ret[i] = targetObj;
        }
      }
    }
    return ret != null ? ret : (T[]) objs;
  }

  private Objects() {
  }
}
