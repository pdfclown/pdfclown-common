/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (init-checkstyle.groovy) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  This script patches the builtin Checkstyle configuration (namely, `google_checks.xml`) with
  custom pdfClown.org settings.

  BINDING VARIABLES:
  - resourcesDir (OPTIONAL)

  REQUIRES `com.puppycrawl.tools:checkstyle` dependency

  NOTE: This arrangement is necessary due to license incompatibility (LGPL 2.1 vs LGPL 3.0) which
  prohibits bundling.
 */

import static java.nio.charset.StandardCharsets.ISO_8859_1
import static org.pdfclown.common.util.io.Files.normal

import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import java.nio.file.Files
import java.nio.file.Path
import org.pdfclown.common.util.io.Resource
import org.pdfclown.common.util.xml.Xmls
import org.w3c.dom.Document

log.info("")
log.info("Checkstyle configuration merger ".padRight(72, '-'))

Path rootDir = normal(Path.of(project.properties['rootdir'] as String))
Path targetFile = rootDir.resolve("target/checkstyle-checks.xml")
if (Files.exists(targetFile)) {
  log.info("Merged checkstyle `${targetFile}` present -- execution SKIPPED")
  return
}

final String CONFIG_FILENAME = "google_checks"

String resourcesDirValue = binding.variables['resourcesDir']?.trim() ?: ''
if (resourcesDirValue.startsWith('${') /* NOTE: If the variable was assigned from the interpolation
                                          over an undefined parameter, its literal representation is
                                          received, and MUST be treated as undefined */) {
  resourcesDirValue = ''
}

def sourceResource = Resource.of("${CONFIG_FILENAME}.xml")
def patchResource = Resource.of(
    "${resourcesDirValue}/org/pdfclown/common/build/conf/checkstyle/${CONFIG_FILENAME}.patch.xsl")

/*
  TODO: When `Xmls.Security` replaces logs with thrown exceptions, remove these redundant feature
  checks.
 */
// Hardened factories (NOTE: Xmls.Security only LOGS hardening failures; verify them explicitly).
DocumentBuilderFactory dbf = Xmls.Security.secure(DocumentBuilderFactory.newDefaultInstance())
TransformerFactory tf = Xmls.Security.secure(TransformerFactory.newDefaultInstance())
// Fail fast: abort the build if any hardening didn't take effect!
[
    (XMLConstants.FEATURE_SECURE_PROCESSING)                        : true,
    'http://apache.org/xml/features/disallow-doctype-decl'          : true,
    'http://xml.org/sax/features/external-general-entities'         : false,
    'http://xml.org/sax/features/external-parameter-entities'       : false,
    'http://apache.org/xml/features/nonvalidating/load-external-dtd': false
].each { name, expected ->
  if (dbf.getFeature(name) != expected)
    throw new IllegalStateException("XML parser hardening failed: $name")
}
if (!tf.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING))
  throw new IllegalStateException("XSLT hardening failed: secure processing")
if (tf.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD) != '')
  throw new IllegalStateException("XSLT hardening failed: external DTD access")
if (tf.getAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET) != '')
  throw new IllegalStateException("XSLT hardening failed: external stylesheet access")

/*
  Load the document stripping its DOCTYPE for security!

  NOTE: Anything unexpected is left in place and rejected by the parser (disallow-doctype-decl).
 */
def sourceContent = new String(sourceResource.openStream().withCloseable { it.readAllBytes() },
    ISO_8859_1)
sourceContent = sourceContent.replaceFirst(/<!DOCTYPE[^>\[]*>/, '')
Document sourceXml = Xmls.xml(new ByteArrayInputStream(sourceContent.getBytes(ISO_8859_1)), dbf)

Transformer transformer = patchResource.openStream().withCloseable {
  tf.newTransformer(new StreamSource(it))
}

Files.createDirectories(targetFile.parent)
Files.newOutputStream(targetFile).withCloseable {
  transformer.transform(new DOMSource(sourceXml), new StreamResult(it))
}
log.info("Merged Checkstyle `${targetFile}` DONE")
