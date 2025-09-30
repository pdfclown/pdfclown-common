/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ResourceNamesTest.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.util.io;

import static java.util.Arrays.asList;
import static org.pdfclown.common.build.internal.util_.Aggregations.entry;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;
import static org.pdfclown.common.build.test.assertion.Assertions.Argument.qnamed;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterized;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.__test.BaseTest;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;

/**
 * @author Stefano Chizzolini
 */
class ResourceNamesTest extends BaseTest {
  private static final List<Named<String>> NAMES = asList(
      qnamed("Normal absolute root",
          "/"),
      qnamed("Backslash absolute root",
          "\\"),
      qnamed("Normal absolute name",
          "/my/absolute/resource"),
      qnamed("Slash-trailing absolute name",
          "/my/absolute/resource/"),
      qnamed("Slash- and backslash-ridden absolute name",
          "//my/\\\\other\\/\\deep//absolute\\resource/"),
      qnamed("Relative root",
          ""),
      qnamed("Normal relative name",
          "my/relative/resource"),
      qnamed("Slash-trailing relative name",
          "my/relative/resource/"),
      qnamed("Slash- and backslash-ridden relative name",
          "my/\\\\other\\/\\deep//relative\\resource/"));

  static Stream<Arguments> abs_Path__unix() {
    var fs = Jimfs.newFileSystem(Configuration.unix().toBuilder()
        .setWorkingDirectory("/host/cwd").build());
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // filePath[0]: relative/index1.html
            // [1] baseDir[0]: local
            "/relative/index1.html",
            // [2] baseDir[1]: ../local
            "/relative/index1.html",
            // [3] baseDir[2]: /host/absolute
            "/relative/index1.html",
            //
            // filePath[1]: ../relative/index2.html
            // [4] baseDir[0]: local
            null,
            // [5] baseDir[1]: ../local
            null,
            // [6] baseDir[2]: /host/absolute
            null,
            //
            // filePath[2]: /host/cwd/local/index3.html
            // [7] baseDir[0]: local
            "/index3.html",
            // [8] baseDir[1]: ../local
            null,
            // [9] baseDir[2]: /host/absolute
            null,
            //
            // filePath[3]: /host/absolute/another/index4.html
            // [10] baseDir[0]: local
            null,
            // [11] baseDir[1]: ../local
            null,
            // [12] baseDir[2]: /host/absolute
            "/another/index4.html"),
        // filePath
        asList(
            fs.getPath("relative/index1.html"),
            fs.getPath("../relative/index2.html"),
            fs.getPath("/host/cwd/local/index3.html"),
            fs.getPath("/host/absolute/another/index4.html")),
        // baseDir
        asList(
            fs.getPath("local/"),
            fs.getPath("../local"),
            fs.getPath("/host/absolute")));
  }

  static Stream<Arguments> abs_Path__win() {
    var fs = Jimfs.newFileSystem(Configuration.windows().toBuilder()
        .setWorkingDirectory("c:\\cwd").build());
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // filePath[0]: relative\index1.html
            // [1] baseDir[0]: local
            "/relative/index1.html",
            // [2] baseDir[1]: ..\local
            "/relative/index1.html",
            // [3] baseDir[2]: c:\absolute
            "/relative/index1.html",
            //
            // filePath[1]: ..\relative\index2.html
            // [4] baseDir[0]: local
            null,
            // [5] baseDir[1]: ..\local
            null,
            // [6] baseDir[2]: c:\absolute
            null,
            //
            // filePath[2]: c:\cwd\local\index3.html
            // [7] baseDir[0]: local
            "/index3.html",
            // [8] baseDir[1]: ..\local
            null,
            // [9] baseDir[2]: c:\absolute
            null,
            //
            // filePath[3]: c:\absolute\another\index4.html
            // [10] baseDir[0]: local
            null,
            // [11] baseDir[1]: ..\local
            null,
            // [12] baseDir[2]: c:\absolute
            "/another/index4.html"),
        // filePath
        asList(
            fs.getPath("relative\\index1.html"),
            fs.getPath("..\\relative\\index2.html"),
            fs.getPath("c:\\cwd\\local\\index3.html"),
            fs.getPath("c:\\absolute\\another\\index4.html")),
        // baseDir
        asList(
            fs.getPath("local\\"),
            fs.getPath("..\\local"),
            fs.getPath("c:\\absolute")));
  }

  static Stream<Arguments> full_Class() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // name[0]: "/"
            // [1] baseType[0]: null
            "/",
            // [2] baseType[1]: org.pdfclown.common.build.util.io.ResourceNames
            "/",
            //
            // name[1]: "\\"
            // [3] baseType[0]: null
            "/",
            // [4] baseType[1]: org.pdfclown.common.build.util.io.ResourceNames
            "/",
            //
            // name[2]: "/my/absolute/resource"
            // [5] baseType[0]: null
            "/my/absolute/resource",
            // [6] baseType[1]: org.pdfclown.common.build.util.io.ResourceNames
            "/my/absolute/resource",
            //
            // name[3]: "/my/absolute/resource/"
            // [7] baseType[0]: null
            "/my/absolute/resource",
            // [8] baseType[1]: org.pdfclown.common.build.util.io.ResourceNames
            "/my/absolute/resource",
            //
            // name[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            // [9] baseType[0]: null
            "/my/other/deep/absolute/resource",
            // [10] baseType[1]: org.pdfclown.common.build.util.io.ResourceNames
            "/my/other/deep/absolute/resource",
            //
            // name[5]: ""
            // [11] baseType[0]: null
            "",
            // [12] baseType[1]: org.pdfclown.common.build.util.io.ResourceNames
            "org/pdfclown/common/build/util/io",
            //
            // name[6]: "my/relative/resource"
            // [13] baseType[0]: null
            "my/relative/resource",
            // [14] baseType[1]: org.pdfclown.common.build.util.io.ResourceNames
            "org/pdfclown/common/build/util/io/my/relative/resource",
            //
            // name[7]: "my/relative/resource/"
            // [15] baseType[0]: null
            "my/relative/resource",
            // [16] baseType[1]: org.pdfclown.common.build.util.io.ResourceNames
            "org/pdfclown/common/build/util/io/my/relative/resource",
            //
            // name[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            // [17] baseType[0]: null
            "my/other/deep/relative/resource",
            // [18] baseType[1]: org.pdfclown.common.build.util.io.ResourceNames
            "org/pdfclown/common/build/util/io/my/other/deep/relative/resource"),
        // name
        NAMES,
        // baseType
        asList(
            null,
            ResourceNames.class));
  }

  static Stream<Arguments> full_String() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // name[0]: "/"
            // [1] basePackage[0]: ""
            "/",
            // [2] basePackage[1]: "org.pdfclown.common.build.util.io"
            "/",
            //
            // name[1]: "\\"
            // [3] basePackage[0]: ""
            "/",
            // [4] basePackage[1]: "org.pdfclown.common.build.util.io"
            "/",
            //
            // name[2]: "/my/absolute/resource"
            // [5] basePackage[0]: ""
            "/my/absolute/resource",
            // [6] basePackage[1]: "org.pdfclown.common.build.util.io"
            "/my/absolute/resource",
            //
            // name[3]: "/my/absolute/resource/"
            // [7] basePackage[0]: ""
            "/my/absolute/resource",
            // [8] basePackage[1]: "org.pdfclown.common.build.util.io"
            "/my/absolute/resource",
            //
            // name[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            // [9] basePackage[0]: ""
            "/my/other/deep/absolute/resource",
            // [10] basePackage[1]: "org.pdfclown.common.build.util.io"
            "/my/other/deep/absolute/resource",
            //
            // name[5]: ""
            // [11] basePackage[0]: ""
            "",
            // [12] basePackage[1]: "org.pdfclown.common.build.util.io"
            "org/pdfclown/common/build/util/io",
            //
            // name[6]: "my/relative/resource"
            // [13] basePackage[0]: ""
            "my/relative/resource",
            // [14] basePackage[1]: "org.pdfclown.common.build.util.io"
            "org/pdfclown/common/build/util/io/my/relative/resource",
            //
            // name[7]: "my/relative/resource/"
            // [15] basePackage[0]: ""
            "my/relative/resource",
            // [16] basePackage[1]: "org.pdfclown.common.build.util.io"
            "org/pdfclown/common/build/util/io/my/relative/resource",
            //
            // name[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            // [17] basePackage[0]: ""
            "my/other/deep/relative/resource",
            // [18] basePackage[1]: "org.pdfclown.common.build.util.io"
            "org/pdfclown/common/build/util/io/my/other/deep/relative/resource"),
        // name
        NAMES,
        // basePackage
        asList(
            EMPTY,
            ResourceNames.class.getPackageName()));
  }

  static Stream<Arguments> name_1() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // [1] name0[0]: "/"
            "/",
            // [2] name0[1]: "\\"
            "/",
            // [3] name0[2]: "/my/absolute/resource"
            "/my/absolute/resource",
            // [4] name0[3]: "/my/absolute/resource/"
            "/my/absolute/resource",
            // [5] name0[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            "/my/other/deep/absolute/resource",
            // [6] name0[5]: ""
            "",
            // [7] name0[6]: "my/relative/resource"
            "my/relative/resource",
            // [8] name0[7]: "my/relative/resource/"
            "my/relative/resource",
            // [9] name0[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            "my/other/deep/relative/resource"),
        // name0
        NAMES);
  }

  static Stream<Arguments> name_2() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // name0[0]: "/"
            // [1] name1[0]: "/"
            "/",
            // [2] name1[1]: "\\"
            "/",
            // [3] name1[2]: "/my/absolute/resource"
            "/my/absolute/resource",
            // [4] name1[3]: "/my/absolute/resource/"
            "/my/absolute/resource",
            // [5] name1[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            "/my/other/deep/absolute/resource",
            // [6] name1[5]: ""
            "/",
            // [7] name1[6]: "my/relative/resource"
            "/my/relative/resource",
            // [8] name1[7]: "my/relative/resource/"
            "/my/relative/resource",
            // [9] name1[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            "/my/other/deep/relative/resource",
            //
            // name0[1]: "\\"
            // [10] name1[0]: "/"
            "/",
            // [11] name1[1]: "\\"
            "/",
            // [12] name1[2]: "/my/absolute/resource"
            "/my/absolute/resource",
            // [13] name1[3]: "/my/absolute/resource/"
            "/my/absolute/resource",
            // [14] name1[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            "/my/other/deep/absolute/resource",
            // [15] name1[5]: ""
            "/",
            // [16] name1[6]: "my/relative/resource"
            "/my/relative/resource",
            // [17] name1[7]: "my/relative/resource/"
            "/my/relative/resource",
            // [18] name1[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            "/my/other/deep/relative/resource",
            //
            // name0[2]: "/my/absolute/resource"
            // [19] name1[0]: "/"
            "/my/absolute/resource",
            // [20] name1[1]: "\\"
            "/my/absolute/resource",
            // [21] name1[2]: "/my/absolute/resource"
            "/my/absolute/resource/my/absolute/resource",
            // [22] name1[3]: "/my/absolute/resource/"
            "/my/absolute/resource/my/absolute/resource",
            // [23] name1[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            "/my/absolute/resource/my/other/deep/absolute/resource",
            // [24] name1[5]: ""
            "/my/absolute/resource",
            // [25] name1[6]: "my/relative/resource"
            "/my/absolute/resource/my/relative/resource",
            // [26] name1[7]: "my/relative/resource/"
            "/my/absolute/resource/my/relative/resource",
            // [27] name1[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            "/my/absolute/resource/my/other/deep/relative/resource",
            //
            // name0[3]: "/my/absolute/resource/"
            // [28] name1[0]: "/"
            "/my/absolute/resource",
            // [29] name1[1]: "\\"
            "/my/absolute/resource",
            // [30] name1[2]: "/my/absolute/resource"
            "/my/absolute/resource/my/absolute/resource",
            // [31] name1[3]: "/my/absolute/resource/"
            "/my/absolute/resource/my/absolute/resource",
            // [32] name1[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            "/my/absolute/resource/my/other/deep/absolute/resource",
            // [33] name1[5]: ""
            "/my/absolute/resource",
            // [34] name1[6]: "my/relative/resource"
            "/my/absolute/resource/my/relative/resource",
            // [35] name1[7]: "my/relative/resource/"
            "/my/absolute/resource/my/relative/resource",
            // [36] name1[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            "/my/absolute/resource/my/other/deep/relative/resource",
            //
            // name0[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            // [37] name1[0]: "/"
            "/my/other/deep/absolute/resource",
            // [38] name1[1]: "\\"
            "/my/other/deep/absolute/resource",
            // [39] name1[2]: "/my/absolute/resource"
            "/my/other/deep/absolute/resource/my/absolute/resource",
            // [40] name1[3]: "/my/absolute/resource/"
            "/my/other/deep/absolute/resource/my/absolute/resource",
            // [41] name1[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            "/my/other/deep/absolute/resource/my/other/deep/absolute/resource",
            // [42] name1[5]: ""
            "/my/other/deep/absolute/resource",
            // [43] name1[6]: "my/relative/resource"
            "/my/other/deep/absolute/resource/my/relative/resource",
            // [44] name1[7]: "my/relative/resource/"
            "/my/other/deep/absolute/resource/my/relative/resource",
            // [45] name1[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            "/my/other/deep/absolute/resource/my/other/deep/relative/resource",
            //
            // name0[5]: ""
            // [46] name1[0]: "/"
            "",
            // [47] name1[1]: "\\"
            "",
            // [48] name1[2]: "/my/absolute/resource"
            "my/absolute/resource",
            // [49] name1[3]: "/my/absolute/resource/"
            "my/absolute/resource",
            // [50] name1[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            "my/other/deep/absolute/resource",
            // [51] name1[5]: ""
            "",
            // [52] name1[6]: "my/relative/resource"
            "my/relative/resource",
            // [53] name1[7]: "my/relative/resource/"
            "my/relative/resource",
            // [54] name1[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            "my/other/deep/relative/resource",
            //
            // name0[6]: "my/relative/resource"
            // [55] name1[0]: "/"
            "my/relative/resource",
            // [56] name1[1]: "\\"
            "my/relative/resource",
            // [57] name1[2]: "/my/absolute/resource"
            "my/relative/resource/my/absolute/resource",
            // [58] name1[3]: "/my/absolute/resource/"
            "my/relative/resource/my/absolute/resource",
            // [59] name1[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            "my/relative/resource/my/other/deep/absolute/resource",
            // [60] name1[5]: ""
            "my/relative/resource",
            // [61] name1[6]: "my/relative/resource"
            "my/relative/resource/my/relative/resource",
            // [62] name1[7]: "my/relative/resource/"
            "my/relative/resource/my/relative/resource",
            // [63] name1[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            "my/relative/resource/my/other/deep/relative/resource",
            //
            // name0[7]: "my/relative/resource/"
            // [64] name1[0]: "/"
            "my/relative/resource",
            // [65] name1[1]: "\\"
            "my/relative/resource",
            // [66] name1[2]: "/my/absolute/resource"
            "my/relative/resource/my/absolute/resource",
            // [67] name1[3]: "/my/absolute/resource/"
            "my/relative/resource/my/absolute/resource",
            // [68] name1[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            "my/relative/resource/my/other/deep/absolute/resource",
            // [69] name1[5]: ""
            "my/relative/resource",
            // [70] name1[6]: "my/relative/resource"
            "my/relative/resource/my/relative/resource",
            // [71] name1[7]: "my/relative/resource/"
            "my/relative/resource/my/relative/resource",
            // [72] name1[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            "my/relative/resource/my/other/deep/relative/resource",
            //
            // name0[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            // [73] name1[0]: "/"
            "my/other/deep/relative/resource",
            // [74] name1[1]: "\\"
            "my/other/deep/relative/resource",
            // [75] name1[2]: "/my/absolute/resource"
            "my/other/deep/relative/resource/my/absolute/resource",
            // [76] name1[3]: "/my/absolute/resource/"
            "my/other/deep/relative/resource/my/absolute/resource",
            // [77] name1[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            "my/other/deep/relative/resource/my/other/deep/absolute/resource",
            // [78] name1[5]: ""
            "my/other/deep/relative/resource",
            // [79] name1[6]: "my/relative/resource"
            "my/other/deep/relative/resource/my/relative/resource",
            // [80] name1[7]: "my/relative/resource/"
            "my/other/deep/relative/resource/my/relative/resource",
            // [81] name1[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            "my/other/deep/relative/resource/my/other/deep/relative/resource"),
        // name0
        NAMES,
        // name1
        NAMES);
  }

  static Stream<Arguments> normal() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // [1] name[0]: "/"
            "/",
            // [2] name[1]: "\\"
            "/",
            // [3] name[2]: "/my/absolute/resource"
            "/my/absolute/resource",
            // [4] name[3]: "/my/absolute/resource/"
            "/my/absolute/resource",
            // [5] name[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            "/my/other/deep/absolute/resource",
            // [6] name[5]: ""
            "",
            // [7] name[6]: "my/relative/resource"
            "my/relative/resource",
            // [8] name[7]: "my/relative/resource/"
            "my/relative/resource",
            // [9] name[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            "my/other/deep/relative/resource"),
        // name
        NAMES);
  }

  static Stream<Arguments> parent() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // [1] name[0]: "/"
            null,
            // [2] name[1]: "\\"
            null,
            // [3] name[2]: "/my/absolute/resource"
            "/my/absolute",
            // [4] name[3]: "/my/absolute/resource/"
            "/my/absolute",
            // [5] name[4]: "//my/\\\\other\\/\\deep//absolute\\resource/"
            "/my/other/deep/absolute",
            // [6] name[5]: ""
            null,
            // [7] name[6]: "my/relative/resource"
            "my/relative",
            // [8] name[7]: "my/relative/resource/"
            "my/relative",
            // [9] name[8]: "my/\\\\other\\/\\deep//relative\\resource/"
            "my/other/deep/relative"),
        // name
        NAMES);
  }

  @ParameterizedTest(autoCloseArguments = false)
  @MethodSource
  void abs_Path__unix(Expected<String> expected, Path filePath, Path baseDir) {
    assertParameterizedOf(
        () -> ResourceNames.abs(filePath, baseDir),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("filePath", filePath),
            entry("baseDir", baseDir))));
  }

  @ParameterizedTest(autoCloseArguments = false)
  @MethodSource
  void abs_Path__win(Expected<String> expected, Path filePath, Path baseDir) {
    assertParameterizedOf(
        () -> ResourceNames.abs(filePath, baseDir),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("filePath", filePath),
            entry("baseDir", baseDir))));
  }

  @ParameterizedTest
  @MethodSource
  void full_Class(Expected<String> expected, String name,
      @Nullable Class<?> baseType) {
    assertParameterizedOf(
        () -> ResourceNames.full(name, baseType),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("name", name),
            entry("baseType", baseType))));
  }

  @ParameterizedTest
  @MethodSource
  void full_String(Expected<String> expected, String name, String basePackage) {
    assertParameterizedOf(
        () -> ResourceNames.full(name, basePackage),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("name", name),
            entry("basePackage", basePackage))));
  }

  void name_0() {
    assertParameterized(ResourceNames.name(), Expected.success(EMPTY), null);
  }

  @ParameterizedTest
  @MethodSource
  void name_1(Expected<String> expected, String name0) {
    assertParameterizedOf(
        () -> ResourceNames.name(name0),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("name0", name0))));
  }

  @ParameterizedTest
  @MethodSource
  void name_2(Expected<String> expected, String name0, String name1) {
    assertParameterizedOf(
        () -> ResourceNames.name(name0, name1),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("name0", name0),
            entry("name1", name1))));
  }

  @ParameterizedTest
  @MethodSource
  void normal(Expected<String> expected, String name) {
    assertParameterizedOf(
        () -> ResourceNames.normal(name),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("name", name))));
  }

  @ParameterizedTest
  @MethodSource
  void parent(Expected<String> expected, String name) {
    assertParameterizedOf(
        () -> ResourceNames.parent(name),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("name", name))));
  }
}
