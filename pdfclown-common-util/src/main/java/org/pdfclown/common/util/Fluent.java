/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (Fluent.java) is part of pdfclown-common-util module in pdfClown Common project (this
  Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.util;

import static org.pdfclown.common.util.Objects.objCast;

import java.util.function.Consumer;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.lang.Immutable;

/**
 * <a href="https://en.wikipedia.org/wiki/Fluent_interface">Fluent</a> object.
 *
 * @author Stefano Chizzolini
 */
@Immutable
public interface Fluent {
  /**
   * Applies the given action to this object.
   * <p>
   * <span class="warning">WARNING: This method leverages unchecked generic casts: because of their
   * double-edged nature (they can elegantly adapt to the receiving context but are also prone to
   * runtime failure in case of type incompatibility), you should use it carefully and declare its
   * {@code T} parameter wherever any ambiguity may occur.</span>
   * </p>
   *
   * @param <T>
   *          Target type.
   * @param action
   *          Action applied to this object.
   * @return This object.
   */
  @SuppressWarnings("unchecked")
  default <T> T act(Consumer<? super T> action) {
    T self = (T) this;
    action.accept(self);
    return self;
  }

  /**
   * Casts this object to a target type.
   * <p>
   * <span class="warning">WARNING: This method leverages unchecked generic casts: because of their
   * double-edged nature (they can elegantly adapt to the receiving context but are also prone to
   * runtime failure in case of type incompatibility), you should use it carefully and declare its
   * {@code T} parameter wherever any ambiguity may occur.</span>
   * </p>
   *
   * @param <T>
   *          Target type.
   * @return This object.
   */
  @SuppressWarnings("unchecked")
  default <T> T cast() {
    return (T) this;
  }

  /**
   * Casts this object to the given target type.
   * <p>
   * The purpose of this method is to allow a safe cast without pre-check.
   * </p>
   *
   * @return {@code null} if this is assignment-incompatible with {@code type}.
   */
  default <T> @Nullable T cast(Class<T> type) {
    return objCast(this, type);
  }

  /**
   * Transforms this object according to the given mapper.
   * <p>
   * <span class="warning">WARNING: This method leverages unchecked generic casts: because of their
   * double-edged nature (they can elegantly adapt to the receiving context but are also prone to
   * runtime failure in case of type incompatibility), you should use it carefully and declare its
   * {@code T} parameter wherever any ambiguity may occur.</span>
   * </p>
   *
   * @param <T>
   * @param <R>
   * @param mapper
   *          Object transformer.
   * @return Transformed object.
   */
  @SuppressWarnings("unchecked")
  default <T, R> R to(Function<? super T, R> mapper) {
    return mapper.apply((T) this);
  }
}
