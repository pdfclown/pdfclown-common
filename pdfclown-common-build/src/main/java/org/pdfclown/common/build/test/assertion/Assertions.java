/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Assertions.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static java.lang.Math.max;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.pdfclown.common.build.internal.util_.Aggregations.cartesianProduct;
import static org.pdfclown.common.build.internal.util_.Conditions.requireEqual;
import static org.pdfclown.common.build.internal.util_.Conditions.requireNotBlank;
import static org.pdfclown.common.build.internal.util_.Conditions.requireState;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Exceptions.wrongArg;
import static org.pdfclown.common.build.internal.util_.Objects.fqnd;
import static org.pdfclown.common.build.internal.util_.Objects.sqnd;
import static org.pdfclown.common.build.internal.util_.Objects.toLiteralString;
import static org.pdfclown.common.build.internal.util_.Strings.ELLIPSIS__CHICAGO;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;
import static org.pdfclown.common.build.internal.util_.Strings.NULL;
import static org.pdfclown.common.build.internal.util_.Strings.S;
import static org.pdfclown.common.build.internal.util_.Strings.SPACE;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.function.FailableSupplier;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.pdfclown.common.build.internal.util.system.Desktops;
import org.pdfclown.common.build.internal.util_.Objects;
import org.pdfclown.common.build.internal.util_.annot.Immutable;
import org.pdfclown.common.build.internal.util_.annot.Unmodifiable;
import org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig.Converter;
import org.pdfclown.common.build.util.system.Runtimes;

