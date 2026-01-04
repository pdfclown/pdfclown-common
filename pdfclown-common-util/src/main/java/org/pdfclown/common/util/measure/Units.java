/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Units.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.measure;

import static javax.measure.MetricPrefix.CENTI;
import static javax.measure.MetricPrefix.HECTO;
import static javax.measure.MetricPrefix.KILO;
import static javax.measure.MetricPrefix.MEGA;
import static javax.measure.MetricPrefix.MILLI;
import static org.pdfclown.common.util.Conditions.requireType;
import static org.pdfclown.common.util.Exceptions.unsupported;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Objects.fqn;
import static org.pdfclown.common.util.Objects.objTo;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.util.Set;
import javax.measure.Dimension;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Immutable;
import tech.units.indriya.AbstractSystemOfUnits;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.function.AbstractConverter;
import tech.units.indriya.function.AddConverter;
import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.unit.BaseUnit;
import tech.units.indriya.unit.TransformedUnit;

/**
 * Common measurement units.
 *
 * @author Stefano Chizzolini
 */
@Immutable
public class Units extends AbstractSystemOfUnits {
  private static final Units INSTANCE = new Units();

  //
  // ------------------------------------------- LENGTH --------------------------------------------
  //

  //
  // INTERNATIONAL SYSTEM (SI) ---------------------------------------------------------------------
  //
  /**
   * Metre (<a href="https://en.wikipedia.org/wiki/International_System_of_Units">SI</a> base unit).
   */
  public static final XtUnit<Length> METRE = addBaseUnit(Length.class,
      tech.units.indriya.unit.Units.METRE);
  /**
   * Centimetre (<a href="https://en.wikipedia.org/wiki/International_System_of_Units">SI</a>
   * derived unit, 10^-2).
   */
  public static final XtUnit<Length> CENTIMETRE = addUnit(CENTI(METRE),
      "Centimetre");
  /**
   * Kilometre (<a href="https://en.wikipedia.org/wiki/International_System_of_Units">SI</a> derived
   * unit, 10^3).
   */
  public static final XtUnit<Length> KILOMETRE = addUnit(KILO(METRE),
      "Kilometre");
  /**
   * Millimetre (<a href="https://en.wikipedia.org/wiki/International_System_of_Units">SI</a>
   * derived unit, 10^-3).
   */
  public static final XtUnit<Length> MILLIMETRE = addUnit(MILLI(METRE),
      "Millimetre");

  //
  // IMPERIAL SYSTEM -------------------------------------------------------------------------------
  //
  /**
   * Inch (<a href="https://en.wikipedia.org/wiki/Imperial_units">Imperial</a> unit).
   */
  public static final XtUnit<Length> INCH = addUnit(METRE.multiply(2.54e-2),
      "Inch", "in");
  /**
   * Foot (<a href="https://en.wikipedia.org/wiki/Imperial_units">Imperial</a> unit).
   */
  public static final XtUnit<Length> FOOT = addUnit(INCH.multiply(12),
      "Foot", "ft");
  /**
   * Nautical mile (<a href="https://en.wikipedia.org/wiki/Imperial_units">Imperial</a> unit).
   */
  public static final XtUnit<Length> NAUTICAL_MILE = addUnit(METRE.multiply(1_852),
      "Nautical mile", "M");
  /**
   * <a href="https://en.wikipedia.org/wiki/United_States_customary_units#US_survey_units">US
   * Survey</a> foot (historical, deprecated).
   */
  public static final XtUnit<Length> US_SURVEY_FOOT = addUnit(METRE.multiply(3.048006e-1),
      "US survey foot", "ft");
  /**
   * Yard (<a href="https://en.wikipedia.org/wiki/Imperial_units">Imperial</a> unit).
   */
  public static final XtUnit<Length> YARD = addUnit(FOOT.multiply(3),
      "Yard", "yd");
  /**
   * Statute mile (<a href="https://en.wikipedia.org/wiki/Imperial_units">Imperial</a> unit).
   */
  public static final XtUnit<Length> MILE = addUnit(YARD.multiply(1760),
      "Mile", "mi");

