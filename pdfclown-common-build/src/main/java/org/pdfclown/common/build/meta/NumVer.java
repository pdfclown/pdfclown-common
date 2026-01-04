/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (NumVer.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.meta;

import static java.lang.Math.min;

/**
 * Numeric version.
 *
 * @author Stefano Chizzolini
 */
public interface NumVer extends Version<NumVer> {
  /**
   * Gets the value at the given version field.
   */
  int get(int index);

  @Override
  default boolean isRegular() {
    return true;
  }

  @Override
  default int precedence(NumVer other) {
    if (this == other)
      return 0;

    for (int i = 0, l = min(this.size(), other.size()); i < l; i++) {
      int ret = this.get(i) - other.get(i);
      if (ret != 0)
        return ret;
    }
    return this.size() - other.size();
  }

  /**
   * Number of fields in this version.
   */
  int size();
}
