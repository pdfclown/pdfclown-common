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
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Exceptions.wrongState;
import static org.pdfclown.common.build.internal.util_.Objects.objDo;
import static org.pdfclown.common.build.internal.util_.ParamMessage.ARG;
import static org.pdfclown.common.build.internal.util_.Strings.S;
import static org.pdfclown.common.build.internal.util_.Strings.SQUARE_BRACKET_OPEN;
import static org.pdfclown.common.build.internal.util_.Strings.strNormToNull;
import static org.pdfclown.common.build.internal.util_.system.Processes.runWithConsumer;
import static org.pdfclown.common.build.internal.util_.xml.Xmls.xml;
import static org.pdfclown.common.build.internal.util_.xml.Xmls.xpath;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.lang3.function.Failable;
import org.apache.commons.lang3.stream.Streams;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.util_.Ref;
import org.pdfclown.common.build.internal.util_.xml.Xmls.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Builds {
  private static final Logger log = LoggerFactory.getLogger(Builds.class);

  /**
   * <a href="https://maven.apache.org/pom.html">Maven Project Object Model (POM) 4.0</a> namespace.
   */
  public static final String NS__POM = "http://maven.apache.org/POM/4.0.0";

  public static final String NS_PREFIX__POM = "pom";

  /**
   * Maven POM XPath.
   * <p>
   * Use {@value #NS_PREFIX__POM} to qualify elements in {@link #NS__POM POM namespace}.
   * </p>
   */
  public static final XPath XPATH__POM = xpath(XPath.Namespaces.of()
      .register(NS_PREFIX__POM, NS__POM));

  private static @Nullable Path mavenCommand;
  private static @Nullable Path mavenHome;
  private static final Map<Path, String> projectArtifactIds = new HashMap<>();

  /**
   * Gets the build classpath associated to a project.
   * <p>
   * Useful for debugging purposes, to run a project through its bare compiled classes, without
   * packaging nor installation.
   * </p>
   * <p>
   * The first element is the build directory containing the compiled classes of the project.
   * </p>
   *
   * @param projectDir
   *          Base directory of the project.
   * @param scope
   *          Scope threshold. Includes dependencies up to the given scope, as described by the
   *          {@code includeScope} parameter of <a href=
   *          "https://maven.apache.org/plugins/maven-dependency-plugin/build-classpath-mojo.html#includeScope">dependency:build-classpath
   *          goal</a>:
   *          <ul>
   *          <li>runtime — runtime and compile dependencies</li>
   *          <li>compile — compile, provided, and system dependencies</li>
   *          <li>test — all dependencies (default)</li>
   *          <li>provided — just provided dependencies</li>
   *          <li>system — just system dependencies</li>
   *          </ul>
   */
  public static List<Path> classpath(Path projectDir, @Nullable String scope) {
    Path projectBuildDir = projectDir.resolve("target/classes");
    if (!isDirectory(projectBuildDir))
      throw wrongState("Build directory MISSING: " + ARG, projectBuildDir);

    var ret = new ArrayList<Path>();
    ret.add(projectBuildDir);

    /*
     * NOTE: This is a quick and dirty way to generate a classpath from a POM -- parsing interactive
     * output ain't no good, but for now it seems the most straightforward, sparing us the
     * intricacies of the Maven API.
     */
    var request = new DefaultInvocationRequest();
    {
      request.setPomFile(projectDir.resolve("pom.xml").toFile());
      request.setGoals(Collections.singletonList("dependency:build-classpath"));
      if (scope != null) {
        request.addArg("-DincludeScope=" + scope);
      }
    }
    var invoker = new DefaultInvoker();
    {
      var mavenHome = mavenHome();
      if (mavenHome == null)
        throw wrongState("Maven home NOT FOUND: "
            + "specify it through `MAVEN_HOME` environment variable");

      invoker.setMavenHome(mavenHome.toFile());
      invoker.setInputStream(new ByteArrayInputStream(new byte[0]));
      invoker.setOutputHandler($line -> {
        /*
         * NOTE: The interactive output is preceded by level tags (e.g. "[INFO]") on each log line
         * except the classpath.
         */
        if (!$line.startsWith(S + SQUARE_BRACKET_OPEN)) {
          Streams.of($line.split(File.pathSeparator))
              .map($ -> {
                try {
                  return Path.of($);
                } catch (InvalidPathException ex) {
                  return null;
                }
              })
              .filter($ -> $ != null && exists($))
              .forEachOrdered(ret::add);
        }
      });
    }
    try {
      invoker.execute(request);
    } catch (MavenInvocationException ex) {
      throw runtime("Classpath retrieval from '" + ARG + "' FAILED", projectDir, ex);
    }
    return ret;
  }

  /**
   * Path of the Maven command (mvn).
   *
   * @return {@code null}, if not found.
   */
  public static synchronized @Nullable Path mavenCommand() {
    if (mavenCommand == null) {
      /*
       * NOTE: `mavenHome()` is responsible to set `mavenCommand` along with itself.
       */
      mavenHome();
    }
    return mavenCommand;
  }

  /**
   * Path of the Maven home.
   * <p>
   * As soon as one of the following entries corresponds to an existing directory The first
   * Detection algorithm:
   * </p>
   * <ol>
   * <li>check <code>MAVEN_HOME</code> envinroment variable</li>
   * </ol>
   *
   * @return {@code null}, if not found.
   */
  public static synchronized @Nullable Path mavenHome() {
    if (mavenHome != null)
      return mavenHome;

    // Environment.
    {
      String mavenHomeString = System.getenv("MAVEN_HOME");
      if (mavenHomeString != null) {
        if (detectMavenHome(Path.of(mavenHomeString)))
          return mavenHome;

        log.warn("Maven home ({}) from environment variable `MAVEN_HOME` INVALID",
            mavenHomeString);
      }
    }

    // Shell query.
    try {
      /*
       * Query `mvn` command itself!
       *
       * NOTE: `mvn -v` command returns, among its version information, its home path.
       */
      //noinspection DataFlowIssue : objDo is @PolyNull
      if (detectMavenHome(shellPath(
          objDo(new ArrayList<>(3), $ -> {
            if (IS_OS_WINDOWS) {
              $.add("cmd.exe");
              $.add("/C");
            } else {
              $.add("bash");
              $.add("-c");
            }
            $.add("mvn -v");
          }),
          $line -> $line.startsWith("Maven home:")
              ? $line.substring("Maven home:".length()).trim()
              : null)))
        return mavenHome;

      if (!IS_OS_WINDOWS) {
        /*
         * Query the Bash shell for `mvn` location!
         *
         * NOTE: Interactive shell mode ensures `PATH` environment variable comprises additional
         * execution locations configured in user-specific files like ".bashrc".
         */
        Path mavenCommad = shellPath(List.of("bash", "-ic", "whereis mvn"),
            $line -> $line.startsWith("mvn:")
                ? $line.substring("mvn:".length()).trim()
                : null);
        if (mavenCommad != null
            && detectMavenHome(mavenCommad.toRealPath().getParent().getParent()))
          /*
           * NOTE: Maven command is at "%mavenHome%/bin/mvn", so we have to climb 2 ancestors to
           * reach home; since the command may be linked, we have to resolve it before walking the
           * filesystem hierarchy.
           */
          return mavenHome;
      }
    } catch (IOException ex) {
      log.warn("Maven home retrieval FAILED", ex);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
    return mavenHome;
  }

  /**
   * Gets the artifact ID of the project a path belongs to.
   *
   * @param path
   *          An arbitrary position within a project.
   * @return {@code null}, if {@code path} is outside any project.
   * @throws NullPointerException
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
            $k -> requireNonNull(strNormToNull(
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

    Path commandPath;
    if (exists(commandPath = path.resolve("bin/mvn"))) {
      mavenHome = path;
      mavenCommand = commandPath;

      return true;
    } else {
      log.warn("Maven command ({}) NOT FOUND", commandPath);

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
    runWithConsumer(args, $line -> {
      var result = consumer.apply($line);
      if (result != null) {
        Path path;
        if (exists(path = Path.of(result))) {
          retRef.set(path);
        }
        return true;
      }

      return false;
    });
    return retRef.get();
  }

  private Builds() {
  }
}
