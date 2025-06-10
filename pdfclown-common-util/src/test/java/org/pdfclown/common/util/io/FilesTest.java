/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FilesTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static org.pdfclown.common.build.test.assertion.Assertions.Argument.arg;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.util.Aggregations.entry;
import static org.pdfclown.common.util.Objects.objToLiteralString;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Argument;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class FilesTest extends BaseTest {
  private static final List<Argument<String>> EXTENSIONS = List.of(
      arg("Multi-part, normal",
          ".tar.gz"),
      arg("Multi-part, alt-case",
          ".tar.GZ"),
      arg("Simple, normal",
          ".gz"),
      arg("Simple, alt-case",
          ".GZ"));

  private static final List<Argument<String>> PATHS = List.of(
      // path[0]
      arg("Multi-part file extension, dot before directory separator, Unix path",
          "/home/me/my.sub/test/obj.tar.gz"),
      arg("Multi-part file extension, dot before directory separator, URI path",
          "smb://myhost/my.sub/test/obj.tar.gz"),
      arg("Multi-part file extension, dot before directory separator, Windows DOS path",
          "C:\\my.sub\\test\\obj.tar.gz"),
      // path[3]
      arg("Multi-part file extension, dot before directory separator, Windows UNC path",
          "\\\\myhost\\my.sub\\test\\obj.tar.gz"),
      arg("Multi-part file extension, dot in base filename",
          "/home/me/my/test/obj-5.2.9.tar2.gz"),
      arg("Multi-part file extension, dot before base filename",
          "C:\\my\\test-1.5\\obj.tar2.gz"));

  private static final Function<Object, String> EXPECTED_SOURCE_CODE_GENERATOR___PATH_OF =
      $ -> "fs.getPath(" + objToLiteralString($) + ")";

  static Stream<Arguments> extension() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // path[0]: '/home/me/my.sub/test/obj.tar.gz (Multi-part file extension, dot before dire. . .'
            ".gz",
            // path[1]: 'smb://myhost/my.sub/test/obj.tar.gz (Multi-part file extension, dot before . . .'
            ".gz",
            // path[2]: 'C:\my.sub\test\obj.tar.gz (Multi-part file extension, dot before directory . . .'
            ".gz",
            // path[3]: '\\myhost\my.sub\test\obj.tar.gz (Multi-part file extension, dot before dire. . .'
            ".gz",
            // path[4]: '/home/me/my/test/obj-5.2.9.tar2.gz (Multi-part file extension, dot in base . . .'
            ".gz",
            // path[5]: 'C:\my\test-1.5\obj.tar2.gz (Multi-part file extension, dot before base filename)'
            ".gz"),
        // path
        PATHS);
  }

  static Stream<Arguments> fileName() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // path[0]: '/home/me/my.sub/test/obj.tar.gz (Multi-part file extension, dot before dire. . .'
            "obj.tar.gz",
            // path[1]: 'smb://myhost/my.sub/test/obj.tar.gz (Multi-part file extension, dot before . . .'
            "obj.tar.gz",
            // path[2]: 'C:\my.sub\test\obj.tar.gz (Multi-part file extension, dot before directory . . .'
            "obj.tar.gz",
            // path[3]: '\\myhost\my.sub\test\obj.tar.gz (Multi-part file extension, dot before dire. . .'
            "obj.tar.gz",
            // path[4]: '/home/me/my/test/obj-5.2.9.tar2.gz (Multi-part file extension, dot in base . . .'
            "obj-5.2.9.tar2.gz",
            // path[5]: 'C:\my\test-1.5\obj.tar2.gz (Multi-part file extension, dot before base filename)'
            "obj.tar2.gz"),
        // path
        PATHS);
  }

  static Stream<Arguments> fullExtension() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // path[0]: '/home/me/my.sub/test/obj.tar.gz (Multi-part file extension, dot before dire. . .'
            ".tar.gz",
            // path[1]: 'smb://myhost/my.sub/test/obj.tar.gz (Multi-part file extension, dot before . . .'
            ".tar.gz",
            // path[2]: 'C:\my.sub\test\obj.tar.gz (Multi-part file extension, dot before directory . . .'
            ".tar.gz",
            // path[3]: '\\myhost\my.sub\test\obj.tar.gz (Multi-part file extension, dot before dire. . .'
            ".tar.gz",
            // path[4]: '/home/me/my/test/obj-5.2.9.tar2.gz (Multi-part file extension, dot in base . . .'
            ".tar2.gz",
            // path[5]: 'C:\my\test-1.5\obj.tar2.gz (Multi-part file extension, dot before base filename)'
            ".tar2.gz"),
        // path
        PATHS);
  }

  static Stream<Arguments> isExtension() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // path[0]: '/home/me/my.sub/test/obj.tar.gz (Multi-part file extension, dot before dire. . .'
            // -- extension[0]: '.tar.gz (Multi-part, normal)'
            false,
            // -- extension[1]: '.tar.GZ (Multi-part, alt-case)'
            false,
            // -- extension[2]: '.gz (Simple, normal)'
            true,
            // -- extension[3]: '.GZ (Simple, alt-case)'
            true,
            // path[1]: 'smb://myhost/my.sub/test/obj.tar.gz (Multi-part file extension, dot before . . .'
            // -- extension[0]: '.tar.gz (Multi-part, normal)'
            false,
            // -- extension[1]: '.tar.GZ (Multi-part, alt-case)'
            false,
            // -- extension[2]: '.gz (Simple, normal)'
            true,
            // -- extension[3]: '.GZ (Simple, alt-case)'
            true,
            // path[2]: 'C:\my.sub\test\obj.tar.gz (Multi-part file extension, dot before directory . . .'
            // -- extension[0]: '.tar.gz (Multi-part, normal)'
            false,
            // -- extension[1]: '.tar.GZ (Multi-part, alt-case)'
            false,
            // -- extension[2]: '.gz (Simple, normal)'
            true,
            // -- extension[3]: '.GZ (Simple, alt-case)'
            true,
            // path[3]: '\\myhost\my.sub\test\obj.tar.gz (Multi-part file extension, dot before dire. . .'
            // -- extension[0]: '.tar.gz (Multi-part, normal)'
            false,
            // -- extension[1]: '.tar.GZ (Multi-part, alt-case)'
            false,
            // -- extension[2]: '.gz (Simple, normal)'
            true,
            // -- extension[3]: '.GZ (Simple, alt-case)'
            true,
            // path[4]: '/home/me/my/test/obj-5.2.9.tar2.gz (Multi-part file extension, dot in base . . .'
            // -- extension[0]: '.tar.gz (Multi-part, normal)'
            false,
            // -- extension[1]: '.tar.GZ (Multi-part, alt-case)'
            false,
            // -- extension[2]: '.gz (Simple, normal)'
            true,
            // -- extension[3]: '.GZ (Simple, alt-case)'
            true,
            // path[5]: 'C:\my\test-1.5\obj.tar2.gz (Multi-part file extension, dot before base filename)'
            // -- extension[0]: '.tar.gz (Multi-part, normal)'
            false,
            // -- extension[1]: '.tar.GZ (Multi-part, alt-case)'
            false,
            // -- extension[2]: '.gz (Simple, normal)'
            true,
            // -- extension[3]: '.GZ (Simple, alt-case)'
            true),
        // path
        PATHS,
        // extension
        EXTENSIONS);
  }

  static Stream<Arguments> isFullExtension() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // path[0]: '/home/me/my.sub/test/obj.tar.gz (Multi-part file extension, dot before dire. . .'
            // -- extension[0]: '.tar.gz (Multi-part, normal)'
            true,
            // -- extension[1]: '.tar.GZ (Multi-part, alt-case)'
            true,
            // -- extension[2]: '.gz (Simple, normal)'
            false,
            // -- extension[3]: '.GZ (Simple, alt-case)'
            false,
            // path[1]: 'smb://myhost/my.sub/test/obj.tar.gz (Multi-part file extension, dot before . . .'
            // -- extension[0]: '.tar.gz (Multi-part, normal)'
            true,
            // -- extension[1]: '.tar.GZ (Multi-part, alt-case)'
            true,
            // -- extension[2]: '.gz (Simple, normal)'
            false,
            // -- extension[3]: '.GZ (Simple, alt-case)'
            false,
            // path[2]: 'C:\my.sub\test\obj.tar.gz (Multi-part file extension, dot before directory . . .'
            // -- extension[0]: '.tar.gz (Multi-part, normal)'
            true,
            // -- extension[1]: '.tar.GZ (Multi-part, alt-case)'
            true,
            // -- extension[2]: '.gz (Simple, normal)'
            false,
            // -- extension[3]: '.GZ (Simple, alt-case)'
            false,
            // path[3]: '\\myhost\my.sub\test\obj.tar.gz (Multi-part file extension, dot before dire. . .'
            // -- extension[0]: '.tar.gz (Multi-part, normal)'
            true,
            // -- extension[1]: '.tar.GZ (Multi-part, alt-case)'
            true,
            // -- extension[2]: '.gz (Simple, normal)'
            false,
            // -- extension[3]: '.GZ (Simple, alt-case)'
            false,
            // path[4]: '/home/me/my/test/obj-5.2.9.tar2.gz (Multi-part file extension, dot in base . . .'
            // -- extension[0]: '.tar.gz (Multi-part, normal)'
            false,
            // -- extension[1]: '.tar.GZ (Multi-part, alt-case)'
            false,
            // -- extension[2]: '.gz (Simple, normal)'
            false,
            // -- extension[3]: '.GZ (Simple, alt-case)'
            false,
            // path[5]: 'C:\my\test-1.5\obj.tar2.gz (Multi-part file extension, dot before base filename)'
            // -- extension[0]: '.tar.gz (Multi-part, normal)'
            false,
            // -- extension[1]: '.tar.GZ (Multi-part, alt-case)'
            false,
            // -- extension[2]: '.gz (Simple, normal)'
            false,
            // -- extension[3]: '.GZ (Simple, alt-case)'
            false),
        // path
        PATHS,
        // extension
        EXTENSIONS);
  }

  static Stream<Arguments> pathOf_unix() {
    var fs = Jimfs.newFileSystem(Configuration.unix());
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // uri[0]: 'relative/uri.html'
            // -- fs[0]: 'com.google.common.jimfs.JimfsFileSystem@4152d38d'
            fs.getPath("relative/uri.html"),
            // uri[1]: '../relative/uri.html'
            // -- fs[0]: 'com.google.common.jimfs.JimfsFileSystem@4152d38d'
            fs.getPath("../relative/uri.html"),
            // uri[2]: 'file:/absolute/local/uri.html'
            // -- fs[0]: 'com.google.common.jimfs.JimfsFileSystem@4152d38d'
            fs.getPath("/absolute/local/uri.html"),
            // uri[3]: 'file://host/absolute/local/uri.html'
            // -- fs[0]: 'com.google.common.jimfs.JimfsFileSystem@4152d38d'
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

  static Stream<Arguments> pathOf_win() {
    var fs = Jimfs.newFileSystem(Configuration.windows());
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // uri[0]: 'relative/uri.html'
            // -- fs[0]: 'com.google.common.jimfs.JimfsFileSystem@245a26e1'
            fs.getPath("relative\\uri.html"),
            // uri[1]: '../relative/uri.html'
            // -- fs[0]: 'com.google.common.jimfs.JimfsFileSystem@245a26e1'
            fs.getPath("..\\relative\\uri.html"),
            // uri[2]: 'file:///c:/absolute/local/uri.html'
            // -- fs[0]: 'com.google.common.jimfs.JimfsFileSystem@245a26e1'
            fs.getPath("c:\\absolute\\local\\uri.html"),
            // uri[3]: 'file://host/absolute/uri.html'
            // -- fs[0]: 'com.google.common.jimfs.JimfsFileSystem@245a26e1'
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

  static Stream<Arguments> replaceFullExtension() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // path[0]: '/home/me/my.sub/test/obj.tar.gz (Multi-part f. . .'
            // -- newExtension[0]: '.zip'
            "/home/me/my.sub/test/obj.zip",
            // path[1]: 'smb://myhost/my.sub/test/obj.tar.gz (Multi-pa. . .'
            // -- newExtension[0]: '.zip'
            "smb://myhost/my.sub/test/obj.zip",
            // path[2]: 'C:\my.sub\test\obj.tar.gz (Multi-part file ex. . .'
            // -- newExtension[0]: '.zip'
            "C:\\my.sub\\test\\obj.zip",
            // path[3]: '\\myhost\my.sub\test\obj.tar.gz (Multi-part f. . .'
            // -- newExtension[0]: '.zip'
            "\\\\myhost\\my.sub\\test\\obj.zip",
            // path[4]: '/home/me/my/test/obj-5.2.9.tar2.gz (Multi-par. . .'
            // -- newExtension[0]: '.zip'
            "/home/me/my/test/obj-5.2.9.zip",
            // path[5]: 'C:\my\test-1.5\obj.tar2.gz (Multi-part file e. . .'
            // -- newExtension[0]: '.zip'
            "C:\\my\\test-1.5\\obj.zip"),
        // path
        PATHS,
        // newExtension
        List.of(".zip"));
  }

  static Stream<Arguments> simpleBaseName() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // path[0]: '/home/me/my.sub/test/obj.tar.gz (Multi-part file extension, dot before dire. . .'
            "obj",
            // path[1]: 'smb://myhost/my.sub/test/obj.tar.gz (Multi-part file extension, dot before . . .'
            "obj",
            // path[2]: 'C:\my.sub\test\obj.tar.gz (Multi-part file extension, dot before directory . . .'
            "obj",
            // path[3]: '\\myhost\my.sub\test\obj.tar.gz (Multi-part file extension, dot before dire. . .'
            "obj",
            // path[4]: '/home/me/my/test/obj-5.2.9.tar2.gz (Multi-part file extension, dot in base . . .'
            "obj-5.2.9",
            // path[5]: 'C:\my\test-1.5\obj.tar2.gz (Multi-part file extension, dot before base filename)'
            "obj"),
        // path
        PATHS);
  }

  static Stream<Arguments> withoutExtension() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // path[0]: '/home/me/my.sub/test/obj.tar.gz (Multi-part file extension, dot before dire. . .'
            "/home/me/my.sub/test/obj.tar",
            // path[1]: 'smb://myhost/my.sub/test/obj.tar.gz (Multi-part file extension, dot before . . .'
            "smb://myhost/my.sub/test/obj.tar",
            // path[2]: 'C:\my.sub\test\obj.tar.gz (Multi-part file extension, dot before directory . . .'
            "C:\\my.sub\\test\\obj.tar",
            // path[3]: '\\myhost\my.sub\test\obj.tar.gz (Multi-part file extension, dot before dire. . .'
            "\\\\myhost\\my.sub\\test\\obj.tar",
            // path[4]: '/home/me/my/test/obj-5.2.9.tar2.gz (Multi-part file extension, dot in base . . .'
            "/home/me/my/test/obj-5.2.9.tar2",
            // path[5]: 'C:\my\test-1.5\obj.tar2.gz (Multi-part file extension, dot before base filename)'
            "C:\\my\\test-1.5\\obj.tar2"),
        // path
        PATHS);
  }

  static Stream<Arguments> withoutFullExtension() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // path[0]: '/home/me/my.sub/test/obj.tar.gz (Multi-part file extension, dot before dire. . .'
            "/home/me/my.sub/test/obj",
            // path[1]: 'smb://myhost/my.sub/test/obj.tar.gz (Multi-part file extension, dot before . . .'
            "smb://myhost/my.sub/test/obj",
            // path[2]: 'C:\my.sub\test\obj.tar.gz (Multi-part file extension, dot before directory . . .'
            "C:\\my.sub\\test\\obj",
            // path[3]: '\\myhost\my.sub\test\obj.tar.gz (Multi-part file extension, dot before dire. . .'
            "\\\\myhost\\my.sub\\test\\obj",
            // path[4]: '/home/me/my/test/obj-5.2.9.tar2.gz (Multi-part file extension, dot in base . . .'
            "/home/me/my/test/obj-5.2.9",
            // path[5]: 'C:\my\test-1.5\obj.tar2.gz (Multi-part file extension, dot before base filename)'
            "C:\\my\\test-1.5\\obj"),
        // path
        PATHS);
  }

  @ParameterizedTest
  @MethodSource
  void extension(String expected, Argument<String> path) {
    assertParameterizedOf(
        () -> Files.extension(path.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("path", path))));
  }

  @ParameterizedTest
  @MethodSource
  void fileName(String expected, Argument<String> path) {
    assertParameterizedOf(
        () -> Files.fileName(path.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("path", path))));
  }

  @ParameterizedTest
  @MethodSource
  void fullExtension(String expected, Argument<String> path) {
    assertParameterizedOf(
        () -> Files.fullExtension(path.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("path", path))));
  }

  @ParameterizedTest
  @MethodSource
  void isExtension(Boolean expected, Argument<String> path, Argument<String> extension) {
    assertParameterizedOf(
        () -> Files.isExtension(path.getValue(), extension.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("path", path),
            entry("extension", extension))));
  }

  @ParameterizedTest
  @MethodSource
  void isFullExtension(Boolean expected, Argument<String> path,
      Argument<String> extension) {
    assertParameterizedOf(
        () -> Files.isFullExtension(path.getValue(), extension.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("path", path),
            entry("extension", extension))));
  }

  @ParameterizedTest(autoCloseArguments = false)
  @MethodSource
  void pathOf_unix(Path expected, URI uri, FileSystem fs) {
    assertParameterizedOf(
        () -> Files.pathOf(uri, fs),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("uri", uri),
            entry("fs", fs)))
                .setExpectedSourceCodeGenerator(EXPECTED_SOURCE_CODE_GENERATOR___PATH_OF));
  }

  @ParameterizedTest(autoCloseArguments = false)
  @MethodSource
  void pathOf_win(Path expected, URI uri, FileSystem fs) {
    assertParameterizedOf(
        () -> Files.pathOf(uri, fs),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("uri", uri),
            entry("fs", fs)))
                .setExpectedSourceCodeGenerator(EXPECTED_SOURCE_CODE_GENERATOR___PATH_OF));
  }

  @ParameterizedTest
  @MethodSource
  void replaceFullExtension(String expected, Argument<String> path,
      String newExtension) {
    assertParameterizedOf(
        () -> Files.replaceFullExtension(path.getValue(), newExtension),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("path", path),
            entry("newExtension", newExtension))));
  }

  @ParameterizedTest
  @MethodSource
  void simpleBaseName(String expected, Argument<String> path) {
    assertParameterizedOf(
        () -> Files.simpleBaseName(path.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("path", path))));
  }

  @ParameterizedTest
  @MethodSource
  void withoutExtension(String expected, Argument<String> path) {
    assertParameterizedOf(
        () -> Files.withoutExtension(path.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("path", path))));
  }

  @ParameterizedTest
  @MethodSource
  void withoutFullExtension(String expected, Argument<String> path) {
    assertParameterizedOf(
        () -> Files.withoutFullExtension(path.getValue()),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("path", path))));
  }
}
