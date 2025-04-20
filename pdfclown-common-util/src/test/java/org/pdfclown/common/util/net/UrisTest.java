/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (UrisTest.java) is part of pdfclown-common-util module in pdfClown Common project (this
  Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
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
