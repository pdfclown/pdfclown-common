/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (CallVerifier.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static org.pdfclown.common.build.test.assertion.Verifiers.FORMATTER__BASIC;
import static org.pdfclown.common.build.test.assertion.Verifiers.FORMATTER__EXCEPTION__BASIC;

import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Immutable;

/**
 * Verifier for approval testing on method calls.
 *
 * @author Stefano Chizzolini
 */
@Immutable
public abstract class CallVerifier extends Verifier {
  private Function<Throwable, String> exceptionFormatter = FORMATTER__EXCEPTION__BASIC;
  private Function<@Nullable Object, String> inputFormatter = FORMATTER__BASIC;
  private Function<@Nullable Object, String> outputFormatter = inputFormatter;

  /**
   * Exception formatter.
   */
  public Function<Throwable, String> getExceptionFormatter() {
    return exceptionFormatter;
  }

  /**
   * Arguments formatter.
   */
  public Function<@Nullable Object, String> getInputFormatter() {
    return inputFormatter;
  }

  /**
   * Result formatter.
   */
  public Function<@Nullable Object, String> getOutputFormatter() {
    return outputFormatter;
  }

  /**
   * Sets {@link #getExceptionFormatter() exceptionFormatter}.
   *
   * @return Clone of this verifier.
   */
  public CallVerifier withExceptionFormatter(Function<Throwable, String> value) {
    var ret = (CallVerifier) clone();
    ret.exceptionFormatter = value;
    return ret;
  }

  /**
   * Sets both {@link #getInputFormatter() inputFormatter} and {@link #getOutputFormatter()
   * outputFormatter}.
   *
   * @return Clone of this verifier.
   */
  public CallVerifier withFormatter(Function<@Nullable Object, String> value) {
    var ret = (CallVerifier) clone();
    ret.inputFormatter = value;
    ret.outputFormatter = value;
    return ret;
  }

  /**
   * Sets {@link #getInputFormatter() inputFormatter}.
   *
   * @return Clone of this verifier.
   */
  public CallVerifier withInputFormatter(Function<@Nullable Object, String> value) {
    var ret = (CallVerifier) clone();
    ret.inputFormatter = value;
    return ret;
  }

  /**
   * Sets {@link #getOutputFormatter() outputFormatter}.
   *
   * @return Clone of this verifier.
   */
  public CallVerifier withOutputFormatter(Function<@Nullable Object, String> value) {
    var ret = (CallVerifier) clone();
    ret.outputFormatter = value;
    return ret;
  }
}
