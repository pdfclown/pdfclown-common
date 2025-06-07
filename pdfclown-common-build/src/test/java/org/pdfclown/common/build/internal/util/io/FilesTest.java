/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FilesTest.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util.io;

import static java.util.Arrays.asList;
import static java.util.Map.entry;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.build.test.assertion.Assertions.cartesianArgumentsStream;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.__test.BaseTest;
import org.pdfclown.common.build.test.assertion.Assertions.Argument;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;

/**
 * @author Stefano Chizzolini
 */
class FilesTest extends BaseTest {
  private static final List<Argument<String>> PATHS = asList(
      Argument.of("/home/me/my.sub/test/obj.tar.gz",
          "Multi-part file extension, dot before directory separator, Unix path"),
      Argument.of("smb://myhost/my.sub/test/obj.tar.gz",
          "Multi-part file extension, dot before directory separator, URI path"),
      Argument.of("C:\\my.sub\\test\\obj.tar.gz",
          "Multi-part file extension, dot before directory separator, Windows DOS path"),
      Argument.of("\\\\myhost\\my.sub\\test\\obj.tar.gz",
          "Multi-part file extension, dot before directory separator, Windows UNC path"),
      Argument.of("/home/me/my/test/obj-5.2.9.tar2.gz",
          "Multi-part file extension, dot in base filename"),
      Argument.of("C:\\my\\test-1.5\\obj.tar2.gz",
          "Multi-part file extension, dot before base filename"));

  static Stream<Arguments> fullExtension() {
    return cartesianArgumentsStream(
        // expected
        asList(
            // path[0]: '/home/me/my.sub/test/obj.tar.gz'
            ".tar.gz",
            // path[1]: 'smb://myhost/my.sub/test/obj.tar.gz'
            ".tar.gz",
            // path[2]: 'C:\my.sub\test\obj.tar.gz'
            ".tar.gz",
            // path[3]: '\\myhost\my.sub\test\obj.tar.gz'
            ".tar.gz",
            // path[4]: '/home/me/my/test/obj-5.2.9.tar2.gz'
            ".tar2.gz",
            // path[5]: 'C:\my\test-1.5\obj.tar2.gz'
            ".tar2.gz"),
        // path
        PATHS);
  }

  static Stream<Arguments> replaceFullExtension() {
    return cartesianArgumentsStream(
        // expected
        asList(
            // path[0]: '/home/me/my.sub/test/obj.tar.gz'
            // -- newExtension[0]: '.zip'
            "/home/me/my.sub/test/obj.zip",
            // path[1]: 'smb://myhost/my.sub/test/obj.tar.gz'
            // -- newExtension[0]: '.zip'
            "smb://myhost/my.sub/test/obj.zip",
            // path[2]: 'C:\my.sub\test\obj.tar.gz'
            // -- newExtension[0]: '.zip'
            "C:\\my.sub\\test\\obj.zip",
            // path[3]: '\\myhost\my.sub\test\obj.tar.gz'
            // -- newExtension[0]: '.zip'
            "\\\\myhost\\my.sub\\test\\obj.zip",
            // path[4]: '/home/me/my/test/obj-5.2.9.tar2.gz'
            // -- newExtension[0]: '.zip'
            "/home/me/my/test/obj-5.2.9.zip",
            // path[5]: 'C:\my\test-1.5\obj.tar2.gz'
            // -- newExtension[0]: '.zip'
            "C:\\my\\test-1.5\\obj.zip"),
        // path
        PATHS,
        // newExtension
        asList(".zip"));
  }

  @ParameterizedTest
  @MethodSource
  void fullExtension(String expected, Argument<String> path) {
    assertParameterizedOf(
        () -> Files.fullExtension(path.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("path", path.getValue()))));
  }

  @ParameterizedTest
  @MethodSource
  void replaceFullExtension(String expected, Argument<String> path, String newExtension) {
    assertParameterizedOf(
        () -> Files.replaceFullExtension(path.getValue(), newExtension),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("path", path.getValue()),
            entry("newExtension", newExtension))));
  }
}
