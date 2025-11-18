/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SemVerTest.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.meta;

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
import org.pdfclown.common.build.__test.BaseTest;
import org.pdfclown.common.build.meta.SemVer.Id;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.build.test.assertion.Assertions.Failure;

/**
 * @author Stefano Chizzolini
 */
class SemVerTest extends BaseTest {
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

  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> compareTo() {
    return argumentsStream(
        cartesian()
            .<String>composeArgConverter(0, SemVer::of)
            .<String>composeArgConverter(1, SemVer::of),
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
            // [6] value1[5]: "1.0.0-alpha.1"
            1,
            // [7] value1[6]: "1.0.0-0.3.7"
            1,
            // [8] value1[7]: "1.0.0-x.7.z.92"
            1,
            // [9] value1[8]: "1.0.0-x.-7.z.92"
            1,
            // [10] value1[9]: "1.0.0-x-y-z.--"
            1,
            // [11] value1[10]: "1.0.0-alpha+001"
            1,
            // [12] value1[11]: "1.0.0+20130313144700"
            -14,
            // [13] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            1,
            // [14] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -24,
            //
            // value0[1]: "1.9.0"
            // [15] value1[0]: "1.0.0"
            9,
            // [16] value1[1]: "1.9.0"
            0,
            // [17] value1[2]: "1.10.0"
            -1,
            // [18] value1[3]: "1.11.0"
            -2,
            // [19] value1[4]: "1.0.0-alpha"
            9,
            // [20] value1[5]: "1.0.0-alpha.1"
            9,
            // [21] value1[6]: "1.0.0-0.3.7"
            9,
            // [22] value1[7]: "1.0.0-x.7.z.92"
            9,
            // [23] value1[8]: "1.0.0-x.-7.z.92"
            9,
            // [24] value1[9]: "1.0.0-x-y-z.--"
            9,
            // [25] value1[10]: "1.0.0-alpha+001"
            9,
            // [26] value1[11]: "1.0.0+20130313144700"
            9,
            // [27] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            9,
            // [28] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            9,
            //
            // value0[2]: "1.10.0"
            // [29] value1[0]: "1.0.0"
            10,
            // [30] value1[1]: "1.9.0"
            1,
            // [31] value1[2]: "1.10.0"
            0,
            // [32] value1[3]: "1.11.0"
            -1,
            // [33] value1[4]: "1.0.0-alpha"
            10,
            // [34] value1[5]: "1.0.0-alpha.1"
            10,
            // [35] value1[6]: "1.0.0-0.3.7"
            10,
            // [36] value1[7]: "1.0.0-x.7.z.92"
            10,
            // [37] value1[8]: "1.0.0-x.-7.z.92"
            10,
            // [38] value1[9]: "1.0.0-x-y-z.--"
            10,
            // [39] value1[10]: "1.0.0-alpha+001"
            10,
            // [40] value1[11]: "1.0.0+20130313144700"
            10,
            // [41] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            10,
            // [42] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            10,
            //
            // value0[3]: "1.11.0"
            // [43] value1[0]: "1.0.0"
            11,
            // [44] value1[1]: "1.9.0"
            2,
            // [45] value1[2]: "1.10.0"
            1,
            // [46] value1[3]: "1.11.0"
            0,
            // [47] value1[4]: "1.0.0-alpha"
            11,
            // [48] value1[5]: "1.0.0-alpha.1"
            11,
            // [49] value1[6]: "1.0.0-0.3.7"
            11,
            // [50] value1[7]: "1.0.0-x.7.z.92"
            11,
            // [51] value1[8]: "1.0.0-x.-7.z.92"
            11,
            // [52] value1[9]: "1.0.0-x-y-z.--"
            11,
            // [53] value1[10]: "1.0.0-alpha+001"
            11,
            // [54] value1[11]: "1.0.0+20130313144700"
            11,
            // [55] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            11,
            // [56] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            11,
            //
            // value0[4]: "1.0.0-alpha"
            // [57] value1[0]: "1.0.0"
            -1,
            // [58] value1[1]: "1.9.0"
            -9,
            // [59] value1[2]: "1.10.0"
            -10,
            // [60] value1[3]: "1.11.0"
            -11,
            // [61] value1[4]: "1.0.0-alpha"
            0,
            // [62] value1[5]: "1.0.0-alpha.1"
            -1,
            // [63] value1[6]: "1.0.0-0.3.7"
            1,
            // [64] value1[7]: "1.0.0-x.7.z.92"
            -23,
            // [65] value1[8]: "1.0.0-x.-7.z.92"
            -23,
            // [66] value1[9]: "1.0.0-x-y-z.--"
            -23,
            // [67] value1[10]: "1.0.0-alpha+001"
            -3,
            // [68] value1[11]: "1.0.0+20130313144700"
            -1,
            // [69] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            -1,
            // [70] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[5]: "1.0.0-alpha.1"
            // [71] value1[0]: "1.0.0"
            -1,
            // [72] value1[1]: "1.9.0"
            -9,
            // [73] value1[2]: "1.10.0"
            -10,
            // [74] value1[3]: "1.11.0"
            -11,
            // [75] value1[4]: "1.0.0-alpha"
            -1,
            // [76] value1[5]: "1.0.0-alpha.1"
            0,
            // [77] value1[6]: "1.0.0-0.3.7"
            1,
            // [78] value1[7]: "1.0.0-x.7.z.92"
            -23,
            // [79] value1[8]: "1.0.0-x.-7.z.92"
            -23,
            // [80] value1[9]: "1.0.0-x-y-z.--"
            -23,
            // [81] value1[10]: "1.0.0-alpha+001"
            -1,
            // [82] value1[11]: "1.0.0+20130313144700"
            -1,
            // [83] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            -1,
            // [84] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[6]: "1.0.0-0.3.7"
            // [85] value1[0]: "1.0.0"
            -1,
            // [86] value1[1]: "1.9.0"
            -9,
            // [87] value1[2]: "1.10.0"
            -10,
            // [88] value1[3]: "1.11.0"
            -11,
            // [89] value1[4]: "1.0.0-alpha"
            -1,
            // [90] value1[5]: "1.0.0-alpha.1"
            -1,
            // [91] value1[6]: "1.0.0-0.3.7"
            0,
            // [92] value1[7]: "1.0.0-x.7.z.92"
            -1,
            // [93] value1[8]: "1.0.0-x.-7.z.92"
            -1,
            // [94] value1[9]: "1.0.0-x-y-z.--"
            -1,
            // [95] value1[10]: "1.0.0-alpha+001"
            -1,
            // [96] value1[11]: "1.0.0+20130313144700"
            -1,
            // [97] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            -1,
            // [98] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[7]: "1.0.0-x.7.z.92"
            // [99] value1[0]: "1.0.0"
            -1,
            // [100] value1[1]: "1.9.0"
            -9,
            // [101] value1[2]: "1.10.0"
            -10,
            // [102] value1[3]: "1.11.0"
            -11,
            // [103] value1[4]: "1.0.0-alpha"
            23,
            // [104] value1[5]: "1.0.0-alpha.1"
            23,
            // [105] value1[6]: "1.0.0-0.3.7"
            1,
            // [106] value1[7]: "1.0.0-x.7.z.92"
            0,
            // [107] value1[8]: "1.0.0-x.-7.z.92"
            -1,
            // [108] value1[9]: "1.0.0-x-y-z.--"
            -4,
            // [109] value1[10]: "1.0.0-alpha+001"
            23,
            // [110] value1[11]: "1.0.0+20130313144700"
            -1,
            // [111] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            22,
            // [112] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[8]: "1.0.0-x.-7.z.92"
            // [113] value1[0]: "1.0.0"
            -1,
            // [114] value1[1]: "1.9.0"
            -9,
            // [115] value1[2]: "1.10.0"
            -10,
            // [116] value1[3]: "1.11.0"
            -11,
            // [117] value1[4]: "1.0.0-alpha"
            23,
            // [118] value1[5]: "1.0.0-alpha.1"
            23,
            // [119] value1[6]: "1.0.0-0.3.7"
            1,
            // [120] value1[7]: "1.0.0-x.7.z.92"
            1,
            // [121] value1[8]: "1.0.0-x.-7.z.92"
            0,
            // [122] value1[9]: "1.0.0-x-y-z.--"
            -4,
            // [123] value1[10]: "1.0.0-alpha+001"
            23,
            // [124] value1[11]: "1.0.0+20130313144700"
            -1,
            // [125] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            22,
            // [126] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[9]: "1.0.0-x-y-z.--"
            // [127] value1[0]: "1.0.0"
            -1,
            // [128] value1[1]: "1.9.0"
            -9,
            // [129] value1[2]: "1.10.0"
            -10,
            // [130] value1[3]: "1.11.0"
            -11,
            // [131] value1[4]: "1.0.0-alpha"
            23,
            // [132] value1[5]: "1.0.0-alpha.1"
            23,
            // [133] value1[6]: "1.0.0-0.3.7"
            1,
            // [134] value1[7]: "1.0.0-x.7.z.92"
            4,
            // [135] value1[8]: "1.0.0-x.-7.z.92"
            4,
            // [136] value1[9]: "1.0.0-x-y-z.--"
            0,
            // [137] value1[10]: "1.0.0-alpha+001"
            23,
            // [138] value1[11]: "1.0.0+20130313144700"
            -1,
            // [139] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            22,
            // [140] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[10]: "1.0.0-alpha+001"
            // [141] value1[0]: "1.0.0"
            -1,
            // [142] value1[1]: "1.9.0"
            -9,
            // [143] value1[2]: "1.10.0"
            -10,
            // [144] value1[3]: "1.11.0"
            -11,
            // [145] value1[4]: "1.0.0-alpha"
            3,
            // [146] value1[5]: "1.0.0-alpha.1"
            -1,
            // [147] value1[6]: "1.0.0-0.3.7"
            1,
            // [148] value1[7]: "1.0.0-x.7.z.92"
            -23,
            // [149] value1[8]: "1.0.0-x.-7.z.92"
            -23,
            // [150] value1[9]: "1.0.0-x-y-z.--"
            -23,
            // [151] value1[10]: "1.0.0-alpha+001"
            0,
            // [152] value1[11]: "1.0.0+20130313144700"
            -1,
            // [153] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            -1,
            // [154] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[11]: "1.0.0+20130313144700"
            // [155] value1[0]: "1.0.0"
            14,
            // [156] value1[1]: "1.9.0"
            -9,
            // [157] value1[2]: "1.10.0"
            -10,
            // [158] value1[3]: "1.11.0"
            -11,
            // [159] value1[4]: "1.0.0-alpha"
            1,
            // [160] value1[5]: "1.0.0-alpha.1"
            1,
            // [161] value1[6]: "1.0.0-0.3.7"
            1,
            // [162] value1[7]: "1.0.0-x.7.z.92"
            1,
            // [163] value1[8]: "1.0.0-x.-7.z.92"
            1,
            // [164] value1[9]: "1.0.0-x-y-z.--"
            1,
            // [165] value1[10]: "1.0.0-alpha+001"
            1,
            // [166] value1[11]: "1.0.0+20130313144700"
            0,
            // [167] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            1,
            // [168] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[12]: "1.0.0-beta+exp.sha.5114f85"
            // [169] value1[0]: "1.0.0"
            -1,
            // [170] value1[1]: "1.9.0"
            -9,
            // [171] value1[2]: "1.10.0"
            -10,
            // [172] value1[3]: "1.11.0"
            -11,
            // [173] value1[4]: "1.0.0-alpha"
            1,
            // [174] value1[5]: "1.0.0-alpha.1"
            1,
            // [175] value1[6]: "1.0.0-0.3.7"
            1,
            // [176] value1[7]: "1.0.0-x.7.z.92"
            -22,
            // [177] value1[8]: "1.0.0-x.-7.z.92"
            -22,
            // [178] value1[9]: "1.0.0-x-y-z.--"
            -22,
            // [179] value1[10]: "1.0.0-alpha+001"
            1,
            // [180] value1[11]: "1.0.0+20130313144700"
            -1,
            // [181] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            0,
            // [182] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[13]: "1.0.0+21AF26D3----117B344092BD"
            // [183] value1[0]: "1.0.0"
            24,
            // [184] value1[1]: "1.9.0"
            -9,
            // [185] value1[2]: "1.10.0"
            -10,
            // [186] value1[3]: "1.11.0"
            -11,
            // [187] value1[4]: "1.0.0-alpha"
            1,
            // [188] value1[5]: "1.0.0-alpha.1"
            1,
            // [189] value1[6]: "1.0.0-0.3.7"
            1,
            // [190] value1[7]: "1.0.0-x.7.z.92"
            1,
            // [191] value1[8]: "1.0.0-x.-7.z.92"
            1,
            // [192] value1[9]: "1.0.0-x-y-z.--"
            1,
            // [193] value1[10]: "1.0.0-alpha+001"
            1,
            // [194] value1[11]: "1.0.0+20130313144700"
            1,
            // [195] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            1,
            // [196] value1[13]: "1.0.0+21AF26D3----117B344092BD"
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
            .<String>composeExpectedConverter(SemVer::of)
            .<String>composeArgConverter(0, SemVer::of),
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
            new Failure("ArgumentException",
                "`id` (PRERELEASE): Stable version cannot increment undefined pre-release"),
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
            new Failure("ArgumentException",
                "`id` (PRERELEASE): Stable version cannot increment undefined pre-release"),
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
            new Failure("ArgumentException",
                "`id` (PRERELEASE): Stable version cannot increment undefined pre-release"),
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
            new Failure("ArgumentException",
                "`id` (PRERELEASE): Stable version cannot increment undefined pre-release"),
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
            "1.0.0-alpha.1",
            // [25] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[5]: "1.0.0-alpha.1"
            // [26] id[0]: "MAJOR"
            "2.0.0",
            // [27] id[1]: "MINOR"
            "1.1.0",
            // [28] id[2]: "PATCH"
            "1.0.1",
            // [29] id[3]: "PRERELEASE"
            "1.0.0-alpha.2",
            // [30] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[6]: "1.0.0-0.3.7"
            // [31] id[0]: "MAJOR"
            "2.0.0",
            // [32] id[1]: "MINOR"
            "1.1.0",
            // [33] id[2]: "PATCH"
            "1.0.1",
            // [34] id[3]: "PRERELEASE"
            "1.0.0-0.3.8",
            // [35] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[7]: "1.0.0-x.7.z.92"
            // [36] id[0]: "MAJOR"
            "2.0.0",
            // [37] id[1]: "MINOR"
            "1.1.0",
            // [38] id[2]: "PATCH"
            "1.0.1",
            // [39] id[3]: "PRERELEASE"
            "1.0.0-x.7.z.93",
            // [40] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[8]: "1.0.0-x.-7.z.92"
            // [41] id[0]: "MAJOR"
            "2.0.0",
            // [42] id[1]: "MINOR"
            "1.1.0",
            // [43] id[2]: "PATCH"
            "1.0.1",
            // [44] id[3]: "PRERELEASE"
            "1.0.0-x.-7.z.93",
            // [45] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[9]: "1.0.0-x-y-z.--"
            // [46] id[0]: "MAJOR"
            "2.0.0",
            // [47] id[1]: "MINOR"
            "1.1.0",
            // [48] id[2]: "PATCH"
            "1.0.1",
            // [49] id[3]: "PRERELEASE"
            "1.0.0-x-y-z.--.1",
            // [50] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[10]: "1.0.0-alpha+001"
            // [51] id[0]: "MAJOR"
            "2.0.0",
            // [52] id[1]: "MINOR"
            "1.1.0",
            // [53] id[2]: "PATCH"
            "1.0.1",
            // [54] id[3]: "PRERELEASE"
            "1.0.0-alpha.1",
            // [55] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[11]: "1.0.0+20130313144700"
            // [56] id[0]: "MAJOR"
            "2.0.0",
            // [57] id[1]: "MINOR"
            "1.1.0",
            // [58] id[2]: "PATCH"
            "1.0.1",
            // [59] id[3]: "PRERELEASE"
            new Failure("ArgumentException",
                "`id` (PRERELEASE): Stable version cannot increment undefined pre-release"),
            // [60] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[12]: "1.0.0-beta+exp.sha.5114f85"
            // [61] id[0]: "MAJOR"
            "2.0.0",
            // [62] id[1]: "MINOR"
            "1.1.0",
            // [63] id[2]: "PATCH"
            "1.0.1",
            // [64] id[3]: "PRERELEASE"
            "1.0.0-beta.1",
            // [65] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID"),
            //
            // value[13]: "1.0.0+21AF26D3----117B344092BD"
            // [66] id[0]: "MAJOR"
            "2.0.0",
            // [67] id[1]: "MINOR"
            "1.1.0",
            // [68] id[2]: "PATCH"
            "1.0.1",
            // [69] id[3]: "PRERELEASE"
            new Failure("ArgumentException",
                "`id` (PRERELEASE): Stable version cannot increment undefined pre-release"),
            // [70] id[4]: "METADATA"
            new Failure("ArgumentException", "`id` (METADATA): INVALID")),
        // value
        VERSION_LITERALS__VALID,
        // id
        List.of(SemVer.Id.values()));
  }

  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> of_String() {
    return argumentsStream(
        cartesian()
            .<String>composeExpectedConverter(SemVer::of),
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
            // [6] value[5]: "1.0.0-alpha.1"
            "1.0.0-alpha.1",
            // [7] value[6]: "1.0.0-0.3.7"
            "1.0.0-0.3.7",
            // [8] value[7]: "1.0.0-x.7.z.92"
            "1.0.0-x.7.z.92",
            // [9] value[8]: "1.0.0-x.-7.z.92"
            "1.0.0-x.-7.z.92",
            // [10] value[9]: "1.0.0-x-y-z.--"
            "1.0.0-x-y-z.--",
            // [11] value[10]: "1.0.0-alpha+001"
            "1.0.0-alpha+001",
            // [12] value[11]: "1.0.0+20130313144700"
            "1.0.0+20130313144700",
            // [13] value[12]: "1.0.0-beta+exp.sha.5114f85"
            "1.0.0-beta+exp.sha.5114f85",
            // [14] value[13]: "1.0.0+21AF26D3----117B344092BD"
            "1.0.0+21AF26D3----117B344092BD",
            // [15] value[14]: "1.11.0.99"
            new Failure("ArgumentFormatException", "`value` (\"1.11.0.99\"): INVALID at index 6"),
            // [16] value[15]: "1.0.0.5-alpha"
            new Failure("ArgumentFormatException",
                "`value` (\"1.0.0.5-alpha\"): INVALID at index 5"),
            // [17] value[16]: "1.-11.0"
            new Failure("ArgumentFormatException", "`value` (\"1.-11.0\"): INVALID at index 2"),
            // [18] value[17]: "1.01.0"
            new Failure("ArgumentFormatException", "`value` (\"1.01.0\"): INVALID at index 3"),
            // [19] value[18]: "1.0.0-00.3.7"
            new Failure("ArgumentFormatException",
                "`value` (\"1.0.0-00.3.7\"): INVALID at index 8"),
            // [20] value[19]: "1.0.0_alpha"
            new Failure("ArgumentFormatException", "`value` (\"1.0.0_alpha\"): INVALID at index 5"),
            // [21] value[20]: "1.0.0-beta+exp.sha.5114f_85"
            new Failure("ArgumentFormatException",
                "`value` (\"1.0.0-beta+exp.sha.5114f_85\"): INVALID at index 24")),
        // value
        VERSION_LITERALS__MIXED);
  }

  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> of_int_int_int_String_String() {
    return argumentsStream(
        simple()
            .<String>composeExpectedConverter(SemVer::of),
        // expected
        asList(
            // [1] major[0]: 1; minor[0]: 0; patch[0]: 0; prerelease[0]: null; metadata[0]: null
            "1.0.0",
            // [2] major[1]: 1; minor[1]: 0; patch[1]: 0; prerelease[1]: "alpha.1"; metadata[1]: ""
            "1.0.0-alpha.1",
            // [3] major[2]: -1; minor[2]: 0; patch[2]: 0; prerelease[2]: "alpha.1"; metadata[2]: "abc"
            new Failure("ArgumentException", "`major` (-1): INVALID"),
            // [4] major[3]: 1; minor[3]: -1; patch[3]: 0; prerelease[3]: "alpha.1"; metadata[3]: "abc"
            new Failure("ArgumentException", "`minor` (-1): INVALID"),
            // [5] major[4]: 1; minor[4]: 0; patch[4]: -1; prerelease[4]: "alpha.1"; metadata[4]: "abc"
            new Failure("ArgumentException", "`patch` (-1): INVALID"),
            // [6] major[5]: 1; minor[5]: 0; patch[5]: 0; prerelease[5]: "alpha!.1"; metadata[5]: "abc"
            new Failure("ArgumentException", "`prerelease` (\"alpha!.1\"): INVALID"),
            // [7] major[6]: 1; minor[6]: 0; patch[6]: 0; prerelease[6]: "alpha.1"; metadata[6]: "+abc"
            new Failure("ArgumentException", "`metadata` (\"+abc\"): INVALID")),
        // major, minor, patch, prerelease, metadata
        // VALID
        asList(1, 0, 0, null, null),
        List.of(1, 0, 0, "alpha.1", ""),
        // INVALID
        List.of(-1, 0, 0, "alpha.1", "abc"),
        List.of(1, -1, 0, "alpha.1", "abc"),
        List.of(1, 0, -1, "alpha.1", "abc"),
        List.of(1, 0, 0, "alpha!.1", "abc"),
        List.of(1, 0, 0, "alpha.1", "+abc"));
  }

  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> precedence() {
    return argumentsStream(
        cartesian()
            .<String>composeArgConverter(0, SemVer::of)
            .<String>composeArgConverter(1, SemVer::of),
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
            // [6] value1[5]: "1.0.0-alpha.1"
            1,
            // [7] value1[6]: "1.0.0-0.3.7"
            1,
            // [8] value1[7]: "1.0.0-x.7.z.92"
            1,
            // [9] value1[8]: "1.0.0-x.-7.z.92"
            1,
            // [10] value1[9]: "1.0.0-x-y-z.--"
            1,
            // [11] value1[10]: "1.0.0-alpha+001"
            1,
            // [12] value1[11]: "1.0.0+20130313144700"
            0,
            // [13] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            1,
            // [14] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            0,
            //
            // value0[1]: "1.9.0"
            // [15] value1[0]: "1.0.0"
            9,
            // [16] value1[1]: "1.9.0"
            0,
            // [17] value1[2]: "1.10.0"
            -1,
            // [18] value1[3]: "1.11.0"
            -2,
            // [19] value1[4]: "1.0.0-alpha"
            9,
            // [20] value1[5]: "1.0.0-alpha.1"
            9,
            // [21] value1[6]: "1.0.0-0.3.7"
            9,
            // [22] value1[7]: "1.0.0-x.7.z.92"
            9,
            // [23] value1[8]: "1.0.0-x.-7.z.92"
            9,
            // [24] value1[9]: "1.0.0-x-y-z.--"
            9,
            // [25] value1[10]: "1.0.0-alpha+001"
            9,
            // [26] value1[11]: "1.0.0+20130313144700"
            9,
            // [27] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            9,
            // [28] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            9,
            //
            // value0[2]: "1.10.0"
            // [29] value1[0]: "1.0.0"
            10,
            // [30] value1[1]: "1.9.0"
            1,
            // [31] value1[2]: "1.10.0"
            0,
            // [32] value1[3]: "1.11.0"
            -1,
            // [33] value1[4]: "1.0.0-alpha"
            10,
            // [34] value1[5]: "1.0.0-alpha.1"
            10,
            // [35] value1[6]: "1.0.0-0.3.7"
            10,
            // [36] value1[7]: "1.0.0-x.7.z.92"
            10,
            // [37] value1[8]: "1.0.0-x.-7.z.92"
            10,
            // [38] value1[9]: "1.0.0-x-y-z.--"
            10,
            // [39] value1[10]: "1.0.0-alpha+001"
            10,
            // [40] value1[11]: "1.0.0+20130313144700"
            10,
            // [41] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            10,
            // [42] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            10,
            //
            // value0[3]: "1.11.0"
            // [43] value1[0]: "1.0.0"
            11,
            // [44] value1[1]: "1.9.0"
            2,
            // [45] value1[2]: "1.10.0"
            1,
            // [46] value1[3]: "1.11.0"
            0,
            // [47] value1[4]: "1.0.0-alpha"
            11,
            // [48] value1[5]: "1.0.0-alpha.1"
            11,
            // [49] value1[6]: "1.0.0-0.3.7"
            11,
            // [50] value1[7]: "1.0.0-x.7.z.92"
            11,
            // [51] value1[8]: "1.0.0-x.-7.z.92"
            11,
            // [52] value1[9]: "1.0.0-x-y-z.--"
            11,
            // [53] value1[10]: "1.0.0-alpha+001"
            11,
            // [54] value1[11]: "1.0.0+20130313144700"
            11,
            // [55] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            11,
            // [56] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            11,
            //
            // value0[4]: "1.0.0-alpha"
            // [57] value1[0]: "1.0.0"
            -1,
            // [58] value1[1]: "1.9.0"
            -9,
            // [59] value1[2]: "1.10.0"
            -10,
            // [60] value1[3]: "1.11.0"
            -11,
            // [61] value1[4]: "1.0.0-alpha"
            0,
            // [62] value1[5]: "1.0.0-alpha.1"
            -1,
            // [63] value1[6]: "1.0.0-0.3.7"
            1,
            // [64] value1[7]: "1.0.0-x.7.z.92"
            -23,
            // [65] value1[8]: "1.0.0-x.-7.z.92"
            -23,
            // [66] value1[9]: "1.0.0-x-y-z.--"
            -23,
            // [67] value1[10]: "1.0.0-alpha+001"
            0,
            // [68] value1[11]: "1.0.0+20130313144700"
            -1,
            // [69] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            -1,
            // [70] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[5]: "1.0.0-alpha.1"
            // [71] value1[0]: "1.0.0"
            -1,
            // [72] value1[1]: "1.9.0"
            -9,
            // [73] value1[2]: "1.10.0"
            -10,
            // [74] value1[3]: "1.11.0"
            -11,
            // [75] value1[4]: "1.0.0-alpha"
            -1,
            // [76] value1[5]: "1.0.0-alpha.1"
            0,
            // [77] value1[6]: "1.0.0-0.3.7"
            1,
            // [78] value1[7]: "1.0.0-x.7.z.92"
            -23,
            // [79] value1[8]: "1.0.0-x.-7.z.92"
            -23,
            // [80] value1[9]: "1.0.0-x-y-z.--"
            -23,
            // [81] value1[10]: "1.0.0-alpha+001"
            -1,
            // [82] value1[11]: "1.0.0+20130313144700"
            -1,
            // [83] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            -1,
            // [84] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[6]: "1.0.0-0.3.7"
            // [85] value1[0]: "1.0.0"
            -1,
            // [86] value1[1]: "1.9.0"
            -9,
            // [87] value1[2]: "1.10.0"
            -10,
            // [88] value1[3]: "1.11.0"
            -11,
            // [89] value1[4]: "1.0.0-alpha"
            -1,
            // [90] value1[5]: "1.0.0-alpha.1"
            -1,
            // [91] value1[6]: "1.0.0-0.3.7"
            0,
            // [92] value1[7]: "1.0.0-x.7.z.92"
            -1,
            // [93] value1[8]: "1.0.0-x.-7.z.92"
            -1,
            // [94] value1[9]: "1.0.0-x-y-z.--"
            -1,
            // [95] value1[10]: "1.0.0-alpha+001"
            -1,
            // [96] value1[11]: "1.0.0+20130313144700"
            -1,
            // [97] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            -1,
            // [98] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[7]: "1.0.0-x.7.z.92"
            // [99] value1[0]: "1.0.0"
            -1,
            // [100] value1[1]: "1.9.0"
            -9,
            // [101] value1[2]: "1.10.0"
            -10,
            // [102] value1[3]: "1.11.0"
            -11,
            // [103] value1[4]: "1.0.0-alpha"
            23,
            // [104] value1[5]: "1.0.0-alpha.1"
            23,
            // [105] value1[6]: "1.0.0-0.3.7"
            1,
            // [106] value1[7]: "1.0.0-x.7.z.92"
            0,
            // [107] value1[8]: "1.0.0-x.-7.z.92"
            -1,
            // [108] value1[9]: "1.0.0-x-y-z.--"
            -4,
            // [109] value1[10]: "1.0.0-alpha+001"
            23,
            // [110] value1[11]: "1.0.0+20130313144700"
            -1,
            // [111] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            22,
            // [112] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[8]: "1.0.0-x.-7.z.92"
            // [113] value1[0]: "1.0.0"
            -1,
            // [114] value1[1]: "1.9.0"
            -9,
            // [115] value1[2]: "1.10.0"
            -10,
            // [116] value1[3]: "1.11.0"
            -11,
            // [117] value1[4]: "1.0.0-alpha"
            23,
            // [118] value1[5]: "1.0.0-alpha.1"
            23,
            // [119] value1[6]: "1.0.0-0.3.7"
            1,
            // [120] value1[7]: "1.0.0-x.7.z.92"
            1,
            // [121] value1[8]: "1.0.0-x.-7.z.92"
            0,
            // [122] value1[9]: "1.0.0-x-y-z.--"
            -4,
            // [123] value1[10]: "1.0.0-alpha+001"
            23,
            // [124] value1[11]: "1.0.0+20130313144700"
            -1,
            // [125] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            22,
            // [126] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[9]: "1.0.0-x-y-z.--"
            // [127] value1[0]: "1.0.0"
            -1,
            // [128] value1[1]: "1.9.0"
            -9,
            // [129] value1[2]: "1.10.0"
            -10,
            // [130] value1[3]: "1.11.0"
            -11,
            // [131] value1[4]: "1.0.0-alpha"
            23,
            // [132] value1[5]: "1.0.0-alpha.1"
            23,
            // [133] value1[6]: "1.0.0-0.3.7"
            1,
            // [134] value1[7]: "1.0.0-x.7.z.92"
            4,
            // [135] value1[8]: "1.0.0-x.-7.z.92"
            4,
            // [136] value1[9]: "1.0.0-x-y-z.--"
            0,
            // [137] value1[10]: "1.0.0-alpha+001"
            23,
            // [138] value1[11]: "1.0.0+20130313144700"
            -1,
            // [139] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            22,
            // [140] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[10]: "1.0.0-alpha+001"
            // [141] value1[0]: "1.0.0"
            -1,
            // [142] value1[1]: "1.9.0"
            -9,
            // [143] value1[2]: "1.10.0"
            -10,
            // [144] value1[3]: "1.11.0"
            -11,
            // [145] value1[4]: "1.0.0-alpha"
            0,
            // [146] value1[5]: "1.0.0-alpha.1"
            -1,
            // [147] value1[6]: "1.0.0-0.3.7"
            1,
            // [148] value1[7]: "1.0.0-x.7.z.92"
            -23,
            // [149] value1[8]: "1.0.0-x.-7.z.92"
            -23,
            // [150] value1[9]: "1.0.0-x-y-z.--"
            -23,
            // [151] value1[10]: "1.0.0-alpha+001"
            0,
            // [152] value1[11]: "1.0.0+20130313144700"
            -1,
            // [153] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            -1,
            // [154] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[11]: "1.0.0+20130313144700"
            // [155] value1[0]: "1.0.0"
            0,
            // [156] value1[1]: "1.9.0"
            -9,
            // [157] value1[2]: "1.10.0"
            -10,
            // [158] value1[3]: "1.11.0"
            -11,
            // [159] value1[4]: "1.0.0-alpha"
            1,
            // [160] value1[5]: "1.0.0-alpha.1"
            1,
            // [161] value1[6]: "1.0.0-0.3.7"
            1,
            // [162] value1[7]: "1.0.0-x.7.z.92"
            1,
            // [163] value1[8]: "1.0.0-x.-7.z.92"
            1,
            // [164] value1[9]: "1.0.0-x-y-z.--"
            1,
            // [165] value1[10]: "1.0.0-alpha+001"
            1,
            // [166] value1[11]: "1.0.0+20130313144700"
            0,
            // [167] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            1,
            // [168] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            0,
            //
            // value0[12]: "1.0.0-beta+exp.sha.5114f85"
            // [169] value1[0]: "1.0.0"
            -1,
            // [170] value1[1]: "1.9.0"
            -9,
            // [171] value1[2]: "1.10.0"
            -10,
            // [172] value1[3]: "1.11.0"
            -11,
            // [173] value1[4]: "1.0.0-alpha"
            1,
            // [174] value1[5]: "1.0.0-alpha.1"
            1,
            // [175] value1[6]: "1.0.0-0.3.7"
            1,
            // [176] value1[7]: "1.0.0-x.7.z.92"
            -22,
            // [177] value1[8]: "1.0.0-x.-7.z.92"
            -22,
            // [178] value1[9]: "1.0.0-x-y-z.--"
            -22,
            // [179] value1[10]: "1.0.0-alpha+001"
            1,
            // [180] value1[11]: "1.0.0+20130313144700"
            -1,
            // [181] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            0,
            // [182] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            -1,
            //
            // value0[13]: "1.0.0+21AF26D3----117B344092BD"
            // [183] value1[0]: "1.0.0"
            0,
            // [184] value1[1]: "1.9.0"
            -9,
            // [185] value1[2]: "1.10.0"
            -10,
            // [186] value1[3]: "1.11.0"
            -11,
            // [187] value1[4]: "1.0.0-alpha"
            1,
            // [188] value1[5]: "1.0.0-alpha.1"
            1,
            // [189] value1[6]: "1.0.0-0.3.7"
            1,
            // [190] value1[7]: "1.0.0-x.7.z.92"
            1,
            // [191] value1[8]: "1.0.0-x.-7.z.92"
            1,
            // [192] value1[9]: "1.0.0-x-y-z.--"
            1,
            // [193] value1[10]: "1.0.0-alpha+001"
            1,
            // [194] value1[11]: "1.0.0+20130313144700"
            0,
            // [195] value1[12]: "1.0.0-beta+exp.sha.5114f85"
            1,
            // [196] value1[13]: "1.0.0+21AF26D3----117B344092BD"
            0),
        // value0
        VERSION_LITERALS__VALID,
        // value1
        VERSION_LITERALS__VALID);
  }

  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> toString_() {
    return argumentsStream(
        cartesian()
            .<String>composeArgConverter(0, SemVer::of),
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
            // [6] value[5]: "1.0.0-alpha.1"
            "1.0.0-alpha.1",
            // [7] value[6]: "1.0.0-0.3.7"
            "1.0.0-0.3.7",
            // [8] value[7]: "1.0.0-x.7.z.92"
            "1.0.0-x.7.z.92",
            // [9] value[8]: "1.0.0-x.-7.z.92"
            "1.0.0-x.-7.z.92",
            // [10] value[9]: "1.0.0-x-y-z.--"
            "1.0.0-x-y-z.--",
            // [11] value[10]: "1.0.0-alpha+001"
            "1.0.0-alpha+001",
            // [12] value[11]: "1.0.0+20130313144700"
            "1.0.0+20130313144700",
            // [13] value[12]: "1.0.0-beta+exp.sha.5114f85"
            "1.0.0-beta+exp.sha.5114f85",
            // [14] value[13]: "1.0.0+21AF26D3----117B344092BD"
            "1.0.0+21AF26D3----117B344092BD"),
        // value
        VERSION_LITERALS__VALID);
  }

  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> with() {
    return argumentsStream(
        cartesian()
            .<String>composeExpectedConverter(SemVer::of)
            .<String>composeArgConverter(0, SemVer::of),
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
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [10] value[1]: "rc"
            "1.0.0+rc",
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
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [20] value[1]: "rc"
            "1.9.0+rc",
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
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [30] value[1]: "rc"
            "1.10.0+rc",
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
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [40] value[1]: "rc"
            "1.11.0+rc",
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
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [50] value[1]: "rc"
            "1.0.0-alpha+rc",
            //
            // ver[5]: "1.0.0-alpha.1"
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
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [60] value[1]: "rc"
            "1.0.0-alpha.1+rc",
            //
            // ver[6]: "1.0.0-0.3.7"
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
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [70] value[1]: "rc"
            "1.0.0-0.3.7+rc",
            //
            // ver[7]: "1.0.0-x.7.z.92"
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
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [80] value[1]: "rc"
            "1.0.0-x.7.z.92+rc",
            //
            // ver[8]: "1.0.0-x.-7.z.92"
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
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [90] value[1]: "rc"
            "1.0.0-x.-7.z.92+rc",
            //
            // ver[9]: "1.0.0-x-y-z.--"
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
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [100] value[1]: "rc"
            "1.0.0-x-y-z.--+rc",
            //
            // ver[10]: "1.0.0-alpha+001"
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
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [110] value[1]: "rc"
            "1.0.0-alpha+rc",
            //
            // ver[11]: "1.0.0+20130313144700"
            // -- id[0]: "MAJOR"
            // [111] value[0]: 8
            "8.0.0",
            // [112] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [113] value[0]: 8
            "1.8.0",
            // [114] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [115] value[0]: 8
            "1.0.8",
            // [116] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [117] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [118] value[1]: "rc"
            "1.0.0-rc",
            // -- id[4]: "METADATA"
            // [119] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [120] value[1]: "rc"
            "1.0.0+rc",
            //
            // ver[12]: "1.0.0-beta+exp.sha.5114f85"
            // -- id[0]: "MAJOR"
            // [121] value[0]: 8
            "8.0.0",
            // [122] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [123] value[0]: 8
            "1.8.0",
            // [124] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [125] value[0]: 8
            "1.0.8",
            // [126] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [127] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [128] value[1]: "rc"
            "1.0.0-rc",
            // -- id[4]: "METADATA"
            // [129] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [130] value[1]: "rc"
            "1.0.0-beta+rc",
            //
            // ver[13]: "1.0.0+21AF26D3----117B344092BD"
            // -- id[0]: "MAJOR"
            // [131] value[0]: 8
            "8.0.0",
            // [132] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[1]: "MINOR"
            // [133] value[0]: 8
            "1.8.0",
            // [134] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[2]: "PATCH"
            // [135] value[0]: 8
            "1.0.8",
            // [136] value[1]: "rc"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // -- id[3]: "PRERELEASE"
            // [137] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [138] value[1]: "rc"
            "1.0.0-rc",
            // -- id[4]: "METADATA"
            // [139] value[0]: 8
            new Failure("ClassCastException",
                "class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')"),
            // [140] value[1]: "rc"
            "1.0.0+rc"),
        // ver
        VERSION_LITERALS__VALID,
        // id
        List.of(SemVer.Id.values()),
        // value
        List.of(8, "rc"));
  }

  @ParameterizedTest
  @MethodSource
  void compareTo(Expected<Integer> expected, SemVer value0, SemVer value1) {
    assertParameterizedOf(
        () -> value0.compareTo(value1),
        expected,
        () -> new ExpectedGeneration(value0, value1));
  }

  @ParameterizedTest
  @MethodSource
  void next(Expected<SemVer> expected, SemVer value, SemVer.Id id) {
    assertParameterizedOf(
        () -> value.next(id),
        expected,
        () -> new ExpectedGeneration(value, id));
  }

  @ParameterizedTest
  @MethodSource
  void of_String(Expected<SemVer> expected, String value) {
    assertParameterizedOf(
        () -> SemVer.of(value),
        expected,
        () -> new ExpectedGeneration(value));
  }

  @ParameterizedTest
  @MethodSource
  void of_int_int_int_String_String(Expected<SemVer> expected, int major, int minor, int patch,
      @Nullable String prerelease, @Nullable String metadata) {
    assertParameterizedOf(
        () -> SemVer.of(major, minor, patch, prerelease, metadata),
        expected,
        () -> new ExpectedGeneration(major, minor, patch, prerelease, metadata));
  }

  @ParameterizedTest
  @MethodSource
  void precedence(Expected<Integer> expected, SemVer value0, SemVer value1) {
    assertParameterizedOf(
        () -> value0.precedence(value1),
        expected,
        () -> new ExpectedGeneration(value0, value1));
  }

  @ParameterizedTest
  @MethodSource
  void toString_(Expected<String> expected, SemVer value) {
    assertParameterizedOf(
        () -> value.toString(),
        expected,
        () -> new ExpectedGeneration(value));
  }

  @ParameterizedTest
  @MethodSource
  @SuppressWarnings("rawtypes")
  void with(Expected<SemVer> expected, SemVer ver, Id id, Comparable value) {
    assertParameterizedOf(
        () -> ver.with(id, value),
        expected,
        () -> new ExpectedGeneration(ver, id, value));
  }
}