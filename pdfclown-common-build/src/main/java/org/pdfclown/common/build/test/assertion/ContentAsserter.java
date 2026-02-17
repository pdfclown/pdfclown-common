/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ContentAsserter.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static org.junit.jupiter.api.Assertions.fail;
import static org.pdfclown.common.util.Conditions.requireNonNullElseThrow;
import static org.pdfclown.common.util.Objects.sqnd;
import static org.pdfclown.common.util.Objects.textLiteral;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__ZIP;
import static org.pdfclown.common.util.io.Files.baseName;
import static org.pdfclown.common.util.io.Files.cognateFile;
import static org.pdfclown.common.util.io.Files.extension;
import static org.pdfclown.common.util.io.Files.isExtension;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang3.function.Failable;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.util.io.ResourceNames;
import org.pdfclown.common.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automated content assertions for integration testing.
 * <p>
 * This class enables checks over data (actual content) against a resource (expected content) which
 * can be {@linkplain Asserter#SYSTEM_PROPERTY__FILES_UPDATE automatically updated}.
 * </p>
 *
 * @param <T>
 *          Content type.
 * @author Stefano Chizzolini
 */
public abstract class ContentAsserter<T> extends Asserter {
  private static final Logger log = LoggerFactory.getLogger(ContentAsserter.class);

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
   * @see Asserter#SYSTEM_PROPERTY__FILES_UPDATE
   */
  protected final void doAssertEquals(String expectedResourceName, Path actualFile, Config config) {
    try {
      doAssertEquals(expectedResourceName, readContent(actualFile), config);
    } catch (IOException ex) {
      fail(ex);
    }
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
   * @see Asserter#SYSTEM_PROPERTY__FILES_UPDATE
   */
  protected final void doAssertEquals(final String expectedResourceName, final T actualContent,
      final Config config) {
    final String expectedResourceFqn = ResourceNames.based(
        expectedResourceName, config.getTest(), true);
    final Path expectedFile = config.getEnv().resourcePath(expectedResourceFqn);
    try {
      var built = false;
      while (true) {
        try {
          T expectedContent = readContent(expectedFile);

          doAssertEquals(expectedContent, actualContent);

          break;
        } catch (AssertionError | FileNotFoundException | NoSuchFileException ex) {
          // Unrecoverable?
          if (built || !isUpdatable()) {
            Path unexpectedActualFile = null;
            if (exists(expectedFile)) {
              unexpectedActualFile = config.getEnv().outputPath(
                  cognateFile(expectedResourceFqn,
                      "_UNEXPECTED" + extension(expectedResourceFqn, true), true));
              try {
                // Save unexpected actual content!
                createDirectories(unexpectedActualFile.getParent());
                writeContent(unexpectedActualFile, actualContent);

                log.info("Assertion sample {}: unexpected actual content saved to {} "
                    + "(expected content is at {})", textLiteral(expectedResourceFqn),
                    textLiteral(unexpectedActualFile), textLiteral(expectedFile));
              } catch (Exception ex1) {
                log.warn("Assertion sample {}: unexpected actual content saving FAILED: {}",
                    textLiteral(expectedResourceFqn), textLiteral(unexpectedActualFile), ex1);
              }
            }

            evalAssertionError(ex.getMessage(), expectedFile, unexpectedActualFile);
          }

          /*
           * Assertion content rebuilding.
           *
           * NOTE: In case of explicit content build request, the actual content is saved into the
           * (either mismatching or missing) expected resource (at both source and target
           * locations).
           */
          {
            built = true;

            log.info("REBUILDING assertion content {} because of {}",
                textLiteral(expectedResourceFqn), sqnd(ex));

            writeExpectedFile(expectedResourceFqn,
                Failable.asConsumer($ -> writeContent($, actualContent)), config);
          }
        }
      }
    } catch (IOException ex) {
      fail(ex);
    }
  }

  /**
   * Asserts that a content matches the expected one.
   * <p>
   * <span class="important">IMPORTANT: This method is for internal use only; derived classes should
   * call {@link #doAssertEquals(String, Object, Config)} or
   * {@link #doAssertEquals(String, Path, Config)} instead.</span>
   * </p>
   */
  protected abstract void doAssertEquals(T expectedContent, T actualContent);

  /**
   * Gets the content of a (either plain or ZIP-compressed) file as string.
   * <p>
   * NOTE: In case of compressed archive, this method would access only its first entry; in case of
   * multi-entry archive, call {@link #doReadStringContent(Path, Predicate)} instead.
   * </p>
   */
  protected String doReadStringContent(Path file) throws IOException {
    return requireNonNullElseThrow(doReadStringContent(file, null), Exceptions::missing);
  }

  /**
   * Gets the content of a (either plain or ZIP-compressed) file as string.
   * <p>
   * NOTE: In case of compressed archive, this method will access only one entry, chosen via
   * {@code filter}.
   * </p>
   *
   * @param filter
   *          Entry filter (compressed file only; {@code null} to filter the first entry).
   */
  protected @Nullable String doReadStringContent(Path file, @Nullable Predicate<String> filter)
      throws IOException {
    if (isExtension(file.getFileName().toString(), FILE_EXTENSION__ZIP)) {
      if (filter == null) {
        filter = $ -> true;
      }
      try (var in = new ZipInputStream(new FileInputStream(file.toFile()))) {
        while (true) {
          ZipEntry zipEntry = in.getNextEntry();
          if (zipEntry == null) {
            break;
          } else if (!zipEntry.isDirectory() && filter.test(zipEntry.getName())) {
            try (var out = new ByteArrayOutputStream()) {
              int len;
              var buf = new byte[1024];
              while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
              }
              return out.toString(UTF_8);
            }
          }
        }
      }
      return null;
    } else
      return readString(file);
  }

  /**
   * Writes string content to file.
   *
   * @param file
   *          Target file ({@value org.pdfclown.common.util.io.Files#FILE_EXTENSION__ZIP} extension
   *          causes data to compress).
   */
  protected void doWriteStringContent(Path file, String content) throws IOException {
    String filename = file.getFileName().toString();
    if (isExtension(filename, FILE_EXTENSION__ZIP)) {
      try (var out = new ZipOutputStream(new FileOutputStream(file.toFile()))) {
        out.putNextEntry(new ZipEntry(baseName(filename)));
        out.write(content.getBytes(UTF_8));
      }
    } else {
      writeString(file, content);
    }
  }

  @Override
  protected Logger getLog() {
    return log;
  }

  protected abstract T readContent(Path file) throws IOException;

  protected abstract void writeContent(Path file, T content) throws IOException;
}
