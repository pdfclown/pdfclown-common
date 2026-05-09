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
import static org.pdfclown.common.build.test.assertion.Verifiers.VERIFIER__COMBINATION;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class UrisTest extends BaseTest {
  @Test
  void relativize() {
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

    VERIFIER__COMBINATION.verify(
        ($from, $to) -> Uris.relativize($from, $to),
        List.of("from", "to"),
        // from
        from,
        // to
        to);
  }
}
