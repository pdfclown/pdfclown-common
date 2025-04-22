/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (Enums.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common> (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.util;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Objects.splitFqn;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Enumeration utilities.
 * <p>
 * This implementation covers multiple enumeration flavors:
 * </p>
 * <ul>
 * <li>{@linkplain Enum regular enumerations}</li>
 * <li>{@linkplain XtEnum domain-bound enumerations}</li>
 * <li>{@linkplain Xnum augmented enumerations}</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
public final class Enums {
  private static Map<Class<?>, Map<?, ?>> values = new HashMap<>();

  /**
   * Gets the constant matching the given code.
   *
   * @param <E>
   *          Constant type.
   * @param <K>
   *          Code type.
   * @param type
   *          Enumeration type.
   * @param code
   *          Code to match.
   * @param matcher
   *          Constant-vs-code matcher.
   * @return {@code null}, if no match was found.
   */
  public static <E, K> @Nullable E get(Class<E> type, K code,
      BiFunction<E, K, Boolean> matcher) {
    for (E value : map(type).values()) {
      if (matcher.apply(value, code))
        return value;
    }
    return null;
  }

  /**
   * Gets the constant matching the given code.
   *
   * @param <E>
   *          Constant type.
   * @param <K>
   *          Code type.
   * @param type
   *          Enumeration type.
   * @param code
   *          Code to match.
   * @param mapper
   *          Constant-to-code mapper.
   * @return {@code null}, if no match was found.
   */
  public static <E, K> @Nullable E get(Class<E> type, K code, Function<E, K> mapper) {
    return get(type, code, ($constant, $code) -> Objects.equals($code, mapper.apply($constant)));
  }

  /**
   * Gets the constant associated to the given key.
   *
   * @param <E>
   *          Constant type.
   * @param type
   *          Enumeration type.
   * @param key
   *          Key to match ({@link Enum#name() name} for {@link Enum}, {@link XtEnum#getCode() code}
   *          for {@link XtEnum}).
   * @return {@code null}, if no match was found.
   * @implNote Lookup is optimized through caching. As a consequence, {@link #map(Class)} returns
   *           the same map used here.
   */
  @SuppressWarnings("unchecked")
  public static <E> @Nullable E get(Class<E> type, @Nullable Object key) {
    var typeValues = values.get(type);
    return (E) (typeValues != null ? typeValues : map(type)).get(key);
  }

  /**
   * Gets the constant associated to the given fully qualified name.
   *
   * @param <E>
   *          Constant type.
   * @param fqn
   *          Fully qualified name.
   * @return {@code null}, if no match was found.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <E extends Enum> @Nullable E get(String fqn) {
    @NonNull
    String[] fqnParts = splitFqn(fqn);
    if (fqnParts[0].isEmpty())
      return null;

    try {
      return (E) Enum.valueOf((Class<E>) Class.forName(fqnParts[0]), fqnParts[1]);
    } catch (ClassNotFoundException ex) {
      return null;
    }
  }

  /**
   * @param <E>
   *          Constant type.
   * @param type
   *          Enumeration type.
   * @param key
   *          Key to match ({@link Enum#name() name} for {@link Enum}, {@link XtEnum#getCode() code}
   *          for {@link XtEnum}).
   * @throws IllegalArgumentException
   *           If there is no constant associated to {@code key}.
   */
  public static <E> E getOrThrow(Class<E> type, Object key) {
    @Nullable
    E value = get(type, key);
    if (value == null)
      throw wrongArg("key", requireNonNull(key), "No matching constant in {}", type);

    return value;
  }

  /**
   * Maps the given enumeration type to its default key definition ({@link Enum#name() name} for
   * {@link Enum}, {@link XtEnum#getCode() code} for {@link XtEnum}).
   * <p>
   * {@link Xnum}-derived types are mapped to their closest enumeration (ie, augmented enumeration
   * only in case of interfaces, otherwise their own enumeration). Eg, let's say that {@code MyXnum}
   * is an interface extending {@code Xnum}, whilst {@code MyEnum1} and {@code MyEnum2} are
   * enumerations implementing {@code MyXnum}: if {@code type} is {@code MyXnum}, its augmented
   * enumeration is retrieved as-is; on the contrary, if {@code type} is {@code MyEnum1} or
   * {@code MyEnum2}, only their respective enumeration is retrieved.
   * </p>
   * <p>
   * Because of caching, this method is idempotent.
   * </p>
   *
   * @param <E>
   *          Constant type.
   * @param <K>
   *          Key type.
   * @param type
   *          An {@link Enum}-derived implementation or an {@link Xnum}-derived interface.
   * @throws IllegalArgumentException
   *           if {@code type} is neither {@link Enum}- nor {@link Xnum}-derived.
   * @see #get(Class, Object)
   */
  @SuppressWarnings("unchecked")
  public static <E, K> Map<K, E> map(Class<E> type) {
    Map<K, E> typeValues = (Map<K, E>) values.get(type);
    if (typeValues == null) {
      // Augmented enumeration?
      if (Xnum.class.isAssignableFrom(type) && type.isInterface()) {
        values.put(type, typeValues = (Map<K, E>) requireNonNull(
            BaseXnum.get((Class<Xnum<K>>) type)).getConstants());
      } else if (type.isEnum()) {
        /*
         * TODO: make returned map unmodifiable
         */
        /*
         * NOTE: Map reference is immediately mapped into `values` composite map to ensure
         * possibly-concurrent enum loading doesn't interfere with this mapping (an early bug was
         * caused by a call to get(Class<E> type, T key) where the implementation of `type`
         * allocated the map as a static field, like this:
         *
         * private static final Map<String, CssProperty> VALUES = Enums.map(CssProperty.class);
         *
         * The Enums.map(Class<E> type) call reentered here while the map reference hadn't been
         * mapped yet. This fix decouples map building from associated enum loading.
         */
        values.put(type, typeValues = new HashMap<>());

        final Map<K, E> target = typeValues;
        @SuppressWarnings("null")
        final Function<E, K> keyMapper = XtEnum.class.isAssignableFrom(type)
            ? $ -> (K) ((XtEnum<?>) $).getCode()
            : $ -> (K) ((Enum<?>) $).name();
        map(type, Collector.of(() -> target,
            ($map, $element) -> $map.put(keyMapper.apply($element), $element),
            ($1, $2) -> $1 /* NOOP: Map combination makes no sense here */));
      } else
        throw wrongArg("type", type, "MUST be an Enum-derived implementation "
            + "or an Xnum-derived interface");
    }
    return typeValues;
  }

  /**
   * Maps the given enumeration type to a custom key definition.
   * <p>
   * NOTE: The returned map is NEVER cached; <i>if your key definition is default, call
   * {@link #map(Class)} instead</i>, so you can leverage caching.
   * </p>
   *
   * @param <E>
   *          Constant type.
   * @param <K>
   *          Key type.
   * @param type
   *          An {@link Enum}-derived implementation or an {@link Xnum}-derived interface.
   * @param keyMapper
   *          Custom key definition.
   * @throws IllegalArgumentException
   *           if {@code type} is neither {@link Enum}- nor {@link Xnum}-derived.
   */
  @SuppressWarnings("null")
  public static <E, K> Map<K, E> map(Class<E> type, Function<? super E, ? extends K> keyMapper) {
    return map(type, Collectors.toMap(keyMapper, Function.identity()));
  }

  @SuppressWarnings({ "unchecked", "null" })
  private static <E, K> Map<K, E> map(Class<E> type,
      Collector<E, ?, @NonNull Map<K, E>> collector) {
    Stream<E> stream;
    if (Xnum.class.isAssignableFrom(type) && type.isInterface()) {
      stream = (Stream<E>) BaseXnum.values((Class<Xnum<K>>) type).stream();
    } else if (type.isEnum()) {
      stream = Stream.of(type.getEnumConstants());
    } else
      throw wrongArg("type", type, "MUST be an Enum-derived implementation "
          + "or an Xnum-derived interface");

    return stream.collect(collector);
  }

  private Enums() {
  }
}
