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
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.pdfclown.common.build.internal.util.Aggregations.cartesianProduct;
import static org.pdfclown.common.build.internal.util.Objects.fqn;
import static org.pdfclown.common.build.internal.util.Objects.fqnd;
import static org.pdfclown.common.build.internal.util.Objects.objToLiteralString;
import static org.pdfclown.common.build.internal.util.Objects.requireState;
import static org.pdfclown.common.build.internal.util.Objects.sqnd;
import static org.pdfclown.common.build.internal.util.Strings.ELLIPSIS__CHICAGO;
import static org.pdfclown.common.build.internal.util.Strings.EMPTY;

import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.PathIterator;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableSupplier;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.pdfclown.common.build.internal.util.Objects;
import org.pdfclown.common.build.internal.util.io.XtPrintStream;
import org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig.Converter;
import org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig.Mode;
import org.pdfclown.common.build.util.lang.Runtimes;

/**
 * Assertion utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Assertions {
  /**
   * Argument value wrapper to use within {@link Arguments}.
   * <p>
   * Contrary to {@link Named}, its payload isn't unwrapped, so this argument goes to the
   * parameterized test as-is — useful for expected result generation (see
   * {@link Assertions#assertParameterized(Object, Expected, Supplier) assertParameterized(..)}).
   * </p>
   *
   * @param <T>
   *          Argument value type.
   * @author Stefano Chizzolini
   */
  public static class Argument<T> {
    public static <T> Argument<T> arg(String label, @Nullable T value) {
      return of(label, value);
    }

    public static <T> Argument<T> of(String label, @Nullable T value) {
      return new Argument<>(label, value);
    }

    private final String label;
    private final @Nullable T value;

    protected Argument(String label, @Nullable T value) {
      this.label = requireNonNull(label, "`label`");
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
      return label;
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
     * NOTE: Contrary to {@link org.junit.jupiter.params.converter.ArgumentConverter
     * ArgumentConverter}, this converter applies at an early stage of test invocation, so no
     * parameter context is available, just its index.
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
       * Maps the given argument value to the type of the corresponding parameter in the
       * parameterized test method.
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
     * Arguments combination mode.
     */
    public enum Mode {
      /**
       * Plain argument tuples.
       */
      SIMPLE,
      /**
       * Cartesian product.
       */
      CARTESIAN
    }

    public static ArgumentsStreamConfig cartesian() {
      return new ArgumentsStreamConfig(Mode.CARTESIAN);
    }

    public static ArgumentsStreamConfig simple() {
      return new ArgumentsStreamConfig(Mode.SIMPLE);
    }

    @Nullable
    Converter converter;

    final Mode mode;

    ArgumentsStreamConfig(Mode mode) {
      this.mode = mode;
    }

    /**
     * Prepends to {@link #getConverter() converter} the given function for the argument at
     * {@code argIndex} position in {@code args} argument.
     * <p>
     * For example, if the parameterized test looks like this:
     * </p>
     * <pre>
     * &#64;ParameterizedTest
     * &#64;MethodSource
     * public void %myTestName%(Object expected, %ArgType0% arg0, %ArgType0% arg1, . . ., %ArgTypeN% argN) {
     *   assertParameterizedOf(
     *       () -&gt; %myMethodToTest%(arg0, arg1, . . ., argN),
     *       expected,
     *       () -&gt; new ExpectedGeneration(List.of(
     *           entry("arg0", arg0),
     *           entry("arg1", arg1),
     *           . . .
     *           entry("argN", argN))));
     * }</pre>
     * <p>
     * then {@code argIndex} = 0 will define the converter to {@code %ArgType0%} of the input values
     * of parameterized test argument {@code arg0}.
     * </p>
     *
     * @see #composeExpectedConverter(Function)
     */
    public ArgumentsStreamConfig composeArgConverter(
        int argIndex, Function<@Nullable Object, @Nullable Object> before) {
      requireNonNull(before);
      var paramIndex = argIndex + 1 /* Offsets `expected` parameter */;
      return composeConverter(
          ($index, $source) -> $index == paramIndex ? before.apply($source) : $source);
    }

    /**
     * Prepends to {@link #getConverter() converter} the given function.
     */
    public ArgumentsStreamConfig composeConverter(Converter before) {
      converter = converter != null ? converter.compose(before) : before;
      return this;
    }

    /**
     * Prepends to {@link #getConverter() converter} the given function for {@code expected}
     * argument.
     *
     * @see #composeArgConverter(int, Function)
     */
    public ArgumentsStreamConfig composeExpectedConverter(
        Function<@Nullable Object, @Nullable Object> before) {
      requireNonNull(before);
      return composeConverter(($index, $source) -> $index == 0 ? before.apply($source) : $source);
    }

    /**
     * Arguments converter.
     * <p>
     * Transforms the values of {@code expected} and {@code args} arguments of
     * {@link #argumentsStream(ArgumentsStreamConfig, List, List[]) argumentsStream(..)} before they
     * are streamed (useful, e.g., to wrap a parameterized test argument as {@linkplain Named
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
    public static <T> Expected<T> failure(ThrownExpected thrown) {
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
    Supplier<Matcher<T>> matcherSupplier;
    @Nullable
    final T returned;
    @Nullable
    final ThrownExpected thrown;

    private Expected(@Nullable T returned, @Nullable ThrownExpected thrown) {
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
    public @Nullable ThrownExpected getThrown() {
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
     * Sets the custom matcher to validate {@link #getReturned() returned}.
     */
    public Expected<T> match(Supplier<Matcher<T>> matcherSupplier) {
      this.matcherSupplier = matcherSupplier;
      return this;
    }

    @Override
    public String toString() {
      return java.util.Objects.toString(returned != null ? returned : thrown);
    }

    Matcher<T> getMatcher() {
      return matcherSupplier != null && returned != null ? matcherSupplier.get() : is(returned);
    }
  }

  /**
   * Generation feed for the expected results of a parameterized test.
   *
   * @author Stefano Chizzolini
   * @see Assertions#assertParameterized(Object, Expected, Supplier)
   */
  public static class ExpectedGeneration {
    String argCommentAbbreviationMarker = ELLIPSIS__CHICAGO;
    Function<@Nullable Object, String> argCommentFormatter = Objects::objToLiteralString;
    final List<Entry<String, @Nullable Object>> args;
    Function<@Nullable Object, String> expectedSourceCodeGenerator = Objects::objToLiteralString;
    int maxArgCommentLength = 20;
    PrintStream out = System.err;
    boolean outOverridable = true;

    /**
     */
    public ExpectedGeneration(List<Entry<String, @Nullable Object>> args) {
      this.args = requireNonNull(args);
    }

    /**
     * Prepends to {@link #getExpectedSourceCodeGenerator() expectedSourceCodeGenerator} the given
     * function.
     */
    public ExpectedGeneration composeExpectedSourceCodeGenerator(
        Function<@Nullable Object, String> before) {
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
    public Function<@Nullable Object, String> getExpectedSourceCodeGenerator() {
      return expectedSourceCodeGenerator;
    }

    /**
     * Maximum length of argument values in comments accompanying expected results source code.
     * <p>
     * DEFAULT: {@code 20}
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
        Function<@Nullable Object, String> value) {
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
   * Expected thrown exception.
   *
   * @author Stefano Chizzolini
   * @see #assertParameterized(Object, Expected, Supplier)
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

    @Override
    public String toString() {
      return sqnd(name) + " (" + abbreviate(message, 30) + ")";
    }
  }

  /**
   * Source code generator of the expected results of a parameterized test fed by
   * {@link #argumentsStream(ArgumentsStreamConfig, List, List[]) argumentsStream(..)} in
   * {@link ArgumentsStreamConfig.Mode#CARTESIAN CARTESIAN} mode.
   * <p>
   * The generated source code looks like this:
   * </p>
   * <pre>
   * // expected
   * java.util.Arrays.asList(
   *   // from[0]
   *   "",
   *   "../another/sub/to.html",
   *   // from[1]
   *   "to.html",
   *   "another/sub/to.html",
   *   . . .
   *   // from[5]
   *   "other/to.html",
   *   "/sub/to.html"),</pre>
   *
   * @author Stefano Chizzolini
   */
  private static class CartesianExpectedGenerator extends ExpectedGenerator {
    private static int count(List<List<?>> args) {
      return args.stream().mapToInt(List::size).reduce(1, Math::multiplyExact);
    }

    /**
     * Argument moduli.
     */
    private final int[] mods;

    /**
     * @param args
     *          Arguments lists.
     */
    CartesianExpectedGenerator(List<List<?>> args) {
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

  /**
   * Source code generator of the expected results of a parameterized test.
   * <p>
   * The generated source code looks like this:
   * </p>
   * <pre>
   * // expected
   * java.util.Arrays.asList(
   *   // %expectedComment[0]%
   *   %expected[0]%,
   *   // %expectedComment[1]%
   *   %expected[1]%,
   *   . . .
   *   // %expectedComment[n]%
   *   %expected[n]%),</pre>
   *
   * @author Stefano Chizzolini
   */
  private abstract static class ExpectedGenerator {
    protected static final String INDENT = SPACE + SPACE;

    /**
     * Arguments tuples count.
     */
    private final int count;
    /**
     * Current arguments tuple index.
     */
    private int index = -1;
    /**
     * Target stream (either <b>internal</b> (for output redirection to clipboard, in case of debug
     * mode) or <b>{@linkplain #generateExpected(Object, ExpectedGeneration) provided}</b>) the
     * generated representation is written to.
     */
    private @Nullable PrintStream out;
    /**
     * Whether {@link #out} lifecycle is internal responsibility of this class.
     */
    private boolean outManaged;

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
     * <li>{@link Throwable} (failed result): to {@link ThrownExpected}</li>
     * <li>any other type (regular result): via {@code expectedSourceCodeGenerator}, if present;
     * otherwise, to literal</li>
     * </ul>
     *
     * @param expected
     *          Expected result.
     * @param generation
     *          Generation feed for the expected result of the parameterized test.
     * @implNote {@link ThrownExpected} replaces the actual {@link Throwable} type in order to
     *           simplify automated instantiation.
     */
    public <T> void generateExpected(@Nullable T expected, ExpectedGeneration generation) {
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

    protected <T> void generateExpectedSourceCode(@Nullable T expected,
        ExpectedGeneration generation) {
      String expectedSourceCode;
      if (expected instanceof Throwable) {
        var ex = (Throwable) expected;
        expectedSourceCode = String.format("new %s(\"%s\", \"%s\")",
            fqnd(ThrownExpected.class), fqn(ex), ex.getMessage());
      } else {
        expectedSourceCode = generation.expectedSourceCodeGenerator.apply(expected);
      }
      out().append(INDENT).append(expectedSourceCode);
    }

    protected PrintStream out() {
      assert out != null;
      return out;
    }

    private void begin(ExpectedGeneration generation) {
      // Output redirect to clipboard?
      if (generation.outOverridable && Runtimes.isDebugging()
          && !GraphicsEnvironment.isHeadless()) {
        out = new XtPrintStream(new ByteArrayOutputStream(), true, UTF_8);
        outManaged = true;
      }
      // No output redirect.
      else {
        out = generation.out;
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

      if (outManaged) {
        // Transfer output to clipboard!
        String data = ((XtPrintStream) out).toDataString();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(data), null);

        System.err.printf(
            "[%s] Expected results source code GENERATED, and COPIED to clipboard.\n",
            sqnd(this));

        out.close();
      }
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
  }

  /**
   * Source code generator of the expected results of a parameterized test fed by
   * {@link #argumentsStream(ArgumentsStreamConfig, List, List[]) argumentsStream(..)} in
   * {@link ArgumentsStreamConfig.Mode#SIMPLE SIMPLE} mode.
   * <p>
   * The generated source code looks like this:
   * </p>
   * <pre>
   * // expected
   * java.util.Arrays.asList(
   *   // from[0]
   *   "",
   *   // from[1]
   *   "to.html",
   *   . . .
   *   // from[5]
   *   "other/to.html"),</pre>
   *
   * @author Stefano Chizzolini
   */
  private static class SimpleExpectedGenerator extends ExpectedGenerator {
    private static int count(List<List<?>> args) {
      int cardinality = 0;
      for (var it = args.listIterator(); it.hasNext();) {
        int argSize = it.next().size();
        if (cardinality == 0) {
          cardinality = argSize;
        } else {
          if (argSize != cardinality)
            throw new IllegalArgumentException(String.format(
                "args[%s].size (%s): INVALID (should be %s)", it.nextIndex() - 1, argSize,
                cardinality));
        }
      }
      return cardinality;
    }

    /**
     * @param args
     *          Arguments lists.
     */
    SimpleExpectedGenerator(List<List<?>> args) {
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

  private static final ThreadLocal<@Nullable ExpectedGenerator> expectedGenerator =
      new ThreadLocal<>();

  /**
   * Combines the given argument lists into a stream of corresponding {@linkplain Arguments
   * parametric tuples} to feed a {@linkplain ParameterizedTest parameterized test}.
   * <p>
   * The {@code expected} parameter shall contain both regular results and failures (i.e., thrown
   * exception) of the method tested with the given {@code args}; failures will be represented as
   * {@link ThrownExpected} for automatic handling.
   * </p>
   * <p>
   * The corresponding parameterized test shall follow this pattern:
   * </p>
   * <pre>
   * &#64;ParameterizedTest
   * &#64;MethodSource
   * public void %myTestName%(Object expected, %ArgType0% arg0, %ArgType0% arg1, . . ., %ArgTypeN% argN) {
   *   assertParameterizedOf(
   *       () -&gt; %myMethodToTest%(arg0, arg1, . . ., argN),
   *       expected,
   *       () -&gt; new ExpectedGeneration(List.of(
   *           entry("arg0", arg0),
   *           entry("arg1", arg1),
   *           . . .
   *           entry("argN", argN))));
   * }</pre>
   * <p>
   * NOTE: In the parameterized test method signature, <i>{@code expected} parameter should be
   * declared as {@code Object}</i> (that's required whenever a failure (i.e., thrown exception) is
   * among the expected results). In any case, it MUST NOT be declared as primitive type (e.g.,
   * {@code boolean}), since it is passed {@code null} during expected results generation (see
   * dedicated section here below).
   * </p>
   * <p>
   * See {@link #assertParameterized(Object, Expected, Supplier)} for more information and a full
   * example.
   * </p>
   * <h2>Expected Results Generation</h2>
   * <p>
   * The source code representation of {@code expected} is automatically generated as described in
   * this section — this simplifies the preparation and maintenance of test cases, also unifying the
   * way failed results (i.e., thrown exceptions) are handled along with regular results.
   * </p>
   * <p>
   * Here, the steps to generate {@code expected} (based on the example in
   * {@link #assertParameterized(Object, Expected, Supplier)}):
   * </p>
   * <ol>
   * <li>pass {@code null} as {@code expected} argument to this method:<pre>
   * private static Stream&lt;Arguments&gt; uncapitalizeGreedy() {
   *     return argumentsStream(
   *       // expected
   *       <span style="background-color:yellow;color:black;">null</span>,
   *       // value
   *       asList(
   *           "Capitalized",
   *           "uncapitalized",
   *           . . .
   *           "UNDERSCORE_TEST"));
   * }</pre></li>
   * <li>in your IDE, <i>run the test in debug mode</i> — this will cause the generated source code
   * to be copied directly into your clipboard.
   * <p>
   * If run in normal mode, the generated source code will be output to
   * {@link ExpectedGeneration#out}, as specified in the
   * {@link #assertParameterized(Object, Expected, Supplier)} call (by default,
   * {@linkplain System#err stderr}):
   * </p>
   * <pre>
   * &#64;ParameterizedTest
   * &#64;MethodSource
   * public void uncapitalizeGreedy(Object expected, String value) {
   *   assertParameterizedOf(
   *       () -&gt; Strings.uncapitalizeGreedy(value),
   *       expected,
   *       () -&gt; new ExpectedGeneration(List.of(
   *           entry("value", value)))); // <span style=
  "background-color:yellow;color:black;">&lt;- No `out` stream specified here, so it defaults to stderr</span>
   * }</pre></li>
   * <li>replace {@code null} (step 1) with the generated source code as {@code expected}
   * argument:<pre>
   * private static Stream&lt;Arguments&gt; uncapitalizeGreedy() {
   *     return argumentsStream(
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
   * }</pre></li>
   * </ol>
   * <h2>Arguments Conversion</h2>
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
   *          test</b>; its size MUST equal that of each {@code args} list.
   * @param args
   *          Test arguments. Corresponds to the <b>domain of the respective test argument</b>; each
   *          list MUST have the same size.
   * @return A stream of tuples, each composed this way:
   *         <ul>
   *         <li>simple stream ({@code config.mode} == {@link Mode#SIMPLE}):<pre>
  * (expected[y], args[0][x], args[1][x], .&nbsp;.&nbsp;., args[n][x])</pre>
   *         <p>
   *         where
   *         </p>
   *         <ul>
   *         <li><code>expected[y] = f(args[0][x], args[1][x], .&nbsp;.&nbsp;., args[n][x])</code></li>
   *         <li><code>n = args.size() - 1</code></li>
   *         <li><code>m = args[0].size() = args[1].size() = .&nbsp;.&nbsp;. = args[n].size()</code></li>
   *         <li><code>y ∈ N : 0 &lt;= y &lt;= m</code></li>
   *         <li><code>x ∈ N : 0 &lt;= x &lt; m</code></li>
   *         </ul>
   *         </li>
   *         <li>Cartesian product stream ({@code config.mode} == {@link Mode#CARTESIAN}):<pre>
    * (expected[y], args[0][x0], args[1][x1], .&nbsp;.&nbsp;., args[n][xn])</pre>
   *         <p>
   *         where
   *         </p>
   *         <ul>
   *         <li><code>expected[y] = f(args[0][x0], args[1][x1], .&nbsp;.&nbsp;., args[n][xn])</code></li>
   *         <li><code>n = args.size() - 1</code></li>
   *         <li><code>y ∈ N : 0 &lt;= y &lt; args[0].size() * args[1].size() * .&nbsp;.&nbsp;. * args[n].size()</code></li>
   *         <li><code>m ∈ N : 0 &lt;= m &lt;= n</code></li>
   *         <li><code>xm ∈ N : 0 &lt;= xm &lt; args[m].size()</code></li>
   *         </ul>
   *         </li>
   *         </ul>
   */
  public static Stream<Arguments> argumentsStream(ArgumentsStreamConfig config,
      @Nullable List<?> expected, List<?>... args) {
    ToIntFunction<List<List<?>>> expectedCounter;
    Function<List<List<?>>, ExpectedGenerator> expectedGeneratorProvider;
    BiFunction<List<?>, List<List<?>>, Stream<Arguments>> argumentsStreamer;
    switch (config.mode) {
      case SIMPLE:
        expectedCounter = SimpleExpectedGenerator::count;
        expectedGeneratorProvider = SimpleExpectedGenerator::new;
        argumentsStreamer = ($expected, $args) -> IntStream.range(0, $expected.size())
            .mapToObj($ -> {
              int i;
              var testArgs = new Object[1 + $args.size()];
              testArgs[i = 0] = $expected.get($);
              while (++i < testArgs.length) {
                testArgs[i] = $args.get(i - 1).get($);
              }
              return Arguments.of(testArgs);
            });
        break;
      case CARTESIAN:
        expectedCounter = CartesianExpectedGenerator::count;
        expectedGeneratorProvider = CartesianExpectedGenerator::new;
        argumentsStreamer = ($expected, $args) -> {
          var indexRef = new AtomicInteger();
          return cartesianProduct($args)
              .map($ -> {
                $.add(0, $expected.get(indexRef.getAndIncrement()));
                return Arguments.of($.toArray());
              });
        };
        break;
      default:
        throw new AssertionError("Unexpected value: " + config.mode);
    }

    // Arguments transformation:
    // 1. Expected results (tested method output).
    int paramIndex = 0;
    if (expected != null) {
      var targetList = new ArrayList<>(expected.size());
      for (var e : expected) {
        targetList.add(e instanceof ThrownExpected
            ? Expected.failure((ThrownExpected) e)
            : Expected.success(config.converter != null
                ? config.converter.apply(paramIndex, e)
                : e));
      }
      expected = unmodifiableList(targetList);
    }
    // 2. Arguments values (tested method inputs).
    List<List<?>> argsList;
    if (config.converter != null) {
      argsList = new ArrayList<>(args.length);
      for (int i = 0; i < args.length; i++) {
        var sourceList = args[i];
        var argList = new ArrayList<>(sourceList.size());
        paramIndex++;
        for (var e : sourceList) {
          argList.add(config.converter.apply(paramIndex, e));
        }
        argsList.add(unmodifiableList(argList));
      }
      argsList = unmodifiableList(argsList);
    } else {
      argsList = asList(args);
    }

    int expectedCount = expectedCounter.applyAsInt(argsList);

    // Expected results generation mode?
    if (expected == null) {
      expected = Collections.nCopies(expectedCount, null);
      expectedGenerator.set(expectedGeneratorProvider.apply(argsList));
    }
    if (expected.size() != expectedCount)
      throw new IllegalArgumentException(String.format(
          "`expected` size (%s): MISMATCH with `args` (SHOULD be %s)", expected.size(),
          expectedCount));

    // Build the argument sets!
    return argumentsStreamer.apply(expected, argsList);
  }

  /**
   * Maps the given items to the corresponding matchers.
   */
  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static <E, T extends Matcher<? super E>> List<Matcher<? super E>> asMatchers(
      Function<E, T> mapper, E... elements) {
    var ret = new ArrayList<T>();
    for (var element : elements) {
      ret.add(mapper.apply(element));
    }
    /*
     * NOTE: Cast is necessary because, due to type erasure, we are forced to declare
     * `List<Matcher<? super E>>` as return type instead of List<T>, since the latter would be
     * statically resolved as List<Object>, causing the linker to match wrong overloads (eg, see
     * org.hamcrest.Matchers.arrayContaining(..)).
     */
    return (List<Matcher<? super E>>) ret;
  }

  /**
   * Asserts that {@code expected} and {@code actual} are equal applying the given comparator.
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
   * regular results and failures (i.e., {@link Throwable}) in a unified and consistent manner.
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
   *          to a custom {@linkplain Matcher matcher} (e.g.,
   *          {@linkplain Matchers#closeTo(double, double) approximate match}):<pre>
   * expected.match(() -> isCloseTo(expected.getReturned())) // where `expected` is Expected&lt;Double></pre>
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
   *          {@code Strings.uncapitalizeGreedy(String value)}:<pre>
   * import static java.util.Arrays.asList;
   * import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
   * import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterized;
   * import static org.pdfclown.common.build.test.assertion.Assertions.evalParameterized;
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
   *         // expected <span style=
  "background-color:yellow;color:black;">&lt;- THIS list is generated automatically (see argumentsStream(..))</span>
   *         java.util.Arrays.asList(
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
   *             "underscore_TEST"),
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
   *   public void uncapitalizeGreedy(Expected&lt;String> expected, String value) {
   *     assertParameterizedOf(
   *         () -&gt; Strings.uncapitalizeGreedy(value)),
   *         expected,
   *         () -&gt; new ExpectedGeneration(List.of(
   *             entry("value", value))));
   *   }
   * }</pre>
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
      if (actual instanceof Throwable) {
        var thrownActual = (Throwable) actual;
        if (!expected.isFailure())
          fail(String.format("Failure UNEXPECTED (expected: %s (%s))", objToLiteralString(expected),
              sqnd(expected.getReturned())), thrownActual);

        var thrownExpected = expected.getThrown();
        assertThat("Throwable.class.name", thrownActual.getClass().getName(),
            is(thrownExpected.getName()));
        assertThat("Throwable.message", thrownActual.getMessage(),
            is(thrownExpected.getMessage()));
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
  public static <T> void assertParameterizedOf(FailableSupplier<Object, Exception> actualExpression,
      @Nullable Expected<T> expected,
      @Nullable Supplier<? extends ExpectedGeneration> generationSupplier) {
    assertParameterized(evalParameterized(actualExpression), expected, generationSupplier);
  }

  /**
   * Asserts that {@code expected} and {@code actual} paths are equal within the given non-negative
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
        throw new RuntimeException(ex.getCause());
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Asserts that {@code expected} and {@code actual} are equal within the given non-negative
   * {@code delta}.
   */
  public static void assertShapeEquals(Shape expected, Shape actual, double delta) {
    assertPathEquals(expected.getPathIterator(null), actual.getPathIterator(null), delta);
  }

  /**
   * Evaluates the given expression.
   * <p>
   * Intended for use within {@linkplain ParameterizedTest parameterized tests}; its result is
   * expected to be checked via {@link #assertParameterized(Object, Expected, Supplier)}.
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
   * Whether parameterized test's expected result's generation is underway.
   *
   * @see #argumentsStream(ArgumentsStreamConfig, List, List[])
   */
  public static boolean isExpectedGenerationMode() {
    return expectedGenerator.get() != null;
  }

  /**
   * Gets the absolute floating-point error tolerance with the minimum order of magnitude, enough to
   * pass the given assertion.
   * <p>
   * Useful to quickly tune assertions in a robust manner, wherever
   * {@linkplain Double#compare(double, double) exact} floating-point comparison is unsuitable.
   * </p>
   *
   * @param assertWrap
   *          Lambda wrapping the assertion to probe.
   * @apiNote To use it, wrap your assertion inside {@code assertWrap}, wiring its argument as the
   *          delta of your assertion; eg, if the assertion is:<pre>
   * assertEquals(myExpected, myActual, myDelta);</pre>
   *          <p>
   *          wrap it this way:
   *          </p>
   *          <pre>
   * Assertions.probeDelta($ {@code ->} {
   *   assertEquals(myExpected, myActual, $);
   * });</pre>
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
