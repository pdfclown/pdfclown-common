/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ServiceProviderNotFoundException.java) is part of pdfclown-common-build module in
  pdfClown Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.temp.util.spi;

import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.pdfclown.common.build.internal.temp.util.function.Functions.toElse;
import static org.pdfclown.common.util.Objects.fqn;
import static org.pdfclown.common.util.Strings.EMPTY;

import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.temp.util.ElementNotFoundException;
import org.pdfclown.common.util.spi.ServiceProvider;

/**
 * Thrown if no {@link ServiceProvider} of the given {@linkplain #getProviderType() type} matched
 * the referenced criteria.
 *
 * @author Stefano Chizzolini
 */
@SuppressWarnings("serial")
public class ServiceProviderNotFoundException extends ElementNotFoundException {
  private final Class<? extends ServiceProvider> providerType;

  public ServiceProviderNotFoundException(Object ref, Class<? extends ServiceProvider> providerType,
      @Nullable String message, @Nullable Throwable cause) {
    super(ref, "SPI: %s%s".formatted(fqn(providerType), toElse(stripToNull(message),
        $ -> " -- " + $, EMPTY)), cause);

    this.providerType = providerType;
  }

  /**
   * {@link ServiceProvider} type.
   */
  public Class<? extends ServiceProvider> getProviderType() {
    return providerType;
  }
}
