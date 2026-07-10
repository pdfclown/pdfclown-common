/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Immutable.java) is part of pdfclown-common-util module in pdfClown Common project
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
 * Indicates that the annotated type or type use is immutable.
 * <p>
 * <b>Immutability</b> is about the <i>stability of the externally-observable object state,
 * referenced objects inclusive</i> (deep immutability); it is stricter than
 * {@linkplain Unmodifiable unmodifiability} and {@linkplain ReadOnly readonly-ness}.
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
 * Due to the intrinsic flexibility of <b>interfaces</b>, <i>the semantics of annotated interfaces
 * are much weaker than annotated classes</i>: whilst the latter extend their immutability to
 * derived classes (any additional state MUST be immutable itself), the former are limited to their
 * own definition (derived interfaces and implementing classes may declare additional members with
 * mutation semantics). Consequently, <span class="important">an object referenced as an immutable
 * interface isn't immutable itself, unless the class exposing that reference is immutable or marks
 * its use as immutable</span>: in such case, the class MUST guarantee that the underlying
 * referenced class is immutable itself (that is, the object MUST be <i>effectively immutable</i> —
 * for example, it may declare a {@link java.util.List List} whose actual class is an immutable list
 * from {@link java.util.List#of(Object[]) List.of(...)}). All considered, immutable interfaces are
 * mostly relevant to implementers rather than users, since immutable classes are required to
 * implement only (effectively) immutable interfaces.
 * </p>
 * <p>
 * NOTE: <span class="important">This annotation is NOT inheritable, it MUST explicitly mark each
 * and every applicable type.</span> The rationale, beside the fact that immutability does not
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
 * <td>YES</td>
 * <td>YES</td>
 * <td>YES</td>
 * <td>NO</td>
 * </tr>
 * </table>
 * <h4>Requirements</h4>
 * <ul>
 * <li>class:
 * <ul>
 * <li>the annotated class MUST have only immutable state (inherited state is also effectively
 * immutable), whose referenced objects are immutable themselves</li>
 * <li>the annotated class may be final (<b>strong immutability</b>), or not (<b>weak
 * immutability</b>); in the latter case, derived classes MUST honor the immutability
 * themselves</li>
 * </ul>
 * </li>
 * <li>interface:
 * <ul>
 * <li>the annotated interface MUST declare no member with mutation semantics (that is, any mutator
 * or other method with side effects), and reference types returned by accessors MUST be immutable
 * themselves</li>
 * <li>the parents of the annotated interface MUST be immutable themselves</li>
 * <li>the children of the annotated interface MUST honor the immutability of the inherited
 * interface, but can add members with mutation semantics</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author Stefano Chizzolini
 * @see Unmodifiable
 * @see ReadOnly
 * @see <a href="https://docs.oracle.com/javase/tutorial/essential/concurrency/imstrat.html">A
 *      Strategy for Defining Immutable Objects</a>
 */
@Documented
@Retention(CLASS)
@Target({ TYPE, TYPE_USE })
public @interface Immutable {
}
