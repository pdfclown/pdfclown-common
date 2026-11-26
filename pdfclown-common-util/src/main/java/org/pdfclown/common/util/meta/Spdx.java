/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (Spdx.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block below this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.meta;

import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Objects.equalsAny;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.Strings.ucase;

import java.util.Collection;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.pdfclown.common.util.text.ParseException;

/**
 * <a href="https://spdx.github.io/spdx-spec/v2.3.1/">SPDX 2.3</a> utilities.
 *
 * @author Stefano Chizzolini
 */
public class Spdx {
  /**
   * <a href="https://spdx.github.io/spdx-spec/v2.3.1/SPDX-license-expressions/">SPDX license
   * expression (SPDX 2.3)</a>.
   *
   * @author Stefano Chizzolini
   */
  public static class LicenseExpression {
    /**
     * SPDX license evaluator.
     *
     * @author Stefano Chizzolini
     */
    @FunctionalInterface
    public interface Evaluator {
      /**
       * Creates an evaluator that allows only the given licenses.
       *
       * @param licenses
       *          (regex) Allowed licenses.
       */
      static Evaluator allowed(Collection<String> licenses) {
        List<Pattern> licensePatterns = compile(licenses);
        return ($licenseId, $exceptionId) -> licensePatterns.stream()
            .anyMatch($ -> $.matcher($licenseId).matches());
      }

      /**
       * Creates an evaluator that allows any but the given licenses.
       *
       * @param licenses
       *          (regex) Forbidden licenses.
       */
      static Evaluator forbidden(Collection<String> licenses) {
        List<Pattern> licensePatterns = compile(licenses);
        return ($licenseId, $exceptionId) -> licensePatterns.stream()
            .noneMatch($ -> $.matcher($licenseId).matches());
      }

      private static List<Pattern> compile(Collection<String> licenses) {
        return licenses.stream()
            .map($ -> Pattern.compile($, Pattern.CASE_INSENSITIVE))
            .toList();
      }

      /**
       * Tests a license.
       */
      boolean test(String licenseId, String exceptionId);
    }

    /**
     * {@link LicenseExpression} evaluation context.
     *
     * @author Stefano Chizzolini
     */
    protected static class Context {
      private final Evaluator evaluator;
      private int position;
      private final List<String> tokens;

      public Context(List<String> tokens, Evaluator evaluator) {
        this.tokens = tokens;
        this.evaluator = evaluator;
      }

      public String consume() {
        checkPosition();
        return tokens.get(position++);
      }

      public String current() {
        checkPosition();
        return tokens.get(position);
      }

      /**
       * @implNote For leniency, all tokens are considered case-insensitive, despite the
       *           specification indicates that the operators should be matched in a case-sensitive
       *           manner.
       */
      public boolean is(String token) {
        return !isEnded() && tokens.get(position).equalsIgnoreCase(token);
      }

      public boolean isEnded() {
        return position >= tokens.size();
      }

      public boolean moveNext() {
        checkPosition();
        return position++ < tokens.size();
      }

      public int position() {
        return position;
      }

      private void checkPosition() {
        if (isEnded())
          throw new ParseException("Unexpected end of expression");
      }
    }

    private static final String TOKEN__AND = "AND";
    private static final String TOKEN__GROUP_BEGIN = S + ROUND_BRACKET_OPEN;
    private static final String TOKEN__GROUP_END = S + ROUND_BRACKET_CLOSE;
    private static final String TOKEN__OR = "OR";
    private static final String TOKEN__WITH = "WITH";

    /**
     * Creates the expression corresponding to the given raw representation.
     */
    public static LicenseExpression of(String expr) {
      return new LicenseExpression(expr);
    }

    private final List<String> tokens;

    protected LicenseExpression(String expr) {
      this.tokens = Pattern.compile("\\(|\\)|[^\\s()]+")
          .matcher(expr)
          .results()
          .map(MatchResult::group)
          .toList();
    }

    /**
     * Evaluates this expression through the given license evaluator.
     *
     * @return Whether the evaluation succeeded.
     */
    public boolean evaluate(Evaluator evaluator) throws ParseException {
      var context = createContext(evaluator);
      var ret = evalOr(context);
      if (!context.isEnded())
        throw new ParseException("Unexpected token", null, context.current(), null, null);

      return ret;
    }

    /**
     * Tokens contained in this expression.
     */
    public List<String> getTokens() {
      return tokens;
    }

    /**
     * Creates the evaluation context for the given evaluator.
     */
    protected Context createContext(Evaluator evaluator) {
      return new Context(tokens, evaluator);
    }

    /**
     * Evaluates the current AND expression.
     *
     * @return Whether the evaluation succeeded.
     */
    protected boolean evalAnd(Context context) throws ParseException {
      var ret = evalOperand(context);
      while (context.is(TOKEN__AND)) {
        context.moveNext();
        /*
         * WARNING: KEEP operand resolution separate from `&&` -- the operand MUST always be
         * evaluated (no short-circuit!), otherwise its tokens would be left unconsumed.
         */
        var operand = evalOperand(context);
        ret = ret && operand;
      }
      return ret;
    }

    /**
     * Evaluates the current operand.
     *
     * @return Whether the evaluation succeeded.
     */
    protected boolean evalOperand(Context context) throws ParseException {
      String t = context.consume();
      // Group?
      if (t.equalsIgnoreCase(TOKEN__GROUP_BEGIN)) {
        var ret = evalOr(context);
        if (!context.is(TOKEN__GROUP_END))
          throw new ParseException("Missing token", null, TOKEN__GROUP_END, null, null);

        context.moveNext();
        return ret;
      }
      // Identifier.
      else {
        // Not an identifier?
        if (equalsAny(ucase(t), TOKEN__GROUP_END, TOKEN__AND, TOKEN__OR, TOKEN__WITH))
          throw new ParseException("Unexpected token", null, t, null, null);

        /*
         * NOTE: In case of "X WITH some-exception":, `some-exception` is skipped, and the license
         * `X` is what gets checked.
         */
        var licenseException = EMPTY;
        if (context.is(TOKEN__WITH)) {
          if (!context.moveNext())
            throw new ParseException("Missing exception after WITH");

          licenseException = context.consume();
        }
        return context.evaluator.test(t, licenseException);
      }
    }

    /**
     * Evaluates the current OR expression.
     *
     * @return Whether the evaluation succeeded.
     */
    protected boolean evalOr(Context context) throws ParseException {
      var ret = evalAnd(context);
      while (context.is(TOKEN__OR)) {
        context.moveNext();
        /*
         * WARNING: KEEP operand resolution separate from `||` -- the operand MUST always be
         * evaluated (no short-circuit!), otherwise its tokens would be left unconsumed.
         */
        var operand = evalAnd(context);
        ret = ret || operand;
      }
      return ret;
    }
  }
}
