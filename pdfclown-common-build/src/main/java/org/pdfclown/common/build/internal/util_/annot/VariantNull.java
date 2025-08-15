/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (VariantNull.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_.annot;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Indicates that the subclass implementations of the method where the annotated type is used are
 * responsible to decide over its nullness (i.e., whether to specialize (in case of output) or
 * generalize (in case of input)).
 * <p>
 * In detail:
 * </p>
 * <ul>
 * <li><b>method inputs</b> (contravariant): the root class defines method parameters as
 * {@link NonNull}; its subclasses are responsible to override the method and either
 * {@linkplain Objects#requireNonNull(Object) check} the required arguments or mark those parameters
 * as {@link Nullable}. Corresponding constructor parameters behave accordingly.</li>
 * <li><b>method outputs</b> (covariant): the root class defines a method's return type as
 * {@link Nullable}; its subclasses are responsible, whenever appropriate, to override the method,
 * mark its output as {@link NonNull} and {@linkplain Objects#requireNonNull(Object) check} its
 * value.</li>
 * </ul>
 * <p>
 * For example:
 * </p>
 * <pre class="lang-java"><code>
 * &#64;NullMarked
 * abstract class MyRootType {
 *   private &#64;Nullable Object myProperty;
 *
 *   protected MyRootType(&#64;VariantNull Object myProperty) {
 *     setMyProperty(myProperty);
 *   }
 *
 *   public &#64;Nullable &#64;VariantNull Object getMyProperty() {
 *     return myProperty;
 *   }
 *
 *   public void setMyProperty(&#64;VariantNull Object value) {
 *     myProperty = value;
 *   }
 * }
 *
 * &#64;NullMarked
 * class MyOptionalType extends MyRootType {
 *   public MyOptionalType(&#64;Nullable Object myProperty) {
 *     super(myProperty);
 *   }
 *
 *   &#64;Override
 *   public &#64;Nullable Object getMyProperty() {
 *     return super.getMyProperty();
 *   }
 *
 *   &#64;Override
 *   public void setMyProperty(&#64;Nullable Object value) {
 *     return super.setMyProperty(value);
 *   }
 * }
 *
 * &#64;NullMarked
 * class MyRequiredType extends MyRootType {
 *   public MyRequiredType(Object myProperty) {
 *     super(myProperty);
 *   }
 *
 *   &#64;Override
 *   public &#64;NonNull Object getMyProperty() {
 *     return requireNonNull(super.getMyProperty());
 *   }
 *
 *   &#64;Override
 *   public void setMyProperty(Object value) {
 *     return super.setMyProperty(requireNonNull(value));
 *   }
 * }</code></pre>
 *
 * @author Stefano Chizzolini
 */
@Documented
@Retention(CLASS)
@Target(TYPE_USE)
public @interface VariantNull {
}
