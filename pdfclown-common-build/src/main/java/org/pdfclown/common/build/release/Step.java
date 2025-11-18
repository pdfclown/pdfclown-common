/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Step.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.release;

/**
 * Release step.
 *
 * @author Stefano Chizzolini
 */
public interface Step {
  /**
   * Executes this step.
   */
  void execute(ReleaseManager releaseManager) throws Exception;

  /**
   * Step name.
   */
  String getName();

  /**
   * Whether this step doesn't change any state.
   */
  boolean isReadOnly();
}
