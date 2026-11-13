/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (check-reuse.groovy) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block below this file
  header, listing the main changes you applied to the original source.
 */

/*-
  This script checks that the copyright attributions are complete, both at file level (via
  `reuse lint` command) and at project level (components listed in `THIRD-PARTY-NOTICES` file are
  checked against the codebase, thus preventing attribution drift).

  REQUIRES:
  - python 3.13+
  - reuse 6.2+ (python package)
 */

// REUSE-IgnoreStart
import groovy.json.JsonSlurper

def rootDir = new File(project.properties['rootdir'] as String).toPath().toAbsolutePath()
    .normalize().toFile()
if (project.basedir != rootDir) {
  log.warn("To execute on root module only -- execution SKIPPED")
  return
}

def sbomFile = new File(rootDir, "target/sbom.json")
sbomFile.parentFile.mkdirs()

// Generate the SPDX report!
def proc = new ProcessBuilder('reuse', 'lint', '--json')
    .directory(rootDir)
    .redirectOutput(sbomFile)
    .redirectError(ProcessBuilder.Redirect.INHERIT)
    .start()
if (proc.waitFor() != 0) {
  // Re-run in plain mode to get human-readable output!
  def out = new StringBuilder()
  proc = new ProcessBuilder('reuse', 'lint')
      .directory(rootDir)
      .redirectError(ProcessBuilder.Redirect.INHERIT)
      .start()
  proc.waitForProcessOutput(out, null)
  throw new IllegalStateException("`reuse lint --json` FAILED:\n${out}")
}
log.debug("${sbomFile} GENERATED")

log.info("")
log.info("[OK] Project is compliant with the REUSE Specification.")

def report = new JsonSlurper().parse(sbomFile)

// Collect all reported files!
def allFiles = []
allFiles.addAll(report.files)

// Extract expected copyright metadata from each file!
def expectedCopyrights = [] as Set
allFiles.each { file ->
  /*
    NOTE: Copyright may appear either as `copyrights` collection or as `copyright` element.
   */
  def fileCopyrights = file.copyrights ?: (file.copyright ? [file.copyright] : [])
  fileCopyrights.each {
    def copyrightText = it['value'].toString()
        .replace("Copyright ", "")
        .replace("SPDX-FileCopyrightText: ", "")
        .replace("SPDX-SnippetCopyrightText: ", "")
        .trim()
    if (copyrightText) {
      expectedCopyrights.add(copyrightText)
    }
  }
}

// Extract actual copyright metadata from `THIRD-PARTY-NOTICES`!
def thirdPartyNoticesFile = new File(rootDir, "THIRD-PARTY-NOTICES.txt")
def actualCopyrights = new LinkedHashSet<String>() // Keeps insertion order.
thirdPartyNoticesFile.eachLine {
  def m = it =~ /(?i)^\s*Copyright:\s*(.+?)\s*$/
  if (m.find()) {
    actualCopyrights << m.group(1)
  }
}

// Diff actual end expected copyright metadata!
def confDir = new File(rootDir, 'src/conf/check-reuse'); confDir.mkdirs()
def expectedMissingFile = new File(confDir, 'third-party-missing.expected.txt')
def expectedMissing = expectedMissingFile.exists()
    ? (expectedMissingFile.readLines() as Set)
    : ([] as Set)
def expectedUnexpectedFile = new File(confDir, 'third-party-unexpected.expected.txt')
def expectedUnexpected = expectedUnexpectedFile.exists()
    ? (expectedUnexpectedFile.readLines() as Set)
    : ([] as Set)
def missing = expectedCopyrights - actualCopyrights - expectedMissing
def unexpected = actualCopyrights - expectedCopyrights - expectedUnexpected
if (missing || unexpected) {
  def sb = new StringBuilder()
  sb << "\nCopyright entries in `${thirdPartyNoticesFile.name}` DO NOT correspond to actual ones:\n"
  if (missing) {
    // EXPECTED missing?
    if (session.userProperties['test.expected.update']) {
      missing += expectedMissing
      expectedMissingFile.text = missing.join('\n')
      missing = null
    }
    // UNEXPECTED missing.
    else {
      sb << "  MISSING (${missing.size()}):\n"
      missing.sort().each { sb << "    - $it\n" }
    }
  }
  if (unexpected) {
    // EXPECTED unexpected?
    if (session.userProperties['test.expected.update']) {
      unexpected += expectedUnexpected
      expectedUnexpectedFile.text = unexpected.join('\n')
      unexpected = null
    }
    // UNEXPECTED unexpected.
    else {
      sb << "  UNEXPECTED (${unexpected.size()}):\n"
      unexpected.sort().each { sb << "    + $it\n" }
    }
  }
  if (missing || unexpected)
    throw new IllegalStateException(sb.toString())
}

def matching = actualCopyrights.intersect(expectedCopyrights) // true positives
log.info("""[OK] Copyright entries (${matching.size()}) in `${thirdPartyNoticesFile.name}` \
correspond to actual ones (${expectedMissing.size() + expectedUnexpected.size()} others ignored). \
WARNING: Since REUSE metadata doesn't track the third-party component associated to a copyright \
attribution, false negatives may occur in case the copyright happens to be the same across \
multiple components: always keep track of components as soon as they are added to or removed from \
the codebase!""")
// REUSE-IgnoreEnd
