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
import static org.pdfclown.common.build.test.Tests.argumentsStream;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.net.URI;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
    return argumentsStream(
        // expected
        asList(
            // from[0]
            URI.create(""),
            URI.create("../another/sub/to.html"),
            URI.create("../../another/my/sub/to.html"),
            URI.create("/my/sub/another/to.html"),
            URI.create("/my/another/to.html"),
            URI.create("/sub/to.html"),
            URI.create("file:/c:/absolute/local/uri.html"),
            URI.create("file://host/absolute/uri.html"),
            URI.create("https://example.io/my/sub/to.html"),
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[1]
            URI.create("../../sub/same.html"),
            URI.create("to.html"),
            URI.create("../../../another/my/sub/to.html"),
            URI.create("/my/sub/another/to.html"),
            URI.create("/my/another/to.html"),
            URI.create("/sub/to.html"),
            URI.create("file:/c:/absolute/local/uri.html"),
            URI.create("file://host/absolute/uri.html"),
            URI.create("https://example.io/my/sub/to.html"),
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[2]
            URI.create("../../../my/sub/same.html"),
            URI.create("../../../my/another/sub/to.html"),
            URI.create("to.html"),
            URI.create("/my/sub/another/to.html"),
            URI.create("/my/another/to.html"),
            URI.create("/sub/to.html"),
            URI.create("file:/c:/absolute/local/uri.html"),
            URI.create("file://host/absolute/uri.html"),
            URI.create("https://example.io/my/sub/to.html"),
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[3]
            URI.create("my/sub/same.html"),
            URI.create("my/another/sub/to.html"),
            URI.create("another/my/sub/to.html"),
            URI.create("to.html"),
            URI.create("../../another/to.html"),
            URI.create("../../../sub/to.html"),
            URI.create("file:/c:/absolute/local/uri.html"),
            URI.create("file://host/absolute/uri.html"),
            URI.create("https://example.io/my/sub/to.html"),
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[4]
            URI.create("my/sub/same.html"),
            URI.create("my/another/sub/to.html"),
            URI.create("another/my/sub/to.html"),
            URI.create("../sub/another/to.html"),
            URI.create("to.html"),
            URI.create("../../sub/to.html"),
            URI.create("file:/c:/absolute/local/uri.html"),
            URI.create("file://host/absolute/uri.html"),
            URI.create("https://example.io/my/sub/to.html"),
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[5]
            URI.create("my/sub/same.html"),
            URI.create("my/another/sub/to.html"),
            URI.create("another/my/sub/to.html"),
            URI.create("../my/sub/another/to.html"),
            URI.create("../my/another/to.html"),
            URI.create("to.html"),
            URI.create("file:/c:/absolute/local/uri.html"),
            URI.create("file://host/absolute/uri.html"),
            URI.create("https://example.io/my/sub/to.html"),
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[6]
            URI.create("my/sub/same.html"),
            URI.create("my/another/sub/to.html"),
            URI.create("another/my/sub/to.html"),
            URI.create("/my/sub/another/to.html"),
            URI.create("/my/another/to.html"),
            URI.create("/sub/to.html"),
            URI.create(""),
            URI.create("file://host/absolute/uri.html"),
            URI.create("https://example.io/my/sub/to.html"),
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[7]
            URI.create("my/sub/same.html"),
            URI.create("my/another/sub/to.html"),
            URI.create("another/my/sub/to.html"),
            URI.create("/my/sub/another/to.html"),
            URI.create("/my/another/to.html"),
            URI.create("/sub/to.html"),
            URI.create("file:/c:/absolute/local/uri.html"),
            URI.create(""),
            URI.create("https://example.io/my/sub/to.html"),
            URI.create("https://example.io/another/deeper/sub/to.html"),
            // from[8]
            URI.create("my/sub/same.html"),
            URI.create("my/another/sub/to.html"),
            URI.create("another/my/sub/to.html"),
            URI.create("/my/sub/another/to.html"),
            URI.create("/my/another/to.html"),
            URI.create("/sub/to.html"),
            URI.create("file:/c:/absolute/local/uri.html"),
            URI.create("file://host/absolute/uri.html"),
            URI.create("to.html"),
            URI.create("../../another/deeper/sub/to.html"),
            // from[9]
            URI.create("my/sub/same.html"),
            URI.create("my/another/sub/to.html"),
            URI.create("another/my/sub/to.html"),
            URI.create("/my/sub/another/to.html"),
            URI.create("/my/another/to.html"),
            URI.create("/sub/to.html"),
            URI.create("file:/c:/absolute/local/uri.html"),
            URI.create("file://host/absolute/uri.html"),
            URI.create("../../../my/sub/to.html"),
            URI.create("to.html")),
        from,
        to);
  }

  @ParameterizedTest
  @MethodSource
  public void _relativeUri(Object expected, URI from, URI to) {
    var actual = evalParameterized(() -> Uris.relativeUri(from, to));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual, $ -> "URI.create(\"" + $ + "\")",
    //        asList("from", "to"),
    //        asList(from, to));

    assertParameterized(actual, expected);
  }
}
