/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (GraphImmutable.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

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
 * Indicates that the annotated type or type use is <i>deeply immutable</i>, that is its
 * immutability includes the graph of the objects it references.
 * <p>
 * <b>Deep immutability</b> is about the <i>stability of the externally-observable object state,
 * referenced objects inclusive</i>; it is stricter than {@linkplain Immutable shallow immutability}
 * and {@linkplain Unmodifiable view unmodifiability}.
 * </p>
 * <p>
 * <b>Externally-observable state</b> comprises values and object references directly associated to
 * the class, and the objects indirectly associated to the class through object references. Mutable
 * private fields which don't influence the externally-observable state are irrelevant (e.g.,
 * defensive copy of arrays and other mutable objects makes them effectively immutable;
 * <a href="https://en.wikipedia.org/wiki/Memoization">memoization</a> doesn't affect the
 * externally-observable state).
 * </p>
 * <p>
 * Because of the intrinsic flexibility of interfaces, <i>the semantics of annotated interfaces are
 * much weaker than annotated classes</i>: whilst the latter extend their immutability to derived
 * classes (any additional state MUST be immutable itself), the former are limited to their own
 * definition (derived interfaces and implementing classes may declare additional state as mutable).
 * Consequently, <span class="important">an object referenced as an immutable interface isn't itself
 * immutable, unless the class exposing that reference is deeply immutable</span>: in such case, the
 * class MUST guarantee that the underlying referenced class is immutable itself (i.e., the object
 * MUST be <i>effectively immutable</i> — e.g., it may declare a {@link java.util.List List} whose
 * actual class is a fixed-size list from {@link java.util.List#of(Object[]) List.of(...)}). All
 * considered, immutable interfaces are mostly relevant to implementers rather than users, since
 * immutable classes are required to implement only (effectively) immutable interfaces.
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
 * <th>Direct (values and object references)</th>
 * <td>YES</td>
 * <td>YES</td>
 * <td>YES</td>
 * <td>NO</td>
 * </tr>
 * <tr>
 * <th>Indirect (referenced objects)</th>
 * <td>YES</td>
 * <td>YES</td>
 * <td>YES</td>
 * <td>NO</td>
 * </tr>
 * </table>
 * <h2>Requirements</h2>
 * <ul>
 * <li>class:
 * <ul>
 * <li>the annotated class has only immutable state (inherited state is also effectively immutable),
 * whose types are themselves deeply immutable</li>
 * <li>the annotated class may be final (<b>strong immutability</b>), or not (<b>weak
 * immutability</b>); in the latter case, derived classes MUST honour the deep immutability
 * themselves</li>
 * </ul>
 * </li>
 * <li>interface:
 * <ul>
 * <li>the annotated interface declares only immutable state, whose types are themselves deeply
 * immutable</li>
 * <li>the parents of the annotated interface are themselves deeply immutable</li>
 * <li>the children of the annotated interface MUST honour the immutability of the inherited
 * interface, but can add mutable state of their own</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author Stefano Chizzolini
 * @see Immutable
 * @see Unmodifiable
 */
@Documented
@Retention(CLASS)
@Target({ TYPE, TYPE_USE })
public @interface GraphImmutable {
}
