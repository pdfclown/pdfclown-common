/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UnitsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.measure;

import static java.util.Arrays.asList;
import static java.util.Map.entry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.util.Aggregations.list;
import static org.pdfclown.common.util.Objects.nonNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Area;
import javax.measure.quantity.ElectricPotential;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Argument;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.build.test.assertion.Assertions.Failure;
import org.pdfclown.common.util.Objects;
import org.pdfclown.common.util.__test.BaseTest;
import tech.units.indriya.format.SimpleQuantityFormat;
import tech.units.indriya.format.SimpleUnitFormat;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
class UnitsTest extends BaseTest {
  private static final double DBL_DELTA = 1e-4;

  private static final Comparator<Unit> COMPARATOR__UNIT = Comparator
      .<Unit, String>comparing(Unit::getSymbol, Comparator.nullsFirst(String::compareToIgnoreCase))
      .thenComparing(Unit::getName);

  private static final List<? super XtUnit<?>> UNITS = Units.getInstance().getUnits().stream()
      .peek($ -> assertThat($, isA(XtUnit.class)))
      .map(XtUnit.class::cast)
      .sorted(COMPARATOR__UNIT)
      .collect(Collectors.toUnmodifiableList());

  static Stream<Arguments> getFactor_Unit() {
    return argumentsStream(
        simpleArgumentsStreamConfig(),
        // expected
        java.util.Arrays.asList(
            // [1] unit[0]: 'a (Are)'
            100.0,
            // [2] unit[1]: 'ac (Acre)'
            4046.8564224,
            // [3] unit[2]: 'cm (Centimetre)'
            0.01,
            // [4] unit[3]: 'cm² (Square cen. . .'
            1.0E-4,
            // [5] unit[4]: 'd (Day)'
            86400.0,
            // [6] unit[5]: 'deg (Degree)'
            0.017453292519943295,
            // [7] unit[6]: 'ft (Foot)'
            0.3048,
            // [8] unit[7]: 'ft (US survey foot)'
            0.3048006,
            // [9] unit[8]: 'ft² (Square foot)'
            0.09290304,
            // [10] unit[9]: 'ft² (US survey . . .'
            0.09290340576036,
            // [11] unit[10]: 'gon (Gradian)'
            0.015707963267948967,
            // [12] unit[11]: 'h (Hour)'
            3600.0,
            // [13] unit[12]: 'ha (Hectare)'
            10000.0,
            // [14] unit[13]: 'Hz (Hertz)'
            1.0,
            // [15] unit[14]: 'in (Inch)'
            0.0254,
            // [16] unit[15]: 'in² (Square inch)'
            6.4516E-4,
            // [17] unit[16]: 'K (Kelvin)'
            1.0,
            // [18] unit[17]: 'kHz (Kilohertz)'
            1000.0,
            // [19] unit[18]: 'km (Kilometre)'
            1000.0,
            // [20] unit[19]: 'km² (Square kil. . .'
            1000000.0,
            // [21] unit[20]: 'ks (Kilosecond)'
            1000.0,
            // [22] unit[21]: 'm (Metre)'
            1.0,
            // [23] unit[22]: 'M (Nautical mile)'
            1852.0,
            // [24] unit[23]: 'MHz (Megahertz)'
            1000000.0,
            // [25] unit[24]: 'mHz (Millihertz)'
            0.001,
            // [26] unit[25]: 'mi (Mile)'
            1609.344,
            // [27] unit[26]: 'min (Minute)'
            60.0,
            // [28] unit[27]: 'mi² (Square mile)'
            2589988.110336,
            // [29] unit[28]: 'mm (Millimetre)'
            0.001,
            // [30] unit[29]: 'mm² (Square mil. . .'
            1.0E-6,
            // [31] unit[30]: 'ms (Millisecond)'
            0.001,
            // [32] unit[31]: 'm² (Square metre)'
            1.0,
            // [33] unit[32]: 'pc (Pica)'
            0.004233333333333334,
            // [34] unit[33]: 'pt (Typographic. . .'
            3.5277777777777776E-4,
            // [35] unit[34]: 'px (Logical pixel)'
            2.6458333333333336E-4,
            // [36] unit[35]: 'rad (Radian)'
            1.0,
            // [37] unit[36]: 's (Second)'
            1.0,
            // [38] unit[37]: 'tr (Turn)'
            6.283185307179586,
            // [39] unit[38]: 'yd (Yard)'
            0.9144,
            // [40] unit[39]: 'yd² (Square yard)'
            0.83612736,
            // [41] unit[40]: '°F (Fahrenheit)'
            0.5555555555555556,
            // [42] unit[41]: '℃ (Celsius)'
            1.0,
            // [43] unit[42]: 'wk (Week)'
            604800.0,
            // [44] unit[43]: 'A (Ampere)'
            1.0),
        // unit
        list()
            .withAll(UNITS)
            .with(tech.units.indriya.unit.Units.WEEK)
            .with(tech.units.indriya.unit.Units.AMPERE));
  }

