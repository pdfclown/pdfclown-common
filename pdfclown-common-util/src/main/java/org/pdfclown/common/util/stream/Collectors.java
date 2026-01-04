/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Collectors.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;

/**
 * Collector utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Collectors {
  /**
   * Gets a collector that accumulates the input elements into a mutable list in
   * {@linkplain Collections#reverse(List) reverse} order.
   *
   * @see <a href="https://www.dontpanicblog.co.uk/2020/10/23/reverse-order-stream/">Reverse order a
   *      Stream in Java</a>
   */
  public static <T> Collector<T, ?, List<T>> toReversedList() {
    return collectingAndThen(toCollection(ArrayList::new), $ -> {
      Collections.reverse($);
      return $;
    });
  }

  private Collectors() {
  }
}
