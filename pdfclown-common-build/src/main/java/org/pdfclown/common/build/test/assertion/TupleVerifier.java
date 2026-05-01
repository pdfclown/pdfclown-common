/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TupleVerifier.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import java.util.List;
import java.util.function.Function;
import org.approvaltests.core.Options;
import org.jspecify.annotations.Nullable;
import org.lambda.functions.Function1;
import org.lambda.functions.Function2;
import org.lambda.functions.Function3;
import org.lambda.functions.Function4;
import org.lambda.functions.Function5;
import org.lambda.functions.Function6;
import org.lambda.functions.Function7;
import org.lambda.functions.Function8;
import org.lambda.functions.Function9;
import org.pdfclown.common.build.util.Tuple;
import org.pdfclown.common.build.util.Tuple2;
import org.pdfclown.common.build.util.Tuple3;
import org.pdfclown.common.build.util.Tuple4;
import org.pdfclown.common.build.util.Tuple5;
import org.pdfclown.common.build.util.Tuple6;
import org.pdfclown.common.build.util.Tuple7;
import org.pdfclown.common.build.util.Tuple8;
import org.pdfclown.common.build.util.Tuple9;
import org.pdfclown.common.util.annot.Immutable;

/**
 * Verifier for approval testing on input tuples (that is, plain argument lists).
 *
 * @author Stefano Chizzolini
 */
@Immutable
public class TupleVerifier extends CallVerifier {
  /**
  */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, O extends @Nullable Object> //
      void verify(Function2<I1, I2, O> call, List<String> labels, List<Tuple2<I1, I2>> inputs) {
    doVerify($ -> call.call($.getE1(), $.getE2()), labels, inputs);
  }

  /**
  */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      O extends @Nullable Object> //
      void verify(Function3<I1, I2, I3, O> call, List<String> labels,
          List<Tuple3<I1, I2, I3>> inputs) {
    doVerify($ -> call.call($.getE1(), $.getE2(), $.getE3()), labels, inputs);
  }

  /**
  */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, O extends @Nullable Object> //
      void verify(Function4<I1, I2, I3, I4, O> call, List<String> labels,
          List<Tuple4<I1, I2, I3, I4>> inputs) {
    doVerify($ -> call.call($.getE1(), $.getE2(), $.getE3(), $.getE4()), labels, inputs);
  }

  /**
  */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, I5 extends @Nullable Object, O extends @Nullable Object> //
      void verify(Function5<I1, I2, I3, I4, I5, O> call, List<String> labels,
          List<Tuple5<I1, I2, I3, I4, I5>> inputs) {
    doVerify($ -> call.call($.getE1(), $.getE2(), $.getE3(), $.getE4(), $.getE5()), labels, inputs);
  }

  /**
  */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, I5 extends @Nullable Object, I6 extends @Nullable Object,
      O extends @Nullable Object> //
      void verify(Function6<I1, I2, I3, I4, I5, I6, O> call, List<String> labels,
          List<Tuple6<I1, I2, I3, I4, I5, I6>> inputs) {
    doVerify($ -> call.call($.getE1(), $.getE2(), $.getE3(), $.getE4(), $.getE5(), $.getE6()),
        labels, inputs);
  }

  /**
  */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, I5 extends @Nullable Object, I6 extends @Nullable Object,
      I7 extends @Nullable Object, O extends @Nullable Object> //
      void verify(Function7<I1, I2, I3, I4, I5, I6, I7, O> call, List<String> labels,
          List<Tuple7<I1, I2, I3, I4, I5, I6, I7>> inputs) {
    doVerify($ -> call.call($.getE1(), $.getE2(), $.getE3(), $.getE4(), $.getE5(), $.getE6(),
        $.getE7()), labels, inputs);
  }

  /**
  */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, I5 extends @Nullable Object, I6 extends @Nullable Object,
      I7 extends @Nullable Object, I8 extends @Nullable Object, O extends @Nullable Object> //
      void verify(Function8<I1, I2, I3, I4, I5, I6, I7, I8, O> call, List<String> labels,
          List<Tuple8<I1, I2, I3, I4, I5, I6, I7, I8>> inputs) {
    doVerify($ -> call.call($.getE1(), $.getE2(), $.getE3(), $.getE4(), $.getE5(), $.getE6(),
        $.getE7(), $.getE8()), labels, inputs);
  }

  /**
  */
  public <I1 extends @Nullable Object, I2 extends @Nullable Object, I3 extends @Nullable Object,
      I4 extends @Nullable Object, I5 extends @Nullable Object, I6 extends @Nullable Object,
      I7 extends @Nullable Object, I8 extends @Nullable Object, I9 extends @Nullable Object,
      O extends @Nullable Object> //
      void verify(Function9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> call, List<String> labels,
          List<Tuple9<I1, I2, I3, I4, I5, I6, I7, I8, I9>> inputs) {
    doVerify($ -> call.call($.getE1(), $.getE2(), $.getE3(), $.getE4(), $.getE5(), $.getE6(),
        $.getE7(), $.getE8(), $.getE9()), labels, inputs);
  }

  @Override
  public TupleVerifier withExceptionFormatter(Function<Throwable, String> value) {
    return (TupleVerifier) super.withExceptionFormatter(value);
  }

  @Override
  public TupleVerifier withFormatter(Function<@Nullable Object, String> value) {
    return (TupleVerifier) super.withFormatter(value);
  }

  @Override
  public TupleVerifier withInputFormatter(Function<@Nullable Object, String> value) {
    return (TupleVerifier) super.withInputFormatter(value);
  }

  @Override
  public TupleVerifier withOptions(Options value) {
    return (TupleVerifier) super.withOptions(value);
  }

  @Override
  public TupleVerifier withOutputFormatter(Function<@Nullable Object, String> value) {
    return (TupleVerifier) super.withOutputFormatter(value);
  }

  private <T extends Tuple<?>, O extends @Nullable Object> //
      void doVerify(Function1<T, O> call, List<String> labels, List<T> inputs) {
    var response = new StringBuilder();
    for (var input : inputs) {
      response.append(getResponse(() -> call.call(input), labels, input.toArray()));
    }

    verifyResponse(response.toString());
  }
}
