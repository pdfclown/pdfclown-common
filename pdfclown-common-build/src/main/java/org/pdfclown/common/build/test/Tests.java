/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (Tests.java) is part of pdfclown-common-build module in pdfClown Common project (this
  Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.build.test;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.pdfclown.common.build.internal.util.Aggregations.cartesianProduct;
import static org.pdfclown.common.build.internal.util.Objects.fqnd;
import static org.pdfclown.common.build.internal.util.Objects.objTo;
import static org.pdfclown.common.build.internal.util.Objects.requireState;

import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableSupplier;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.pdfclown.common.build.internal.util.lang.Reflects;

/**
 * Test utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Tests {
  /**
   * Argument value wrapper to use within {@link Arguments}.
   *
   * @param <T>
   *          Argument value type.
   * @author Stefano Chizzolini
   */
  public static class Argument<T> {
    public static <T> Argument<T> of(T value, String description) {
      return new Argument<>(value, description);
    }

    private final String description;
    private final T value;

    protected Argument(T value, String description) {
      this.value = value;
      this.description = description;
    }

    /**
     * {@linkplain #getValue() Argument value} description.
     * <p>
     * Useful to describe what makes this data sample relevant to the test cases.
     * </p>
     */
    public String getDescription() {
      return description;
    }

    /**
     * Argument value.
     */
    public T getValue() {
      return value;
    }

    @Override
    public String toString() {
      return StringUtils.wrap(Objects.toString(value), '\'') + SPACE + "(" + description + ")";
    }
  }

  /**
   * Source code generator of {@code expected} test results for
   * {@link Tests#argumentsStream(List, List...) argumentsStream(..)}.
   * <p>
   * Automatically generates the codomain representation of the Cartesian product of the arguments
   * of parametric tests — eg:
   * </p>
   * <pre>
   *{@code /}/ expected
   *java.util.Arrays.asList(
   *  // from[0]
   *  URI.create(""),
   *  URI.create("../another/sub/to.html"),
   *  URI.create("../../another/my/sub/to.html"),
   *  // from[1]
   *  URI.create("to.html"),
   *  URI.create("another/sub/to.html"),
   *  URI.create("../../../my/sub/to.html"),
   *  . . .
   *  // from[5]
   *  URI.create("other/to.html"),
   *  URI.create("/sub/to.html"),
   *  URI.create("my/sub/to.html")),</pre>
   *
   * @author Stefano Chizzolini
   */
  public static class ArgumentsGenerator {
    private int count;
    /**
     * Argument counts.
     */
    private int[] counts;
    /**
     * Next expected result index.
     */
    private int index;
    /**
     * Argument moduli.
     */
    private int[] mods;

    /**
     * @param count
     *          Cartesian count of {@code args}.
     * @param args
     *          Arguments lists.
     */
    ArgumentsGenerator(int count, List<?>[] args) {
      this.count = count;

      counts = new int[args.length];
      mods = new int[args.length - 1];
      for (int i = 0; i < counts.length; i++) {
        counts[i] = args[i].size();
        if (i < mods.length) {
          mods[i] = (count /= counts[i]);
        }
      }
    }

    /**
     * Generates the source code representation of the expected result for
     * {@linkplain Tests#argumentsStream(List, List...) parameterized test feeding}.
     *
     * @param out
     *          Target stream.
     * @param expectedSourceCode
     *          Source code representation of the expected result.
     * @param argNames
     *          Argument names.
     * @param args
     *          Argument values.
     * @implNote For convenience, expected result representations are grouped via comment separators
     *           based on arguments' moduli — eg, if there are 3 arguments, with 9, 2 and 5 elements
     *           respectively, the expected results will be 90, composed by 9 groups corresponding
     *           to argument 1, each composed by 2 groups corresponding to argument 2, each
     *           comprising 5 elements corresponding to argument 3.
     */
    public void generateExpected(PrintStream out, String expectedSourceCode, List<String> argNames,
        List<?> args) {
      if (index == 0) {
        out.println("// expected\n"
            + "java.util.Arrays.asList(");
      }
      for (int i = 0; i < mods.length; i++) {
        if (index % mods[i] == 0) {
          out.printf("// %s %s[%s]='%s'\n",
              StringUtils.repeat('-', (i + 1) * 2),
              argNames.get(i),
              (i == 0 ? index : index % mods[i - 1]) / mods[i],
              abbreviate(Objects.toString(args.get(i)).lines().findFirst().get(), "[...]", 80));
        }
      }
      out.print(expectedSourceCode);

      if (++index == count) {
        out.println("),");
      } else {
        out.println(",");
      }
    }

    /**
     * Generates the source code representation of the expected result for
     * {@linkplain Tests#argumentsStream(List, List...) parameterized test feeding}.
     * <p>
     * The expected result is mapped to source code representation via
     * {@code expectedSourceCodeGenerator}; {@link Throwable} is mapped to {@link ThrownExpected}.
     * </p>
     *
     * @param out
     *          Target stream.
     * @param expected
     *          Expected result.
     * @param expectedSourceCodeGenerator
     *          Generates the source code representation of {@code expected}.
     * @param argNames
     *          Argument names.
     * @param args
     *          Argument values.
     * @implNote {@link ThrownExpected} replaces the actual {@link Throwable} type in order to
     *           simplify automated instantiation.
     */
    public <T> void generateExpectedOf(PrintStream out, T expected,
        Function<T, String> expectedSourceCodeGenerator, List<String> argNames, List<?> args) {
      generateExpected(out, objTo(expected, $ -> {
        if ($ instanceof Throwable) {
          var t = (Throwable) $;
          return String.format("new %s(\"%s\", \"%s\")", fqnd(ThrownExpected.class),
              t.getClass().getName(), t.getMessage());
        } else
          return expectedSourceCodeGenerator.apply($);
      }), argNames, args);
    }

    /**
     * Generates the source code representation of the expected result for
     * {@linkplain Tests#argumentsStream(List, List...) parameterized test feeding}.
     * <p>
     * The expected result is mapped to source code representation based on its value type:
     * </p>
     * <ul>
     * <li>primitive values and strings are mapped to their literal</li>
     * <li>{@link Throwable} is mapped to {@link ThrownExpected}</li>
     * <li>any other type is mapped to literal string — to customize its representation, use
     * {@link #generateExpectedOf(PrintStream, Object, Function, List, List)} instead</li>
     * </ul>
     *
     * @param out
     *          Target stream.
     * @param expected
     *          Expected result.
     * @param argNames
     *          Argument names.
     * @param args
     *          Argument values.
     * @implNote {@link ThrownExpected} replaces the actual {@link Throwable} type in order to
     *           simplify automated instantiation.
     */
    public <T> void generateExpectedOf(PrintStream out, T expected, List<String> argNames,
        List<?> args) {
      generateExpectedOf(out, expected, $ -> {
        if ($ instanceof Number)
          return $.toString();
        else if ($ instanceof Character)
          return "'" + $ + "'";
        else
          return $.toString().replace("\\", "\\\\").lines()
              .collect(joining("\\n\"\n+ \"", "\"", "\""));
      }, argNames, args);
    }
  }

  /**
   * Expected thrown exception.
   *
   * @author Stefano Chizzolini
   * @see Tests.ArgumentsGenerator#generateExpectedOf(PrintStream, Object, Function, List, List)
   * @see Tests#assertParameterized(Object, Object)
   */
  public static class ThrownExpected {
    private final String message;
    private final String name;

    public ThrownExpected(String name, String message) {
      this.name = name;
      this.message = message;
    }

    public String getMessage() {
      return message;
    }

    public String getName() {
      return name;
    }
  }

  private static ThreadLocal<ArgumentsGenerator> argumentsGenerator = new ThreadLocal<>();

  /**
   * Design-time arguments generator for {@link Tests#argumentsStream(List, List...)
   * argumentsStream(..)}.
   *
   * @throws IllegalStateException
   *           if called outside the argument generation session described in
   *           {@link #argumentsStream(List, List...)}.
   */
  public static ArgumentsGenerator argumentsGenerator() {
    return requireState(argumentsGenerator.get(),
        "`argumentsGenerator` is available only during parameterized argument generation "
            + "(see Tests.argumentsStream(..))");
  }

  /**
   * Combines the given argument lists into a stream of corresponding {@linkplain Arguments
   * parametric tuples} (Cartesian product) to feed a {@linkplain ParameterizedTest parameterized
   * test}.
   * <h2>Arguments generation</h2>
   * <p>
   * Since the size of {@code expected} rapidly escalates due to the Cartesian product of
   * {@code args}, in order to simplify the creation and maintenance of test cases, the source code
   * of {@code expected} can be automatically generated through {@link Tests#argumentsGenerator()
   * argumentsGenerator()}:
   * </p>
   * <ol>
   * <li>pass {@code null} as {@code expected} argument to this method:<pre>
   *import static java.util.Arrays.asList;
   *import static org.pdfclown.common.build.test.Tests.argumentsStream;
   *import static org.pdfclown.common.build.test.Tests.assertParameterized;
   *import static org.pdfclown.common.build.test.Tests.evalParameterized;
   *. . .
   *private static {@code Stream<Arguments>} _isFullExtension() {
   *  return argumentsStream(
   *    // expected
   *    null,
   *    // path
   *    asList(. . .),
   *    // extension
   *    asList(. . .));
   *}</pre></li>
   * <li>intercept the test case runs to generate the source code for each expected result (in this
   * case, it is printed to stderr):<pre>
   *{@code @}ParameterizedTest
   *{@code @}MethodSource
   *public void _isFullExtension(boolean expected, {@code Argument<String>} path, String extension) {
   *  var actual = evalParameterized(
   *    () {@code ->} Files.isFullExtension(path.getValue(), extension));
   *
   *  {@code /}*
   *   * DO NOT remove (useful in case of arguments update)
   *   *{@code /}
   *  org.pdfclown.common.build.test.Tests.argumentsGenerator().generateExpectedOf(System.err,
   *    actual,
   *    asList("path", "extension"),
   *    asList(path, extension));
   *
   *  assertParameterized(actual, expected);
   *}</pre></li>
   * <li>replace the previously-defined {@code null} {@code expected} argument (see step 1) with the
   * generated source code</li>
   * <li>comment the generative code (see step 2) for later use in case of arguments update</li>
   * </ol>
   *
   * @param expected
   *          Expected test results ({@code null}, to automatically populated for generative run).
   *          Corresponds to the codomain of the test, whose size MUST equal the Cartesian product
   *          of {@code args} (eg, if {@code args.size() == 2} and {@code args[0].size() == 5} and
   *          {@code args[1].size() == 4}, then {@code expected.size() == 20}).
   * @param args
   *          Test arguments. Each list corresponds to the domain of the respective argument.
   * @return A stream of tuples, each composed this way: <pre>
   *{ expected[y], args[0][x0], args[1][x1], ..., args[n][xn] }</pre>
   *         <p>
   *         where
   *         </p>
   *         <ul>
   *         <li><code>n == (args.size() - 1)</code></li>
   *         <li><code>y ∈ { 0 ... (args[0].size() * args[1].size() * ... * args[n].size()) }</code></li>
   *         <li><code>xX ∈ { 0 ... (args[X].size() - 1) }</code></li>
   *         </ul>
   */
  public static Stream<Arguments> argumentsStream(@Nullable List<?> expected, List<?>... args) {
    var argsList = List.of(args);
    int cartesianSize = argsList.stream().mapToInt(List::size).reduce(1, Math::multiplyExact);

    // Expected to stub?
    if (expected == null) {
      expected = Collections.nCopies(cartesianSize, null);

      argumentsGenerator.set(new ArgumentsGenerator(cartesianSize, args));
    } else {
      argumentsGenerator.set(null);
    }

    if (expected.size() != cartesianSize)
      throw new IllegalArgumentException(String.format(
          "`expected` size (%s) MISMATCH with `args` cartesian product (SHOULD be %s)",
          expected.size(), cartesianSize));

    final var expectedList = expected;
    var indexRef = new AtomicInteger();
    return cartesianProduct(argsList)
        .map($ -> {
          $.add(0, expectedList.get(indexRef.getAndIncrement()));
          return Arguments.of($.toArray());
        });
  }

  /**
   * Asserts the actual value corresponds to the expected one.
   * <p>
   * Intended for use within {@linkplain ParameterizedTest parameterized tests}, to check both valid
   * and invalid (ie, {@link Throwable}) results in a unified and consistent manner.
   * </p>
   *
   * @param actual
   *          Result of {@link #evalParameterized(FailableSupplier)}.
   * @param expected
   *          Expected result (provided by {@link #argumentsStream(List, List...)} and generated by
   *          {@link #argumentsGenerator()}; in case of expected invalid result (ie,
   *          {@link Throwable}), MUST be {@link ThrownExpected}).
   * @apiNote For example, to test a method whose signature is
   *          {@code Strings.abbreviateMultiline(String value, int maxLineCount,
   *     int averageLineLength, String marker)}:<pre>
   *import static java.util.Arrays.asList;
   *import static org.pdfclown.common.build.test.Tests.argumentsStream;
   *import static org.pdfclown.common.build.test.Tests.assertParameterized;
   *import static org.pdfclown.common.build.test.Tests.evalParameterized;
   *
   *public class StringsTest {
   *  . . .
   *
   *  private static {@code Stream<Arguments>} _abbreviateMultiline() {
   *    return argumentsStream(
   *      // expected
   *      asList(
   *        // -- value[0]='1:  A multi-line text to test whether Strings.abbreviateMultiline(..) metho[...]'
   *        // ---- maxLineCount[0]='10'
   *        // ------ averageLineLength[0]='80'
   *        "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
   *        . . .,
   *        // ------ averageLineLength[3]='0'
   *        "...",
   *        "[...]",
   *        // ---- maxLineCount[1]='6'
   *        // ------ averageLineLength[0]='80'
   *        "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
   *        . . .),
   *      // value
   *      asList(
   *        "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave "
   *          + "correctly.\n"
   *          . . .),
   *      // maxLineCount
   *      asList(
   *        10,
   *        6,
   *        0),
   *      // averageLineLength
   *      asList(
   *        80,
   *        40,
   *        20,
   *        0),
   *      // marker
   *      asList(
   *        "...",
   *        "[...]"));
   *  }
   *
   *  {@code /}*
   *   * NOTE: `expected` parameter SHOULD be `Object` whenever an invalid result (ie, thrown
   *   * exception encapsulated as `ThrownExpected`) is among the expected cases.
   *   *{@code /}
   *  {@code @}ParameterizedTest
   *  {@code @}MethodSource
   *  public void _abbreviateMultiline(Object expected, String value, int maxLineCount,
   *      int averageLineLength, String marker) {
   *    {@code /}*
   *     * NOTE: `actual` contains either the result of `Strings.abbreviateMultiline(..)` or
   *     * its thrown exception.
   *     *{@code /}
   *     var actual = evalParameterized(
   *         () {@code ->} Strings.abbreviateMultiline(value, maxLineCount, averageLineLength, marker));
   *
   *    {@code /}*
   *     * DO NOT remove (useful in case of arguments update)
   *     *{@code /}
   *    // org.pdfclown.common.build.test.Tests.argumentsGenerator().generateExpectedOf(System.err,
   *    //   actual,
   *    //   asList("value", "maxLineCount", "averageLineLength", "marker"),
   *    //   asList(value, maxLineCount, averageLineLength, marker));
   *
   *    assertParameterized(actual, expected);
   *  }
   *
   *  . . .
   *}</pre>
   */
  public static void assertParameterized(Object actual, Object expected) {
    /*
     * Arguments generation underway?
     *
     * NOTE: During arguments generation, parameterized assertions are skipped (otherwise, they
     * would fail fast amid arguments generation, since their state is incomplete).
     */
    if (argumentsGenerator.get() != null)
      return;

    if (actual instanceof Throwable) {
      var throwableActual = (Throwable) actual;

      if (!(expected instanceof ThrownExpected)) {
        fail(String.format("Failure UNEXPECTED (expected: %s)", expected), throwableActual);
      }

      var thrownExpected = (ThrownExpected) expected;
      assertThat("Throwable.class.name", throwableActual.getClass().getName(),
          is(thrownExpected.getName()));
      assertThat("Throwable.message", throwableActual.getMessage(),
          is(thrownExpected.getMessage()));
    } else {
      assertThat(actual, is(expected));
    }
  }

  /**
   * Evaluates the given expression.
   * <p>
   * Intended for use within {@linkplain ParameterizedTest parameterized tests}; its result is
   * expected to be checked via {@link #assertParameterized(Object, Object)}.
   * </p>
   *
   * @return The result of {@code expression}, or its thrown exception (unchecked exceptions
   *         ({@link UncheckedIOException}, {@link UndeclaredThrowableException}) are unwrapped).
   */
  public static Object evalParameterized(FailableSupplier<Object, Exception> expression) {
    try {
      return expression.get();
    } catch (Throwable ex) {
      if (ex instanceof UncheckedIOException)
        return ex.getCause();
      else if (ex instanceof UndeclaredThrowableException)
        return ((UndeclaredThrowableException) ex).getUndeclaredThrowable();
      else
        return ex;
    }
  }

  /**
   * Gets the stack frame of the currently executing test.
   * <p>
   * Useful to detect which test is currently executing.
   * </p>
   * <p>
   * Test detection is based on junit annotations (see <b>test method</b> definition in
   * {@link Test @Test}).
   * </p>
   */
  public static Optional<StackFrame> testFrame() {
    return Reflects.callerFrame($ -> {
      Method m = Reflects.method($);
      return m.isAnnotationPresent(Test.class)
          || m.isAnnotationPresent(RepeatedTest.class)
          || m.isAnnotationPresent(ParameterizedTest.class)
          || m.isAnnotationPresent(TestFactory.class)
          || m.isAnnotationPresent(TestTemplate.class);
    });
  }

  private Tests() {
  }
}
