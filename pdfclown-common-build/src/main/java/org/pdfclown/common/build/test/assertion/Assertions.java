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

import static java.nio.charset.StandardCharsets.UTF_8;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.lang3.function.FailableSupplier;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.pdfclown.common.build.util.lang.Runtimes;

/**
 * Assertion utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Assertions {
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
      return String.format("%s (%s)", value, description);
    }
  }

  /**
   * Generation feed for the expected results of a parameterized test.
   *
   * @see Assertions#assertParameterized(Object, Object, Supplier)
   */
  public static class ExpectedGeneration {
    final List<Map.Entry<String, Object>> args;
    final @Nullable Function<Object, String> expectedSourceCodeGenerator;
    final PrintStream out;

    public ExpectedGeneration(List<Map.Entry<String, Object>> args) {
      this(args, null);
    }

    public ExpectedGeneration(List<Map.Entry<String, Object>> args,
        @Nullable Function<Object, String> expectedSourceCodeGenerator) {
      this(args, expectedSourceCodeGenerator, System.err);
    }

    /**
    */
    public ExpectedGeneration(List<Map.Entry<String, Object>> args,
        @Nullable Function<Object, String> expectedSourceCodeGenerator, PrintStream out) {
      this.args = args;
      this.expectedSourceCodeGenerator = expectedSourceCodeGenerator;
      this.out = out;
    }
  }

  /**
   * Source code generator of the expected results of a parameterized test fed by
   * {@link #cartesianArgumentsStream (List, List...) cartesianArgumentsStream(..)}.
   * <p>
   * The generated source code looks like this:
   * </p>
   * <pre>
   * // expected
   * java.util.Arrays.asList(
   *   // from[0]
   *   URI.create(""),
   *   URI.create("../another/sub/to.html"),
   *   // from[1]
   *   URI.create("to.html"),
   *   URI.create("another/sub/to.html"),
   *   . . .
   *   // from[5]
   *   URI.create("other/to.html"),
   *   URI.create("/sub/to.html")),</pre>
   *
   * @author Stefano Chizzolini
   */
  public static class ExpectedGenerator {
    private static final String INDENT = SPACE + SPACE;

    private final int count;
    /**
     * Next expected result index.
     */
    private int index;
    /**
     * Argument moduli.
     */
    private final int[] mods;

    private @Nullable PrintStream out;
    private @Nullable ByteArrayOutputStream outStream;

    /**
     * @param args
     *          Arguments lists.
     */
    ExpectedGenerator(List<List<?>> args) {
      count = cartesianProductCount(args);
      mods = new int[args.size()];
      {
        var count = this.count;
        final var counts = new int[args.size()];
        for (int i = 0; i < counts.length; i++) {
          counts[i] = args.get(i).size();
          mods[i] = (count /= counts[i]);
        }
      }
    }

    /**
     * Generates the source code representation of the expected result for
     * {@linkplain #cartesianArgumentsStream (List, List...) parameterized test feeding}.
     * <p>
     * The expected result is mapped to source code representation based on its value type:
     * </p>
     * <ul>
     * <li>{@link Throwable} (failed result): to {@link ThrownExpected}</li>
     * <li>any other type (regular result): via {@code expectedSourceCodeGenerator}, if present;
     * otherwise, to literal</li>
     * </ul>
     *
     * @param out
     *          Target stream the generated representation is written to.
     * @param expected
     *          Expected result.
     * @param expectedSourceCodeGenerator
     *          Generates the source code representation of regular {@code expected}.
     * @param args
     *          Argument values.
     * @implNote {@link ThrownExpected} replaces the actual {@link Throwable} type in order to
     *           simplify automated instantiation.
     */
    public <T> void generateExpected(PrintStream out, @Nullable T expected,
        @Nullable Function<@Nullable T, String> expectedSourceCodeGenerator,
        List<Map.Entry<String, Object>> args) {
      String expectedSourceCode;
      if (expected instanceof Throwable) {
        var ex = (Throwable) expected;
        expectedSourceCode = String.format("new %s(\"%s\", \"%s\")", fqnd(ThrownExpected.class),
            fqn(ex), ex.getMessage());
      } else if (expectedSourceCodeGenerator != null) {
        expectedSourceCode = expectedSourceCodeGenerator.apply(expected);
      } else {
        expectedSourceCode = objToLiteralString(expected, true);
      }

      out = beginExpected(out);

      for (int i = 0; i < mods.length; i++) {
        if (index % mods[i] == 0) {
          var arg = args.get(i);
          out.append(INDENT).printf("// %s%s%s[%s]: '%s'\n",
              repeat('-', 2 * i),
              i == 0 ? EMPTY : SPACE,
              arg.getKey(),
              (i == 0 ? index : index % mods[i - 1]) / mods[i],
              abbreviate(Objects.toString(arg.getValue()).lines().findFirst().orElse("???"),
                  ELLIPSIS__CHICAGO, 50));
        }
      }
      out.append(INDENT).append(expectedSourceCode);

      endExpected();
    }

    private PrintStream beginExpected(PrintStream out) {
      if (index == 0) {
        open();
        if (this.out == null) {
          this.out = out;
        }

        this.out.println("// expected\n"
            + "java.util.Arrays.asList(");
      }
      assert this.out != null;
      return this.out;
    }

    private void close() {
      assert out != null;

      if (outStream != null) {
        String data = outStream.toString(UTF_8);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(data), null);

        out.close();
        outStream = null;
      }
      out = null;
    }

    private void endExpected() {
      assert out != null;

      if (++index == count) {
        out.println("),");

        close();
      } else {
        out.println(",");
      }
    }

    private void open() {
      if (Runtimes.isDebugging() && !GraphicsEnvironment.isHeadless()) {
        out = new PrintStream(outStream = new ByteArrayOutputStream(), true, UTF_8);
      }
    }
  }

  /**
   * Expected thrown exception.
   *
   * @author Stefano Chizzolini
   * @see ExpectedGenerator#generateExpected(PrintStream, Object, Function, List)
   * @see #assertParameterized(Object, Object, Supplier )
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

  private static final ThreadLocal<@Nullable ExpectedGenerator> expectedGenerator =
      new ThreadLocal<>();

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
   * See {@link #cartesianArgumentsStream (List, List[])} for more information about parameterized
   * test definition.
   * </p>
   *
   * @param actual
   *          Result of the method tested via {@link #evalParameterized( FailableSupplier )}.
   * @param expected
   *          Expected result, provided by {@link #cartesianArgumentsStream (List, List...)}; can be
   *          any of these:
   *          <ul>
   *          <li><b>value as-is</b> (for {@linkplain Matchers#is(Object) exact match}): either
   *          regular result or failure ({@link ThrownExpected})</li>
   *          <li><b>value wrapped in a {@linkplain Matcher matcher}</b> (for custom match) — e.g.,
   *          {@linkplain Matchers#closeTo(double, double) approximate match}:<pre>
   * expected instanceof Double ? is(closeTo((double) expected, DELTA)) : expected</pre>
   *          <p>
   *          NOTE: The matcher MUST be guarded by a check on {@code expected} type as it could also
   *          be {@link ThrownExpected} (in case of expected failure) or {@code null} (if arguments
   *          generation is underway)
   *          </p>
   *          </li>
   *          </ul>
   * @param generationSupplier
   *          Supplies the feed for arguments generation (see
   *          {@link #cartesianArgumentsStream (List, List[])}). <span class="important">IMPORTANT:
   *          In case of additional calls within the same parameterized test, it MUST be
   *          omitted</span> in order to suppress repeated arguments generation (otherwise,
   *          supplying multiple times the same generation feed would disrupt the process).
   * @apiNote For example, to test a method whose signature is
   *          {@code Strings.uncapitalizeGreedy(String value)}:<pre>
   * import static java.util.Arrays.asList;
   * import static org.pdfclown.common.build.test.assertion.Assertions.cartesianArgumentsStream;
   * import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterized;
   * import static org.pdfclown.common.build.test.assertion.Assertions.evalParameterized;
   *
   * import java.util.List;
   * import java.util.stream.Stream;
   * import org.junit.jupiter.params.ParameterizedTest;
   * import org.junit.jupiter.params.provider.Arguments;
   * import org.junit.jupiter.params.provider.MethodSource;
   * import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
   *
   * public class StringsTest {
   *   private static Stream&lt;Arguments&gt; _uncapitalizeGreedy() {
   *     return cartesianArgumentsStream(
   *         // expected <span style=
  "background-color:yellow;color:black;">&lt;- THIS list is generated automatically (see cartesianArgumentsStream(..))</span>
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
   *   public void _uncapitalizeGreedy(Object expected, String value) {
   *     assertParameterizedOf(
   *         () -&gt; Strings.uncapitalizeGreedy(value)),
   *         expected,
   *         () -&gt; new ExpectedGeneration(List.of(
   *             entry("value", value))));
   *   }
   * }</pre>
   */
  public static void assertParameterized(@Nullable Object actual, @Nullable Object expected,
      @Nullable Supplier<? extends ExpectedGeneration> generationSupplier) {
    ExpectedGenerator generator = expectedGenerator.get();
    /*
     * Assertion enabled?
     *
     * NOTE: During arguments generation, parameterized assertions are skipped (otherwise, they
     * would fail fast since their state is incomplete).
     */
    if (generator == null) {
      // Failed result?
      if (actual instanceof Throwable) {
        var thrownActual = (Throwable) actual;
        if (!(expected instanceof ThrownExpected))
          fail(String.format("Failure UNEXPECTED (expected: '%s' (%s))", expected, sqnd(expected)),
              thrownActual);

        var thrownExpected = (ThrownExpected) expected;
        assertThat("Throwable.class.name", thrownActual.getClass().getName(),
            is(thrownExpected.getName()));
        assertThat("Throwable.message", thrownActual.getMessage(),
            is(thrownExpected.getMessage()));
      }
      // Regular result.
      else {
        assertThat(actual, expected instanceof Matcher ? (Matcher<Object>) expected : is(expected));
      }
    }
    // Arguments generation.
    else {
      // Arguments generation suppressed?
      if (generationSupplier == null)
        return;

      var generation = generationSupplier.get();
      generator.generateExpected(generation.out, actual, generation.expectedSourceCodeGenerator,
          generation.args);
    }
  }

  /**
   * Asserts the evaluation of the actual expression corresponds to the expected value.
   *
   * @param actualExpression
   *          Expression to {@linkplain #evalParameterized(FailableSupplier) evaluate} for actual
   *          result.
   * @see #assertParameterized(Object, Object, Supplier)
   */
  public static void assertParameterizedOf(FailableSupplier<Object, Exception> actualExpression,
      @Nullable Object expected,
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
   * Combines the given argument lists into a stream of corresponding {@linkplain Arguments
   * parametric tuples} (Cartesian product) to feed a {@linkplain ParameterizedTest parameterized
   * test}.
   * <p>
   * The {@code expected} parameter shall contain both regular results and failures (i.e., thrown
   * exception) of the method tested with the given {@code args}; failures will be represented as
   * {@link ThrownExpected} to simplify their automated handling.
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
   * {@code boolean}), since it is passed {@code null} during arguments generation (see dedicated
   * section here below).
   * </p>
   * <p>
   * See {@link #assertParameterized(Object, Object, Supplier)} for more information and a full
   * example.
   * </p>
   * <h2>Arguments generation</h2>
   * <p>
   * The source code representation of {@code expected} is automatically generated as described in
   * this section. This simplifies the preparation and maintenance of test cases (especially
   * considering that the Cartesian product of {@code args} causes the size of {@code expected} to
   * rapidly escalate), also unifying the way failed results (i.e., thrown exceptions) are dealt
   * with (see {@link ExpectedGenerator#generateExpected(PrintStream, Object, Function, List)
   * ExpectedGenerator} for more information on their source code representation).
   * </p>
   * <p>
   * Here, the steps to generate {@code expected} (based on the example in
   * {@link #assertParameterized(Object, Object, Supplier)}):
   * </p>
   * <ol>
   * <li>pass {@code null} as {@code expected} argument to this method:<pre>
   * private static Stream&lt;Arguments&gt; _uncapitalizeGreedy() {
   *     return cartesianArgumentsStream(
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
   * {@link ExpectedGeneration#out}, as specified in your
   * {@link #assertParameterized(Object, Object, Supplier)} call (by default, {@linkplain System#err
   * stderr}):
   * </p>
   * <pre>
   * &#64;ParameterizedTest
   * &#64;MethodSource
   * public void _uncapitalizeGreedy(Object expected, String value) {
   *   assertParameterizedOf(
   *       () -&gt; Strings.uncapitalizeGreedy(value),
   *       expected,
   *       () -&gt; new ExpectedGeneration(List.of(
   *           entry("value", value)))); // <span style=
  "background-color:yellow;color:black;">&lt;- No `out` stream specified here, so it defaults to stderr</span>
   * }</pre></li>
   * <li>replace {@code null} (step 1) with the generated source code as {@code expected}
   * argument:<pre>
   * private static Stream&lt;Arguments&gt; _uncapitalizeGreedy() {
   *     return cartesianArgumentsStream(
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
   *
   * @param expected
   *          Expected test results, or {@code null} to automatically generate them (see "Arguments
   *          generation" section here above). Corresponds to the <b>codomain of the test</b>, whose
   *          size MUST equal the Cartesian product of {@code args} — e.g., if
   *          {@code args.size() == 2}, {@code args[0].size() == 5} and {@code args[1].size() == 4},
   *          then {@code expected.size() == 20}.
   * @param args
   *          Test arguments. Each list corresponds to the <b>domain of the respective test
   *          argument</b>.
   * @return A stream of tuples, each composed this way: <pre>
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
   */
  public static Stream<Arguments> cartesianArgumentsStream(@Nullable List<?> expected,
      List<?>... args) {
    var argsList = List.of(args);
    int cartesianCount = cartesianProductCount(argsList);

    // Expected to stub?
    if (expected == null) {
      expected = Collections.nCopies(cartesianCount, null);

      expectedGenerator.set(new ExpectedGenerator(argsList));
    } else {
      expectedGenerator.set(null);
    }

    if (expected.size() != cartesianCount)
      throw new IllegalArgumentException(String.format(
          "`expected` size (%s) MISMATCH with `args` cartesian product (SHOULD be %s)",
          expected.size(), cartesianCount));

    final var expectedList = expected;
    var indexRef = new AtomicInteger();
    return cartesianProduct(argsList)
        .map($ -> {
          $.add(0, expectedList.get(indexRef.getAndIncrement()));
          return Arguments.of($.toArray());
        });
  }

  /**
   * Evaluates the given expression.
   * <p>
   * Intended for use within {@linkplain ParameterizedTest parameterized tests}; its result is
   * expected to be checked via {@link #assertParameterized(Object, Object, Supplier)}.
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
   * @see #cartesianArgumentsStream (List, List[])
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

  private static int cartesianProductCount(List<List<?>> args) {
    return args.stream().mapToInt(List::size).reduce(1, Math::multiplyExact);
  }

  private Assertions() {
  }
}
