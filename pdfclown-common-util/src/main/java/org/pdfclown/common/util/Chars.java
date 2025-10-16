/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Chars.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

/**
 * Character utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Chars {
  /**
   * {@code >}
   */
  public static final char ANGLE_BRACKET_CLOSE = '>';
  /**
   * {@code <}
   */
  public static final char ANGLE_BRACKET_OPEN = '<';
  /**
   * {@code '}
   */
  public static final char APOSTROPHE = '\'';
  /**
   * {@code \}
   */
  public static final char BACKSLASH = '\\';
  /**
   * {@code `}
   */
  public static final char BACKTICK = '`';
  /**
   * {@code :}
   */
  public static final char COLON = ':';
  /**
   * {@code ,}
   */
  public static final char COMMA = ',';
  /**
   * Carriage-return character.
   */
  public static final char CR = '\r';
  /**
   * <code>}</code>
   */
  public static final char CURLY_BRACE_CLOSE = '}';
  /**
   * <code>{</code>
   */
  public static final char CURLY_BRACE_OPEN = '{';
  /**
   * {@code $}
   */
  public static final char DOLLAR = '$';
  /**
   * {@code .}
   */
  public static final char DOT = '.';
  /**
   * Double quote ({@code "}).
   */
  public static final char DQUOTE = '\"';
  /**
   * {@code #}
   */
  public static final char HASH = '#';
  /**
   * {@code -}
   */
  public static final char HYPHEN = '-';
  /**
   * Greater-than ({@code >})
   */
  public static final char GT = ANGLE_BRACKET_CLOSE;
  /**
   * Line-feed character.
   */
  public static final char LF = '\n';
  /**
   * Underscore character ({@code _})
   */
  public static final char LOW_LINE = '_';
  /**
   * Less-than ({@code <})
   */
  public static final char LT = ANGLE_BRACKET_OPEN;
  /**
   * {@code -}
   */
  public static final char MINUS = HYPHEN;
  /**
   * <a href="https://en.wikipedia.org/wiki/Non-breaking_space">Non-breaking space</a> (aka hard
   * space) prevents automatic line break at its position. In some formats, including HTML, it also
   * prevents consecutive whitespace characters from collapsing into a single space.
   */
  public static final char NBSP = 160;
  /**
   * {@code %}
   */
  public static final char PERCENT = '%';
  /**
   * {@code |}
   */
  public static final char PIPE = '|';
  /**
   * {@code +}
   */
  public static final char PLUS = '+';
  /**
   * {@code )}
   */
  public static final char ROUND_BRACKET_CLOSE = ')';
  /**
   * {@code (}
   */
  public static final char ROUND_BRACKET_OPEN = '(';
  /**
   * {@code ;}
   */
  public static final char SEMICOLON = ';';
  /**
   * {@code /}
   */
  public static final char SLASH = '/';
  /**
   * <a href="https://en.wikipedia.org/wiki/Soft_hyphen">Soft hyphen</a> is a hint for text
   * rendering systems to break words across lines by inserting a visible hyphen if it falls on the
   * line end, otherwise it remains invisible within the line.
   */
  public static final char SOFT_HYPHEN = '\u00ad';
  /**
   * Space character.
   */
  public static final char SPACE = ' ';
  /**
   * {@code ]}
   */
  public static final char SQUARE_BRACKET_CLOSE = ']';
  /**
   * {@code [}
   */
  public static final char SQUARE_BRACKET_OPEN = '[';
  /**
   * Single quote ({@code '})
   */
  public static final char SQUOTE = APOSTROPHE;
  /**
   * {@code *}
   */
  public static final char STAR = '*';
  /**
   * Tab character.
   */
  public static final char TAB = '\t';
  /**
   * {@code _}
   */
  public static final char UNDERSCORE = LOW_LINE;

  private Chars() {
  }
}
