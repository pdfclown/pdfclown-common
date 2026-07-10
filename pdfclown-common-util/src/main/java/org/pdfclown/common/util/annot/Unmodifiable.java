/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Unmodifiable.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.annot;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated type or type use is unmodifiable.
 * <p>
 * <b>Unmodifiability</b> is about the <i>stability of the externally-observable object state,
 * referenced objects exclusive</i> (shallow immutability); it is stricter than {@linkplain ReadOnly
 * readonly-ness} and looser than {@linkplain Immutable immutability}.
 * </p>
 * <p>
 * <b>Externally-observable state</b> comprises values and object references directly associated to
 * the class, and the objects indirectly associated to the class through object references. Mutable
 * private fields which don't influence the externally-observable state are irrelevant (for example,
 * defensive copy of arrays and other mutable objects makes them effectively immutable;
 * <a href="https://en.wikipedia.org/wiki/Memoization">memoization</a> doesn't affect the
 * externally-observable state).
 * </p>
 * <p>
 * NOTE: <span class="important">This annotation is NOT inheritable, it MUST explicitly mark each
 * and every applicable type.</span> The rationale, beside the fact that unmodifiability does not
 * extend to derived interfaces, is that clarity should win over succinctness.
 * </p>
 * <table>
 * <caption>Stability on type definition</caption>
 * <tr>
 * <th rowspan="3">State</th>
 * <th colspan="4">Stability</th>
 * </tr>
 * <tr>
 * <th colspan="2">Class</th>
 * <th colspan="2">Interface</th>
 * </tr>
 * <tr>
 * <th>Self</th>
 * <th>Derived types</th>
 * <th>Self</th>
 * <th>Derived types</th>
 * </tr>
 * <tr>
 * <td>Direct/Shallow (values and object references)</td>
 * <td>YES</td>
 * <td>YES</td>
 * <td>YES</td>
 * <td>NO</td>
 * </tr>
 * <tr>
 * <td>Indirect/Deep (referenced objects)</td>
 * <td>NO</td>
 * <td>NO</td>
 * <td>NO</td>
 * <td>NO</td>
 * </tr>
 * </table>
 * <h4>Requirements</h4>
 * <ul>
 * <li>class:
 * <ul>
 * <li>the annotated class MUST have only unmodifiable state (inherited state is also effectively
 * unmodifiable), whose referenced objects may be, on the contrary, mutable</li>
 * <li>the annotated class may be final (<b>strong unmodifiability</b>), or not (<b>weak
 * unmodifiability</b>); in the latter case, derived classes MUST honor the unmodifiability
 * themselves</li>
 * </ul>
 * </li>
 * <li>interface: not applicable (unmodifiability is an implementation detail, use {@link ReadOnly}
 * instead)</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 * @see Immutable
 * @see ReadOnly
 */
@Documented
@Retention(CLASS)
@Target({ TYPE, TYPE_USE })
public @interface Unmodifiable {
}
