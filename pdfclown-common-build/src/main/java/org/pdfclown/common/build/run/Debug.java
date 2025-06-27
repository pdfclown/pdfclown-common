/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Debug.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.run;

import static java.lang.System.out;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Objects.fqnd;
import static org.pdfclown.common.build.internal.util_.Objects.objToLiteralString;
import static org.pdfclown.common.build.internal.util_.Objects.sqnd;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;
import static org.pdfclown.common.build.internal.util_.Strings.SPACE;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Scanner;
import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.system.LogManager;
import org.pdfclown.common.build.system.LogManager.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import picocli.CommandLine;
import picocli.CommandLine.Option;

/**
 * Debugging launcher.
 *
 * @author Stefano Chizzolini
 */
public abstract class Debug {
  /**
   * CLI arguments.
   * <p>
   * Represents the deserialized form of the raw CLI arguments.
   * </p>
   *
   * @author Stefano Chizzolini
   */
  public abstract static class CliArgs {
    /**
     * Whether verbose logging is enabled.
     */
    @Option(names = { "-d", "--debug" }, description = "Whether verbose logging is enabled.")
    public boolean debug;

    /**
     * Whether interactive mode is enabled.
     */
    @Option(names = { "-i", "--interact" }, description = "Whether interactive mode is enabled.")
    public boolean interactive;

    /**
     * Whether {@linkplain jdk.jfr Flight Recorder} data is dumped to file (named after the launcher
     * class).
     */
    @Option(names = { "-r", "--record" }, description = "Whether Flight Recorder data is dumped"
        + " to file (named after the launcher class).")
    public boolean recording;

    /**
     * Whether usage synopsis is requested to display.
     */
    @Option(names = { "-h", "?" }, usageHelp = true, description = "This help message.")
    public boolean usageHelpRequested;

    /**
     * Validates the deserialized arguments.
     * <p>
     * In case of missing arguments, enters interactive mode if {@linkplain #interactive enabled}.
     * </p>
     */
    protected void validate() {
      if (interactive) {
        if (!recording) {
          recording = promptArg("recording",
              "whether you want Flight Recorder to dump diagnostic data to file", "[y/N]")
                  .equalsIgnoreCase("y");
        }
      }
    }
  }

  private static final Logger log = LoggerFactory.getLogger(Debug.class);

  /**
   * Terminates the currently running Java Virtual Machine.
   * <p>
   * If {@code message} or {@code ex} is defined, the process has failed and exit code {@code 1} is
   * returned.
   * </p>
   *
   * @param message
   *          Failure message.
   * @param ex
   *          Failure exception.
   * @param cli
   *          Command line to print usage synopsis.
   */
  protected static void exit(@Nullable String message, @Nullable Throwable ex,
      @Nullable CommandLine cli) {
    var failed = (message = stripToNull(message)) != null || ex != null;
    if (failed) {
      log.error(requireNonNullElse(message, "Exception UNHANDLED"), ex);
    }

    if (cli != null) {
      cli.usage(out);
      out.println();
    }

    System.exit(failed ? 1 : 0);
  }

  /**
   * Initializes the CLI execution of the given debug application.
   *
   * @param debugType
   *          Debug application class.
   * @param title
   *          Debug application title.
   * @param cliArgs
   *          Debug application arguments.
   * @return Command line for the given arguments.
   */
  protected static CommandLine init(Class<? extends Debug> debugType, String title,
      CliArgs cliArgs) {
    LogManager.applyProfile(Profile.CLI);

    out.printf("\n%s (%s)\n\n", title, fqnd(debugType));

    return new CommandLine(cliArgs)
        .setCommandName(sqnd(debugType))
        .setCaseInsensitiveEnumValuesAllowed(true)
        .setUsageHelpWidth(100);
  }

  /**
   * Loads and validates the given CLI arguments.
   *
   * @param cli
   *          Command line created via {@link #init(Class, String, CliArgs) init(..)}.
   * @param args
   *          CLI arguments to parse.
   * @return {@code cli}
   * @implNote This method is decoupled from {@link #init(Class, String, CliArgs) init(..)} to allow
   *           {@code cli} customization.
   */
  protected static CommandLine parseArgs(CommandLine cli, String... args) {
    var cliArgs = (CliArgs) cli.getCommand();
    try {
      cli.parseArgs(args);
      if (cliArgs.usageHelpRequested) {
        exit(null, null, cli);
      }

      cliArgs.validate();
    } catch (Exception ex) {
      exit("Arguments validation FAILED", ex, cli);
    }

    out.println("\nARGUMENTS:");
    for (Field field : cliArgs.getClass().getFields()) {
      try {
        out.printf("  %s: %s\n", field.getName(), field.get(cliArgs));
      } catch (IllegalAccessException ex) {
        throw runtime(ex) /* Should NEVER happen */;
      }
    }
    out.println();

    if (cliArgs.debug) {
      LogManager.setLevel(Level.DEBUG);
    }

    return cli;
  }

  /**
   * Requests to enter via CLI the value of the given argument.
   *
   * @param name
   *          Argument name.
   * @param description
   *          Argument description.
   * @param hint
   *          Value suggestion (e.g., "[y/N]" for a boolean (yes/no) where {@code false} (no) is
   *          default).
   * @return Entered value.
   */
  protected static String promptArg(String name, String description, @Nullable String hint) {
    out.printf("-> %s (ENTER %s)%s: ", name, description,
        (hint = stripToNull(hint)) != null ? SPACE + hint : EMPTY);
    return new Scanner(System.in).nextLine();
  }

  private @Nullable Recording record;

  /**
   * Activates diagnostic recording.
   * <p>
   * The recorded data is dumped in the current working directory to a ".jfr" file named after this
   * debugging launcher.
   * </p>
   *
   * @see jdk.jfr
   */
  public void record() {
    record(Path.of(sqnd(this) + ".jfr"));
  }

  /**
   * Activates diagnostic recording.
   *
   * @param file
   *          File to dump the recorded data to.
   * @see jdk.jfr
   */
  public void record(Path file) {
    file = requireNonNull(file, "`file`").toAbsolutePath();

    try {
      record = new Recording(Configuration.getConfiguration("default"));
      record.setDestination(file);
    } catch (IOException | ParseException ex) {
      if (record != null) {
        record.close();
        record = null;
      }
      throw runtime(ex);
    }
  }

  /**
   * Launches the execution.
   */
  public final void run() {
    onStart();

    try {
      doRun();
    } finally {
      onEnd();
    }
  }

  /**
   * Executes the core debug logic.
   */
  protected abstract void doRun();

  /**
   * @implSpec Implementations MUST call their overridden counterpart respecting proper call nesting
   *           with {@link #onStart()}, like this:<pre>
   * &#64;Override
   * protected void onStart(){
   *   super.onStart();
   *   . . .
   * }
   *
   * &#64;Override
   * protected void onEnd(){
   *   . . .
   *   super.onEnd();
   * }</pre>
   * @see #onStart()
   */
  protected void onEnd() {
    if (record != null) {
      record.stop();

      log.info("Flight recorder FINISHED (dumped to {})",
          objToLiteralString(record.getDestination()));
    }
  }

  /**
   * @see #onEnd()
   */
  protected void onStart() {
    if (record != null) {
      record.start();

      log.info("Flight recorder RUNNING...");
    }
  }
}
