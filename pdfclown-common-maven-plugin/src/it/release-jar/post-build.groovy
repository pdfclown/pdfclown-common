/*
  SPDX-FileCopyrightText: 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later
 */
import java.nio.file.*
import java.util.zip.*

ret = true

repoDir = new File(basedir, "target/remote-repo")
filebasePath = 'org/pdfclown/it/release-jar/1.0/release-jar-1.0'
for (path : [
    filebasePath + '.pom',
    filebasePath + '.pom.md5',
    filebasePath + '.pom.sha1',
    filebasePath + '.jar',
    filebasePath + '.jar.md5',
    filebasePath + '.jar.sha1'
    ]) {
  file = new File(repoDir, path)
  println("- checking $file")
  if (!file.isFile())
    throw new FileNotFoundException(file.toString())

  if (file.name.endsWith('.pom')) {
    expectedFile = new File(basedir, "expected/release-jar-1.0/pom.xml.regex")
    println("-- checking for match with expected content ($expectedFile)")
    assert file.text ==~ expectedFile.text
  } else if (file.name.endsWith('.jar')) {
    destPath = new File(basedir, 'target/temp').toPath()
    Files.createDirectories(destPath)

    println "-- inspecting contents"
    try (ZipFile archive = new ZipFile(file)) {
      expectedEntryNames = [
        'META-INF/maven/org.pdfclown.it/release-jar/pom.properties',
        'META-INF/maven/org.pdfclown.it/release-jar/pom.xml',
        'META-INF/MANIFEST.MF',
        ]
      for (ZipEntry entry : archive.entries()) {
        entryDestPath = destPath.resolve(entry.name)
        if (entry.isDirectory()) {
          if (!Files.exists(entryDestPath)) {
            Files.createDirectory(entryDestPath)
          }
          continue
        }

        if (!expectedEntryNames.remove(entry.name)) {
          ret = false
          println "--- entry UNEXPECTED: ${entry.name}"
          continue
        }

        Files.copy(archive.getInputStream(entry), entryDestPath, StandardCopyOption.REPLACE_EXISTING)

        entryFile = entryDestPath.toFile()
        expectedFile = new File(basedir, "expected/release-jar-1.0/${entryFile.name}.regex")
        if (expectedFile.isFile()) {
          println("--- entry ${entry.name}: checking for match with expected content ($expectedFile)")
          assert entryFile.text ==~ expectedFile.text
        }
      }
      if (ret) {
        ret = expectedEntryNames.isEmpty()
      }
    }
  }
}

return ret
