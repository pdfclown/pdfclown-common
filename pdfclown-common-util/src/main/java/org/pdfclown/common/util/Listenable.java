/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (Listenable.java) is part of pdfclown-common-util module in pdfClown Common project
  (this Program).

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

import org.jspecify.annotations.NonNull;

/**
 * Observable object.
 *
 * @param <T>
 *          Listener type.
 * @author Stefano Chizzolini
 */
public interface Listenable<T extends Listener> {
  /**
   * Adds the given listener as a weak reference.
   *
   * @param listener
   * @return Self.
   */
  Listenable<T> addListener(@NonNull T listener);

  /**
   * Removes the given listener.
   *
   * @param listener
   * @return Whether the given listener was removed.
   */
  boolean removeListener(@NonNull T listener);
}
