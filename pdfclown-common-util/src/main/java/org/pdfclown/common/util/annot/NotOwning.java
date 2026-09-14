/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (NotOwning.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.annot;

/**
 * Indicates that ownership should NOT be transferred to the receiver of the annotated element for
 * the purposes of resource disposal, to prevent leaking.
 * <p>
 * Similar to <a href=
 * "https://checkerframework.org/api/org/checkerframework/checker/mustcall/qual/NotOwning.html">@NotOwning
 * (Checker Framework)</a>.
 * </p>
 * <p>
 * Default:
 * </p>
 * <ul>
 * <li>{@linkplain java.lang.annotation.ElementType#FIELD FIELD} (value): {@code @NotOwning}</li>
 * <li>{@linkplain java.lang.annotation.ElementType#CONSTRUCTOR CONSTRUCTOR} (new instance):
 * {@link Owning @Owning} (fixed)</li>
 * <li>{@linkplain java.lang.annotation.ElementType#METHOD METHOD} (return value):
 * {@link Owning @Owning}</li>
 * <li>{@linkplain java.lang.annotation.ElementType#PARAMETER PARAMETER} (value):
 * {@code @NotOwning}</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
public @interface NotOwning {
}
