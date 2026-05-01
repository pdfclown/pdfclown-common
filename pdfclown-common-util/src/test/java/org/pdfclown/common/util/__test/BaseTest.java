/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BaseTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.__test;

import org.pdfclown.common.build.test.Test;
import org.pdfclown.common.build.test.assertion.CombinationVerifier;
import org.pdfclown.common.build.test.assertion.TupleVerifier;

/**
 * Module-specific unit test.
 *
 * @author Stefano Chizzolini
 */
public abstract class BaseTest extends Test {
  protected static final CombinationVerifier combinationVerifier = new CombinationVerifier();
  protected static final TupleVerifier tupleVerifier = new TupleVerifier();
}
