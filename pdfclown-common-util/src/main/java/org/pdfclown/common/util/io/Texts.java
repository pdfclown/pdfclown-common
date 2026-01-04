/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Texts.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static org.pdfclown.common.util.Objects.opt;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text-related I/O utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Texts {
  /**
   * Replaces each substring in a text file that matches the given {@linkplain Pattern regular
   * expression} with the replacement.
   *
   * @return Whether matches were found.
   */
  public static boolean replaceText(Path file, String regex, String replacement)
      throws IOException {
    String oldText = readString(file);
    Matcher m = Pattern.compile(regex).matcher(oldText);
    if (!m.find())
      return false;

    String newText = m.replaceAll(replacement);
    if (!newText.equals(oldText)) {
      writeString(file, newText);
    }
    return true;
  }

  /**
   * Gets the 1-based coordinates corresponding to a character offset in the stream.
   *
   * @param reader
   *          Text reader.
   * @param offset
   *          Character offset.
   */
  public static Optional<TextCoords> textCoords(Reader reader, int offset) throws IOException {
    try (var r = new LineNumberReader(reader)) {
      r.setLineNumber(1) /* 1-based line numbering */;
      int count = 0;
      int start = -1;
      int lastLineNumber = -1;
      while (r.read() != -1 && count < offset) {
        if (lastLineNumber != r.getLineNumber()) {
          start = count;
          lastLineNumber = r.getLineNumber();
        }
        count++;
      }
      return opt(count == offset ? new TextCoords(r.getLineNumber(), count - start) : null);
    }
  }

  private Texts() {
  }

  /**
   * Text coordinates.
   *
   * @author Stefano Chizzolini
   */
  public record TextCoords(int line, int column) {
  }
}
