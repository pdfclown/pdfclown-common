/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Experimental.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.annot;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated element, member of the public API, is unstable.
 * <p>
 * This annotation implies that, since the feature is still evolving, the API may be subject to
 * incompatible changes (even removal) in a future release.
 * </p>
 * <p>
 * Similar to <a href=
 * "https://guava.dev/releases/snapshot-jre/api/docs/com/google/common/annotations/Beta.html">@Beta
 * (Guava)</a>.
 * </p>
 *
 * @author Stefano Chizzolini
 */
@Documented
@Retention(CLASS)
@Target({ TYPE, FIELD, CONSTRUCTOR, METHOD })
public @interface Experimental {
  /**
   * Description.
   */
  String value() default "";
}
