/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Derived.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.annot;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Indicates that the annotated field represents secondary state, derived from primary state.
 * <p>
 * Contrary to {@link LazyNonNull @LazyNonNull}, <i>a field marked with this annotation can (at
 * least potentially) be later reset to {@code null}</i>, and re-initialized on next accessor call
 * (this behavior is typical of derived fields which store computationally-intensive information
 * based on primary state: if the latter changes, the former is invalidated so it can be freshly
 * re-initialized on demand). Consequently, this annotation is typically accompanied by
 * {@link Nullable @Nullable}; otherwise, it defaults to {@link NonNull @NonNull}, with
 * {@link InitNonNull @InitNonNull} as close alternative.
 * </p>
 * <p>
 * <span class="important">IMPORTANT: Because of its dependence on primary state, <i>the annotated
 * field should NEVER be accessed directly until its accessor method is called</i>.</span>
 * </p>
 * <p>
 * For example [*]:
 * </p>
 * <pre>
 *{@code @}NullMarked
 *class MyClass {
 *  {@code @}Derived
 *  transient {@code @}Nullable Object object;
 *  Object primaryData = "Something";
 *
 *  MyClass() {
 *    . . .
 *  }
 *
 *  public Object getObject() {
 *    if (object == null) {
 *      object = primaryData.toString();
 *    }
 *    return object;
 *  }
 *
 *  public void setPrimaryData(Object value) {
 *    primaryData = value;
 *
 *    object = null;
 *  }
 *}</pre>
 * <p>
 * <span class="important">[*] IMPORTANT: To semantically denote its derivative nature, <i>it is
 * recommended to accompany this annotation with the {@code transient} keyword</i>, even if no
 * serialization usage is expected.</span>
 * </p>
 *
 * @author Stefano Chizzolini
 * @see InitNonNull
 * @see LazyNonNull
 * @see Nullable
 */
@Documented
@Retention(CLASS)
@Target(FIELD)
public @interface Derived {
}