  //
  // OTHER UNITS -----------------------------------------------------------------------------------
  //
  /**
   * <a href="https://en.wikipedia.org/wiki/Pixel#Logical_pixel">Logical pixel</a> (1⁄96 inch).
   */
  public static final XtUnit<Length> PIXEL = addUnit(INCH.divide(96),
      "Logical pixel", "px");
  /**
   * <a href="https://en.wikipedia.org/wiki/Point_(typography)">Typographic point</a> (1⁄72 inch).
   */
  public static final XtUnit<Length> POINT = addUnit(INCH.divide(72),
      "Typographic point", "pt");
  /**
   * <a href="https://en.wikipedia.org/wiki/Pica_(typography)">Pica</a>
   */
  public static final XtUnit<Length> PICA = addUnit(POINT.multiply(12),
      "Pica", "pc");

  //
  // -------------------------------------------- AREA ---------------------------------------------
  //

  //
  // INTERNATIONAL SYSTEM (SI) ---------------------------------------------------------------------
  //
  /**
   * Square metre (<a href="https://en.wikipedia.org/wiki/SI_derived_unit">SI derived base
   * unit</a>).
   */
  public static final XtUnit<Area> SQUARE_METRE = defaultUnit(Area.class, addUnit(area(METRE),
      "Square metre"));
  /**
   * Square centimetre (<a href="https://en.wikipedia.org/wiki/SI_derived_unit">SI derived unit</a>,
   * 10^-4).
   */
  public static final XtUnit<Area> SQUARE_CENTIMETRE = addUnit(area(CENTIMETRE),
      "Square centimetre");
  /**
   * Square kilometre (<a href="https://en.wikipedia.org/wiki/SI_derived_unit">SI derived unit</a>,
   * 10^6).
   */
  public static final XtUnit<Area> SQUARE_KILOMETRE = addUnit(area(KILOMETRE),
      "Square kilometre");
  /**
   * Square millimetre (<a href="https://en.wikipedia.org/wiki/SI_derived_unit">SI derived unit</a>,
   * 10^-6).
   */
  public static final XtUnit<Area> SQUARE_MILLIMETRE = addUnit(area(MILLIMETRE),
      "Square millimetre");

  //
  // IMPERIAL SYSTEM -------------------------------------------------------------------------------
  //
  /**
   * Square foot (<a href="https://en.wikipedia.org/wiki/Imperial_units">Imperial</a> unit).
   */
  public static final XtUnit<Area> SQUARE_FOOT = addUnit(area(FOOT),
      "Square foot", "ft²");
  /**
   * Square inch (<a href="https://en.wikipedia.org/wiki/Imperial_units">Imperial</a> unit).
   */
  public static final XtUnit<Area> SQUARE_INCH = addUnit(area(INCH),
      "Square inch", "in²");
  /**
   * Square statute mile (<a href="https://en.wikipedia.org/wiki/Imperial_units">Imperial</a> unit).
   */
  public static final XtUnit<Area> SQUARE_MILE = addUnit(area(MILE),
      "Square mile", "mi²");
  /**
   * Square yard (<a href="https://en.wikipedia.org/wiki/Imperial_units">Imperial</a> unit).
   */
  public static final XtUnit<Area> SQUARE_YARD = addUnit(area(YARD),
      "Square yard", "yd²");
  /**
   * <a href="https://en.wikipedia.org/wiki/United_States_customary_units#US_survey_units">US
   * Survey</a> square foot (historical, deprecated).
   */
  public static final XtUnit<Area> US_SURVEY_SQUARE_FOOT = addUnit(area(US_SURVEY_FOOT),
      "US survey square foot", "ft²");
  /**
   * <a href="https://en.wikipedia.org/wiki/Acre">Acre</a>
   * (<a href="https://en.wikipedia.org/wiki/Imperial_units">Imperial</a> unit).
   */
  public static final XtUnit<Area> ACRE = addUnit(SQUARE_YARD.multiply(4_840),
      "Acre", "ac");

