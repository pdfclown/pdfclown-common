/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (MatchesText.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion.match;

import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.pdfclown.common.build.internal.util_.Strings.ELLIPSIS;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsEqual;

/**
 * Matches arbitrarily large strings.
 * <p>
 * In case of mismatch, shows only the text fragment where the first difference occurred — contrary
 * to standard {@link IsEqual is(equalTo(..))} matcher, which shows the text in full.
 * </p>
 *
 * @author Stefano Chizzolini
 * @see MatchesTextFile
 */
public class MatchesText extends TypeSafeMatcher<String> {
  private static final int CHUNK_LENGTH__MAX = 80;

  private final boolean caseIgnored;
  private final String expected;
  private int line = 1;
  private int lineStart;
  private int column = 1;

  /**
  */
  public MatchesText(String expected, boolean caseIgnored) {
    this.expected = requireNonNull(expected, "`expected`");
    this.caseIgnored = caseIgnored;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("matches ").appendValue(abbreviate(expected, CHUNK_LENGTH__MAX));
  }

  @Override
  protected void describeMismatchSafely(String item, Description description) {
    int offset = column;
    String prefix = EMPTY;
    if (column > CHUNK_LENGTH__MAX) {
      offset = CHUNK_LENGTH__MAX;
      prefix = ELLIPSIS;
    }
    int chunkStart = lineStart + column - offset;
    int chunkEnd = chunkStart + offset + 30;
    String suffix = ELLIPSIS;
    {
      int lineEnd = item.indexOf('\n', chunkStart);
      if (chunkEnd > lineEnd) {
        chunkEnd = lineEnd;
        suffix = EMPTY;
      }
    }
    int expectedChunkEnd = chunkStart + offset + 30;
    String expectedSuffix = ELLIPSIS;
    {
      int expectedLineEnd = expected.indexOf('\n', chunkStart);
      if (expectedChunkEnd > expectedLineEnd) {
        expectedChunkEnd = expectedLineEnd;
        expectedSuffix = EMPTY;
      }
    }
    description.appendText("differed at line ")
        .appendText(Integer.toString(line)).appendText(", column ")
        .appendText(Integer.toString(column)).appendText(":\n")
        .appendText(prefix).appendText(item.substring(chunkStart, chunkEnd)).appendText(suffix)
        .appendText("\n").appendText(leftPad("^", prefix.length() + offset))
        .appendText("\n  instead of:\n")
        .appendText(prefix).appendText(expected.substring(chunkStart, expectedChunkEnd))
        .appendText(expectedSuffix);
  }

  @Override
  protected boolean matchesSafely(String item) {
    for (int i = 0, l = min(item.length(), expected.length()); i < l; i++) {
      char actualChar = item.charAt(i);
      char expectedChar = expected.charAt(i);
      if (caseIgnored) {
        actualChar = Character.toLowerCase(actualChar);
        expectedChar = Character.toLowerCase(expectedChar);
      }
      if (actualChar != expectedChar)
        return false;

      if (actualChar == '\n') {
        line++;
        lineStart += column;
        column = 1;
      } else {
        column++;
      }
    }
    return item.length() == expected.length();
  }
}
