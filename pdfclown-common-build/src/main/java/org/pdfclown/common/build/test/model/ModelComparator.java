/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ModelComparator.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.model;

import java.util.List;

/**
 * Domain object comparator.
 *
 * @param <TModel>
 *          Model type.
 * @param <TDiff>
 *          Difference type.
 * @author Stefano Chizzolini
 */
public abstract class ModelComparator<TModel, TDiff> {
  /**
   * Compares objects.
   *
   * @return Differences.
   */
  public abstract List<TDiff> compare(TModel obj1, TModel obj2);
}
