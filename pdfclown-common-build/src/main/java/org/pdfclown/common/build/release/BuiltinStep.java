/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BuiltinStep.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.release;

import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static org.pdfclown.common.build.internal.util_.Chars.DOLLAR;
import static org.pdfclown.common.build.internal.util_.Conditions.requireNotBlank;
import static org.pdfclown.common.build.internal.util_.Exceptions.missing;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Exceptions.wrongArgOpt;
import static org.pdfclown.common.build.internal.util_.Objects.objTo;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;
import static org.pdfclown.common.build.internal.util_.Strings.S;
import static org.pdfclown.common.build.internal.util_.system.Processes.executeElseThrow;
import static org.pdfclown.common.build.internal.util_.system.Processes.unixCommand;
import static org.pdfclown.common.build.release.ReleaseManager.SCM_REF__HEAD;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.function.FailableConsumer;
import org.pdfclown.common.build.internal.util_.Ref;

/**
 * Built-in release step.
 * <p>
 * Most steps in this enumeration mimic <a href=
 * "https://maven.apache.org/components/maven-release-archives/maven-release-3.1.1/maven-release-manager/apidocs/org/apache/maven/shared/release/phase/package-summary.html">Maven
 * Release phases</a>. Their {@linkplain Enum#ordinal() declaration order} is significant to the
 * default release sequence.
 * </p>
 * <p>
 * This implementation assumes Unix (Bash shell) as OS, and Git as SCM.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public enum BuiltinStep implements Step {
  /**
   * Checks whether the tag already exists, which indicates this release is redundant.
   */
  SCM_CHECK($ -> {
    var outputRef = new Ref<String>();
    executeElseThrow(unixCommand(
        "git tag -l %s"
            .formatted($.getReleaseTag())),
        $.getBaseDir(), outputRef);
    if (!outputRef.get().isEmpty())
      throw runtime("Cannot prepare the release because its tag ({}) already exists",
          $.getReleaseTag());
  }, true),
  /**
   * Checks whether the content checked out from the repository has local modifications, thus it
   * cannot be safely released.
   * <p>
   * Mimics <a href=
   * "https://maven.apache.org/components/maven-release-archives/maven-release-3.1.1/maven-release-manager/apidocs/org/apache/maven/shared/release/phase/ScmCheckModificationsPhase.html"><code>scm-check-modifications</code>
   * phase</a> of Maven Release Manager.
   * </p>
   */
  SCM_MODIFICATIONS_CHECK($ -> {
    var outputRef = new Ref<String>();
    executeElseThrow(unixCommand("git status -s"), $.getBaseDir(), outputRef);
    if (!outputRef.get().isEmpty())
      throw runtime("Cannot prepare the release because of local modifications:\n{}",
          outputRef.get());
  }, true),
  /**
   * Creates and checks out the release branch.
   */
  RELEASE_SCM_BRANCH(BuiltinStep::executeScmReleaseBranch, false),
  /**
   * Prepares the project configuration for release.
   * <p>
   * Similarly to <a href=
   * "https://maven.apache.org/components/maven-release-archives/maven-release-3.1.1/maven-release-manager/apidocs/org/apache/maven/shared/release/phase/RewritePomsForReleasePhase.html"><code>rewrite-poms-for-release</code>
   * phase</a> of Maven Release Manager, updates the following CI-friendly parameters in
   * {@code .mvn/maven.config}:
   * </p>
   * <ul>
   * <li>{@code revision} (<b>project version</b>) to the release version</li>
   * <li>{@code scmTag} (<b>current SCM tag</b>) to the release tag</li>
   * </ul>
   */
  RELEASE_POM_UPDATE($ -> executePomUpdate($, $.getReleaseVersion(), $.getReleaseTag()), false),
  /**
   * Ensures no unreleased snapshot is among the dependencies of the projects being released.
   * <p>
   * Mimics <a href=
   * "https://maven.apache.org/components/maven-release-archives/maven-release-3.1.1/maven-release-manager/apidocs/org/apache/maven/shared/release/phase/CheckDependencySnapshotsPhase.html"><code>check-dependency-snapshots</code>
   * phase</a> of Maven Release Manager.
   * </p>
   * <p>
   * <span class="important">IMPORTANT: This step MUST follow {@link #RELEASE_POM_UPDATE} (which
   * updates the project's version to release), otherwise the project's old development version will
   * interfere with this check.</span>
   * </p>
   */
  DEPENDENCY_SNAPSHOTS_CHECK($ -> executeElseThrow(unixCommand(
      "%s enforcer:enforce -Denforcer.rules=requireReleaseDeps -Denforcer.failFast=true"
          .formatted($.getMavenExec())),
      $.getBaseDir()), true),
  /**
   * Updates the changelog file with release version changes.
   */
  RELEASE_CHANGELOG_UPDATE($ -> executeElseThrow(unixCommand(
      "cz changelog --unreleased-version %s --incremental"
          .formatted($.getReleaseVersion())),
      $.getBaseDir()), false),
  /**
   * Publishes the project artifacts to the central repository.
   * <p>
   * If {@link ReleaseManager#isRemotePushEnabled() ReleaseManager.remotePushEnabled} is false, the
   * artifacts are installed locally instead.
   * </p>
   */
  DEPLOY($ -> executeElseThrow(unixCommand(
      "%s clean %s %s"
          .formatted($.getMavenExec(), $.isRemotePushEnabled() ? "deploy" : "install", objTo(
              $.isRemotePushEnabled() ? $.getDeploymentProfiles() : $.getInstallationProfiles(),
              $$ -> !$$.isEmpty() ? "-P" + $$ : EMPTY))),
      $.getBaseDir()), false),
  /**
   * Commits to the local SCM repository the changes done to prepare the release, and tags them.
   * <p>
   * Mimics both <a href=
   * "https://maven.apache.org/components/maven-release-archives/maven-release-3.1.1/maven-release-manager/apidocs/org/apache/maven/shared/release/phase/ScmCommitReleasePhase.html"><code>scm-commit-release</code></a>
   * and <a href=
   * "https://maven.apache.org/components/maven-release-archives/maven-release-3.1.1/maven-release-manager/apidocs/org/apache/maven/shared/release/phase/ScmTagPhase.html"><code>scm-tag</code>
   * </a> phases of Maven Release Manager.
   * </p>
   */
  RELEASE_SCM_COMMIT($ -> {
    executeScmVersionCommit($, "release");
    executeScmReleaseTag($);
  }, false),
  /**
   * Prepares the project configuration for the next development iteration.
   * <p>
   * Similarly to <a href=
   * "https://maven.apache.org/components/maven-release-archives/maven-release-3.1.1/maven-release-manager/apidocs/org/apache/maven/shared/release/phase/RewritePomsForDevelopmentPhase.html"><code>rewrite-poms-for-development</code>
   * phase</a> of Maven Release Manager, updates the following CI-friendly parameters in
   * {@code .mvn/maven.config}:
   * </p>
   * <ul>
   * <li>{@code revision} (<b>project version</b>) to the next development version</li>
   * <li>{@code scmTag} (<b>current SCM tag</b>) to {@code HEAD}</li>
   * </ul>
   */
  DEV_POM_UPDATE($ -> executePomUpdate($, $.getDevVersion(), SCM_REF__HEAD), false),
  /**
   * Commits to the SCM repository the changes done to prepare the next development iteration.
   * <p>
   * Mimics <a href=
   * "https://maven.apache.org/components/maven-release-archives/maven-release-3.1.1/maven-release-manager/apidocs/org/apache/maven/shared/release/phase/ScmCommitDevelopmentPhase.html"><code>scm-commit-development</code>
   * phase</a> of Maven Release Manager.
   * </p>
   */
  DEV_SCM_COMMIT($ -> executeScmVersionCommit($, "dev"), false),
  /**
   * Pushes commits and tags to remote SCM repository.
   */
  SCM_PUSH(BuiltinStep::executeScmPush, false);

  private static final String MAVEN_CONFIG_PARAM__REVISION = "revision";
  private static final String MAVEN_CONFIG_PARAM__SCM_TAG = "scmTag";

  private static final String PATHNAME__MAVEN_CONFIG = ".mvn/maven.config";

  private static final Pattern PATTERN__MAVEN_CONFIG_PARAM = Pattern.compile("(-D(\\S+)=)\\S+");

  private static final int PATTERN_GROUP_INDEX__MAVEN_CONFIG_PARAM__ASSIGN = 1;
  private static final int PATTERN_GROUP_INDEX__MAVEN_CONFIG_PARAM__NAME = 2;

  /**
   * Updates the project version as defined in .mvn/maven.config file.
   */
  private static void executePomUpdate(ReleaseManager manager, String version, String scmTag) {
    requireNotBlank(version, "`version`");
    requireNotBlank(scmTag, "`scmTag`");

    var b = new StringBuilder();
    final var mavenConfigFile = manager.getBaseDir().resolve(PATHNAME__MAVEN_CONFIG);
    try {
      String mavenConfig = readString(mavenConfigFile);

      Matcher m = PATTERN__MAVEN_CONFIG_PARAM.matcher(mavenConfig);
      while (m.find()) {
        String newParamValue;
        switch (m.group(PATTERN_GROUP_INDEX__MAVEN_CONFIG_PARAM__NAME)) {
          case MAVEN_CONFIG_PARAM__REVISION:
            newParamValue = version;
            version = null;
            break;
          case MAVEN_CONFIG_PARAM__SCM_TAG:
            newParamValue = scmTag;
            scmTag = null;
            break;
          default:
            continue;
        }
        m.appendReplacement(b, S + DOLLAR + PATTERN_GROUP_INDEX__MAVEN_CONFIG_PARAM__ASSIGN
            + newParamValue);
      }
      if (version != null)
        throw missing(MAVEN_CONFIG_PARAM__REVISION, "parameter NOT FOUND in {}", mavenConfigFile);
      else if (scmTag != null)
        throw missing(MAVEN_CONFIG_PARAM__SCM_TAG, "parameter NOT FOUND in {}", mavenConfigFile);

      m.appendTail(b);

      writeString(mavenConfigFile, b.toString());
    } catch (Exception ex) {
      throw runtime("{} update FAILED", mavenConfigFile, ex);
    }
  }

  private static void executeScmPush(ReleaseManager manager) {
    if (manager.isRemotePushEnabled()) {
      try {
        executeElseThrow(unixCommand(
            "git push && git push --tags"),
            manager.getBaseDir());
      } catch (Exception ex) {
        throw runtime("SCM push FAILED", ex);
      }
    }
  }

  private static void executeScmReleaseBranch(ReleaseManager manager) {
    var releaseBranchName = "release/" + manager.getReleaseVersion();
    try {
      executeElseThrow(unixCommand(
          "git checkout -b %s %s"
              .formatted(releaseBranchName, manager.getReleaseBranchStartPoint())),
          manager.getBaseDir());
    } catch (Exception ex) {
      throw runtime("SCM branch {} creation FAILED", releaseBranchName, ex);
    }
  }

  /**
   * Tags locally the given project version.
   */
  private static void executeScmReleaseTag(ReleaseManager manager) {
    try {
      executeElseThrow(unixCommand(
          "git tag -a %s -m \"Release %s\""
              .formatted(manager.getReleaseTag(), manager.getReleaseVersion())),
          manager.getBaseDir());
    } catch (Exception ex) {
      throw runtime("SCM tagging FAILED", ex);
    }
  }

  /**
   * Creates locally a version bump SCM commit.
   *
   * @param kind
   *          Version bump kind (either {@code "release"} or {@code "dev"}).
   */
  private static void executeScmVersionCommit(ReleaseManager manager, String kind) {
    String version = switch (kind) {
      case "release" -> manager.getReleaseVersion();
      case "dev" -> manager.getDevVersion();
      default -> throw wrongArgOpt("kind", kind, null, List.of("release", "dev"));
    };
    try {
      executeElseThrow(unixCommand(
          "git add . && git commit -m \"bump: %s version %s\""
              .formatted(kind, version)),
          manager.getBaseDir());
    } catch (Exception ex) {
      throw runtime("SCM commit FAILED", ex);
    }
  }

  private final FailableConsumer<ReleaseManager, Exception> operation;
  private final boolean readOnly;

  BuiltinStep(FailableConsumer<ReleaseManager, Exception> operation, boolean readOnly) {
    this.operation = operation;
    this.readOnly = readOnly;
  }

  @Override
  public void execute(ReleaseManager manager) throws Exception {
    operation.accept(manager);
  }

  @Override
  public String getName() {
    return name();
  }

  public boolean isReadOnly() {
    return readOnly;
  }
}