  static Stream<Arguments> getFactor_Unit_Unit() {
    return argumentsStream(
        simpleArgumentsStreamConfig(),
        // expected
        java.util.Arrays.asList(
            // [1] unit[0]: 'm (Metre)'; target[0]: 'm (Metre)'
            1.0,
            // [2] unit[1]: 'm (Metre)'; target[1]: 'cm (Centimetre)'
            100.0,
            // [3] unit[2]: 'ft (Foot)'; target[2]: 'km (Kilometre)'
            3.048E-4,
            // [4] unit[3]: 'ft (Foot)'; target[3]: 'ft² (Square foot)'
            new Failure(
                "javax.measure.UnconvertibleException",
                "javax.measure.IncommensurableException: ft is not compatible with ft²"),
            // [5] unit[4]: 'ac (Acre)'; target[4]: 'ha (Hectare)'
            0.40468564224,
            // [6] unit[5]: '℃ (Celsius)'; target[5]: 'K (Kelvin)'
            1.0,
            // [7] unit[6]: 'h (Hour)'; target[6]: 'K (Kelvin)'
            new Failure(
                "javax.measure.UnconvertibleException",
                "javax.measure.IncommensurableException: h is not compatible with K")),
        // unit
        asList(
            Units.METRE,
            Units.METRE,
            Units.FOOT,
            Units.FOOT,
            Units.ACRE,
            Units.CELSIUS,
            Units.HOUR),
        // target
        asList(
            Units.METRE,
            Units.CENTIMETRE,
            Units.KILOMETRE,
            Units.SQUARE_FOOT,
            Units.HECTARE,
            Units.KELVIN,
            Units.KELVIN));
  }

  static Stream<Arguments> getOffset_Unit() {
    return argumentsStream(
        simpleArgumentsStreamConfig(),
        // expected
        java.util.Arrays.asList(
            // [1] unit[0]: 'a (Are)'
            0.0,
            // [2] unit[1]: 'ac (Acre)'
            0.0,
            // [3] unit[2]: 'cm (Centimetre)'
            0.0,
            // [4] unit[3]: 'cm² (Square cen. . .'
            0.0,
            // [5] unit[4]: 'd (Day)'
            0.0,
            // [6] unit[5]: 'deg (Degree)'
            0.0,
            // [7] unit[6]: 'ft (Foot)'
            0.0,
            // [8] unit[7]: 'ft (US survey foot)'
            0.0,
            // [9] unit[8]: 'ft² (Square foot)'
            0.0,
            // [10] unit[9]: 'ft² (US survey . . .'
            0.0,
            // [11] unit[10]: 'gon (Gradian)'
            0.0,
            // [12] unit[11]: 'h (Hour)'
            0.0,
            // [13] unit[12]: 'ha (Hectare)'
            0.0,
            // [14] unit[13]: 'Hz (Hertz)'
            0.0,
            // [15] unit[14]: 'in (Inch)'
            0.0,
            // [16] unit[15]: 'in² (Square inch)'
            0.0,
            // [17] unit[16]: 'K (Kelvin)'
            0.0,
            // [18] unit[17]: 'kHz (Kilohertz)'
            0.0,
            // [19] unit[18]: 'km (Kilometre)'
            0.0,
            // [20] unit[19]: 'km² (Square kil. . .'
            0.0,
            // [21] unit[20]: 'ks (Kilosecond)'
            0.0,
            // [22] unit[21]: 'm (Metre)'
            0.0,
            // [23] unit[22]: 'M (Nautical mile)'
            0.0,
            // [24] unit[23]: 'MHz (Megahertz)'
            0.0,
            // [25] unit[24]: 'mHz (Millihertz)'
            0.0,
            // [26] unit[25]: 'mi (Mile)'
            0.0,
            // [27] unit[26]: 'min (Minute)'
            0.0,
            // [28] unit[27]: 'mi² (Square mile)'
            0.0,
            // [29] unit[28]: 'mm (Millimetre)'
            0.0,
            // [30] unit[29]: 'mm² (Square mil. . .'
            0.0,
            // [31] unit[30]: 'ms (Millisecond)'
            0.0,
            // [32] unit[31]: 'm² (Square metre)'
            0.0,
            // [33] unit[32]: 'pc (Pica)'
            0.0,
            // [34] unit[33]: 'pt (Typographic. . .'
            0.0,
            // [35] unit[34]: 'px (Logical pixel)'
            0.0,
            // [36] unit[35]: 'rad (Radian)'
            0.0,
            // [37] unit[36]: 's (Second)'
            0.0,
            // [38] unit[37]: 'tr (Turn)'
            0.0,
            // [39] unit[38]: 'yd (Yard)'
            0.0,
            // [40] unit[39]: 'yd² (Square yard)'
            0.0,
            // [41] unit[40]: '°F (Fahrenheit)'
            459.67,
            // [42] unit[41]: '℃ (Celsius)'
            273.15),
        // unit
        UNITS);
  }

