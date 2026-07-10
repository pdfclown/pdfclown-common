/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ReadOnly.java) is part of pdfclown-common-util module in pdfClown Common project
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
 * Indicates that the observers of the annotated type or type use have only read access to the
 * object.
 * <p>
 * <b>Readonly-ness</b> is about the <i>stability of the externally-observable object state,
 * referenced objects exclusive, against observers' write access</i> (that is,
 * <span class="important">the state of the object may be mutable per-se, but the observers cannot
 * mutate it</span>); it is looser than {@linkplain Unmodifiable unmodifiability} and
 * {@linkplain Immutable immutability}. Unmodifiable views (see also the dedicated section in
 * {@linkplain java.util.Collection collections}) are comprised in this definition, as a
 * specialization aware of the underlying structure of the object.
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
 * are much weaker than annotated classes</i>: whilst the latter extend their readonly-ness to
 * derived classes (any additional member MUST be read-only itself), the former are limited to their
 * own definition (derived interfaces and implementing classes may declare additional members as
 * mutable). Consequently, <span class="important">an object referenced as a read-only interface
 * isn't read-only itself, unless the type exposing that reference is {@linkplain Immutable
 * immutable} or marks its use as read-only</span>. All considered, read-only interfaces are mostly
 * relevant to implementers rather than users, since read-only classes are required to implement
 * only (effectively) read-only interfaces.
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
 * <li>the annotated class may be mutable per-se, but MUST effectively have no member with mutation
 * semantics (that is, any mutator or other method with side effects MUST either throw
 * {@link UnsupportedOperationException} or behave to prevent such mutations), whose referenced
 * objects may be, on the contrary, mutable</li>
 * <li>the annotated class may be final (<b>strong read-only</b>), or not (<b>weak read-only</b>);
 * in the latter case, derived classes MUST honor the readonly-ness themselves</li>
 * </ul>
 * </li>
 * <li>interface:
 * <ul>
 * <li>the annotated interface MUST declare no member with mutation semantics (that is, any mutator
 * or other method with side effects), whilst reference types returned by accessors may be
 * mutable</li>
 * <li>the parents of the annotated interface MUST be read-only themselves</li>
 * <li>the children of the annotated interface MUST honor the readonly-ness of the inherited
 * interface, but may add members with mutation semantics</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author Stefano Chizzolini
 * @see Unmodifiable
 * @see Immutable
 */
@Documented
@Retention(CLASS)
@Target({ TYPE, TYPE_USE })
public @interface ReadOnly {
}
