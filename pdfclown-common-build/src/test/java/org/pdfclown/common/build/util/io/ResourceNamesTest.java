/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ResourceNamesTest.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.util.io;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.pdfclown.common.util.Strings.EMPTY;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings("Convert2MethodRef")
public class ResourceNamesTest extends BaseTest {
  private static final List<Object> BASES = asList(
      null,
      EMPTY,
      ResourceNames.class,
      ResourceNames.class.getPackageName());

  public static final List<String> NAMES = asList(
      // Normal absolute root
      "/",
      // Backslash absolute root
      "\\",
      // Normal absolute name with extension
      "/my/absolute/Resource.txt",
      // Normal absolute name with extension and dotted folder (INVALID)
      "/my/absolute.sub/Resource.txt",
      // Slash-trailing absolute name
      "/my/absolute/resource/",
      // Slash- and backslash-ridden absolute name
      "//my/\\\\other\\/\\deep//absolute\\resource/",
      // Relative root
      "",
      // Normal relative name with extension
      "my/relative/Resource.txt",
      // Slash-trailing relative name
      "my/relative/resource/",
      // Slash- and backslash-ridden relative name
      "my/\\\\other\\/\\deep//relative\\resource/");

  @Test
  void abs() {
    combinationVerifier.verify(
        (name) -> ResourceNames.abs(name),
        List.of("name"),
        // name
        NAMES);
  }

  @Test
  void absBased() {
    combinationVerifier.verify(
        (name, base) -> ResourceNames.absBased(name, base),
        List.of("name", "base"),
        // name
        NAMES,
        // base
        BASES);
  }

  @Test
  void fromPath__unix() throws IOException {
    try (var fs = Jimfs.newFileSystem(Configuration.unix().toBuilder()
        .setWorkingDirectory("/host/cwd").build())) {
      combinationVerifier.verify(
          (filePath, baseDir) -> ResourceNames.fromPath(filePath, baseDir),
          List.of("filePath", "baseDir"),
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
  }

  @Test
  void fromPath__win() throws IOException {
    try (var fs = Jimfs.newFileSystem(Configuration.windows().toBuilder()
        .setWorkingDirectory("c:\\cwd").build())) {
      combinationVerifier.verify(
          (filePath, baseDir) -> ResourceNames.fromPath(filePath, baseDir),
          List.of("filePath", "baseDir"),
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
  }

  @Test
  void fromTypeName() {
    combinationVerifier.verify(
        (typeName) -> ResourceNames.fromTypeName(typeName),
        List.of("typeName"),
        // typeName
        asList(
            null,
            EMPTY,
            ResourceNames.class.getName(),
            ResourceNames.class.getPackageName()));
  }

  @Test
  void isAbs() {
    combinationVerifier.verify(
        (name) -> ResourceNames.isAbs(name),
        List.of("name"),
        // name
        NAMES);
  }

  @Test
  void name_0() {
    assertThat(ResourceNames.name(), Matchers.is(EMPTY));
  }

  @Test
  void name_1() {
    combinationVerifier.verify(
        (parts0) -> ResourceNames.name(parts0),
        List.of("parts[0]"),
        // parts[0]
        NAMES);
  }

  @Test
  void name_2() {
    combinationVerifier.verify(
        (parts0, parts1) -> ResourceNames.name(parts0, parts1),
        List.of("parts[0]", "parts[1]"),
        // parts[0]
        NAMES,
        // parts[1]
        NAMES);
  }

  @Test
  void normal() {
    combinationVerifier.verify(
        (name) -> ResourceNames.normal(name),
        List.of("name"),
        // name
        NAMES);
  }

  @Test
  void parent() {
    combinationVerifier.verify(
        (name) -> ResourceNames.parent(name),
        List.of("name"),
        // name
        NAMES);
  }

  @Test
  void rel() {
    combinationVerifier.verify(
        (name) -> ResourceNames.rel(name),
        List.of("name"),
        // name
        NAMES);
  }

  @Test
  void relBased() {
    combinationVerifier.verify(
        (name, base) -> ResourceNames.relBased(name, base),
        List.of("name", "base"),
        // name
        NAMES,
        // base
        BASES);
  }

  @Test
  void toTypeName() {
    combinationVerifier.verify(
        (name) -> ResourceNames.toTypeName(name),
        List.of("name"),
        // name
        NAMES);
  }
}
