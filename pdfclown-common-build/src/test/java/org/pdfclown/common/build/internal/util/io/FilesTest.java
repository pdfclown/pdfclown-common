/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (FilesTest.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common> (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.build.internal.util.io;

import static java.util.Arrays.asList;
import static org.pdfclown.common.build.test.Tests.argumentsStream;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.__test.BaseTest;
import org.pdfclown.common.build.test.Tests.Argument;

/**
 * @author Stefano Chizzolini
 */
public class FilesTest extends BaseTest {
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

  private static Stream<Arguments> _fullExtension() {
    return argumentsStream(
        // expected
        asList(
            ".tar.gz",
            ".tar.gz",
            ".tar.gz",
            ".tar.gz",
            ".tar2.gz",
            ".tar2.gz"),
        // path
        PATHS);
  }

  private static Stream<Arguments> _replaceFullExtension() {
    return argumentsStream(
        // expected
        asList(
            // -- path[0]='/home/me/my.sub/test/obj.tar.gz'
            "/home/me/my.sub/test/obj.zip",
            // -- path[1]='smb://myhost/my.sub/test/obj.tar.gz'
            "smb://myhost/my.sub/test/obj.zip",
            // -- path[2]='C:\my.sub\test\obj.tar.gz'
            "C:\\my.sub\\test\\obj.zip",
            // -- path[3]='\\myhost\my.sub\test\obj.tar.gz'
            "\\\\myhost\\my.sub\\test\\obj.zip",
            // -- path[4]='/home/me/my/test/obj-5.2.9.tar2.gz'
            "/home/me/my/test/obj-5.2.9.zip",
            // -- path[5]='C:\my\test-1.5\obj.tar2.gz'
            "C:\\my\\test-1.5\\obj.zip"),
        // path
        PATHS,
        // newExtension
        asList(".zip"));
  }

  @ParameterizedTest
  @MethodSource
  public void _fullExtension(String expected, Argument<String> path) {
    var actual = evalParameterized(
        () -> Files.fullExtension(path.getValue()));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual,
    //        asList("path"),
    //        asList(path.getValue()));

    assertParameterized(actual, expected);
  }

  @ParameterizedTest
  @MethodSource
  public void _replaceFullExtension(String expected, Argument<String> path, String newExtension) {
    var actual = evalParameterized(
        () -> Files.replaceFullExtension(path.getValue(), newExtension));

    //    /*
    //     * DO NOT remove (useful in case of arguments update)
    //     */
    //    generateExpected(actual,
    //        asList("path", "newExtension"),
    //        asList(path.getValue(), newExtension));

    assertParameterized(actual, expected);
  }
}
