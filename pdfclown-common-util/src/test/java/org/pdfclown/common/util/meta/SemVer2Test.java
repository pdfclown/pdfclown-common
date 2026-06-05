/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SemVer2Test.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.pdfclown.common.build.test.assertion.Verifiers.COMBINATION;
import static org.pdfclown.common.build.test.assertion.Verifiers.TUPLE;
import static org.pdfclown.common.build.util.Tuple.tuple;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings("Convert2MethodRef")
class SemVer2Test extends BaseTest {
  /**
   * Valid version samples.
   */
  private static final List<String> VERSION_LITERALS__VALID = List.of(
      // Stable version
      "1.0.0",
      "1.9.0",
      "1.10.0",
      "1.11.0",
      // Pre-release
      "1.0.0-alpha",
      "1.0.0-alpha.1",
      "1.0.0-0.3.7",
      "1.0.0-x.7.z.92",
      "1.0.0-x.-7.z.92",
      "1.0.0-x-y-z.--",
      // Build metadata
      "1.0.0-alpha+001",
      "1.0.0+20130313144700",
      "1.0.0-beta+exp.sha.5114f85",
      "1.0.0+21AF26D3----117B344092BD");

  /**
   * Invalid version samples.
   * <p>
   * Specification:
   * </p>
   * <ul>
   * <li>[RULE 2] A normal version number MUST take the form X.Y.Z where X, Y, and Z are
   * non-negative integers, and MUST NOT contain leading zeroes.</li>
   * <li>[RULE 9] A pre-release version MAY be denoted by appending a hyphen and a series of dot
   * separated identifiers immediately following the patch version. Identifiers MUST comprise only
   * ASCII alphanumerics and hyphens [0-9A-Za-z-]. Identifiers MUST NOT be empty. Numeric
   * identifiers MUST NOT include leading zeroes.</li>
   * <li>[RULE 10] Build metadata MAY be denoted by appending a plus sign and a series of dot
   * separated identifiers immediately following the patch or pre-release version. Identifiers MUST
   * comprise only ASCII alphanumerics and hyphens [0-9A-Za-z-]. Identifiers MUST NOT be empty.</li>
   * </ul>
   */
  private static final List<String> VERSION_LITERALS__INVALID = List.of(
      // INVALID [RULE 2] (normal version with X.Y.Z form)
      "1.11.0.99",
      // INVALID [RULE 2] (normal version with X.Y.Z form)
      "1.0.0.5-alpha",
      // INVALID [RULE 2] (normal version without negative integers)
      "1.-11.0",
      // INVALID [RULE 2] (normal version without leading zeroes)
      "1.01.0",
      // INVALID [RULE 9] (pre-release without leading zeroes)
      "1.0.0-00.3.7",
      // INVALID [RULE 9] (pre-release denoted by appending a hyphen)
      "1.0.0_alpha",
      // INVALID [RULE 10] (build metadata MUST comprise only [0-9A-Za-z-])
      "1.0.0-beta+exp.sha.5114f_85");

  /**
   * Version samples, both valid and invalid.
   */
  private static final List<String> VERSION_LITERALS__MIXED;
  static {
    var versionLiterals = new ArrayList<>(VERSION_LITERALS__VALID);
    versionLiterals.addAll(VERSION_LITERALS__INVALID);
    VERSION_LITERALS__MIXED = unmodifiableList(versionLiterals);
  }

  @Test
  void compareTo() {
    COMBINATION.verify(
        (value0, value1) -> value0.compareTo(value1),
        List.of("value0", "value1"),
        // value0
        VERSION_LITERALS__VALID.stream().map(SemVer2::of).toList(),
        // value1
        VERSION_LITERALS__VALID.stream().map(SemVer2::of).toList());
  }

  @Test
  void next() {
    COMBINATION.verify(
        (value, id) -> value.next(id),
        List.of("value", "id"),
        // value
        VERSION_LITERALS__VALID.stream().map(SemVer2::of).toList(),
        // id
        List.of(SemVer2.Id.values()));
  }

  @Test
  void of_int_int_int_String_String() {
    TUPLE.verify(
        (major, minor, patch, prerelease, metadata) -> SemVer2.of(major, minor, patch, prerelease,
            metadata),
        List.of("major", "minor", "patch", "prerelease", "metadata"),
        asList(
            // VALID
            tuple(1, 0, 0, null, null),
            tuple(1, 0, 0, "alpha.1", ""),
            // INVALID
            tuple(-1, 0, 0, "alpha.1", "abc"),
            tuple(1, -1, 0, "alpha.1", "abc"),
            tuple(1, 0, -1, "alpha.1", "abc"),
            tuple(1, 0, 0, "alpha!.1", "abc"),
            tuple(1, 0, 0, "alpha.1", "+abc")));
  }

  @Test
  void of_String() {
    COMBINATION.verify(
        (value) -> SemVer2.of(value),
        List.of("value"),
        // value
        VERSION_LITERALS__MIXED);
  }

  @Test
  void precedence() {
    COMBINATION.verify(
        (value0, value1) -> value0.precedence(value1),
        List.of("value0", "value1"),
        // value0
        VERSION_LITERALS__VALID.stream().map(SemVer2::of).toList(),
        // value1
        VERSION_LITERALS__VALID.stream().map(SemVer2::of).toList());
  }

  @Test
  @SuppressWarnings({ "rawtypes", "unchecked" })
  void to() {
    COMBINATION.verify(
        (ver, versionType) -> ver.to(versionType),
        List.of("ver", "versionType"),
        // ver
        VERSION_LITERALS__VALID.stream().map(SemVer2::of).toList(),
        // versionType
        List.<Class>of(SemVer1.class, SemVer2.class));
  }

  @Test
  void toString_() {
    COMBINATION.verify(
        (value) -> value.toString(),
        List.of("value"),
        // value
        VERSION_LITERALS__VALID.stream().map(SemVer2::of).toList());
  }

  @Test
  void with() {
    COMBINATION.verify(
        (ver, id, value) -> ver.with(id, value),
        List.of("ver", "id", "value"),
        // ver
        VERSION_LITERALS__VALID.stream().map(SemVer2::of).toList(),
        // id
        List.of(SemVer2.Id.values()),
        // value
        List.of(8, "rc"));
  }

  @Test
  void withPrereleaseSuffix() {
    COMBINATION.verify(
        (ver, fieldIndex, value) -> ver.withPrereleaseSuffix(fieldIndex, value),
        List.of("ver", "fieldIndex", "value"),
        // ver
        VERSION_LITERALS__VALID.stream().map(SemVer2::of).toList(),
        // fieldIndex
        List.of(-1, 0, 1),
        // value
        List.of(8, "rc", "SNAPSHOT"));
  }
}