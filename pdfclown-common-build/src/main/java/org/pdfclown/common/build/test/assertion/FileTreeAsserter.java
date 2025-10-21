/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FileTreeAsserter.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static org.junit.jupiter.api.Assertions.fail;
import static org.pdfclown.common.build.internal.util_.Chars.LF;
import static org.pdfclown.common.build.internal.util_.Chars.SPACE;
import static org.pdfclown.common.build.internal.util_.Objects.sqnd;
import static org.pdfclown.common.build.internal.util_.Objects.textLiteral;
import static org.pdfclown.common.build.internal.util_.io.Files.diff;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import org.pdfclown.common.build.internal.util_.io.Files.Diff;
import org.pdfclown.common.build.internal.util_.io.Files.Diff.FileStatus;
import org.pdfclown.common.build.util.io.ResourceNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automated file tree assertions for integration testing.
 * <p>
 * This class enables massive checks over a directory of arbitrary depth (actual file tree) against
 * a resource (expected file tree) which can be automatically updated.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class FileTreeAsserter extends Asserter {
  private static final Logger log = LoggerFactory.getLogger(FileTreeAsserter.class);

  /**
   * Asserts that a file tree matches the expected one.
   *
   * @param expectedDirResourceName
   *          Resource name of the expected file tree.
   * @param actualDir
   *          Actual file tree.
   * @param config
   *          Assertion configuration.
   * @throws AssertionError
   *           if {@code actualDir} doesn't match the one at {@code expectedDirResourceName}.
   * @see Asserter#PARAM_NAME__UPDATE
   */
  public void assertEquals(final String expectedDirResourceName,
      final Path actualDir, final Config config) {
    final String expectedDirResourceFqn = ResourceNames.based(
        expectedDirResourceName, config.getTest(), true);
    final Path expectedDir = config.getEnv().resourcePath(expectedDirResourceFqn);

    final String testId = getTestId(config, () -> expectedDirResourceFqn);

    try {
      var built = false;
      while (true) {
        try {
          Diff diff = diff(expectedDir, actualDir);
          if (diff.isSame()) {
            break;
          }

          var b = new StringBuilder("Differences:");
          for (var file : diff.getDiffFiles()) {
            final var status = diff.status(file);
            b.append(LF).append(
                status == FileStatus.DIR1_ONLY ? "--"
                    : status == FileStatus.DIR2_ONLY ? "++"
                    : "!=")
                .append(SPACE).append(file);
          }
          fail(b.toString());
        } catch (AssertionError | FileNotFoundException ex) {
          // Unrecoverable?
          if (built || !isUpdatable()) {
            log.info("Test resource {}: unexpected actual file structure saved to {}"
                + " (expected file structure is at {})",
                textLiteral(expectedDirResourceFqn), textLiteral(actualDir),
                textLiteral(expectedDir));

            evalAssertionError(testId, ex.getMessage(), expectedDir, actualDir);
          }

          // Assertion resource rebuilding.
          {
            /*
             * NOTE: In case of explicit resource build request, the actual directory is saved into
             * the (either mismatching or missing) expected directory resource (at both source and
             * target locations).
             */
            built = true;

            log.info("REBUILDING assertion directory resource {} because of {}",
                textLiteral(expectedDirResourceFqn), sqnd(ex));

            writeExpectedDirectory(expectedDirResourceFqn, actualDir, config);
          }
        }
      }
    } catch (IOException ex) {
      fail(ex);
    }
  }

  @Override
  protected Logger getLog() {
    return log;
  }
}
