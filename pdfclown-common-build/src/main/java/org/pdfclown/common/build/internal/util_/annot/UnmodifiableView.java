/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UnmodifiableView.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_.annot;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated type or type use is an unmodifiable view, that is observers have
 * read-only access to the backing object.
 * <p>
 * <b>View unmodifiability</b> is about the <i>stability of the externally-observable object state,
 * referenced objects exclusive, against observers' write access</i> (that is,
 * <span class="important">the state of the backing object may be mutable per-se, but the observers
 * cannot mutate it</span>); it is looser than {@linkplain Unmodifiable real unmodifiability} and
 * {@linkplain Immutable immutability}.
 * </p>
 * <p>
 * <b>Externally-observable state</b> comprises values and object references directly associated to
 * the class, and the objects indirectly associated to the class through object references. Mutable
 * private fields which don't influence the externally-observable state are irrelevant (for example,
 * defensive copy of arrays and other mutable objects makes them effectively immutable;
 * <a href="https://en.wikipedia.org/wiki/Memoization">memoization</a> doesn't affect the
 * externally-observable state).
 * </p>
 * <table>
 * <caption>Stability on type use</caption>
 * <tr>
 * <th rowspan="2">State</th>
 * <th colspan="3">Stability</th>
 * </tr>
 * <tr>
 * <th>Observers</th>
 * <th>Owners</th>
 * <th>Effective</th>
 * </tr>
 * <tr>
 * <td>Direct/Shallow (values and object references)</td>
 * <td>YES</td>
 * <td>NO</td>
 * <td>NO</td>
 * </tr>
 * <tr>
 * <td>Indirect/Deep (referenced objects)</td>
 * <td>NO</td>
 * <td>NO</td>
 * <td>NO</td>
 * </tr>
 * </table>
 * <h4>Requirements</h4>
 * <ul>
 * <li>class:
 * <ul>
 * <li>the annotated class has effectively no member with mutation semantics (any setter or other
 * method with side effects throws {@link UnsupportedOperationException})</li>
 * <li>the annotated class is final (<b>strong unmodifiability</b>), or not (<b>weak
 * unmodifiability</b>); in the latter case, derived classes MUST honour the unmodifiability
 * themselves</li>
 * </ul>
 * </li>
 * <li>interface: not applicable (view unmodifiability is an implementation detail)</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 * @see Unmodifiable
 * @see Immutable
 */
@Documented
@Retention(CLASS)
@Target({ TYPE, TYPE_USE })
public @interface UnmodifiableView {
}
