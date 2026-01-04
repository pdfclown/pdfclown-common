/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UrisTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.net;

import static java.util.Arrays.asList;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.net.URI;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class UrisTest extends BaseTest {
  @SuppressWarnings("DataFlowIssue")
  static Stream<Arguments> relativize() {
    var from = asList(
        URI.create("my/sub/same.html"),
        URI.create("my/another/sub/from.html"),
        URI.create("another/my/sub/from.html"),
        URI.create("/my/sub/another/from.html"),
        URI.create("/my/another/from.html"),
        URI.create("/sub/from.html"),
        URI.create("file:///c:/absolute/local/uri.html"),
        URI.create("file://host/absolute/uri.html"),
        URI.create("https://example.io/my/sub/from.html"),
        URI.create("https://example.io/another/deeper/sub/from.html"));
    var to = from.stream()
        .map($ -> URI.create(
            ($.getScheme() != null ? $.getScheme() + ":" : EMPTY)
                + ($.getAuthority() != null ? "//" + $.getAuthority() : EMPTY)
                + $.getPath().replace("from", "to")))
        .collect(Collectors.toCollection(ArrayList::new));
    return argumentsStream(
        cartesian()
            .composeExpectedConverter(URI::create),
        // expected
        asList(
            // from[0]: "my/sub/same.html"
            // [1] to[0]: "my/sub/same.html"
            "",
            // [2] to[1]: "my/another/sub/to.html"
            "../another/sub/to.html",
            // [3] to[2]: "another/my/sub/to.html"
            "../../another/my/sub/to.html",
            // [4] to[3]: "/my/sub/another/to.html"
            "/my/sub/another/to.html",
            // [5] to[4]: "/my/another/to.html"
            "/my/another/to.html",
            // [6] to[5]: "/sub/to.html"
            "/sub/to.html",
            // [7] to[6]: "file:/c:/absolute/local/uri.html"
            "file:/c:/absolute/local/uri.html",
            // [8] to[7]: "file://host/absolute/uri.html"
            "file://host/absolute/uri.html",
            // [9] to[8]: "https://example.io/my/sub/to.html"
            "https://example.io/my/sub/to.html",
            // [10] to[9]: "https://example.io/another/deeper/sub/to.html"
            "https://example.io/another/deeper/sub/to.html",
            //
            // from[1]: "my/another/sub/from.html"
            // [11] to[0]: "my/sub/same.html"
            "../../sub/same.html",
            // [12] to[1]: "my/another/sub/to.html"
            "to.html",
            // [13] to[2]: "another/my/sub/to.html"
            "../../../another/my/sub/to.html",
            // [14] to[3]: "/my/sub/another/to.html"
            "/my/sub/another/to.html",
            // [15] to[4]: "/my/another/to.html"
            "/my/another/to.html",
            // [16] to[5]: "/sub/to.html"
            "/sub/to.html",
            // [17] to[6]: "file:/c:/absolute/local/uri.html"
            "file:/c:/absolute/local/uri.html",
            // [18] to[7]: "file://host/absolute/uri.html"
            "file://host/absolute/uri.html",
            // [19] to[8]: "https://example.io/my/sub/to.html"
            "https://example.io/my/sub/to.html",
            // [20] to[9]: "https://example.io/another/deeper/sub/to.html"
            "https://example.io/another/deeper/sub/to.html",
            //
            // from[2]: "another/my/sub/from.html"
            // [21] to[0]: "my/sub/same.html"
            "../../../my/sub/same.html",
            // [22] to[1]: "my/another/sub/to.html"
            "../../../my/another/sub/to.html",
            // [23] to[2]: "another/my/sub/to.html"
            "to.html",
            // [24] to[3]: "/my/sub/another/to.html"
            "/my/sub/another/to.html",
            // [25] to[4]: "/my/another/to.html"
            "/my/another/to.html",
            // [26] to[5]: "/sub/to.html"
            "/sub/to.html",
            // [27] to[6]: "file:/c:/absolute/local/uri.html"
            "file:/c:/absolute/local/uri.html",
            // [28] to[7]: "file://host/absolute/uri.html"
            "file://host/absolute/uri.html",
            // [29] to[8]: "https://example.io/my/sub/to.html"
            "https://example.io/my/sub/to.html",
            // [30] to[9]: "https://example.io/another/deeper/sub/to.html"
            "https://example.io/another/deeper/sub/to.html",
            //
            // from[3]: "/my/sub/another/from.html"
            // [31] to[0]: "my/sub/same.html"
            "my/sub/same.html",
            // [32] to[1]: "my/another/sub/to.html"
            "my/another/sub/to.html",
            // [33] to[2]: "another/my/sub/to.html"
            "another/my/sub/to.html",
            // [34] to[3]: "/my/sub/another/to.html"
            "to.html",
            // [35] to[4]: "/my/another/to.html"
            "../../another/to.html",
            // [36] to[5]: "/sub/to.html"
            "../../../sub/to.html",
            // [37] to[6]: "file:/c:/absolute/local/uri.html"
            "file:/c:/absolute/local/uri.html",
            // [38] to[7]: "file://host/absolute/uri.html"
            "file://host/absolute/uri.html",
            // [39] to[8]: "https://example.io/my/sub/to.html"
            "https://example.io/my/sub/to.html",
            // [40] to[9]: "https://example.io/another/deeper/sub/to.html"
            "https://example.io/another/deeper/sub/to.html",
            //
            // from[4]: "/my/another/from.html"
            // [41] to[0]: "my/sub/same.html"
            "my/sub/same.html",
            // [42] to[1]: "my/another/sub/to.html"
            "my/another/sub/to.html",
            // [43] to[2]: "another/my/sub/to.html"
            "another/my/sub/to.html",
            // [44] to[3]: "/my/sub/another/to.html"
            "../sub/another/to.html",
            // [45] to[4]: "/my/another/to.html"
            "to.html",
            // [46] to[5]: "/sub/to.html"
            "../../sub/to.html",
            // [47] to[6]: "file:/c:/absolute/local/uri.html"
            "file:/c:/absolute/local/uri.html",
            // [48] to[7]: "file://host/absolute/uri.html"
            "file://host/absolute/uri.html",
            // [49] to[8]: "https://example.io/my/sub/to.html"
            "https://example.io/my/sub/to.html",
            // [50] to[9]: "https://example.io/another/deeper/sub/to.html"
            "https://example.io/another/deeper/sub/to.html",
            //
            // from[5]: "/sub/from.html"
            // [51] to[0]: "my/sub/same.html"
            "my/sub/same.html",
            // [52] to[1]: "my/another/sub/to.html"
            "my/another/sub/to.html",
            // [53] to[2]: "another/my/sub/to.html"
            "another/my/sub/to.html",
            // [54] to[3]: "/my/sub/another/to.html"
            "../my/sub/another/to.html",
            // [55] to[4]: "/my/another/to.html"
            "../my/another/to.html",
            // [56] to[5]: "/sub/to.html"
            "to.html",
            // [57] to[6]: "file:/c:/absolute/local/uri.html"
            "file:/c:/absolute/local/uri.html",
            // [58] to[7]: "file://host/absolute/uri.html"
            "file://host/absolute/uri.html",
            // [59] to[8]: "https://example.io/my/sub/to.html"
            "https://example.io/my/sub/to.html",
            // [60] to[9]: "https://example.io/another/deeper/sub/to.html"
            "https://example.io/another/deeper/sub/to.html",
            //
            // from[6]: "file:///c:/absolute/local/uri.html"
            // [61] to[0]: "my/sub/same.html"
            "my/sub/same.html",
            // [62] to[1]: "my/another/sub/to.html"
            "my/another/sub/to.html",
            // [63] to[2]: "another/my/sub/to.html"
            "another/my/sub/to.html",
            // [64] to[3]: "/my/sub/another/to.html"
            "/my/sub/another/to.html",
            // [65] to[4]: "/my/another/to.html"
            "/my/another/to.html",
            // [66] to[5]: "/sub/to.html"
            "/sub/to.html",
            // [67] to[6]: "file:/c:/absolute/local/uri.html"
            "",
            // [68] to[7]: "file://host/absolute/uri.html"
            "file://host/absolute/uri.html",
            // [69] to[8]: "https://example.io/my/sub/to.html"
            "https://example.io/my/sub/to.html",
            // [70] to[9]: "https://example.io/another/deeper/sub/to.html"
            "https://example.io/another/deeper/sub/to.html",
            //
            // from[7]: "file://host/absolute/uri.html"
            // [71] to[0]: "my/sub/same.html"
            "my/sub/same.html",
            // [72] to[1]: "my/another/sub/to.html"
            "my/another/sub/to.html",
            // [73] to[2]: "another/my/sub/to.html"
            "another/my/sub/to.html",
            // [74] to[3]: "/my/sub/another/to.html"
            "/my/sub/another/to.html",
            // [75] to[4]: "/my/another/to.html"
            "/my/another/to.html",
            // [76] to[5]: "/sub/to.html"
            "/sub/to.html",
            // [77] to[6]: "file:/c:/absolute/local/uri.html"
            "file:/c:/absolute/local/uri.html",
            // [78] to[7]: "file://host/absolute/uri.html"
            "",
            // [79] to[8]: "https://example.io/my/sub/to.html"
            "https://example.io/my/sub/to.html",
            // [80] to[9]: "https://example.io/another/deeper/sub/to.html"
            "https://example.io/another/deeper/sub/to.html",
            //
            // from[8]: "https://example.io/my/sub/from.html"
            // [81] to[0]: "my/sub/same.html"
            "my/sub/same.html",
            // [82] to[1]: "my/another/sub/to.html"
            "my/another/sub/to.html",
            // [83] to[2]: "another/my/sub/to.html"
            "another/my/sub/to.html",
            // [84] to[3]: "/my/sub/another/to.html"
            "/my/sub/another/to.html",
            // [85] to[4]: "/my/another/to.html"
            "/my/another/to.html",
            // [86] to[5]: "/sub/to.html"
            "/sub/to.html",
            // [87] to[6]: "file:/c:/absolute/local/uri.html"
            "file:/c:/absolute/local/uri.html",
            // [88] to[7]: "file://host/absolute/uri.html"
            "file://host/absolute/uri.html",
            // [89] to[8]: "https://example.io/my/sub/to.html"
            "to.html",
            // [90] to[9]: "https://example.io/another/deeper/sub/to.html"
            "../../another/deeper/sub/to.html",
            //
            // from[9]: "https://example.io/another/deeper/sub/from.html"
            // [91] to[0]: "my/sub/same.html"
            "my/sub/same.html",
            // [92] to[1]: "my/another/sub/to.html"
            "my/another/sub/to.html",
            // [93] to[2]: "another/my/sub/to.html"
            "another/my/sub/to.html",
            // [94] to[3]: "/my/sub/another/to.html"
            "/my/sub/another/to.html",
            // [95] to[4]: "/my/another/to.html"
            "/my/another/to.html",
            // [96] to[5]: "/sub/to.html"
            "/sub/to.html",
            // [97] to[6]: "file:/c:/absolute/local/uri.html"
            "file:/c:/absolute/local/uri.html",
            // [98] to[7]: "file://host/absolute/uri.html"
            "file://host/absolute/uri.html",
            // [99] to[8]: "https://example.io/my/sub/to.html"
            "../../../my/sub/to.html",
            // [100] to[9]: "https://example.io/another/deeper/sub/to.html"
            "to.html"),
        // from
        from,
        // to
        to);
  }

  @ParameterizedTest
  @MethodSource
  void relativize(Expected<URI> expected, URI from, URI to) {
    assertParameterizedOf(
        () -> Uris.relativize(from, to),
        expected,
        () -> new ExpectedGeneration(from, to)
            .setMaxArgCommentLength(50));
  }
}
