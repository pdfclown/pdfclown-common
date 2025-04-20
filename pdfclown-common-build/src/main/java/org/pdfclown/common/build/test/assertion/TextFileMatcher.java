/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (TextFileMatcher.java) is part of pdfclown-common-build module in pdfClown Common
  project (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.build.test.assertion;

import static java.nio.file.Files.readString;
import static org.pdfclown.common.build.internal.util.Strings.S;
import static org.pdfclown.common.build.internal.util.Strings.SPACE;

import java.io.IOException;
import java.nio.file.Path;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matches text file contents.
 * <p>
 * In case of mismatch, shows only the text fragment where the first difference occurred.
 * </p>
 *
 * @author Stefano Chizzolini
 * @see TextMatcher
 */
public class TextFileMatcher extends TypeSafeMatcher<Path> {
  public static TextFileMatcher matchesFileContent(Path expectedContentPath) {
    return new TextFileMatcher(expectedContentPath, false);
  }

  public static TextFileMatcher matchesFileContentIgnoreCase(Path expectedContentPath) {
    return new TextFileMatcher(expectedContentPath, true);
  }

  private String actual;
  private Path expectedContentPath;
  private TextMatcher matcher;

  protected TextFileMatcher(Path expectedContentPath, boolean caseIgnored) {
    try {
      matcher = new TextMatcher(readString(this.expectedContentPath = expectedContentPath),
          caseIgnored);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("file content matches ").appendValue(expectedContentPath);
  }

  @Override
  protected void describeMismatchSafely(Path item, Description description) {
    description.appendValue(item).appendText(S + SPACE);
    matcher.describeMismatch(actual, description);
  }

  @Override
  protected boolean matchesSafely(Path item) {
    try {
      return matcher.matches(actual = readString(item));
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
