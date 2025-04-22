/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (Ref.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common> (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.util.lang;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

/**
 * Mutable object wrapper.
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
