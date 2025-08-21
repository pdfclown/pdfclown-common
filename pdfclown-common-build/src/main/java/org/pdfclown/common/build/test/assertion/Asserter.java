/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Asserter.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.ParamMessage.ARG;
import static org.pdfclown.common.build.internal.util_.Strings.COMMA;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;
import static org.pdfclown.common.build.internal.util_.Strings.LF;
import static org.pdfclown.common.build.internal.util_.Strings.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.build.internal.util_.Strings.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.build.internal.util_.Strings.S;
import static org.pdfclown.common.build.internal.util_.Strings.SLASH;
import static org.pdfclown.common.build.internal.util_.Strings.abbreviateMultiline;
import static org.pdfclown.common.build.internal.util_.regex.Patterns.globToRegex;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.pdfclown.common.build.internal.util.system.Builds;
import org.pdfclown.common.build.internal.util_.system.Systems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for assertions based on automatically managed state.
 * <p>
 * The expected state (typically persisted as resource file) is automatically managed to ensure easy
 * and robust test maintenance, providing testers with CLI hints to fix unexpected output and update
 * the corresponding resources.
 * </p>
 * <p>
 * In case of error, full assertion reports are written to a dedicated log file
 * ({@code target/test-logs/pdfclown/assert.log}).
 * </p>
 *
 * @author Stefano Chizzolini
 */
public abstract class Asserter {
  /**
   * {@link Asserter} configuration.
   *
   * @author Stefano Chizzolini
   */
  public static class Config implements Cloneable {
    final org.pdfclown.common.build.test.assertion.Test test;
    @Nullable
    String testId;

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

    public @Nullable String getTestId() {
      return testId;
    }

