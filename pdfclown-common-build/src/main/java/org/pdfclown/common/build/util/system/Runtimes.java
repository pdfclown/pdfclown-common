/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Runtimes.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.util.system;

import java.lang.management.ManagementFactory;

/**
 * Runtime utilities.
 */
public final class Runtimes {
  private enum DebugSingleton {
    INSTANCE;

    final boolean debugging;

    DebugSingleton() {
      /*
       * Check if the Java Debug Wire Protocol (JDWP) agent is used (see also
       * <https://stackoverflow.com/a/73125047/1624781>).
       */
      debugging = ManagementFactory.getRuntimeMXBean()
          .getInputArguments().toString().contains("-agentlib:jdwp");
    }
  }

  /**
   * Whether the runtime is in debug mode (that is, the debug agent is used).
   *
   * @implNote The detection is based on the presence of
   *           <a href="https://en.wikipedia.org/wiki/Java_Debug_Wire_Protocol">Java Debug Wire
   *           Protocol (JDWP)</a> configuration.
   */
  public static boolean isDebugging() {
    return DebugSingleton.INSTANCE.debugging;
  }

  private Runtimes() {
  }
}
