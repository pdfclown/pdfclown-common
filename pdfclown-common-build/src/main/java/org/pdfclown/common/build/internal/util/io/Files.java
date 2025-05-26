/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Files.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util.io;

import static org.pdfclown.common.build.internal.util.Strings.DOT;
import static org.pdfclown.common.build.internal.util.Strings.S;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.Nullable;

/**
 * File utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Files {
  // SourceFQN: org.pdfclown.common.util.io.Files.PATH_SUPER
  public static final String PATH_SUPER = S + DOT + DOT;

  /**
   * Full file extension pattern.
   * <p>
   * A greedy sequence of one or more extensions, each beginning with a dot, followed by a
   * non-digit, followed by one or more characters different from dot (extension separator), slash
   * (Unix directory separator) or back-slash (Windows directory separator).
   * </p>
   */
  private static final Pattern PATTERN__FULL_EXTENSION = Pattern.compile("(\\.\\D[^.\\\\/]+)+$");

  private static final String FILE_EXTENSION__ZIP = ".zip";

  // SourceFQN: org.pdfclown.common.util.io.Files.fullExtension(..)
  /**
   * Gets the full extension of the given path.
   * <p>
   * Any dot-prefixed tailing part which doesn't begin with a digit is included in the extension;
   * therefore, composite extensions (eg, ".tar.gz") are recognized, while version codes are ignored
   * (eg, "commons-io-2.8.0.jar" returns ".jar", NOT ".8.0.jar").
   * </p>
   *
   * @return Empty, if no extension.
   */
  public static String fullExtension(String path) {
    Matcher m = PATTERN__FULL_EXTENSION.matcher(path);
    return m.find() ? m.group() : "";
  }

  /**
   * Gets the content of the given (either plain or ZIP-compressed) file as string.
   * <p>
   * This is an extension to {@link java.nio.file.Files#readString(java.nio.file.Path)}.
   * </p>
   * <p>
   * NOTE: In case of compressed archive, this method would access only its first entry; in case of
   * multi-entry archive, call {@link #readString(File, Predicate)} instead.
   * </p>
   */
  public static @Nullable String readString(File file) throws IOException {
    return readString(file, null);
  }

  /**
   * Gets the content of the given (either plain or ZIP-compressed) file as string.
   * <p>
   * This is an extension to {@link java.nio.file.Files#readString(java.nio.file.Path)}.
   * </p>
   * <p>
   * NOTE: In case of compressed archive, this method would access only one entry, chosen via
   * {@code filter}.
   * </p>
   *
   * @param filter
   *          Entry filter (compressed file only; {@code null} for filtering the first entry).
   */
  public static @Nullable String readString(File file, @Nullable Predicate<String> filter)
      throws IOException {
    if (file.getPath().endsWith(FILE_EXTENSION__ZIP)) {
      if (filter == null) {
        filter = $ -> true;
      }
      try (var in = new ZipInputStream(new FileInputStream(file))) {
        while (true) {
          ZipEntry zipEntry = in.getNextEntry();
          if (zipEntry == null) {
            break;
          } else if (!zipEntry.isDirectory() && filter.test(zipEntry.getName())) {
            try (var out = new ByteArrayOutputStream()) {
              int len;
              var buf = new byte[1024];
              while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
              }
              return out.toString(StandardCharsets.UTF_8);
            }
          }
        }
      }
      return null;
    } else
      return java.nio.file.Files.readString(file.toPath());
  }

  // SourceFQN: org.pdfclown.common.util.io.Files.replaceFullExtension(..)
  /**
   * Replaces the {@linkplain #fullExtension(String) full extension} of the given path with the
   * given extension.
   */
  public static String replaceFullExtension(String path, String newExtension) {
    Matcher m = PATTERN__FULL_EXTENSION.matcher(path);
    return (m.find() ? path.substring(0, m.start()) : path) + newExtension;
  }

  /**
   * Cleans the given directory, or creates it if not existing.
   *
   * @return {@code dir}
   */
  public static File resetDir(File dir) throws IOException {
    if (dir.exists()) {
      FileUtils.cleanDirectory(dir);
    } else {
      //noinspection ResultOfMethodCallIgnored
      dir.mkdirs();
    }
    return dir;
  }

  /**
   * Writes the given data string to file.
   *
   * @param file
   *          Target file ({@value #FILE_EXTENSION__ZIP} extension causes data to compress).
   */
  public static void writeString(File file, String data) throws IOException {
    if (file.getName().endsWith(FILE_EXTENSION__ZIP)) {
      try (var out = new ZipOutputStream(new FileOutputStream(file))) {
        out.putNextEntry(new ZipEntry(
            file.getName().substring(0, file.getName().length() - FILE_EXTENSION__ZIP.length())));
        out.write(data.getBytes(StandardCharsets.UTF_8));
      }
    } else {
      java.nio.file.Files.writeString(file.toPath(), data);
    }
  }

  private Files() {
  }
}
