/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Ref.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import java.io.Serial;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

/**
 * Mutable wrapper.
 *
 * @param <T>
 *          Value type.
 * @apiNote Useful to mimic by-reference argument semantics.
 * @author Stefano Chizzolini
 */
public final class Ref<T> extends MutableObject<@Nullable T> {
  @Serial
  private static final long serialVersionUID = 1L;

  public Ref() {
  }

  public Ref(@Nullable T value) {
    super(value);
  }

  /**
   * Sets {@link #get() value} as undefined.
   */
  public void clear() {
    set(null);
  }

  /**
   * Whether {@link #get() value} is undefined.
   */
  public boolean isEmpty() {
    return get() == null;
  }

  /**
   * Whether {@link #get() value} is defined.
   */
  public boolean isPresent() {
    return get() != null;
  }

  /**
   * Sets {@link #get() value}.
   */
  public void set(@Nullable T value) {
    setValue(value);
  }
}
