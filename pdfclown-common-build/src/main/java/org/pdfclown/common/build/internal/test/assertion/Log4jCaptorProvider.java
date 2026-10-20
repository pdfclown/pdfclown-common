/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Log4jCaptorProvider.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.test.assertion;

import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.spi.LogCaptorProvider;
import org.pdfclown.common.build.test.assertion.LogCaptor;

/**
 * {@link Log4jCaptor} provider.
 *
 * @author Stefano Chizzolini
 */
public class Log4jCaptorProvider implements LogCaptorProvider {
  @Override
  public @Nullable Function<String, LogCaptor> getFactory(String implName) {
    return implName.contains("log4j") ? Log4jCaptor::new : null;
  }

  @Override
  public int getPriority() {
    return 0;
  }

  @Override
  public boolean isAvailable() {
    return true;
  }
}