    public Config setTestId(String value) {
      testId = value;
      return this;
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
     * Appends the {@linkplain Object#toString() string representation} of the given object to the
     * current error entry.
     */
    public ErrorMessageBuilder append(Object obj) {
      base.append(obj);
      return this;
    }

    /**
     * Appends the given text to the current error entry.
     */
    public ErrorMessageBuilder append(String text) {
      base.append(text);
      return this;
    }

    /**
     * Begins a new error entry.
     */
    public ErrorMessageBuilder error(String text) {
      if (base.length() > 0) {
        base.append("\n");
      }
      return append(text);
    }

    /**
     * Whether this message is empty.
     */
    public boolean isEmpty() {
      return base.length() == 0;
    }

    /**
     * Begins a new page-related error entry.
     */
    public ErrorMessageBuilder pageError(int pageIndex) {
      return error("Page ").append(pageIndex).append(": ");
    }

    @Override
    public String toString() {
      return base.toString();
    }
  }

  private static final Logger log = LoggerFactory.getLogger(Asserter.class);

  /**
   * CLI parameter specifying whether assertion resource building is enabled for executed tests.
   * <p>
   * Assertion resources represent the expected state against which the corresponding actual state
   * generated by the current project code is validated. If a resource is missing or its validation
   * is false negative because the current project code innovated the expected state, this asserter
   * can regenerate such resource through this CLI parameter.
   * </p>
   * <p>
   * The value of this CLI parameter is a boolean which can be omitted (default: {@code true}).
   * </p>
   *
   * @apiNote Common usage examples:
   *          <ul>
   *          <li>to regenerate all the mismatching resources, no matter the tests they belong to:
   *          <pre class="lang-shell"><code>
   * mvn verify ... -Dpdfclown.assert.update</code></pre></li>
   *          <li>to regenerate the mismatching resources belonging to specific test classes (e.g.,
   *          "MyObjectIT"): <pre class="lang-shell"><code>
   * mvn verify ... -Dpdfclown.assert.update -Dtest=MyObjectIT</code></pre></li>
   *          <li>to regenerate the mismatching resources belonging to specific test cases (e.g.,
   *          "MyObjectIT.myTest"): <pre class="lang-shell"><code>
   * mvn verify ... -Dpdfclown.assert.update -Dtest=MyObjectIT#myTest</code></pre></li>
   *          <li>to regenerate the mismatching resources belonging to multiple test classes (e.g.,
   *          MyObjectIT and MyOtherObjectIT), they can be specified as a comma-separated list:
   *          <pre class="lang-shell"><code>
   * mvn verify ... -Dpdfclown.assert.update -Dtest=MyObjectIT,MyOtherObjectIT</code></pre></li>
   *          </ul>
   *          <p>
   *          NOTE: {@code test} CLI parameter is typically mapped by Maven plugins (such as
   *          <a href="https://maven.apache.org/surefire/maven-failsafe-plugin/">Failsafe</a>) to
   *          the corresponding JUnit system property which allows fine-grained test selection (see
   *          the relevant documentation); if your building system doesn't support it, adjust your
   *          commands accordingly. Furthermore, if the names of your test cases are overridden
   *          (i.e., their names are different from the corresponding test methods), it's up to you
   *          to use their actual names, as they are internally resolved by JUnit.
   *          </p>
   */
  public static final String PARAM_NAME__UPDATE = "pdfclown.assert.update";

  private static final Predicate<String> FILTER__UPDATE = fqnFilter(PARAM_NAME__UPDATE);

  /**
   * Builds the predicate corresponding to the given CLI parameter, whose value is a string of
   * comma-separated FQNs.
   * <p>
   * The predicate is expected to evaluate fully-qualified test identifiers (e.g.,
   * "/org/pdfclown/layout/LayoutIT_testPdfAConformance.pdf").
   * </p>
   *
   * @param paramName
   *          CLI parameter.
   * @return Truth predicate if {@code paramName} has no specified CLI value.
   */
  protected static Predicate<String> fqnFilter(String paramName) {
    String regex = null;
    boolean defaultResult = false;
    {
      String paramValue = System.getProperty(paramName);
      if (paramValue != null) {
        // Any FQN?
        if (Systems.getBoolProperty(paramName)) {
          defaultResult = true;
        }
        // Specific FQNs.
        else {
          try {
            var b = new StringBuilder();
            String[] paramValueItems = paramValue.split(S + COMMA);
            for (int i = 0; i < paramValueItems.length; i++) {
              if (i > 0) {
                b.append('|');
              }
              if (!paramValueItems[i].contains(S + SLASH)) {
                paramValueItems[i] = "/**" + paramValueItems[i];
              }
              b.append(ROUND_BRACKET_OPEN).append(globToRegex(paramValueItems[i]))
                  .append(ROUND_BRACKET_CLOSE);
            }
            regex = b.toString();
          } catch (Exception ex) {
            log.error("`" + ARG + "` CLI parameter initialization FAILED", paramName, ex);
          }
        }
      }
    }
    log.info("`" + ARG + "` CLI parameter: " + ARG, paramName,
        regex != null ? regex : defaultResult ? "ANY" : "NONE");

    if (regex != null)
      return Pattern.compile(regex).asMatchPredicate();
    else {
      final var result = defaultResult;
      return $ -> result;
    }
  }

  /**
   * Evaluates the assertion result and throws an assertion error in case of failure.
   * <p>
   * This method is expected to be invoked at the end of the assertion, after all the detected
   * errors were combined in the given message:
   * </p>
   * <ul>
   * <li>if {@code message} is empty, the assertion succeeded: this method quietly returns</li>
   * <li>if {@code message} is not empty, the assertion failed: this method enters the full content
   * of {@code message} into the assertion log, then throws its shortened version as
   * {@link AssertionError}</li>
   * </ul>
   *
   * @param testId
   *          Test identifier (either simple or fully-qualified). Typically corresponds to the
   *          test-unit-specific resource name of the expected file (e.g.,
   *          "LayoutIT_testPdfAConformance.pdf", or fully-qualified
   *          "/org/pdfclown/layout/LayoutIT_testPdfAConformance.pdf").
   * @param message
   *          Assertion error message (if empty, no error is thrown).
   * @param expectedFile
   *          Expected test result (resource file).
   * @param actualFile
   *          Actual test result (output file).
   * @throws AssertionError
   *           If {@code message} is not empty.
   */
  protected void evalAssertionError(String testId, @Nullable String message, Path expectedFile,
      @Nullable Path actualFile) throws AssertionError {
    if (message == null || (message = message.strip()).isEmpty())
      return;

    var testAnnotationTypes = Set.of(Test.class, ParameterizedTest.class);
    String testName = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
        .walk($frames -> $frames
            .skip(2)
            .<Optional<Method>>map($ -> {
              try {
                return Optional.of($.getDeclaringClass().getDeclaredMethod($.getMethodName(),
                    $.getMethodType().parameterArray()));
              } catch (NoSuchMethodException e) {
                return Optional.empty();
              }
            })
            .filter($ -> $
                .map($$ -> Stream.of($$.getDeclaredAnnotations())
                    .anyMatch($$$ -> testAnnotationTypes.contains($$$.annotationType())))
                .orElse(false))
            .findFirst()
            .map($ -> $
                .map($$ -> $$.getDeclaringClass().getSimpleName() + "#" + $$.getName())
                .orElseThrow())
            .orElse(EMPTY));
    if (testName.isEmpty())
      throw runtime("Failed test method NOT FOUND on stack trace (should be marked with any of "
          + "these annotations: " + ARG + ")",
          testAnnotationTypes.stream().map(Class::getName).collect(toList()));

    message = String.format(Locale.ROOT, "Test '%s' FAILED:\n%s", testName, message);
    String projectArtifactId = Builds.artifactId(expectedFile);
    String hint = String.format(Locale.ROOT,
        "\nCompared files:\n"
            + " * EXPECTED: %s\n"
            + " * ACTUAL: %s\n"
            + "To retry, enter this CLI parameter into your command:\n"
            + "  mvn verify -pl %s -Dtest=\"%s\"\n"
            + "To confirm the actual changes as expected, enter these CLI parameters into your "
            + "command:\n"
            + "  mvn verify -pl %s -D%s=\"%s\" -Dtest=\"%s\"\n",
        expectedFile, requireNonNull(actualFile, "N/A"),
        projectArtifactId, testName,
        projectArtifactId, PARAM_NAME__UPDATE, testId, testName);

    // Log (full message).
    getLog().error(LogMarker.VERBOSE, ARG + LF + ARG, message, hint);

    // Exception (shortened message).
    throw new AssertionError(String.format("%s\n"
        + "(see pdfclown/assert.log for further information)\n"
        + "%s", abbreviateMultiline(message, 5, 100), hint));
  }

  /**
   * Implementation-specific logger.
   */
  protected abstract Logger getLog();

  /**
   * Resolves the test identifier.
   * <p>
   * The {@linkplain Config#getTestId() configured identifier} has priority over the default one.
   * </p>
   */
  @SuppressWarnings("null")
  protected String getTestId(Supplier<String> defaultSupplier, Config config) {
    return config.getTestId() != null ? config.getTestId() : defaultSupplier.get();
  }

  /**
   * Gets whether the expected resources associated to the given ID can be overwritten in case of
   * mismatch with their actual counterparts.
   */
  protected boolean isUpdatable(String testId) {
    return FILTER__UPDATE.test(testId);
  }

  /**
   * Writes the expected resource on both source and target sides.
   *
   * @param resourceName
   *          Resource to write.
   * @param writer
   *          Resource generator.
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
    } catch (RuntimeException ex) {
      throw runtime("Expected resource build FAILED: " + sourceFile,
          ex.getCause() != null ? ex.getCause() : ex);
    }
    getLog().info("Expected resource BUILT at " + ARG, sourceFile);

    // Target file.
    Path targetFile = config.getEnv().resourcePath(resourceName);
    try {
      Files.createDirectories(targetFile.getParent());
      Files.copy(sourceFile, targetFile, REPLACE_EXISTING);
    } catch (RuntimeException ex) {
      throw runtime("Expected resource copy to target FAILED "
          + "(re-running tests should fix it): " + targetFile, ex);
    }
    getLog().info("Expected resource COPIED to target (" + ARG + ")", targetFile);
  }

  /**
   * Writes the expected resource on both source and target sides.
   *
   * @param resourceName
   *          Resource to write.
   * @param actualFile
   *          Actual file to overwrite the expected resource.
   * @param config
   *          Assertion configuration.
   */
  protected void writeExpectedFile(String resourceName, Path actualFile, Config config)
      throws IOException {
    writeExpectedFile(resourceName,
        $ -> {
          try {
            Files.copy(actualFile, $, REPLACE_EXISTING);
          } catch (IOException ex) {
            throw runtime(ex);
          }
        }, config);
  }
}
