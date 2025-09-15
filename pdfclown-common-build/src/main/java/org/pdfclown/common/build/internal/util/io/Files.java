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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.pdfclown.common.build.internal.util_.io.Files.FILE_EXTENSION__ZIP;
import static org.pdfclown.common.build.internal.util_.io.Files.baseName;
import static org.pdfclown.common.build.internal.util_.io.Files.isExtension;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.jspecify.annotations.Nullable;

/**
 * File utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Files {
  /**
   * Gets the content of a (either plain or ZIP-compressed) file as string.
   * <p>
   * This is an extension to {@link java.nio.file.Files#readString(java.nio.file.Path)}.
   * </p>
   * <p>
   * NOTE: In case of compressed archive, this method would access only its first entry; in case of
   * multi-entry archive, call {@link #readString(Path, Predicate)} instead.
   * </p>
   */
  public static @Nullable String readString(Path file) throws IOException {
    return readString(file, null);
  }

  /**
   * Gets the content of a (either plain or ZIP-compressed) file as string.
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
  public static @Nullable String readString(Path file, @Nullable Predicate<String> filter)
      throws IOException {
    if (isExtension(file.getFileName().toString(), FILE_EXTENSION__ZIP)) {
      if (filter == null) {
        filter = $ -> true;
      }
      try (var in = new ZipInputStream(new FileInputStream(file.toFile()))) {
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
              return out.toString(UTF_8);
            }
          }
        }
      }
      return null;
    } else
      return java.nio.file.Files.readString(file);
  }

  /**
   * Writes a data string to file.
   *
   * @param file
   *          Target file
   *          ({@value org.pdfclown.common.build.internal.util_.io.Files#FILE_EXTENSION__ZIP}
   *          extension causes data to compress).
   */
  public static void writeString(Path file, String data) throws IOException {
    String filename = file.getFileName().toString();
    if (isExtension(filename, FILE_EXTENSION__ZIP)) {
      try (var out = new ZipOutputStream(new FileOutputStream(file.toFile()))) {
        out.putNextEntry(new ZipEntry(baseName(filename)));
        out.write(data.getBytes(UTF_8));
      }
    } else {
      java.nio.file.Files.writeString(file, data);
    }
  }

  private Files() {
  }
}
