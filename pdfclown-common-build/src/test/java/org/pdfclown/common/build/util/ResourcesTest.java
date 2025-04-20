/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (ResourcesTest.java) is part of pdfclown-common-build module in pdfClown Common project
  (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.build.util;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.pdfclown.common.build.test.Tests.argumentsStream;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.__test.BaseTest;
import org.pdfclown.common.build.test.Tests.Argument;

/**
 * @author Stefano Chizzolini
 */
public class ResourcesTest extends BaseTest {
  private static final List<Argument<String>> NAMES = List.of(
      Argument.of("/",
          "Normal absolute root"),
      Argument.of("\\",
          "Backslash absolute root"),
      Argument.of("/my/absolute/resource",
          "Normal absolute name"),
      Argument.of("/my/absolute/resource/",
          "Slash-trailing absolute name"),
      Argument.of("//my/\\\\other\\/\\deep//absolute\\resource/",
          "Slash- and backslash-ridden absolute name"),
      Argument.of("",
          "Relaive root"),
      Argument.of("my/relative/resource",
          "Normal relative name"),
      Argument.of("my/relative/resource/",
          "Slash-trailing relative name"),
      Argument.of("my/\\\\other\\/\\deep//relative\\resource/",
          "Slash- and backslash-ridden relative name"));

  private static Stream<Arguments> _absName_filePath_unix() {
    var fs = Jimfs.newFileSystem(Configuration.unix().toBuilder()
        .setWorkingDirectory("/host/cwd").build());
    return argumentsStream(
        // expected
        asList(
            // filePath[0]
            "/relative/index1.html",
            "/relative/index1.html",
            "/relative/index1.html",
            // filePath[1]
            null,
            null,
            null,
            // filePath[2]
            "/index3.html",
            null,
            null,
            // filePath[3]
            null,
            null,
            "/another/index4.html"),
        // filePath
        List.of(
            fs.getPath("relative/index1.html"),
            fs.getPath("../relative/index2.html"),
            fs.getPath("/host/cwd/local/index3.html"),
            fs.getPath("/host/absolute/another/index4.html")),
        // baseDir
        List.of(
            fs.getPath("local/"),
            fs.getPath("../local"),
            fs.getPath("/host/absolute")));
  }

  private static Stream<Arguments> _absName_filePath_win() {
    var fs = Jimfs.newFileSystem(Configuration.windows().toBuilder()
        .setWorkingDirectory("c:\\cwd").build());
    return argumentsStream(
        // expected
        asList(
            // filePath[0]
            "/relative/index1.html",
            "/relative/index1.html",
            "/relative/index1.html",
            // filePath[1]
            null,
            null,
            null,
            // filePath[2]
            "/index3.html",
            null,
            null,
            // filePath[3]
            null,
            null,
            "/another/index4.html"),
        // filePath
        List.of(
            fs.getPath("relative\\index1.html"),
            fs.getPath("..\\relative\\index2.html"),
            fs.getPath("c:\\cwd\\local\\index3.html"),
            fs.getPath("c:\\absolute\\another\\index4.html")),
        // baseDir
        List.of(
            fs.getPath("local\\"),
            fs.getPath("..\\local"),
            fs.getPath("c:\\absolute")));
  }

  private static Stream<Arguments> _fullName_basePackage() {
    return argumentsStream(
        // expected
        asList(
            // name[0]
            "/",
            "/",
            // name[1]
            "/",
            "/",
            // name[2]
            "/my/absolute/resource",
            "/my/absolute/resource",
            // name[3]
            "/my/absolute/resource",
            "/my/absolute/resource",
            // name[4]
            "/my/other/deep/absolute/resource",
            "/my/other/deep/absolute/resource",
            // name[5]
            "",
            "org/pdfclown/common/build/util",
            // name[6]
            "my/relative/resource",
            "org/pdfclown/common/build/util/my/relative/resource",
            // name[7]
            "my/relative/resource",
            "org/pdfclown/common/build/util/my/relative/resource",
            // name[8]
            "my/other/deep/relative/resource",
            "org/pdfclown/common/build/util/my/other/deep/relative/resource"),
        // name
        NAMES,
        // basePackage
        asList(
            EMPTY,
            Resources.class.getPackageName()));
  }

  private static Stream<Arguments> _fullName_baseType() {
    return argumentsStream(
        // expected
        asList(
            // name[0]
            "/",
            "/",
            // name[1]
            "/",
            "/",
            // name[2]
            "/my/absolute/resource",
            "/my/absolute/resource",
            // name[3]
            "/my/absolute/resource",
            "/my/absolute/resource",
            // name[4]
            "/my/other/deep/absolute/resource",
            "/my/other/deep/absolute/resource",
            // name[5]
            "",
            "org/pdfclown/common/build/util",
            // name[6]
            "my/relative/resource",
            "org/pdfclown/common/build/util/my/relative/resource",
            // name[7]
            "my/relative/resource",
            "org/pdfclown/common/build/util/my/relative/resource",
            // name[8]
            "my/other/deep/relative/resource",
            "org/pdfclown/common/build/util/my/other/deep/relative/resource"),
        // name
        NAMES,
        // baseType
        asList(
            null,
            Resources.class));
  }

  private static Stream<Arguments> _name_1() {
    return argumentsStream(
        // expected
        asList(
            "/",
            "/",
            "/my/absolute/resource",
            "/my/absolute/resource",
            "/my/other/deep/absolute/resource",
            "",
            "my/relative/resource",
            "my/relative/resource",
            "my/other/deep/relative/resource"),
        // name0
        NAMES);
  }

  private static Stream<Arguments> _name_2() {
    return argumentsStream(
        // expected
        asList(
            // name0[0]
            "/",
            "/",
            "/my/absolute/resource",
            "/my/absolute/resource",
            "/my/other/deep/absolute/resource",
            "/",
            "/my/relative/resource",
            "/my/relative/resource",
            "/my/other/deep/relative/resource",
            // name0[1]
            "/",
            "/",
            "/my/absolute/resource",
            "/my/absolute/resource",
            "/my/other/deep/absolute/resource",
            "/",
            "/my/relative/resource",
            "/my/relative/resource",
            "/my/other/deep/relative/resource",
            // name0[2]
            "/my/absolute/resource",
            "/my/absolute/resource",
            "/my/absolute/resource/my/absolute/resource",
            "/my/absolute/resource/my/absolute/resource",
            "/my/absolute/resource/my/other/deep/absolute/resource",
            "/my/absolute/resource",
            "/my/absolute/resource/my/relative/resource",
            "/my/absolute/resource/my/relative/resource",
            "/my/absolute/resource/my/other/deep/relative/resource",
            // name0[3]
            "/my/absolute/resource",
            "/my/absolute/resource",
            "/my/absolute/resource/my/absolute/resource",
            "/my/absolute/resource/my/absolute/resource",
            "/my/absolute/resource/my/other/deep/absolute/resource",
            "/my/absolute/resource",
            "/my/absolute/resource/my/relative/resource",
            "/my/absolute/resource/my/relative/resource",
            "/my/absolute/resource/my/other/deep/relative/resource",
            // name0[4]
            "/my/other/deep/absolute/resource",
            "/my/other/deep/absolute/resource",
            "/my/other/deep/absolute/resource/my/absolute/resource",
            "/my/other/deep/absolute/resource/my/absolute/resource",
            "/my/other/deep/absolute/resource/my/other/deep/absolute/resource",
            "/my/other/deep/absolute/resource",
            "/my/other/deep/absolute/resource/my/relative/resource",
            "/my/other/deep/absolute/resource/my/relative/resource",
            "/my/other/deep/absolute/resource/my/other/deep/relative/resource",
            // name0[5]
            "",
            "",
            "my/absolute/resource",
            "my/absolute/resource",
            "my/other/deep/absolute/resource",
            "",
            "my/relative/resource",
            "my/relative/resource",
            "my/other/deep/relative/resource",
            // name0[6]
            "my/relative/resource",
            "my/relative/resource",
            "my/relative/resource/my/absolute/resource",
            "my/relative/resource/my/absolute/resource",
            "my/relative/resource/my/other/deep/absolute/resource",
            "my/relative/resource",
            "my/relative/resource/my/relative/resource",
            "my/relative/resource/my/relative/resource",
            "my/relative/resource/my/other/deep/relative/resource",
            // name0[7]
            "my/relative/resource",
            "my/relative/resource",
            "my/relative/resource/my/absolute/resource",
            "my/relative/resource/my/absolute/resource",
            "my/relative/resource/my/other/deep/absolute/resource",
            "my/relative/resource",
            "my/relative/resource/my/relative/resource",
            "my/relative/resource/my/relative/resource",
            "my/relative/resource/my/other/deep/relative/resource",
            // name0[8]
            "my/other/deep/relative/resource",
            "my/other/deep/relative/resource",
            "my/other/deep/relative/resource/my/absolute/resource",
            "my/other/deep/relative/resource/my/absolute/resource",
            "my/other/deep/relative/resource/my/other/deep/absolute/resource",
            "my/other/deep/relative/resource",
            "my/other/deep/relative/resource/my/relative/resource",
            "my/other/deep/relative/resource/my/relative/resource",
            "my/other/deep/relative/resource/my/other/deep/relative/resource"),
        // name0
        NAMES,
        // name1
        NAMES);
  }

  private static Stream<Arguments> _normalize() {
    return argumentsStream(
        // expected
        asList(
            "/",
            "/",
            "/my/absolute/resource",
            "/my/absolute/resource",
            "/my/other/deep/absolute/resource",
            "",
            "my/relative/resource",
            "my/relative/resource",
            "my/other/deep/relative/resource"),
        // name
        NAMES);
  }

  private static Stream<Arguments> _parent() {
    return argumentsStream(
        // expected
        asList(
            null,
            null,
            "/my/absolute",
            "/my/absolute",
            "/my/other/deep/absolute",
            null,
            "my/relative",
            "my/relative",
            "my/other/deep/relative"),
        // name
        NAMES);
  }

  @ParameterizedTest(autoCloseArguments = false)
  @MethodSource
  public void _absName_filePath_unix(String expected, Path filePath, Path baseDir) {
    var actual = evalParameterized(
        () -> Resources.absName(filePath, baseDir));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual,
    //        asList("filePath", "baseDir"),
    //        asList(filePath, baseDir));

    assertParameterized(actual, expected);
  }

  @ParameterizedTest(autoCloseArguments = false)
  @MethodSource
  public void _absName_filePath_win(String expected, Path filePath, Path baseDir) {
    var actual = evalParameterized(
        () -> Resources.absName(filePath, baseDir));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual,
    //        asList("filePath", "baseDir"),
    //        asList(filePath, baseDir));

    assertParameterized(actual, expected);
  }

  @ParameterizedTest
  @MethodSource
  public void _fullName_basePackage(String expected, Argument<String> name, String basePackage) {
    var actual = evalParameterized(
        () -> Resources.fullName(name.getValue(), basePackage));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual,
    //        asList("name", "basePackage"),
    //        asList(name, basePackage));

    assertParameterized(actual, expected);
  }

  @ParameterizedTest
  @MethodSource
  public void _fullName_baseType(String expected, Argument<String> name,
      @Nullable Class<?> baseType) {
    var actual = evalParameterized(
        () -> Resources.fullName(name.getValue(), baseType));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual,
    //        asList("name", "baseType"),
    //        asList(name, baseType));

    assertParameterized(actual, expected);
  }

  public void _name_0() {
    assertParameterized(Resources.name(), EMPTY);
  }

  @ParameterizedTest
  @MethodSource
  public void _name_1(String expected, Argument<String> name0) {
    var actual = evalParameterized(
        () -> Resources.name(name0.getValue()));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual,
    //        asList("name0"),
    //        asList(name0));

    assertParameterized(actual, expected);
  }

  @ParameterizedTest
  @MethodSource
  public void _name_2(String expected, Argument<String> name0, Argument<String> name1) {
    var actual = evalParameterized(
        () -> Resources.name(name0.getValue(), name1.getValue()));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual,
    //        asList("name0", "name1"),
    //        asList(name0, name1));

    assertParameterized(actual, expected);
  }

  @ParameterizedTest
  @MethodSource
  public void _normalize(String expected, Argument<String> name) {
    var actual = evalParameterized(
        () -> Resources.normalize(name.getValue()));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual,
    //        asList("name"),
    //        asList(name));

    assertParameterized(actual, expected);
  }

  @ParameterizedTest
  @MethodSource
  public void _parent(String expected, Argument<String> name) {
    var actual = evalParameterized(
        () -> Resources.parent(name.getValue()));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual,
    //        asList("name"),
    //        asList(name));

    assertParameterized(actual, expected);
  }
}