  //
  // OTHER UNITS -----------------------------------------------------------------------------------
  //
  /**
   * <a href="https://en.wikipedia.org/wiki/Hectare#Are">Are</a>
   * (<a href="https://en.wikipedia.org/wiki/Non-SI_units_mentioned_in_the_SI">SI-accepted</a> unit,
   * 10^2), that is square decametre (dam²).
   */
  public static final XtUnit<Area> ARE = addUnit(HECTO(SQUARE_METRE),
      "Are", "a");
  /**
   * <a href="https://en.wikipedia.org/wiki/Hectare">Hectare</a>
   * (<a href="https://en.wikipedia.org/wiki/Non-SI_units_mentioned_in_the_SI">SI-accepted</a> unit,
   * 10^4), that is square hectometre (hm²).
   */
  public static final XtUnit<Area> HECTARE = addUnit(HECTO(ARE),
      "Hectare", "ha");

  //
  // -------------------------------------------- ANGLE --------------------------------------------
  //

  //
  // INTERNATIONAL SYSTEM (SI) ---------------------------------------------------------------------
  //
  /**
   * <a href="https://en.wikipedia.org/wiki/Radian">Radian</a>
   * (<a href="https://en.wikipedia.org/wiki/International_System_of_Units">SI</a> derived unit).
   */
  public static final XtUnit<Angle> RADIAN = defaultUnit(Angle.class, addUnit(
      tech.units.indriya.unit.Units.RADIAN));
  /**
   * <a href="https://en.wikipedia.org/wiki/Degree_(angle)">Degree</a>
   * (<a href="https://en.wikipedia.org/wiki/Non-SI_units_mentioned_in_the_SI">SI-accepted</a> unit,
   * PI/180 rad).
   */
  public static final XtUnit<Angle> DEGREE = addUnit(RADIAN.multiply(Math.PI / 180),
      "Degree", "deg");

  //
  // OTHER UNITS -----------------------------------------------------------------------------------
  //
  /**
   * <a href="https://en.wikipedia.org/wiki/Gradian">Gradian</a> (PI/200 rad).
   */
  public static final XtUnit<Angle> GRADIAN = addUnit(RADIAN.multiply(Math.PI / 200),
      "Gradian", "gon");
  /**
   * <a href="https://en.wikipedia.org/wiki/Turn_(angle)">Turn</a> (2*PI rad).
   */
  public static final XtUnit<Angle> TURN = addUnit(RADIAN.multiply(Math.PI * 2),
      "Turn", "tr");

  //
  // -------------------------------------------- TIME ---------------------------------------------
  //

  //
  // INTERNATIONAL SYSTEM (SI) ---------------------------------------------------------------------
  //
  /**
   * <a href="https://en.wikipedia.org/wiki/Second">Second</a>
   * (<a href="https://en.wikipedia.org/wiki/International_System_of_Units">SI</a> base unit).
   */
  public static final XtUnit<Time> SECOND = addBaseUnit(Time.class,
      tech.units.indriya.unit.Units.SECOND);
  /**
   * Kilosecond (<a href="https://en.wikipedia.org/wiki/International_System_of_Units">SI</a>
   * derived unit, 10^3).
   */
  public static final XtUnit<Time> KILOSECOND = addUnit(KILO(SECOND),
      "Kilosecond");
  /**
   * Millisecond (<a href="https://en.wikipedia.org/wiki/International_System_of_Units">SI</a>
   * derived unit, 10^-3).
   */
  public static final XtUnit<Time> MILLISECOND = addUnit(MILLI(SECOND),
      "Millisecond");
  /**
   * Minute
   * (<a href="https://en.wikipedia.org/wiki/Non-SI_units_mentioned_in_the_SI">SI-accepted</a> unit,
   * 60s).
   */
  public static final XtUnit<Time> MINUTE = addUnit(tech.units.indriya.unit.Units.MINUTE);
  /**
   * Hour (<a href="https://en.wikipedia.org/wiki/Non-SI_units_mentioned_in_the_SI">SI-accepted</a>
   * unit, 60min).
   */
  public static final XtUnit<Time> HOUR = addUnit(tech.units.indriya.unit.Units.HOUR);
  /**
   * Day (<a href="https://en.wikipedia.org/wiki/Non-SI_units_mentioned_in_the_SI">SI-accepted</a>
   * unit, 24h).
   */
  public static final XtUnit<Time> DAY = addUnit(tech.units.indriya.unit.Units.DAY);

