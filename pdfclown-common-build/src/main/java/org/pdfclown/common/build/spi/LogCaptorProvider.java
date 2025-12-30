/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (LogCaptorProvider.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.spi;

import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.test.assertion.LogCaptor;
import org.pdfclown.common.util.spi.ServiceProvider;

/**
 * {@link LogCaptor} provider.
 *
 * @author Stefano Chizzolini
 */
public interface LogCaptorProvider extends ServiceProvider {
  /**
   * Gets the {@link LogCaptor} factory corresponding to a logging implementation.
   *
   * @param implName
   *          (lower-case) Logging implementation name.
   * @return {@code LogCaptor} factory by logger name. {@code null}, if this provider doesn't
   *         support {@code implName}.
   */
  @Nullable
  Function<String, LogCaptor> getFactory(String implName);

  @Override
  default int getPriority() {
    return 0;
  }

  @Override
  default boolean isAvailable() {
    return true;
  }
}
