/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Asserter.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static java.nio.file.Files.exists;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.pdfclown.common.build.internal.temp.util.Exceptions.failedIO;
import static org.pdfclown.common.build.internal.temp.util.Exceptions.runtime;
import static org.pdfclown.common.build.internal.temp.util.Objects.textLiteral;
import static org.pdfclown.common.build.internal.temp.util.Strings.EMPTY;
import static org.pdfclown.common.build.internal.temp.util.Strings.abbreviateMultiline;
import static org.pdfclown.common.build.internal.temp.util.io.Files.copyDirectory;
import static org.pdfclown.common.build.internal.temp.util.io.Files.resetDirectory;
import static org.pdfclown.common.build.system.LogManager.MARKER__VERBOSE;
import static org.pdfclown.common.util.Chars.LF;
import static org.pdfclown.common.util.system.Systems.getBooleanProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Consumer;
import org.apache.commons.lang3.function.Failable;
import org.apache.commons.lang3.function.FailableFunction;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.temp.util.ParamMessage;
import org.pdfclown.common.build.system.LogManager;
import org.pdfclown.common.build.util.system.Builds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for assertions based on automatically managed state.
 * <p>
 * The expected state (typically persisted as resource files) is automatically managed to ensure
 * convenient test maintenance, providing testers with CLI hints to fix unexpected output and
 * {@linkplain #SYSTEM_PROPERTY__UPDATE_EXPECTED update} the corresponding resources.
 * </p>
 * <p>
 * Missing resources (for example, in case of new tests added to the suite) are treated as failed
 * assertions — silently updating them would be dangerous, as they could inadvertently sneak invalid
 * content in the expected state.
 * </p>
 * <p>
 * During a comparison between actual and expected states, assertion errors are incrementally
 * appended to a single message; in the end, the message is
 * {@linkplain #evalAssertionResult(String, Path, Path, Config) evaluated} and, if non-empty, logged
 * as a {@linkplain org.pdfclown.common.build.system.LogManager#MARKER__VERBOSE verbose} error entry
 * to a dedicated log file ({@code target/test-logs/pdfclown/assertion.log}), then wrapped into an
 * {@link AssertionError} in shortened form and thrown.
 * </p>
 *
 * @implSpec Implementations MUST query {@link #isUpdatable(Config)} to decide whether the expected
 *           state can be updated instead of emitting a
 *           {@linkplain #evalAssertionResult(String, Path, Path, Config) mismatch error}.
 * @author Stefano Chizzolini
 */
public abstract class Asserter {
  /**
   * {@link Asserter} configuration.
   *
   * @author Stefano Chizzolini
   */
  @SuppressWarnings("ClassCanBeRecord")
  public static class Config implements Cloneable {
    final org.pdfclown.common.build.test.assertion.Test test;

    public Config(org.pdfclown.common.build.test.assertion.Test test) {
      this.test = requireNonNull(test, "`test`");
    }

    @Override
    public Config clone() {
      try {
        return (Config) super.clone();
      } catch (CloneNotSupportedException ex) {
        throw runtime(ex);
      }
    }

    public TestEnvironment getEnv() {
      return test.getEnv();
    }

    public org.pdfclown.common.build.test.assertion.Test getTest() {
      return test;
    }
  }

  /**
   * Error message builder.
   *
   * @author Stefano Chizzolini
   */
  public static class ErrorMessageBuilder {
    private final StringBuilder base = new StringBuilder();

    /**
     * Appends the {@linkplain Object#toString() string representation} of an object to the current
     * error entry.
     */
    public ErrorMessageBuilder append(Object obj) {
      base.append(obj);
      return this;
    }

    /**
     * Appends text to the current error entry.
     */
    public ErrorMessageBuilder append(String text) {
      base.append(text);
      return this;
    }

    /**
     * Begins a new error entry.
     */
    public ErrorMessageBuilder error(String text) {
      if (!base.isEmpty()) {
        base.append("\n");
      }
      return append(text);
    }

    /**
     * Whether this message is empty.
     */
    public boolean isEmpty() {
      return base.isEmpty();
    }

    @Override
    public String toString() {
      return base.toString();
    }
  }

  private static final Logger log = LoggerFactory.getLogger(Asserter.class);

  /**
   * System property specifying whether {@linkplain Asserter assertion} resource update is enabled
   * for executed tests.
   * <p>
   * Assertion resources represent the expected state against which the corresponding actual state
   * generated by the tested project code is validated. If a resource is missing or the
   * corresponding validation is false negative because the tested project code innovated the
   * expected state, {@linkplain Asserter asserters} can regenerate it through this property.
   * </p>
   * <p>
   * The value of this property is a boolean which can be omitted (default: {@code true}).
   * </p>
   *
   * @apiNote Common usage examples (Maven build system):
   *          <ul>
   *          <li>to regenerate all the mismatching resources, no matter the tests they belong to:
   *          <pre class="lang-shell"><code>
   * mvn verify ... -Dtest.expected.update</code></pre></li>
   *          <li>to regenerate the mismatching resources belonging to specific test classes (for
   *          example, "MyObjectIT"): <pre class="lang-shell"><code>
   * mvn verify ... -Dtest.expected.update -Dit.test=MyObjectIT</code></pre></li>
   *          <li>to regenerate the mismatching resources belonging to specific test cases (for
   *          example, "MyObjectIT.myTest"): <pre class="lang-shell"><code>
   * mvn verify ... -Dtest.expected.update -Dit.test=MyObjectIT#myTest</code></pre></li>
   *          <li>to regenerate the mismatching resources belonging to multiple test classes (for
   *          example, MyObjectIT and MyOtherObjectIT), they can be specified as a comma-separated
   *          list: <pre class="lang-shell"><code>
   * mvn verify ... -Dtest.expected.update -Dit.test=MyObjectIT,MyOtherObjectIT</code></pre></li>
   *          </ul>
   */
  public static final String SYSTEM_PROPERTY__UPDATE_EXPECTED = "test.expected.update";
  static {
    log.info("`{}` system property: {}", SYSTEM_PROPERTY__UPDATE_EXPECTED,
        getBooleanProperty(SYSTEM_PROPERTY__UPDATE_EXPECTED));
  }

  /**
   * Evaluates the assertion result and throws an assertion error in case of failure.
   * <p>
   * This method MUST be invoked at the end of the assertion, after all the detected errors were
   * combined in the message:
   * </p>
   * <ul>
   * <li>if {@code errorMessage} is empty, the assertion succeeded: this method quietly returns</li>
   * <li>if {@code errorMessage} is not empty, the assertion failed: this method enters the full
   * content of {@code errorMessage} into the assertion log, then throws its shortened version as
   * {@link AssertionError}</li>
   * </ul>
   *
   * @param errorMessage
   *          Assertion error message.
   * @param expectedFile
   *          Expected test result (resource file).
   * @param actualFile
   *          Actual test result (output file).
   * @throws AssertionError
   *           If {@code errorMessage} is not empty.
   */
  protected void evalAssertionResult(@Nullable String errorMessage, Path expectedFile,
      Path actualFile, Config config) throws AssertionError {
    if (isBlank(errorMessage))
      return;

    String testQName = config.getTest().getTestQName();

    errorMessage = """
        Test %s FAILED:
        %s""".formatted(textLiteral(testQName), errorMessage);
    String projectArtifactId = Builds.projectArtifactId(expectedFile);
    String hint = ParamMessage.format(
        """

            Compared files:
             * EXPECTED: {}
             * ACTUAL: {}
            To retry, enter this command:
              mvn verify -pl {} -Dit.test={}
            To confirm the actual changes as expected, enter this command:
              mvn verify -pl {} -Dit.test={} -D{}
            """,
        expectedFile + (exists(expectedFile) ? EMPTY : " (MISSING)"),
        actualFile + (exists(actualFile) ? EMPTY : " (MISSING)"),
        projectArtifactId, textLiteral(testQName),
        projectArtifactId, textLiteral(testQName), SYSTEM_PROPERTY__UPDATE_EXPECTED);

    // Log (full message).
    getLog().error(MARKER__VERBOSE, "{}" + LF + "{}", errorMessage, hint);

    // Exception (shortened message).
    throw new AssertionError("""
        %s
        (see '%s' for further information)
        %s""".formatted(abbreviateMultiline(errorMessage, 5, 500),
        LogManager.getLogFiles().get(LogManager.APPENDER_NAME__ASSERTION), hint));
  }

  /**
   * Implementation-specific logger.
   */
  protected abstract Logger getLog();

  /**
   * Gets whether the expected resources can be overwritten in case of mismatch with their actual
   * counterparts.
   */
  protected boolean isUpdatable(Config config) {
    return getBooleanProperty(SYSTEM_PROPERTY__UPDATE_EXPECTED);
  }

  /**
   * Notifies a resource was written on filesystem, changing the expected state.
   */
  protected void onExpectedResourceUpdated(String resourceName, Config config) {
  }

  /**
   * Reads an expected resource.
   *
   * @param resourceName
   *          Resource to read.
   * @param reader
   *          Reads the resource.
   * @param config
   *          Assertion configuration.
   * @param <T>
   *          Resource object type.
   */
  protected <T> T readExpectedFile(String resourceName,
      FailableFunction<Path, T, IOException> reader,
      Config config) throws IOException {
    Path expectedFile = config.getEnv().resourcePath(resourceName);
    try {
      return reader.apply(expectedFile);
    } catch (IOException ex) {
      var b = new StringBuilder("Expected resource load FAILED at ").append(expectedFile);
      if (ex instanceof NoSuchFileException) {
        b.append(" (MISSING)");
        //noinspection DataFlowIssue,AssignmentToCatchBlockParameter : nullable
        ex = null /* NOTE: No need of redundant stack-trace for trivial cases */;
      }
      throw failedIO(b.toString(), ex);
    }
  }

  /**
   * Writes an expected directory.
   * <p>
   * After written to source, the resource is also copied to the target side in order to synchronize
   * ongoing tests.
   * </p>
   *
   * @param resourceName
   *          Resource to write.
   * @param writer
   *          Writes the resource.
   * @param config
   *          Assertion configuration.
   */
  protected void writeExpectedDirectory(String resourceName, Consumer<Path> writer, Config config)
      throws IOException {
    // Source directory.
    Path sourceDir = config.getEnv().resourceSrcPath(resourceName);
    try {
      resetDirectory(sourceDir);
      writer.accept(sourceDir);
    } catch (Exception ex) {
      throw failedIO("Expected resource build FAILED: {}", sourceDir, ex);
    }
    getLog().info("Expected directory resource BUILT at {}", textLiteral(sourceDir));

    // Target file.
    Path targetDir = config.getEnv().resourcePath(resourceName);
    try {
      resetDirectory(targetDir);
      copyDirectory(sourceDir, targetDir);
    } catch (Exception ex) {
      throw failedIO("""
          Expected resource copy to target FAILED (re-running tests should fix it): {}""",
          targetDir, ex);
    }
    getLog().info("Expected directory resource COPIED to target at {}", textLiteral(targetDir));

    onExpectedResourceUpdated(resourceName, config);
  }

  /**
   * Writes an expected directory.
   * <p>
   * After written to source, the resource is also copied to the target side in order to synchronize
   * ongoing tests.
   * </p>
   *
   * @param resourceName
   *          Resource to write.
   * @param actualDir
   *          Actual directory to overwrite the expected resource.
   * @param config
   *          Assertion configuration.
   * @implNote Marked as final to enforce overloads consistency.
   */
  protected final void writeExpectedDirectory(String resourceName, Path actualDir, Config config)
      throws IOException {
    writeExpectedDirectory(resourceName, Failable.asConsumer($ -> copyDirectory(actualDir, $)),
        config);
  }

  /**
   * Writes an expected resource.
   * <p>
   * After written to source, the resource is also copied to the target side in order to synchronize
   * ongoing tests.
   * </p>
   *
   * @param resourceName
   *          Resource to write.
   * @param writer
   *          Writes the resource.
   * @param config
   *          Assertion configuration.
   */
  protected void writeExpectedFile(String resourceName, Consumer<Path> writer, Config config)
      throws IOException {
    // Source file.
    Path sourceFile = config.getEnv().resourceSrcPath(resourceName);
    try {
      Files.createDirectories(sourceFile.getParent());
      writer.accept(sourceFile);
    } catch (Exception ex) {
      throw failedIO("Expected resource build FAILED: {}", sourceFile, ex);
    }
    getLog().info("Expected resource BUILT at {}", textLiteral(sourceFile));

    // Target file.
    Path targetFile = config.getEnv().resourcePath(resourceName);
    try {
      Files.createDirectories(targetFile.getParent());
      Files.copy(sourceFile, targetFile, REPLACE_EXISTING);
    } catch (Exception ex) {
      throw failedIO("""
          Expected resource copy to target FAILED (re-running tests should fix it): {}""",
          targetFile, ex);
    }
    getLog().info("Expected resource COPIED to target at {}", textLiteral(targetFile));

    onExpectedResourceUpdated(resourceName, config);
  }

  /**
   * Writes an expected resource.
   * <p>
   * After written to source, the resource is also copied to the target side in order to synchronize
   * ongoing tests.
   * </p>
   *
   * @param resourceName
   *          Resource to write.
   * @param actualFile
   *          Actual file to overwrite the expected resource.
   * @param config
   *          Assertion configuration.
   * @implNote Marked as final to enforce overloads consistency.
   */
  protected final void writeExpectedFile(String resourceName, Path actualFile, Config config)
      throws IOException {
    writeExpectedFile(resourceName, Failable.asConsumer(
        $ -> Files.copy(actualFile, $, REPLACE_EXISTING)), config);
  }
}
