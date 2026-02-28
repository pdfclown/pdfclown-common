/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PathEndsWith.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion.match;

import java.nio.file.Path;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matches when the examined path ends with the given one.
 *
 * @author Stefano Chizzolini
 */
public class PathEndsWith extends TypeSafeMatcher<Path> {
  /**
   * Creates a {@link PathEndsWith} matcher.
   */
  public static PathEndsWith pathEndsWith(Path path) {
    return new PathEndsWith(path);
  }

  private final Path expected;

  public PathEndsWith(Path expected) {
    this.expected = expected;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("a path ending with ").appendValue(expected);
  }

  @Override
  protected boolean matchesSafely(Path item) {
    return item.endsWith(expected);
  }
}
