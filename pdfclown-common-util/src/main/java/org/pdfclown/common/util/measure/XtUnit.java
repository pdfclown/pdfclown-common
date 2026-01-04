/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (XtUnit.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.measure;

import static org.pdfclown.common.util.Conditions.requireState;
import static org.pdfclown.common.util.Conditions.requireType;
import static org.pdfclown.common.util.measure.Units.unwrap;
import static org.pdfclown.common.util.measure.Units.wrap;

import java.util.Map;
import javax.measure.Dimension;
import javax.measure.Prefix;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Immutable;
import tech.units.indriya.AbstractUnit;

/**
 * Extended {@linkplain Unit measurement unit}.
 * <p>
 * Provides additional functionality on top of {@linkplain AbstractUnit JSR 385's reference
 * implementation}:
 * </p>
 * <ul>
 * <li>{@linkplain #getQuantityType() quantity type} resolution</li>
 * <li>direct quantity value conversion {@linkplain #to(Unit, double) to} and
 * {@linkplain #from(Unit, double) from} this unit (useful any time explicit quantity instantiation
 * is unnecessary)</li>
 * </ul>
 *
 * @param <Q>
 *          Quantity type measured by this unit.
 * @author Stefano Chizzolini
 */
@Immutable
public class XtUnit<Q extends Quantity<Q>> extends AbstractUnit<Q> {
  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unchecked")
  public static <Q extends Quantity<Q>> XtUnit<Q> of(Unit<Q> base) {
    return new XtUnit<Q>(requireType(base, AbstractUnit.class));
  }

  final AbstractUnit<Q> base;

  protected XtUnit(AbstractUnit<Q> base) {
    super(base.getSymbol());

    this.base = base;

    setName(base.getName());
  }

  @Override
  public int compareTo(Unit<Q> that) {
    return base.compareTo(unwrap(that));
  }

  @Override
  public XtUnit<Q> divide(double divisor) {
    return wrap(base.divide(divisor));
  }

  @Override
  @SuppressWarnings("rawtypes")
  public boolean equals(@Nullable Object obj) {
    if (this == obj)
      return true;
    else if (obj == null)
      return false;
    else if (getClass() != obj.getClass())
      return false;

    return base.equals(((XtUnit) obj).base);
  }

  /**
   * Converts the value from the unit.
   *
   * @param source
   *          Source unit.
   * @param sourceValue
   *          Value expressed in {@code source} unit.
   * @return Value expressed in this unit.
   */
  public double from(Unit<Q> source, double sourceValue) {
    return source.getConverterTo(base).convert(sourceValue);
  }

  @Override
  public Map<? extends Unit<?>, Integer> getBaseUnits() {
    return base.getBaseUnits();
  }

  @Override
  public Dimension getDimension() {
    return base.getDimension();
  }

  /**
   * Quantity type measured by this unit.
   */
  @SuppressWarnings("unchecked")
  public Class<Q> getQuantityType() {
    return requireState((Class<Q>) Units.getQuantityType(this), "Mapping missing");
  }

  @Override
  public String getSymbol() {
    /*
     * NOTE: In case `base` is `ProductUnit`, the symbol is undefined, so we have to derive it from
     * its string representation (see also `tech.units.indriya.format.SimpleUnitFormat`).
     */
    return super.getSymbol() != null ? super.getSymbol() : base.toString();
  }

  @Override
  public UnitConverter getSystemConverter() {
    return base.getSystemConverter();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + base.hashCode();
    return result;
  }

  @Override
  public boolean isEquivalentTo(Unit<Q> that) {
    return base.isEquivalentTo(unwrap(that));
  }

  @Override
  public boolean isSystemUnit() {
    return base.isSystemUnit();
  }

  @Override
  public XtUnit<Q> multiply(double multiplier) {
    return wrap(base.multiply(multiplier));
  }

  @Override
  public Unit<?> pow(int n) {
    return base.pow(n);
  }

  @Override
  public XtUnit<Q> prefix(Prefix prefix) {
    return wrap(base.prefix(prefix));
  }

  @Override
  public XtUnit<Q> shift(double offset) {
    return wrap(base.shift(offset));
  }

  /**
   * Converts the value to the unit.
   *
   * @param target
   *          Target unit.
   * @param sourceValue
   *          Value expressed in this unit.
   * @return Value expressed in {@code target} unit.
   */
  public double to(Unit<Q> target, double sourceValue) {
    return base.getConverterTo(target).convert(sourceValue);
  }

  @Override
  public String toString() {
    /*
     * NOTE: We cannot rely on the default implementation, as
     * `tech.units.indriya.format.SimpleUnitFormat` expects its own `AbstractUnit` subtypes to
     * resolve the associated symbol.
     */
    return getSymbol();
  }

  @Override
  protected Unit<Q> toSystemUnit() {
    return base.getSystemUnit();
  }
}