  //
  // ------------------------------------------ FREQUENCY ------------------------------------------
  //

  //
  // INTERNATIONAL SYSTEM (SI) ---------------------------------------------------------------------
  //
  /**
   * <a href="https://en.wikipedia.org/wiki/Hertz">Hertz</a>
   * (<a href="https://en.wikipedia.org/wiki/International_System_of_Units">SI</a> derived unit,
   * 1/s).
   */
  public static final XtUnit<Frequency> HERTZ = defaultUnit(Frequency.class, addUnit(
      tech.units.indriya.unit.Units.HERTZ));
  /**
   * <a href="https://en.wikipedia.org/wiki/Hertz">Kilohertz</a>
   * (<a href="https://en.wikipedia.org/wiki/International_System_of_Units">SI</a> derived unit,
   * 10^3).
   */
  public static final XtUnit<Frequency> KILOHERTZ = addUnit(KILO(HERTZ),
      "Kilohertz");
  /**
   * <a href="https://en.wikipedia.org/wiki/Hertz">Megahertz</a>
   * (<a href="https://en.wikipedia.org/wiki/International_System_of_Units">SI</a> derived unit,
   * 10^6).
   */
  public static final XtUnit<Frequency> MEGAHERTZ = addUnit(MEGA(HERTZ),
      "Megahertz");
  /**
   * <a href="https://en.wikipedia.org/wiki/Hertz">Millihertz</a>
   * (<a href="https://en.wikipedia.org/wiki/International_System_of_Units">SI</a> derived unit,
   * 10^-3).
   */
  public static final XtUnit<Frequency> MILLIHERTZ = addUnit(MILLI(HERTZ),
      "Millihertz");

  //
  // ----------------------------------------- TEMPERATURE -----------------------------------------
  //

  //
  // INTERNATIONAL SYSTEM (SI) ---------------------------------------------------------------------
  //
  /**
   * <a href="https://en.wikipedia.org/wiki/Kelvin">Kelvin</a>.
   */
  public static final XtUnit<Temperature> KELVIN = addBaseUnit(Temperature.class,
      tech.units.indriya.unit.Units.KELVIN);
  /**
   * <a href="https://en.wikipedia.org/wiki/Celsius">Degree Celsius</a>.
   */
  public static final XtUnit<Temperature> CELSIUS = addUnit(tech.units.indriya.unit.Units.CELSIUS);

  //
  // IMPERIAL SYSTEM -------------------------------------------------------------------------------
  //
  /**
   * <a href="https://en.wikipedia.org/wiki/Fahrenheit">Fahrenheit</a>.
   */
  public static final XtUnit<Temperature> FAHRENHEIT = addUnit(
      new TransformedUnit<>("°F", "Fahrenheit", Units.KELVIN, MultiplyConverter
          .ofRational(5, 9).concatenate(new AddConverter(459.67))));

  //
  // -----------------------------------------------------------------------------------------------
  //

  static {
    /*
     * Adding default units for quantity types not registered in this unit system...
     *
     * NOTE: This trick allows units declared outside this unit system to resolve their quantity
     * type via getQuantityType(..).
     */
    try (ScanResult scanResult = new ClassGraph().enableClassInfo()
        .acceptPackages(Quantity.class.getPackageName()).scan()) {
      scanResult.getClassesImplementing(Quantity.class).stream()
          .map(ClassInfo::loadClass)
          .forEach($ -> {
            @SuppressWarnings("rawtypes")
            Class quantityType = $;
            if (!INSTANCE.quantityToUnit.containsKey(quantityType)) {
              @SuppressWarnings("unchecked")
              var defaultUnit = tech.units.indriya.unit.Units.getInstance().getUnit(quantityType);
              //noinspection unchecked
              defaultUnit(quantityType, wrap(defaultUnit));
            }
          });
    }
  }

