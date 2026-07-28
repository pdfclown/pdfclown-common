/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TextPosition.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.temp.util.text;

import static java.lang.Math.max;
import static org.pdfclown.common.build.internal.temp.util.Exceptions.wrongArg;

import org.pdfclown.common.util.annot.Immutable;

/**
 * Position in a bi-dimensional text space defined by line terminators.
 *
 * @author Stefano Chizzolini
 */
@Immutable
public final class TextPosition implements Comparable<TextPosition> {
  private static final int COORDINATE__UNDEFINED = 0;
  private static final int OFFSET__UNDEFINED = -1;

  private static final TextPosition ABSENT = new TextPosition(
      OFFSET__UNDEFINED, COORDINATE__UNDEFINED, COORDINATE__UNDEFINED);

  /**
   * Undefined position.
   */
  public static TextPosition absent() {
    return ABSENT;
  }

  /**
   * Position defined according to the given coordinates.
   *
   * @param offset
   *          ({@code 0}-based) Offset (if outside this range, it is undefined).
   * @param line
   *          ({@code 1}-based) Vertical position (if outside this range, it is undefined, and
   *          {@code column} MUST be so, as well).
   * @param column
   *          ({@code 1}-based) Horizontal position (if outside this range, it is undefined, and
   *          {@code line} MUST be so, as well).
   * @return {@link #absent()}, if all the coordinates are undefined.
   * @throws IllegalArgumentException
   *           if {@code line} and {@code column} are mutually inconsistent (they MUST be both
   *           either {@code 1}-based (defined coordinates) or below {@code 1} (undefined
   *           coordinates)).
   */
  public static TextPosition of(int offset, int line, int column) {
    offset = max(offset, OFFSET__UNDEFINED);
    if (line <= COORDINATE__UNDEFINED && column <= COORDINATE__UNDEFINED) {
      if (offset == OFFSET__UNDEFINED)
        return ABSENT;

      line = COORDINATE__UNDEFINED;
      column = COORDINATE__UNDEFINED;
    } else if (line <= COORDINATE__UNDEFINED || column <= COORDINATE__UNDEFINED)
      throw wrongArg(null, null, "Both `line` and `column` MUST be 1-based, or undefined");

    return new TextPosition(offset, line, column);
  }

  private final int column;
  private final int line;

  private final int offset;

  private TextPosition(int offset, int line, int column) {
    this.offset = offset;
    this.line = line;
    this.column = column;
  }

  @Override
  public int compareTo(TextPosition o) {
    // Compare by `offset` if defined, or if coordinates are undefined themselves!
    if (this.offset > OFFSET__UNDEFINED || this.line == COORDINATE__UNDEFINED)
      return Integer.compare(this.offset, o.offset);
    else {
      int cmp;
      return (cmp = Integer.compare(this.line, o.line)) != 0 ? cmp
          : Integer.compare(this.column, o.column);
    }
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof TextPosition that
        && this.offset == that.offset
        && this.line == that.line
        && this.column == that.column);
  }

  @Override
  public int hashCode() {
    int ret = offset;
    ret = 31 * ret + line;
    ret = 31 * ret + column;
    return ret;
  }

  /**
   * Whether this position is defined.
   */
  @SuppressWarnings("ReferenceEquality")
  public boolean isPresent() {
    return this != ABSENT;
  }

  @Override
  public String toString() {
    return line > COORDINATE__UNDEFINED ? "line %s, column %s".formatted(line, column)
        : "position " + (offset > OFFSET__UNDEFINED ? offset : "UNDEFINED");
  }
}
