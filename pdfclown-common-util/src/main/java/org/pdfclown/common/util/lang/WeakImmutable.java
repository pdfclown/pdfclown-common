/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (WeakImmutable.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.lang;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated type is
 * <a href="https://en.wikipedia.org/wiki/Immutable_object">weakly immutable</a> (ie, limited to its
 * own compilation unit — being extensible, its actual immutability depends on derived types).
 * <p>
 * Useful to inform implementers of derived types about the opportunity to make them immutable (in
 * case of closed inheritance, this will become redundant after moving to Java 17 (sealed classes)).
 * </p>
 * <h2>Requirements</h2>
 * <ul>
 * <li>class:
 * <ul>
 * <li>the annotated class has only {@code final} fields, which are themselves effectively immutable
 * (eg, arrays' defensive copy)</li>
 * <li>the parent class of the annotated class is itself weakly immutable</li>
 * </ul>
 * </li>
 * <li>interface:
 * <ul>
 * <li>members of the annotated interface with state-altering semantics (setters or other methods
 * with side effects) have {@linkplain UnsupportedOperationException unsupported} default
 * implementations; other semantic details may be possibly delegated to derived types (they should
 * be specified in the {@code @}implSpec section of javadoc)</li>
 * <li>all the parent interfaces of the annotated interface are themselves at least weakly
 * immutable</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
@Documented
@Retention(CLASS)
@Target(TYPE)
public @interface WeakImmutable {
}