  /**
   * Derives an area unit from a corresponding length one.
   *
   * @throws ClassCastException
   *           if {@code length} doesn't inherit from {@link AbstractUnit}.
   */
  @SuppressWarnings("unchecked")
  public static XtUnit<Area> area(Unit<Length> length) {
    return new XtUnit<>((AbstractUnit<Area>) length.pow(2));
  }

  /**
   * Gets the dimension corresponding to the quantity type.
   *
   * @return {@code null}, if {@code quantityType} isn't mapped.
   */
  public static <Q extends Quantity<Q>> @Nullable Dimension getDimension(Class<Q> quantityType) {
    return objTo(INSTANCE.quantityToUnit.get(quantityType), Unit::getDimension);
  }

  /**
   * Gets the <a href="https://en.wikipedia.org/wiki/Conversion_of_units">conversion factor</a>
   * against the {@linkplain Unit#getSystemUnit() system unit}, that is how many system units
   * correspond to the unit.
   */
  public static <Q extends Quantity<Q>> double getFactor(Unit<Q> unit) {
    return getFactor(unit, unit.getSystemUnit());
  }

  /**
   * Gets the <a href="https://en.wikipedia.org/wiki/Conversion_of_units">conversion factor</a>
   * against the target unit, that is how many target units correspond to the unit.
   */
  public static <Q extends Quantity<Q>> double getFactor(Unit<Q> unit, Unit<Q> target) {
    UnitConverter converter = unit.getConverterTo(target);
    if (converter == AbstractConverter.IDENTITY || converter instanceof AddConverter)
      return 1;
    else if (converter instanceof MultiplyConverter multiplyConverter)
      return multiplyConverter.getFactor().doubleValue();
    else if (converter instanceof AbstractConverter.Pair pair) {
      /*
       * NOTE: The assumption is that non-linear units within the same quantity are related by a
       * simple function combining factor and offset.
       */
      return pair.getLeft() instanceof MultiplyConverter
          ? ((MultiplyConverter) pair.getLeft()).getFactor().doubleValue()
          : ((MultiplyConverter) pair.getRight()).getFactor().doubleValue();
    } else
      throw unsupported("Converter `{}` UNKNOWN", fqn(converter));
  }

  public static Units getInstance() {
    return INSTANCE;
  }

  /**
   * Gets the <a href="https://en.wikipedia.org/wiki/Conversion_of_units">conversion offset</a>
   * against the {@linkplain Unit#getSystemUnit() system unit}, that is distance from system unit at
   * zero.
   */
  public static <Q extends Quantity<Q>> double getOffset(Unit<Q> unit) {
    UnitConverter converter = unit.getConverterTo(unit.getSystemUnit());
    if (converter instanceof AddConverter addConverter)
      return addConverter.getOffset().doubleValue();
    else if (converter == AbstractConverter.IDENTITY || converter instanceof MultiplyConverter)
      return 0;
    else if (converter instanceof AbstractConverter.Pair pair) {
      /*
       * NOTE: The assumption is that non-linear units within the same quantity are related by a
       * simple function combining factor and offset.
       */
      return pair.getLeft() instanceof AddConverter
          ? ((AddConverter) pair.getLeft()).getOffset().doubleValue()
          : ((AddConverter) pair.getRight()).getOffset().doubleValue();
    } else
      throw unsupported("Converter `{}` UNKNOWN", fqn(converter));
  }

  /**
   * Gets the quantity type corresponding to the unit.
   * <p>
   * All {@link javax.measure.quantity standard quantity types} are supported.
   * </p>
   *
   * @return {@code null}, if {@code unit} is associated to an unknown quantity type.
   */
  @SuppressWarnings("rawtypes")
  public static @Nullable Class<? extends Quantity> getQuantityType(Unit<?> unit) {
    var systemUnit = unit.getSystemUnit();
    for (var entry : INSTANCE.quantityToUnit.entrySet()) {
      if (systemUnit.equals(entry.getValue()))
        return entry.getKey();
    }
    return null;
  }

