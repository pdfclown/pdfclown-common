/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (WeakNullness.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.lang;

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
 * responsible to decide over its nullness (ie, whether to specialize (in case of output) or
 * generalize (in case of input)).
 * <ul>
 * <li>method inputs (contravariant): the root class defines method parameters as {@link NonNull};
 * its subclasses are responsible to override the method and either
 * {@linkplain Objects#requireNonNull(Object) check} the required arguments or mark those parameters
 * as {@link Nullable}. Corresponding constructor parameters behave accordingly.</li>
 * <li>method outputs (covariant): the root class defines a method return type as {@link Nullable};
 * its subclasses are responsible, whenever appropriate, to override the method, mark its output as
 * {@link NonNull} and {@linkplain Objects#requireNonNull(Object) check} its value.</li>
 * </ul>
 * <p>
 * For example:
 * </p>
 * <pre>
 * {@code @}NullMarked
 * abstract class MyRootType {
 *   private {@code @}Nullable Object myProperty;
 *
 *   protected MyRootType(Object myProperty) {
 *     setMyProperty(myProperty);
 *   }
 *
 *   public {@code @}Nullable {@code @}WeakNullness Object getMyProperty() {
 *     return myProperty;
 *   }
 *
 *   public void setMyProperty({@code @}WeakNullness Object value) {
 *     myProperty = value;
 *   }
 * }
 *
 * {@code @}NullMarked
 * class MyOptionalType extends MyRootType {
 *   {@code @}SuppressWarnings("null")
 *   public MyOptionalType({@code @}Nullable Object myProperty) {
 *     super(myProperty);
 *   }
 *
 *   {@code @}Override
 *   public {@code @}Nullable Object getMyProperty() {
 *     return super.getMyProperty();
 *   }
 *
 *   {@code @}Override
 *   {@code @}SuppressWarnings("null")
 *   public void setMyProperty({@code @}Nullable Object value) {
 *     return super.setMyProperty(value);
 *   }
 * }
 *
 * {@code @}NullMarked
 * class MyRequiredType extends MyRootType {
 *   public MyRequiredType(Object myProperty) {
 *     super(myProperty);
 *   }
 *
 *   {@code @}Override
 *   public {@code @}NonNull Object getMyProperty() {
 *     return requireNonNull(super.getMyProperty());
 *   }
 *
 *   {@code @}Override
 *   public void setMyProperty(Object value) {
 *     return super.setMyProperty(requireNonNull(value));
 *   }
 * }
 * </pre>
 *
 * @author Stefano Chizzolini
 */
@Documented
@Retention(CLASS)
@Target(TYPE_USE)
public @interface WeakNullness {
}
