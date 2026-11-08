/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (init-build-info.groovy) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block below this file
  header, listing the main changes you applied to the original source.
 */
/*
  This script prepares information used during the build.

  BINDING VARIABLES:
  - commitTime (REQUIRED)
  - commitIdMetadata (REQUIRED)
  - commitDirty (OPTIONAL)
 */

import java.time.ZonedDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

log.info("")
log.info("Build information ".padRight(72, '-'))

String commitTime = binding.variables['commitTime']?.trim() ?: ''
if (commitTime.isEmpty())
  throw new IllegalStateException("Required property 'commitTime' is not set")

String commitIdMetadata = binding.variables['commitIdMetadata']?.trim() ?: ''

boolean commitDirty = binding.variables['commitDirty']?.trim()?.equalsIgnoreCase('true')

/*
  Timestamp for reproducible builds (see
  <https://maven.apache.org/guides/mini/guide-reproducible-builds.html>).

  NOTE: Reproducible builds are meaningful for SCM-traceable code only; therefore, the current
  commit's timestamp is used only if the working tree is clean. In case of dirty working tree, the
  timestamp is generated on the fly, following the volatility of the built code.
 */
def buildTimestamp = commitDirty
    ? ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS)
    .format(DateTimeFormatter.ISO_INSTANT)
    : commitTime

// Share the build timestamp as property for interpolation!
session.userProperties.setProperty('buildTimestamp', buildTimestamp)

log.info("* buildTimestamp: " + buildTimestamp +
    " (" + (commitDirty ? "DIRTY" : "REPRODUCIBLE") + ")")

/*
  Metadata-augmented implementation version (SemVer2).

  In case of SNAPSHOT, appends metadata to the version to fully identify the build it originated
  from.
 */
def implementationVersion = project.version
if (implementationVersion.endsWith("SNAPSHOT")) {
  if (commitIdMetadata.isEmpty())
    throw new IllegalStateException("Required property 'commitIdMetadata' is not set")

  implementationVersion += "+" + commitIdMetadata
}

// Share the implementation version as property for interpolation!
session.userProperties.setProperty('implementationVersion', implementationVersion)

log.info("* implementationVersion: " + implementationVersion)
