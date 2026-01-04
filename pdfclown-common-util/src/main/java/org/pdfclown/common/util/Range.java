/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Range.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.lang.Math.signum;
import static org.pdfclown.common.util.Chars.COMMA;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.SQUARE_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.SQUARE_BRACKET_OPEN;
import static org.pdfclown.common.util.Conditions.requireEqual;
import static org.pdfclown.common.util.Conditions.requireState;
import static org.pdfclown.common.util.Objects.isSameType;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Immutable;

/**
 * Interval of comparable objects.
 *
 * @param <T>
 *          Value type.
 *          <p>
 *          NOTE: In order to support open inheritance and {@link Enum} (which has its own
 *          hard-coded natural ordering), this parameter isn't required to be {@link Comparable};
 *          values are evaluated either by {@linkplain #contains(Object) natural order} (if value
 *          type is intrinsically comparable) or via {@linkplain #contains(Object, Comparator)
 *          explicit comparator}.
 *          </p>
 * @author Stefano Chizzolini
 */
@Immutable
public class Range<T> {
  /**
   * Interval endpoint.
   *
   * @param <T>
   *          Value type.
   * @author Stefano Chizzolini
   */
  @Immutable
  public static final class Endpoint<T> {
    @SuppressWarnings({ "rawtypes" })
    static final Endpoint UNBOUND = new Endpoint<>(null, false);

    @SuppressWarnings("unchecked")
    public static <T> Endpoint<T> of(@Nullable T value, boolean closed) {
      return value != null ? new Endpoint<>(value, closed) : (Endpoint<T>) UNBOUND;
    }

    private final boolean closed;
    private final @Nullable T value;

    /**
     */
    private Endpoint(@Nullable T value, boolean closed) {
      this.value = value;
      this.closed = closed;
    }

    @Override
    @SuppressWarnings("EqualsDoesntCheckParameterClass")
    public boolean equals(@Nullable Object o) {
      if (o == this)
        return true;
      else if (!isSameType(o, this))
        return false;

      var that = (Endpoint<?>) o;
      assert that != null;
      return that.closed == this.closed
          && Objects.equals(that.value, this.value);
    }

    /**
     * Endpoint value.
     *
     * @throws IllegalStateException
     *           if not {@link #isBounded() bounded}.
     */
    public T getValue() {
      return requireState(value, "UNBOUNDED");
    }

    @Override
    public int hashCode() {
      var ret = Boolean.hashCode(closed);
      if (value != null) {
        ret ^= value.hashCode();
      }
      return ret;
    }

    /**
     * Whether this endpoint is limited rather than infinite.
     */
    public boolean isBounded() {
      return value != null;
    }

    /**
     * Whether {@link #getValue() value} is included.
     */
    public boolean isClosed() {
      return closed;
    }

    @Override
    public String toString() {
      return Objects.toString(value, "∞");
    }

    StringBuilder toString(int sign, StringBuilder b) {
      if (sign < 0) {
        b.append(closed ? SQUARE_BRACKET_OPEN : ROUND_BRACKET_OPEN);
      }

      if (value != null) {
        b.append(value);
      } else {
        b.append(sign < 0 ? "-" : sign > 0 ? "+" : EMPTY).append("∞");
      }

      if (sign > 0) {
        b.append(closed ? SQUARE_BRACKET_CLOSE : ROUND_BRACKET_CLOSE);
      }

      return b;
    }
  }

  static class NumericRange<T extends Number> extends Range<T> {
    private static final Comparator<Number> COMPARATOR =
        ($1, $2) -> (int) signum($1.doubleValue() - $2.doubleValue());

    NumericRange(Endpoint<T> lowerEndpoint, Endpoint<T> upperEndpoint) {
      super(lowerEndpoint, upperEndpoint);
    }

    @Override
    public boolean contains(Object value) {
      return contains(value, COMPARATOR);
    }
  }

  private static final Map<Class<? extends Number>, Range<? extends Number>> normal =
      new HashMap<>();

  /**
   * New range, inclusive of lower endpoint.
   */
  public static <T> Range<T> atLeast(T lower) {
    return new Range<>(Endpoint.of(lower, true), Endpoint.of(null, false));
  }

  /**
   * New range, inclusive of upper endpoint.
   */
  public static <T> Range<T> atMost(T upper) {
    return new Range<>(Endpoint.of(null, false), Endpoint.of(upper, true));
  }

