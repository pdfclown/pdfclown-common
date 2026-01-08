/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SemVer1.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.meta;

import static java.lang.Character.isDigit;
import static java.lang.Integer.parseInt;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.HYPHEN;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Objects.isSameType;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.regex.Patterns.indexOfMatchFailure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.ArgumentFormatException;
import org.pdfclown.common.util.annot.Derived;
import org.pdfclown.common.util.annot.Immutable;

/**
 * <a href="https://semver.org/spec/v1.0.0.html">Semantic Version 1.0.0</a>.
 *
 * @author Stefano Chizzolini
 */
@Immutable
@SuppressWarnings("rawtypes")
public class SemVer1 extends SemVer<SemVer1> {
  private static final String PATTERN_GROUP__MAJOR = "major";
  private static final String PATTERN_GROUP__MINOR = "minor";
  private static final String PATTERN_GROUP__PATCH = "patch";
  private static final String PATTERN_GROUP__PRERELEASE = "prerelease";

  private static final Pattern PATTERN__SEM_VER = Pattern.compile("""
      ^\
      (?<%s>0|[1-9]\\d*)\\.\
      (?<%s>0|[1-9]\\d*)\\.\
      (?<%s>0|[1-9]\\d*)\
      (?:-(?<%s>[0-9a-zA-Z-]+))?\
      $""".formatted(
      PATTERN_GROUP__MAJOR,
      PATTERN_GROUP__MINOR,
      PATTERN_GROUP__PATCH,
      PATTERN_GROUP__PRERELEASE));

  /**
   * Maximum number of pre-release fields supported by {@link #prereleaseFieldsBitset}.
   */
  private static final int PRERELEASE_FIELDS_BITSET_MAX_COUNT = 16;

  /**
   * Checks whether the version conforms to <a href="https://semver.org/spec/v1.0.0.html">Semantic
   * Versioning 1.0.0</a>.
   *
   * @return {@code version}.
   * @throws IllegalArgumentException
   *           if {@code version} is not a valid semantic version.
   */
  public static String check(String version) {
    Matcher m = PATTERN__SEM_VER.matcher(version);
    if (!m.find())
      throw wrongArg("version", version);

    return version;
  }

  /**
   * @throws ArgumentFormatException
   *           if {@code value} is not a valid semantic version.
   */
  public static SemVer1 of(String value) {
    Matcher m = PATTERN__SEM_VER.matcher(value);
    if (!m.find())
      throw new ArgumentFormatException(null, value, indexOfMatchFailure(m));

    return new SemVer1(
        parseInt(m.group(PATTERN_GROUP__MAJOR)),
        parseInt(m.group(PATTERN_GROUP__MINOR)),
        parseInt(m.group(PATTERN_GROUP__PATCH)),
        m.group(PATTERN_GROUP__PRERELEASE));
  }

  /**
   */
  public static SemVer1 of(int major, int minor, int patch, @Nullable String prerelease) {
    /*
     * NOTE: Validation is based on regex, so we have to serialize the version components, keeping
     * track of their offsets.
     */
    var b = new StringBuilder();
    var offsets = new int[4];
    for (int i = 0; i < offsets.length; i++) {
      switch (i) {
        case 0:
          b.append(major).append(DOT);
          break;
        case 1:
          b.append(minor).append(DOT);
          break;
        case 2:
          b.append(patch);
          break;
        case 3:
          if (!isEmpty(prerelease)) {
            b.append(HYPHEN).append(prerelease);
          }
          break;
        default:
          throw unexpected(i);
      }
      offsets[i] = b.length();
    }
    try {
      return of(b.toString());
    } catch (ArgumentFormatException ex) {
      for (int i = 0; i < offsets.length; i++) {
        if (offsets[i] > ex.getOffset()) {
          switch (i) {
            case 0:
              throw wrongArg("major", major);
            case 1:
              throw wrongArg("minor", minor);
            case 2:
              throw wrongArg("patch", patch);
            case 3:
              throw wrongArg("prerelease", prerelease);
            default:
              // NOP
          }
        }
      }
      throw unexpected("Invalid offset not matched");
    }
  }

