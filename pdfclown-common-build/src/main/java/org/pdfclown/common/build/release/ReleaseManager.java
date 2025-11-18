/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ReleaseManager.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.release;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.containsWhitespace;
import static org.apache.commons.lang3.StringUtils.stripToEmpty;
import static org.apache.commons.lang3.SystemUtils.IS_OS_UNIX;
import static org.pdfclown.common.build.internal.util_.Chars.LF;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Exceptions.wrongArg;
import static org.pdfclown.common.build.internal.util_.Exceptions.wrongState;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.meta.SemVer;
import org.pdfclown.common.build.meta.SemVer.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Release manager.
 * <p>
 * Lightweight alternative to <a href="https://maven.apache.org/maven-release/index.html">Maven
 * Release</a>, focused on
 * <a href="https://maven.apache.org/guides/mini/guide-maven-ci-friendly.html">Maven CI-Friendly
 * Versions</a>.
 * </p>
 * <p>
 * To maximize flexibility while still retaining a self-documenting version declaration, this
 * implementation assumes {@code revision} is specified through the corresponding parameter in
 * {@code .mvn/maven.config} instead of a property inside {@code pom.xml} (the latter proved
 * problematic in case of complex reactor hierarchies).
 * </p>
 * <p>
 * Build profiles can be activated for {@linkplain #getDeploymentProfiles() deployment} and
 * {@linkplain #getInstallationProfiles() installation}; their activation depends on whether
 * {@linkplain ReleaseManager#isRemotePushEnabled() remote push} is enabled or not, respectively.
 * </p>
 * <p>
 * To locally simulate the release without modifying the state of remote SCM and artifact
 * repositories, set {@link ReleaseManager#isRemotePushEnabled() remotePushEnabled} to {@code false}
 * (NOTE: afterward, this will require manual revert of local SCM commits and tagging in non-CI
 * environments, as no rollback has been implemented yet). To simulate the release without any side
 * effect, set {@link ReleaseManager#isDryRun() dryRun} to {@code true}.
 * </p>
 * <p>
 * This implementation assumes Unix (Bash shell) as OS, and Git as SCM.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class ReleaseManager {
  /**
   * Version scheme.
   *
   * @author Stefano Chizzolini
   */
  public enum VersionScheme {
    /**
     * <a href="https://semver.org/spec/v2.0.0.html">Semantic Versioning 2.0.0</a>.
     */
    SEMVER2
  }

  abstract static class VersionResolver {
    static class SemVer2VersionResolver extends VersionResolver {
      /**
       * @implNote The development version will have {@code SNAPSHOT} as pre-release identifier,
       *           while its normal identifiers will be calculated as follows:
       *           <ul>
       *           <li>if {@code releaseVersion} is stable (say, {@code 2.0.0}), increment patch
       *           (say, {@code 2.0.1-SNAPSHOT})</li>
       *           <li>otherwise ({@code releaseVersion} is pre-release — say, {@code 2.0.0-alpha}),
       *           leave as-is (say, {@code 2.0.0-SNAPSHOT})</li>
       *           </ul>
       */
      @Override
      public String getNextDevVersion(String releaseVersion) {
        var v = SemVer.of(releaseVersion);
        return (v.isStable() ? v.next(Id.PATCH) : v).with(Id.PRERELEASE, "SNAPSHOT").toString();
      }
    }

    private static final Map<VersionScheme, Supplier<VersionResolver>> factories =
        Map.of(VersionScheme.SEMVER2, SemVer2VersionResolver::new);

    public static VersionResolver of(VersionScheme scheme) {
      return factories.get(scheme).get();
    }

    /**
     * Calculates the next development version based on the release version.
     */
    public abstract String getNextDevVersion(String releaseVersion);
  }

  private static final Logger log = LoggerFactory.getLogger(ReleaseManager.class);

  private static String checkProfiles(String value) {
    if (containsWhitespace(requireNonNull(value)))
      throw wrongArg(null, value, "MUST NOT contain whitespace");

    return value;
  }

  private static String scmTag(String version) {
    return "v" + version;
  }

  private final Path baseDir;
  private String deploymentProfiles = EMPTY;
  private final String devVersion;
  private boolean dryRun;
  private String installationProfiles = EMPTY;
  private String releaseBranchStartPoint = EMPTY;
  private final String releaseTag;
  private final String releaseVersion;
  private boolean remotePushEnabled = true;
  private final List<Step> steps = new ArrayList<>(asList(BuiltinStep.values()));
  private final VersionScheme versionScheme = VersionScheme.SEMVER2;

  /**
  */
  public ReleaseManager(Path baseDir, String releaseVersion) {
    if (!IS_OS_UNIX)
      throw wrongState("MUST run under a Unix system");

    this.baseDir = requireNonNull(baseDir, "`baseDir`");
    this.releaseVersion = requireNonNull(releaseVersion, "`releaseVersion`");

    this.devVersion = VersionResolver.of(versionScheme).getNextDevVersion(releaseVersion);
    this.releaseTag = scmTag(releaseVersion);
  }

  /**
   * Executes a release.
   * <p>
   * A release comprises its preparation and deployment.
   * </p>
   *
   * @implNote In case of {@link #isDryRun() dryRun}, the initial read-only steps are executed,
   *           whilst the other ones are simulated.
   */
  public void execute() {
    log.info("Execution details:" + LF
        + "- release branch start point: {}" + LF
        + "- release version (tag): {} ({})" + LF
        + "- next development version: {}" + LF
        + "- deployment profiles: {}" + LF
        + "- installation profiles: {}" + LF
        + "- remote push: {}" + LF
        + "- dry run: {}",
        getReleaseBranchStartPoint().isEmpty() ? "HEAD" : getReleaseBranchStartPoint(),
        getReleaseVersion(),
        getReleaseTag(),
        getDevVersion(),
        getDeploymentProfiles(),
        getInstallationProfiles(),
        isRemotePushEnabled()
            ? "YES (publishing + remote SCM push)"
            : "NO (local installation + local SCM commit)",
        isDryRun());

    var dryExecutable = true;
    for (var it = steps.listIterator(); it.hasNext();) {
      var i = it.nextIndex() + 1;
      var e = it.next();

      log.info("Step {} ({})", i, e.getName());

      if (!e.isReadOnly()) {
        dryExecutable = false;
      }
      if (!dryRun || dryExecutable) {
        try {
          e.execute(this);
        } catch (Exception ex) {
          if (dryRun) {
            log.warn("Step {} ({}) FAILED", i, e.getName(), ex);
          } else
            throw runtime("Step {} ({}) FAILED", i, e.getName(), ex);
        }
      }
    }
  }

  /**
   * Root project directory.
   */
  public Path getBaseDir() {
    return baseDir;
  }

  /**
   * Build profiles to activate on deploy.
   *
   * @return Comma-separated list.
   */
  public String getDeploymentProfiles() {
    return deploymentProfiles;
  }

  /**
   * Next development version.
   */
  public String getDevVersion() {
    return devVersion;
  }

  /**
   * Build profiles to activate on installation.
   *
   * @return Comma-separated list.
   * @see #isRemotePushEnabled()
   */
  public String getInstallationProfiles() {
    return installationProfiles;
  }

  /**
   * Commit the release branch is created from.
   */
  public String getReleaseBranchStartPoint() {
    return releaseBranchStartPoint;
  }

  /**
   * Release tag.
   */
  public String getReleaseTag() {
    return releaseTag;
  }

  /**
   * Release version.
   */
  public String getReleaseVersion() {
    return releaseVersion;
  }

  /**
   * Release steps to execute.
   * <p>
   * By default, corresponds to the whole sequence of {@link BuiltinStep}.
   * </p>
   */
  public List<Step> getSteps() {
    return steps;
  }

  /**
   * Version scheme to handle project versions.
   */
  public VersionScheme getVersionScheme() {
    return versionScheme;
  }

  /**
   * Whether the execution is only simulated.
   */
  public boolean isDryRun() {
    return dryRun;
  }

  /**
   * Whether remote SCM and artifact repositories can be updated.
   * <p>
   * Disable to locally simulate the release (installation, commits and tagging to local
   * repositories).
   * </p>
   */
  public boolean isRemotePushEnabled() {
    return remotePushEnabled;
  }

  /**
   * Sets {@link #getDeploymentProfiles() deploymentProfiles}.
   */
  public ReleaseManager setDeploymentProfiles(String value) {
    deploymentProfiles = checkProfiles(value);
    return this;
  }

  /**
   * Sets {@link #isDryRun() dryRun}.
   */
  public ReleaseManager setDryRun(boolean value) {
    dryRun = value;
    return this;
  }

  /**
   * Sets {@link #getInstallationProfiles() installationProfiles}.
   */
  public ReleaseManager setInstallationProfiles(String value) {
    installationProfiles = checkProfiles(value);
    return this;
  }

  /**
   * Sets {@link #getReleaseBranchStartPoint() releaseBranchStartPoint}.
   */
  public ReleaseManager setReleaseBranchStartPoint(@Nullable String value) {
    releaseBranchStartPoint = stripToEmpty(value);
    return this;
  }

  /**
   * Sets {@link #isRemotePushEnabled() remotePushEnabled}.
   */
  public ReleaseManager setRemotePushEnabled(boolean value) {
    remotePushEnabled = value;
    return this;
  }
}
