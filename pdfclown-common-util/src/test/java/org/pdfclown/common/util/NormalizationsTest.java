/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (NormalizationsTest.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.pdfclown.common.build.test.assertion.Verifiers.COMBINATION;
import static org.pdfclown.common.util.ConditionsTest.STRING_NAMES;
import static org.pdfclown.common.util.ConditionsTest.STRING_VALUES;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings("Convert2MethodRef")
class NormalizationsTest extends BaseTest {
  @Test
  void normalizeToNonEmpty() {
    //noinspection DataFlowIssue : null intended
    COMBINATION.verify(
        (value, name) -> Normalizations.normalizeToNonEmpty(value, name),
        List.of("value", "name"),
        // value
        STRING_VALUES,
        // name
        STRING_NAMES);
  }
}