  /**
   * New range, inclusive of both endpoints.
   *
   * @param lower
   *          ({@code null}, for unbounded endpoint)
   * @param upper
   *          ({@code null}, for unbounded endpoint)
   * @throws ArgumentException
   *           if arguments are numbers of different types (allowing them would cause ambiguities on
   *           value comparison — see also the observations in {@link #numeric(Range)}).
   */
  @SuppressWarnings("unchecked")
  public static <T> Range<T> closed(@Nullable T lower, @Nullable T upper) {
    if (lower instanceof Number && upper != null) {
      var type = (Class<? extends Number>) requireEqual(upper.getClass(), lower.getClass(),
          "upper.class");
      if (((Number) lower).doubleValue() == 0 && ((Number) upper).doubleValue() == 1)
        return (Range<T>) normal(type);
    }

    return new Range<>(Endpoint.of(lower, true), Endpoint.of(upper, true));
  }

  /**
   * New range, exclusive of lower endpoint.
   */
  public static <T> Range<T> greaterThan(T lower) {
    return new Range<>(Endpoint.of(lower, false), Endpoint.of(null, false));
  }

  /**
   * New range, exclusive of upper endpoint.
   */
  public static <T> Range<T> lessThan(T upper) {
    return new Range<>(Endpoint.of(null, false), Endpoint.of(upper, false));
  }

  /**
   * Gets the normal range (that is {@code  0} to {@code  1}, inclusive) for the type.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Number> Range<T> normal(Class<T> type) {
    return (Range<T>) normal.computeIfAbsent(type, $ -> new Range<>(
        Endpoint.of(Numbers.to(0, type), true),
        Endpoint.of(Numbers.to(1, type), true)));
  }

  /**
   * Gets the numeric range equivalent to the range.
   * <p>
   * A numeric range provides mathematical comparison across the {@link Number} hierarchy,
   * overcoming the limitations of natural comparison, which works within the same boxed type only
   * (for example, {@link Float} cannot be compared directly with {@link Integer} or {@link Double},
   * so the number {@code 0.5f} will be considered NOT in the integer range {@code [0,1]}).
   * </p>
   */
  public static <T extends Number> Range<T> numeric(Range<T> range) {
    return range instanceof NumericRange ? range : new NumericRange<>(range.lower, range.upper);
  }

  /**
   * New range.
   */
  public static <T> Range<T> of(Endpoint<T> lower, Endpoint<T> upper) {
    return new Range<>(lower, upper);
  }

  /**
   * New range, exclusive of both endpoints.
   *
   * @param lower
   *          ({@code null}, for unbounded endpoint)
   * @param upper
   *          ({@code null}, for unbounded endpoint)
   */
  public static <T> Range<T> open(@Nullable T lower, @Nullable T upper) {
    return new Range<>(Endpoint.of(lower, false), Endpoint.of(upper, false));
  }

  private final Endpoint<T> upper;
  private final Endpoint<T> lower;

  private Range(Endpoint<T> lower, Endpoint<T> upper) {
    this.lower = lower;
    this.upper = upper;
  }

  /**
   * Gets whether the value is contained within this interval.
   */
  public boolean contains(Object value) {
    return contains(value, Comparator.naturalOrder());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (super.equals(o))
      return true;
    else if (!isSameType(o, this))
      return false;

    var that = (Range<?>) o;
    assert that != null;
    return Objects.equals(that.lower, this.lower)
        && Objects.equals(that.upper, this.upper);
  }

  /**
   * Lower endpoint.
   */
  public Endpoint<T> getLower() {
    return lower;
  }

  /**
   * Upper endpoint.
   */
  public Endpoint<T> getUpper() {
    return upper;
  }

  @Override
  public int hashCode() {
    return lower.hashCode() ^ upper.hashCode();
  }

  @Override
  public String toString() {
    var b = new StringBuilder();
    {
      lower.toString(-1, b).append(COMMA);
      upper.toString(1, b);
    }
    return b.toString();
  }

  /**
   * Gets whether the value is contained within this interval, according to the comparator.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected boolean contains(Object value, Comparator comparator) {
    int compare = lower.isBounded()
        ? comparator.compare(value, lower.getValue())
        : 1;
    if (compare < 0 || (compare == 0 && !lower.isClosed()))
      return false;

    compare = upper.isBounded()
        ? comparator.compare(value, upper.getValue())
        : -1;
    return compare < 0 || (compare == 0 && upper.isClosed());
  }
}
