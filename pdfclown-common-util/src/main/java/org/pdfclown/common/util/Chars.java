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
  public static final char ANGLE_BRACKET_CLOSE = '>';
  public static final char ANGLE_BRACKET_OPEN = '<';
  public static final char APOSTROPHE = '\'';
  public static final char BACKSLASH = '\\';
  public static final char BACKTICK = '`';
  public static final char COLON = ':';
  public static final char COMMA = ',';
  /**
   * Carriage-return character.
   */
  public static final char CR = '\r';
  public static final char CURLY_BRACE_CLOSE = '}';
  public static final char CURLY_BRACE_OPEN = '{';
  public static final char DOLLAR = '$';
  public static final char DOT = '.';
  /**
   * Double quote (aka quotation mark).
   */
  public static final char DQUOTE = '\"';
  public static final char HASH = '#';
  public static final char HYPHEN = '-';
  /**
   * Greater-than character.
   */
  public static final char GT = ANGLE_BRACKET_CLOSE;
  /**
   * Line-feed character.
   */
  public static final char LF = '\n';
  /**
   * Underscore character.
   */
  public static final char LOW_LINE = '_';
  /**
   * Less-than character.
   */
  public static final char LT = ANGLE_BRACKET_OPEN;
  public static final char MINUS = HYPHEN;
  /**
   * <a href="https://en.wikipedia.org/wiki/Non-breaking_space">Non-breaking space</a> (aka hard
   * space) character preventing automatic line break at its position. In some formats, including
   * HTML, it also prevents consecutive whitespace characters from collapsing into a single space.
   */
  public static final char NBSP = 160;
  public static final char PERCENT = '%';
  /**
   * Vertical bar.
   */
  public static final char PIPE = '|';
  public static final char PLUS = '+';
  public static final char ROUND_BRACKET_CLOSE = ')';
  public static final char ROUND_BRACKET_OPEN = '(';
  public static final char SEMICOLON = ';';
  public static final char SLASH = '/';
  public static final char SOFT_HYPHEN = '\u00ad';
  public static final char SPACE = ' ';
  public static final char SQUARE_BRACKET_CLOSE = ']';
  public static final char SQUARE_BRACKET_OPEN = '[';
  /**
   * Single quote (aka apostrophe).
   */
  public static final char SQUOTE = APOSTROPHE;
  public static final char STAR = '*';
  public static final char TAB = '\t';
  public static final char UNDERSCORE = LOW_LINE;

  private Chars() {
  }
}
