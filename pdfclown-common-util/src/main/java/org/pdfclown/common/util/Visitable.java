/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Visitable.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Element type in a {@linkplain Visitor visitor} pattern.
 *
 * @param <V>
 *          Visitor type.
 * @author Stefano Chizzolini
 * @implSpec In order to leverage the double-dispatch mechanism, implementers are expected to invoke
 *           the methods of the visitor associated to a type hierarchy from the
 *           {@link #accept(Visitor, Object)} method implemented by each type belonging to that
 *           hierarchy, e.g.:<pre class="lang-java"><code>
 * public class MyObject implements Visitable&lt;MyVisitor&lt;?,?&gt;&gt; {
 *   . . .
 *   &#64;Override
 *   public Object accept(MyVisitor visitor, Object data) {
 *     return visitor.visit(this, data);
 *   }
 *   . . .
 * }</code></pre>
 * @author Stefano Chizzolini
 * @see Visitor
 */
public interface Visitable<V extends Visitor<?, ?>> {
  /**
   * Accepts a visit.
   *
   * @param visitor
   *          Visiting object.
   * @param data
   *          Supplemental data (depends on {@code visitor} semantics).
   * @return Result (depends on {@code visitor} semantics).
   */
  <@Nullable R, @Nullable D> R accept(@NonNull V visitor, D data);
}
