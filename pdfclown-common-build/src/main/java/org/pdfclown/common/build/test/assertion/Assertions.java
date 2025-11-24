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
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static java.util.Arrays.asList;
import static java.util.Collections.binarySearch;
import static java.util.Collections.unmodifiableList;
import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.apache.commons.lang3.StringUtils.stripToEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.pdfclown.common.build.internal.util_.Aggregations.cartesianProduct;
import static org.pdfclown.common.build.internal.util_.Chars.COMMA;
import static org.pdfclown.common.build.internal.util_.Chars.DOT;
import static org.pdfclown.common.build.internal.util_.Chars.HYPHEN;
import static org.pdfclown.common.build.internal.util_.Chars.LF;
import static org.pdfclown.common.build.internal.util_.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.build.internal.util_.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.build.internal.util_.Chars.SLASH;
import static org.pdfclown.common.build.internal.util_.Chars.SPACE;
import static org.pdfclown.common.build.internal.util_.Conditions.requireEqual;
import static org.pdfclown.common.build.internal.util_.Conditions.requireNonNullElseThrow;
import static org.pdfclown.common.build.internal.util_.Conditions.requireNotBlank;
import static org.pdfclown.common.build.internal.util_.Conditions.requireState;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Exceptions.unexpected;
import static org.pdfclown.common.build.internal.util_.Objects.INDEX__NOT_FOUND;
import static org.pdfclown.common.build.internal.util_.Objects.basicLiteral;
import static org.pdfclown.common.build.internal.util_.Objects.found;
import static org.pdfclown.common.build.internal.util_.Objects.fqnd;
import static org.pdfclown.common.build.internal.util_.Objects.literal;
import static org.pdfclown.common.build.internal.util_.Objects.objTo;
import static org.pdfclown.common.build.internal.util_.Objects.objToElseGet;
import static org.pdfclown.common.build.internal.util_.Objects.sqnd;
import static org.pdfclown.common.build.internal.util_.Objects.textLiteral;
import static org.pdfclown.common.build.internal.util_.Strings.ELLIPSIS;
import static org.pdfclown.common.build.internal.util_.Strings.ELLIPSIS__CHICAGO;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;
import static org.pdfclown.common.build.internal.util_.Strings.NULL;
import static org.pdfclown.common.build.internal.util_.Strings.S;
import static org.pdfclown.common.build.internal.util_.io.Files.FILE_EXTENSION__JAVA;
import static org.pdfclown.common.build.internal.util_.reflect.Reflects.methodFqn;
import static org.pdfclown.common.build.internal.util_.system.Systems.getBooleanProperty;
import static org.pdfclown.common.build.test.Tests.testFrame;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.function.Failable;
import org.apache.commons.lang3.function.FailableSupplier;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.pdfclown.common.build.internal.util.lang.Javas;
import org.pdfclown.common.build.internal.util_.Exceptions;
import org.pdfclown.common.build.internal.util_.Objects;
import org.pdfclown.common.build.internal.util_.annot.Immutable;
import org.pdfclown.common.build.internal.util_.annot.LazyNonNull;
import org.pdfclown.common.build.internal.util_.annot.Unmodifiable;
import org.pdfclown.common.build.internal.util_.io.IndentPrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common assertion utilities.
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
      return label.isEmpty() ? textLiteral(value)
          : "%s (%s)".formatted(textLiteral(value), label);
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
   * {@linkplain #argumentsStream(ArgumentsStreamStrategy, List, List[]) Arguments stream} strategy.
   *
   * @author Stefano Chizzolini
   */
  public abstract static class ArgumentsStreamStrategy {
    /**
     * Argument converter.
     * <p>
     * NOTE: Contrary to the {@linkplain org.junit.jupiter.params.converter.ArgumentConverter JUnit
     * converter}, this one applies at an early stage of test invocation, so no parameter context is
     * available, just its index.
     * </p>
     *
     * @author Stefano Chizzolini
     * @see #argumentsStream(ArgumentsStreamStrategy, List, List[])
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
     * {@linkplain #argumentsStream(ArgumentsStreamStrategy, List, List[]) Arguments stream}
     * strategy for Cartesian-product argument tuples.
     *
     * @author Stefano Chizzolini
     */
    static class Cartesian extends ArgumentsStreamStrategy {
      /**
       * {@link Cartesian} source code generator of the expected results of a parameterized test fed
       * by {@link #argumentsStream(ArgumentsStreamStrategy, List, List[]) argumentsStream(..)}.
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

        Generator(int count, List<List<?>> args) {
          super(count);

          mods = new int[args.size()];
          {
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
              if (i == 0 && generation.args.length > 1 && getIndex() > 0) {
                out().println("//");
              }

              // Level title.
              var indexLabel = i == last ? "[" + (getIndex() + 1) + "] " : EMPTY;
              var argIndent = max(0, 2 * i - indexLabel.length());
              out().printf("// %s%s%s%s[%s]: %s\n",
                  indexLabel,
                  repeat(HYPHEN, argIndent),
                  argIndent == 0 ? EMPTY : SPACE,
                  getParamName(i),
                  (i == 0 ? getIndex() : getIndex() % mods[i - 1]) / mods[i],
                  formatArgComment(generation.args[i], generation));
            }
          }
        }
      }

      @Override
      protected List<List<?>> convertArgs(List<List<?>> args) {
        if (converter != null) {
          var index = new AtomicInteger(1);
          return args.stream()
              .map($ -> {
                var ret = $.stream()
                    .map($$ -> converter.apply(index.get(), $$))
                    .collect(Collectors.toList());
                index.incrementAndGet();
                return ret;
              })
              .collect(Collectors.toList());
        } else
          return args;
      }

      @Override
      protected int getExpectedCount(List<List<?>> args) {
        return args.stream().mapToInt(List::size).reduce(1, Math::multiplyExact);
      }

      @Override
      protected ExpectedGenerator getExpectedGenerator(List<List<?>> args) {
        return new Generator(getExpectedCount(args), args);
      }

      @Override
      protected Stream<Arguments> streamArguments(List<?> expected, List<List<?>> args) {
        var index = new AtomicInteger();
        return cartesianProduct(args)
            .map($ -> {
              $.add(0, expected.get(index.getAndIncrement()));
              return Arguments.of($.toArray());
            });
      }
    }

    /**
     * {@linkplain #argumentsStream(ArgumentsStreamStrategy, List, List[]) Arguments stream}
     * strategy for plain argument tuples.
     *
     * @author Stefano Chizzolini
     */
    static class Simple extends ArgumentsStreamStrategy {
      /**
       * {@link Simple} source code generator of the expected results of a parameterized test fed by
       * {@link #argumentsStream(ArgumentsStreamStrategy, List, List[]) argumentsStream(..)}.
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
        Generator(int count) {
          super(count);
        }

        @Override
        protected void generateExpectedComment(ExpectedGeneration generation) {
          out().printf("// [%s] ", getIndex() + 1);
          for (int i = 0; i < generation.args.length; i++) {
            if (i > 0) {
              out().append("; ");
            }

            out().printf("%s[%s]: %s", getParamName(i), getIndex(),
                formatArgComment(generation.args[i], generation));
          }
          out().append("\n");
        }
      }

      @Override
      protected List<List<?>> convertArgs(List<List<?>> args) {
        if (converter != null)
          return args.stream()
              .map($ -> {
                var ret = new ArrayList<>($.size());
                for (var i = 0; i < $.size(); i++) {
                  ret.add(converter.apply(i + 1, $.get(i)));
                }
                return ret;
              })
              .collect(Collectors.toList());
        else
          return args;
      }

      @Override
      protected int getExpectedCount(List<List<?>> args) {
        return args.size();
      }

      @Override
      protected ExpectedGenerator getExpectedGenerator(List<List<?>> args) {
        return new Generator(getExpectedCount(args));
      }

      @Override
      protected Stream<Arguments> streamArguments(List<?> expected, List<List<?>> args) {
        return IntStream.range(0, expected.size())
            .mapToObj($ -> {
              var args_ = args.get($);
              var arguments = new Object[1 + args_.size()];
              int i;
              arguments[i = 0] = expected.get($);
              while (++i < arguments.length) {
                arguments[i] = args_.get(i - 1);
              }
              return Arguments.of(arguments);
            });
      }
    }

    private static final Map<String,
        UnaryOperator<String>> THROWN_MESSAGE_NORMALIZERS__BUILTIN = Map.of(
            /*
             * NOTE: Since Java 14 (see <https://openjdk.java.net/jeps/358>), NPE's message has
             * changed (it describes which variable is null, whilst previously it didn't provide
             * such information). To avoid false negatives switching between pre- and post-Java 14
             * JDKs, all messages are suppressed.
             */
            sqnd(NullPointerException.class), $ -> EMPTY);

    /**
     * Creates a strategy for Cartesian-product
     * {@linkplain #argumentsStream(ArgumentsStreamStrategy, List, List[]) arguments stream}.
     * <p>
     * Each list in {@code args} represents the test cases for the parameter at the corresponding
     * position.
     * </p>
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
    public static ArgumentsStreamStrategy cartesian() {
      return new Cartesian();
    }

    /**
     * Creates a strategy for plain
     * {@linkplain #argumentsStream(ArgumentsStreamStrategy, List, List[]) arguments stream}.
     * <p>
     * Each list in {@code args} represents a test case.
     * </p>
     * <p>
     * {@code expected} and {@code args} MUST have the same size.
     * </p>
     * <p>
     * The resulting argument tuples will be composed this way:
     * </p>
     * <pre>
    * (expected[i], args[i][0], args[i][1], .&nbsp;.&nbsp;., args[i][n])</pre>
     * <p>
     * where
     * </p>
     * <pre>
     * expected[i] = f(args[i][0], args[i][1], .&nbsp;.&nbsp;., args[i][n])
     * i ∈ N : 0 &lt;= i &lt; m
     * m = expected.size() = args.size()
     * n = args[0].size() - 1
     * </pre>
     */
    public static ArgumentsStreamStrategy simple() {
      return new Simple();
    }

    @Nullable
    Converter converter;
    Map<String, UnaryOperator<String>> thrownMessageNormalizers =
        THROWN_MESSAGE_NORMALIZERS__BUILTIN;

    protected ArgumentsStreamStrategy() {
    }

    /**
     * Adds the message normalizer for a throwable type.
     * <p>
     * Useful to extract from the message of specific throwable types the contents which are
     * invariant across JDKs and third-party components. For example, since Java 14
     * {@link NullPointerException}'s message <a href="https://openjdk.java.net/jeps/358">has
     * changed</a> (it describes which variable is null, whilst previously it didn't provide such
     * information); to avoid false negatives switching between pre- and post-Java 14 JDKs, all
     * messages are suppressed by a built-in normalizer.
     * </p>
     */
    public ArgumentsStreamStrategy addThrownMessageNormalizer(Class<? extends Throwable> type,
        UnaryOperator<String> normalizer) {
      if (thrownMessageNormalizers == THROWN_MESSAGE_NORMALIZERS__BUILTIN) {
        thrownMessageNormalizers = new HashMap<>(THROWN_MESSAGE_NORMALIZERS__BUILTIN);
      }
      thrownMessageNormalizers.put(sqnd(type), normalizer);
      return this;
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
     *       () -&gt; new ExpectedGeneration(arg0, arg1, . . ., argN));
     * }</code></pre>
     * <p>
     * then {@code argIndex} = 0 will define the converter to {@code ArgType0} of the input values
     * of parameterized test argument {@code arg0} which corresponds to {@code args[0]} of
     * {@link #argumentsStream(ArgumentsStreamStrategy, List, List[]) argumentsStream(..)}.
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
    public <T> ArgumentsStreamStrategy composeArgConverter(
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
    public ArgumentsStreamStrategy composeConverter(Converter before) {
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
    public <T> ArgumentsStreamStrategy composeExpectedConverter(
        Function<@Nullable T, @Nullable Object> before) {
      requireNonNull(before);
      return composeConverter(
          ($index, $source) -> $index == 0 ? before.apply((T) $source) : $source);
    }

    /**
     * Arguments converter.
     * <p>
     * Transforms the values of {@code expected} and {@code args} arguments of
     * {@link #argumentsStream(ArgumentsStreamStrategy, List, List[]) argumentsStream(..)} before
     * they are streamed (useful, for example, to wrap a parameterized test argument as
     * {@linkplain Named named}).
     * </p>
     * <p>
     * DEFAULT: {@code null} (arguments passed as-is — for efficiency, this should be used instead
     * of identity transformation).
     * </p>
     * <p>
     * For more information, see "Arguments Conversion" section in
     * {@link #argumentsStream(ArgumentsStreamStrategy, List, List[]) argumentsStream(..)}.
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
    public ArgumentsStreamStrategy setConverter(@Nullable Converter value) {
      converter = value;
      return this;
    }

    protected abstract List<List<?>> convertArgs(List<List<?>> args);

    protected abstract int getExpectedCount(List<List<?>> args);

    protected abstract ExpectedGenerator getExpectedGenerator(List<List<?>> args);

    protected abstract Stream<Arguments> streamArguments(List<?> expected, List<List<?>> args);
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
      return failure(thrown, null);
    }

    /**
     * New failed result.
     *
     * @param thrown
     *          Thrown exception.
     * @param thrownMessageNormalizer
     *          Maps the thrown message to its normal form. Useful to prevent variations across JDKs
     *          and third-party components from disrupting test assertions against expected messages
     *          (see also
     *          {@link ArgumentsStreamStrategy#addThrownMessageNormalizer(Class, UnaryOperator)}).
     */
    public static <T> Expected<T> failure(Failure thrown,
        @Nullable UnaryOperator<String> thrownMessageNormalizer) {
      var ret = new Expected<T>(null, requireNonNull(thrown));
      ret.thrownMessageNormalizer = requireNonNullElse(thrownMessageNormalizer,
          UnaryOperator.identity());
      return ret;
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
    @Nullable
    UnaryOperator<String> thrownMessageNormalizer;

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
      return basicLiteral(returned != null ? returned : thrown);
    }

    Matcher<? super T> getMatcher() {
      return matcherProvider != null && returned != null
          ? matcherProvider.apply(returned)
          : is(returned);
    }

    String normalizeThrownMessage(Failure failure) {
      assert thrownMessageNormalizer != null;

      return thrownMessageNormalizer.apply(failure.getMessage());
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
      return "new %s(%s)".formatted(fqnd(type),
          Arrays.stream(args).map(Objects::literal).collect(joining(",")));
    }

    /**
     * Gets factory source code to use in {@link #setExpectedSourceCodeGenerator(Function)}.
     *
     * @see #expectedSourceCodeForConstructor(Class, Object...)
     */
    public static String expectedSourceCodeForFactory(Class<?> type, String methodName,
        @Nullable Object... args) {
      return "%s.%s(%s)".formatted(fqnd(type), methodName,
          Arrays.stream(args).map(Objects::literal).collect(joining(",")));
    }

    String argCommentAbbreviationMarker = ELLIPSIS__CHICAGO;
    Function<@Nullable Object, String> argCommentFormatter = Objects::literal;
    final @Nullable Object[] args;
    @Nullable
    TestEnvironment environment;
    Function<Object, String> expectedSourceCodeGenerator = Objects::literal;
    int maxArgCommentLength = MAX_ARG_COMMENT_LENGTH__DEFAULT;
    @Nullable
    Appendable out;
    String @Nullable [] paramNames;

    /**
     */
    public ExpectedGeneration(@Nullable Object... args) {
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
     * Arguments consumed by the current test invocation.
     */
    public @Nullable Object[] getArgs() {
      return args;
    }

    /**
     * Test environment.
     * <p>
     * Useful in case the tested project is not organized according to <a href=
     * "https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html">Maven's
     * Standard Directory Layout</a>
     * </p>
     */
    public @Nullable TestEnvironment getEnvironment() {
      return environment;
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
     * Stream where the generated expected results' source code will be output.
     * <p>
     * If undefined, the source code file of the test is automatically updated.
     * </p>
     */
    public @Nullable Appendable getOut() {
      return out;
    }

    /**
     * Parameter names corresponding to the {@linkplain #getArgs() arguments}.
     * <p>
     * Normally there is no need to specify them, as they are automatically retrieved during
     * expected results generation; sometimes, however, they could be inaccessible (for example,
     * while the filesystem is mocked).
     * </p>
     * <p>
     * <span class="important">IMPORTANT: NEVER use this collection during expected results
     * generation; use {@link ExpectedGenerator#getParamName(int)} instead.</span>
     * </p>
     */
    public String @Nullable [] getParamNames() {
      return paramNames;
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
     * Sets {@link #getEnvironment() environment}.
     */
    public ExpectedGeneration setEnvironment(@Nullable TestEnvironment value) {
      environment = value;
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
    public ExpectedGeneration setOut(Appendable value) {
      out = requireNonNull(value);
      return this;
    }

    /**
     * Sets {@link #getParamNames() paramNames}.
     */
    public ExpectedGeneration setParamNames(String @Nullable... value) {
      paramNames = value;
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
   * @see Assertions#assertParameterized(Object, Expected, Supplier)
   */
  public abstract static class ExpectedGenerator {
    private static final String METHOD_NAME__AS_LIST = "asList";

    private @Nullable MethodCallExpr argumentsStreamCall;
    /**
     * Arguments tuples count.
     */
    private final int count;
    private @Nullable CompilationUnitEditor editor;
    /**
     * Internal buffer for updating the test source code file.
     *
     * @see #out
     */
    private @Nullable StringWriter editorBuffer;
    /**
     * Current arguments tuple index.
     */
    private int index = -1;
    /**
     * Target stream (either {@linkplain #editorBuffer internal} or
     * {@linkplain ExpectedGeneration#getOut() external}) the generated expected results are written
     * to.
     */
    private @Nullable IndentPrintWriter out;
    private String @Nullable [] paramNames;
    private @Nullable String testMethodFqn;

    protected ExpectedGenerator(int count) {
      this.count = count;
    }

    /**
     * Generates the source code representation of the expected result for
     * {@linkplain #argumentsStream(ArgumentsStreamStrategy, List, List[]) parameterized test
     * feeding}.
     * <p>
     * The expected result is mapped to source code representation based on its value type:
     * </p>
     * <ul>
     * <li>failed result (thrown {@link Throwable}) — to {@link Failure}</li>
     * <li>regular result — via
     * {@code generation.}{@link ExpectedGeneration#getExpectedSourceCodeGenerator()
     * expectedSourceCodeGenerator}</li>
     * <li>{@code null} — to literal
     * ({@value org.pdfclown.common.build.internal.util_.Strings#NULL})</li>
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

    /**
     * Whether all the argument iterations have already been processed.
     */
    public boolean isComplete() {
      return out == null && index >= 0;
    }

    protected String formatArgComment(@Nullable Object arg, ExpectedGeneration generation) {
      String comment;
      var ret = abbreviate(
          comment = generation.argCommentFormatter.apply(arg).lines().findFirst().orElse(EMPTY),
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

    protected void generateExpectedSourceCode(@Nullable Object expected,
        ExpectedGeneration generation) {
      // Generate the source code corresponding to `expected`!
      String expectedSourceCode;
      if (expected == null) {
        expectedSourceCode = NULL;
      } else if (expected instanceof Failure failure) {
        var failureRef = fqnd(Failure.class);
        if (editor != null) {
          if (editor.tryImport(failureRef, false)) {
            failureRef = Failure.class.getSimpleName();
          }
        }
        expectedSourceCode = "new %s(%s, %s)".formatted(failureRef, literal(failure.getName()),
            literal(failure.getMessage()));
      } else {
        expectedSourceCode = generation.expectedSourceCodeGenerator.apply(expected);
      }

      // Check the generated source code is valid!
      try {
        Expression expression = Javas.PARSER.parseExpression(expectedSourceCode);
        if (expression instanceof LiteralStringValueExpr) {
          // Split multiline string literal at newlines to improve readability!
          expectedSourceCode = expectedSourceCode.replaceAll(
              "\\\\n(?!\"\\z)"/*
                               * NOTE: `(?!\"\\z)` expression ensures trailing newline doesn't split
                               * to empty tail
                               */,
              "\\\\n\"\n+\"");
        }
      } catch (RuntimeException ex) {
        throw runtime("Generated expected source code INVALID", ex);
      }

      // Add the generated source code to output!
      out().append(expectedSourceCode);
    }

    /**
     * Gets the parameter name at the given position.
     */
    protected String getParamName(int index) {
      assert paramNames != null;

      return paramNames[index];
    }

    /**
     * Output stream for generated expected results.
     */
    protected IndentPrintWriter out() {
      assert out != null;

      return out;
    }

    private void begin(ExpectedGeneration generation) {
      if (generation.paramNames != null) {
        paramNames = generation.paramNames;
      }

      out = IndentPrintWriter.of(generation.out != null
          // Output redirection to arbitrary stream.
          ? generation.out
          // Output to update test unit's source code file.
          : (editorBuffer = new StringWriter()), null);

      var testFrame = testFrame().orElseThrow();
      testMethodFqn = methodFqn(testFrame);

      printInfo("Expected results source code generation underway for `" + testMethodFqn
          + "()`" + ELLIPSIS);

      if (editorBuffer != null /* Output to update the source code file */
          || paramNames == null /* Output redirection requiring test metadata retrieval */) {
        try {
          editor = getCompilationUnitEditor(testFrame.getDeclaringClass(), generation.environment);
        } catch (IOException ex) {
          throw runtime("Compilation unit `{}` loading FAILED (TIP: {})", testFrame.getClassName(),
              editorBuffer != null
                  ? """
                      if filesystem is mocked, specify `ExpectedGeneration.out` and \
                      `ExpectedGeneration.paramNames` to avoid source code access; \
                      otherwise, if your project doesn't adhere to Maven's standard directory \
                      layout, provide a `TestEnvironment` to resolve project files accordingly"""
                  : "specify `ExpectedGeneration.paramNames` to avoid source code access",
              ex);
        }
        for (MethodDeclaration method : editor.source.getPrimaryType()
            .orElseThrow().getMethodsByName(testFrame.getMethodName())) {
          if (method.getParameters().isEmpty()) {
            MethodDeclaration argumentsSourceMethod = editor.source.getPrimaryType()
                .orElseThrow().getMethodsBySignature(testFrame.getMethodName()).get(0);
            argumentsStreamCall = argumentsSourceMethod.getBody().orElseThrow()
                .findFirst(MethodCallExpr.class, $ -> $.getNameAsString().equals("argumentsStream"))
                .orElseThrow();
          } else if (method.isAnnotationPresent(ParameterizedTest.class)) {
            paramNames = method.getParameters().stream()
                .skip(1) // Skips `expected`
                .map(Parameter::getNameAsString)
                .toArray(String[]::new);
          }
        }
        requireNonNullElseThrow(paramNames,
            () -> runtime("{} for {}() NOT FOUND", ParameterizedTest.class, testMethodFqn));
        requireNonNullElseThrow(argumentsStreamCall,
            () -> runtime("{}() NOT FOUND", testMethodFqn));

        if (editorBuffer == null) {
          /*
           * NOTE: In case of output redirection the editor must be suppressed, as it is only used
           * temporarily for test metadata retrieval via compilation unit inspection.
           */
          editor = null;
        }
      }
    }

    private void beginExpected(ExpectedGeneration generation) {
      if (++index == 0) {
        begin(generation);

        var asListMethodRef = "java.util.Arrays" + DOT + METHOD_NAME__AS_LIST;
        if (editor != null && editor.tryImport(asListMethodRef, true)) {
          asListMethodRef = METHOD_NAME__AS_LIST;
        }
        out()
            .append("," + LF)
            .setLevel(argumentsStreamCall != null
                ? (argumentsStreamCall.getArguments().get(0).getBegin().orElseThrow().column - 1)
                    / out().getIndent().getWidth()
                : 0)
            .append("// expected" + LF)
            .append(asListMethodRef).append("(" + LF)
            .indent();
      }
    }

    private void end() {
      assert out != null;

      String target;
      if (editor != null) {
        assert editorBuffer != null;
        assert argumentsStreamCall != null;

        target = textLiteral(editor.file);

        // Replace the old expected results' expression with the generated one!
        editor.replace(
            argumentsStreamCall.getArguments().get(0).getEnd().orElseThrow().right(1),
            argumentsStreamCall.getArguments().get(1).getEnd().orElseThrow().right(1),
            editorBuffer.toString());

        out.close();
      } else {
        target = "stream";
      }

      printInfo("Expected results source code GENERATED for `%s()` to %s"
          .formatted(testMethodFqn, target));

      out = null;
    }

    private void endExpected() {
      if (index == getCount() - 1) {
        out().print(S + ROUND_BRACKET_CLOSE);

        end();
      } else {
        out().println(S + COMMA);
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
    private final String message;
    private final String name;

    /**
     * @param message
     *          NOTE: Normalized converting {@code null} (which is a valid
     *          {@link Throwable#getMessage()}) to empty.
     */
    public Failure(String name, @Nullable String message) {
      this.name = requireNonNull(name, "`name`");
      this.message = stripToEmpty(message);
    }

    /**
     * Message of the thrown exception.
     */
    public String getMessage() {
      return message;
    }

    /**
     * Simple type name of the thrown exception.
     */
    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return name + SPACE + ROUND_BRACKET_OPEN + message + ROUND_BRACKET_CLOSE;
    }
  }

  /**
   * Compilation unit editor.
   * <p>
   * Because of technical limitations in the parsed {@linkplain #source compilation unit}, it is
   * used as a read-only model (source), whilst the modifications are applied to a
   * {@linkplain #targetBuilder buffer} (target).
   * </p>
   *
   * @implNote Despite javaparser's rich API, its awkwardness in dealing with comments and
   *           replacement positioning severely impairs source code editing, forcing us to bake this
   *           custom editor for easy and precise source code replacement, leveraging javaparser for
   *           model reading only.
   *           <p>
   *           Here are javaparser's editing issues preventing it from being used (let us know if
   *           you can solve them):
   *           </p>
   *           <ul>
   *           <li>out-of-sequence {@linkplain Node#getOrphanComments() orphan comments} make
   *           painful if not practically impossible to place multiple line comments before a
   *           newly-inserted expression, like this: <pre class="lang-java" data-line="3-4"><code>
  // expected
  asList(
  <span style="background-color:yellow;color:black;">// from[0]: "my/sub/same.html"
  // [1] to[0]: "my/sub/same.html"</span>
  "",
  // [2] to[1]: "my/another/sub/to.html"
  "../another/sub/to.html",
  . . .
  //
  // from[1]: "my/another/sub/from.html"
  // [11] to[0]: "my/sub/same.html"
  "../../sub/same.html",
  . . .</code></pre></li>
   *           <li>on argument addition, non-orphan comments are stuck to the line preceding their
   *           owner node's insertion point, thus disrupting their formatting, as trailing comments
   *           are left in place by formatters — for example:
   *           <pre class="lang-java" data-line="2-5"><code>
  // expected
  asList(<span style="background-color:yellow;color:black;">// [1] unit[0]: a (Are)</span>
  100.0, <span style="background-color:yellow;color:black;">// [2] unit[1]: ac (Acre)</span>
  4046.8564224, <span style=
  "background-color:yellow;color:black;">// [3] unit[2]: cm (Centimetre)</span>
  0.01, <span style=
  "background-color:yellow;color:black;">// [4] unit[3]: cm² (Square centimetre)</span>
  . . .</code></pre></li>
   *           <li>on argument replacement of an expression preceded by a comment, the insertion
   *           point of the new comment+expression is at the old expression's position rather than
   *           the old comment's, leaving a blank line — for example, if the original code is:
   *           <pre class="lang-java" data-line="3-4"><code>
  return argumentsStream(
  cartesian(),
  <span style="background-color:yellow;color:black;">    // expected
  asList(</span>
  . . .</code></pre>
   *           <p>
   *           then its replacement is:
   *           </p>
   *           <pre class="lang-java" data-line="3-5"><code>
  return argumentsStream(
  cartesian(),
  <span style="background-color:yellow;color:black;">

  // expected
  asList(</span>
  . . .</code></pre></li>
   *           </ul>
   *           <p>
   *           NOTE: javaparser editing here assumes {@linkplain LexicalPreservingPrinter preserving
   *           existing formatting}.
   *           </p>
   * @author Stefano Chizzolini
   */
  private static class CompilationUnitEditor {
    /**
     * Compilation unit.
     */
    final CompilationUnit source;
    /**
     * {@linkplain #source Compilation unit} file.
     */
    final Path file;

    private boolean changed;
    /**
     * Modified {@linkplain #source compilation unit} content.
     */
    private final StringBuilder targetBuilder;
    private final Set<String> targetImports = new HashSet<>();
    /**
     * Sequence of line differences between {@linkplain #targetBuilder target} and
     * {@linkplain #source source}, keyed by source line.
     */
    private final List<Entry<Integer, Integer>> targetLineOffsets = new ArrayList<>();

    CompilationUnitEditor(Path file) throws IOException {
      this.file = file;

      source = Javas.PARSER.parse(file);
      targetBuilder = new StringBuilder(readString(file));
    }

    /**
     * Inserts content at the given position.
     *
     * @param position
     *          {@link #source} position where to insert the content.
     * @param newContent
     *          Content to insert.
     */
    public void insert(Position position, String newContent) {
      int targetPosition = indexOf(position);

      targetBuilder.insert(targetPosition, newContent);

      onChange(position, EMPTY, newContent);
    }

    /**
     * Whether the {@linkplain #source compilation unit} has changed.
     */
    public boolean isChanged() {
      return changed;
    }

    /**
     * Replaces content within the given range.
     *
     * @param start
     *          Starting {@link #source} position to replace.
     * @param end
     *          Ending {@link #source} position to replace.
     * @param newContent
     *          Replacement content.
     */
    public void replace(Position start, Position end, String newContent) {
      int targetStart = indexOf(start);
      int targetEnd = indexOf(end);

      String oldContent = targetBuilder.substring(targetStart, targetEnd);

      targetBuilder.replace(targetStart, targetEnd, newContent);

      onChange(start, oldContent, newContent);
    }

    /**
     * Tries to declare the import of an element.
     *
     * @param name
     *          Fully-qualified name to import.
     * @param static_
     *          Whether the import must be static (in case {@code name} is a method, rather than a
     *          type).
     * @return {@code true}, if {@code name} is imported; otherwise, a collision with existing
     *         imports occurred.
     */
    public boolean tryImport(String name, boolean static_) {
      if (targetImports.contains(name))
        return true;

      var simpleName = name.substring(name.lastIndexOf(DOT) + 1);
      boolean found = false;
      for (var import_ : source.getImports()) {
        if (import_.getNameAsString().equals(name)) {
          found = true;
          break;
        } else if (import_.getName().getIdentifier().equals(simpleName))
          return false;
      }
      if (!found) {
        Position start = requireNonNull(objToElseGet(
            source.getImports().getFirst().orElse(null),
            $ -> $.getBegin().orElseThrow(),
            () -> source.getPackageDeclaration().orElseThrow().getEnd().orElseThrow().right(1)),
            "Import statements location NOT FOUND");
        insert(start, "import %s%s;\n".formatted(static_ ? "static" + SPACE : EMPTY, name));
      }
      targetImports.add(name);
      return true;
    }

    /**
     * Gets the target index corresponding to a source position.
     *
     * @param position
     *          {@link #source} position.
     */
    private int indexOf(Position position) {
      // Convert source line to target!
      int targetLine = position.line;
      for (var lineOffset : targetLineOffsets) {
        if (lineOffset.getKey().compareTo(position.line) < 0) {
          targetLine += lineOffset.getValue();
        } else {
          break;
        }
      }

      int index = 0;
      int line = 1;
      while (line < targetLine) {
        index = targetBuilder.indexOf(S + LF, index);
        if (!found(index))
          return INDEX__NOT_FOUND;

        line++;
        index++;
      }
      return index + position.column - 1;
    }

    private void onChange(Position start, String oldContent, String newContent) {
      changed = true;

      @SuppressWarnings("unchecked")
      int targetLineOffsetsIndex = binarySearch(targetLineOffsets, start.line,
          Comparator.comparingInt(
              $ -> $ instanceof Entry ? ((Entry<Integer, Integer>) $).getKey() : (Integer) $));
      if (found(targetLineOffsetsIndex))
        throw unexpected(targetLineOffsetsIndex,
            "Target line offset already exists (replacing already changed content NOT ALLOWED)");

      int sourceLineCount = countMatches(oldContent, LF);
      int targetLineCount = countMatches(newContent, LF);

      targetLineOffsetsIndex = -(targetLineOffsetsIndex + 1);
      targetLineOffsets.add(targetLineOffsetsIndex,
          entry(start.line, targetLineCount - sourceLineCount));
    }
  }

  private static @LazyNonNull @Nullable Map<Path, CompilationUnitEditor> compilationUnitEditors;

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

  private static final FileTreeAsserter ASSERTER__FILE_TREE = new FileTreeAsserter();
  private static final TextAsserter ASSERTER__TEXT = new TextAsserter();

  private static final Logger log = LoggerFactory.getLogger(Assertions.class);

  /**
   * CLI parameter specifying whether
   * {@linkplain #argumentsStream(ArgumentsStreamStrategy, List, List[]) expected results
   * generation} is enabled for executed {@linkplain ParameterizedTest parameterized tests}.
   * <p>
   * <b>Expected results</b> represent the state against which the corresponding actual state
   * generated by the tested project code is
   * {@linkplain #assertParameterized(Object, Expected, Supplier) validated}. If the expected
   * results are undefined or their validation is false negative because the tested project code
   * innovated the expected state, the {@linkplain #assertParameterized(Object, Expected, Supplier)
   * validator} can regenerate them through this CLI parameter.
   * </p>
   * <p>
   * The value of this CLI parameter is a boolean which can be omitted (default: {@code true}).
   * </p>
   *
   * @apiNote Common usage examples (Maven build system):
   *          <ul>
   *          <li>to regenerate all the results, no matter the tests they belong to:
   *          <pre class="lang-shell"><code>
   * mvn test ... -Dassert.params.update</code></pre></li>
   *          <li>to regenerate the results belonging to specific test classes (for example,
   *          {@code MyObjectTest}): <pre class="lang-shell"><code>
   * mvn test ... -Dassert.params.update -Dtest=MyObjectTest</code></pre></li>
   *          <li>to regenerate the results belonging to specific test cases (for example,
   *          {@code MyObjectTest.myTest}): <pre class="lang-shell"><code>
   * mvn test ... -Dassert.params.update -Dtest=MyObjectTest#myTest</code></pre></li>
   *          <li>to regenerate the results belonging to multiple test classes (for example,
   *          {@code MyObjectTest} and {@code MyOtherObjectTest}), they can be specified as a
   *          comma-separated list: <pre class="lang-shell"><code>
   * mvn test ... -Dassert.params.update -Dtest=MyObjectTest,MyOtherObjectTest</code></pre></li>
   *          </ul>
   *          <p>
   *          NOTE: {@code test} CLI parameter is typically mapped by Maven plugins (such as
   *          <a href="https://maven.apache.org/surefire/maven-failsafe-plugin/">Failsafe</a>) to
   *          the corresponding JUnit system property which allows fine-grained test selection (see
   *          the relevant documentation); if your build system doesn't support it, adjust your
   *          commands accordingly. Furthermore, if the names of your test cases are overridden
   *          (that is, their names are different from the corresponding test methods), it's up to
   *          you to use their actual names, as they are internally resolved by JUnit.
   *          </p>
   */
  public static final String PARAM_NAME__PARAMS_UPDATE = "assert.params.update";
  static {
    log.info("`{}` CLI parameter: {}", PARAM_NAME__PARAMS_UPDATE,
        getBooleanProperty(PARAM_NAME__PARAMS_UPDATE));
  }

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
   *       () -&gt; new ExpectedGeneration(arg0, arg1, . . ., argN));
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
   * There are two ways to trigger the automatic source code generation (code snippets herein are
   * based on the example in {@link #assertParameterized(Object, Expected, Supplier)
   * assertParameterized(..)}):
   * </p>
   * <ul>
   * <li>either execute the test passing {@code null} as {@code expected} argument to this method —
   * for example:<pre class="lang-java" data-line="6"><code>
   * class StringsTest {
   *   private static Stream&lt;Arguments&gt; uncapitalizeGreedy() {
   *     return argumentsStream(
   *         ArgumentsStreamStrategy.cartesian(),
   *         // expected
   *         <span style="background-color:yellow;color:black;">null</span>,
   *         // value
   *         asList(
   *             "Capitalized",
   *             "uncapitalized",
   *             . . .
   *             "UNDERSCORE_TEST"));
   *   }
   * }</code></pre></li>
   * <li>or launch the test specifying the {@value #PARAM_NAME__PARAMS_UPDATE} CLI parameter — for
   * example (Maven build system):<pre class="lang-shell"><code>
  mvn test ... -Dassert.params.update -Dtest=StringsTest#uncapitalizeGreedy</code></pre>
   * <p>
   * NOTE: The same parameter can obviously be passed to a run configuration within an IDE.
   * </p>
   * </li>
   * </ul>
   * <p>
   * The ensuing test execution will automatically update the source code of {@code expected}
   * argument with the generated expected results:
   * </p>
   * <pre class="lang-java" data-line="5-12"><code>
   * private static Stream&lt;Arguments&gt; uncapitalizeGreedy() {
   *   return argumentsStream(
   *       ArgumentsStreamStrategy.cartesian(),
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
   * }</code></pre>
   * <p>
   * To regenerate all the results, no matter the tests they belong to:
   * </p>
   * <pre class="lang-shell"><code>
  mvn test ... -Dassert.params.update</code></pre>
   * <p>
   * See {@value #PARAM_NAME__PARAMS_UPDATE} CLI parameter for further information.
   * </p>
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
   * {@linkplain ArgumentsStreamStrategy#setConverter(ArgumentsStreamStrategy.Converter) pass the
   * converter} to {@code strategy} parameter.
   * </p>
   *
   * @param expected
   *          Expected test results, or {@code null} to automatically generate them (see "Expected
   *          Results Generation" section here above). Corresponds to the <b>codomain of the
   *          test</b>.
   * @param args
   *          Test arguments. Corresponds to the <b>domain of the respective test argument</b>.
   * @return A stream of tuples, composed according to the algorithm provided by {@code strategy}.
   */
  public static Stream<Arguments> argumentsStream(ArgumentsStreamStrategy strategy,
      @Nullable List<?> expected, List<?>... args) {
    if (getBooleanProperty(PARAM_NAME__PARAMS_UPDATE)) {
      // Force generation mode!
      expected = null;
    }

    // Prepare the argument lists!
    List<List<?>> argsList = strategy.convertArgs(asList(args));
    List<Expected<?>> expectedList;
    {
      int expectedCount = strategy.getExpectedCount(argsList);

      // Expected results.
      if (expected != null) {
        requireEqual(expected.size(), expectedCount, "expected.size");

        expectedList = new ArrayList<>(expected.size());
        for (var e : expected) {
          expectedList.add(e instanceof Failure failure
              ? Expected.failure(failure,
                  strategy.thrownMessageNormalizers.get(failure.getName()))
              : Expected.success(strategy.converter != null
                  ? strategy.converter.apply(0, e)
                  : e));
        }
        expectedList = unmodifiableList(expectedList);
      }
      // Expected results generation mode.
      else {
        expectedList = Collections.nCopies(expectedCount, EXPECTED__VOID);
        expectedGenerator.set(strategy.getExpectedGenerator(argsList));
      }
    }

    // Combine the argument lists into the arguments stream!
    return strategy.streamArguments(expectedList, argsList);
  }

  /**
   * Asserts that a file tree matches the expected one.
   *
   * @param expectedDirResourceName
   *          Resource name of the expected file tree.
   * @param actualDir
   *          Actual file tree.
   * @param test
   *          Current test unit.
   * @throws AssertionError
   *           if {@code actualDir} doesn't match the one at {@code expectedDirResourceName}.
   * @see Asserter#PARAM_NAME__UPDATE
   */
  public static void assertFileTreeEquals(String expectedDirResourceName, Path actualDir,
      Test test) {
    ASSERTER__FILE_TREE.assertEquals(expectedDirResourceName, actualDir, new Asserter.Config(test));
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
   * See {@link #argumentsStream(ArgumentsStreamStrategy, List, List[]) argumentsStream(..)} for
   * more information about parameterized test definition.
   * </p>
   *
   * @param actual
   *          Result of the method tested via {@link #evalParameterized(FailableSupplier)}.
   * @param expected
   *          Expected result, provided by
   *          {@link #argumentsStream(ArgumentsStreamStrategy, List, List[]) argumentsStream(..)};
   *          it can be passed as-is (for {@linkplain Matchers#is(Object) exact match}), or
   *          associated to a custom {@linkplain Matcher matcher} — for example,
   *          {@linkplain Matchers#closeTo(double, double) approximate
   *          match}:<pre class="lang-java"><code>
   * expected.match($ -&gt; isCloseTo($)) // where `expected` is Expected&lt;Double&gt;</code></pre>
   * @param generationSupplier
   *          Supplies the feed for expected results generation (see
   *          {@link #argumentsStream(ArgumentsStreamStrategy, List, List[]) argumentsStream(..)}).
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
   * import org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy;
   * import org.pdfclown.common.build.test.assertion.Assertions.Expected;
   * import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
   *
   * public class StringsTest {
   *   private static Stream&lt;Arguments&gt; uncapitalizeGreedy() {
   *     return argumentsStream(
   *         ArgumentsStreamStrategy.cartesian(),
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
   *         () -&gt; new ExpectedGeneration(value));
   *   }
   * }</code></pre>
   */
  public static <T> void assertParameterized(@Nullable Object actual,
      Expected<T> expected, @Nullable Supplier<? extends ExpectedGeneration> generationSupplier) {
    ExpectedGenerator generator = expectedGenerator.get();
    /*
     * Assertion enabled?
     *
     * NOTE: During expected results generation, parameterized assertions are skipped (otherwise,
     * they would fail fast since their state is incomplete).
     */
    if (generator == null) {
      // Failed result?
      if (actual instanceof Failure actualFailure) {
        if (!expected.isFailure())
          fail("Failure UNEXPECTED (expected: %s (%s); actual: %s)".formatted(
              textLiteral(expected), sqnd(expected.getReturned()), textLiteral(actual)));

        var expectedFailure = expected.getThrown();
        assert expectedFailure != null;

        assertThat("Throwable.class.name", actualFailure.getName(), is(expectedFailure.getName()));
        assertThat("Throwable.message", expected.normalizeThrownMessage(actualFailure),
            is(expected.normalizeThrownMessage(expectedFailure)));
      }
      // Regular result.
      else {
        if (!expected.isSuccess())
          fail("Success UNEXPECTED (expected: %s)".formatted(expected));

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
        if (complete) {
          expectedGenerator.remove() /*
                                      * Ensures the generator is properly discarded, either on
                                      * normal completion or on malfunction
                                      */;
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
      Expected<T> expected, @Nullable Supplier<? extends ExpectedGeneration> generationSupplier) {
    assertParameterized(evalParameterized(actualExpression), expected, generationSupplier);
  }

  /**
   * Asserts that a file matches the expected one.
   *
   * @param expectedResourceName
   *          Resource name of the expected file.
   * @param actualFile
   *          Actual file.
   * @param test
   *          Current test unit.
   * @throws AssertionError
   *           if {@code actualFile} doesn't match the content of {@code expectedResourceName}.
   * @see Asserter#PARAM_NAME__UPDATE
   */
  public static void assertTextEquals(String expectedResourceName, Path actualFile, Test test) {
    ASSERTER__TEXT.assertEquals(expectedResourceName, actualFile, new Asserter.Config(test));
  }

  /**
   * Asserts that a content matches the expected one.
   *
   * @param expectedResourceName
   *          Resource name of the expected content.
   * @param actualContent
   *          Actual content.
   * @param test
   *          Current test unit.
   * @throws AssertionError
   *           if {@code actualContent} doesn't match the content of {@code expectedResourceName}.
   * @see Asserter#PARAM_NAME__UPDATE
   */
  public static void assertTextEquals(String expectedResourceName, String actualContent,
      Test test) {
    ASSERTER__TEXT.assertEquals(expectedResourceName, actualContent, new Asserter.Config(test));
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
  public static @Nullable Object evalParameterized(
      FailableSupplier<@Nullable Object, Exception> expression) {
    try {
      return expression.get();
    } catch (Throwable ex) {
      return objTo(Exceptions.actual(ex), $ -> new Failure(sqnd($), $.getMessage()));
    }
  }

  /**
   * Whether parameterized test's expected result's generation is underway.
   *
   * @see #argumentsStream(ArgumentsStreamStrategy, List, List[])
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

  /**
   * Gets the editor for the compilation unit corresponding to a type.
   *
   * @param environment
   *          Test environment, used to resolve the source code file corresponding to {@code type}
   *          (if undefined, the file is assumed to be inside a project organized according to
   *          <a href=
   *          "https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html">Maven's
   *          Standard Directory Layout</a>).
   */
  private static synchronized CompilationUnitEditor getCompilationUnitEditor(Class<?> type,
      @Nullable TestEnvironment environment) throws IOException {
    if (compilationUnitEditors == null) {
      compilationUnitEditors = new HashMap<>();

      // Schedule modified compilation units saving on session end!
      Runtime.getRuntime().addShutdownHook(
          new Thread(() -> compilationUnitEditors.values().stream()
              .filter(CompilationUnitEditor::isChanged)
              .forEach(Failable.asConsumer($ -> writeString($.file, $.targetBuilder)))));
    }

    var compilationUnitFile = environment != null
        ? environment.typeSrcPath(type)
        : Path.of(EMPTY).toAbsolutePath()
            .resolve("src/test/java/" + type.getName().replace(DOT, SLASH) + FILE_EXTENSION__JAVA);
    try {
      return compilationUnitEditors.computeIfAbsent(compilationUnitFile,
          Failable.asFunction(CompilationUnitEditor::new));
    } catch (UncheckedIOException ex) {
      throw ex.getCause();
    }
  }

  private Assertions() {
  }
}
