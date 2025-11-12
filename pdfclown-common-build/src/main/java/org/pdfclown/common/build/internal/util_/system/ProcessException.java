/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ProcessException.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_.system;

import static org.pdfclown.common.build.internal.util_.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.build.internal.util_.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.build.internal.util_.Chars.SEMICOLON;
import static org.pdfclown.common.build.internal.util_.Chars.SPACE;

/**
 * Process execution failure.
 *
 * @author Stefano Chizzolini
 */
public class ProcessException extends RuntimeException {
  private static String buildMessage(int exitCode, String command, String message) {
    var b = new StringBuilder();
    if (!message.isEmpty()) {
      b.append(message).append(SPACE).append(ROUND_BRACKET_OPEN);
    }
    b.append("exitCode:").append(SPACE).append(exitCode);
    if (!command.isEmpty()) {
      b.append(SEMICOLON).append(SPACE)
          .append("command:").append(SPACE).append(command);
    }
    if (!message.isEmpty()) {
      b.append(ROUND_BRACKET_CLOSE);
    }
    return b.toString();
  }

  private final int exitCode;
  private final String command;

  /**
  */
  public ProcessException(int exitCode, String command, String message) {
    super(buildMessage(exitCode, command, message));

    this.command = command;
    this.exitCode = exitCode;
  }

  public String getCommand() {
    return command;
  }

  public int getExitCode() {
    return exitCode;
  }
}
