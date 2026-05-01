/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UnitsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.measure;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.pdfclown.common.build.util.Tuple.tuple;

import java.util.Comparator;
import java.util.List;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Area;
import javax.measure.quantity.ElectricPotential;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.util.Tuple2;
import org.pdfclown.common.util.Aggregations;
import org.pdfclown.common.util.__test.BaseTest;
import tech.units.indriya.format.SimpleQuantityFormat;
import tech.units.indriya.format.SimpleUnitFormat;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings({ "unchecked", "rawtypes", "Convert2MethodRef" })
class UnitsTest extends BaseTest {
  private static final Comparator<Unit> COMPARATOR__UNIT = Comparator
      .<Unit, String>comparing(Unit::getSymbol, Comparator.nullsFirst(String::compareToIgnoreCase))
      .thenComparing(Unit::getName);

  private static final List<? extends Unit> UNITS = Units.getInstance().getUnits().stream()
      .peek($ -> assertThat($, isA(XtUnit.class)))
      .sorted(COMPARATOR__UNIT)
      .toList();

  /**
   * FIXME: Weirdly enough, {@link XtUnit} is NOT properly parsed by {@link SimpleQuantityFormat},
   * whilst {@link SimpleUnitFormat} parses the exact same unit (mi²) flawlessly (?!) — see
   * {@link #_unit_parsing()}.
   */
  @Test
  void _quantity_parsing() {
    Quantity quantity = SimpleQuantityFormat.getInstance().parse("50 mi²");

    // TODO: enable when parsing fixed.
    //    assertThat(quantity.getUnit(), is(Units.SQUARE_MILE));
    assertThat(quantity.getValue(), is(50));
  }

  @Test
  void _unit_parsing() {
    {
      Unit<?> unit = SimpleUnitFormat.getInstance().parse("mi²");
      Class<? extends Quantity> quantityType = Units.getQuantityType(unit);

      assertThat(quantityType, is(Area.class));
    }

    {
      // (see <https://github.com/unitsofmeasurement/indriya/issues/438>)
      Unit<?> unit = SimpleUnitFormat.getInstance().parse("V");
      Class<? extends Quantity> quantityType = Units.getQuantityType(unit);

      assertThat(quantityType, is(ElectricPotential.class));
      assertThat(XtUnit.of(unit).getQuantityType(), is(ElectricPotential.class));
    }
  }

  @Test
  void getFactor_Unit() {
    combinationVerifier.verify(
        (unit) -> Units.getFactor(unit),
        List.of("unit"),
        // unit
        Aggregations.<Unit>list()
            .withAll(UNITS)
            .with(tech.units.indriya.unit.Units.WEEK)
            .with(tech.units.indriya.unit.Units.AMPERE));
  }

  @Test
  void getFactor_Unit_Unit() {
    tupleVerifier.verify(
        (unit, target) -> Units.getFactor(unit, target),
        List.of("unit", "target"),
        List.<Tuple2<Unit, Unit>>of(
            tuple(Units.METRE, Units.METRE),
            tuple(Units.METRE, Units.CENTIMETRE),
            tuple(Units.FOOT, Units.KILOMETRE),
            tuple(Units.FOOT, Units.SQUARE_FOOT),
            tuple(Units.ACRE, Units.HECTARE),
            tuple(Units.CELSIUS, Units.KELVIN),
            tuple(Units.HOUR, Units.KELVIN)));
  }

  @Test
  void getOffset_Unit() {
    combinationVerifier.verify(
        (unit) -> Units.getOffset(unit),
        List.of("unit"),
        // unit
        UNITS);
  }

  @Test
  void getQuantityType() {
    combinationVerifier.verify(
        (unit) -> ((XtUnit) unit).getQuantityType(),
        List.of("unit"),
        // unit
        UNITS);
  }

  @Test
  void getQuantityType__external() {
    combinationVerifier.verify(
        (unit) -> Units.getQuantityType(unit),
        List.of("unit"),
        // unit
        tech.units.indriya.unit.Units.getInstance().getUnits().stream()
            .sorted(COMPARATOR__UNIT)
            .collect(toUnmodifiableList()));
  }
}
