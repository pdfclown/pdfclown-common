/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SemVer1Test.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.simple;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.build.test.assertion.Assertions.Failure;
import org.pdfclown.common.util.__test.BaseTest;
import org.pdfclown.common.util.meta.SemVer.Id;

/**
 * @author Stefano Chizzolini
 */
class SemVer1Test extends BaseTest {
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
      "1.0.0-alpha1",
      "1.0.0-alpha-1",
      "1.0.0-beta1",
      "1.0.0-beta-2",
      "1.0.0-beta10",
      "1.0.0-rc1");

  /**
   * Invalid version samples.
   * <p>
   * Specification:
   * </p>
   * <ul>
   * <li>[RULE 2] A normal version number MUST take the form X.Y.Z where X, Y, and Z are
   * (non-negative) integers (, and MUST NOT contain leading zeroes).</li>
   * <li>[RULE 4] A pre-release version MAY be denoted by appending an arbitrary string immediately
   * following the patch version and a dash (hyphen). The string MUST be comprised of only
   * alphanumerics plus dash (hyphen) [0-9A-Za-z-].</li>
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
      // INVALID [RULE 4] (pre-release only with hyphens as separators)
      "1.0.0-00.3.7",
      // INVALID [RULE 4] (pre-release denoted by appending a hyphen)
      "1.0.0_alpha",
      // INVALID [RULE 4] (pre-release only with hyphens as separators)
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

  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> compareTo() {
    return argumentsStream(
        cartesian()
            .<String>composeArgConverter(0, SemVer1::of)
            .<String>composeArgConverter(1, SemVer1::of),
        // expected
        asList(
            // value0[0]: "1.0.0"
            // [1] value1[0]: "1.0.0"
            0,
            // [2] value1[1]: "1.9.0"
            -9,
            // [3] value1[2]: "1.10.0"
            -10,
            // [4] value1[3]: "1.11.0"
            -11,
            // [5] value1[4]: "1.0.0-alpha"
            1,
            // [6] value1[5]: "1.0.0-alpha1"
            1,
            // [7] value1[6]: "1.0.0-alpha-1"
            1,
            // [8] value1[7]: "1.0.0-beta1"
            1,
            // [9] value1[8]: "1.0.0-beta-2"
            1,
            // [10] value1[9]: "1.0.0-beta10"
            1,
            // [11] value1[10]: "1.0.0-rc1"
            1,
            //
            // value0[1]: "1.9.0"
            // [12] value1[0]: "1.0.0"
            9,
            // [13] value1[1]: "1.9.0"
            0,
            // [14] value1[2]: "1.10.0"
            -1,
            // [15] value1[3]: "1.11.0"
            -2,
            // [16] value1[4]: "1.0.0-alpha"
            9,
            // [17] value1[5]: "1.0.0-alpha1"
            9,
            // [18] value1[6]: "1.0.0-alpha-1"
            9,
            // [19] value1[7]: "1.0.0-beta1"
            9,
            // [20] value1[8]: "1.0.0-beta-2"
            9,
            // [21] value1[9]: "1.0.0-beta10"
            9,
            // [22] value1[10]: "1.0.0-rc1"
            9,
            //
            // value0[2]: "1.10.0"
            // [23] value1[0]: "1.0.0"
            10,
            // [24] value1[1]: "1.9.0"
            1,
            // [25] value1[2]: "1.10.0"
            0,
            // [26] value1[3]: "1.11.0"
            -1,
            // [27] value1[4]: "1.0.0-alpha"
            10,
            // [28] value1[5]: "1.0.0-alpha1"
            10,
            // [29] value1[6]: "1.0.0-alpha-1"
            10,
            // [30] value1[7]: "1.0.0-beta1"
            10,
            // [31] value1[8]: "1.0.0-beta-2"
            10,
            // [32] value1[9]: "1.0.0-beta10"
            10,
            // [33] value1[10]: "1.0.0-rc1"
            10,
            //
            // value0[3]: "1.11.0"
            // [34] value1[0]: "1.0.0"
            11,
            // [35] value1[1]: "1.9.0"
            2,
            // [36] value1[2]: "1.10.0"
            1,
            // [37] value1[3]: "1.11.0"
            0,
            // [38] value1[4]: "1.0.0-alpha"
            11,
            // [39] value1[5]: "1.0.0-alpha1"
            11,
            // [40] value1[6]: "1.0.0-alpha-1"
            11,
            // [41] value1[7]: "1.0.0-beta1"
            11,
            // [42] value1[8]: "1.0.0-beta-2"
            11,
            // [43] value1[9]: "1.0.0-beta10"
            11,
            // [44] value1[10]: "1.0.0-rc1"
            11,
            //
            // value0[4]: "1.0.0-alpha"
            // [45] value1[0]: "1.0.0"
            -1,
            // [46] value1[1]: "1.9.0"
            -9,
            // [47] value1[2]: "1.10.0"
            -10,
            // [48] value1[3]: "1.11.0"
            -11,
            // [49] value1[4]: "1.0.0-alpha"
            0,
            // [50] value1[5]: "1.0.0-alpha1"
            -1,
            // [51] value1[6]: "1.0.0-alpha-1"
            -1,
            // [52] value1[7]: "1.0.0-beta1"
            -1,
            // [53] value1[8]: "1.0.0-beta-2"
            -1,
            // [54] value1[9]: "1.0.0-beta10"
            -1,
            // [55] value1[10]: "1.0.0-rc1"
            -17,
            //
            // value0[5]: "1.0.0-alpha1"
            // [56] value1[0]: "1.0.0"
            -1,
            // [57] value1[1]: "1.9.0"
            -9,
            // [58] value1[2]: "1.10.0"
            -10,
            // [59] value1[3]: "1.11.0"
            -11,
            // [60] value1[4]: "1.0.0-alpha"
            -1,
            // [61] value1[5]: "1.0.0-alpha1"
            0,
            // [62] value1[6]: "1.0.0-alpha-1"
            0,
            // [63] value1[7]: "1.0.0-beta1"
            -1,
            // [64] value1[8]: "1.0.0-beta-2"
            -1,
            // [65] value1[9]: "1.0.0-beta10"
            -1,
            // [66] value1[10]: "1.0.0-rc1"
            -17,
            //
            // value0[6]: "1.0.0-alpha-1"
            // [67] value1[0]: "1.0.0"
            -1,
            // [68] value1[1]: "1.9.0"
            -9,
            // [69] value1[2]: "1.10.0"
            -10,
            // [70] value1[3]: "1.11.0"
            -11,
            // [71] value1[4]: "1.0.0-alpha"
            -1,
            // [72] value1[5]: "1.0.0-alpha1"
            0,
            // [73] value1[6]: "1.0.0-alpha-1"
            0,
            // [74] value1[7]: "1.0.0-beta1"
            -1,
            // [75] value1[8]: "1.0.0-beta-2"
            -1,
            // [76] value1[9]: "1.0.0-beta10"
            -1,
            // [77] value1[10]: "1.0.0-rc1"
            -17,
            //
            // value0[7]: "1.0.0-beta1"
            // [78] value1[0]: "1.0.0"
            -1,
            // [79] value1[1]: "1.9.0"
            -9,
            // [80] value1[2]: "1.10.0"
            -10,
            // [81] value1[3]: "1.11.0"
            -11,
            // [82] value1[4]: "1.0.0-alpha"
            1,
            // [83] value1[5]: "1.0.0-alpha1"
            1,
            // [84] value1[6]: "1.0.0-alpha-1"
            1,
            // [85] value1[7]: "1.0.0-beta1"
            0,
            // [86] value1[8]: "1.0.0-beta-2"
            -1,
            // [87] value1[9]: "1.0.0-beta10"
            -1,
            // [88] value1[10]: "1.0.0-rc1"
            -16,
            //
            // value0[8]: "1.0.0-beta-2"
            // [89] value1[0]: "1.0.0"
            -1,
            // [90] value1[1]: "1.9.0"
            -9,
            // [91] value1[2]: "1.10.0"
            -10,
            // [92] value1[3]: "1.11.0"
            -11,
            // [93] value1[4]: "1.0.0-alpha"
            1,
            // [94] value1[5]: "1.0.0-alpha1"
            1,
            // [95] value1[6]: "1.0.0-alpha-1"
            1,
            // [96] value1[7]: "1.0.0-beta1"
            1,
            // [97] value1[8]: "1.0.0-beta-2"
            0,
            // [98] value1[9]: "1.0.0-beta10"
            -1,
            // [99] value1[10]: "1.0.0-rc1"
            -16,
            //
            // value0[9]: "1.0.0-beta10"
            // [100] value1[0]: "1.0.0"
            -1,
            // [101] value1[1]: "1.9.0"
            -9,
            // [102] value1[2]: "1.10.0"
            -10,
            // [103] value1[3]: "1.11.0"
            -11,
            // [104] value1[4]: "1.0.0-alpha"
            1,
            // [105] value1[5]: "1.0.0-alpha1"
            1,
            // [106] value1[6]: "1.0.0-alpha-1"
            1,
            // [107] value1[7]: "1.0.0-beta1"
            1,
            // [108] value1[8]: "1.0.0-beta-2"
            1,
            // [109] value1[9]: "1.0.0-beta10"
            0,
            // [110] value1[10]: "1.0.0-rc1"
            -16,
            //
            // value0[10]: "1.0.0-rc1"
            // [111] value1[0]: "1.0.0"
            -1,
            // [112] value1[1]: "1.9.0"
            -9,
            // [113] value1[2]: "1.10.0"
            -10,
            // [114] value1[3]: "1.11.0"
            -11,
            // [115] value1[4]: "1.0.0-alpha"
            17,
            // [116] value1[5]: "1.0.0-alpha1"
            17,
            // [117] value1[6]: "1.0.0-alpha-1"
            17,
            // [118] value1[7]: "1.0.0-beta1"
            16,
            // [119] value1[8]: "1.0.0-beta-2"
            16,
            // [120] value1[9]: "1.0.0-beta10"
            16,
            // [121] value1[10]: "1.0.0-rc1"
            0),
        // value0
        VERSION_LITERALS__VALID,
        // value1
        VERSION_LITERALS__VALID);
  }

  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> next() {
    return argumentsStream(
        cartesian()
            .<String>composeExpectedConverter(SemVer1::of)
            .<String>composeArgConverter(0, SemVer1::of),
        // expected
        asList(
            // value[0]: "1.0.0"
            // [1] id[0]: "MAJOR"
            "2.0.0",
            // [2] id[1]: "MINOR"
            "1.1.0",
            // [3] id[2]: "PATCH"
            "1.0.1",
            // [4] id[3]: "PRERELEASE"
            new Failure("IllegalStateException",
                "Regular version cannot increment undefined pre-release"),
            // [5] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[1]: "1.9.0"
            // [6] id[0]: "MAJOR"
            "2.0.0",
            // [7] id[1]: "MINOR"
            "1.10.0",
            // [8] id[2]: "PATCH"
            "1.9.1",
            // [9] id[3]: "PRERELEASE"
            new Failure("IllegalStateException",
                "Regular version cannot increment undefined pre-release"),
            // [10] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[2]: "1.10.0"
            // [11] id[0]: "MAJOR"
            "2.0.0",
            // [12] id[1]: "MINOR"
            "1.11.0",
            // [13] id[2]: "PATCH"
            "1.10.1",
            // [14] id[3]: "PRERELEASE"
            new Failure("IllegalStateException",
                "Regular version cannot increment undefined pre-release"),
            // [15] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[3]: "1.11.0"
            // [16] id[0]: "MAJOR"
            "2.0.0",
            // [17] id[1]: "MINOR"
            "1.12.0",
            // [18] id[2]: "PATCH"
            "1.11.1",
            // [19] id[3]: "PRERELEASE"
            new Failure("IllegalStateException",
                "Regular version cannot increment undefined pre-release"),
            // [20] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[4]: "1.0.0-alpha"
            // [21] id[0]: "MAJOR"
            "2.0.0",
            // [22] id[1]: "MINOR"
            "1.1.0",
            // [23] id[2]: "PATCH"
            "1.0.1",
            // [24] id[3]: "PRERELEASE"
            "1.0.0-alpha-1",
            // [25] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[5]: "1.0.0-alpha1"
            // [26] id[0]: "MAJOR"
            "2.0.0",
            // [27] id[1]: "MINOR"
            "1.1.0",
            // [28] id[2]: "PATCH"
            "1.0.1",
            // [29] id[3]: "PRERELEASE"
            "1.0.0-alpha2",
            // [30] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[6]: "1.0.0-alpha-1"
            // [31] id[0]: "MAJOR"
            "2.0.0",
            // [32] id[1]: "MINOR"
            "1.1.0",
            // [33] id[2]: "PATCH"
            "1.0.1",
            // [34] id[3]: "PRERELEASE"
            "1.0.0-alpha-2",
            // [35] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[7]: "1.0.0-beta1"
            // [36] id[0]: "MAJOR"
            "2.0.0",
            // [37] id[1]: "MINOR"
            "1.1.0",
            // [38] id[2]: "PATCH"
            "1.0.1",
            // [39] id[3]: "PRERELEASE"
            "1.0.0-beta2",
            // [40] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[8]: "1.0.0-beta-2"
            // [41] id[0]: "MAJOR"
            "2.0.0",
            // [42] id[1]: "MINOR"
            "1.1.0",
            // [43] id[2]: "PATCH"
            "1.0.1",
            // [44] id[3]: "PRERELEASE"
            "1.0.0-beta-3",
            // [45] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[9]: "1.0.0-beta10"
            // [46] id[0]: "MAJOR"
            "2.0.0",
            // [47] id[1]: "MINOR"
            "1.1.0",
            // [48] id[2]: "PATCH"
            "1.0.1",
            // [49] id[3]: "PRERELEASE"
            "1.0.0-beta11",
            // [50] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[10]: "1.0.0-rc1"
            // [51] id[0]: "MAJOR"
            "2.0.0",
            // [52] id[1]: "MINOR"
            "1.1.0",
            // [53] id[2]: "PATCH"
            "1.0.1",
            // [54] id[3]: "PRERELEASE"
            "1.0.0-rc2",
            // [55] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID")),
        // value
        VERSION_LITERALS__VALID,
        // id
        List.of(Id.values()));
  }

  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> of_String() {
    return argumentsStream(
        cartesian()
            .<String>composeExpectedConverter(SemVer1::of),
        // expected
        asList(
            // [1] value[0]: "1.0.0"
            "1.0.0",
            // [2] value[1]: "1.9.0"
            "1.9.0",
            // [3] value[2]: "1.10.0"
            "1.10.0",
            // [4] value[3]: "1.11.0"
            "1.11.0",
            // [5] value[4]: "1.0.0-alpha"
            "1.0.0-alpha",
            // [6] value[5]: "1.0.0-alpha1"
            "1.0.0-alpha1",
            // [7] value[6]: "1.0.0-alpha-1"
            "1.0.0-alpha-1",
            // [8] value[7]: "1.0.0-beta1"
            "1.0.0-beta1",
            // [9] value[8]: "1.0.0-beta-2"
            "1.0.0-beta-2",
            // [10] value[9]: "1.0.0-beta10"
            "1.0.0-beta10",
            // [11] value[10]: "1.0.0-rc1"
            "1.0.0-rc1",
            // [12] value[11]: "1.11.0.99"
            new Failure("ArgumentFormatException", "`value` (\"1.11.0.99\"): INVALID at index 6"),
            // [13] value[12]: "1.0.0.5-alpha"
            new Failure("ArgumentFormatException",
                "`value` (\"1.0.0.5-alpha\"): INVALID at index 5"),
            // [14] value[13]: "1.-11.0"
            new Failure("ArgumentFormatException", "`value` (\"1.-11.0\"): INVALID at index 2"),
            // [15] value[14]: "1.01.0"
            new Failure("ArgumentFormatException", "`value` (\"1.01.0\"): INVALID at index 3"),
            // [16] value[15]: "1.0.0-00.3.7"
            new Failure("ArgumentFormatException",
                "`value` (\"1.0.0-00.3.7\"): INVALID at index 8"),
            // [17] value[16]: "1.0.0_alpha"
            new Failure("ArgumentFormatException", "`value` (\"1.0.0_alpha\"): INVALID at index 5"),
            // [18] value[17]: "1.0.0-beta+exp.sha.5114f_85"
            new Failure("ArgumentFormatException",
                "`value` (\"1.0.0-beta+exp.sha.5114f_85\"): INVALID at index 10")),
        // value
        VERSION_LITERALS__MIXED);
  }

  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> of_int_int_int_String() {
    return argumentsStream(
        simple()
            .<String>composeExpectedConverter(SemVer1::of),
        // expected
        asList(
            // [1] major[0]: 1; minor[0]: 0; patch[0]: 0; prerelease[0]: null
            "1.0.0",
            // [2] major[1]: 1; minor[1]: 0; patch[1]: 0; prerelease[1]: "alpha1"
            "1.0.0-alpha1",
            // [3] major[2]: -1; minor[2]: 0; patch[2]: 0; prerelease[2]: "alpha1"
            new Failure("ArgumentException", "`major` (-1): INVALID"),
            // [4] major[3]: 1; minor[3]: -1; patch[3]: 0; prerelease[3]: "alpha1"
            new Failure("ArgumentException", "`minor` (-1): INVALID"),
            // [5] major[4]: 1; minor[4]: 0; patch[4]: -1; prerelease[4]: "alpha1"
            new Failure("ArgumentException", "`patch` (-1): INVALID"),
            // [6] major[5]: 1; minor[5]: 0; patch[5]: 0; prerelease[5]: "alpha.1"
            new Failure("ArgumentException", "`prerelease` (\"alpha.1\"): INVALID"),
            // [7] major[6]: 1; minor[6]: 0; patch[6]: 0; prerelease[6]: "alpha1+abc"
            new Failure("ArgumentException", "`prerelease` (\"alpha1+abc\"): INVALID")),
        // major, minor, patch, prerelease, metadata
        // VALID
        asList(1, 0, 0, null),
        List.of(1, 0, 0, "alpha1"),
        // INVALID
        List.of(-1, 0, 0, "alpha1"),
        List.of(1, -1, 0, "alpha1"),
        List.of(1, 0, -1, "alpha1"),
        List.of(1, 0, 0, "alpha.1"),
        List.of(1, 0, 0, "alpha1+abc"));
  }

  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> precedence() {
    return argumentsStream(
        cartesian()
            .<String>composeArgConverter(0, SemVer1::of)
            .<String>composeArgConverter(1, SemVer1::of),
        // expected
        asList(
            // value0[0]: "1.0.0"
            // [1] value1[0]: "1.0.0"
            0,
            // [2] value1[1]: "1.9.0"
            -9,
            // [3] value1[2]: "1.10.0"
            -10,
            // [4] value1[3]: "1.11.0"
            -11,
            // [5] value1[4]: "1.0.0-alpha"
            1,
            // [6] value1[5]: "1.0.0-alpha1"
            1,
            // [7] value1[6]: "1.0.0-alpha-1"
            1,
            // [8] value1[7]: "1.0.0-beta1"
            1,
            // [9] value1[8]: "1.0.0-beta-2"
            1,
            // [10] value1[9]: "1.0.0-beta10"
            1,
            // [11] value1[10]: "1.0.0-rc1"
            1,
            //
            // value0[1]: "1.9.0"
            // [12] value1[0]: "1.0.0"
            9,
            // [13] value1[1]: "1.9.0"
            0,
            // [14] value1[2]: "1.10.0"
            -1,
            // [15] value1[3]: "1.11.0"
            -2,
            // [16] value1[4]: "1.0.0-alpha"
            9,
            // [17] value1[5]: "1.0.0-alpha1"
            9,
            // [18] value1[6]: "1.0.0-alpha-1"
            9,
            // [19] value1[7]: "1.0.0-beta1"
            9,
            // [20] value1[8]: "1.0.0-beta-2"
            9,
            // [21] value1[9]: "1.0.0-beta10"
            9,
            // [22] value1[10]: "1.0.0-rc1"
            9,
            //
            // value0[2]: "1.10.0"
            // [23] value1[0]: "1.0.0"
            10,
            // [24] value1[1]: "1.9.0"
            1,
            // [25] value1[2]: "1.10.0"
            0,
            // [26] value1[3]: "1.11.0"
            -1,
            // [27] value1[4]: "1.0.0-alpha"
            10,
            // [28] value1[5]: "1.0.0-alpha1"
            10,
            // [29] value1[6]: "1.0.0-alpha-1"
            10,
            // [30] value1[7]: "1.0.0-beta1"
            10,
            // [31] value1[8]: "1.0.0-beta-2"
            10,
            // [32] value1[9]: "1.0.0-beta10"
            10,
            // [33] value1[10]: "1.0.0-rc1"
            10,
            //
            // value0[3]: "1.11.0"
            // [34] value1[0]: "1.0.0"
            11,
            // [35] value1[1]: "1.9.0"
            2,
            // [36] value1[2]: "1.10.0"
            1,
            // [37] value1[3]: "1.11.0"
            0,
            // [38] value1[4]: "1.0.0-alpha"
            11,
            // [39] value1[5]: "1.0.0-alpha1"
            11,
            // [40] value1[6]: "1.0.0-alpha-1"
            11,
            // [41] value1[7]: "1.0.0-beta1"
            11,
            // [42] value1[8]: "1.0.0-beta-2"
            11,
            // [43] value1[9]: "1.0.0-beta10"
            11,
            // [44] value1[10]: "1.0.0-rc1"
            11,
            //
            // value0[4]: "1.0.0-alpha"
            // [45] value1[0]: "1.0.0"
            -1,
            // [46] value1[1]: "1.9.0"
            -9,
            // [47] value1[2]: "1.10.0"
            -10,
            // [48] value1[3]: "1.11.0"
            -11,
            // [49] value1[4]: "1.0.0-alpha"
            0,
            // [50] value1[5]: "1.0.0-alpha1"
            -1,
            // [51] value1[6]: "1.0.0-alpha-1"
            -1,
            // [52] value1[7]: "1.0.0-beta1"
            -1,
            // [53] value1[8]: "1.0.0-beta-2"
            -1,
            // [54] value1[9]: "1.0.0-beta10"
            -1,
            // [55] value1[10]: "1.0.0-rc1"
            -17,
            //
            // value0[5]: "1.0.0-alpha1"
            // [56] value1[0]: "1.0.0"
            -1,
            // [57] value1[1]: "1.9.0"
            -9,
            // [58] value1[2]: "1.10.0"
            -10,
            // [59] value1[3]: "1.11.0"
            -11,
            // [60] value1[4]: "1.0.0-alpha"
            -1,
            // [61] value1[5]: "1.0.0-alpha1"
            0,
            // [62] value1[6]: "1.0.0-alpha-1"
            0,
            // [63] value1[7]: "1.0.0-beta1"
            -1,
            // [64] value1[8]: "1.0.0-beta-2"
            -1,
            // [65] value1[9]: "1.0.0-beta10"
            -1,
            // [66] value1[10]: "1.0.0-rc1"
            -17,
            //
            // value0[6]: "1.0.0-alpha-1"
            // [67] value1[0]: "1.0.0"
            -1,
            // [68] value1[1]: "1.9.0"
            -9,
            // [69] value1[2]: "1.10.0"
            -10,
            // [70] value1[3]: "1.11.0"
            -11,
            // [71] value1[4]: "1.0.0-alpha"
            -1,
            // [72] value1[5]: "1.0.0-alpha1"
            0,
            // [73] value1[6]: "1.0.0-alpha-1"
            0,
            // [74] value1[7]: "1.0.0-beta1"
            -1,
            // [75] value1[8]: "1.0.0-beta-2"
            -1,
            // [76] value1[9]: "1.0.0-beta10"
            -1,
            // [77] value1[10]: "1.0.0-rc1"
            -17,
            //
            // value0[7]: "1.0.0-beta1"
            // [78] value1[0]: "1.0.0"
            -1,
            // [79] value1[1]: "1.9.0"
            -9,
            // [80] value1[2]: "1.10.0"
            -10,
            // [81] value1[3]: "1.11.0"
            -11,
            // [82] value1[4]: "1.0.0-alpha"
            1,
            // [83] value1[5]: "1.0.0-alpha1"
            1,
            // [84] value1[6]: "1.0.0-alpha-1"
            1,
            // [85] value1[7]: "1.0.0-beta1"
            0,
            // [86] value1[8]: "1.0.0-beta-2"
            -1,
            // [87] value1[9]: "1.0.0-beta10"
            -1,
            // [88] value1[10]: "1.0.0-rc1"
            -16,
            //
            // value0[8]: "1.0.0-beta-2"
            // [89] value1[0]: "1.0.0"
            -1,
            // [90] value1[1]: "1.9.0"
            -9,
            // [91] value1[2]: "1.10.0"
            -10,
            // [92] value1[3]: "1.11.0"
            -11,
            // [93] value1[4]: "1.0.0-alpha"
            1,
            // [94] value1[5]: "1.0.0-alpha1"
            1,
            // [95] value1[6]: "1.0.0-alpha-1"
            1,
            // [96] value1[7]: "1.0.0-beta1"
            1,
            // [97] value1[8]: "1.0.0-beta-2"
            0,
            // [98] value1[9]: "1.0.0-beta10"
            -1,
            // [99] value1[10]: "1.0.0-rc1"
            -16,
            //
            // value0[9]: "1.0.0-beta10"
            // [100] value1[0]: "1.0.0"
            -1,
            // [101] value1[1]: "1.9.0"
            -9,
            // [102] value1[2]: "1.10.0"
            -10,
            // [103] value1[3]: "1.11.0"
            -11,
            // [104] value1[4]: "1.0.0-alpha"
            1,
            // [105] value1[5]: "1.0.0-alpha1"
            1,
            // [106] value1[6]: "1.0.0-alpha-1"
            1,
            // [107] value1[7]: "1.0.0-beta1"
            1,
            // [108] value1[8]: "1.0.0-beta-2"
            1,
            // [109] value1[9]: "1.0.0-beta10"
            0,
            // [110] value1[10]: "1.0.0-rc1"
            -16,
            //
            // value0[10]: "1.0.0-rc1"
            // [111] value1[0]: "1.0.0"
            -1,
            // [112] value1[1]: "1.9.0"
            -9,
            // [113] value1[2]: "1.10.0"
            -10,
            // [114] value1[3]: "1.11.0"
            -11,
            // [115] value1[4]: "1.0.0-alpha"
            17,
            // [116] value1[5]: "1.0.0-alpha1"
            17,
            // [117] value1[6]: "1.0.0-alpha-1"
            17,
            // [118] value1[7]: "1.0.0-beta1"
            16,
            // [119] value1[8]: "1.0.0-beta-2"
            16,
            // [120] value1[9]: "1.0.0-beta10"
            16,
            // [121] value1[10]: "1.0.0-rc1"
            0),
        // value0
        VERSION_LITERALS__VALID,
        // value1
        VERSION_LITERALS__VALID);
  }

  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> with() {
    return argumentsStream(
        cartesian()
            .<String>composeExpectedConverter(SemVer1::of)
            .<String>composeArgConverter(0, SemVer1::of),
        // expected
        asList(
            // ver[0]: "1.0.0"
            // -- id[0]: "MAJOR"
            // [1] value[0]: 8
            "8.0.0",
            // [2] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [3] value[0]: 8
            "1.8.0",
            // [4] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [5] value[0]: 8
            "1.0.8",
            // [6] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [7] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [8] value[1]: "rc"
            "1.0.0-rc",
            // -- id[4]: "METADATA"
            // [9] value[0]: 8
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            // [10] value[1]: "rc"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // ver[1]: "1.9.0"
            // -- id[0]: "MAJOR"
            // [11] value[0]: 8
            "8.0.0",
            // [12] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [13] value[0]: 8
            "1.8.0",
            // [14] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [15] value[0]: 8
            "1.9.8",
            // [16] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [17] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [18] value[1]: "rc"
            "1.9.0-rc",
            // -- id[4]: "METADATA"
            // [19] value[0]: 8
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            // [20] value[1]: "rc"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // ver[2]: "1.10.0"
            // -- id[0]: "MAJOR"
            // [21] value[0]: 8
            "8.0.0",
            // [22] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [23] value[0]: 8
            "1.8.0",
            // [24] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [25] value[0]: 8
            "1.10.8",
            // [26] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [27] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [28] value[1]: "rc"
            "1.10.0-rc",
            // -- id[4]: "METADATA"
            // [29] value[0]: 8
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            // [30] value[1]: "rc"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // ver[3]: "1.11.0"
            // -- id[0]: "MAJOR"
            // [31] value[0]: 8
            "8.0.0",
            // [32] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [33] value[0]: 8
            "1.8.0",
            // [34] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [35] value[0]: 8
            "1.11.8",
            // [36] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [37] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [38] value[1]: "rc"
            "1.11.0-rc",
            // -- id[4]: "METADATA"
            // [39] value[0]: 8
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            // [40] value[1]: "rc"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // ver[4]: "1.0.0-alpha"
            // -- id[0]: "MAJOR"
            // [41] value[0]: 8
            "8.0.0",
            // [42] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [43] value[0]: 8
            "1.8.0",
            // [44] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [45] value[0]: 8
            "1.0.8",
            // [46] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [47] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [48] value[1]: "rc"
            "1.0.0-rc",
            // -- id[4]: "METADATA"
            // [49] value[0]: 8
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            // [50] value[1]: "rc"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // ver[5]: "1.0.0-alpha1"
            // -- id[0]: "MAJOR"
            // [51] value[0]: 8
            "8.0.0",
            // [52] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [53] value[0]: 8
            "1.8.0",
            // [54] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [55] value[0]: 8
            "1.0.8",
            // [56] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [57] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [58] value[1]: "rc"
            "1.0.0-rc",
            // -- id[4]: "METADATA"
            // [59] value[0]: 8
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            // [60] value[1]: "rc"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // ver[6]: "1.0.0-alpha-1"
            // -- id[0]: "MAJOR"
            // [61] value[0]: 8
            "8.0.0",
            // [62] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [63] value[0]: 8
            "1.8.0",
            // [64] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [65] value[0]: 8
            "1.0.8",
            // [66] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [67] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [68] value[1]: "rc"
            "1.0.0-rc",
            // -- id[4]: "METADATA"
            // [69] value[0]: 8
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            // [70] value[1]: "rc"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // ver[7]: "1.0.0-beta1"
            // -- id[0]: "MAJOR"
            // [71] value[0]: 8
            "8.0.0",
            // [72] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [73] value[0]: 8
            "1.8.0",
            // [74] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [75] value[0]: 8
            "1.0.8",
            // [76] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [77] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [78] value[1]: "rc"
            "1.0.0-rc",
            // -- id[4]: "METADATA"
            // [79] value[0]: 8
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            // [80] value[1]: "rc"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // ver[8]: "1.0.0-beta-2"
            // -- id[0]: "MAJOR"
            // [81] value[0]: 8
            "8.0.0",
            // [82] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [83] value[0]: 8
            "1.8.0",
            // [84] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [85] value[0]: 8
            "1.0.8",
            // [86] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [87] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [88] value[1]: "rc"
            "1.0.0-rc",
            // -- id[4]: "METADATA"
            // [89] value[0]: 8
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            // [90] value[1]: "rc"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // ver[9]: "1.0.0-beta10"
            // -- id[0]: "MAJOR"
            // [91] value[0]: 8
            "8.0.0",
            // [92] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [93] value[0]: 8
            "1.8.0",
            // [94] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [95] value[0]: 8
            "1.0.8",
            // [96] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [97] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [98] value[1]: "rc"
            "1.0.0-rc",
            // -- id[4]: "METADATA"
            // [99] value[0]: 8
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            // [100] value[1]: "rc"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // ver[10]: "1.0.0-rc1"
            // -- id[0]: "MAJOR"
            // [101] value[0]: 8
            "8.0.0",
            // [102] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [103] value[0]: 8
            "1.8.0",
            // [104] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [105] value[0]: 8
            "1.0.8",
            // [106] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [107] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [108] value[1]: "rc"
            "1.0.0-rc",
            // -- id[4]: "METADATA"
            // [109] value[0]: 8
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            // [110] value[1]: "rc"
            new Failure("ArgumentException", "`id` (METADATA): INVALID")),
        // ver
        VERSION_LITERALS__VALID,
        // id
        List.of(Id.values()),
        // value
        List.of(8, "rc"));
  }

  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> withPrereleaseSuffix() {
    return argumentsStream(
        cartesian()
            .<String>composeExpectedConverter(SemVer1::of)
            .<String>composeArgConverter(0, SemVer1::of),
        // expected
        asList(
            // ver[0]: "1.0.0"
            // -- fieldIndex[0]: -1
            // [1] value[0]: 8
            "1.0.0-8",
            // [2] value[1]: "rc"
            "1.0.0-rc",
            // [3] value[2]: "SNAPSHOT"
            "1.0.0-SNAPSHOT",
            // -- fieldIndex[1]: 0
            // [4] value[0]: 8
            "1.0.0-8",
            // [5] value[1]: "rc"
            "1.0.0-rc",
            // [6] value[2]: "SNAPSHOT"
            "1.0.0-SNAPSHOT",
            // -- fieldIndex[2]: 1
            // [7] value[0]: 8
            new Failure("IndexOutOfBoundsException", "toIndex = 1"),
            // [8] value[1]: "rc"
            new Failure("IndexOutOfBoundsException", "toIndex = 1"),
            // [9] value[2]: "SNAPSHOT"
            new Failure("IndexOutOfBoundsException", "toIndex = 1"),
            //
            // ver[1]: "1.9.0"
            // -- fieldIndex[0]: -1
            // [10] value[0]: 8
            "1.9.0-8",
            // [11] value[1]: "rc"
            "1.9.0-rc",
            // [12] value[2]: "SNAPSHOT"
            "1.9.0-SNAPSHOT",
            // -- fieldIndex[1]: 0
            // [13] value[0]: 8
            "1.9.0-8",
            // [14] value[1]: "rc"
            "1.9.0-rc",
            // [15] value[2]: "SNAPSHOT"
            "1.9.0-SNAPSHOT",
            // -- fieldIndex[2]: 1
            // [16] value[0]: 8
            new Failure("IndexOutOfBoundsException", "toIndex = 1"),
            // [17] value[1]: "rc"
            new Failure("IndexOutOfBoundsException", "toIndex = 1"),
            // [18] value[2]: "SNAPSHOT"
            new Failure("IndexOutOfBoundsException", "toIndex = 1"),
            //
            // ver[2]: "1.10.0"
            // -- fieldIndex[0]: -1
            // [19] value[0]: 8
            "1.10.0-8",
            // [20] value[1]: "rc"
            "1.10.0-rc",
            // [21] value[2]: "SNAPSHOT"
            "1.10.0-SNAPSHOT",
            // -- fieldIndex[1]: 0
            // [22] value[0]: 8
            "1.10.0-8",
            // [23] value[1]: "rc"
            "1.10.0-rc",
            // [24] value[2]: "SNAPSHOT"
            "1.10.0-SNAPSHOT",
            // -- fieldIndex[2]: 1
            // [25] value[0]: 8
            new Failure("IndexOutOfBoundsException", "toIndex = 1"),
            // [26] value[1]: "rc"
            new Failure("IndexOutOfBoundsException", "toIndex = 1"),
            // [27] value[2]: "SNAPSHOT"
            new Failure("IndexOutOfBoundsException", "toIndex = 1"),
            //
            // ver[3]: "1.11.0"
            // -- fieldIndex[0]: -1
            // [28] value[0]: 8
            "1.11.0-8",
            // [29] value[1]: "rc"
            "1.11.0-rc",
            // [30] value[2]: "SNAPSHOT"
            "1.11.0-SNAPSHOT",
            // -- fieldIndex[1]: 0
            // [31] value[0]: 8
            "1.11.0-8",
            // [32] value[1]: "rc"
            "1.11.0-rc",
            // [33] value[2]: "SNAPSHOT"
            "1.11.0-SNAPSHOT",
            // -- fieldIndex[2]: 1
            // [34] value[0]: 8
            new Failure("IndexOutOfBoundsException", "toIndex = 1"),
            // [35] value[1]: "rc"
            new Failure("IndexOutOfBoundsException", "toIndex = 1"),
            // [36] value[2]: "SNAPSHOT"
            new Failure("IndexOutOfBoundsException", "toIndex = 1"),
            //
            // ver[4]: "1.0.0-alpha"
            // -- fieldIndex[0]: -1
            // [37] value[0]: 8
            "1.0.0-alpha-8",
            // [38] value[1]: "rc"
            "1.0.0-alpha-rc",
            // [39] value[2]: "SNAPSHOT"
            "1.0.0-alpha-SNAPSHOT",
            // -- fieldIndex[1]: 0
            // [40] value[0]: 8
            "1.0.0-8",
            // [41] value[1]: "rc"
            "1.0.0-rc",
            // [42] value[2]: "SNAPSHOT"
            "1.0.0-SNAPSHOT",
            // -- fieldIndex[2]: 1
            // [43] value[0]: 8
            "1.0.0-alpha-8",
            // [44] value[1]: "rc"
            "1.0.0-alpha-rc",
            // [45] value[2]: "SNAPSHOT"
            "1.0.0-alpha-SNAPSHOT",
            //
            // ver[5]: "1.0.0-alpha1"
            // -- fieldIndex[0]: -1
            // [46] value[0]: 8
            "1.0.0-alpha1-8",
            // [47] value[1]: "rc"
            "1.0.0-alpha1-rc",
            // [48] value[2]: "SNAPSHOT"
            "1.0.0-alpha1-SNAPSHOT",
            // -- fieldIndex[1]: 0
            // [49] value[0]: 8
            "1.0.0-8",
            // [50] value[1]: "rc"
            "1.0.0-rc",
            // [51] value[2]: "SNAPSHOT"
            "1.0.0-SNAPSHOT",
            // -- fieldIndex[2]: 1
            // [52] value[0]: 8
            "1.0.0-alpha8",
            // [53] value[1]: "rc"
            "1.0.0-alpha-rc",
            // [54] value[2]: "SNAPSHOT"
            "1.0.0-alpha-SNAPSHOT",
            //
            // ver[6]: "1.0.0-alpha-1"
            // -- fieldIndex[0]: -1
            // [55] value[0]: 8
            "1.0.0-alpha-1-8",
            // [56] value[1]: "rc"
            "1.0.0-alpha-1-rc",
            // [57] value[2]: "SNAPSHOT"
            "1.0.0-alpha-1-SNAPSHOT",
            // -- fieldIndex[1]: 0
            // [58] value[0]: 8
            "1.0.0-8",
            // [59] value[1]: "rc"
            "1.0.0-rc",
            // [60] value[2]: "SNAPSHOT"
            "1.0.0-SNAPSHOT",
            // -- fieldIndex[2]: 1
            // [61] value[0]: 8
            "1.0.0-alpha-8",
            // [62] value[1]: "rc"
            "1.0.0-alpha-rc",
            // [63] value[2]: "SNAPSHOT"
            "1.0.0-alpha-SNAPSHOT",
            //
            // ver[7]: "1.0.0-beta1"
            // -- fieldIndex[0]: -1
            // [64] value[0]: 8
            "1.0.0-beta1-8",
            // [65] value[1]: "rc"
            "1.0.0-beta1-rc",
            // [66] value[2]: "SNAPSHOT"
            "1.0.0-beta1-SNAPSHOT",
            // -- fieldIndex[1]: 0
            // [67] value[0]: 8
            "1.0.0-8",
            // [68] value[1]: "rc"
            "1.0.0-rc",
            // [69] value[2]: "SNAPSHOT"
            "1.0.0-SNAPSHOT",
            // -- fieldIndex[2]: 1
            // [70] value[0]: 8
            "1.0.0-beta8",
            // [71] value[1]: "rc"
            "1.0.0-beta-rc",
            // [72] value[2]: "SNAPSHOT"
            "1.0.0-beta-SNAPSHOT",
            //
            // ver[8]: "1.0.0-beta-2"
            // -- fieldIndex[0]: -1
            // [73] value[0]: 8
            "1.0.0-beta-2-8",
            // [74] value[1]: "rc"
            "1.0.0-beta-2-rc",
            // [75] value[2]: "SNAPSHOT"
            "1.0.0-beta-2-SNAPSHOT",
            // -- fieldIndex[1]: 0
            // [76] value[0]: 8
            "1.0.0-8",
            // [77] value[1]: "rc"
            "1.0.0-rc",
            // [78] value[2]: "SNAPSHOT"
            "1.0.0-SNAPSHOT",
            // -- fieldIndex[2]: 1
            // [79] value[0]: 8
            "1.0.0-beta-8",
            // [80] value[1]: "rc"
            "1.0.0-beta-rc",
            // [81] value[2]: "SNAPSHOT"
            "1.0.0-beta-SNAPSHOT",
            //
            // ver[9]: "1.0.0-beta10"
            // -- fieldIndex[0]: -1
            // [82] value[0]: 8
            "1.0.0-beta10-8",
            // [83] value[1]: "rc"
            "1.0.0-beta10-rc",
            // [84] value[2]: "SNAPSHOT"
            "1.0.0-beta10-SNAPSHOT",
            // -- fieldIndex[1]: 0
            // [85] value[0]: 8
            "1.0.0-8",
            // [86] value[1]: "rc"
            "1.0.0-rc",
            // [87] value[2]: "SNAPSHOT"
            "1.0.0-SNAPSHOT",
            // -- fieldIndex[2]: 1
            // [88] value[0]: 8
            "1.0.0-beta8",
            // [89] value[1]: "rc"
            "1.0.0-beta-rc",
            // [90] value[2]: "SNAPSHOT"
            "1.0.0-beta-SNAPSHOT",
            //
            // ver[10]: "1.0.0-rc1"
            // -- fieldIndex[0]: -1
            // [91] value[0]: 8
            "1.0.0-rc1-8",
            // [92] value[1]: "rc"
            "1.0.0-rc1-rc",
            // [93] value[2]: "SNAPSHOT"
            "1.0.0-rc1-SNAPSHOT",
            // -- fieldIndex[1]: 0
            // [94] value[0]: 8
            "1.0.0-8",
            // [95] value[1]: "rc"
            "1.0.0-rc",
            // [96] value[2]: "SNAPSHOT"
            "1.0.0-SNAPSHOT",
            // -- fieldIndex[2]: 1
            // [97] value[0]: 8
            "1.0.0-rc8",
            // [98] value[1]: "rc"
            "1.0.0-rc-rc",
            // [99] value[2]: "SNAPSHOT"
            "1.0.0-rc-SNAPSHOT"),
        // ver
        VERSION_LITERALS__VALID,
        // fieldIndex
        List.of(-1, 0, 1),
        // value
        List.of(8, "rc", "SNAPSHOT"));
  }

  @ParameterizedTest
  @MethodSource
  void compareTo(Expected<Integer> expected, SemVer1 value0, SemVer1 value1) {
    assertParameterizedOf(
        () -> value0.compareTo(value1),
        expected,
        () -> new ExpectedGeneration(value0, value1));
  }

  @ParameterizedTest
  @MethodSource
  void next(Expected<SemVer1> expected, SemVer1 value, Id id) {
    assertParameterizedOf(
        () -> value.next(id),
        expected,
        () -> new ExpectedGeneration(value, id));
  }

  @ParameterizedTest
  @MethodSource
  void of_String(Expected<SemVer1> expected, String value) {
    assertParameterizedOf(
        () -> SemVer1.of(value),
        expected,
        () -> new ExpectedGeneration(value));
  }

  @ParameterizedTest
  @MethodSource
  void of_int_int_int_String(Expected<SemVer1> expected, int major, int minor, int patch,
      @Nullable String prerelease) {
    assertParameterizedOf(
        () -> SemVer1.of(major, minor, patch, prerelease),
        expected,
        () -> new ExpectedGeneration(major, minor, patch, prerelease));
  }

  @ParameterizedTest
  @MethodSource
  void precedence(Expected<Integer> expected, SemVer1 value0, SemVer1 value1) {
    assertParameterizedOf(
        () -> value0.precedence(value1),
        expected,
        () -> new ExpectedGeneration(value0, value1));
  }

  @ParameterizedTest
  @MethodSource
  @SuppressWarnings("rawtypes")
  void with(Expected<SemVer1> expected, SemVer1 ver, Id id, Comparable value) {
    assertParameterizedOf(
        () -> ver.with(id, value),
        expected,
        () -> new ExpectedGeneration(ver, id, value));
  }

  @ParameterizedTest
  @MethodSource
  @SuppressWarnings("rawtypes")
  void withPrereleaseSuffix(Expected<SemVer1> expected, SemVer1 ver, int fieldIndex,
      Comparable value) {
    assertParameterizedOf(
        () -> ver.withPrereleaseSuffix(fieldIndex, value),
        expected,
        () -> new ExpectedGeneration(ver, fieldIndex, value));
  }
}