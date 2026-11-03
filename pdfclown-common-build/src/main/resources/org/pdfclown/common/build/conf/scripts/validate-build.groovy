/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (validate-build.groovy) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  This script validates the current build.
 */

checkManualProfiles()

/**
 * Prohibits the manual activation/deactivation of state profiles.
 * <p>See "STATE PROFILES" in `pdfclown-common-super` POM for further information.</p>
 */
void checkManualProfiles() {
  final String PREFIX__STATE_PROFILE = '@'

  def problems = []

  def invalidEnabledProfiles = session.request.activeProfiles.findAll {
    it.startsWith(PREFIX__STATE_PROFILE)
  }
  if (invalidEnabledProfiles) {
    problems << "${invalidEnabledProfiles} enabled"
  }

  def invalidDisabledProfiles = session.request.inactiveProfiles.findAll {
    it.startsWith(PREFIX__STATE_PROFILE)
  }
  if (invalidDisabledProfiles) {
    problems << "${invalidDisabledProfiles} disabled"
  }

  if (problems)
    throw new IllegalStateException("Implicit-activation profiles (${problems.join('; ')}) " +
        "cannot be set manually")
}
