/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Snapshot.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.annot;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated type use is isolated from the original state.
 * <p>
 * <b>Snapshots</b> are about the <i>stability of the original object state against write access</i>
 * (that is, <span class="important">the snapshot object may be mutable per-se, but its mutation
 * won't affect the original object as the former is a detached copy of the latter</span>); their
 * effect on the original object state is equivalent to {@linkplain ReadOnly readonly-ness};
 * however, read-only objects are alive (they can be observed mutating) whilst snapshot objects are
 * dead (they represent the original state only at the moment of their emission).
 * </p>
 *
 * @apiNote Useful to mark defensively copied objects, either returned from or received by methods,
 *          to indicate that their mutation won't cause side effects to the original state.
 * @author Stefano Chizzolini
 * @see Immutable
 * @see Unmodifiable
 * @see ReadOnly
 */
@Documented
@Retention(CLASS)
@Target({ TYPE_USE })
public @interface Snapshot {
}