  static Stream<Arguments> getQuantityType() {
    return argumentsStream(
        simpleArgumentsStreamConfig()
            .<String>composeExpectedConverter(Objects::type),
        // expected
        java.util.Arrays.asList(
            // [1] unit[0]: 'a (Are)'
            "javax.measure.quantity.Area",
            // [2] unit[1]: 'ac (Acre)'
            "javax.measure.quantity.Area",
            // [3] unit[2]: 'cm (Centimetre)'
            "javax.measure.quantity.Length",
            // [4] unit[3]: 'cm² (Square cen. . .'
            "javax.measure.quantity.Area",
            // [5] unit[4]: 'd (Day)'
            "javax.measure.quantity.Time",
            // [6] unit[5]: 'deg (Degree)'
            "javax.measure.quantity.Angle",
            // [7] unit[6]: 'ft (Foot)'
            "javax.measure.quantity.Length",
            // [8] unit[7]: 'ft (US survey foot)'
            "javax.measure.quantity.Length",
            // [9] unit[8]: 'ft² (Square foot)'
            "javax.measure.quantity.Area",
            // [10] unit[9]: 'ft² (US survey . . .'
            "javax.measure.quantity.Area",
            // [11] unit[10]: 'gon (Gradian)'
            "javax.measure.quantity.Angle",
            // [12] unit[11]: 'h (Hour)'
            "javax.measure.quantity.Time",
            // [13] unit[12]: 'ha (Hectare)'
            "javax.measure.quantity.Area",
            // [14] unit[13]: 'Hz (Hertz)'
            "javax.measure.quantity.Frequency",
            // [15] unit[14]: 'in (Inch)'
            "javax.measure.quantity.Length",
            // [16] unit[15]: 'in² (Square inch)'
            "javax.measure.quantity.Area",
            // [17] unit[16]: 'K (Kelvin)'
            "javax.measure.quantity.Temperature",
            // [18] unit[17]: 'kHz (Kilohertz)'
            "javax.measure.quantity.Frequency",
            // [19] unit[18]: 'km (Kilometre)'
            "javax.measure.quantity.Length",
            // [20] unit[19]: 'km² (Square kil. . .'
            "javax.measure.quantity.Area",
            // [21] unit[20]: 'ks (Kilosecond)'
            "javax.measure.quantity.Time",
            // [22] unit[21]: 'm (Metre)'
            "javax.measure.quantity.Length",
            // [23] unit[22]: 'M (Nautical mile)'
            "javax.measure.quantity.Length",
            // [24] unit[23]: 'MHz (Megahertz)'
            "javax.measure.quantity.Frequency",
            // [25] unit[24]: 'mHz (Millihertz)'
            "javax.measure.quantity.Frequency",
            // [26] unit[25]: 'mi (Mile)'
            "javax.measure.quantity.Length",
            // [27] unit[26]: 'min (Minute)'
            "javax.measure.quantity.Time",
            // [28] unit[27]: 'mi² (Square mile)'
            "javax.measure.quantity.Area",
            // [29] unit[28]: 'mm (Millimetre)'
            "javax.measure.quantity.Length",
            // [30] unit[29]: 'mm² (Square mil. . .'
            "javax.measure.quantity.Area",
            // [31] unit[30]: 'ms (Millisecond)'
            "javax.measure.quantity.Time",
            // [32] unit[31]: 'm² (Square metre)'
            "javax.measure.quantity.Area",
            // [33] unit[32]: 'pc (Pica)'
            "javax.measure.quantity.Length",
            // [34] unit[33]: 'pt (Typographic. . .'
            "javax.measure.quantity.Length",
            // [35] unit[34]: 'px (Logical pixel)'
            "javax.measure.quantity.Length",
            // [36] unit[35]: 'rad (Radian)'
            "javax.measure.quantity.Angle",
            // [37] unit[36]: 's (Second)'
            "javax.measure.quantity.Time",
            // [38] unit[37]: 'tr (Turn)'
            "javax.measure.quantity.Angle",
            // [39] unit[38]: 'yd (Yard)'
            "javax.measure.quantity.Length",
            // [40] unit[39]: 'yd² (Square yard)'
            "javax.measure.quantity.Area",
            // [41] unit[40]: '°F (Fahrenheit)'
            "javax.measure.quantity.Temperature",
            // [42] unit[41]: '℃ (Celsius)'
            "javax.measure.quantity.Temperature"),
        // unit
        UNITS);
  }

