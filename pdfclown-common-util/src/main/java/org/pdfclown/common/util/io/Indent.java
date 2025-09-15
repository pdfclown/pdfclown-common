/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Indent.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0

  Source: https://github.com/talsma-ict/umldoclet/blob/6a7b2e09126b38e1c49103ba85754c7fdfb01db4/src/main/java/nl/talsmasoftware/umldoclet/rendering/indent/Indentation.java

  Changes: Indentations at arbitrary level can be obtained via `withLevel`.
 */
package org.pdfclown.common.util.io;

import static java.lang.Math.max;
import static org.pdfclown.common.util.Objects.isSameType;
import static org.pdfclown.common.util.Strings.SPACE;
import static org.pdfclown.common.util.Strings.TAB;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import org.pdfclown.common.util.annot.Immutable;

// SourceName: nl.talsmasoftware.umldoclet.rendering.indent.Indentation
/**
 * Textual indentation.
 * <p>
 * Its {@link #getLevel() level} defines how many times an indentation step (defined by a
 * {@link #getSymbol() symbol} repeated to fill its {@link #getWidth() width}) is repeated to
 * generate the {@linkplain #toString() indentation string}.
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation to pdfclown-common-util)
 */
@Immutable
public final class Indent implements CharSequence, Serializable {
  private static final long serialVersionUID = 1L;

  /*
   * Caching of first 5 instances of 2, 4 spaces, tabs indentations.
   */
  private static final Indent[] TWO_SPACES = new Indent[5];
  private static final Indent[] FOUR_SPACES = new Indent[5];
  private static final Indent[] TABS = new Indent[5];
  static {
    for (int i = 0; i < TWO_SPACES.length; i++) {
      TWO_SPACES[i] = new Indent(2, SPACE, i);
    }
    for (int i = 0; i < FOUR_SPACES.length; i++) {
      FOUR_SPACES[i] = new Indent(4, SPACE, i);
    }
    for (int i = 0; i < TABS.length; i++) {
      TABS[i] = new Indent(1, TAB, i);
    }
  }

  /**
   * Default indentation (4 spaces, level 0).
   */
  public static final Indent DEFAULT = FOUR_SPACES[0];

  /**
   * No indentation.
   * <p>
   * Calls to {@link #increase()} or {@link #decrease()} have no effect.
   * </p>
   */
  public static final Indent NONE = new Indent(0, SPACE, 0);

  /**
   * Gets the space indentation corresponding to the coordinates.
   *
   * @param width
   *          Indentation step length.
   * @param level
   *          Indentation level.
   */
  public static Indent spaces(int width, int level) {
    return width < 0 ? spaces(DEFAULT.symbol == SPACE ? DEFAULT.width : 4, level)
        : width == 0 ? NONE
        : width == 2 && level < TWO_SPACES.length ? TWO_SPACES[max(level, 0)]
        : width == 4 && level < FOUR_SPACES.length ? FOUR_SPACES[max(level, 0)]
        : new Indent(width, SPACE, level);
  }

  /**
   * Gets the tab indentation corresponding to the level.
   *
   * @param level
   *          Indentation level.
   */
  public static Indent tabs(final int level) {
    return level < TABS.length ? TABS[max(level, 0)] : new Indent(1, TAB, level);
  }

  /**
   * Gets the indentation corresponding to the coordinates, reusing cached instances whenever
   * available.
   */
  private static Indent resolve(final int width, final char symbol, final int level) {
    return width == 0 ? NONE
        : symbol == SPACE ? spaces(width, level)
        : symbol == TAB && width == 1 ? tabs(level)
        : new Indent(width, symbol, level);
  }

  private final int level;
  // SourceName: ch
  private final char symbol;
  private final int width;

  private final transient String value;

  private Indent(final int width, final char symbol, final int level) {
    this.width = max(width, 0);
    this.level = max(level, 0);
    this.symbol = symbol;

    {
      var buf = new char[this.width * this.level];
      Arrays.fill(buf, this.symbol);
      this.value = String.valueOf(buf);
    }
  }

  @Override
  public char charAt(int index) {
    return value.charAt(index);
  }

  /**
   * Gets an indentation with decreased level.
   */
  public Indent decrease() {
    return withLevel(level - 1);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    else if (!isSameType(this, o))
      return false;

    var that = (Indent) o;
    return this.width == that.width
        && this.symbol == that.symbol
        && this.level == that.level;
  }

  /**
   * How many steps this indentation is made of.
   */
  public int getLevel() {
    return level;
  }

  /**
   * Character used to fill this indentation.
   */
  public char getSymbol() {
    return symbol;
  }

  /**
   * How many {@link #getSymbol() symbol}s an indentation step is made of.
   */
  public int getWidth() {
    return width;
  }

  @Override
  public int hashCode() {
    return Objects.hash(width, symbol, level);
  }

  /**
   * Gets an indentation with increased level.
   */
  public Indent increase() {
    return withLevel(level + 1);
  }

  @Override
  public int length() {
    return value.length();
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return value.substring(start, end);
  }

  /**
   * Gets this indentation as a string.
   */
  @Override
  public String toString() {
    return value;
  }

  /**
   * Gets an indentation with the level.
   */
  public Indent withLevel(int value) {
    if (value < 0) {
      value = 0;
    }
    return value == level ? this : resolve(width, symbol, value);
  }

  /**
   * Makes sure that, after deserialization, cached instances are resolved whenever possible.
   *
   * @see <a href=
   *      "https://docs.oracle.com/en/java/javase/11/docs/specs/serialization/input.html#the-readresolve-method">The
   *      readResolve Method</a>
   */
  private Object readResolve() {
    return resolve(width, symbol, level);
  }
}
