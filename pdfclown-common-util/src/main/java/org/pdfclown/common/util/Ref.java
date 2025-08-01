/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Ref.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

/**
 * Mutable wrapper.
 * <p>
 * Useful to mimic by-reference argument semantics.
 * </p>
 *
 * @param <T>
 *          Value type.
 * @author Stefano Chizzolini
 */
public final class Ref<T> extends MutableObject<T> {
  private static final long serialVersionUID = 1L;

  public Ref() {
  }

  public Ref(@Nullable T value) {
    super(value);
  }

  /**
   * Sets {@link #getValue() value} as undefined.
   */
  public void clear() {
    setValue(null);
  }

  /**
   * Whether {@link #getValue() value} is undefined.
   */
  public boolean isEmpty() {
    return getValue() == null;
  }

  /**
   * Whether {@link #getValue() value} is defined.
   */
  public boolean isPresent() {
    return getValue() != null;
  }
}
