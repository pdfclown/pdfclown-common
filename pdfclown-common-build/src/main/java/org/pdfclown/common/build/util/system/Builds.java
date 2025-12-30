/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Builds.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.util.system;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.apache.commons.lang3.SystemUtils.IS_OS_UNIX;
import static org.pdfclown.common.util.Chars.LF;
import static org.pdfclown.common.util.Chars.SQUARE_BRACKET_OPEN;
import static org.pdfclown.common.util.Conditions.requireDirectory;
import static org.pdfclown.common.util.Conditions.requireFile;
import static org.pdfclown.common.util.Conditions.requireState;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.unsupported;
import static org.pdfclown.common.util.Exceptions.wrongState;
import static org.pdfclown.common.util.Objects.objDo;
import static org.pdfclown.common.util.Objects.sqnd;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.system.Processes.execute;
import static org.pdfclown.common.util.system.Processes.osCommand;
import static org.pdfclown.common.util.system.Processes.unixCommand;
import static org.pdfclown.common.util.xml.Xmls.xml;
import static org.pdfclown.common.util.xml.Xmls.xpath;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.function.Failable;
import org.apache.commons.lang3.stream.Streams;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.system.MavenPathResolver;
import org.pdfclown.common.build.system.ProjectDirId;
import org.pdfclown.common.build.system.ProjectPathResolver;
import org.pdfclown.common.util.Ref;
import org.pdfclown.common.util.xml.Xmls.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Build utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Builds {
  private static final Logger log = LoggerFactory.getLogger(Builds.class);

  /**
   * <a href="https://maven.apache.org/pom.html">Maven Project Object Model (POM) 4.0</a> XML
   * namespace.
   */
  public static final String NS__POM = "http://maven.apache.org/POM/4.0.0";

  /**
   * Preferred XML prefix for {@link #NS__POM POM namespace}.
   */
  public static final String NS_PREFIX__POM = "pom";

  /**
   * Maven POM XPath.
   * <p>
   * Use {@value #NS_PREFIX__POM} to qualify elements in {@link #NS__POM POM namespace}.
   * </p>
   */
  public static final XPath XPATH__POM = xpath(XPath.Namespaces.of()
      .register(NS_PREFIX__POM, NS__POM));

  private static final Pattern PATTERN__MAVEN_LOG_LINE = Pattern.compile("\\[(\\w+?)] (.*)");

  private static @Nullable Path mavenExecutable;
  private static @Nullable Path mavenHome;
  private static final Map<Path, String> projectArtifactIds = new HashMap<>();

  /**
   * Gets the build classpath associated to a project.
   * <p>
   * The first element is the build directory containing the compiled classes of the project.
   * </p>
   * <p>
   * Useful for debugging purposes, to run a project through its bare compiled classes, without
   * packaging nor installation.
   * </p>
   *
   * @param projectDir
   *          Base directory of the project.
   * @param scope
   *          Scope threshold. Includes dependencies up to the scope, as described by the <a href=
   *          "https://maven.apache.org/plugins/maven-dependency-plugin/build-classpath-mojo.html#includeScope">{@code includeScope}
   *          parameter of dependency:build-classpath goal</a>:
   *          <ul>
   *          <li>runtime — runtime and compile dependencies</li>
   *          <li>compile — compile, provided, and system dependencies</li>
   *          <li>test — all dependencies (default)</li>
   *          <li>provided — just provided dependencies</li>
   *          <li>system — just system dependencies</li>
   *          </ul>
   * @throws RuntimeException
   *           if the execution failed.
   */
  public static List<Path> classpath(Path projectDir, @Nullable String scope)
      throws FileNotFoundException {
    var pathResolver = ProjectPathResolver.of(projectDir);
    if (pathResolver instanceof MavenPathResolver) {
      try {
        /*
         * NOTE: Parsing Maven Invoker's interactive output is a sloppy way to collect the classpath
         * from a POM, but for now it seems the most straightforward, sparing us the intricacies of
         * Maven API.
         */
        var ret = new ArrayList<Path>();

        Path pomFile = requireFile(pathResolver.resolve(ProjectDirId.BASE, "pom.xml"));

        // 1. Build directory.
        ret.add(requireDirectory(pathResolver.resolve(ProjectDirId.MAIN_TARGET)));

        // 2. Dependencies.
        var error = new StringBuilder();
        var invoker = new DefaultInvoker();
        invoker.execute(objDo(new DefaultInvocationRequest(), $ -> {
          $.setMavenHome(mavenHome().toFile());
          $.setPomFile(pomFile.toFile());
          $.setGoals(List.of("dependency:build-classpath"));
          if (scope != null) {
            $.addArg("-DincludeScope=" + scope);
          }
          $.setOutputHandler($line -> {
            /*
             * NOTE: The interactive output is preceded by level tags (for example "[INFO]") on each
             * log line except the classpath. Apparently, Maven error log lines ("[ERROR]" tag) are
             * sent to stdout, so `setErrorHandler(..)` is useless.
             */
            Matcher logMatcher = PATTERN__MAVEN_LOG_LINE.matcher($line);
            if (logMatcher.find()) {
              if (logMatcher.group(1).equals(Level.ERROR.toString())) {
                error.append(LF).append(logMatcher.group(2));
              }
            } else if (!$line.startsWith(S + SQUARE_BRACKET_OPEN)) {
              Streams.of($line.split(File.pathSeparator))
                  .map($$ -> {
                    try {
                      return Path.of($$);
                    } catch (InvalidPathException ex) {
                      return null;
                    }
                  })
                  .filter($$ -> $$ != null && exists($$))
                  .forEachOrdered(ret::add);
            }
          });
          $.setInputStream(
              new ByteArrayInputStream(
                  new byte[0]) /* Just to avoid interactive mode complaining */);
        }));
        if (error.length() > 0)
          throw runtime("Classpath retrieval from {} FAILED: {}",
              pathResolver.resolve(ProjectDirId.BASE), error);

        return ret;
      } catch (MavenInvocationException ex) {
        throw runtime("Classpath retrieval from {} FAILED", pathResolver.resolve(ProjectDirId.BASE),
            ex);
      }
    } else
      throw unsupported("Project type NOT SUPPORTED: {}", sqnd(pathResolver));
  }

  /**
   * Path of the Maven command (mvn).
   *
   * @throws IllegalStateException
   *           if not found.
   */
  public static synchronized Path mavenExecutable() {
    if (mavenExecutable == null) {
      /*
       * NOTE: `mavenHome()` is responsible to set `mavenExecutable` along with itself; if missing,
       * it throws a nice exception suggesting how to fix the configuration.
       */
      mavenHome();
    }
    return mavenExecutable;
  }

  /**
   * Path of the Maven home.
   * <p>
   * Detection algorithm (the first valid path wins):
   * </p>
   * <ol>
   * <li>check system property <code>maven.home</code></li>
   * <li>check environment variable <code>MAVEN_HOME</code></li>
   * <li>check Maven executable version information (<code>mvn -v</code>)</li>
   * <li>check Maven executable location (Unix only. Bash shell: <code>whereis mvn</code>)</li>
   * </ol>
   *
   * @throws IllegalStateException
   *           if not found.
   */
  public static synchronized Path mavenHome() {
    if (mavenHome != null)
      return mavenHome;

    // Environment.
    {
      String mavenHomeString;
      if ((mavenHomeString = System.getProperty("maven.home")) != null) {
        if (detectMavenHome(Path.of(mavenHomeString)))
          return mavenHome;

        log.warn("Maven home ({}) from system property `maven.home` INVALID", mavenHomeString);
      }
      if ((mavenHomeString = System.getenv("MAVEN_HOME")) != null) {
        if (detectMavenHome(Path.of(mavenHomeString)))
          return mavenHome;

        log.warn("Maven home ({}) from environment variable `MAVEN_HOME` INVALID", mavenHomeString);
      }
    }

    // Shell query.
    try {
      /*
       * Query `mvn` command itself!
       *
       * NOTE: `mvn -v` command returns, among its version information, its home path.
       */
      if (detectMavenHome(shellPath(osCommand("mvn -v"),
          $line -> $line.startsWith("Maven home:")
              ? $line.substring("Maven home:".length()).trim()
              : null)))
        return mavenHome;

      if (IS_OS_UNIX) {
        /*
         * Query the shell for `mvn` location!
         *
         * NOTE: Interactive shell mode ensures `PATH` environment variable comprises additional
         * execution locations configured in user-specific files like ".bashrc".
         */
        Path mavenCommad = shellPath(unixCommand("whereis mvn", /* interactive: */true),
            $line -> $line.startsWith("mvn:")
                ? $line.substring("mvn:".length()).trim()
                : null);
        if (mavenCommad != null
            /*
             * NOTE: Maven command is at "%mavenHome%/bin/mvn", so we have to climb 2 ancestors to
             * reach home; since the command may be linked, we have to resolve it before walking the
             * filesystem hierarchy.
             */
            && detectMavenHome(mavenCommad.toRealPath().getParent().getParent()))
          return mavenHome;
      }
    } catch (IOException ex) {
      log.warn("Maven home retrieval FAILED", ex);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }

    throw wrongState("""
        Maven home NOT FOUND: specify it through system property `maven.home` or environment \
        variable `MAVEN_HOME`""");
  }

  /**
   * Gets the artifact ID of the project a path belongs to.
   *
   * @param path
   *          An arbitrary position within a project.
   * @return {@code null}, if {@code path} is outside any project.
   * @throws IllegalStateException
   *           if the artifact ID was missing from the project.
   * @throws RuntimeException
   *           if the access to project metadata failed.
   * @implNote Currently supports Maven only.
   */
  public static @Nullable String projectArtifactId(Path path) {
    Path dir = isDirectory(path) ? path : path.getParent();
    while (dir != null) {
      if (projectArtifactIds.containsKey(dir))
        return projectArtifactIds.get(dir);

      var pomFile = dir.resolve("pom.xml");
      if (exists(pomFile)) {
        return projectArtifactIds.computeIfAbsent(dir, Failable.asFunction(
            $k -> requireState(stripToNull(
                XPATH__POM.nodeValue(NS_PREFIX__POM + ":project/" + NS_PREFIX__POM + ":artifactId",
                    xml(pomFile))),
                () -> "`artifactId` NOT FOUND in " + pomFile)));
      }

      dir = dir.getParent();
    }
    return null;
  }

  /**
   * Stores the path, along with the associated command (mvn), if it corresponds to Maven home.
   *
   * @return Whether {@code path} is Maven home.
   */
  private static boolean detectMavenHome(@Nullable Path path) {
    if (path == null)
      return false;
    else if (!isDirectory(path)) {
      log.warn("Maven home ({}) INVALID", path);

      return false;
    }

    Path executablePath;
    if (exists(executablePath = path.resolve("bin/mvn"))
        || exists(executablePath = path.resolve("bin/mvn.cmd"))) {
      mavenHome = path;
      mavenExecutable = executablePath;

      return true;
    } else {
      log.warn("Maven command (mvn) NOT FOUND");

      return false;
    }
  }

  /**
   * Extracts an existing path from the output of the shell command.
   *
   * @return {@code null}, if not found.
   */
  private static @Nullable Path shellPath(List<String> args,
      Function<String, @Nullable String> consumer)
      throws IOException, InterruptedException {
    var retRef = new Ref<@Nullable Path>();
    execute(args, null, $line -> {
      if (retRef.isEmpty()) {
        var result = consumer.apply($line);
        if (result != null) {
          Path path;
          if (exists(path = Path.of(result))) {
            retRef.set(path);
          }
        }
      }
    });
    return retRef.get();
  }

  private Builds() {
  }
}