  static Stream<Arguments> getQuantityType__external() {
    return argumentsStream(
        simpleArgumentsStreamConfig()
            .<String>composeExpectedConverter(Objects::type),
        // expected
        asList(
            // [1] unit[0]: "m³ (Cubic metre)"
            "javax.measure.quantity.Volume",
            // [2] unit[1]: "g (Gram)"
            "javax.measure.quantity.Mass",
            // [3] unit[2]: "km/h (Kilometr. . ."
            "javax.measure.quantity.Speed",
            // [4] unit[3]: "m/s (Metre per. . ."
            "javax.measure.quantity.Speed",
            // [5] unit[4]: "m/s² (Metre pe. . ."
            "javax.measure.quantity.Acceleration",
            // [6] unit[5]: "m² (Square metre)"
            "javax.measure.quantity.Area",
            // [7] unit[6]: "one (One)"
            "javax.measure.quantity.Dimensionless",
            // [8] unit[7]: "% (Percent)"
            "javax.measure.quantity.Dimensionless",
            // [9] unit[8]: "A (Ampere)"
            "javax.measure.quantity.ElectricCurrent",
            // [10] unit[9]: "Bq (Becquerel)"
            "javax.measure.quantity.Radioactivity",
            // [11] unit[10]: "C (Coulomb)"
            "javax.measure.quantity.ElectricCharge",
            // [12] unit[11]: "cd (Candela)"
            "javax.measure.quantity.LuminousIntensity",
            // [13] unit[12]: "d (Day)"
            "javax.measure.quantity.Time",
            // [14] unit[13]: "F (Farad)"
            "javax.measure.quantity.ElectricCapacitance",
            // [15] unit[14]: "Gy (Gray)"
            "javax.measure.quantity.RadiationDoseAbsorbed",
            // [16] unit[15]: "H (Henry)"
            "javax.measure.quantity.ElectricInductance",
            // [17] unit[16]: "h (Hour)"
            "javax.measure.quantity.Time",
            // [18] unit[17]: "Hz (Hertz)"
            "javax.measure.quantity.Frequency",
            // [19] unit[18]: "J (Joule)"
            "javax.measure.quantity.Energy",
            // [20] unit[19]: "K (Kelvin)"
            "javax.measure.quantity.Temperature",
            // [21] unit[20]: "kat (Katal)"
            "javax.measure.quantity.CatalyticActivity",
            // [22] unit[21]: "kg (Kilogram)"
            "javax.measure.quantity.Mass",
            // [23] unit[22]: "l (Litre)"
            "javax.measure.quantity.Volume",
            // [24] unit[23]: "lm (Lumen)"
            "javax.measure.quantity.LuminousFlux",
            // [25] unit[24]: "lx (Lux)"
            "javax.measure.quantity.Illuminance",
            // [26] unit[25]: "m (Metre)"
            "javax.measure.quantity.Length",
            // [27] unit[26]: "min (Minute)"
            "javax.measure.quantity.Time",
            // [28] unit[27]: "mo (Month)"
            "javax.measure.quantity.Time",
            // [29] unit[28]: "mol (Mole)"
            "javax.measure.quantity.AmountOfSubstance",
            // [30] unit[29]: "N (Newton)"
            "javax.measure.quantity.Force",
            // [31] unit[30]: "Pa (Pascal)"
            "javax.measure.quantity.Pressure",
            // [32] unit[31]: "rad (Radian)"
            "javax.measure.quantity.Angle",
            // [33] unit[32]: "s (Second)"
            "javax.measure.quantity.Time",
            // [34] unit[33]: "S (Siemens)"
            "javax.measure.quantity.ElectricConductance",
            // [35] unit[34]: "sr (Steradian)"
            "javax.measure.quantity.SolidAngle",
            // [36] unit[35]: "Sv (Sievert)"
            "javax.measure.quantity.RadiationDoseEffective",
            // [37] unit[36]: "T (Tesla)"
            "javax.measure.quantity.MagneticFluxDensity",
            // [38] unit[37]: "V (Volt)"
            "javax.measure.quantity.ElectricPotential",
            // [39] unit[38]: "W (Watt)"
            "javax.measure.quantity.Power",
            // [40] unit[39]: "Wb (Weber)"
            "javax.measure.quantity.MagneticFlux",
            // [41] unit[40]: "wk (Week)"
            "javax.measure.quantity.Time",
            // [42] unit[41]: "yr (Year)"
            "javax.measure.quantity.Time",
            // [43] unit[42]: "Ω (Ohm)"
            "javax.measure.quantity.ElectricResistance",
            // [44] unit[43]: "℃ (Celsius)"
            "javax.measure.quantity.Temperature"),
        // unit
        tech.units.indriya.unit.Units.getInstance().getUnits().stream()
            .sorted(COMPARATOR__UNIT)
            .collect(Collectors.toUnmodifiableList()));
  }

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