  @Derived
  private transient @Nullable List<Comparable> prereleaseFields;
  @Derived
  private transient int prereleaseFieldsBitset;

  SemVer1(int major, int minor, int patch, @Nullable String prerelease) {
    super(major, minor, patch, prerelease);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass())
      return false;

    var that = (SemVer1) o;
    return this.getMajor() == that.getMajor() && this.getMinor() == that.getMinor()
        && this.patch == that.patch
        && Objects.equals(this.prerelease, that.prerelease);
  }

  /**
   * {@inheritDoc}
   * <p>
   * The pre-release is split into tokens according to a simplified version of the algorithm
   * described by <a href="https://maven.apache.org/pom.html#Version_Order_Specification">Maven's
   * Version Order Specification</a>, in particular:
   * </p>
   * <ul>
   * <li>tokens are delimited by hyphens and transitions between digits and letters</li>
   * <li>empty tokens are replaced with {@code 0}</li>
   * <li>trailing hyphen is trimmed</li>
   * </ul>
   * <p>
   * This algorithm is both legitimate (the specification just <i>recommends</i> the lexicographic
   * order) and appropriate (lexicographically ordering identifiers like {@code "beta1"} leads to
   * notoriously broken numerical sequences).
   * </p>
   */
  @Override
  public List<Comparable> getPrereleaseFields() {
    if (prereleaseFields == null) {
      if (prerelease.isEmpty()) {
        prereleaseFields = List.of();
      } else {
        var prereleaseFields = new ArrayList<Comparable>();
        var numeric = false;
        var begin = 0;
        for (int i = 0; i < prerelease.length(); i++) {
          var c = prerelease.charAt(i);
          if (c == HYPHEN
              || (i > begin && isDigit(c) != numeric /*
                                                      * Transition between digits and letters
                                                      *
                                                      * NOTE: Unicode digit check is safe as parsed
                                                      * version is, by definition, ASCII-only
                                                      */)) {
            var synthesized = begin == i;
            prereleaseFields.add(parsePrereleaseField(synthesized
                ? "0" /* NOTE: Empty tokens are replaced with `0` */
                : prerelease.substring(begin, i)));
            if (c == HYPHEN) {
              if (synthesized) {
                setPrereleaseFieldSynthesized(prereleaseFields.size() - 1);
              }
              begin = i + 1;
            } else {
              setPrereleaseFieldImplicit(prereleaseFields.size());
              numeric = !numeric;
              begin = i;
            }
          } else if (i == begin) {
            numeric = isDigit(c);
          }
        }
        /*
         * NOTE: Trailing hyphen is trimmed.
         */
        if (begin < prerelease.length()) {
          prereleaseFields.add(parsePrereleaseField(prerelease.substring(begin)));
        }
        this.prereleaseFields = unmodifiableList(prereleaseFields);
      }
    }
    return prereleaseFields;
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor, patch, prerelease);
  }

  /**
   * {@inheritDoc}
   * <p>
   * If {@code id} == {@link Id#PRERELEASE} and its last field is non-numeric, an additional numeric
   * field is initialized to 1 — for example, {@code "1.0.0-alpha"} --&gt; {@code "1.0.0-alpha1"}.
   * </p>
   *
   * @throws IllegalArgumentException
   *           if {@code id} is {@link Id#METADATA}.
   * @see #with(Id, Comparable)
   */
  @Override
  public SemVer1 next(Id id) {
    return switch (id) {
      case MAJOR -> new SemVer1(major + 1, 0, 0, EMPTY);
      case MINOR -> new SemVer1(major, minor + 1, 0, EMPTY);
      case PATCH -> new SemVer1(major, minor, patch + 1, EMPTY);
      case PRERELEASE -> new SemVer1(major, minor, patch, buildPrerelease(nextPrerelease()));
      case METADATA -> throw wrongArg("id", id);
    };
  }

  @Override
  public String toString() {
    var b = new StringBuilder().append(major).append(DOT).append(minor).append(DOT).append(patch);
    if (!prerelease.isEmpty()) {
      b.append(HYPHEN).append(prerelease);
    }
    return b.toString();
  }

  /**
   * @throws ClassCastException
   *           if {@code value} is incompatible with the identifier.
   * @throws IllegalArgumentException
   *           if {@code id} is {@link Id#METADATA}.
   * @see #next(Id)
   */
  public SemVer1 with(Id id, Comparable value) {
    return switch (id) {
      case MAJOR -> new SemVer1((Integer) value, 0, 0, EMPTY);
      case MINOR -> new SemVer1(major, (Integer) value, 0, EMPTY);
      case PATCH -> new SemVer1(major, minor, (Integer) value, EMPTY);
      case PRERELEASE -> new SemVer1(major, minor, patch, (String) value);
      case METADATA -> throw wrongArg("id", id);
    };
  }

  /**
   * {@inheritDoc}
   * <p>
   * The existing suffix is replaced with the given one — for example:
   * {@code "1.2.5-alpha1-SNAPSHOT"} with {@code fieldIndex} == {@code 1} and {@code value} ==
   * {@code 8} --&gt; {@code "1.2.5-alpha8"}.
   * </p>
   */
  @Override
  public SemVer1 withPrereleaseSuffix(int fieldIndex, Comparable value) {
    if (fieldIndex < 0) {
      fieldIndex = getPrereleaseFields().size();
    }
    var fields = new ArrayList<>(getPrereleaseFields().subList(0, fieldIndex));
    fields.add(value);
    return new SemVer1(major, minor, patch, buildPrerelease(fields));
  }

  /**
   * Builds the pre-release corresponding to the given fields.
   * <p>
   * The fields are expected to follow the same delimiter pattern as this version; however, in case
   * of adjacent fields of the same type (that is, both {@link String} or both {@link Integer}), the
   * original pattern is overridden to ensure an explicit hyphen between them.
   * </p>
   */
  private String buildPrerelease(List<Comparable> fields) {
    var b = new StringBuilder();
    for (int i = 0, l = fields.size(); i < l; i++) {
      if (i > 0
          && (!isPrereleaseFieldImplicit(i) || isSameType(fields.get(i), fields.get(i - 1)))) {
        b.append(HYPHEN);
      }
      if (!isPrereleaseFieldSynthesized(i)) {
        b.append(fields.get(i));
      }
    }
    return b.toString();
  }

  /**
   * Gets whether the preceding field delimiter is implicit.
   * <p>
   * Corresponds to a transition between digits and letters, as specified by
   * <a href="https://maven.apache.org/pom.html#Version_Order_Specification">Maven's Version Order
   * Specification</a> (see {@link #getPrereleaseFields()}).
   * </p>
   *
   * @param index
   *          Field index.
   */
  private boolean isPrereleaseFieldImplicit(int index) {
    return (prereleaseFieldsBitset & (1 << index)) != 0;
  }

  /**
   * Gets whether the field has been synthesized.
   * <p>
   * Corresponds to the case of an empty token replaced with {@code 0}, as specified by
   * <a href="https://maven.apache.org/pom.html#Version_Order_Specification">Maven's Version Order
   * Specification</a> (see {@link #getPrereleaseFields()}).
   * </p>
   *
   * @param index
   *          Field index.
   */
  private boolean isPrereleaseFieldSynthesized(int index) {
    return (prereleaseFieldsBitset & (1 << index + PRERELEASE_FIELDS_BITSET_MAX_COUNT)) != 0;
  }

  /**
   * Sets {@link #isPrereleaseFieldImplicit(int) prereleaseFieldImplicit}.
   */
  private void setPrereleaseFieldImplicit(int index) {
    prereleaseFieldsBitset |= 1 << index;
  }

  /**
   * Sets {@link #isPrereleaseFieldSynthesized(int) prereleaseFieldSynthesized}.
   */
  private void setPrereleaseFieldSynthesized(int index) {
    prereleaseFieldsBitset |= 1 << index + PRERELEASE_FIELDS_BITSET_MAX_COUNT;
  }
}
