/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ServiceProvider.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.spi;

import static java.util.Comparator.comparingInt;
import static org.pdfclown.common.util.Objects.fqn;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.pdfclown.common.util.annot.Immutable;

/**
 * Pluggable extension based on the {@linkplain ServiceLoader SPI} mechanism.
 *
 * @author Stefano Chizzolini
 */
@Immutable
public interface ServiceProvider {
  /**
   * Retrieves available providers of the type, sorted by {@link #getPriority() priority}.
   *
   * @param <T>
   *          Provider type.
   * @return Stream sorted by ascending priority (the lower the priority, the better).
   */
  static <T extends ServiceProvider> Stream<T> discover(Class<T> providerType) {
    return doDiscover(providerType).filter(ServiceProvider::isAvailable);
  }

  /**
   * Retrieves all the providers of the type, whatever their {@link #isAvailable() availability},
   * sorted by {@link #getPriority() priority}.
   * <p>
   * Useful to reveal unavailable providers for diagnostic purposes.
   * </p>
   *
   * @param <T>
   *          Provider type.
   * @return Stream sorted by ascending priority (the lower the priority, the better).
   */
  static <T extends ServiceProvider> Stream<T> discoverAll(Class<T> providerType) {
    return doDiscover(providerType);
  }

  /**
   * Retrieves the best available provider of the type.
   *
   * @param <T>
   *          Provider type.
   */
  static <T extends ServiceProvider> Optional<T> discoverBest(Class<T> providerType) {
    return discover(providerType).findFirst();
  }

  private static <T extends ServiceProvider> Stream<T> doDiscover(Class<T> providerType) {
    var ret = ServiceLoader.load(providerType).stream()
        .map(ServiceLoader.Provider::get)
        .sorted(comparingInt(ServiceProvider::getPriority));

    if (Util.serviceProviderLog.isInfoEnabled()) {
      List<T> providers = ret.toList();
      {
        var b = new StringBuilder("DISCOVERED ").append(providerType.getName())
            .append(" implementations:");
        if (providers.isEmpty()) {
          b.append(" NONE");
        } else {
          for (T provider : providers) {
            b.append("\n  - %s (available: %s; priority: %s)".formatted(
                fqn(provider), provider.isAvailable() ? "YES" : "NO", provider.getPriority()));
          }
        }
        Util.serviceProviderLog.info(b.toString());
      }
      ret = providers.stream();
    }

    return ret;
  }

  /**
   * Implementation priority, that is a capability index used to rank available implementations (the
   * lesser, the better — zero means full capability).
   * <p>
   * Each implementation is expected to declare a priority comparable to other implementations of
   * the same {@linkplain ServiceProvider provider type} — for example, barcode renderers would
   * return their level of graphical inaccuracy (a ZXing-based renderer is, despite equivalent
   * encoding accuracy, less refined than an Okapi-based renderer as only the latter differentiates
   * bar lengths in 1D labels and adjusts the placement of human-readable symbols at character
   * level, making for a more professionally-looking rendering)
   * </p>
   */
  int getPriority();

  /**
   * Whether this implementation is available.
   * <p>
   * An implementation may require resources (such as optional dependencies) which can be checked at
   * runtime only.
   * </p>
   */
  boolean isAvailable();
}
