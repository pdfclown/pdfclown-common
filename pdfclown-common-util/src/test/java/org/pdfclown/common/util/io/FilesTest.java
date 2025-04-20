/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (FilesTest.java) is part of pdfclown-common-util module in pdfClown Common project (this
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
package org.pdfclown.common.util.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.pdfclown.common.build.test.Tests.argumentsStream;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.Tests.Argument;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
public class FilesTest extends BaseTest {
  private static final List<Argument<String>> EXTENSIONS = List.of(
      Argument.of(".tar.gz",
          "Multi-part, normal"),
      Argument.of(".tar.GZ",
          "Multi-part, alt-case"),
      Argument.of(".gz",
          "Simple, normal"),
      Argument.of(".GZ",
          "Simple, alt-case"));

  private static final List<Argument<String>> PATHS = List.of(
      // path[0]
      Argument.of("/home/me/my.sub/test/obj.tar.gz",
          "Multi-part file extension, dot before directory separator, Unix path"),
      Argument.of("smb://myhost/my.sub/test/obj.tar.gz",
          "Multi-part file extension, dot before directory separator, URI path"),
      Argument.of("C:\\my.sub\\test\\obj.tar.gz",
          "Multi-part file extension, dot before directory separator, Windows DOS path"),
      // path[3]
      Argument.of("\\\\myhost\\my.sub\\test\\obj.tar.gz",
          "Multi-part file extension, dot before directory separator, Windows UNC path"),
      Argument.of("/home/me/my/test/obj-5.2.9.tar2.gz",
          "Multi-part file extension, dot in base filename"),
      Argument.of("C:\\my\\test-1.5\\obj.tar2.gz",
          "Multi-part file extension, dot before base filename"));

  private static Stream<Arguments> _extension() {
    return argumentsStream(
        // expected
        List.of(
            // path[0]
            ".gz",
            ".gz",
            ".gz",
            // path[3]
            ".gz",
            ".gz",
            ".gz"),
        // path
        PATHS);
  }

  private static Stream<Arguments> _fileName() {
    return argumentsStream(
        // expected
        List.of(
            // path[0]
            "obj.tar.gz",
            "obj.tar.gz",
            "obj.tar.gz",
            // path[3]
            "obj.tar.gz",
            "obj-5.2.9.tar2.gz",
            "obj.tar2.gz"),
        // path
        PATHS);
  }

  private static Stream<Arguments> _fullBaseName() {
    return argumentsStream(
        // expected
        List.of(
            // path[0]
            "obj",
            "obj",
            "obj",
            // path[3]
            "obj",
            "obj-5.2.9",
            "obj"),
        // path
        PATHS);
  }

  private static Stream<Arguments> _fullExtension() {
    return argumentsStream(
        // expected
        List.of(
            // path[0]
            ".tar.gz",
            ".tar.gz",
            ".tar.gz",
            // path[3]
            ".tar.gz",
            ".tar2.gz",
            ".tar2.gz"),
        // path
        PATHS);
  }

  private static Stream<Arguments> _isExtension() {
    return argumentsStream(
        // expected
        List.of(
            // path[0]
            false, false, true, true,
            false, false, true, true,
            false, false, true, true,
            // path[3]
            false, false, true, true,
            false, false, true, true,
            false, false, true, true),
        // path
        PATHS,
        // extension
        EXTENSIONS);
  }

  private static Stream<Arguments> _isFullExtension() {
    return argumentsStream(
        // expected
        List.of(
            // path[0]
            true, true, false, false,
            true, true, false, false,
            true, true, false, false,
            // path[3]
            true, true, false, false,
            false, false, false, false,
            false, false, false, false),
        // path
        PATHS,
        // extension
        EXTENSIONS);
  }

  private static Stream<Arguments> _replaceFullExtension() {
    return argumentsStream(
        // expected
        List.of(
            // path[0]
            "/home/me/my.sub/test/obj.zip",
            "smb://myhost/my.sub/test/obj.zip",
            "C:\\my.sub\\test\\obj.zip",
            // path[3]
            "\\\\myhost\\my.sub\\test\\obj.zip",
            "/home/me/my/test/obj-5.2.9.zip",
            "C:\\my\\test-1.5\\obj.zip"),
        // path
        PATHS,
        // newExtension
        List.of(".zip"));
  }

  private static Stream<Arguments> _toPath_unix() {
    var fs = Jimfs.newFileSystem(Configuration.unix());
    return argumentsStream(
        // expected
        List.of(
            // uri[0]
            fs.getPath("relative/uri.html"),
            // uri[1]
            fs.getPath("../relative/uri.html"),
            // uri[2]
            fs.getPath("/absolute/local/uri.html"),
            // uri[3]
            fs.getPath("/host/absolute/local/uri.html")),
        // uri
        List.of(
            URI.create("relative/uri.html"),
            URI.create("../relative/uri.html"),
            URI.create("file:/absolute/local/uri.html"),
            URI.create("file://host/absolute/local/uri.html")),
        // fs
        List.of(fs));
  }

  private static Stream<Arguments> _toPath_win() {
    var fs = Jimfs.newFileSystem(Configuration.windows());
    return argumentsStream(
        // expected
        List.of(
            // uri[0]
            fs.getPath("relative\\uri.html"),
            // uri[1]
            fs.getPath("..\\relative\\uri.html"),
            // uri[2]
            fs.getPath("c:\\absolute\\local\\uri.html"),
            // uri[3]
            fs.getPath("\\\\host\\absolute\\uri.html")),
        // uri
        List.of(
            URI.create("relative/uri.html"),
            URI.create("../relative/uri.html"),
            URI.create("file:///c:/absolute/local/uri.html"),
            URI.create("file://host/absolute/uri.html")),
        // fs
        List.of(fs));
  }

  private static Stream<Arguments> _withoutExtension() {
    return argumentsStream(
        // expected
        List.of(
            // path[0]
            "/home/me/my.sub/test/obj.tar",
            "smb://myhost/my.sub/test/obj.tar",
            "C:\\my.sub\\test\\obj.tar",
            // path[3]
            "\\\\myhost\\my.sub\\test\\obj.tar",
            "/home/me/my/test/obj-5.2.9.tar2",
            "C:\\my\\test-1.5\\obj.tar2"),
        // path
        PATHS);
  }

  private static Stream<Arguments> _withoutFullExtension() {
    return argumentsStream(
        // expected
        List.of(
            // path[0]
            "/home/me/my.sub/test/obj",
            "smb://myhost/my.sub/test/obj",
            "C:\\my.sub\\test\\obj",
            // path[3]
            "\\\\myhost\\my.sub\\test\\obj",
            "/home/me/my/test/obj-5.2.9",
            "C:\\my\\test-1.5\\obj"),
        // path
        PATHS);
  }

  @ParameterizedTest
  @MethodSource
  public void _extension(String expected, Argument<String> path) {
    assertThat(Files.extension(path.getValue()), is(expected));
  }

  @ParameterizedTest
  @MethodSource
  public void _fileName(String expected, Argument<String> path) {
    assertThat(Files.fileName(path.getValue()), is(expected));
  }

  @ParameterizedTest
  @MethodSource
  public void _fullBaseName(String expected, Argument<String> path) {
    assertThat(Files.simpleBaseName(path.getValue()), is(expected));
  }

  @ParameterizedTest
  @MethodSource
  public void _fullExtension(String expected, Argument<String> path) {
    assertThat(Files.fullExtension(path.getValue()), is(expected));
  }

  @ParameterizedTest
  @MethodSource
  public void _isExtension(boolean expected, Argument<String> path, Argument<String> extension) {
    assertThat(Files.isExtension(path.getValue(), extension.getValue()), is(expected));
  }

  @ParameterizedTest
  @MethodSource
  public void _isFullExtension(boolean expected, Argument<String> path,
      Argument<String> extension) {
    assertThat(Files.isFullExtension(path.getValue(), extension.getValue()), is(expected));
  }

  @ParameterizedTest
  @MethodSource
  public void _replaceFullExtension(String expected, Argument<String> path,
      String newExtension) {
    assertThat(Files.replaceFullExtension(path.getValue(), newExtension), is(expected));
  }

  @ParameterizedTest(autoCloseArguments = false)
  @MethodSource
  public void _toPath_unix(Path expected, URI uri, FileSystem fs) {
    assertThat(Files.pathOf(uri, fs), is(expected));
  }

  @ParameterizedTest(autoCloseArguments = false)
  @MethodSource
  public void _toPath_win(Path expected, URI uri, FileSystem fs) {
    assertThat(Files.pathOf(uri, fs), is(expected));
  }

  @ParameterizedTest
  @MethodSource
  public void _withoutExtension(String expected, Argument<String> path) {
    assertThat(Files.withoutExtension(path.getValue()), is(expected));
  }

  @ParameterizedTest
  @MethodSource
  public void _withoutFullExtension(String expected, Argument<String> path) {
    assertThat(Files.withoutFullExtension(path.getValue()), is(expected));
  }
}
