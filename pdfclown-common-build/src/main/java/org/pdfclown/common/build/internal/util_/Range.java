/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Range.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_;

import static org.pdfclown.common.build.internal.util_.Objects.isSameType;

import java.util.Comparator;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.util_.annot.Immutable;

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
public final class Range<T> {
  /**
   * Interval endpoint.
   *
   * @param <T>
   *          Value type.
   * @author Stefano Chizzolini
   */
  @Immutable
  public static final class Bound<T> {
    @SuppressWarnings({ "rawtypes" })
    private static final Bound UNBOUND = new Bound<>(null, false);

    @SuppressWarnings("unchecked")
    public static <T> Bound<T> of(@Nullable T value, boolean inclusive) {
      return value != null ? new Bound<>(value, inclusive) : (Bound<T>) UNBOUND;
    }

    private final T value;
    private final boolean inclusive;

    /**
     */
    private Bound(T value, boolean inclusive) {
      this.value = value;
      this.inclusive = inclusive;
    }

    @Override
    @SuppressWarnings("EqualsDoesntCheckParameterClass")
    public boolean equals(@Nullable Object o) {
      if (o == this)
        return true;
      else if (!isSameType(o, this))
        return false;

      var that = (Bound<?>) o;
      assert that != null;
      return that.inclusive == this.inclusive
          && Objects.equals(that.value, this.value);
    }

    public T getValue() {
      return value;
    }

    @Override
    public int hashCode() {
      var ret = Boolean.hashCode(inclusive);
      if (value != null) {
        ret ^= value.hashCode();
      }
      return ret;
    }

    public boolean isInclusive() {
      return inclusive;
    }
  }

  public static <T> Range<T> exclusive(@Nullable T low, @Nullable T high) {
    return new Range<>(Bound.of(low, false), Bound.of(high, false));
  }

  public static <T> Range<T> inclusive(@Nullable T low, @Nullable T high) {
    return new Range<>(Bound.of(low, true), Bound.of(high, true));
  }

  public static <T> Range<T> of(Bound<T> low, Bound<T> high) {
    return new Range<>(low, high);
  }

  public static <T> Range<T> of(@Nullable T low, @Nullable T high) {
    return inclusive(low, high);
  }

  private final Bound<T> high;
  private final Bound<T> low;

  private Range(Bound<T> low, Bound<T> high) {
    this.low = low;
    this.high = high;
  }

  /**
   * Gets whether the given value is contained within this interval.
   *
   * @apiNote Comparison is performed via natural order, so values are required to be
   *          {@link Comparable}.
   */
  public boolean contains(T value) {
    return contains(value, Comparator.naturalOrder());
  }

  /**
   * Gets whether the given value is contained within this interval.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean contains(T value, Comparator comparator) {
    int compare = low.value != null ? comparator.compare(value, low.value) : 1;
    if (!(compare > 0 || (compare == 0 && low.inclusive)))
      return false;

    compare = high.value != null ? comparator.compare(value, high.value) : -1;
    return compare < 0 || (compare == 0 && high.inclusive);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (super.equals(o))
      return true;
    else if (!isSameType(o, this))
      return false;

    var that = (Range<?>) o;
    assert that != null;
    return Objects.equals(that.low, this.low)
        && Objects.equals(that.high, this.high);
  }

  /**
   * Upper bound.
   */
  public T getHigh() {
    return high.value;
  }

  /**
   * Lower bound.
   */
  public T getLow() {
    return low.value;
  }

  @Override
  public int hashCode() {
    return low.hashCode() ^ high.hashCode();
  }

  /**
   * Whether this interval is closed above (ie, inclusive).
   */
  public boolean isHighInclusive() {
    return high.inclusive;
  }

  /**
   * Whether this interval is closed below (ie, inclusive).
   */
  public boolean isLowInclusive() {
    return low.inclusive;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + (low.inclusive ? "[" : "(") + (low.value != null ? low.value : "-INF") + ","
        + (high.value != null ? high.value : "+INF") + (high.inclusive ? "]" : ")");
  }
}
