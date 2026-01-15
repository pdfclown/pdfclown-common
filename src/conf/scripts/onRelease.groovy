/*
  This script updates project release version references in the documentation.
 */

import static java.nio.file.Files.readString
import static java.nio.file.Files.walkFileTree
import static java.nio.file.Files.writeString
import static org.pdfclown.common.build.internal.temp.util.system.Processes.executeGetElseThrow
import static org.pdfclown.common.util.Exceptions.runtime
import static org.pdfclown.common.util.Exceptions.wrongState
import static org.pdfclown.common.util.Objects.found
import static org.pdfclown.common.util.system.Processes.osCommand;

import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.regex.Pattern
import org.apache.commons.lang3.mutable.MutableInt
import org.pdfclown.common.build.release.ReleaseManager
import org.pdfclown.common.build.release.Step
import org.pdfclown.common.util.meta.SemVer1
import org.slf4j.event.Level


// javadoc.io URL reference pattern.
final var javadocUrlRegExFormat = "(org.pdfclown/pdfclown-common-[^/]+/)%s(/)"


var stepIndex = manager.steps.findIndexOf({ it.name == "RELEASE_POM_UPDATE" })
if (!found(stepIndex))
  throw wrongState("RELEASE_POM_UPDATE step NOT FOUND")

manager.steps.add(++stepIndex, new Step() {
  @Override
  void execute(ReleaseManager manager) {
    /*
      NOTE: The previous release version corresponds to the annotated tag closest to current commit
      in current branch (for example, "v0.2.1"), stripped of its "v" prefix. It is loaded as
      `SemVer1` to ensure its value is valid.

      IMPORTANT: Such version resolution MUST occur within this step, as it depends on the currently
      checked-out branch.
     */
    String oldReleaseVersion = SemVer1.of(executeGetElseThrow(osCommand("git describe --abbrev=0"),
        manager.baseDir).substring(1)).toString()

    manager.log(Level.INFO, "Updating release version references ({} -> {}) in files...",
        oldReleaseVersion, manager.releaseVersion)

    /*
      Traversing the repository to update project release version references...
     */
    final var counter = new MutableInt()
    final var dirNameExcludeFilter = Pattern.compile("\\..*|src|target").asMatchPredicate()
    final var fileNameIncludeFilter = Pattern.compile(".*\\.md").asMatchPredicate()
    final var javadocUrlPattern = Pattern.compile(javadocUrlRegExFormat
        .formatted(oldReleaseVersion))
    final var javadocUrlReplacement = "\$1%s\$2".formatted(manager.releaseVersion)
    walkFileTree(manager.baseDir, new SimpleFileVisitor<Path>() {
        @Override
        FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr)
            throws IOException {
          if (dirNameExcludeFilter.test(dir.getFileName().toString()))
            return FileVisitResult.SKIP_SUBTREE;
          else
            return FileVisitResult.CONTINUE;
        }

        @Override
        FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
          if (fileNameIncludeFilter.test(file.getFileName().toString())) {
            try {
              var content = readString(file)
              var newContent = javadocUrlPattern.matcher(content).replaceAll(javadocUrlReplacement)
              if (!newContent.equals(content)) {
                writeString(file, newContent)
                counter.increment()

                manager.log(Level.INFO, "-> {}", file)
              } else {
                manager.log(Level.DEBUG, "{}", file)
              }
            } catch (Exception ex) {
              throw runtime("Project release version references update in {} FAILED", file, ex)
            }
          }
          return FileVisitResult.CONTINUE;
        }
      });
    /*
      NOTE: The assumption here is that, due to required Javadoc references such as badges, in the
      project there MUST be release version references to update; if no file was updated, there has
      to be an issue in the matching algorithm.
     */
    if(counter.intValue() == 0)
      throw runtime("No project release version reference updated")

    manager.log(Level.INFO, "Updated files: {}", counter.intValue())
  }

  @Override
  String getName() {
    return "RELEASE_DOCS_UPDATE:SCRIPT"
  }

  @Override
  boolean isReadOnly() {
    return false
  }

  @Override
  String toString() {
    return getName()
  }
})