  /**
   * Adds the
   * <a href="https://en.wikipedia.org/wiki/Unit_of_measurement#Base_and_derived_units">base
   * unit</a> of the quantity type.
   *
   * @param unitSystem
   *          Target unit system.
   * @param quantityType
   *          Quantity type {@code unit} has to be associated to.
   * @param unit
   *          Base unit.
   * @return {@linkplain #wrap Wrapped} {@code unit}.
   * @throws ClassCastException
   *           if {@code unit} doesn't inherit from {@link AbstractUnit}.
   * @throws IllegalArgumentException
   *           if a unit is already associated to {@code quantityType}.
   */
  @SuppressWarnings("unchecked")
  protected static <Q extends Quantity<Q>> XtUnit<Q> addBaseUnit(Units unitSystem,
      Class<Q> quantityType, Unit<Q> unit) {
    return defaultUnit(quantityType, addUnit(unitSystem, requireType(unit, BaseUnit.class)),
        unitSystem);
  }

  /**
   * Imports in the system a unit from another {@linkplain tech.units.indriya indriya-based} system.
   *
   * @param unitSystem
   *          Target unit system.
   * @param unit
   *          Imported unit.
   * @return {@linkplain #wrap Wrapped} {@code unit}.
   * @throws ClassCastException
   *           if {@code unit} doesn't inherit from {@link AbstractUnit}.
   */
  protected static <Q extends Quantity<Q>> XtUnit<Q> addUnit(Units unitSystem, Unit<Q> unit) {
    var ret = wrap(unit);
    unitSystem.units.add(ret);
    SimpleUnitFormat.getInstance().label(ret, ret.getSymbol());
    return ret;
  }

  /**
   * Adds a new unit to the system.
   *
   * @param unitSystem
   *          Target unit system.
   * @param unit
   *          New unit.
   * @param name
   *          Unit name.
   * @return {@linkplain #wrap Wrapped} {@code unit}.
   * @throws ClassCastException
   *           if {@code unit} doesn't inherit from {@link AbstractUnit}.
   */
  protected static <Q extends Quantity<Q>> XtUnit<Q> addUnit(Units unitSystem, Unit<Q> unit,
      String name) {
    return addUnit(unitSystem, unit, name, null);
  }

  /**
   * Adds a new unit to the system.
   *
   * @param unitSystem
   *          Target unit system.
   * @param unit
   *          New unit.
   * @param name
   *          Unit name.
   * @param symbol
   *          Unit symbol.
   * @return {@linkplain #wrap Wrapped} {@code unit}.
   * @throws ClassCastException
   *           if {@code unit} doesn't inherit from {@link AbstractUnit}.
   */
  protected static <Q extends Quantity<Q>> XtUnit<Q> addUnit(Units unitSystem, Unit<Q> unit,
      String name, @Nullable String symbol) {
    var ret = Helper.addUnit(unitSystem.units, wrap(unit), name, symbol);
    SimpleUnitFormat.getInstance().label(ret, ret.getSymbol());
    return ret;
  }

  /**
   * Defines the unit as default for the quantity type in the system.
   *
   * @param quantityType
   *          Quantity type {@code unit} has to be associated to.
   * @param unit
   *          Default unit.
   * @param unitSystem
   *          Target unit system.
   * @return {@code unit}
   * @throws IllegalArgumentException
   *           if a unit is already associated to {@code quantityType}.
   */
  protected static <U extends XtUnit<Q>, Q extends Quantity<Q>> U defaultUnit(Class<Q> quantityType,
      U unit, Units unitSystem) {
    if (unitSystem.quantityToUnit.containsKey(quantityType))
      throw wrongArg("quantityType", quantityType,
          "Default unit already defined ({}) for this quantity type",
          unitSystem.quantityToUnit.get(quantityType));

    unitSystem.quantityToUnit.put(quantityType, unwrap(unit));
    return unit;
  }

