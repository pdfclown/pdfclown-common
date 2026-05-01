/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (CombinationVerifier.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: ?-2026 Llewellyn Falco

  SPDX-License-Identifier: Apache-2.0

  Source: https://github.com/approvals/ApprovalTests.Java/blob/8452841b8bc430fe069f93bb413ccc913213087a/approvaltests/src/main/java/org/approvaltests/combinations/CombinationApprovals.java
  Source: https://github.com/approvals/ApprovalTests.Java/blob/8452841b8bc430fe069f93bb413ccc913213087a/approvaltests/src/main/java/org/approvaltests/combinations/CombinationsHelper.java

  Changes:
  - complete refactoring as instantiable hierarchical class
  - type parameter names normalized
  - array parameters replaced with `java.util.List`
  - `options` parameter redefined as field
  - customizable formatters
 */
package org.pdfclown.common.build.test.assertion;

import static org.approvaltests.combinations.CombinationsHelper.EMPTY_ENTRY;
import static org.approvaltests.combinations.CombinationsHelper.filterEmpty;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.COMMA;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Chars.SQUARE_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.SQUARE_BRACKET_OPEN;

import java.util.List;
import java.util.function.Function;
import org.approvaltests.combinations.SkipCombination;
import org.approvaltests.core.Options;
import org.jspecify.annotations.Nullable;
import org.lambda.actions.Action9;
import org.lambda.functions.Function1;
import org.lambda.functions.Function2;
import org.lambda.functions.Function3;
import org.lambda.functions.Function4;
import org.lambda.functions.Function5;
import org.lambda.functions.Function6;
import org.lambda.functions.Function7;
import org.lambda.functions.Function8;
import org.lambda.functions.Function9;
import org.pdfclown.common.util.Strings;
import org.pdfclown.common.util.annot.Immutable;

/*-
  SourceName: org.approvaltests.combinations.CombinationApprovals
  SourceName: org.approvaltests.combinations.CombinationsHelper
 */
/**
 * Verifier for approval testing on input combinations (that is, Cartesian product of input
 * arguments).
 *
 * @author Stefano Chizzolini (adaptation to pdfclown-common-build and redesign)
 * @implNote This implementation provides more flexibility and a more modern API than the original
 *           {@link org.approvaltests.combinations.CombinationApprovals}.
 */
@Immutable
public class CombinationVerifier extends CallVerifier {
  static final List<Object> EMPTY = List.of(EMPTY_ENTRY);

  // SourceName: verifyAllCombinations
  /**
   */
  public <I1 extends @Nullable Object, O extends @Nullable Object> //
      void verify(Function1<I1, O> call, List<String> labels, List<I1> inputs1) {
    doVerify(($1, $2, $3, $4, $5, $6, $7, $8, $9) -> call.call($1), labels, inputs1, EMPTY, EMPTY,
        EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY);
  }

  // SourceName: verifyAllCombinations
  /**
  */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, O extends @Nullable Object> //
      void verify(Function2<I1, I2, O> call, List<String> labels, List<I1> inputs1,
          List<I2> inputs2) {
    doVerify(($1, $2, $3, $4, $5, $6, $7, $8, $9) -> call.call($1, $2), labels, inputs1, inputs2,
        EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY);
  }

  // SourceName: verifyAllCombinations
  /**
   */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      O extends @Nullable Object> //
      void verify(Function3<I1, I2, I3, O> call, List<String> labels, List<I1> inputs1,
          List<I2> inputs2, List<I3> inputs3) {
    doVerify(($1, $2, $3, $4, $5, $6, $7, $8, $9) -> call.call($1, $2, $3), labels, inputs1,
        inputs2, inputs3, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY);
  }

  // SourceName: verifyAllCombinations
  /**
   */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, O extends @Nullable Object> //
      void verify(Function4<I1, I2, I3, I4, O> call, List<String> labels, List<I1> inputs1,
          List<I2> inputs2, List<I3> inputs3, List<I4> inputs4) {
    doVerify(($1, $2, $3, $4, $5, $6, $7, $8, $9) -> call.call($1, $2, $3, $4), labels, inputs1,
        inputs2, inputs3, inputs4, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY);
  }

  // SourceName: verifyAllCombinations
  /**
   */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, I5 extends @Nullable Object, O extends @Nullable Object> //
      void verify(Function5<I1, I2, I3, I4, I5, O> call, List<String> labels, List<I1> inputs1,
          List<I2> inputs2, List<I3> inputs3, List<I4> inputs4, List<I5> inputs5) {
    doVerify(($1, $2, $3, $4, $5, $6, $7, $8, $9) -> call.call($1, $2, $3, $4, $5), labels, inputs1,
        inputs2, inputs3, inputs4, inputs5, EMPTY, EMPTY, EMPTY, EMPTY);
  }

  // SourceName: verifyAllCombinations
  /**
   */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, I5 extends @Nullable Object, I6 extends @Nullable Object,
      O extends @Nullable Object> //
      void verify(Function6<I1, I2, I3, I4, I5, I6, O> call, List<String> labels, List<I1> inputs1,
          List<I2> inputs2, List<I3> inputs3, List<I4> inputs4, List<I5> inputs5,
          List<I6> inputs6) {
    doVerify(($1, $2, $3, $4, $5, $6, $7, $8, $9) -> call.call($1, $2, $3, $4, $5, $6), labels,
        inputs1, inputs2, inputs3, inputs4, inputs5, inputs6, EMPTY, EMPTY, EMPTY);
  }

  // SourceName: verifyAllCombinations
  /**
   */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, I5 extends @Nullable Object, I6 extends @Nullable Object,
      I7 extends @Nullable Object, O extends @Nullable Object> //
      void verify(Function7<I1, I2, I3, I4, I5, I6, I7, O> call, List<String> labels,
          List<I1> inputs1, List<I2> inputs2, List<I3> inputs3, List<I4> inputs4, List<I5> inputs5,
          List<I6> inputs6, List<I7> inputs7) {
    doVerify(($1, $2, $3, $4, $5, $6, $7, $8, $9) -> call.call($1, $2, $3, $4, $5, $6, $7), labels,
        inputs1, inputs2, inputs3, inputs4, inputs5, inputs6, inputs7, EMPTY, EMPTY);
  }

  // SourceName: verifyAllCombinations
  /**
   */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, I5 extends @Nullable Object, I6 extends @Nullable Object,
      I7 extends @Nullable Object, I8 extends @Nullable Object, O extends @Nullable Object> //
      void verify(Function8<I1, I2, I3, I4, I5, I6, I7, I8, O> call, List<String> labels,
          List<I1> inputs1, List<I2> inputs2, List<I3> inputs3, List<I4> inputs4, List<I5> inputs5,
          List<I6> inputs6, List<I7> inputs7, List<I8> inputs8) {
    doVerify(
        ($1, $2, $3, $4, $5, $6, $7, $8, $9) -> call.call($1, $2, $3, $4, $5, $6, $7, $8), labels,
        inputs1, inputs2, inputs3, inputs4, inputs5, inputs6, inputs7, inputs8, EMPTY);
  }

  // SourceName: verifyAllCombinations
  /**
   */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, I5 extends @Nullable Object, I6 extends @Nullable Object,
      I7 extends @Nullable Object, I8 extends @Nullable Object, I9 extends @Nullable Object,
      O extends @Nullable Object> //
      void verify(Function9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> call, List<String> labels,
          List<I1> inputs1, List<I2> inputs2, List<I3> inputs3, List<I4> inputs4, List<I5> inputs5,
          List<I6> inputs6, List<I7> inputs7, List<I8> inputs8, List<I9> inputs9) {
    doVerify(call, labels, inputs1, inputs2, inputs3, inputs4, inputs5, inputs6, inputs7, inputs8,
        inputs9);
  }

  @Override
  public CombinationVerifier withExceptionFormatter(Function<Throwable, String> value) {
    return (CombinationVerifier) super.withExceptionFormatter(value);
  }

  @Override
  public CombinationVerifier withFormatter(Function<@Nullable Object, String> value) {
    return (CombinationVerifier) super.withFormatter(value);
  }

  @Override
  public CombinationVerifier withInputFormatter(Function<@Nullable Object, String> value) {
    return (CombinationVerifier) super.withInputFormatter(value);
  }

  @Override
  public CombinationVerifier withOptions(Options value) {
    return (CombinationVerifier) super.withOptions(value);
  }

  @Override
  public CombinationVerifier withOutputFormatter(Function<@Nullable Object, String> value) {
    return (CombinationVerifier) super.withOutputFormatter(value);
  }

  // SourceName: doForAllCombinations
  private <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, I5 extends @Nullable Object, I6 extends @Nullable Object,
      I7 extends @Nullable Object, I8 extends @Nullable Object, I9 extends @Nullable Object> //
      void doCombinations(List<I1> inputs1, List<I2> inputs2, List<I3> inputs3, List<I4> inputs4,
          List<I5> inputs5, List<I6> inputs6, List<I7> inputs7, List<I8> inputs8, List<I9> inputs9,
          Action9<I1, I2, I3, I4, I5, I6, I7, I8, I9> action) {
    for (var in1 : inputs1) {
      for (var in2 : inputs2) {
        for (var in3 : inputs3) {
          for (var in4 : inputs4) {
            for (var in5 : inputs5) {
              for (var in6 : inputs6) {
                for (var in7 : inputs7) {
                  for (var in8 : inputs8) {
                    for (var in9 : inputs9) {
                      action.call(in1, in2, in3, in4, in5, in6, in7, in8, in9);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // SourceName: verifyAllCombinations
  private <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, I5 extends @Nullable Object, I6 extends @Nullable Object,
      I7 extends @Nullable Object, I8 extends @Nullable Object, I9 extends @Nullable Object,
      O extends @Nullable Object> //
      void doVerify(Function9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> call, List<String> labels,
          List<I1> inputs1, List<I2> inputs2, List<I3> inputs3, List<I4> inputs4, List<I5> inputs5,
          List<I6> inputs6, List<I7> inputs7, List<I8> inputs8, List<I9> inputs9) {
    var response = new StringBuilder();
    doCombinations(inputs1, inputs2, inputs3, inputs4, inputs5, inputs6, inputs7, inputs8, inputs9,
        (input1, input2, input3, input4, input5, input6, input7, input8, input9) -> response.append(
            getResponse(call, labels, input1, input2, input3, input4, input5, input6,
                input7, input8, input9)));

    verifyResponse(response.toString());
  }

  private String formatInputs(List<String> labels, Object... objects) {
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

  // SourceName: getCombinationText
  private <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, I5 extends @Nullable Object, I6 extends @Nullable Object,
      I7 extends @Nullable Object, I8 extends @Nullable Object, I9 extends @Nullable Object,
      O extends @Nullable Object> //
      String getResponse(Function9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> call, List<String> labels,
          I1 input1, I2 input2, I3 input3, I4 input4, I5 input5, I6 input6, I7 input7, I8 input8,
          I9 input9) {
    String result;
    try {
      result = getOutputFormatter().apply(call.call(input1, input2, input3, input4, input5, input6,
          input7, input8, input9));
    } catch (SkipCombination ex) {
      return Strings.EMPTY;
    } catch (Throwable ex) {
      result = getExceptionFormatter().apply(ex);
    }
    return "%s => %s\n".formatted(formatInputs(labels, input1, input2, input3, input4, input5,
        input6, input7, input8, input9), result);
  }
}
