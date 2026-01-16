/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Processes.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.system;

import static org.apache.commons.lang3.SystemUtils.IS_OS_UNIX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.apache.commons.lang3.SystemUtils.OS_NAME;
import static org.pdfclown.common.util.Chars.LF;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Objects.objTo;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.Ref;

/**
 * Process utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Processes {
  /**
   * Synchronously executes a command.
   *
   * @param command
   *          Command to execute, along with its arguments.
   * @param directory
   *          Working directory (defaults to the working directory of the current Java process,
   *          usually the directory named by the system property {@code user.dir}).
   * @return Exit code.
   * @see #executeElseThrow(List, Path)
   */
  public static int execute(List<String> command, @Nullable Path directory)
      throws IOException, InterruptedException {
    /*
     * TODO: To date, there is an open bug affecting IntelliJ IDEA
     * <https://youtrack.jetbrains.com/issue/IDEA-258730> as, apparently,
     * `ProcessBuilder.inheritIO()` causes the execution to hang.
     *
     * Furthermore, remotely debugging maven (`mvnDebug`) causes the execution in IntelliJ IDEA to
     * hang if a maven command (`mvn`) is run via `ProcessBuilder`.
     */
    var builder = new ProcessBuilder(command)
        .directory(objTo(directory, Path::toFile))
        .inheritIO();
    Process process = builder.start();
    return process.waitFor();
  }

  /**
   * Synchronously executes a command, consuming its output.
   *
   * @param command
   *          Command to execute, along with its arguments.
   * @param directory
   *          Working directory (defaults to the working directory of the current Java process,
   *          usually the directory named by the system property {@code user.dir}).
   * @param consumer
   *          Consumes the process output line by line.
   * @return Exit code.
   * @see #executeElseThrow(List, Path, Consumer)
   */
  public static int execute(List<String> command, @Nullable Path directory,
      Consumer<String> consumer) throws IOException, InterruptedException {
    var builder = new ProcessBuilder(command)
        .directory(objTo(directory, Path::toFile));
    Process process = builder.start();
    try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        consumer.accept(line);
      }
    }
    return process.waitFor();
  }

  /**
   * Synchronously executes a command.
   *
   * @param command
   *          Command to execute, along with its arguments.
   * @param directory
   *          Working directory (defaults to the working directory of the current Java process,
   *          usually the directory named by the system property {@code user.dir}).
   * @param outputRef
   *          Process output.
   * @return Exit code.
   * @see #executeGet(List, Path)
   * @see #executeGetElseThrow(List, Path)
   */
  public static int execute(List<String> command, @Nullable Path directory, Ref<String> outputRef)
      throws IOException, InterruptedException {
    var b = new StringBuilder();
    var ret = execute(command, directory, $ -> b.append($).append(LF));
    outputRef.set(b.toString());
    return ret;
  }

  /**
   * Synchronously executes a command.
   *
   * @param command
   *          Command to execute, along with its arguments.
   * @param directory
   *          Working directory (defaults to the working directory of the current Java process,
   *          usually the directory named by the system property {@code user.dir}).
   * @throws ProcessException
   *           if the process returns a non-zero exit code.
   * @see #execute(List, Path)
   */
  public static void executeElseThrow(List<String> command, @Nullable Path directory)
      throws IOException, InterruptedException, ProcessException {
    int exitCode = execute(command, directory);
    if (exitCode != 0)
      throw new ProcessException(exitCode, String.join(S + SPACE, command), EMPTY);
  }

  /**
   * Synchronously executes a command, consuming its output.
   *
   * @param command
   *          Command to execute, along with its arguments.
   * @param directory
   *          Working directory (defaults to the working directory of the current Java process,
   *          usually the directory named by the system property {@code user.dir}).
   * @param consumer
   *          Consumes the process output line by line.
   * @throws ProcessException
   *           if the process returns a non-zero exit code.
   * @see #execute(List, Path, Consumer)
   */
  public static void executeElseThrow(List<String> command, @Nullable Path directory,
      Consumer<String> consumer) throws IOException, InterruptedException {
    int exitCode = execute(command, directory, consumer);
    if (exitCode != 0)
      throw new ProcessException(exitCode, String.join(S + SPACE, command), EMPTY);
  }

  /**
   * Gets the output from the executed command.
   * <p>
   * NOTE: This method ignores the exit code of the executed process; if significant, use
   * {@link #executeGetElseThrow(List, Path)} (or {@link #execute(List, Path, Ref)}) instead.
   * </p>
   *
   * @param command
   *          Command to execute, along with its arguments.
   * @param directory
   *          Working directory (defaults to the working directory of the current Java process,
   *          usually the directory named by the system property {@code user.dir}).
   * @see #execute(List, Path, Ref)
   * @see #executeGetElseThrow(List, Path)
   */
  public static String executeGet(List<String> command, @Nullable Path directory)
      throws IOException, InterruptedException {
    var outputRef = new Ref<String>();
    execute(command, directory, outputRef);
    return outputRef.get();
  }

  /**
   * Gets the output from the executed command.
   *
   * @param command
   *          Command to execute, along with its arguments.
   * @param directory
   *          Working directory (defaults to the working directory of the current Java process,
   *          usually the directory named by the system property {@code user.dir}).
   * @throws ProcessException
   *           if the process returns a non-zero exit code.
   * @see #execute(List, Path, Ref)
   * @see #executeGet(List, Path)
   */
  public static String executeGetElseThrow(List<String> command, @Nullable Path directory)
      throws IOException, InterruptedException {
    var outputRef = new Ref<String>();
    int exitCode = execute(command, directory, outputRef);
    if (exitCode != 0)
      throw new ProcessException(exitCode, String.join(S + SPACE, command), EMPTY);

    return outputRef.get();
  }

  /**
   * Prepares a command for the current OS.
   * <p>
   * Spares users from breaking commands into parts.
   * </p>
   */
  public static List<String> osCommand(String command) {
    return osCommand(command, false);
  }

  /**
   * Prepares a command for the current OS.
   * <p>
   * Spares users from breaking commands into parts.
   * </p>
   *
   * @param interactive
   *          Whether the command has to run in interactive shell (this may load additional
   *          configuration, depending on the system; for example, Bash shell loads
   *          {@code ~/.bashrc} file).
   */
  public static List<String> osCommand(String command, boolean interactive) {
    if (IS_OS_UNIX)
      return unixCommand(command, interactive);
    else if (IS_OS_WINDOWS)
      return winCommand(command);
    else
      throw unexpected(OS_NAME);
  }

  /**
   * Prepares a Unix command (Bash shell).
   * <p>
   * Spares users from breaking commands into parts.
   * </p>
   */
  public static List<String> unixCommand(String command) {
    return unixCommand(command, false);
  }

  /**
   * Prepares a Unix command (Bash shell).
   * <p>
   * Spares users from breaking commands into parts.
   * </p>
   */
  public static List<String> unixCommand(String command, boolean interactive) {
    return List.of("bash", interactive ? "-ci" : "-c", command);
  }

  /**
   * Prepares a Windows command.
   * <p>
   * Spares users from breaking commands into parts.
   * </p>
   */
  public static List<String> winCommand(String command) {
    return List.of("cmd", "/C", command);
  }

  private Processes() {
  }
}
