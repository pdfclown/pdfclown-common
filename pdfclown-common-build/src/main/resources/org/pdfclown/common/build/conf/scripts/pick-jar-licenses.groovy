/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (pick-jar-licenses.groovy) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block below this file
  header, listing the main changes you applied to the original source.
 */

/*-
  This script picks the licenses attributed to the code of the current module and puts them under
  the output directory at `META-INF/LICENSES`.

  BINDING VARIABLES:
  - outputDir (REQUIRED)
  - sourceDir (REQUIRED): must be either `{basedir}/src/main` or `{basedir}/src/test`
  - includes (OPTIONAL): list of GLOBs relative to the source (`java`) or resource (`resources`)
    level of `sourceDir` (for example, `org/pdfclown/core/test/**`). If undefined, all files under
    `sourceDir` are included.

  REQUIRES:
  - python 3.13+
  - reuse 6.2+ (python package)

  WARNING: License picking is based on a temporary SPDX report generated on the fly if missing; as
  it could become inadvertently stale, it is recommended to clean the build whenever the codebase
  may have changed.
 */

import static org.pdfclown.common.util.io.Files.normal

import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.util.regex.Pattern
import org.pdfclown.common.util.regex.Patterns

/*
  Package licenses only on deploy or non-CI environment!

  NOTE: License packaging requires external dependencies (namely `reuse` Python package) which are
  currently excluded from ordinary pdfClown.org CI builds.
 */
def ci = Boolean.parseBoolean(System.getenv('CI')) /* NOTE: Most CIs (GitHub Actions inclusive) set
                                                      `CI` environment variable to `true` */
def deploying = session.request.goals.contains('deploy') /* See
                                          `org.pdfclown.common.build.release.BuiltinStep.DEPLOY` */
if (ci && !deploying) {
  log.info("Non-deployment CI build detected -- execution SKIPPED")
  return
}

String outputDirValue = binding.variables['outputDir']?.trim() ?: ''
if (outputDirValue.isEmpty())
  throw new IllegalStateException("Property 'outputDir' REQUIRED")

String sourceDirValue = binding.variables['sourceDir']?.trim() ?: ''
if (sourceDirValue.isEmpty())
  throw new IllegalStateException("Property 'sourceDir' REQUIRED")

Path outputDir = Path.of(outputDirValue, 'META-INF/LICENSES')
Path rootDir = normal(Path.of(project.properties['rootdir'] as String))
Path baseDir = normal(project.basedir.toPath())
Path sourceDir = Path.of(sourceDirValue)
if (!sourceDir.startsWith(baseDir))
  throw new IllegalStateException("Property 'sourceDir' (${sourceDir}) MUST be under project " +
      "base directory (${baseDir})")

/*
  NOTE: `reuse` SBOM contains paths relative to the root (e.g. `module-a/src/main/...`), so the
  matching pattern MUST be defined accordingly.
 */
String includePrefix = rootDir.relativize(sourceDir).toString() + '/*/'
def includes = binding.variables['includes']?.split(',')*.trim()?.findAll() ?: [""]
String regex = "^(?:" + includes
    .collect { '(?:' + Patterns.globToRegex(includePrefix + it.replaceFirst('^/+', '')) + ')' }
    .join('|') + ")"
Pattern includesPattern = Pattern.compile(regex)

log.debug("PATTERN (globs: " + includes + "): " + includesPattern.pattern())

/*
  1. Generate the SPDX document if missing!

  WARNING: MUST be explicitly deleted in case of changes to the codebase (this should not be a
  problem on CI-based publishing (nightly builds and releases), as builds there are expected to be
  fresh and clean).
 */
Path sbomFile = rootDir.resolve('target/sbom.txt')
if (!Files.isRegularFile(sbomFile)) {
  def proc = new ProcessBuilder('reuse', 'spdx')
      .directory(rootDir.toFile())
      .start()
  def out = new StringBuilder()
  def err = new StringBuilder()
  proc.consumeProcessOutput(out, err)
  if (proc.waitFor() != 0)
    throw new IllegalStateException("reuse spdx failed:\n${err}")

  Files.createDirectories(sbomFile.parent)
  // Write to a temp file and move atomically, so a concurrent module never reads a partial file!
  Path tmp = Files.createTempFile(sbomFile.parent, 'sbom', '.tmp')
  Files.write(tmp, out.toString().getBytes(StandardCharsets.UTF_8))
  Files.move(tmp, sbomFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)

  log.info("${sbomFile} GENERATED")
}

// 2. Collect licenses attributed to this module's source files!
def usedLicenses = new TreeSet<String>()
String current = null
Files.readAllLines(sbomFile).each {
  if (it.startsWith('FileName:')) {
    current = it.substring('FileName:'.length()).trim() - ~/^\.\//
  } else if (current && it.startsWith('LicenseInfoInFile:') && current =~ includesPattern) {
    def license = it.substring('LicenseInfoInFile:'.length()).trim()
    // Purge "or later" operator from the license reference!
    if (license.endsWith('+')) {
      license = license.dropRight(1)
    }
    usedLicenses << license
  }
}
if (usedLicenses.isEmpty())
  throw new IllegalStateException("No licenses found under '${includesPattern.pattern()}'")

// 3. Copy the matching license files!
Files.createDirectories(outputDir)
Path licensesDir = rootDir.resolve('LICENSES')
def missing = []
usedLicenses.each {
  Path src = licensesDir.resolve("${it}.txt")
  if (Files.isRegularFile(src)) {
    Files.copy(src, outputDir.resolve(src.fileName), StandardCopyOption.REPLACE_EXISTING)

    log.info("* `${it}` license INCLUDED")
  } else {
    missing << it
  }
}
if (missing)
  throw new IllegalStateException("No file in `LICENSES/` for: ${missing.join(', ')}")
