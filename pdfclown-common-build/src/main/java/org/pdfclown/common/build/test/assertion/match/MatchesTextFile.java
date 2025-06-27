/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (MatchesTextFile.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion.match;

import static java.nio.file.Files.readString;
import static org.pdfclown.common.build.internal.util_.Strings.S;
import static org.pdfclown.common.build.internal.util_.Strings.SPACE;

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
 * @see MatchesText
 */
public class MatchesTextFile extends TypeSafeMatcher<Path> {
  @SuppressWarnings("NotNullFieldNotInitialized")
  private /* @InitNonNull */ String actual;
  private final Path expectedContentPath;
  private final MatchesText matcher;

  /**
  */
  public MatchesTextFile(Path expectedContentPath, boolean caseIgnored) {
    try {
      matcher = new MatchesText(readString(this.expectedContentPath = expectedContentPath),
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
