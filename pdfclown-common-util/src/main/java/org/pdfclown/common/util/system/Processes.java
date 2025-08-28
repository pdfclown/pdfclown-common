/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Processes.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

/**
 * Process utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Processes {
  /**
   * Executes a command, consuming its output.
   *
   * @param command
   *          Command along with its arguments.
   * @param consumer
   *          Consumes the process output line by line.
   * @return Exit code.
   */
  public static int execute(List<String> command, Consumer<String> consumer)
      throws IOException, InterruptedException {
    var processBuilder = new ProcessBuilder(command);
    Process process = processBuilder.start();
    try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        consumer.accept(line);
      }
    }
    return process.waitFor();
  }

  private Processes() {
  }
}
