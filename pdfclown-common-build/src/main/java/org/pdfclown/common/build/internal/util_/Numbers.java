/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Numbers.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_;

import static org.pdfclown.common.build.internal.util_.Exceptions.unexpected;

import java.math.BigDecimal;

/**
 * Number utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Numbers {
  private static final String[] ROMAN_DIGITS = {
      "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };
  private static final int[] ROMAN_VALUES = {
      1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };

  // SPDX-SnippetBegin
  // SPDX-SnippetCopyrightText: © 2015-2022 Daniel Fickling, 2015 Patrick Wright
  // SPDX-License-Identifier: LGPL-3.0-only
  //
  // Source: https://github.com/danfickle/openhtmltopdf/blob/780ba564839f1ad5abfa5df12e4aebb9dd6782d2/openhtmltopdf-core/src/main/java/com/openhtmltopdf/layout/CounterLanguage.java#L18
  // SourceName: com.openhtmltopdf.layout.CounterLanguage.toLatin
  // Changes: adaptation to pdfClown
  /**
   * Converts the number to latin-alphabet numeral.
   *
   * @return (A to Z for the first 26 pages, AA to ZZ for the next 26, etc.)
   */
  public static String intToLatin(int val) {
    var b = new StringBuilder();
    val -= 1;
    while (val >= 0) {
      int letter = val % 26;
      val = val / 26 - 1;
      b.insert(0, ((char) (letter + 65)));
    }
    return b.toString();
  }
  // SPDX-SnippetEnd

  // SPDX-SnippetBegin
  // SPDX-SnippetCopyrightText: © 2015-2022 Daniel Fickling, 2015 Patrick Wright
  // SPDX-License-Identifier: LGPL-3.0-only
  //
  // Source: https://github.com/danfickle/openhtmltopdf/blob/780ba564839f1ad5abfa5df12e4aebb9dd6782d2/openhtmltopdf-core/src/main/java/com/openhtmltopdf/layout/CounterLanguage.java#L4
  // SourceName: com.openhtmltopdf.layout.CounterLanguage.toRoman
  // Changes: adaptation to pdfClown
  /**
   * Converts the number to roman numeral.
   *
   * @return (I, II, III, IV, V, VI, ..., IX, X, etc.)
   */
  public static String intToRoman(int val) {
    var b = new StringBuilder();
    for (int i = 0; i < ROMAN_VALUES.length; i++) {
      int count = val / ROMAN_VALUES[i];
      //noinspection StringRepeatCanBeUsed
      for (int j = 0; j < count; j++) {
        b.append(ROMAN_DIGITS[i]);
      }
      val -= ROMAN_VALUES[i] * count;
    }
    return b.toString();
  }
  // SPDX-SnippetEnd

  /**
   * Converts the value to the type.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Number> T to(Number value, Class<T> type) {
    if (type == value.getClass())
      return (T) value;
    else if (type == Integer.class)
      return (T) (Object) value.intValue();
    else if (type == Long.class)
      return (T) (Object) value.longValue();
    else if (type == Float.class)
      return (T) (Object) value.floatValue();
    else if (type == Double.class)
      return (T) (Object) value.doubleValue();
    else if (type == Short.class)
      return (T) (Object) value.shortValue();
    else if (type == Byte.class)
      return (T) (Object) value.byteValue();
    else if (type == BigDecimal.class)
      return (T) BigDecimal.valueOf(value.doubleValue());
    else
      throw unexpected(type);
  }

  private Numbers() {
  }
}
