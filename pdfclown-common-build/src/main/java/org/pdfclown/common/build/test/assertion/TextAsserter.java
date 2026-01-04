/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TextAsserter.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Automated text assertions for integration testing.
 * <p>
 * This class enables checks over text (actual content) against a resource (expected content) which
 * can be {@linkplain Asserter#PARAM_NAME__UPDATE automatically updated}.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class TextAsserter extends ContentAsserter<String> {
  /**
   * Asserts that a file matches the expected one.
   *
   * @param expectedResourceName
   *          Resource name of the expected file.
   * @param actualFile
   *          Actual file.
   * @param config
   *          Assertion configuration.
   * @throws AssertionError
   *           if {@code actualFile} doesn't match the content of {@code expectedResourceName}.
   * @see Asserter#PARAM_NAME__UPDATE
   */
  public void assertEquals(String expectedResourceName, Path actualFile, Config config) {
    doAssertEquals(expectedResourceName, actualFile, config);
  }

  /**
   * Asserts that a content matches the expected one.
   *
   * @param expectedResourceName
   *          Resource name of the expected content.
   * @param actualContent
   *          Actual content.
   * @param config
   *          Assertion configuration.
   * @throws AssertionError
   *           if {@code actualContent} doesn't match the content of {@code expectedResourceName}.
   * @see Asserter#PARAM_NAME__UPDATE
   */
  public void assertEquals(String expectedResourceName, String actualContent, Config config) {
    doAssertEquals(expectedResourceName, actualContent, config);
  }

  @Override
  protected void doAssertEquals(String expectedContent, String actualContent) {
    assertArrayEquals(expectedContent.toCharArray(), actualContent.toCharArray());
  }

  @Override
  protected String readContent(Path file) throws IOException {
    return doReadStringContent(file);
  }

  @Override
  protected void writeContent(Path file, String content) throws IOException {
    doWriteStringContent(file, content);
  }
}
