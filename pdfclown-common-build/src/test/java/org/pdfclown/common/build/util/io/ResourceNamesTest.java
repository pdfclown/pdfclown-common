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
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.pdfclown.common.build.internal.util.Aggregations.entry;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterized;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.build.test.assertion.Assertions.cartesianArgumentsStream;

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
import org.pdfclown.common.build.test.assertion.Assertions.Argument;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;

/**
 * @author Stefano Chizzolini
 */
public class ResourceNamesTest extends BaseTest {
  private static final List<Argument<String>> NAMES = asList(
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

  static Stream<Arguments> _absName_filePath_unix() {
    var fs = Jimfs.newFileSystem(Configuration.unix().toBuilder()
        .setWorkingDirectory("/host/cwd").build());
    return cartesianArgumentsStream(
        // expected
        java.util.Arrays.asList(
            // filePath[0]: 'relative/index1.html'
            // -- baseDir[0]: 'local'
            "/relative/index1.html",
            // -- baseDir[1]: '../local'
            "/relative/index1.html",
            // -- baseDir[2]: '/host/absolute'
            "/relative/index1.html",
            // filePath[1]: '../relative/index2.html'
            // -- baseDir[0]: 'local'
            null,
            // -- baseDir[1]: '../local'
            null,
            // -- baseDir[2]: '/host/absolute'
            null,
            // filePath[2]: '/host/cwd/local/index3.html'
            // -- baseDir[0]: 'local'
            "/index3.html",
            // -- baseDir[1]: '../local'
            null,
            // -- baseDir[2]: '/host/absolute'
            null,
            // filePath[3]: '/host/absolute/another/index4.html'
            // -- baseDir[0]: 'local'
            null,
            // -- baseDir[1]: '../local'
            null,
            // -- baseDir[2]: '/host/absolute'
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

  static Stream<Arguments> _absName_filePath_win() {
    var fs = Jimfs.newFileSystem(Configuration.windows().toBuilder()
        .setWorkingDirectory("c:\\cwd").build());
    return cartesianArgumentsStream(
        // expected
        java.util.Arrays.asList(
            // filePath[0]: 'relative\index1.html'
            // -- baseDir[0]: 'local'
            "/relative/index1.html",
            // -- baseDir[1]: '..\local'
            "/relative/index1.html",
            // -- baseDir[2]: 'c:\absolute'
            "/relative/index1.html",
            // filePath[1]: '..\relative\index2.html'
            // -- baseDir[0]: 'local'
            null,
            // -- baseDir[1]: '..\local'
            null,
            // -- baseDir[2]: 'c:\absolute'
            null,
            // filePath[2]: 'c:\cwd\local\index3.html'
            // -- baseDir[0]: 'local'
            "/index3.html",
            // -- baseDir[1]: '..\local'
            null,
            // -- baseDir[2]: 'c:\absolute'
            null,
            // filePath[3]: 'c:\absolute\another\index4.html'
            // -- baseDir[0]: 'local'
            null,
            // -- baseDir[1]: '..\local'
            null,
            // -- baseDir[2]: 'c:\absolute'
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

  static Stream<Arguments> _fullName_basePackage() {
    return cartesianArgumentsStream(
        // expected
        java.util.Arrays.asList(
            // name[0]: '/ (Normal absolute root)'
            // -- basePackage[0]: '???'
            "/",
            // -- basePackage[1]: 'org.pdfclown.common.build.util.io'
            "/",
            // name[1]: '\ (Backslash absolute root)'
            // -- basePackage[0]: '???'
            "/",
            // -- basePackage[1]: 'org.pdfclown.common.build.util.io'
            "/",
            // name[2]: '/my/absolute/resource (Normal absolute name)'
            // -- basePackage[0]: '???'
            "/my/absolute/resource",
            // -- basePackage[1]: 'org.pdfclown.common.build.util.io'
            "/my/absolute/resource",
            // name[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            // -- basePackage[0]: '???'
            "/my/absolute/resource",
            // -- basePackage[1]: 'org.pdfclown.common.build.util.io'
            "/my/absolute/resource",
            // name[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            // -- basePackage[0]: '???'
            "/my/other/deep/absolute/resource",
            // -- basePackage[1]: 'org.pdfclown.common.build.util.io'
            "/my/other/deep/absolute/resource",
            // name[5]: ' (Relaive root)'
            // -- basePackage[0]: '???'
            "",
            // -- basePackage[1]: 'org.pdfclown.common.build.util.io'
            "org/pdfclown/common/build/util/io",
            // name[6]: 'my/relative/resource (Normal relative name)'
            // -- basePackage[0]: '???'
            "my/relative/resource",
            // -- basePackage[1]: 'org.pdfclown.common.build.util.io'
            "org/pdfclown/common/build/util/io/my/relative/resource",
            // name[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            // -- basePackage[0]: '???'
            "my/relative/resource",
            // -- basePackage[1]: 'org.pdfclown.common.build.util.io'
            "org/pdfclown/common/build/util/io/my/relative/resource",
            // name[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            // -- basePackage[0]: '???'
            "my/other/deep/relative/resource",
            // -- basePackage[1]: 'org.pdfclown.common.build.util.io'
            "org/pdfclown/common/build/util/io/my/other/deep/relative/resource"),
        // name
        NAMES,
        // basePackage
        asList(
            EMPTY,
            ResourceNames.class.getPackageName()));
  }

  static Stream<Arguments> _fullName_baseType() {
    return cartesianArgumentsStream(
        // expected
        java.util.Arrays.asList(
            // name[0]: '/ (Normal absolute root)'
            // -- baseType[0]: 'null'
            "/",
            // -- baseType[1]: 'class org.pdfclown.common.build.util.io.ResourceNames'
            "/",
            // name[1]: '\ (Backslash absolute root)'
            // -- baseType[0]: 'null'
            "/",
            // -- baseType[1]: 'class org.pdfclown.common.build.util.io.ResourceNames'
            "/",
            // name[2]: '/my/absolute/resource (Normal absolute name)'
            // -- baseType[0]: 'null'
            "/my/absolute/resource",
            // -- baseType[1]: 'class org.pdfclown.common.build.util.io.ResourceNames'
            "/my/absolute/resource",
            // name[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            // -- baseType[0]: 'null'
            "/my/absolute/resource",
            // -- baseType[1]: 'class org.pdfclown.common.build.util.io.ResourceNames'
            "/my/absolute/resource",
            // name[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            // -- baseType[0]: 'null'
            "/my/other/deep/absolute/resource",
            // -- baseType[1]: 'class org.pdfclown.common.build.util.io.ResourceNames'
            "/my/other/deep/absolute/resource",
            // name[5]: ' (Relaive root)'
            // -- baseType[0]: 'null'
            "",
            // -- baseType[1]: 'class org.pdfclown.common.build.util.io.ResourceNames'
            "org/pdfclown/common/build/util/io",
            // name[6]: 'my/relative/resource (Normal relative name)'
            // -- baseType[0]: 'null'
            "my/relative/resource",
            // -- baseType[1]: 'class org.pdfclown.common.build.util.io.ResourceNames'
            "org/pdfclown/common/build/util/io/my/relative/resource",
            // name[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            // -- baseType[0]: 'null'
            "my/relative/resource",
            // -- baseType[1]: 'class org.pdfclown.common.build.util.io.ResourceNames'
            "org/pdfclown/common/build/util/io/my/relative/resource",
            // name[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            // -- baseType[0]: 'null'
            "my/other/deep/relative/resource",
            // -- baseType[1]: 'class org.pdfclown.common.build.util.io.ResourceNames'
            "org/pdfclown/common/build/util/io/my/other/deep/relative/resource"),
        // name
        NAMES,
        // baseType
        asList(
            null,
            ResourceNames.class));
  }

  static Stream<Arguments> _name_1() {
    return cartesianArgumentsStream(
        // expected
        java.util.Arrays.asList(
            // name0[0]: '/ (Normal absolute root)'
            "/",
            // name0[1]: '\ (Backslash absolute root)'
            "/",
            // name0[2]: '/my/absolute/resource (Normal absolute name)'
            "/my/absolute/resource",
            // name0[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            "/my/absolute/resource",
            // name0[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            "/my/other/deep/absolute/resource",
            // name0[5]: ' (Relaive root)'
            "",
            // name0[6]: 'my/relative/resource (Normal relative name)'
            "my/relative/resource",
            // name0[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            "my/relative/resource",
            // name0[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            "my/other/deep/relative/resource"),
        // name0
        NAMES);
  }

  static Stream<Arguments> _name_2() {
    return cartesianArgumentsStream(
        // expected
        java.util.Arrays.asList(
            // name0[0]: '/ (Normal absolute root)'
            // -- name1[0]: '/ (Normal absolute root)'
            "/",
            // -- name1[1]: '\ (Backslash absolute root)'
            "/",
            // -- name1[2]: '/my/absolute/resource (Normal absolute name)'
            "/my/absolute/resource",
            // -- name1[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            "/my/absolute/resource",
            // -- name1[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            "/my/other/deep/absolute/resource",
            // -- name1[5]: ' (Relaive root)'
            "/",
            // -- name1[6]: 'my/relative/resource (Normal relative name)'
            "/my/relative/resource",
            // -- name1[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            "/my/relative/resource",
            // -- name1[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            "/my/other/deep/relative/resource",
            // name0[1]: '\ (Backslash absolute root)'
            // -- name1[0]: '/ (Normal absolute root)'
            "/",
            // -- name1[1]: '\ (Backslash absolute root)'
            "/",
            // -- name1[2]: '/my/absolute/resource (Normal absolute name)'
            "/my/absolute/resource",
            // -- name1[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            "/my/absolute/resource",
            // -- name1[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            "/my/other/deep/absolute/resource",
            // -- name1[5]: ' (Relaive root)'
            "/",
            // -- name1[6]: 'my/relative/resource (Normal relative name)'
            "/my/relative/resource",
            // -- name1[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            "/my/relative/resource",
            // -- name1[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            "/my/other/deep/relative/resource",
            // name0[2]: '/my/absolute/resource (Normal absolute name)'
            // -- name1[0]: '/ (Normal absolute root)'
            "/my/absolute/resource",
            // -- name1[1]: '\ (Backslash absolute root)'
            "/my/absolute/resource",
            // -- name1[2]: '/my/absolute/resource (Normal absolute name)'
            "/my/absolute/resource/my/absolute/resource",
            // -- name1[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            "/my/absolute/resource/my/absolute/resource",
            // -- name1[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            "/my/absolute/resource/my/other/deep/absolute/resource",
            // -- name1[5]: ' (Relaive root)'
            "/my/absolute/resource",
            // -- name1[6]: 'my/relative/resource (Normal relative name)'
            "/my/absolute/resource/my/relative/resource",
            // -- name1[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            "/my/absolute/resource/my/relative/resource",
            // -- name1[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            "/my/absolute/resource/my/other/deep/relative/resource",
            // name0[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            // -- name1[0]: '/ (Normal absolute root)'
            "/my/absolute/resource",
            // -- name1[1]: '\ (Backslash absolute root)'
            "/my/absolute/resource",
            // -- name1[2]: '/my/absolute/resource (Normal absolute name)'
            "/my/absolute/resource/my/absolute/resource",
            // -- name1[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            "/my/absolute/resource/my/absolute/resource",
            // -- name1[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            "/my/absolute/resource/my/other/deep/absolute/resource",
            // -- name1[5]: ' (Relaive root)'
            "/my/absolute/resource",
            // -- name1[6]: 'my/relative/resource (Normal relative name)'
            "/my/absolute/resource/my/relative/resource",
            // -- name1[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            "/my/absolute/resource/my/relative/resource",
            // -- name1[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            "/my/absolute/resource/my/other/deep/relative/resource",
            // name0[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            // -- name1[0]: '/ (Normal absolute root)'
            "/my/other/deep/absolute/resource",
            // -- name1[1]: '\ (Backslash absolute root)'
            "/my/other/deep/absolute/resource",
            // -- name1[2]: '/my/absolute/resource (Normal absolute name)'
            "/my/other/deep/absolute/resource/my/absolute/resource",
            // -- name1[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            "/my/other/deep/absolute/resource/my/absolute/resource",
            // -- name1[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            "/my/other/deep/absolute/resource/my/other/deep/absolute/resource",
            // -- name1[5]: ' (Relaive root)'
            "/my/other/deep/absolute/resource",
            // -- name1[6]: 'my/relative/resource (Normal relative name)'
            "/my/other/deep/absolute/resource/my/relative/resource",
            // -- name1[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            "/my/other/deep/absolute/resource/my/relative/resource",
            // -- name1[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            "/my/other/deep/absolute/resource/my/other/deep/relative/resource",
            // name0[5]: ' (Relaive root)'
            // -- name1[0]: '/ (Normal absolute root)'
            "",
            // -- name1[1]: '\ (Backslash absolute root)'
            "",
            // -- name1[2]: '/my/absolute/resource (Normal absolute name)'
            "my/absolute/resource",
            // -- name1[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            "my/absolute/resource",
            // -- name1[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            "my/other/deep/absolute/resource",
            // -- name1[5]: ' (Relaive root)'
            "",
            // -- name1[6]: 'my/relative/resource (Normal relative name)'
            "my/relative/resource",
            // -- name1[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            "my/relative/resource",
            // -- name1[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            "my/other/deep/relative/resource",
            // name0[6]: 'my/relative/resource (Normal relative name)'
            // -- name1[0]: '/ (Normal absolute root)'
            "my/relative/resource",
            // -- name1[1]: '\ (Backslash absolute root)'
            "my/relative/resource",
            // -- name1[2]: '/my/absolute/resource (Normal absolute name)'
            "my/relative/resource/my/absolute/resource",
            // -- name1[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            "my/relative/resource/my/absolute/resource",
            // -- name1[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            "my/relative/resource/my/other/deep/absolute/resource",
            // -- name1[5]: ' (Relaive root)'
            "my/relative/resource",
            // -- name1[6]: 'my/relative/resource (Normal relative name)'
            "my/relative/resource/my/relative/resource",
            // -- name1[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            "my/relative/resource/my/relative/resource",
            // -- name1[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            "my/relative/resource/my/other/deep/relative/resource",
            // name0[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            // -- name1[0]: '/ (Normal absolute root)'
            "my/relative/resource",
            // -- name1[1]: '\ (Backslash absolute root)'
            "my/relative/resource",
            // -- name1[2]: '/my/absolute/resource (Normal absolute name)'
            "my/relative/resource/my/absolute/resource",
            // -- name1[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            "my/relative/resource/my/absolute/resource",
            // -- name1[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            "my/relative/resource/my/other/deep/absolute/resource",
            // -- name1[5]: ' (Relaive root)'
            "my/relative/resource",
            // -- name1[6]: 'my/relative/resource (Normal relative name)'
            "my/relative/resource/my/relative/resource",
            // -- name1[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            "my/relative/resource/my/relative/resource",
            // -- name1[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            "my/relative/resource/my/other/deep/relative/resource",
            // name0[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            // -- name1[0]: '/ (Normal absolute root)'
            "my/other/deep/relative/resource",
            // -- name1[1]: '\ (Backslash absolute root)'
            "my/other/deep/relative/resource",
            // -- name1[2]: '/my/absolute/resource (Normal absolute name)'
            "my/other/deep/relative/resource/my/absolute/resource",
            // -- name1[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            "my/other/deep/relative/resource/my/absolute/resource",
            // -- name1[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            "my/other/deep/relative/resource/my/other/deep/absolute/resource",
            // -- name1[5]: ' (Relaive root)'
            "my/other/deep/relative/resource",
            // -- name1[6]: 'my/relative/resource (Normal relative name)'
            "my/other/deep/relative/resource/my/relative/resource",
            // -- name1[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            "my/other/deep/relative/resource/my/relative/resource",
            // -- name1[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            "my/other/deep/relative/resource/my/other/deep/relative/resource"),
        // name0
        NAMES,
        // name1
        NAMES);
  }

  static Stream<Arguments> _normalize() {
    return cartesianArgumentsStream(
        // expected
        java.util.Arrays.asList(
            // name[0]: '/ (Normal absolute root)'
            "/",
            // name[1]: '\ (Backslash absolute root)'
            "/",
            // name[2]: '/my/absolute/resource (Normal absolute name)'
            "/my/absolute/resource",
            // name[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            "/my/absolute/resource",
            // name[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            "/my/other/deep/absolute/resource",
            // name[5]: ' (Relaive root)'
            "",
            // name[6]: 'my/relative/resource (Normal relative name)'
            "my/relative/resource",
            // name[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            "my/relative/resource",
            // name[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            "my/other/deep/relative/resource"),
        // name
        NAMES);
  }

  static Stream<Arguments> _parent() {
    return cartesianArgumentsStream(
        // expected
        java.util.Arrays.asList(
            // name[0]: '/ (Normal absolute root)'
            null,
            // name[1]: '\ (Backslash absolute root)'
            null,
            // name[2]: '/my/absolute/resource (Normal absolute name)'
            "/my/absolute",
            // name[3]: '/my/absolute/resource/ (Slash-trailing absolute name)'
            "/my/absolute",
            // name[4]: '//my/\\other\/\deep//absolute\resource/ (Slash- and backslash-ridden absolu. . .'
            "/my/other/deep/absolute",
            // name[5]: ' (Relaive root)'
            null,
            // name[6]: 'my/relative/resource (Normal relative name)'
            "my/relative",
            // name[7]: 'my/relative/resource/ (Slash-trailing relative name)'
            "my/relative",
            // name[8]: 'my/\\other\/\deep//relative\resource/ (Slash- and backslash-ridden relative. . .'
            "my/other/deep/relative"),
        // name
        NAMES);
  }

  @ParameterizedTest(autoCloseArguments = false)
  @MethodSource
  public void _absName_filePath_unix(String expected, Path filePath, Path baseDir) {
    assertParameterizedOf(
        () -> ResourceNames.absName(filePath, baseDir),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("filePath", filePath),
            entry("baseDir", baseDir))));
  }

  @ParameterizedTest(autoCloseArguments = false)
  @MethodSource
  public void _absName_filePath_win(String expected, Path filePath, Path baseDir) {
    assertParameterizedOf(
        () -> ResourceNames.absName(filePath, baseDir),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("filePath", filePath),
            entry("baseDir", baseDir))));
  }

  @ParameterizedTest
  @MethodSource
  public void _fullName_basePackage(String expected, Argument<String> name, String basePackage) {
    assertParameterizedOf(
        () -> ResourceNames.fullName(name.getValue(), basePackage),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("name", name),
            entry("basePackage", basePackage))));
  }

  @ParameterizedTest
  @MethodSource
  public void _fullName_baseType(String expected, Argument<String> name,
      @Nullable Class<?> baseType) {
    assertParameterizedOf(
        () -> ResourceNames.fullName(name.getValue(), baseType),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("name", name),
            entry("baseType", baseType))));
  }

  public void _name_0() {
    assertParameterized(ResourceNames.name(), EMPTY, null);
  }

  @ParameterizedTest
  @MethodSource
  public void _name_1(String expected, Argument<String> name0) {
    assertParameterizedOf(
        () -> ResourceNames.name(name0.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("name0", name0))));
  }

  @ParameterizedTest
  @MethodSource
  public void _name_2(String expected, Argument<String> name0, Argument<String> name1) {
    assertParameterizedOf(
        () -> ResourceNames.name(name0.getValue(), name1.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("name0", name0),
            entry("name1", name1))));
  }

  @ParameterizedTest
  @MethodSource
  public void _normalize(String expected, Argument<String> name) {
    assertParameterizedOf(
        () -> ResourceNames.normalize(name.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("name", name))));
  }

  @ParameterizedTest
  @MethodSource
  public void _parent(String expected, Argument<String> name) {
    assertParameterizedOf(
        () -> ResourceNames.parent(name.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("name", name))));
  }
}