/**
 * Assertion utilities.
 * <p>
 * In particular, <b>parameterized tests</b> are provided a
 * {@linkplain #assertParameterized(Object, Expected, Supplier) convenient framework} to streamline
 * definition and maintenance of test cases:
 * </p>
 * <ul>
 * <li><b>regular results and failures</b> are unified in a single automated assertion path</li>
 * <li><b>input arguments</b> can be combined via Cartesian product for massive testing</li>
 * <li><b>expected results</b> are automatically generated on design time, relieving users from
 * wasteful maintenance efforts</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
public final class Assertions {
  /**
   * Argument value wrapper to use within {@link Arguments}.
   * <p>
   * Contrary to {@link Named}, its payload isn't unwrapped, so this argument is passed to the
   * parameterized test as-is — useful for expected result generation (see
   * {@link Assertions#assertParameterized(Object, Expected, Supplier) assertParameterized(..)}).
   * </p>
   * <table>
   * <caption>Argument instantiation summary</caption>
   * <tr>
   * <th>Scope</th>
   * <th>Representation<br>
   * (<code>toString()</code>)</th>
   * <th>Use</th>
   * </tr>
   * <tr>
   * <td>Display only (unwrap to test)</td>
   * <td>Name only</td>
   * <td>{@link Named#named(String, Object)}</td>
   * </tr>
   * <tr>
   * <td>Display only (unwrap to test)</td>
   * <td>Name and value</td>
   * <td>{@link Argument#qnamed(String, Object)}</td>
   * </tr>
   * <tr>
   * <td>Display and execution (pass as-is to test)</td>
   * <td>Name and value</td>
   * <td>{@link Argument#arg(String, Object)}</td>
   * </tr>
   * </table>
   *
   * @param <T>
   *          Argument value type.
   * @author Stefano Chizzolini
   */
  public static class Argument<T> {
    /**
     * Alias of {@link #of(String, Object)}.
     *
     * @param label
     *          Name.
     * @param value
     *          Payload.
     * @throws IllegalArgumentException
     *           if {@code label} is blank.
     */
    public static <T> Argument<T> arg(String label, @Nullable T value) {
      return of(label, value);
    }

    /**
     * Creates an argument value wrapper.
     * <p>
     * See "Argument instantiation summary" in {@link Argument} for more information.
     * </p>
     *
     * @param label
     *          Name.
     * @param value
     *          Payload.
     * @throws IllegalArgumentException
     *           if {@code label} is blank.
     */
    public static <T> Argument<T> of(String label, @Nullable T value) {
      return new Argument<>(label, value);
    }

    /**
     * Creates a named argument value with qualified string representation, showing both its payload
     * and its label.
     * <p>
     * See "Argument instantiation summary" in {@link Argument} for more information.
     * </p>
     *
     * @param label
     *          Name.
     * @param value
     *          Payload.
     * @throws IllegalArgumentException
     *           if {@code label} is blank.
     */
    public static <T> Named<T> qnamed(String label, @Nullable T value) {
      return Named.of(toString(requireNotBlank(label, "label"), value), value);
    }

    private static String toString(String label, @Nullable Object value) {
      return label.isEmpty() ? toLiteralString(value)
          : String.format(Locale.ROOT, "%s (%s)", toLiteralString(value), label);
    }

    private final String label;
    private final @Nullable T value;

    protected Argument(String label, @Nullable T value) {
      this.label = requireNotBlank(label, "label");
      this.value = value;
    }

    /**
     * Label of {@linkplain #getValue() argument value}.
     * <p>
     * Corresponds to {@link Named#getName()}.
     * </p>
     */
    public String getLabel() {
      return label;
    }

    /**
     * Argument value.
     * <p>
     * Corresponds to {@link Named#getPayload()}.
     * </p>
     */
    public @Nullable T getNullableValue() {
      return value;
    }

    /**
     * Argument value.
     * <p>
     * Corresponds to {@link Named#getPayload()}.
     * </p>
     *
     * @throws IllegalStateException
     *           if undefined.
     * @see #getNullableValue()
     */
    public T getValue() {
      return requireState(value);
    }

    @Override
    public String toString() {
      return toString(label, value);
    }
  }

  /**
   * {@linkplain #argumentsStream(ArgumentsStreamConfig, List, List[]) Arguments stream}
   * configuration.
   *
   * @author Stefano Chizzolini
   */
  public static class ArgumentsStreamConfig {
    /**
     * Argument converter.
     * <p>
     * NOTE: Contrary to the {@linkplain org.junit.jupiter.params.converter.ArgumentConverter JUnit
     * converter}, this one applies at an early stage of test invocation, so no parameter context is
     * available, just its index.
     * </p>
     *
     * @author Stefano Chizzolini
     * @see #argumentsStream(ArgumentsStreamConfig, List, List[])
     */
    public interface Converter extends BiFunction<Integer, @Nullable Object, @Nullable Object> {
      /**
       * Composes a converter that first applies this converter to its input, and then applies the
       * {@code after} converter to the result.
       */
      default Converter andThen(Converter after) {
        requireNonNull(after);
        return ($index, $source) -> after.apply($index, apply($index, $source));
      }

      /**
       * Maps an argument value to the type of the corresponding parameter in the parameterized test
       * method.
       *
       * @param index
       *          Parameter index.
       * @param source
       *          Argument value.
       */
      @Override
      @Nullable
      Object apply(Integer index, @Nullable Object source);

      /**
       * Composes a converter that first applies the {@code before} converter to its input, and then
       * applies this converter to the result.
       */
      default Converter compose(Converter before) {
        requireNonNull(before);
        return ($index, $source) -> apply($index, before.apply($index, $source));
      }
    }

    /**
     * {@linkplain #argumentsStream(ArgumentsStreamConfig, List, List[]) Arguments stream}
     * configuration for Cartesian-product argument tuples.
     *
     * @author Stefano Chizzolini
     */
    static class Cartesian extends ArgumentsStreamConfig {
      /**
       * {@link Cartesian} source code generator of the expected results of a parameterized test fed
       * by {@link #argumentsStream(ArgumentsStreamConfig, List, List[]) argumentsStream(..)}.
       * <p>
       * The generated source code looks like this:
       * </p>
       * <pre class="lang-java"><code>
       * // expected
       * java.util.Arrays.asList(
       *     // from[0]
       *     "",
       *     "../another/sub/to.html",
       *     // from[1]
       *     "to.html",
       *     "another/sub/to.html",
       *     . . .
       *     // from[5]
       *     "other/to.html",
       *     "/sub/to.html"),</code></pre>
       *
       * @author Stefano Chizzolini
       */
      static class Generator extends ExpectedGenerator {
        /**
         * Argument moduli.
         */
        private final int[] mods;

        /**
         * @param args
         *          Arguments lists.
         */
        Generator(List<List<?>> args) {
          super(count(args));

          mods = new int[args.size()];
          {
            var count = getCount();
            final var counts = new int[args.size()];
            for (int i = 0; i < counts.length; i++) {
              counts[i] = args.get(i).size();
              mods[i] = (count /= counts[i]);
            }
          }
        }

        @Override
        protected void generateExpectedComment(ExpectedGeneration generation) {
          for (int i = 0, last = mods.length - 1; i <= last; i++) {
            if (getIndex() % mods[i] == 0) {
              // Main level separator.
              if (i == 0 && generation.args.size() > 1 && getIndex() > 0) {
                out().append(INDENT).println("//");
              }

              // Level title.
              var arg = generation.args.get(i);
              var indexLabel = i == last ? "[" + (getIndex() + 1) + "] " : EMPTY;
              var argIndent = max(0, 2 * i - indexLabel.length());
              out().append(INDENT).printf("// %s%s%s%s[%s]: %s\n",
                  indexLabel,
                  repeat('-', argIndent),
                  argIndent == 0 ? EMPTY : SPACE,
                  arg.getKey(),
                  (i == 0 ? getIndex() : getIndex() % mods[i - 1]) / mods[i],
                  formatArgComment(arg, generation));
            }
          }
        }
      }

      private static int count(List<List<?>> args) {
        return args.stream().mapToInt(List::size).reduce(1, Math::multiplyExact);
      }

      Cartesian() {
        super(
            ($expected, $args) -> {
              var indexRef = new AtomicInteger();
              return cartesianProduct($args)
                  .map($ -> {
                    $.add(0, $expected.get(indexRef.getAndIncrement()));
                    return Arguments.of($.toArray());
                  });
            },
            Cartesian::count,
            Generator::new);
      }
    }

    /**
     * {@linkplain #argumentsStream(ArgumentsStreamConfig, List, List[]) Arguments stream}
     * configuration for plain argument tuples.
     *
     * @author Stefano Chizzolini
     */
    static class Simple extends ArgumentsStreamConfig {
      /**
       * {@link Simple} source code generator of the expected results of a parameterized test fed by
       * {@link #argumentsStream(ArgumentsStreamConfig, List, List[]) argumentsStream(..)}.
       * <p>
       * The generated source code looks like this:
       * </p>
       * <pre class="lang-java"><code>
       * // expected
       * java.util.Arrays.asList(
       *     // from[0]
       *     "",
       *     // from[1]
       *     "to.html",
       *     . . .
       *     // from[5]
       *     "other/to.html"),</code></pre>
       *
       * @author Stefano Chizzolini
       */
      static class Generator extends ExpectedGenerator {
        /**
         * @param args
         *          Arguments lists.
         */
        Generator(List<List<?>> args) {
          super(count(args));
        }

        @Override
        protected void generateExpectedComment(ExpectedGeneration generation) {
          out().append(INDENT).printf("// [%s] ", getIndex() + 1);
          for (int i = 0; i < generation.args.size(); i++) {
            if (i > 0) {
              out().append("; ");
            }

            var arg = generation.args.get(i);
            out().printf("%s[%s]: %s", arg.getKey(), getIndex(), formatArgComment(arg, generation));
          }
          out().append("\n");
        }
      }

      private static int count(List<List<?>> args) {
        int ret = 0;
        for (var it = args.listIterator(); it.hasNext();) {
          int argSize = it.next().size();
          if (ret == 0) {
            ret = argSize;
          } else {
            if (argSize != ret)
              throw wrongArg("args[" + (it.nextIndex() - 1) + "].size", argSize,
                  "INVALID (should be {})", ret);
          }
        }
        return ret;
      }

      Simple() {
        super(
            ($expected, $args) -> IntStream.range(0, $expected.size())
                .mapToObj($ -> {
                  int i;
                  var testArgs = new Object[1 + $args.size()];
                  testArgs[i = 0] = $expected.get($);
                  while (++i < testArgs.length) {
                    testArgs[i] = $args.get(i - 1).get($);
                  }
                  return Arguments.of(testArgs);
                }),
            Simple::count,
            Generator::new);
      }
    }

    /**
     * New configuration for Cartesian-product
     * {@linkplain #argumentsStream(ArgumentsStreamConfig, List, List[]) arguments stream}.
     * <p>
     * The size of {@code expected} MUST be equal to the product of the sizes of {@code args}.
     * </p>
     * <p>
     * The resulting argument tuples will be composed this way:
     * </p>
     * <pre>
    * (expected[i], args[0][j<sub>0</sub>], args[1][j<sub>1</sub>], .&nbsp;.&nbsp;., args[n][j<sub>n</sub>])</pre>
     * <p>
     * where
     * </p>
     * <pre>
     * expected[i] = f(args[0][j<sub>0</sub>], args[1][j<sub>1</sub>], .&nbsp;.&nbsp;., args[n][j<sub>n</sub>])
     * i ∈ N : 0 &lt;= i &lt; args[0].size() * args[1].size() * .&nbsp;.&nbsp;. * args[n].size()
     * n = args.size() - 1
     * j<sub>m</sub> ∈ N : 0 &lt;= j<sub>m</sub> &lt; args[m].size()
     * m ∈ N : 0 &lt;= m &lt;= n
     * </pre>
     */
    public static ArgumentsStreamConfig cartesian() {
      return new Cartesian();
    }

    /**
     * New configuration for plain {@linkplain #argumentsStream(ArgumentsStreamConfig, List, List[])
     * arguments stream}.
     * <p>
     * All argument lists ({@code expected} and each list in {@code args}) MUST have the same size.
     * </p>
     * <p>
     * The resulting argument tuples will be composed this way:
     * </p>
     * <pre>
    * (expected[i], args[0][i], args[1][i], .&nbsp;.&nbsp;., args[n][i])</pre>
     * <p>
     * where
     * </p>
     * <pre>
     * expected[i] = f(args[0][i], args[1][i], .&nbsp;.&nbsp;., args[n][i])
     * i ∈ N : 0 &lt;= i &lt; m
     * m = expected.size() = args[0].size() = args[1].size() = .&nbsp;.&nbsp;. = args[n].size()
     * n = args.size() - 1
     * </pre>
     */
    public static ArgumentsStreamConfig simple() {
      return new Simple();
    }

    final BiFunction<List<?>, List<List<?>>, Stream<Arguments>> argumentsStreamer;
    @Nullable
    Converter converter;
    final ToIntFunction<List<List<?>>> expectedCounter;
    final Function<List<List<?>>, ExpectedGenerator> expectedGeneratorProvider;

    protected ArgumentsStreamConfig(
        BiFunction<List<?>, List<List<?>>, Stream<Arguments>> argumentsStreamer,
        ToIntFunction<List<List<?>>> expectedCounter,
        Function<List<List<?>>, ExpectedGenerator> expectedGeneratorProvider) {
      this.argumentsStreamer = argumentsStreamer;
      this.expectedCounter = expectedCounter;
      this.expectedGeneratorProvider = expectedGeneratorProvider;
    }

    /**
     * Prepends to {@link #getConverter() converter} a function for the argument at the given
     * position in the arguments list.
     * <p>
     * For example, if the parameterized test looks like this:
     * </p>
     * <pre class="lang-java"><code>
     * &#64;ParameterizedTest
     * &#64;MethodSource
     * public void myTestName(Expected&lt;String&gt; expected, ArgType0 arg0, ArgType1 arg1, . . ., ArgTypeN argN) {
     *   assertParameterizedOf(
     *       () -&gt; myMethodToTest(arg0, arg1, . . ., argN),
     *       expected,
     *       () -&gt; new ExpectedGeneration(List.of(
     *           entry("arg0", arg0),
     *           entry("arg1", arg1),
     *           . . .
     *           entry("argN", argN))));
     * }</code></pre>
     * <p>
     * then {@code argIndex} = 0 will define the converter to {@code ArgType0} of the input values
     * of parameterized test argument {@code arg0} which corresponds to {@code args[0]} of
     * {@link #argumentsStream(ArgumentsStreamConfig, List, List[]) argumentsStream(..)}.
     * </p>
     *
     * @param <T>
     *          Input type.
     * @implNote {@code <T>} provides implicit casting to the type commanded by the function; this
     *           works conveniently like a type declaration, considering the weakly-typed context of
     *           arguments streams, sparing redundant explicit casting.
     * @see #composeExpectedConverter(Function)
     */
    @SuppressWarnings("unchecked")
    public <T> ArgumentsStreamConfig composeArgConverter(
        int argIndex, Function<@Nullable T, @Nullable Object> before) {
      requireNonNull(before);
      var paramIndex = argIndex + 1 /* Offsets `expected` parameter */;
      return composeConverter(
          ($index, $source) -> $index == paramIndex ? before.apply((T) $source) : $source);
    }

    /**
     * Prepends to {@link #getConverter() converter} a function.
     *
     * @see #composeArgConverter(int, Function)
     * @see #composeExpectedConverter(Function)
     */
    public ArgumentsStreamConfig composeConverter(Converter before) {
      converter = converter != null ? converter.compose(before) : before;
      return this;
    }

    /**
     * Prepends to {@link #getConverter() converter} a function for {@code expected} argument.
     *
     * @param <T>
     *          Input type.
     * @implNote {@code <T>} provides implicit casting to the type commanded by the function; this
     *           works conveniently like a type declaration, considering the weakly-typed context of
     *           arguments streams, sparing redundant explicit casting.
     * @see #composeArgConverter(int, Function)
     */
    @SuppressWarnings("unchecked")
    public <T> ArgumentsStreamConfig composeExpectedConverter(
        Function<@Nullable T, @Nullable Object> before) {
      requireNonNull(before);
      return composeConverter(
          ($index, $source) -> $index == 0 ? before.apply((T) $source) : $source);
    }

    /**
     * Arguments converter.
     * <p>
     * Transforms the values of {@code expected} and {@code args} arguments of
     * {@link #argumentsStream(ArgumentsStreamConfig, List, List[]) argumentsStream(..)} before they
     * are streamed (useful, for example, to wrap a parameterized test argument as {@linkplain Named
     * named}).
     * </p>
     * <p>
     * DEFAULT: {@code null} (arguments passed as-is — for efficiency, this should be used instead
     * of identity transformation).
     * </p>
     * <p>
     * For more information, see "Arguments Conversion" section in
     * {@link #argumentsStream(ArgumentsStreamConfig, List, List[]) argumentsStream(..)}.
     * </p>
     */
    public @Nullable Converter getConverter() {
      return converter;
    }

    /**
     * Sets {@link #getConverter() converter}.
     *
     * @see #composeConverter(Converter)
     */
    public ArgumentsStreamConfig setConverter(@Nullable Converter value) {
      converter = value;
      return this;
    }
  }

  /**
   * Parameterized test result.
   *
   * @author Stefano Chizzolini
   * @see Assertions#assertParameterized(Object, Expected, Supplier)
   */
  public static class Expected<T> {
    /**
     * New failed result.
     *
     * @param thrown
     *          Thrown exception.
     */
    public static <T> Expected<T> failure(Failure thrown) {
      return new Expected<>(null, requireNonNull(thrown));
    }

    /**
     * New regular result.
     *
     * @param returned
     *          Returned value.
     */
    public static <T> Expected<T> success(@Nullable T returned) {
      return new Expected<>(returned, null);
    }

    @Nullable
    Function<T, Matcher<? super T>> matcherProvider;
    @Nullable
    final T returned;
    @Nullable
    final Failure thrown;

    private Expected(@Nullable T returned, @Nullable Failure thrown) {
      this.thrown = thrown;
      this.returned = returned;
    }

    /**
     * Regular result.
     */
    public @Nullable T getReturned() {
      return returned;
    }

    /**
     * Thrown exception.
     */
    public @Nullable Failure getThrown() {
      return thrown;
    }

    /**
     * Whether this result represents a failure; if so, {@link #getThrown() thrown} is defined.
     */
    public boolean isFailure() {
      return thrown != null;
    }

    /**
     * Whether this result represents a success; if so, {@link #getThrown() thrown} is undefined.
     */
    public boolean isSuccess() {
      return !isFailure();
    }

    /**
     * Sets the custom matcher to validate the actual regular result against the expected one.
     */
    public Expected<T> match(Function<T, Matcher<? super T>> matcherProvider) {
      this.matcherProvider = matcherProvider;
      return this;
    }

    @Override
    public String toString() {
      return toLiteralString(returned != null ? returned : thrown);
    }

    Matcher<? super T> getMatcher() {
      return matcherProvider != null && returned != null
          ? matcherProvider.apply(returned)
          : is(returned);
    }
  }

  /**
   * Generation feed for the expected results of a parameterized test.
   *
   * @author Stefano Chizzolini
   * @see Assertions#assertParameterized(Object, Expected, Supplier)
   */
  public static class ExpectedGeneration {
    private static final int MAX_ARG_COMMENT_LENGTH__DEFAULT = 50;

    /**
     * Gets the constructor source code to use in {@link #setExpectedSourceCodeGenerator(Function)}.
     *
     * @see #expectedSourceCodeForFactory(Class, String, Object...)
     */
    public static String expectedSourceCodeForConstructor(Class<?> type, @Nullable Object... args) {
      return String.format("new %s(%s)", fqnd(type),
          Arrays.stream(args).map(Objects::toLiteralString).collect(Collectors.joining(",")));
    }

    /**
     * Gets factory source code to use in {@link #setExpectedSourceCodeGenerator(Function)}.
     *
     * @see #expectedSourceCodeForConstructor(Class, Object...)
     */
    public static String expectedSourceCodeForFactory(Class<?> type, String methodName,
        @Nullable Object... args) {
      return String.format("%s.%s(%s)", fqnd(type), methodName,
          Arrays.stream(args).map(Objects::toLiteralString).collect(Collectors.joining(",")));
    }

    String argCommentAbbreviationMarker = ELLIPSIS__CHICAGO;
    Function<@Nullable Object, String> argCommentFormatter = Objects::toLiteralString;
    final List<Entry<String, @Nullable Object>> args;
    Function<Object, String> expectedSourceCodeGenerator = Objects::toLiteralString;
    int maxArgCommentLength = MAX_ARG_COMMENT_LENGTH__DEFAULT;
    PrintStream out = System.err;
    boolean outOverridable = true;

    /**
     */
    public ExpectedGeneration(List<Entry<String, @Nullable Object>> args) {
      this.args = requireNonNull(args);
    }

    /**
     * Prepends to {@link #getExpectedSourceCodeGenerator() expectedSourceCodeGenerator} a function.
     */
    public ExpectedGeneration composeExpectedSourceCodeGenerator(Function<Object, String> before) {
      expectedSourceCodeGenerator = expectedSourceCodeGenerator.compose(before);
      return this;
    }

    /**
     * Abbreviation marker to append to argument values exceeding {@link #getMaxArgCommentLength()
     * maxArgCommentLength} in comments accompanying expected results source code.
     * <p>
     * DEFAULT: <code>".&nbsp;.&nbsp;."</code>
     * </p>
     */
    public String getArgCommentAbbreviationMarker() {
      return argCommentAbbreviationMarker;
    }

    /**
     * Formatter of the argument values in comments accompanying expected results source code.
     * <p>
     * DEFAULT: literal representation.
     * </p>
     */
    public Function<@Nullable Object, String> getArgCommentFormatter() {
      return argCommentFormatter;
    }

    /**
     * Arguments (pairs of parameter name and corresponding argument value) consumed by the current
     * test invocation.
     */
    public List<Entry<String, @Nullable Object>> getArgs() {
      return args;
    }

    /**
     * Source code generator of the expected results.
     * <p>
     * DEFAULT: literal representation.
     * </p>
     */
    public Function<Object, String> getExpectedSourceCodeGenerator() {
      return expectedSourceCodeGenerator;
    }

    /**
     * Maximum length of argument values in comments accompanying expected results source code.
     * <p>
     * DEFAULT: {@value #MAX_ARG_COMMENT_LENGTH__DEFAULT}
     * </p>
     */
    public int getMaxArgCommentLength() {
      return maxArgCommentLength;
    }

    /**
     * Stream where the generated expected results source code will be output.
     * <p>
     * DEFAULT: {@linkplain System#err stderr}
     * </p>
     */
    public PrintStream getOut() {
      return out;
    }

    /**
     * Whether {@link #getOut() out} can be replaced by the generator (for example, to divert the
     * output to the clipboard — see debug mode in "Expected Results Generation" section within
     * {@link #argumentsStream(ArgumentsStreamConfig, List, List[]) argumentsStream(..)}).
     * <p>
     * DEFAULT: {@code true}
     * </p>
     */
    public boolean isOutOverridable() {
      return outOverridable;
    }

    /**
     * Sets {@link #getArgCommentAbbreviationMarker() argCommentAbbreviationMarker}.
     */
    public ExpectedGeneration setArgCommentAbbreviationMarker(String value) {
      argCommentAbbreviationMarker = requireNonNull(value);
      return this;
    }

    /**
     * Sets {@link #getArgCommentFormatter() argCommentFormatter}.
     */
    public ExpectedGeneration setArgCommentFormatter(Function<@Nullable Object, String> value) {
      argCommentFormatter = requireNonNull(value);
      return this;
    }

    /**
     * Sets {@link #getExpectedSourceCodeGenerator() expectedSourceCodeGenerator}.
     */
    public ExpectedGeneration setExpectedSourceCodeGenerator(
        Function<Object, String> value) {
      expectedSourceCodeGenerator = requireNonNull(value);
      return this;
    }

    /**
     * Sets {@link #getMaxArgCommentLength() maxArgCommentLength}.
     */
    public ExpectedGeneration setMaxArgCommentLength(int value) {
      maxArgCommentLength = value;
      return this;
    }

    /**
     * Sets {@link #getOut() out}.
     */
    public ExpectedGeneration setOut(PrintStream value) {
      out = requireNonNull(value);
      return this;
    }

    /**
     * Sets {@link #isOutOverridable() outOverridable}.
     */
    public ExpectedGeneration setOutOverridable(boolean value) {
      outOverridable = value;
      return this;
    }
  }

  /**
   * Source code generator of the expected results of a parameterized test.
   * <p>
   * The generated source code looks like this:
   * </p>
   * <pre class="lang-java"><code>
   * // expected
   * java.util.Arrays.asList(
   *     // expectedComment_0
   *     expected_0,
   *     // expectedComment_1
   *     expected_1,
   *     . . .
   *     // expectedComment_n
   *     expected_n),</code></pre>
   *
   * @author Stefano Chizzolini
   */
  public abstract static class ExpectedGenerator {
    protected static final String INDENT = S + SPACE + SPACE;

    /**
     * Arguments tuples count.
     */
    private final int count;
    /**
     * Current arguments tuple index.
     */
    private int index = -1;
    /**
     * Target stream (either internal (for output redirection to clipboard, in case of debug mode)
     * or {@linkplain #generateExpected(Object, ExpectedGeneration) provided}) the generated
     * representation is written to.
     */
    private @Nullable PrintStream out;
    /**
     * Internal target stream buffer.
     *
     * @see #out
     */
    private @Nullable ByteArrayOutputStream outBuffer;

    protected ExpectedGenerator(int count) {
      this.count = count;
    }

    /**
     * Generates the source code representation of the expected result for
     * {@linkplain #argumentsStream(ArgumentsStreamConfig, List, List[]) parameterized test
     * feeding}.
     * <p>
     * The expected result is mapped to source code representation based on its value type:
     * </p>
     * <ul>
     * <li>failed result (thrown {@link Throwable}) — to {@link Failure}</li>
     * <li>regular result — via
     * {@code generation.}{@link ExpectedGeneration#getExpectedSourceCodeGenerator()
     * expectedSourceCodeGenerator}</li>
     * <li>{@code null} — to literal null ({@code "null"})</li>
     * </ul>
     *
     * @param expected
     *          Expected result.
     * @param generation
     *          Generation feed for the expected result of the parameterized test.
     * @implNote {@link Failure} replaces the actual {@link Throwable} type in order to disambiguate
     *           between thrown exceptions and exceptions returned as regular results.
     */
    public <T> void generateExpected(@Nullable Object expected, ExpectedGeneration generation) {
      beginExpected(generation);

      generateExpectedComment(generation);
      generateExpectedSourceCode(expected, generation);

      endExpected();
    }

    /**
     * Expected results count.
     */
    public int getCount() {
      return count;
    }

    /**
     * Current expected result index.
     */
    public int getIndex() {
      return index;
    }

    public boolean isComplete() {
      return out == null && index >= 0;
    }

    protected String formatArgComment(Entry<String, @Nullable Object> arg,
        ExpectedGeneration generation) {
      String comment;
      var ret = abbreviate(
          comment = generation.argCommentFormatter.apply(arg.getValue()).lines().findFirst()
              .orElse(EMPTY),
          generation.argCommentAbbreviationMarker,
          generation.maxArgCommentLength);
      /*
       * NOTE: In case of abbreviation of quoted string, the ending quote MUST be restored.
       */
      if (!ret.equals(comment)) {
        char endChar = comment.charAt(comment.length() - 1);
        switch (endChar) {
          case '\'':
          case '"':
            if (endChar == comment.charAt(0)) {
              ret += endChar;
            }
            // FALLTHRU
          default:
        }
      }
      return ret;
    }

    protected abstract void generateExpectedComment(ExpectedGeneration generation);

    protected <T> void generateExpectedSourceCode(@Nullable Object expected,
        ExpectedGeneration generation) {
      String expectedSourceCode;
      if (expected == null) {
        expectedSourceCode = NULL;
      } else if (expected instanceof Failure) {
        var failure = (Failure) expected;
        expectedSourceCode = String.format("new %s(\"%s\", %s)",
            fqnd(Failure.class), failure.getName(), toLiteralString(failure.getMessage()));
      } else {
        //noinspection unchecked
        expectedSourceCode = generation.expectedSourceCodeGenerator.apply((T) expected);
      }
      out().append(INDENT).append(expectedSourceCode);
    }

    protected PrintStream out() {
      assert out != null;
      return out;
    }

    private void begin(ExpectedGeneration generation) {
      // Output redirect to clipboard?
      if (generation.outOverridable && Runtimes.isDebugging() && Desktops.isGUI()) {
        out = new PrintStream(outBuffer = new ByteArrayOutputStream(), true, UTF_8);
      }
      // No output redirect.
      else {
        out = generation.out;

        printInfo("Expected results source code generation underway...");
      }
    }

    private void beginExpected(ExpectedGeneration generation) {
      if (++index == 0) {
        begin(generation);

        out().println("// expected\n"
            + "java.util.Arrays.asList(");
      }
    }

    private void end() {
      assert out != null;

      var message = new StringBuilder("Expected results source code GENERATED");
      if (outBuffer != null) {
        // Transfer output to clipboard!
        Desktops.copyToClipboard(outBuffer.toString(UTF_8));

        message.append(", and COPIED to clipboard (IMPORTANT: in order to work, a clipboard "
            + "manager must be active on the system)");

        out.close();
      }
      printInfo(message);

      out = null;
    }

    private void endExpected() {
      if (index == getCount() - 1) {
        out().println("),");

        end();
      } else {
        out().println(",");
      }
    }

    private void printInfo(Object text) {
      System.err.printf("\n[%s] %s\n\n", sqnd(this), text);
    }
  }

  /**
   * Expected thrown exception.
   *
   * @author Stefano Chizzolini
   * @see #assertParameterized(Object, Expected, Supplier)
   */
  @Immutable
  public static class Failure {
    private final @Nullable String message;
    private final String name;

    public Failure(String name, @Nullable String message) {
      this.name = requireNonNull(name, "`name`");
      this.message = message;
    }

    public @Nullable String getMessage() {
      return message;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return sqnd(name) + " (" + abbreviate(message, 30) + ")";
    }
  }

  private static final ThreadLocal<@Nullable ExpectedGenerator> expectedGenerator =
      new ThreadLocal<>();

  @Unmodifiable
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static final Expected<?> EXPECTED__VOID = new Expected(null, null) {
    @Override
    public Expected match(Function matcherProvider) {
      // NOP
      return this;
    }
  };

  /**
   * Combines argument lists into a stream of corresponding {@linkplain Arguments parametric tuples}
   * to feed a {@linkplain ParameterizedTest parameterized test}.
   * <p>
   * The {@code expected} parameter shall contain both regular results and failures (that is, thrown
   * exceptions, represented as {@link Failure} for automatic handling) of the method tested with
   * {@code args}.
   * </p>
   * <p>
   * The corresponding parameterized test shall look like this:
   * </p>
   * <pre class="lang-java"><code>
   * &#64;ParameterizedTest
   * &#64;MethodSource
   * public void myTestName(Expected&lt;String&gt; expected, ArgType0 arg0, ArgType1 arg1, . . ., ArgTypeN argN) {
   *   assertParameterizedOf(
   *       () -&gt; myMethodToTest(arg0, arg1, . . ., argN),
   *       expected,
   *       () -&gt; new ExpectedGeneration(List.of(
   *           entry("arg0", arg0),
   *           entry("arg1", arg1),
   *           . . .
   *           entry("argN", argN))));
   * }</code></pre>
   * <p>
   * See {@link #assertParameterized(Object, Expected, Supplier) assertParameterized(..)} for more
   * information and a full example.
   * </p>
   * <h4>Expected Results Generation</h4>
   * <p>
   * The source code representation of {@code expected} is automatically generated as described in
   * this section — this simplifies the preparation and maintenance of test cases, also unifying the
   * way failed results (that is, thrown exceptions) are handled along with regular ones.
   * </p>
   * <p>
   * Here, the steps to generate {@code expected} (based on the example in
   * {@link #assertParameterized(Object, Expected, Supplier) assertParameterized(..)}):
   * </p>
   * <ol>
   * <li>pass {@code null} as {@code expected} argument to this
   * method:<pre class="lang-java" data-line="5"><code>
   * private static Stream&lt;Arguments&gt; uncapitalizeGreedy() {
   *   return argumentsStream(
   *       ArgumentsStreamConfig.cartesian(),
   *       // expected
   *       <span style="background-color:yellow;color:black;">null</span>,
   *       // value
   *       asList(
   *           "Capitalized",
   *           "uncapitalized",
   *           . . .
   *           "UNDERSCORE_TEST"));
   * }</code></pre></li>
   * <li>in your IDE, <i>run the test in debug mode — this will cause the generated source code to
   * be copied directly into your clipboard</i>, ready to get pasted as {@code expected} argument
   * (see next step).
   * <p>
   * Conversely, if run in normal mode, the generated source code will be output to
   * {@link ExpectedGeneration#out}, as specified in the
   * {@link #assertParameterized(Object, Expected, Supplier) assertParameterized(..)} call (by
   * default, {@linkplain System#err stderr}):
   * </p>
   * <pre class="lang-java" data-line="8"><code>
   * &#64;ParameterizedTest
   * &#64;MethodSource
   * public void uncapitalizeGreedy(Expected&lt;String&gt; expected, String value) {
   *   assertParameterizedOf(
   *       () -&gt; Strings.uncapitalizeGreedy(value),
   *       expected,
   *       () -&gt; new ExpectedGeneration(List.of(
   *           entry("value", value)))); // <span style=
   * "background-color:yellow;color:black;">&lt;- No `out` stream specified here, so it defaults to stderr</span>
   * }</code></pre></li>
   * <li>replace {@code null} (step 1) with the generated source code (step 2) as {@code expected}
   * argument:<pre class="lang-java" data-line="5-12"><code>
   * private static Stream&lt;Arguments&gt; uncapitalizeGreedy() {
   *   return argumentsStream(
   *       ArgumentsStreamConfig.cartesian(),
   *       // expected
   *       <span style="background-color:yellow;color:black;">java.util.Arrays.asList(
   *           // value[0]: 'Capitalized'
   *           "capitalized",
   *           // value[1]: 'uncapitalized'
   *           "uncapitalized",
   *           . . .
   *           // value[7]: 'UNDERSCORE_TEST'
   *           "underscore_TEST")</span>,
   *       // value
   *       asList(
   *           "Capitalized",
   *           "uncapitalized",
   *           . . .
   *           "UNDERSCORE_TEST"));
   * }</code></pre></li>
   * </ol>
   * <h4>Arguments Conversion</h4>
   * <p>
   * JUnit 5 allows to customize the representation of input values for parameterized test arguments
   * in the invocation display name via {@link Named}, and to explicitly convert such input values
   * to parameterized test arguments via {@link org.junit.jupiter.params.converter.ArgumentConverter
   * ArgumentConverter}. Whilst effective, such mechanisms are a bit convoluted ({@code Named} is
   * typically applied statically one by one (or via ad-hoc transformation); on the other hand,
   * explicit {@code ArgumentConverter} requires the corresponding arguments to be annotated one by
   * one with {@link org.junit.jupiter.params.converter.ConvertWith @ConvertWith} or a custom
   * meta-annotation (there is a long-standing discussion about
   * <a href="https://github.com/junit-team/junit5/issues/853">decoupling conversion via service</a>
   * which, at the moment, is 8 years old...)) and mutually agnostic (the argument payload is
   * extracted from {@code Named} and passed to the converter).
   * </p>
   * <p>
   * Such behavior is inconvenient for the arguments streams generated by this method: for
   * consistency, the generated source code of the expected results (see "Expected Results
   * Generation" section here above) should use the same argument descriptions displayed in the
   * invocation name; anticipating argument conversion at arguments stream generation allows to
   * combine custom display representation and custom mapping in a single step, without bloating the
   * target test method with parameter annotations. In order to do so,
   * {@linkplain ArgumentsStreamConfig#setConverter(Converter) pass the converter} to {@code config}
   * parameter.
   * </p>
   *
   * @param config
   *          Stream configuration.
   * @param expected
   *          Expected test results, or {@code null} to automatically generate them (see "Expected
   *          Results Generation" section here above). Corresponds to the <b>codomain of the
   *          test</b>.
   * @param args
   *          Test arguments. Corresponds to the <b>domain of the respective test argument</b>.
   * @return A stream of tuples, composed according to the algorithm provided by {@code config}.
   */
  public static Stream<Arguments> argumentsStream(ArgumentsStreamConfig config,
      @Nullable List<?> expected, List<?>... args) {
    // Argument lists transformation.
    List<Expected<?>> expectedList;
    List<List<?>> argsList;
    {
      // 1. Arguments values (tested method inputs).
      if (config.converter != null) {
        argsList = new ArrayList<>(args.length);
        int paramIndex = 1;
        for (List<?> sourceList : args) {
          var argList = new ArrayList<>(sourceList.size());
          for (var e : sourceList) {
            argList.add(config.converter.apply(paramIndex, e));
          }
          argsList.add(unmodifiableList(argList));
          paramIndex++;
        }
        argsList = unmodifiableList(argsList);
      } else {
        argsList = asList(args);
      }
      int expectedCount = config.expectedCounter.applyAsInt(argsList);

      // 2. Expected results (tested method output).
      if (expected != null) {
        requireEqual(expected.size(), expectedCount, "expected.size");

        expectedList = new ArrayList<>(expected.size());
        for (var e : expected) {
          expectedList.add(e instanceof Failure
              ? Expected.failure((Failure) e)
              : Expected.success(config.converter != null
                  ? config.converter.apply(0, e)
                  : e));
        }
        expectedList = unmodifiableList(expectedList);
      }
      // Expected results generation mode.
      else {
        expectedList = Collections.nCopies(expectedCount, EXPECTED__VOID);
        expectedGenerator.set(config.expectedGeneratorProvider.apply(argsList));
      }
    }

    // Combine the argument lists into the arguments stream!
    return config.argumentsStreamer.apply(expectedList, argsList);
  }

  /**
   * Asserts that {@code expected} and {@code actual} are equal applying a comparator.
   * <p>
   * This method supplements junit's
   * {@link org.junit.jupiter.api.Assertions#assertIterableEquals(Iterable, Iterable)
   * assertIterableEquals(..)} (which uses standard {@link Object#equals(Object)} for the same
   * purpose) to make the evaluation more flexible.
   * </p>
   */
  public static <T> void assertIterableEquals(Iterable<T> expected, Iterable<T> actual,
      Comparator<T> comparator) {
    if (expected == actual)
      return;

    Iterator<T> expectedItr = expected.iterator();
    Iterator<T> actualItr = actual.iterator();
    while (expectedItr.hasNext()) {
      assertTrue(actualItr.hasNext());

      T expectedElement = expectedItr.next();
      T actualElement = actualItr.next();
      //noinspection SimplifiableAssertion
      assertTrue(comparator.compare(expectedElement, actualElement) == 0);
    }
    assertFalse(actualItr.hasNext());
  }

  /**
   * Asserts the actual value corresponds to the expected one.
   * <p>
   * Intended for use within {@linkplain ParameterizedTest parameterized tests}, to check both
   * regular results and failures (that is, {@link Throwable}) in a unified and consistent manner.
   * </p>
   * <p>
   * See {@link #argumentsStream(ArgumentsStreamConfig, List, List[]) argumentsStream(..)} for more
   * information about parameterized test definition.
   * </p>
   *
   * @param actual
   *          Result of the method tested via {@link #evalParameterized(FailableSupplier)}.
   * @param expected
   *          Expected result, provided by
   *          {@link #argumentsStream(ArgumentsStreamConfig, List, List[]) argumentsStream(..)}; it
   *          can be passed as-is (for {@linkplain Matchers#is(Object) exact match}), or associated
   *          to a custom {@linkplain Matcher matcher} — for example,
   *          {@linkplain Matchers#closeTo(double, double) approximate
   *          match}:<pre class="lang-java"><code>
   * expected.match($ -&gt; isCloseTo($)) // where `expected` is Expected&lt;Double&gt;</code></pre>
   * @param generationSupplier
   *          Supplies the feed for expected results generation (see
   *          {@link #argumentsStream(ArgumentsStreamConfig, List, List[]) argumentsStream(..)}).
   *          <span class="important">IMPORTANT: In case of additional calls within the same
   *          parameterized test, they MUST comply with the following prescriptions</span>:
   *          <ul>
   *          <li><i>this argument MUST be set to {@code null}</i>, in order to suppress repeated
   *          expected results generation (otherwise, supplying multiple times the same feed will
   *          disrupt the generation)</li>
   *          <li><i>the additional calls MUST precede the main one</i>, as the latter manages the
   *          arguments generator (otherwise, failures may occur at the end of the generation as the
   *          generator is closed earlier)</li>
   *          </ul>
   * @apiNote For example, to test a method whose signature is
   *          {@code Strings.uncapitalizeGreedy(String value)}:<pre class="lang-java" data-line=
   *          "18-35"><code>
   * import static java.util.Arrays.asList;
   * import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
   * import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
   *
   * import java.util.List;
   * import java.util.stream.Stream;
   * import org.junit.jupiter.params.ParameterizedTest;
   * import org.junit.jupiter.params.provider.Arguments;
   * import org.junit.jupiter.params.provider.MethodSource;
   * import org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig;
   * import org.pdfclown.common.build.test.assertion.Assertions.Expected;
   * import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
   *
   * public class StringsTest {
   *   private static Stream&lt;Arguments&gt; uncapitalizeGreedy() {
   *     return argumentsStream(
   *         ArgumentsStreamConfig.cartesian(),
   *         <span style=
  "background-color:yellow;color:black;">// expected &lt;- THIS list is generated automatically (see argumentsStream(..))
   *         asList(
   *             // value[0]: 'Capitalized'
   *             "capitalized",
   *             // value[1]: 'uncapitalized'
   *             "uncapitalized",
   *             // value[2]: 'EOF'
   *             "eof",
   *             // value[3]: 'XObject'
   *             "xObject",
   *             // value[4]: 'IOException'
   *             "ioException",
   *             // value[5]: 'UTF8Test'
   *             "utf8Test",
   *             // value[6]: 'UTF8TEST'
   *             "utf8TEST",
   *             // value[7]: 'UNDERSCORE_TEST'
   *             "underscore_TEST")</span>,
   *         // value
   *         asList(
   *             "Capitalized",
   *             "uncapitalized",
   *             "EOF",
   *             "XObject",
   *             "IOException",
   *             "UTF8Test",
   *             "UTF8TEST",
   *             "UNDERSCORE_TEST"));
   *   }
   *
   *   &#64;ParameterizedTest
   *   &#64;MethodSource
   *   public void uncapitalizeGreedy(Expected&lt;String&gt; expected, String value) {
   *     assertParameterizedOf(
   *         () -&gt; Strings.uncapitalizeGreedy(value)),
   *         expected,
   *         () -&gt; new ExpectedGeneration(List.of(
   *             entry("value", value))));
   *   }
   * }</code></pre>
   */
  public static <T> void assertParameterized(@Nullable Object actual,
      @Nullable Expected<T> expected,
      @Nullable Supplier<? extends ExpectedGeneration> generationSupplier) {
    ExpectedGenerator generator = expectedGenerator.get();
    /*
     * Assertion enabled?
     *
     * NOTE: During expected results generation, parameterized assertions are skipped (otherwise,
     * they would fail fast since their state is incomplete).
     */
    if (generator == null) {
      assert expected != null;

      // Failed result?
      if (actual instanceof Failure) {
        if (!expected.isFailure())
          fail(String.format("Failure UNEXPECTED (expected: %s (%s); actual: %s)",
              toLiteralString(expected), sqnd(expected.getReturned()), actual));

        var thrownActual = (Failure) actual;
        var thrownExpected = expected.getThrown();
        assert thrownExpected != null;
        assertThat("Throwable.class.name", thrownActual.getName(), is(thrownExpected.getName()));
        /*
         * NOTE: DataFlowIssue is false positive: `actual` arg of `assertThat(..)` and `value` arg
         * of `is(..)` can be null, no NPE is possible.
         */
        //noinspection DataFlowIssue -- see NOTE above
        assertThat("Throwable.message", thrownActual.getMessage(), is(thrownExpected.getMessage()));
      }
      // Regular result.
      else {
        if (!expected.isSuccess())
          fail(String.format("Success UNEXPECTED (expected: %s)", expected));

        //noinspection unchecked
        assertThat((T) actual, expected.getMatcher());
      }
    }
    // Expected results generation.
    else {
      // Expected results generation suppressed?
      if (generationSupplier == null)
        return;

      var complete = true;
      try {
        var generation = generationSupplier.get();
        generator.generateExpected(actual, generation);
        complete = generator.isComplete();
      } finally {
        /*
         * Ensures the generator is properly discarded, either on normal completion or on
         * malfunction.
         */
        if (complete) {
          expectedGenerator.remove();
        }
      }
    }
  }

  /**
   * Asserts the evaluation of the actual expression corresponds to the expected value.
   *
   * @param actualExpression
   *          Expression to {@linkplain #evalParameterized(FailableSupplier) evaluate} for actual
   *          result.
   * @see #assertParameterized(Object, Expected, Supplier)
   */
  public static <T> void assertParameterizedOf(
      FailableSupplier<@Nullable Object, Exception> actualExpression,
      @Nullable Expected<T> expected,
      @Nullable Supplier<? extends ExpectedGeneration> generationSupplier) {
    assertParameterized(evalParameterized(actualExpression), expected, generationSupplier);
  }

  /**
   * Asserts that {@code expected} and {@code actual} paths are equal within a non-negative
   * {@code delta}.
   */
  public static void assertPathEquals(PathIterator expected, PathIterator actual, double delta) {
    class Segment {
      final int kind;
      final double[] coords;

      public Segment(int kind, double[] coords) {
        this.kind = kind;
        this.coords = coords.clone();
      }
    }

    var actualSegmentQueue = new SynchronousQueue<Segment>();
    var segmentIndex = new int[1];
    try {
      Executions.failFast(
          () -> PathEvaluator.eval(expected, ($segmentKind, $coords, $coordsCount) -> {
            var assertMessagePrefix = "Segment " + segmentIndex[0]++;
            try {
              var actualSegment = actualSegmentQueue.take();
              assertEquals($segmentKind, actualSegment.kind,
                  assertMessagePrefix + ", segmentKind");

              for (int i = 0; i < $coordsCount;) {
                var coordAssertMessagePrefix =
                    String.format("%s, point %s, ", assertMessagePrefix, i % 2);
                assertEquals($coords[i], actualSegment.coords[i++], delta,
                    coordAssertMessagePrefix + "x");
                assertEquals($coords[i], actualSegment.coords[i++], delta,
                    coordAssertMessagePrefix + "y");
              }
              return true;
            } catch (InterruptedException ex) {
              return false;
            }
          }),
          () -> PathEvaluator.eval(actual, ($segmentKind, $coords, $coordsCount) -> {
            try {
              actualSegmentQueue.put(new Segment($segmentKind, $coords));
              return true;
            } catch (InterruptedException ex) {
              return false;
            }
          }));
      assertTrue(expected.isDone());
      assertTrue(actual.isDone());
    } catch (ExecutionException ex) {
      if (ex.getCause() instanceof AssertionError) {
        fail(ex.getCause());
      } else
        throw runtime(ex.getCause());
    } catch (InterruptedException ex) {
      throw runtime(ex);
    }
  }

  /**
   * Asserts that {@code expected} and {@code actual} are equal within a non-negative {@code delta}.
   */
  public static void assertShapeEquals(Shape expected, Shape actual, double delta) {
    assertPathEquals(expected.getPathIterator(null), actual.getPathIterator(null), delta);
  }

  /**
   * Evaluates an expression.
   * <p>
   * Intended for use within {@linkplain ParameterizedTest parameterized tests}; its result is
   * expected to be checked via {@link #assertParameterized(Object, Expected, Supplier)
   * assertParameterized(..)}.
   * </p>
   *
   * @return
   *         <ul>
   *         <li>{@link Failure} — if {@code expression} failed throwing an exception (unchecked
   *         exceptions ({@link UncheckedIOException}, {@link UndeclaredThrowableException}) are
   *         unwrapped)</li>
   *         <li>regular result — if {@code expression} succeeded</li>
   *         </ul>
   */
  @SuppressWarnings("AssignmentToCatchBlockParameter")
  public static @Nullable Object evalParameterized(
      FailableSupplier<@Nullable Object, Exception> expression) {
    try {
      return expression.get();
    } catch (Throwable ex) {
      if (ex instanceof UncheckedIOException) {
        ex = ex.getCause();
      } else if (ex instanceof UndeclaredThrowableException) {
        ex = ((UndeclaredThrowableException) ex).getUndeclaredThrowable();
      }
      return new Failure(sqnd(ex), ex.getMessage());
    }
  }

  /**
   * Whether parameterized test's expected result's generation is underway.
   *
   * @see #argumentsStream(ArgumentsStreamConfig, List, List[])
   */
  public static boolean isExpectedGenerationMode() {
    return expectedGenerator.get() != null;
  }

  /**
   * Gets the absolute floating-point error tolerance with the minimum order of magnitude, enough to
   * pass the assertion.
   * <p>
   * Useful to quickly tune assertions in a robust manner, wherever
   * {@linkplain Double#compare(double, double) exact} floating-point comparison is unsuitable.
   * </p>
   *
   * @param assertWrap
   *          Lambda wrapping the assertion to probe.
   * @apiNote To use it, wrap your assertion inside {@code assertWrap}, wiring its argument as the
   *          delta of your assertion; for example, if the assertion
   *          is:<pre class="lang-java"><code>
   * assertEquals(myExpected, myActual, myDelta);</code></pre>
   *          <p>
   *          wrap it this way:
   *          </p>
   *          <pre class="lang-java"><code>
   * Assertions.probeDelta($ {@code ->} {
   *   assertEquals(myExpected, myActual, $);
   * });</code></pre>
   *          <p>
   *          and run your test; probeDelta will iterate until success, printing to stdout the
   *          resulting delta:
   *          </p>
   *          <pre>
   * probeDelta -- result: 1.0E-7</pre>
   */
  public static double probeDelta(DoubleConsumer assertWrap) {
    var delta = 1e-16;
    while (true) {
      try {
        assertWrap.accept(delta);

        System.out.println("probeDelta -- result: " + delta);

        return delta;
      } catch (AssertionError ex) {
        delta *= 10 /* Inflates delta to the next order of magnitude */;
      }
    }
  }

  private Assertions() {
  }
}
