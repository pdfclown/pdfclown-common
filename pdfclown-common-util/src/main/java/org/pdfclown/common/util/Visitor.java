/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Visitor.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import org.jspecify.annotations.Nullable;

/**
 * <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor design pattern</a>.
 * <p>
 * <span class="important">IMPORTANT: This visitor's methods should be invoked <i>only</i> from the
 * {@link Visitable#accept(Visitor, Object)} method implemented by the types belonging to the
 * element hierarchy associated to this visitor's implementation. <i>Invoking this visitor's methods
 * directly may cause unpredictable results</i>, as it would bypass the double-dispatch
 * mechanism.</span>
 * </p>
 *
 * @param <R>
 *          Return type of this visitor's methods. Use {@link Void} for visitors that do not need to
 *          return results.
 * @param <D>
 *          Type of the ancillary {@code data} parameter of this visitor's methods. Use {@code Void}
 *          for visitors that do not need a {@code data} parameter.
 * @author Stefano Chizzolini
 * @see Visitable
 * @implSpec Implementers are expected to define one method per element type, as
 *           follows:<pre class="lang-java"><code>
 * R visit(%ElementType% obj, D data);</code></pre>
 *           <p>
 *           Each element type, in turn, is expected to implement {@link Visitable}.
 *           </p>
 *           <p>
 *           Visits are expected to cross an inheritance line within the element type hierarchy
 *           until a meaningful operation can be performed (this arrangement is the most robust
 *           possible, as any missing operation would eventually end up visiting the method
 *           associated to the root type of the hierarchy — perfect point to trap malfunctions),
 *           e.g.:
 *           </p>
 *           <pre class="lang-java"><code>
 * public class MyVisitor&lt;R, D&gt; implements Visitor&lt;R, D&gt; {
 *   . . .
 *
 *   &#64;Override
 *   public R visit(Bicycle obj, D data) {
 *     return visit((HumanPoweredVehicle) obj, data);
 *   }
 *
 *   &#64;Override
 *   public R visit(Car obj, D data) {
 *     return visit((MotorVehicle) obj, data);
 *   }
 *
 *   &#64;Override
 *   public R visit(HumanPoweredVehicle obj, D data) {
 *     return visit((Vehicle) obj, data);
 *   }
 *
 *   &#64;Override
 *   public R visit(Motorcycle obj, D data) {
 *     return visit((MotorVehicle) obj, data);
 *   }
 *
 *   &#64;Override
 *   public R visit(MotorVehicle obj, D data) {
 *     return visit((Vehicle) obj, data);
 *   }
 *
 *   &#64;Override
 *   public R visit(Vehicle obj, D data) {
 *     &#47;*
 *      * NOTE: `Vehicle` is the root type of this hierarchy.
 *      *
 *      * As terminal visit for each and every inheritance line, this method can work like a
 *      * trap for any element type in the hierarchy whose visit implementation is missing. The
 *      * following statement throws a `org.pdfclown.util.NotImplementedException` reporting
 *      * what implementation is missing; its error stack would be like this:
 *      *
 *      * Exception in thread "main" org.pdfclown.util.NotImplementedException:
 *      *     MyVisitor.visit(mydomain.vehicle.Car, D)
 *      *&#47;
 *     throw org.pdfclown.common.util.Exceptions.TODO(
 *       "{}.visit({}, D)",
 *       org.pdfclown.util.Objects.sqn(this),
 *       org.pdfclown.util.Objects.fqn(obj));
 *   }
 *
 *   . . .
 * }</code></pre>
 */
public interface Visitor<R extends @Nullable Object, D extends @Nullable Object> {
}
