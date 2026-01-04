/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Requires.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.annot;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated element <em>transitively</em> depends on
 * {@linkplain org.pdfclown.common.util.annot.DependsOn.Dependency optional dependencies}.
 * <p>
 * Callers can safely invoke the element, as the responsibility to handle missing dependencies is
 * upon the latter. <span class="important">Since callers become transitively dependent on those
 * dependencies, they MUST in turn annotate themselves with the same annotation, in order to
 * document their own dependencies.</span>
 * </p>
 *
 * @author Stefano Chizzolini
 * @see DependsOn
 */
@Documented
@Retention(RUNTIME)
@Target({ PACKAGE, TYPE, METHOD, CONSTRUCTOR })
public @interface Requires {
  /**
   * Optional dependencies.
   *
   * @return (format: <code>"groupId:artifactId"</code>)
   */
  String[] value();
}