  /**
   * Gets the inner representation of the unit.
   *
   * @return Unwrapped {@code unit}.
   * @see #wrap(Unit)
   */
  protected static <Q extends Quantity<Q>> Unit<Q> unwrap(Unit<Q> unit) {
    return unit instanceof XtUnit<Q> xtUnit ? xtUnit.base : unit;
  }

  /**
   * Gets the augmented representation of the unit.
   *
   * @return Wrapped {@code unit}.
   * @throws ClassCastException
   *           if {@code unit} doesn't inherit from {@link AbstractUnit}.
   * @see #unwrap(Unit)
   */
  protected static <Q extends Quantity<Q>> XtUnit<Q> wrap(Unit<Q> unit) {
    return unit instanceof XtUnit<Q> xtUnit ? xtUnit : new XtUnit<>((AbstractUnit<Q>) unit);
  }

  /**
   * Adds the
   * <a href="https://en.wikipedia.org/wiki/Unit_of_measurement#Base_and_derived_units">base
   * unit</a> of the quantity type.
   *
   * @param quantityType
   *          Quantity type {@code unit} has to be associated to.
   * @param unit
   *          Base unit.
   * @return {@linkplain #wrap Wrapped} {@code unit}.
   * @throws ClassCastException
   *           if {@code unit} doesn't inherit from {@link AbstractUnit}.
   * @throws IllegalArgumentException
   *           if a unit is already associated to {@code quantityType}.
   */
  private static <Q extends Quantity<Q>> XtUnit<Q> addBaseUnit(Class<Q> quantityType,
      Unit<Q> unit) {
    return addBaseUnit(INSTANCE, quantityType, unit);
  }

  /**
   * Imports a unit from another {@linkplain tech.units.indriya indriya-based} system.
   *
   * @param unit
   *          Imported unit.
   * @return {@linkplain #wrap Wrapped} {@code unit}.
   * @throws ClassCastException
   *           if {@code unit} doesn't inherit from {@link AbstractUnit}.
   */
  private static <Q extends Quantity<Q>> XtUnit<Q> addUnit(Unit<Q> unit) {
    return addUnit(INSTANCE, unit);
  }

  /**
   * Adds a new unit to this system.
   *
   * @param unit
   *          New unit.
   * @param name
   *          Unit name.
   * @return {@linkplain #wrap Wrapped} {@code unit}.
   * @throws ClassCastException
   *           if {@code unit} doesn't inherit from {@link AbstractUnit}.
   */
  private static <Q extends Quantity<Q>> XtUnit<Q> addUnit(Unit<Q> unit, String name) {
    return addUnit(INSTANCE, unit, name);
  }

  /**
   * Adds a new unit to this system.
   *
   * @param unit
   *          New unit.
   * @param name
   *          Unit name.
   * @param symbol
   *          Unit symbol.
   * @return {@linkplain #wrap Wrapped} {@code unit}.
   * @throws ClassCastException
   *           if {@code unit} doesn't inherit from {@link AbstractUnit}.
   */
  private static <Q extends Quantity<Q>> XtUnit<Q> addUnit(Unit<Q> unit, String name,
      @Nullable String symbol) {
    return addUnit(INSTANCE, unit, name, symbol);
  }

  /**
   * Defines the unit as default for the quantity type.
   *
   * @param quantityType
   *          Quantity type {@code unit} has to be associated to.
   * @param unit
   *          Default unit.
   * @return {@code unit}
   * @throws IllegalArgumentException
   *           if a unit is already associated to {@code quantityType}.
   */
  private static <U extends XtUnit<Q>, Q extends Quantity<Q>> U defaultUnit(Class<Q> quantityType,
      U unit) {
    return defaultUnit(quantityType, unit, INSTANCE);
  }

  @Override
  public String getName() {
    return "pdfClown Common Units";
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<XtUnit<?>> getUnits(Dimension dimension) {
    return (Set<XtUnit<?>>) super.getUnits(dimension);
  }
}
