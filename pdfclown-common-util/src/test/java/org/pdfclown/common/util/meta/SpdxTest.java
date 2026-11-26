/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (SpdxTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block below this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.meta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.pdfclown.common.util.__test.BaseTest;
import org.pdfclown.common.util.text.ParseException;

/**
 * @author Stefano Chizzolini
 */
class SpdxTest extends BaseTest {
  @Nested
  class And {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        MIT AND Apache-2.0                   | true
        MIT AND GPL-3.0-only                 | false
        GPL-3.0-only AND MIT                 | false
        GPL-3.0-only AND GPL-2.0-only        | false
        MIT AND Apache-2.0 AND MIT           | true
        MIT AND Apache-2.0 AND GPL-3.0-only  | false
        MIT AND GPL-3.0-only AND Apache-2.0  | false
        GPL-3.0-only AND MIT AND Apache-2.0  | false
        """)
    void allowed(String expr, boolean expected) {
      assertEquals(expected, evalAllowed(expr, "MIT", "Apache-2\\.0"));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        MIT AND Apache-2.0           | true
        MIT AND GPL-3.0-only         | false
        GPL-3.0-only AND MIT         | false
        GPL-3.0-only AND AGPL-3.0-only| false
        """)
    void forbidden(String expr, boolean expected) {
      assertEquals(expected, evalForbidden(expr, "GPL.*", "AGPL.*"));
    }
  }

  @Nested
  class CaseInsensitivity {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        mit or gpl-3.0-only                  | true
        Mit Or Gpl-3.0-only                  | true
        mit and apache-2.0                   | true
        mit and gpl-3.0-only                 | false
        gpl-3.0-only or mit and apache-2.0   | true
        gpl-3.0-only and mit or apache-2.0   | true
        gpl-2.0-only with classpath-exception-2.0 | false
        """)
    void operators(String expr, boolean expected) {
      assertEquals(expected, evalAllowed(expr, "MIT", "Apache-2\\.0"));
    }

    @Test
    void withLowercase() {
      assertTrue(evalAllowed("gpl-2.0-only with Classpath-exception-2.0", "GPL-2\\.0-only"));
    }
  }

  @Nested
  class Errors {
    @ParameterizedTest
    @ValueSource(strings = {
        // empty
        "",
        // blank
        "   ",
        // dangling AND
        "MIT AND",
        // dangling OR
        "MIT OR",
        // leading AND
        "AND MIT",
        // leading OR
        "OR MIT",
        // lone operator
        "AND",
        // lone operator
        "OR",
        // doubled operator
        "MIT AND AND Apache-2.0",
        // doubled operator
        "MIT OR OR Apache-2.0",
        // consecutive operators
        "MIT AND OR Apache-2.0",
        // consecutive operators
        "MIT OR AND Apache-2.0",
        // missing operator
        "MIT Apache-2.0",
        // missing operator
        "MIT MIT",
        // unclosed group
        "(MIT",
        // unclosed nested group
        "((MIT)",
        // unclosed group
        "(MIT OR Apache-2.0",
        // unopened group
        "MIT)",
        // extra closing bracket
        "(MIT))",
        // closing bracket as operand
        "MIT OR )",
        // empty group
        "()",
        // lone bracket
        "(",
        // lone bracket
        ")",
        // missing exception
        "MIT WITH",
        // leading WITH
        "WITH MIT",
        // lone WITH
        "WITH",
        // dangling group
        "MIT AND (",
        // missing operator between groups
        "(MIT) (Apache-2.0)"
    })
    void malformedExpression(String expr) {
      assertThrows(ParseException.class, () -> evalAllowed(expr, ".*"));
      assertThrows(ParseException.class, () -> evalForbidden(expr, "NONE"));
    }

    @Test
    void malformedExpressionIsRejectedEvenIfResultIsKnownEarly() {
      // Evaluation must not stop at the first (decisive) operand, ignoring the rest.
      assertThrows(ParseException.class, () -> evalAllowed("GPL-3.0-only AND", "MIT"));
      assertThrows(ParseException.class, () -> evalAllowed("MIT OR", "MIT"));
      assertThrows(ParseException.class, () -> evalAllowed("GPL-3.0-only AND (MIT", "MIT"));
      assertThrows(ParseException.class, () -> evalAllowed("MIT OR (MIT", "MIT"));
    }
  }

  @Nested
  class Groups {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        (MIT)                                   | true
        (GPL-3.0-only)                          | false
        ((MIT))                                 | true
        (((GPL-3.0-only)))                      | false
        (MIT OR GPL-3.0-only)                   | true
        (MIT AND GPL-3.0-only)                  | false
        (MIT OR GPL-3.0-only) AND GPL-2.0-only  | false
        (MIT OR GPL-3.0-only) AND Apache-2.0    | true
        (GPL-3.0-only OR MIT) AND MIT           | true
        MIT AND (GPL-3.0-only OR MIT)           | true
        MIT AND (GPL-3.0-only OR GPL-2.0-only)  | false
        (GPL-3.0-only AND MIT) OR GPL-2.0-only  | false
        (GPL-3.0-only AND MIT) OR MIT           | true
        GPL-3.0-only OR (MIT AND Apache-2.0)    | true
        GPL-3.0-only OR (MIT AND GPL-2.0-only)  | false
        (MIT OR GPL-3.0-only) AND (GPL-2.0-only OR Apache-2.0) | true
        (MIT AND GPL-3.0-only) OR (GPL-2.0-only AND Apache-2.0)| false
        (MIT AND Apache-2.0) OR (GPL-2.0-only AND GPL-3.0-only)| true
        ((GPL-3.0-only OR MIT) AND (MIT OR GPL-3.0-only))      | true
        ((GPL-3.0-only OR MIT) AND (GPL-2.0-only OR GPL-3.0-only)) | false
        (MIT OR (GPL-3.0-only AND (GPL-2.0-only OR MIT)))      | true
        (GPL-3.0-only OR (MIT AND (GPL-2.0-only OR GPL-1.0-only))) | false
        (MIT)OR(GPL-3.0-only)                   | true
        (GPL-3.0-only)AND(MIT)                  | false
        """)
    void allowed(String expr, boolean expected) {
      assertEquals(expected, evalAllowed(expr, "MIT", "Apache-2\\.0"));
    }

    @Test
    void forbidden() {
      assertTrue(evalForbidden("(MIT OR GPL-3.0-only) AND Apache-2.0", "GPL.*"));
      assertFalse(evalForbidden("(MIT OR GPL-3.0-only) AND AGPL-3.0-only", "GPL.*", "AGPL.*"));
      assertFalse(evalForbidden("(GPL-3.0-only OR AGPL-3.0-only) OR GPL-2.0-only", "GPL.*",
          "AGPL.*"));
    }
  }

  @Nested
  class Identifier {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        MIT           | true
        Apache-2.0    | true
        GPL-3.0-only  | false
        """)
    void allowed(String expr, boolean expected) {
      assertEquals(expected, evalAllowed(expr, "MIT", "Apache-2\\.0"));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        MIT           | true
        Apache-2.0    | true
        GPL-3.0-only  | false
        AGPL-3.0-only | false
        """)
    void forbidden(String expr, boolean expected) {
      assertEquals(expected, evalForbidden(expr, "GPL.*", "AGPL.*"));
    }

    @Test
    void multiplePatterns() {
      assertTrue(evalAllowed("BSD-3-Clause", "MIT", "BSD-.*", "ISC"));
      assertFalse(evalForbidden("ISC", "MIT", "BSD-.*", "ISC"));
      assertTrue(evalForbidden("Zlib", "MIT", "BSD-.*", "ISC"));
    }

    @Test
    void orLaterSuffix() {
      assertTrue(evalAllowed("GPL-2.0+", "GPL-2\\.0\\+"));
      assertFalse(evalForbidden("GPL-2.0+", "GPL.*"));
    }

    @Test
    void regexIsCaseInsensitive() {
      assertTrue(evalAllowed("MIT", "mit"));
      assertFalse(evalForbidden("GPL-3.0-only", "gpl.*"));
    }

    @Test
    void regexMustMatchWholeIdentifier() {
      // `LGPL-2.1-only` is not matched by `GPL.*` (full match required).
      assertTrue(evalForbidden("LGPL-2.1-only", "GPL.*"));
      assertFalse(evalAllowed("LGPL-2.1-only", "GPL.*"));
      assertFalse(evalAllowed("MIT-0", "MIT"));
    }
  }

  @Nested
  class Or {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        MIT OR Apache-2.0                  | true
        MIT OR GPL-3.0-only                | true
        GPL-3.0-only OR MIT                | true
        GPL-3.0-only OR GPL-2.0-only       | false
        GPL-3.0-only OR GPL-2.0-only OR MIT| true
        MIT OR GPL-3.0-only OR GPL-2.0-only| true
        GPL-3.0-only OR MIT OR GPL-2.0-only| true
        GPL-1.0-only OR GPL-2.0-only OR GPL-3.0-only | false
        """)
    void allowed(String expr, boolean expected) {
      assertEquals(expected, evalAllowed(expr, "MIT", "Apache-2\\.0"));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        MIT OR GPL-3.0-only          | true
        GPL-3.0-only OR MIT          | true
        GPL-3.0-only OR AGPL-3.0-only| false
        MIT OR Apache-2.0            | true
        """)
    void forbidden(String expr, boolean expected) {
      assertEquals(expected, evalForbidden(expr, "GPL.*", "AGPL.*"));
    }
  }

  @Nested
  class Parsing {
    @Test
    void evaluatorReceivesEachLicenseInOrder() {
      var licenses = new ArrayList<String>();
      Spdx.LicenseExpression.of("(MIT OR GPL-3.0-only) AND Apache-2.0")
          .evaluate(($license, $exception) -> {
            licenses.add($license);
            return true;
          });

      // Every operand is parsed (and tested) once, in textual order.
      assertEquals(List.of("MIT", "GPL-3.0-only", "Apache-2.0"), licenses);
    }

    @Test
    void expressionIsReusable() {
      var expr = Spdx.LicenseExpression.of("MIT OR GPL-3.0-only AND Apache-2.0");
      assertTrue(expr.evaluate(Spdx.LicenseExpression.Evaluator.allowed(Set.of("MIT"))));
      assertFalse(expr.evaluate(Spdx.LicenseExpression.Evaluator.allowed(Set.of("GPL.*"))));
      assertTrue(expr.evaluate(Spdx.LicenseExpression.Evaluator.forbidden(Set.of("GPL.*"))));
      assertTrue(expr.evaluate(Spdx.LicenseExpression.Evaluator.allowed(Set.of("MIT"))));
    }

    /**
     * Operands following a decisive one must still be consumed (no short-circuit skipping of
     * tokens).
     */
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        GPL-3.0-only AND MIT                              | false
        GPL-3.0-only AND MIT AND MIT                      | false
        GPL-3.0-only AND (MIT OR MIT)                     | false
        MIT OR GPL-3.0-only                               | true
        MIT OR GPL-3.0-only OR GPL-2.0-only               | true
        MIT OR (GPL-3.0-only AND GPL-2.0-only)            | true
        (GPL-3.0-only AND MIT) OR (MIT OR GPL-2.0-only)   | true
        """)
    void remainingTokensAreConsumed(String expr, boolean expected) {
      assertEquals(expected, evalAllowed(expr, "MIT"));
    }
  }

  /**
   * Operator priority: {@code AND} binds tighter than {@code OR}.
   */
  @Nested
  class Priority {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        # A OR B AND C  == A OR (B AND C)  (NOT (A OR B) AND C)
        MIT OR GPL-3.0-only AND GPL-2.0-only          | true
        # A AND B OR C  == (A AND B) OR C  (NOT A AND (B OR C))
        GPL-3.0-only AND MIT OR MIT                   | true
        GPL-3.0-only AND GPL-2.0-only OR MIT          | true
        GPL-3.0-only AND MIT OR GPL-2.0-only          | false
        MIT AND GPL-3.0-only OR GPL-2.0-only          | false
        MIT AND Apache-2.0 OR GPL-3.0-only            | true
        GPL-3.0-only OR MIT AND Apache-2.0            | true
        GPL-3.0-only OR MIT AND GPL-2.0-only          | false
        GPL-3.0-only OR GPL-2.0-only AND MIT          | false
        MIT AND MIT OR GPL-3.0-only AND GPL-2.0-only  | true
        GPL-3.0-only AND GPL-2.0-only OR MIT AND MIT  | true
        GPL-3.0-only AND MIT OR GPL-2.0-only AND MIT  | false
        MIT AND GPL-3.0-only OR MIT AND GPL-2.0-only  | false
        MIT OR GPL-3.0-only AND GPL-2.0-only OR GPL-1.0-only | true
        GPL-3.0-only OR MIT AND Apache-2.0 OR GPL-1.0-only   | true
        GPL-3.0-only OR GPL-2.0-only OR MIT AND Apache-2.0   | true
        GPL-3.0-only OR GPL-2.0-only OR MIT AND GPL-1.0-only | false
        MIT AND Apache-2.0 AND GPL-3.0-only OR GPL-2.0-only  | false
        MIT AND Apache-2.0 AND GPL-3.0-only OR MIT           | true
        """)
    void allowed(String expr, boolean expected) {
      assertEquals(expected, evalAllowed(expr, "MIT", "Apache-2\\.0"));
    }

    @Test
    void forbidden() {
      // OR-ed alternative satisfied by AND-ed pair of permitted licenses.
      assertTrue(evalForbidden("GPL-3.0-only OR MIT AND Apache-2.0", "GPL.*"));
      // AND-ed pair fails, alternative is forbidden too.
      assertFalse(evalForbidden("GPL-3.0-only OR MIT AND AGPL-3.0-only", "GPL.*", "AGPL.*"));
      // (forbidden AND permitted) fails; permitted alternative saves it.
      assertTrue(evalForbidden("GPL-3.0-only AND MIT OR Apache-2.0", "GPL.*"));
    }

    @Test
    void ungroupedEqualsExplicitlyGrouped() {
      var licenses = List.of("MIT", "GPL-3.0-only");
      for (var a : licenses)
        for (var b : licenses)
          for (var c : licenses) {
            var ungrouped = evalAllowed(a + " OR " + b + " AND " + c, "MIT");
            var grouped = evalAllowed(a + " OR (" + b + " AND " + c + ")", "MIT");
            assertEquals(grouped, ungrouped, a + " OR " + b + " AND " + c);

            ungrouped = evalAllowed(a + " AND " + b + " OR " + c, "MIT");
            grouped = evalAllowed("(" + a + " AND " + b + ") OR " + c, "MIT");
            assertEquals(grouped, ungrouped, a + " AND " + b + " OR " + c);
          }
    }
  }

  @Nested
  class Tokens {
    @Test
    void bracketsWithoutSpaces() {
      assertEquals(
          List.of("(", "MIT", ")", "OR", "(", "GPL-2.0-only", ")"),
          Spdx.LicenseExpression.of("(MIT)OR(GPL-2.0-only)").getTokens());
    }

    @Test
    void extraWhitespace() {
      assertEquals(
          List.of("MIT", "AND", "Apache-2.0"),
          Spdx.LicenseExpression.of("  MIT \t AND\n Apache-2.0  ").getTokens());
    }

    @Test
    void operatorsAndGroups() {
      assertEquals(
          List.of("(", "MIT", "OR", "GPL-2.0-only", ")", "AND", "Apache-2.0"),
          Spdx.LicenseExpression.of("(MIT OR GPL-2.0-only) AND Apache-2.0").getTokens());
    }

    @Test
    void orLaterSuffix() {
      assertEquals(List.of("GPL-2.0+"), Spdx.LicenseExpression.of("GPL-2.0+").getTokens());
    }

    @Test
    void simple() {
      assertEquals(List.of("MIT"), Spdx.LicenseExpression.of("MIT").getTokens());
    }
  }

  @Nested
  class Whitespace {
    @Test
    void surroundingAndRepeated() {
      assertTrue(evalAllowed("   MIT   ", "MIT"));
      assertTrue(evalAllowed(" \t( MIT \n OR   GPL-3.0-only )\t", "MIT"));
      assertFalse(evalAllowed("MIT\tAND\nGPL-3.0-only", "MIT"));
    }
  }

  @Nested
  class With {
    @Test
    void exceptionIdIsEmptyWithoutWith() {
      var calls = new ArrayList<String>();
      Spdx.LicenseExpression.of("MIT").evaluate(($license, $exception) -> calls.add($exception));

      assertEquals(List.of(""), calls);
    }

    @Test
    void exceptionIdIsPassedToEvaluator() {
      var calls = new ArrayList<String>();
      Spdx.LicenseExpression.of("GPL-2.0-only WITH Classpath-exception-2.0")
          .evaluate(($license, $exception) -> calls.add($license + "|" + $exception));

      assertEquals(List.of("GPL-2.0-only|Classpath-exception-2.0"), calls);
    }

    @Test
    void exceptionIsIgnoredByMatching() {
      assertTrue(evalAllowed("GPL-2.0-only WITH Classpath-exception-2.0", "GPL-2\\.0-only"));
      assertFalse(evalAllowed("GPL-2.0-only WITH Classpath-exception-2.0", "MIT"));
      assertFalse(evalForbidden("GPL-2.0-only WITH Classpath-exception-2.0", "GPL.*"));
      assertTrue(evalForbidden("GPL-2.0-only WITH Classpath-exception-2.0", "AGPL.*"));
    }

    @Test
    void withEvaluatorCanDiscriminateOnException() {
      // Custom evaluator allowing GPL only when combined with a given exception
      var evaluator = (Spdx.LicenseExpression.Evaluator) (
          $license, $exception) -> !$license.startsWith("GPL")
              || "Classpath-exception-2.0".equals($exception);

      assertTrue(Spdx.LicenseExpression.of("GPL-2.0-only WITH Classpath-exception-2.0")
          .evaluate(evaluator));
      assertFalse(Spdx.LicenseExpression.of("GPL-2.0-only").evaluate(evaluator));
      assertFalse(
          Spdx.LicenseExpression.of("GPL-2.0-only WITH Other-exception").evaluate(evaluator));
    }

    @Test
    void withInsideAnd() {
      assertTrue(evalAllowed("GPL-2.0-only WITH Classpath-exception-2.0 AND MIT",
          "GPL-2\\.0-only", "MIT"));
      assertFalse(evalAllowed("GPL-2.0-only WITH Classpath-exception-2.0 AND MIT", "MIT"));
      assertFalse(evalAllowed("MIT AND GPL-2.0-only WITH Classpath-exception-2.0", "MIT"));
    }

    @Test
    void withInsideGroup() {
      assertTrue(evalAllowed("(GPL-2.0-only WITH Classpath-exception-2.0) AND MIT",
          "GPL-2\\.0-only", "MIT"));
      assertTrue(evalForbidden("(GPL-2.0-only WITH Classpath-exception-2.0) OR MIT", "GPL.*"));
    }

    @Test
    void withInsideOr() {
      assertTrue(evalAllowed("GPL-3.0-only OR GPL-2.0-only WITH Classpath-exception-2.0",
          "GPL-2\\.0-only"));
      assertFalse(evalAllowed("GPL-3.0-only OR GPL-2.0-only WITH Classpath-exception-2.0", "MIT"));
      assertTrue(evalAllowed("GPL-2.0-only WITH Classpath-exception-2.0 OR MIT", "MIT"));
    }
  }

  private static boolean evalAllowed(String expr, String... allowedRegexes) {
    return Spdx.LicenseExpression.of(expr)
        .evaluate(Spdx.LicenseExpression.Evaluator.allowed(Set.of(allowedRegexes)));
  }

  private static boolean evalForbidden(String expr, String... forbiddenRegexes) {
    return Spdx.LicenseExpression.of(expr)
        .evaluate(Spdx.LicenseExpression.Evaluator.forbidden(Set.of(forbiddenRegexes)));
  }

  @Test
  void toString_() {
    assertThat("( MIT OR ( GPL-3.0-only AND ( GPL-2.0-only OR MIT ) ) )",
        is(Spdx.LicenseExpression.of("(MIT OR (GPL-3.0-only AND (GPL-2.0-only OR MIT)))")
            .toString()));
  }
}