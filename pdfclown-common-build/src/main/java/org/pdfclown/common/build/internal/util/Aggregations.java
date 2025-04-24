/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (Aggregations.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Aggregation (ie, collection or map) utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Aggregations {
  // SourceFQN: org.pdfclown.common.util.Aggregations.cartesianProduct(..)
  /**
   * Gets the Cartesian product of the given lists.
   *
   * @return Sequential stream.
   */
  public static Stream<List<Object>> cartesianProduct(List<List<?>> lists) {
    return cartesianProduct(lists, 0);
  }

  private static Stream<List<Object>> cartesianProduct(List<List<?>> lists, int index) {
    if (index == lists.size())
      return Stream.of(new ArrayList<>());

    return lists.get(index).stream()
        .flatMap($ -> cartesianProduct(lists, index + 1)
            .map($$ -> {
              var newList = new ArrayList<Object>($$);
              newList.add(0, $);
              return newList;
            }));
  }

  private Aggregations() {
  }
}
