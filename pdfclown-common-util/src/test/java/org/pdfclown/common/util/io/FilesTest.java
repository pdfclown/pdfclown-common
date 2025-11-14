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

import static java.util.Arrays.asList;
import static org.pdfclown.common.build.test.assertion.Assertions.Argument.qnamed;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class FilesTest extends BaseTest {
  private static final List<Named<String>> EXTENSIONS = List.of(
      qnamed("Multi-part, normal",
          ".tar.gz"),
      qnamed("Multi-part, alt-case",
          ".tar.GZ"),
      qnamed("Simple, normal",
          ".gz"),
      qnamed("Simple, alt-case",
          ".GZ"));

  private static final List<Named<String>> FILES = List.of(
      qnamed("Unix path, dot inside directory, multi-part file extension",
          "/home/me/my.sub/test/obj.TAR.GZ"),
      qnamed("URI path, dot inside directory, multi-part file extension",
          "smb://myhost/my.sub/test/obj.TAR.gz"),
      qnamed("Windows DOS path, dot inside directory, multi-part file extension",
          "C:\\my.sub\\test\\obj.tar.GZ"),
      qnamed("Windows UNC path, dot inside directory, multi-part file extension",
          "\\\\myhost\\my.sub\\test\\obj.tar.gz"),
      qnamed("Dot inside base filename, multi-part file extension",
          "/home/me/my/test/obj-5.2.9.tar2.gz"));

  static Stream<Arguments> baseName_full() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] file[0]: "/home/me/my.sub/test/obj.TAR.GZ"
            "obj",
            // [2] file[1]: "smb://myhost/my.sub/test/obj.TAR.gz"
            "obj",
            // [3] file[2]: "C:\\my.sub\\test\\obj.tar.GZ"
            "obj",
            // [4] file[3]: "\\\\myhost\\my.sub\\test\\obj.tar.gz"
            "obj",
            // [5] file[4]: "/home/me/my/test/obj-5.2.9.tar2.gz"
            "obj-5.2.9"),
        // file
        FILES);
  }

  static Stream<Arguments> cognateFile_full() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] file[0]: "/home/me/my.sub/test/obj.TAR.GZ"
            "/home/me/my.sub/test/obj_tmp",
            // [2] file[1]: "smb://myhost/my.sub/test/obj.TAR.gz"
            "smb://myhost/my.sub/test/obj_tmp",
            // [3] file[2]: "C:\\my.sub\\test\\obj.tar.GZ"
            "C:\\my.sub\\test\\obj_tmp",
            // [4] file[3]: "\\\\myhost\\my.sub\\test\\obj.tar.gz"
            "\\\\myhost\\my.sub\\test\\obj_tmp",
            // [5] file[4]: "/home/me/my/test/obj-5.2.9.tar2.gz"
            "/home/me/my/test/obj-5.2.9_tmp"),
        // file
        FILES);
  }

  static Stream<Arguments> cognateFile_notFull() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] file[0]: "/home/me/my.sub/test/obj.TAR.GZ"
            "/home/me/my.sub/test/obj.TAR_tmp",
            // [2] file[1]: "smb://myhost/my.sub/test/obj.TAR.gz"
            "smb://myhost/my.sub/test/obj.TAR_tmp",
            // [3] file[2]: "C:\\my.sub\\test\\obj.tar.GZ"
            "C:\\my.sub\\test\\obj.tar_tmp",
            // [4] file[3]: "\\\\myhost\\my.sub\\test\\obj.tar.gz"
            "\\\\myhost\\my.sub\\test\\obj.tar_tmp",
            // [5] file[4]: "/home/me/my/test/obj-5.2.9.tar2.gz"
            "/home/me/my/test/obj-5.2.9.tar2_tmp"),
        // file
        FILES);
  }

  static Stream<Arguments> extension_full() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] file[0]: "/home/me/my.sub/test/obj.TAR.GZ"
            ".TAR.GZ",
            // [2] file[1]: "smb://myhost/my.sub/test/obj.TAR.gz"
            ".TAR.gz",
            // [3] file[2]: "C:\\my.sub\\test\\obj.tar.GZ"
            ".tar.GZ",
            // [4] file[3]: "\\\\myhost\\my.sub\\test\\obj.tar.gz"
            ".tar.gz",
            // [5] file[4]: "/home/me/my/test/obj-5.2.9.tar2.gz"
            ".tar2.gz"),
        // file
        FILES);
  }

  static Stream<Arguments> extension_notFull() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] file[0]: "/home/me/my.sub/test/obj.TAR.GZ"
            ".GZ",
            // [2] file[1]: "smb://myhost/my.sub/test/obj.TAR.gz"
            ".gz",
            // [3] file[2]: "C:\\my.sub\\test\\obj.tar.GZ"
            ".GZ",
            // [4] file[3]: "\\\\myhost\\my.sub\\test\\obj.tar.gz"
            ".gz",
            // [5] file[4]: "/home/me/my/test/obj-5.2.9.tar2.gz"
            ".gz"),
        // file
        FILES);
  }

  static Stream<Arguments> filename() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] path[0]: "/home/me/my.sub/test/obj.TAR.GZ"
            "obj.TAR.GZ",
            // [2] path[1]: "smb://myhost/my.sub/test/obj.TAR.gz"
            "obj.TAR.gz",
            // [3] path[2]: "C:\\my.sub\\test\\obj.tar.GZ"
            "obj.tar.GZ",
            // [4] path[3]: "\\\\myhost\\my.sub\\test\\obj.tar.gz"
            "obj.tar.gz",
            // [5] path[4]: "/home/me/my/test/obj-5.2.9.tar2.gz"
            "obj-5.2.9.tar2.gz"),
        // path
        FILES);
  }

  static Stream<Arguments> isExtension_full() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // file[0]: "/home/me/my.sub/test/obj.TAR.GZ"
            // [1] extension[0]: ".tar.gz"
            true,
            // [2] extension[1]: ".tar.GZ"
            true,
            // [3] extension[2]: ".gz"
            false,
            // [4] extension[3]: ".GZ"
            false,
            //
            // file[1]: "smb://myhost/my.sub/test/obj.TAR.gz"
            // [5] extension[0]: ".tar.gz"
            true,
            // [6] extension[1]: ".tar.GZ"
            true,
            // [7] extension[2]: ".gz"
            false,
            // [8] extension[3]: ".GZ"
            false,
            //
            // file[2]: "C:\\my.sub\\test\\obj.tar.GZ"
            // [9] extension[0]: ".tar.gz"
            true,
            // [10] extension[1]: ".tar.GZ"
            true,
            // [11] extension[2]: ".gz"
            false,
            // [12] extension[3]: ".GZ"
            false,
            //
            // file[3]: "\\\\myhost\\my.sub\\test\\obj.tar.gz"
            // [13] extension[0]: ".tar.gz"
            true,
            // [14] extension[1]: ".tar.GZ"
            true,
            // [15] extension[2]: ".gz"
            false,
            // [16] extension[3]: ".GZ"
            false,
            //
            // file[4]: "/home/me/my/test/obj-5.2.9.tar2.gz"
            // [17] extension[0]: ".tar.gz"
            false,
            // [18] extension[1]: ".tar.GZ"
            false,
            // [19] extension[2]: ".gz"
            false,
            // [20] extension[3]: ".GZ"
            false),
        // file
        FILES,
        // extension
        EXTENSIONS);
  }

  static Stream<Arguments> isExtension_notFull() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // file[0]: "/home/me/my.sub/test/obj.TAR.GZ"
            // [1] extension[0]: ".tar.gz"
            false,
            // [2] extension[1]: ".tar.GZ"
            false,
            // [3] extension[2]: ".gz"
            true,
            // [4] extension[3]: ".GZ"
            true,
            //
            // file[1]: "smb://myhost/my.sub/test/obj.TAR.gz"
            // [5] extension[0]: ".tar.gz"
            false,
            // [6] extension[1]: ".tar.GZ"
            false,
            // [7] extension[2]: ".gz"
            true,
            // [8] extension[3]: ".GZ"
            true,
            //
            // file[2]: "C:\\my.sub\\test\\obj.tar.GZ"
            // [9] extension[0]: ".tar.gz"
            false,
            // [10] extension[1]: ".tar.GZ"
            false,
            // [11] extension[2]: ".gz"
            true,
            // [12] extension[3]: ".GZ"
            true,
            //
            // file[3]: "\\\\myhost\\my.sub\\test\\obj.tar.gz"
            // [13] extension[0]: ".tar.gz"
            false,
            // [14] extension[1]: ".tar.GZ"
            false,
            // [15] extension[2]: ".gz"
            true,
            // [16] extension[3]: ".GZ"
            true,
            //
            // file[4]: "/home/me/my/test/obj-5.2.9.tar2.gz"
            // [17] extension[0]: ".tar.gz"
            false,
            // [18] extension[1]: ".tar.GZ"
            false,
            // [19] extension[2]: ".gz"
            true,
            // [20] extension[3]: ".GZ"
            true),
        // file
        FILES,
        // extension
        EXTENSIONS);
  }

  static Stream<Arguments> path__unix() {
    var fs = Jimfs.newFileSystem(Configuration.unix());
    //noinspection DataFlowIssue
    return argumentsStream(
        cartesian()
            .<String>composeExpectedConverter(fs::getPath),
        // expected
        asList(
            // uri[0]: "relative/uri.html"
            // [1] fs[0]: "com.google.common.jimfs.JimfsFileSystem@512abf25"
            "relative/uri.html",
            //
            // uri[1]: "../relative/uri.html"
            // [2] fs[0]: "com.google.common.jimfs.JimfsFileSystem@512abf25"
            "../relative/uri.html",
            //
            // uri[2]: "file:/absolute/local/uri.html"
            // [3] fs[0]: "com.google.common.jimfs.JimfsFileSystem@512abf25"
            "/absolute/local/uri.html",
            //
            // uri[3]: "file://host/absolute/local/uri.html"
            // [4] fs[0]: "com.google.common.jimfs.JimfsFileSystem@512abf25"
            "/host/absolute/local/uri.html"),
        // uri
        List.of(
            "relative/uri.html",
            "../relative/uri.html",
            "file:/absolute/local/uri.html",
            "file://host/absolute/local/uri.html"),
        // fs
        List.of(fs));
  }

  static Stream<Arguments> path__win() {
    var fs = Jimfs.newFileSystem(Configuration.windows());
    //noinspection DataFlowIssue
    return argumentsStream(
        cartesian()
            .<String>composeExpectedConverter(fs::getPath),
        // expected
        asList(
            // uri[0]: "relative/uri.html"
            // [1] fs[0]: "com.google.common.jimfs.JimfsFileSystem@281b2dfd"
            "relative\\uri.html",
            //
            // uri[1]: "../relative/uri.html"
            // [2] fs[0]: "com.google.common.jimfs.JimfsFileSystem@281b2dfd"
            "..\\relative\\uri.html",
            //
            // uri[2]: "file:///c:/absolute/local/uri.html"
            // [3] fs[0]: "com.google.common.jimfs.JimfsFileSystem@281b2dfd"
            "c:\\absolute\\local\\uri.html",
            //
            // uri[3]: "file://host/absolute/uri.html"
            // [4] fs[0]: "com.google.common.jimfs.JimfsFileSystem@281b2dfd"
            "\\\\host\\absolute\\uri.html"),
        // uri
        List.of(
            "relative/uri.html",
            "../relative/uri.html",
            "file:///c:/absolute/local/uri.html",
            "file://host/absolute/uri.html"),
        // fs
        List.of(fs));
  }

  @ParameterizedTest
  @MethodSource
  void baseName_full(Expected<String> expected, String file) {
    assertParameterizedOf(
        () -> Files.baseName(file, true),
        expected,
        () -> new ExpectedGeneration(file));
  }

  @ParameterizedTest
  @MethodSource
  void cognateFile_full(Expected<String> expected, String file) {
    assertParameterizedOf(
        () -> Files.cognateFile(file, "_tmp", true),
        expected,
        () -> new ExpectedGeneration(file));
  }

  @ParameterizedTest
  @MethodSource
  void cognateFile_notFull(Expected<String> expected, String file) {
    assertParameterizedOf(
        () -> Files.cognateFile(file, "_tmp"),
        expected,
        () -> new ExpectedGeneration(file));
  }

  @ParameterizedTest
  @MethodSource
  void extension_full(Expected<String> expected, String file) {
    assertParameterizedOf(
        () -> Files.extension(file, true),
        expected,
        () -> new ExpectedGeneration(file));
  }

  @ParameterizedTest
  @MethodSource
  void extension_notFull(Expected<String> expected, String file) {
    assertParameterizedOf(
        () -> Files.extension(file),
        expected,
        () -> new ExpectedGeneration(file));
  }

  @ParameterizedTest
  @MethodSource
  void filename(Expected<String> expected, String path) {
    assertParameterizedOf(
        () -> Files.filename(path),
        expected,
        () -> new ExpectedGeneration(path));
  }

  @ParameterizedTest
  @MethodSource
  void isExtension_full(Expected<Boolean> expected, String file, String extension) {
    assertParameterizedOf(
        () -> Files.isExtension(file, extension, true),
        expected,
        () -> new ExpectedGeneration(file, extension));
  }

  @ParameterizedTest
  @MethodSource
  void isExtension_notFull(Expected<Boolean> expected, String file, String extension) {
    assertParameterizedOf(
        () -> Files.isExtension(file, extension),
        expected,
        () -> new ExpectedGeneration(file, extension));
  }

  @ParameterizedTest(autoCloseArguments = false)
  @MethodSource
  void path__unix(Expected<Path> expected, URI uri, FileSystem fs) {
    assertParameterizedOf(
        () -> Files.path(uri, fs),
        expected,
        () -> new ExpectedGeneration(uri, fs)
            .setMaxArgCommentLength(50));
  }

  @ParameterizedTest(autoCloseArguments = false)
  @MethodSource
  void path__win(Expected<Path> expected, URI uri, FileSystem fs) {
    assertParameterizedOf(
        () -> Files.path(uri, fs),
        expected,
        () -> new ExpectedGeneration(uri, fs)
            .setMaxArgCommentLength(50));
  }
}
