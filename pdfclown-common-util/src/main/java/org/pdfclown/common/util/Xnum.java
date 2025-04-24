/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (Xnum.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

/**
 * Augmented enumeration.
 * <p>
 * Closes the gap between the inherently-rigid regular enumerations and flexibility-demanding use
 * cases.
 * </p>
 * <p>
 * See {@link BaseXnum} for further details on the way to implement this interface.
 * </p>
 *
 * @param <T>
 *          Domain-specific identity type.
 * @author Stefano Chizzolini
 */
public interface Xnum<T> extends XtEnum<T> {
}
