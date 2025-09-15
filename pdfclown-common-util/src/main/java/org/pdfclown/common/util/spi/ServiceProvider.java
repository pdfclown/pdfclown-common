/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ServiceProvider.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.spi;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.pdfclown.common.util.annot.Immutable;

/**
 * Pluggable extension based on the {@link ServiceLoader} mechanism.
 * <p>
 * Services consumed by pdfClown are expected to comply with this protocol.
 * </p>
 *
 * @author Stefano Chizzolini
 */
@Immutable
public interface ServiceProvider {
  /**
   * Retrieves available providers of the type, sorted by priority.
   *
   * @param <T>
   *          Provider type.
   * @return Immutable list, sorted by ascending priority (the lower the priority, the better).
   */
  static <T extends ServiceProvider> List<T> discover(Class<T> providerType) {
    return collect(providerType, doDiscover(providerType).filter(ServiceProvider::isAvailable));
  }

  /**
   * Retrieves all the providers of the type, whatever their status, sorted by priority.
   * <p>
   * Useful to reveal unavailable providers for diagnostic purposes.
   * </p>
   *
   * @param <T>
   *          Provider type.
   * @return Immutable list, sorted by ascending priority (the lower the priority, the better).
   */
  static <T extends ServiceProvider> List<T> discoverAll(Class<T> providerType) {
    return collect(providerType, doDiscover(providerType));
  }

  /**
   * Retrieves the best available provider of the type.
   *
   * @param <T>
   *          Provider type.
   */
  static <T extends ServiceProvider> T discoverBest(Class<T> providerType) {
    List<T> providers = discover(providerType);
    return !providers.isEmpty() ? providers.get(0) : null;
  }

  private static <T extends ServiceProvider> List<T> collect(Class<T> providerType,
      Stream<T> providerStream) {
    var ret = providerStream.collect(toUnmodifiableList());

    if (Util.serviceProviderLog.isInfoEnabled()) {
      var b = new StringBuilder("DISCOVERED ").append(providerType.getName())
          .append(" implementations:");
      if (ret.isEmpty()) {
        b.append(" NONE");
      } else {
        for (T provider : ret) {
          b.append("\n  - ").append(provider.getClass().getName()).append(" (status: ")
              .append(provider.isAvailable() ? "OK" : "N/A").append("; priority: ")
              .append(provider.getPriority()).append(")");
        }
      }
      Util.serviceProviderLog.info(b.toString());
    }

    return ret;
  }

  private static <T extends ServiceProvider> Stream<T> doDiscover(Class<T> providerType) {
    return ServiceLoader.load(providerType).stream().map(ServiceLoader.Provider::get)
        .sorted(Comparator.comparingInt(ServiceProvider::getPriority));
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
