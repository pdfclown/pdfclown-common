/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FunctionsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.function;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.pdfclown.common.build.test.assertion.Verifiers.COMBINATION;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
public class FunctionsTest extends BaseTest {
  private static final class IntSupplier implements Supplier<Integer> {
    @Override
    public Integer get() {
      return 21;
    }
  }

  private static final class NullSupplier implements Supplier<Object> {
    @Override
    public Object get() {
      return null;
    }
  }

  /**
   * Consumer covering both success and failure (in this case, {@link ClassCastException}).
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static final Consumer<Object> CONSUMER = $ -> ((Integer) $).doubleValue();
  public static final List<Integer> DEFAULT_VALUES = asList(null, 8);
  /**
   * Mapper covering both success (non-null (if argument is {@link Integer}) and null) and failure
   * (in this case, {@link ClassCastException}).
   */
  @SuppressWarnings("UnnecessaryUnboxing")
  public static final Function<Object, @Nullable Integer> MAPPER =
      $ -> $ != null ? ((Integer) $).intValue() /* Fails if type mismatch */ : null;
  /**
   * Objects covering both null and non-null (valid and invalid) argument values.
   */
  public static final List<Object> OBJS = asList(
      null,
      // Non-null, invalid
      "hello",
      // Non-null, valid
      42);;
  /**
   * Suppliers covering both non-null and null return values.
   *
   * @implNote These suppliers are deliberately NOT implemented as lambdas in order to make their
   *           type name stable (lambdas are identified by implementation-specific names which vary
   *           by JVM vendor and version) for reproducible test results.
   */
  public static final List<Supplier<?>> SUPPLIERS = asList(new NullSupplier(), new IntSupplier());

  @Test
  void let() {
    COMBINATION.verify(
        (obj) -> Functions.let(obj, CONSUMER),
        List.of("obj"),
        // obj
        OBJS);
  }

  /**
   * NOTE: {@link Functions#quietly(org.apache.commons.lang3.function.FailableRunnable)} can NEVER
   * fail per-se; if the operation fails on call, that's part of its regular execution.
   */
  @Test
  void quietly() {
    var ref = new MutableObject<>();
    Functions.quietly(() -> {
      ref.setValue("DONE");
      throw new IllegalStateException("FAILED");
    });

    assertThat(ref.get(), is("DONE"));
  }

  /**
   * NOTE: {@link Functions#quietly(org.apache.commons.lang3.function.FailableRunnable)} can NEVER
   * fail per-se; if the operation fails on call, that's part of its regular execution.
   */
  @Test
  void quietly_Consumer() {
    var exceptionRef = new MutableObject<Throwable>();
    Functions.quietly(() -> {
      throw new IllegalStateException("FAILED");
    }, exceptionRef::setValue);

    assertThat(exceptionRef.get(), instanceOf(IllegalStateException.class));
    assertThat(exceptionRef.get().getMessage(), is("FAILED"));
  }

  @Test
  void to() {
    COMBINATION.verify(
        (obj) -> Functions.to(obj, MAPPER),
        List.of("obj"),
        // obj
        OBJS);
  }

  @Test
  void toElse() {
    COMBINATION.verify(
        (obj, defaultValue) -> Functions.toElse(obj, MAPPER, defaultValue),
        List.of("obj", "defaultValue"),
        // obj
        OBJS,
        // defaultValue
        DEFAULT_VALUES);
  }

  @Test
  void toElseGet() {
    COMBINATION.verify(
        (obj, supplier) -> Functions.toElseGet(obj, MAPPER, supplier),
        List.of("obj", "supplier"),
        // obj
        OBJS,
        // supplier
        SUPPLIERS);
  }

  @Test
  void toElseGetOrNull() {
    COMBINATION.verify(
        (obj, supplier) -> Functions.toElseGetOrNull(obj, MAPPER, supplier),
        List.of("obj", "supplier"),
        // obj
        OBJS,
        // supplier
        SUPPLIERS);
  }

  @Test
  void toOrNull() {
    COMBINATION.verify(
        (obj) -> Functions.toOrNull(obj, MAPPER),
        List.of("obj"),
        // obj
        OBJS);
  }

  @Test
  void tryGet__fail() {
    var ret = Functions.tryGet(() -> {
      throw new IllegalStateException("FAILED");
    });

    assertThat(ret, is(nullValue()));
  }

  @Test
  void tryGet__ok() {
    var ret = Functions.tryGet(() -> "RESULT");

    assertThat(ret, is("RESULT"));
  }

  @Test
  void tryGetElse__fail() {
    var ret = Functions.tryGetElse(() -> {
      throw new IllegalStateException("FAILED");
    }, "ALT");

    assertThat(ret, is("ALT"));
  }

  @Test
  void tryGetElse__ok() {
    var ret = Functions.tryGetElse(() -> "RESULT", "ALT");

    assertThat(ret, is("RESULT"));
  }

  @Test
  void tryLet() {
    COMBINATION.verify(
        (obj) -> Functions.tryLet(obj, CONSUMER::accept),
        List.of("obj"),
        // obj
        OBJS);
  }

  @Test
  void tryTo() {
    COMBINATION.verify(
        (obj) -> Functions.tryTo(obj, MAPPER::apply),
        List.of("obj"),
        // obj
        OBJS);
  }

  @Test
  void tryToElse() {
    COMBINATION.verify(
        (obj, defaultValue) -> Functions.tryToElse(obj, MAPPER::apply, defaultValue),
        List.of("obj", "defaultValue"),
        // obj
        OBJS,
        // defaultValue
        DEFAULT_VALUES);
  }

  @Test
  void tryToElseGet() {
    COMBINATION.verify(
        (obj, supplier) -> Functions.tryToElseGet(obj, MAPPER::apply, supplier),
        List.of("obj", "supplier"),
        // obj
        OBJS,
        // supplier
        SUPPLIERS);
  }

  @Test
  void tryToElseGetOrNull() {
    COMBINATION.verify(
        (obj, supplier) -> Functions.tryToElseGetOrNull(obj, MAPPER::apply, supplier),
        List.of("obj", "supplier"),
        // obj
        OBJS,
        // supplier
        SUPPLIERS);
  }
}