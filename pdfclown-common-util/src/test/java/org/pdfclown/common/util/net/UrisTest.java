/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UrisTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.net;

import static java.util.Arrays.asList;
import static java.util.Map.entry;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.build.test.assertion.Assertions.cartesianArgumentsStream;
import static org.pdfclown.common.util.Objects.objToLiteralString;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
public class UrisTest extends BaseTest {
  private static Stream<Arguments> _relativeUri() {
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
    return cartesianArgumentsStream(
        // expected
        java.util.Arrays.asList(
            // from[0]: 'my/sub/same.html'
            // -- to[0]: 'my/sub/same.html'
            URI.create(""),
            // -- to[1]: 'my/another/sub/to.html'
            URI.create("../another/sub/to.html"),
            // -- to[2]: 'another/my/sub/to.html'
            URI.create("../../another/my/sub/to.html"),
            // -- to[3]: '/my/sub/another/to.html'
            URI.create("/my/sub/another/to.html"),
            // -- to[4]: '/my/another/to.html'
            URI.create("/my/another/to.html"),
            // -- to[5]: '/sub/to.html'
            URI.create("/sub/to.html"),
            // -- to[6]: 'file:/c:/absolute/local/uri.html'
            URI.create("file:/c:/absolute/local/uri.html"),
            // -- to[7]: 'file://host/absolute/uri.html'
            URI.create("file://host/absolute/uri.html"),
            // -- to[8]: 'https://example.io/my/sub/to.html'
            URI.create("https://example.io/my/sub/to.html"),
            // -- to[9]: 'https://example.io/another/deeper/sub/to.html'
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[1]: 'my/another/sub/from.html'
            // -- to[0]: 'my/sub/same.html'
            URI.create("../../sub/same.html"),
            // -- to[1]: 'my/another/sub/to.html'
            URI.create("to.html"),
            // -- to[2]: 'another/my/sub/to.html'
            URI.create("../../../another/my/sub/to.html"),
            // -- to[3]: '/my/sub/another/to.html'
            URI.create("/my/sub/another/to.html"),
            // -- to[4]: '/my/another/to.html'
            URI.create("/my/another/to.html"),
            // -- to[5]: '/sub/to.html'
            URI.create("/sub/to.html"),
            // -- to[6]: 'file:/c:/absolute/local/uri.html'
            URI.create("file:/c:/absolute/local/uri.html"),
            // -- to[7]: 'file://host/absolute/uri.html'
            URI.create("file://host/absolute/uri.html"),
            // -- to[8]: 'https://example.io/my/sub/to.html'
            URI.create("https://example.io/my/sub/to.html"),
            // -- to[9]: 'https://example.io/another/deeper/sub/to.html'
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[2]: 'another/my/sub/from.html'
            // -- to[0]: 'my/sub/same.html'
            URI.create("../../../my/sub/same.html"),
            // -- to[1]: 'my/another/sub/to.html'
            URI.create("../../../my/another/sub/to.html"),
            // -- to[2]: 'another/my/sub/to.html'
            URI.create("to.html"),
            // -- to[3]: '/my/sub/another/to.html'
            URI.create("/my/sub/another/to.html"),
            // -- to[4]: '/my/another/to.html'
            URI.create("/my/another/to.html"),
            // -- to[5]: '/sub/to.html'
            URI.create("/sub/to.html"),
            // -- to[6]: 'file:/c:/absolute/local/uri.html'
            URI.create("file:/c:/absolute/local/uri.html"),
            // -- to[7]: 'file://host/absolute/uri.html'
            URI.create("file://host/absolute/uri.html"),
            // -- to[8]: 'https://example.io/my/sub/to.html'
            URI.create("https://example.io/my/sub/to.html"),
            // -- to[9]: 'https://example.io/another/deeper/sub/to.html'
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[3]: '/my/sub/another/from.html'
            // -- to[0]: 'my/sub/same.html'
            URI.create("my/sub/same.html"),
            // -- to[1]: 'my/another/sub/to.html'
            URI.create("my/another/sub/to.html"),
            // -- to[2]: 'another/my/sub/to.html'
            URI.create("another/my/sub/to.html"),
            // -- to[3]: '/my/sub/another/to.html'
            URI.create("to.html"),
            // -- to[4]: '/my/another/to.html'
            URI.create("../../another/to.html"),
            // -- to[5]: '/sub/to.html'
            URI.create("../../../sub/to.html"),
            // -- to[6]: 'file:/c:/absolute/local/uri.html'
            URI.create("file:/c:/absolute/local/uri.html"),
            // -- to[7]: 'file://host/absolute/uri.html'
            URI.create("file://host/absolute/uri.html"),
            // -- to[8]: 'https://example.io/my/sub/to.html'
            URI.create("https://example.io/my/sub/to.html"),
            // -- to[9]: 'https://example.io/another/deeper/sub/to.html'
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[4]: '/my/another/from.html'
            // -- to[0]: 'my/sub/same.html'
            URI.create("my/sub/same.html"),
            // -- to[1]: 'my/another/sub/to.html'
            URI.create("my/another/sub/to.html"),
            // -- to[2]: 'another/my/sub/to.html'
            URI.create("another/my/sub/to.html"),
            // -- to[3]: '/my/sub/another/to.html'
            URI.create("../sub/another/to.html"),
            // -- to[4]: '/my/another/to.html'
            URI.create("to.html"),
            // -- to[5]: '/sub/to.html'
            URI.create("../../sub/to.html"),
            // -- to[6]: 'file:/c:/absolute/local/uri.html'
            URI.create("file:/c:/absolute/local/uri.html"),
            // -- to[7]: 'file://host/absolute/uri.html'
            URI.create("file://host/absolute/uri.html"),
            // -- to[8]: 'https://example.io/my/sub/to.html'
            URI.create("https://example.io/my/sub/to.html"),
            // -- to[9]: 'https://example.io/another/deeper/sub/to.html'
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[5]: '/sub/from.html'
            // -- to[0]: 'my/sub/same.html'
            URI.create("my/sub/same.html"),
            // -- to[1]: 'my/another/sub/to.html'
            URI.create("my/another/sub/to.html"),
            // -- to[2]: 'another/my/sub/to.html'
            URI.create("another/my/sub/to.html"),
            // -- to[3]: '/my/sub/another/to.html'
            URI.create("../my/sub/another/to.html"),
            // -- to[4]: '/my/another/to.html'
            URI.create("../my/another/to.html"),
            // -- to[5]: '/sub/to.html'
            URI.create("to.html"),
            // -- to[6]: 'file:/c:/absolute/local/uri.html'
            URI.create("file:/c:/absolute/local/uri.html"),
            // -- to[7]: 'file://host/absolute/uri.html'
            URI.create("file://host/absolute/uri.html"),
            // -- to[8]: 'https://example.io/my/sub/to.html'
            URI.create("https://example.io/my/sub/to.html"),
            // -- to[9]: 'https://example.io/another/deeper/sub/to.html'
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[6]: 'file:///c:/absolute/local/uri.html'
            // -- to[0]: 'my/sub/same.html'
            URI.create("my/sub/same.html"),
            // -- to[1]: 'my/another/sub/to.html'
            URI.create("my/another/sub/to.html"),
            // -- to[2]: 'another/my/sub/to.html'
            URI.create("another/my/sub/to.html"),
            // -- to[3]: '/my/sub/another/to.html'
            URI.create("/my/sub/another/to.html"),
            // -- to[4]: '/my/another/to.html'
            URI.create("/my/another/to.html"),
            // -- to[5]: '/sub/to.html'
            URI.create("/sub/to.html"),
            // -- to[6]: 'file:/c:/absolute/local/uri.html'
            URI.create(""),
            // -- to[7]: 'file://host/absolute/uri.html'
            URI.create("file://host/absolute/uri.html"),
            // -- to[8]: 'https://example.io/my/sub/to.html'
            URI.create("https://example.io/my/sub/to.html"),
            // -- to[9]: 'https://example.io/another/deeper/sub/to.html'
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[7]: 'file://host/absolute/uri.html'
            // -- to[0]: 'my/sub/same.html'
            URI.create("my/sub/same.html"),
            // -- to[1]: 'my/another/sub/to.html'
            URI.create("my/another/sub/to.html"),
            // -- to[2]: 'another/my/sub/to.html'
            URI.create("another/my/sub/to.html"),
            // -- to[3]: '/my/sub/another/to.html'
            URI.create("/my/sub/another/to.html"),
            // -- to[4]: '/my/another/to.html'
            URI.create("/my/another/to.html"),
            // -- to[5]: '/sub/to.html'
            URI.create("/sub/to.html"),
            // -- to[6]: 'file:/c:/absolute/local/uri.html'
            URI.create("file:/c:/absolute/local/uri.html"),
            // -- to[7]: 'file://host/absolute/uri.html'
            URI.create(""),
            // -- to[8]: 'https://example.io/my/sub/to.html'
            URI.create("https://example.io/my/sub/to.html"),
            // -- to[9]: 'https://example.io/another/deeper/sub/to.html'
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[8]: 'https://example.io/my/sub/from.html'
            // -- to[0]: 'my/sub/same.html'
            URI.create("my/sub/same.html"),
            // -- to[1]: 'my/another/sub/to.html'
            URI.create("my/another/sub/to.html"),
            // -- to[2]: 'another/my/sub/to.html'
            URI.create("another/my/sub/to.html"),
            // -- to[3]: '/my/sub/another/to.html'
            URI.create("/my/sub/another/to.html"),
            // -- to[4]: '/my/another/to.html'
            URI.create("/my/another/to.html"),
            // -- to[5]: '/sub/to.html'
            URI.create("/sub/to.html"),
            // -- to[6]: 'file:/c:/absolute/local/uri.html'
            URI.create("file:/c:/absolute/local/uri.html"),
            // -- to[7]: 'file://host/absolute/uri.html'
            URI.create("file://host/absolute/uri.html"),
            // -- to[8]: 'https://example.io/my/sub/to.html'
            URI.create("to.html"),
            // -- to[9]: 'https://example.io/another/deeper/sub/to.html'
            URI.create("../../another/deeper/sub/to.html"),
            // from[9]: 'https://example.io/another/deeper/sub/from.html'
            // -- to[0]: 'my/sub/same.html'
            URI.create("my/sub/same.html"),
            // -- to[1]: 'my/another/sub/to.html'
            URI.create("my/another/sub/to.html"),
            // -- to[2]: 'another/my/sub/to.html'
            URI.create("another/my/sub/to.html"),
            // -- to[3]: '/my/sub/another/to.html'
            URI.create("/my/sub/another/to.html"),
            // -- to[4]: '/my/another/to.html'
            URI.create("/my/another/to.html"),
            // -- to[5]: '/sub/to.html'
            URI.create("/sub/to.html"),
            // -- to[6]: 'file:/c:/absolute/local/uri.html'
            URI.create("file:/c:/absolute/local/uri.html"),
            // -- to[7]: 'file://host/absolute/uri.html'
            URI.create("file://host/absolute/uri.html"),
            // -- to[8]: 'https://example.io/my/sub/to.html'
            URI.create("../../../my/sub/to.html"),
            // -- to[9]: 'https://example.io/another/deeper/sub/to.html'
            URI.create("to.html")),
        from,
        to);
  }

  @ParameterizedTest
  @MethodSource
  public void _relativeUri(Object expected, URI from, URI to) {
    assertParameterizedOf(
        () -> Uris.relativeUri(from, to),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("from", from),
            entry("to", to)),
            $ -> "URI.create(" + objToLiteralString($) + ")"));
  }
}
