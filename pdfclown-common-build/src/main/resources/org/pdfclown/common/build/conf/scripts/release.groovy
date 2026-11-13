/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (release.groovy) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block below this file
  header, listing the main changes you applied to the original source.
 */

/*-
  This script launches the project release to public artifact repository.
 */

import static org.pdfclown.common.build.Builds.runHook
import static org.pdfclown.common.util.Conditions.requireState
import static org.pdfclown.common.util.Strings.stripToNull

import org.pdfclown.common.build.release.ReleaseManager
import org.pdfclown.common.util.meta.SemVer1
import org.pdfclown.common.util.meta.SemVer2

final def USER_PROPERTY__DRYRUN = "dryRun"
final def USER_PROPERTY__RELEASE_STARTPOINT = "release.startpoint"
final def USER_PROPERTY__RELEASE_VERSION = "release.version"

final def releaseVersion = requireState(
    stripToNull(session.userProperties[USER_PROPERTY__RELEASE_VERSION]),
    "`${USER_PROPERTY__RELEASE_VERSION}` user property MISSING")

def projectDir = project.basedir.toPath()

def manager = new ReleaseManager(projectDir, SemVer2.of(releaseVersion).to(SemVer1.class)
    .toString())
    .setReleaseBranchStartPoint(session.userProperties[USER_PROPERTY__RELEASE_STARTPOINT])
    .setDeploymentProfiles("publish,reuse")
    .setInstallationProfiles("full,reuse")
    .setRemotePushEnabled(
        System.getenv("MAVEN_GPG_PASSPHRASE") != null /* Activates remote push only if GPG signing
                                                         is configured */)
    .setDryRun(session.userProperties[USER_PROPERTY__DRYRUN] != null)

runHook("onReleaseBegin", new Binding([manager: manager]), projectDir)

manager.execute()

runHook("onReleaseEnd", new Binding([manager: manager]), projectDir)
