/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Listenable.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
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
