/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FilesTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.util.Arrays.asList;
import static org.pdfclown.common.build.test.assertion.Verifiers.COMBINATION;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class FilesTest extends BaseTest {
  private static final List<String> EXTENSIONS = asList(
      // Multi-part, normal
      ".tar.gz",
      // Multi-part, alt-case
      ".tar.GZ",
      // Simple, normal
      ".gz",
      // Simple, alt-case
      ".GZ");

  private static final List<String> FILES = asList(
      // Unix path, dot inside directory, multi-part file extension
      "/home/me/my.sub/test/obj.TAR.GZ",
      // URI path, dot inside directory, multi-part file extension
      "smb://myhost/my.sub/test/obj.TAR.gz",
      // Windows DOS path, dot inside directory, multi-part file extension
      "C:\\my.sub\\test\\obj.tar.GZ",
      // Windows UNC path, dot inside directory, multi-part file extension
      "\\\\myhost\\my.sub\\test\\obj.tar.gz",
      // Dot inside base filename, multi-part file extension
      "/home/me/my/test/obj-5.2.9.tar2.gz");

  @Test
  void baseName_full() {
    COMBINATION.verify(
        (file) -> Files.baseName(file, true),
        List.of("file"),
        // file
        FILES);
  }

  @Test
  void cognateFile_full() {
    COMBINATION.verify(
        (file) -> Files.cognateFile(file, "_tmp", true),
        List.of("file"),
        // file
        FILES);
  }

  @Test
  void cognateFile_notFull() {
    COMBINATION.verify(
        (file) -> Files.cognateFile(file, "_tmp"),
        List.of("file"),
        // file
        FILES);
  }

  @Test
  void extension_full() {
    COMBINATION.verify(
        (file) -> Files.extension(file, true),
        List.of("file"),
        // file
        FILES);
  }

  @Test
  void extension_notFull() {
    COMBINATION.verify(
        (file) -> Files.extension(file),
        List.of("file"),
        // file
        FILES);
  }

  @Test
  void filename() {
    COMBINATION.verify(
        (path) -> Files.filename(path),
        List.of("path"),
        // path
        FILES);
  }

  @Test
  void isExtension_full() {
    COMBINATION.verify(
        (file, extension) -> Files.isExtension(file, extension, true),
        List.of("file", "extension"),
        // file
        FILES,
        // extension
        EXTENSIONS);
  }

  @Test
  void isExtension_notFull() {
    COMBINATION.verify(
        (file, extension) -> Files.isExtension(file, extension),
        List.of("file", "extension"),
        // file
        FILES,
        // extension
        EXTENSIONS);
  }

  @Test
  void path__unix() throws IOException {
    try (var fs = Jimfs.newFileSystem(Configuration.unix())) {
      COMBINATION.verify(
          (uri) -> Files.path(URI.create(uri), fs),
          List.of("uri"),
          // uri
          asList(
              "relative/uri.html",
              "../relative/uri.html",
              "file:/absolute/local/uri.html",
              "file://host/absolute/local/uri.html"));
    }
  }

  @Test
  void path__win() throws IOException {
    try (var fs = Jimfs.newFileSystem(Configuration.windows())) {
      COMBINATION.verify(
          (uri) -> Files.path(URI.create(uri), fs),
          List.of("uri"),
          // uri
          asList(
              "relative/uri.html",
              "../relative/uri.html",
              "file:///c:/absolute/local/uri.html",
              "file://host/absolute/uri.html"));
    }
  }
}
