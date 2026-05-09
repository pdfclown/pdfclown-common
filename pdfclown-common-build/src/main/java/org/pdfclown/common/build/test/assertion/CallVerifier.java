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

import static org.approvaltests.combinations.CombinationsHelper.filterEmpty;
import static org.pdfclown.common.build.test.assertion.Verifiers.FORMATTER__BASIC;
import static org.pdfclown.common.build.test.assertion.Verifiers.FORMATTER__EXCEPTION__BASIC;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.COMMA;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Chars.SQUARE_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.SQUARE_BRACKET_OPEN;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.approvaltests.Approvals;
import org.approvaltests.combinations.SkipCombination;
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

  /**
   * Gets a single call response.
   *
   * @param outputSupplier
   *          Call wrapper.
   * @param labels
   *          Argument labels.
   * @param inputs
   *          Call arguments.
   * @return Call response in the form {@code "%INPUTS% => %OUTPUT%"}.
   */
  protected String getResponse(Supplier<@Nullable Object> outputSupplier, List<String> labels,
      @Nullable Object[] inputs) {
    String output;
    try {
      output = getOutputFormatter().apply(outputSupplier.get());
    } catch (SkipCombination ex) {
      return EMPTY;
    } catch (Throwable ex) {
      output = getExceptionFormatter().apply(ex);
    }
    return "%s => %s\n".formatted(formatInputs(labels, inputs), output);
  }

  /**
   * Verifies the call responses as a whole.
   *
   * @param response
   *          Multi-line aggregation of {@linkplain #getResponse(Supplier, List, Object[]) call
   *          responses}.
   */
  protected void verifyResponse(String response) {
    Approvals.verify(response, prepareOptions(null));
  }

  // SPDX-SnippetBegin
  // SPDX-SnippetCopyrightText: ?-2026 Llewellyn Falco
  // SPDX-License-Identifier: Apache-2.0
  //
  // Source: https://github.com/approvals/ApprovalTests.Java/blob/8452841b8bc430fe069f93bb413ccc913213087a/approvaltests/src/main/java/org/approvaltests/combinations/CombinationsHelper.java#L109
  // SourceName: org.approvaltests.combinations.CombinationsHelper.formatInputs
  private String formatInputs(List<String> labels, @Nullable Object... objects) {
    List<Object> values = filterEmpty(objects);
    var b = new StringBuilder();
    b.append(SQUARE_BRACKET_OPEN);
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        b.append(COMMA).append(SPACE);
      }
      String label = i < labels.size() ? labels.get(i) : null;
      if (label != null) {
        b.append(label).append(COLON).append(SPACE);
      }
      b.append(getInputFormatter().apply(values.get(i)));
    }
    return b.append(SQUARE_BRACKET_CLOSE).toString();
  }
  // SPDX-SnippetEnd
}
