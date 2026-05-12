/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Verifier.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static java.util.stream.Collectors.joining;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.LF;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Objects.literal;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;

import com.spun.util.logger.SimpleLogger;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.reporters.AutoApproveReporter;
import org.approvaltests.reporters.QuietReporter;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.temp.util.io.ResourceNames;
import org.pdfclown.common.build.system.ProjectDirId;
import org.pdfclown.common.build.system.ProjectPathResolver;
import org.pdfclown.common.build.test.Tests;
import org.pdfclown.common.build.util.system.Builds;
import org.pdfclown.common.build.util.system.Runtimes;
import org.pdfclown.common.util.annot.Immutable;
import org.pdfclown.common.util.system.Systems;

/**
 * Verifier for approval testing.
 *
 * @author Stefano Chizzolini
 */
@Immutable
public abstract class Verifier implements Cloneable {
  /**
   * Stacktrace-based approval namer.
   * <p>
   * Differently from the {@linkplain org.approvaltests.namer.StackTraceNamer vanilla namer},
   * approved files are treated as regular resources, placed inside a directory named after the
   * corresponding test class (more precisely, its binary name). Such solution avoids messy
   * cluttering of test source directories with redundant, verbosely-named approved files, and
   * ensures more consistency in the project layout across the test suites.
   * </p>
   */
  @Immutable
  public static class DefaultNamer extends Namer {
    private final Path sourceFilePath;
    private final Path targetFilePath;
    private final String testMethodName;

    /**
    */
    public DefaultNamer() {
      var testFrame = Tests.testFrame().orElseThrow();

      testMethodName = testFrame.getMethodName();

      String testFolderResourceName = ResourceNames.fromType(testFrame.getDeclaringClass());
      sourceFilePath = projectPaths.resolve(ProjectDirId.TEST_RESOURCE_SOURCE,
          testFolderResourceName);
      targetFilePath = projectPaths.resolve(ProjectDirId.TEST_OUTPUT,
          testFolderResourceName);
    }

    @Override
    public String getApprovalName() {
      return testMethodName + getAdditionalInformation();
    }

    @Override
    public File getApprovedFile(String extensionWithDot) {
      return getApprovalFile(sourceFilePath, FILE_QUALIFIER__APPROVED, extensionWithDot);
    }

    @Override
    public File getReceivedFile(String extensionWithDot) {
      return getApprovalFile(targetFilePath, FILE_QUALIFIER__RECEIVED, extensionWithDot);
    }

    @Override
    public String getSourceFilePath() {
      return sourceFilePath.toString();
    }

    /**
     * Gets the approval file corresponding to the given coordinates.
     */
    protected File getApprovalFile(Path baseDir, String qualifier, String fileExtension) {
      return baseDir.resolve(getApprovalName() + qualifier + fileExtension).toFile();
    }
  }

  /**
   * Verifier namer.
   */
  @Immutable
  public abstract static class Namer implements ApprovalNamer, Cloneable {
    public static final String QUALIFIER__APPROVED = "expected";
    public static final String QUALIFIER__RECEIVED = "actual";

    public static final String FILE_QUALIFIER__APPROVED = DOT + QUALIFIER__APPROVED;
    public static final String FILE_QUALIFIER__RECEIVED = DOT + QUALIFIER__RECEIVED;

    protected final ProjectPathResolver projectPaths;

    private String additionalInformation = EMPTY;

    /**
    */
    public Namer() {
      try {
        projectPaths = ProjectPathResolver.of(Builds.projectDir());
      } catch (FileNotFoundException ex) {
        throw runtime(ex);
      }
    }

    @Override
    public Namer addAdditionalInformation(String value) {
      var ret = (Namer) clone();
      ret.additionalInformation += DOT + value;
      return ret;
    }

    @Override
    public String getAdditionalInformation() {
      return additionalInformation;
    }

    @Override
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    protected Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException ex) {
        throw runtime(ex);
      }
    }
  }

  static {
    /*
     * NOTE: This is necessary to globally align the namer factory to our logic, as namer's
     * instantiation is buried inside otherwise non-configurable ApprovalsTest logic (AFAIK, it
     * doesn't provide finer customization granularity in this respect).
     */
    Approvals.namerCreater = DefaultNamer::new;
  }

  private Options options = new Options()
      /*
       * NOTE: Removing trailing whitespace is necessary as certain comparators (e.g., IntelliJ)
       * seem to silently strip it from the received file content when updating the corresponding
       * approved file; without applying such normalization, the next verifications would fail as
       * the received file would keep containing trailing whitespace.
       */
      .withScrubber($ -> $.lines()
          .map(String::stripTrailing)
          .collect(joining(S + LF, EMPTY, S + LF)));

  /**
  */
  public Options getOptions() {
    return options;
  }

  /**
  */
  public Verifier withOptions(Options value) {
    var ret = (Verifier) clone();
    ret.options = value;
    return ret;
  }

  @Override
  @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
  protected Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException ex) {
      throw runtime(ex);
    }
  }

  /**
   * Prepares the options for verification.
   *
   * @param namerType
   *          Namer type to enforce. If {@link #getOptions() options} contains an incompatible type,
   *          it is overridden by this one.
   */
  protected Options prepareOptions(@Nullable Class<? extends Namer> namerType) {
    if (namerType == null) {
      namerType = DefaultNamer.class;
    }

    var ret = this.options;

    // Force namer to required type!
    if (!namerType.isAssignableFrom(ret.forFile().getNamer().getClass())) {
      try {
        ret = ret.forFile().withNamer(namerType.getDeclaredConstructor().newInstance());
      } catch (InstantiationException | InvocationTargetException | IllegalAccessException
          | NoSuchMethodException ex) {
        throw runtime(ex);
      }
    }

    /*
     * Automatically update assertion resources?
     *
     * NOTE: In case of automated assertion resources update, received mismatching files are
     * auto-approved.
     *
     * This system property has to be used sparingly, only in case of massive updates due to
     * infrastructural changes such as new formatting.
     */
    if (Systems.getBooleanProperty(Asserter.SYSTEM_PROPERTY__UPDATE_EXPECTED)) {
      ret = ret.withReporter(new AutoApproveReporter() {
        @Override
        public boolean report(String received, String approved) {
          if (!super.report(received, approved))
            throw unexpected(false, "Auto approval not handled");

          SimpleLogger.message("%s automatically UPDATED".formatted(literal(approved)));
          return true;
        }
      });
    }
    /*
     * Disable reporter?
     *
     * NOTE: Because of their invasiveness (for example, an IntelliJ diff dialog pops up per failed
     * test), reporters are disabled by default, unless users explicitly enable them or the test is
     * debugged in an IDE.
     */
    else if (!Systems.getBooleanProperty("assert.reporter.enabled") && !Runtimes.isDebugging()) {
      ret = ret.withReporter(new QuietReporter());
    }
    return ret;
  }
}