  @ParameterizedTest
  @MethodSource
  void getFactor_Unit(Expected<Double> expected, Argument<Unit<?>> unit) {
    assertParameterizedOf(
        () -> Units.getFactor(unit.getValue()),
        expected.match($ -> isCloseTo(nonNull($))),
        () -> new ExpectedGeneration(List.of(
            entry("unit", unit))));
  }

  @ParameterizedTest
  @MethodSource
  void getFactor_Unit_Unit(Expected<Double> expected, Argument<Unit<?>> unit,
      Argument<Unit<?>> target) {
    assertParameterizedOf(
        () -> Units.getFactor((Unit) unit.getValue(), target.getValue()),
        expected.match($ -> isCloseTo(nonNull($))),
        () -> new ExpectedGeneration(List.of(
            entry("unit", unit),
            entry("target", target))));
  }

  @ParameterizedTest
  @MethodSource
  void getOffset_Unit(Expected<Double> expected, Argument<Unit<?>> unit) {
    assertParameterizedOf(
        () -> Units.getOffset(unit.getValue()),
        expected.match($ -> isCloseTo(nonNull($))),
        () -> new ExpectedGeneration(List.of(
            entry("unit", unit))));
  }

  @ParameterizedTest
  @MethodSource
  void getQuantityType(Expected<Class<?>> expected, Argument<XtUnit<?>> unit) {
    assertParameterizedOf(
        () -> unit.getValue().getQuantityType(),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("unit", unit)))
                .setExpectedSourceCodeGenerator(Objects::fqn));
  }

  @ParameterizedTest
  @MethodSource
  void getQuantityType__external(Expected<Class<?>> expected, Argument<Unit<?>> unit) {
    assertParameterizedOf(
        () -> Units.getQuantityType(unit.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("unit", unit)))
                .setExpectedSourceCodeGenerator(Objects::fqn));
  }

  private Matcher<Double> isCloseTo(double value) {
    return is(closeTo(value, DBL_DELTA));
  }
}
