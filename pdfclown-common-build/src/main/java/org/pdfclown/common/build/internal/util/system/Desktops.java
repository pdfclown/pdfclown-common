/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Desktops.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util.system;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * GUI utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Desktops {
  /**
   * Copies the given data to the system clipboard.
   * <p>
   * IMPORTANT: Before calling this method, {@linkplain #isGUI() check whether a windowing system is
   * present}.
   * </p>
   *
   * @throws IllegalStateException
   *           if the clipboard is unavailable.
   * @implNote On windowing systems with active selection (such as
   *           <a href="https://en.wikipedia.org/wiki/X_Window_selection">X Window</a>), data must
   *           be transferred to the target client before the source client (this process)
   *           terminates, which implies, in the context of an inherently ephemeral test process,
   *           that:
   *           <ol>
   *           <li>there must be a clipboard manager to hold the data until the user actually pastes
   *           it</li>
   *           <li>the process must hold on enough to let the clipboard manager complete the
   *           transfer to its own buffer — previously, when no delay was applied, running this
   *           method caused erratic behavior, randomly missing the transfer</li>
   *           </ol>
   */
  public static void copyToClipboard(String data) {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(new StringSelection(data), null);

    try {
      //  Delay process termination enough to let the clipboard manager acquire the data!
      Thread.sleep(250);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Gets whether a windowing system is present in this environment.
   */
  public static boolean isGUI() {
    return !GraphicsEnvironment.isHeadless();
  }

  private Desktops() {
  }
}
