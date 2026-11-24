/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (PatternsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block below this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.regex;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.pdfclown.common.build.test.assertion.Verifiers.COMBINATION;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.test.assertion.Assertions.Argument;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings({ "Convert2MethodRef", "DataFlowIssue" })
class PatternsTest extends BaseTest {
  static class PatternArgument extends Argument<String> {
    private final List<String> matches;
    private final List<String> mismatches;

    PatternArgument(String payload, List<String> matches, List<String> mismatches) {
      super("pattern", payload);

      this.matches = matches;
      this.mismatches = mismatches;
    }

    /**
     * Samples matching {@linkplain #getPayload() regex}.
     */
    public List<String> getMatches() {
      return matches;
    }

    /**
     * Samples not matching {@linkplain #getPayload() regex}.
     */
    public List<String> getMismatches() {
      return mismatches;
    }
  }

  /**
   * <a href=
   * "https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string">Official
   * Semantic Versioning 2.0 regular expression</a>.
   */
  private static final Pattern PATTERN__SEM_VER = Pattern.compile("""
      ^\
      (0|[1-9]\\d*)\\.\
      (0|[1-9]\\d*)\\.\
      (0|[1-9]\\d*)\
      (?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)\
      (?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?\
      (?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?\
      $""");

  @Test
  void escapeGlob() {
    COMBINATION.verify(
        (glob) -> Patterns.escapeGlob(glob),
        List.of("glob"),
        // glob
        List.of(
            "plain",
            "a*b?c",
            "[x]{y,z}",
            // backslashes are doubled: literal backslashes
            "c:\\dir\\f",
            // backslash then '*': literal backslash + escaped '*'
            "\\*",
            // '/' is structure: left as is
            "a/b",
            ""));
  }

  @Test
  void escapeGlob__match() {
    COMBINATION.verify(
        (glob) -> assertRegex(Patterns.globToRegex(
            "**/" + Patterns.escapeGlob(glob.getPayload()) + "/**"), glob),
        List.of("glob"),
        // glob
        List.of(
            new PatternArgument(
                "my*dir",
                List.of(
                    "a/my*dir/b",
                    "a\\my*dir\\b"),
                List.of(
                    "a/myXdir/b")),
            new PatternArgument(
                "[x]",
                List.of("a/[x]/b"),
                List.of("a/x/b")),
            new PatternArgument(
                "{a,b}",
                List.of("x/{a,b}/y"),
                List.of("x/a/y")),
            new PatternArgument(
                "a,b",
                List.of("z/a,b/c"),
                List.of()),
            new PatternArgument(
                "what?",
                List.of("x/what?/y"),
                List.of("x/whats/y")),
            new PatternArgument(
                "[a/b]",
                List.of(
                    // brackets escaped, so the strict rule is not hit
                    "p/[a/b]/q"),
                List.of()),
            new PatternArgument(
                "c:\\dir",
                List.of(
                    // literal backslash matches only a backslash
                    "c:\\dir\\f"),
                List.of("c:/dir/f")),
            new PatternArgument(
                "c:/dir",
                List.of(
                    // '/' in a fragment is a separator: matches both
                    "c:\\dir\\f",
                    "c:/dir/f"),
                List.of()),
            new PatternArgument(
                "a/b",
                List.of("x/a\\b/y"),
                List.of())));
  }

  @Test
  void globToRegex() {
    COMBINATION.verify(
        (glob) -> assertRegex(Patterns.globToRegex(glob.getPayload()), glob),
        List.of("glob"),
        // glob
        List.of(
            new PatternArgument("**/my*.*",
                List.of(
                    "myFile.jpg",
                    "/home/usr/Pictures/myFile.jpg",
                    "/home/usr/Documents/myFile.html",
                    "/home/usr/Projects/foobar/foobar-core/src/main/resources/html/myProject.html"),
                List.of(
                    "/home/usr/Documents/file.html")),
            /*
             * Same as previous case, to demonstrate transparent support to Windows despite Unix
             * separator.
             */
            new PatternArgument("**/my*.*",
                List.of(
                    "myFile.jpg",
                    "C:\\usr\\Pictures\\myFile.jpg",
                    "C:\\usr\\Documents\\myFile.html",
                    "C:\\usr\\Projects\\foobar\\foobar-core\\src\\main\\resources\\html\\myProject.html"),
                List.of(
                    "C:\\usr\\Documents\\file.html")),
            /*
             * Same as previous case, to demonstrate Windows separator are opaque (that is, not
             * recognized as separators, just matching literally).
             */
            new PatternArgument("**\\my*.*",
                List.of("myFile.jpg"),
                List.of(
                    "C:\\usr\\Pictures\\myFile.jpg",
                    "/home/usr/Pictures/myFile.jpg")),
            // ----
            new PatternArgument(
                "**/src/conf/**",
                List.of(
                    // ---- Unix paths
                    "src/conf/MyFile.conf",
                    "module1/src/conf/Another.conf",
                    "a/b/c/src/conf/x/y/z.conf",
                    // ---- Windows paths
                    "src\\conf\\MyFile.conf",
                    "module1\\src\\conf\\Another.conf"),
                List.of(
                    // ---- Unix paths
                    "src/other/MyFile.conf")),
            new PatternArgument(
                "*/**/src/conf/**",
                List.of(
                    // ---- Unix paths
                    "module1/src/conf/Another.conf",
                    "a/b/c/src/conf/x.conf",
                    // ---- Windows paths
                    "module1\\src\\conf\\Another.conf",
                    "a\\b\\c\\src\\conf\\x.conf"),
                List.of(
                    // ---- Unix paths
                    "src/conf/MyFile.conf",
                    // ---- Windows paths
                    "src\\conf\\MyFile.conf")),
            new PatternArgument(
                "/*/**/src/conf/**",
                List.of(
                    // ---- Unix paths
                    "/module1/src/conf/Another.conf",
                    // ---- Windows paths
                    "\\module1\\src\\conf\\Another.conf"),
                List.of(
                    // ---- Unix paths
                    "/src/conf/MyFile.conf")),
            new PatternArgument(
                "**/src/conf/**/*.conf",
                List.of(
                    "src/conf/MyFile.conf",
                    "m/src/conf/d/e.conf"),
                List.of(
                    "m/src/conf/d/e.txt")),
            new PatternArgument(
                "a/**/b",
                List.of(
                    "a\\b",
                    "a\\x\\y\\b"),
                List.of()),
            new PatternArgument(
                "a/b/c",
                List.of(
                    // mixed separators within one path
                    "a\\b/c"),
                List.of()),
            // ---- A backslash in a glob is never a separator
            new PatternArgument(
                "a\\\\b",
                List.of(
                    // glob a\\b: literal backslash matches a backslash
                    "a\\b"),
                List.of(
                    // ... and not a '/'
                    "a/b",
                    "ab")),
            new PatternArgument(
                "a\\\\/b",
                List.of(
                    // glob a\\/b: literal backslash, then separator
                    "a\\/b"),
                List.of(
                    "a\\b")),
            // ---- Wildcards never cross either kind of separator
            new PatternArgument(
                "*.java",
                List.of(
                    "Foo.java"),
                List.of(
                    "a/Foo.java",
                    "a\\Foo.java")),
            new PatternArgument(
                "file?.txt",
                List.of(
                    "file1.txt"),
                List.of(
                    "file/.txt",
                    "file\\.txt")),
            new PatternArgument(
                "a**b",
                List.of(
                    "axxb"),
                List.of(
                    "ax/xb",
                    "ax\\xb")),
            new PatternArgument(
                "**",
                List.of(
                    "any/thing\\at/all"),
                List.of()),
            // ---- Escapes (single backslash)
            new PatternArgument(
                "a\\*b",
                List.of("a*b"),
                List.of("axb")),
            new PatternArgument(
                "\\[x]",
                List.of("[x]"),
                List.of()),
            new PatternArgument(
                "a\\?b",
                List.of("a?b"),
                List.of("axb")),
            new PatternArgument(
                "a\\{b\\}",
                List.of("a{b}"),
                List.of()),
            new PatternArgument(
                "a\\\\\\*",
                List.of(
                    // glob a\\\* = 'a', literal backslash, literal '*'
                    "a\\*"),
                List.of(
                    // the backslash is literal, not a separator
                    "a/*",
                    "a\\x")),
            // ---- Classes, braces, metacharacters
            new PatternArgument(
                "[a-c]x",
                List.of("bx"),
                List.of()),
            new PatternArgument(
                "[!a-c]x",
                List.of("dx"),
                List.of("ax")),
            new PatternArgument(
                "[!a]x",
                List.of(),
                List.of("/x")),
            new PatternArgument(
                "[\\]]x",
                List.of("]x"),
                List.of()),
            new PatternArgument(
                "[]a]x",
                List.of(
                    // leading ']' is literal
                    "]x"),
                List.of()),
            new PatternArgument(
                "[\\d]x",
                List.of(
                    // escaped letter is the letter, not regex \d
                    "dx"),
                List.of(
                    "5x")),
            new PatternArgument(
                "[!\\]]x",
                List.of(
                    // negated class with an escaped ']'
                    "ax"),
                List.of(
                    "]x")),
            new PatternArgument(
                "a\\[b/c]d",
                List.of(
                    // escaped '[' then a real separator: fine
                    "a[b\\c]d"),
                List.of()),
            new PatternArgument(
                "[a-c]/[x-z]",
                List.of(
                    // separator BETWEEN classes: fine
                    "b\\y"),
                List.of()),
            new PatternArgument(
                "a[b\\\\c]d",
                List.of(
                    // glob a[b\\c]d: class with a literal backslash member
                    "a\\d"),
                List.of(
                    "a/d")),
            new PatternArgument(
                "src/{main,test}/**/*.{java,kt}",
                List.of(
                    "src/main/a/B.java",
                    "src/test\\B.kt"),
                List.of(
                    "src/dev/B.java")),
            new PatternArgument(
                "{**/a,b}",
                List.of(
                    "x/y/a",
                    "b"),
                List.of()),
            new PatternArgument(
                "a.b+c",
                List.of("a.b+c"),
                List.of("aXb+c"))));
  }

  /**
   * Globs that must be rejected.
   */
  @Test
  void globToRegex__invalid() {
    COMBINATION.verify(
        (glob) -> Patterns.globToRegex(glob),
        List.of("glob"),
        // glob
        List.of(
            // '/' inside a bracket expression
            "a[b/c]d",
            // escaped slash inside a class
            "[a\\/b]",
            // escaped slash outside a class: write '/' instead
            "a\\/b",
            // ... also after a globstar
            "**\\/a",
            // glob a\\\/b: literal backslash, then the illegal escape \/
            "a\\\\\\/b",
            // negated class containing a separator
            "[!/]x",
            // leading ']' literal, then a separator
            "[]/]",
            // unterminated, and a separator follows
            "src/[abc/**",
            // unterminated '['
            "file[1.txt",
            // the ']' is escaped, so the class never closes
            "[abc\\]",
            // dangling escape
            "a\\",
            // unbalanced '{'
            "{a,b"));
  }

  @Test
  void indexOfMatchFailure() {
    COMBINATION.verify(
        (input) -> {
          Matcher matcher = PATTERN__SEM_VER.matcher(input);
          //noinspection ResultOfMethodCallIgnored
          matcher.find();
          return Patterns.indexOfMatchFailure(matcher);
        },
        List.of("input"),
        // input
        List.of(
            // VALID
            "1.0.0",
            "1.0.0-alpha",
            // INVALID
            "1.11.0.99",
            "1.-11.0",
            "1.01.0",
            "1.0.0-00.3.7",
            "1.0.0_alpha",
            "1.0.0.5-alpha"));
  }

  @Test
  void wildcardToRegex() {
    COMBINATION.verify(
        (wildcard) -> assertRegex(Patterns.wildcardToRegex(wildcard.getPayload()), wildcard),
        List.of("wildcard"),
        // wildcard
        List.of(
            // ---------- Literals and whole-string matching ----------
            new PatternArgument(
                "abc",
                List.of("abc"),
                List.of("ab", "abcd", "xabc", "ABC", "")),
            new PatternArgument(
                "",
                List.of(""),
                List.of("a", " ")),
            new PatternArgument(
                "a.b",
                List.of("a.b"),
                List.of("axb", "ab", "a..b")),
            // Regex metacharacters are plain literals in a glob
            new PatternArgument(
                "(a|b)+$",
                List.of("(a|b)+$"),
                List.of("a", "b", "ab", "aab")),
            // ---------- '*' : any sequence, including empty ----------
            // '*' alone matches every string, so no mismatch sample exists
            new PatternArgument(
                "*",
                List.of("", "a", "abc", "a/b", ".hidden", "a*b"),
                List.of("a\nb")),
            new PatternArgument(
                "a*",
                List.of("a", "ab", "abc", "a/b/c", "a.b"),
                List.of("", "b", "ba")),
            new PatternArgument(
                "*a",
                List.of("a", "ba", "bba", "/a", ".a"),
                List.of("", "b", "ab")),
            new PatternArgument(
                "a*c",
                List.of("ac", "abc", "abbbc", "a/b/c", "acc", "a*c"),
                List.of("a", "ab", "abcd", "bc", "ca")),
            new PatternArgument(
                "a*b*c",
                List.of("abc", "aXbYc", "abbcbc", "a/b/c"),
                List.of("acb", "ab", "bc", "abcd")),
            // Requires backtracking: the first 'ab' is not the final one
            new PatternArgument(
                "*ab",
                List.of("ab", "aab", "abab", "xxab"),
                List.of("aba", "abb", "a", "")),
            new PatternArgument(
                "a**b",
                List.of("ab", "axb", "a/x/b"),
                List.of("a", "ba", "abc")),
            // ---------- '?' : exactly one character ----------
            new PatternArgument(
                "?",
                List.of("a", "/", ".", "*", " "),
                List.of("", "ab")),
            new PatternArgument(
                "a?c",
                List.of("abc", "a/c", "a.c", "a?c", "a*c"),
                List.of("ac", "abbc", "abcd", "xabc")),
            new PatternArgument(
                "???",
                List.of("abc", "a/b", "...", "   "),
                List.of("", "ab", "abcd")),
            new PatternArgument(
                "caf?",
                List.of("cafe", "café"),
                List.of("caf", "cafés")),
            // ---------- Bracket expressions: sets ----------
            new PatternArgument(
                "[abc]",
                List.of("a", "b", "c"),
                List.of("d", "A", "ab", "")),
            new PatternArgument(
                "x[abc]y",
                List.of("xay", "xby", "xcy"),
                List.of("xy", "xdy", "xaby", "XAY")),
            new PatternArgument(
                "[abc]*",
                List.of("a", "bxyz", "c/d"),
                List.of("", "d", "xa")),
            // ---------- Bracket expressions: ranges ----------
            new PatternArgument(
                "[a-c]",
                List.of("a", "b", "c"),
                List.of("d", "A", "-", "")),
            new PatternArgument(
                "[a-a]",
                List.of("a"),
                List.of("b", "")),
            new PatternArgument(
                "[0-9]",
                List.of("0", "5", "9"),
                List.of("a", "-", "10", "")),
            new PatternArgument(
                "[a-cx-z0-9]",
                List.of("b", "y", "5", "a", "z"),
                List.of("d", "w", "A", "-", "bb")),
            new PatternArgument(
                "[A-Z]",
                List.of("A", "M", "Z"),
                List.of("a", "0", "")),
            // ---------- Bracket expressions: negation ----------
            new PatternArgument(
                "[!abc]",
                List.of("d", "Z", "1", "/", "."),
                List.of("a", "b", "c", "", "dd")),
            new PatternArgument(
                "[!0-9]",
                List.of("a", "-", "/"),
                List.of("0", "5", "9", "")),
            new PatternArgument(
                "a[!b]c",
                List.of("aac", "a/c", "a.c"),
                List.of("abc", "ac", "axxc")),
            // '^' negation: widely supported but unspecified by POSIX; remove if unsupported
            new PatternArgument(
                "[^abc]",
                List.of("d", "Z", "1"),
                List.of("a", "b", "c", "")),
            // ---------- Bracket expressions: special members ----------
            // ']' as first member is literal
            new PatternArgument(
                "[]a]",
                List.of("]", "a"),
                List.of("b", "[", "]a", "")),
            new PatternArgument(
                "[!]a]",
                List.of("b", "[", "/"),
                List.of("]", "a", "")),
            // '-' first or last is literal
            new PatternArgument(
                "[-a]",
                List.of("-", "a"),
                List.of("b", ",", "")),
            new PatternArgument(
                "[a-]",
                List.of("a", "-"),
                List.of("b", ",", "")),
            // '[' inside a set is literal
            new PatternArgument(
                "[[a]",
                List.of("[", "a"),
                List.of("]", "b", "")),
            // Wildcard characters inside brackets are literal
            new PatternArgument(
                "[*?]",
                List.of("*", "?"),
                List.of("a", "**", "")),
            // ---------- Escaping with backslash ----------
            new PatternArgument(
                "\\*",
                List.of("*"),
                List.of("a", "", "**", "\\*")),
            new PatternArgument(
                "\\?",
                List.of("?"),
                List.of("a", "", "??")),
            new PatternArgument(
                "a\\*c",
                List.of("a*c"),
                List.of("abc", "ac", "a*cd", "a**c")),
            new PatternArgument(
                "\\[abc]",
                List.of("[abc]"),
                List.of("a", "b", "[a]", "")),
            new PatternArgument(
                "\\[",
                List.of("["),
                List.of("a", "\\[", "")),
            // Escaped backslash matches exactly one backslash
            new PatternArgument(
                "\\\\",
                List.of("\\"),
                List.of("\\\\", "a", "")),
            new PatternArgument(
                "a\\\\*",
                List.of("a\\", "a\\b", "a\\\\"),
                List.of("a", "ab", "\\a")),
            // Escaping an ordinary character yields that character
            new PatternArgument(
                "\\a",
                List.of("a"),
                List.of("b", "\\a", "")),
            new PatternArgument(
                "*\\**",
                List.of("*", "a*b", "**", "x*"),
                List.of("", "ab", "a?b")),
            // ---------- '/' is an ordinary character ----------
            new PatternArgument(
                "a/b",
                List.of("a/b"),
                List.of("ab", "a//b", "a/b/")),
            new PatternArgument(
                "a*b",
                List.of("a/b", "a/x/b", "a//b"),
                List.of("a/", "/b", "a/c")),
            new PatternArgument(
                "a?b",
                List.of("a/b"),
                List.of("ab", "a//b")),
            new PatternArgument(
                "a[/]b",
                List.of("a/b"),
                List.of("ab", "a.b")),
            new PatternArgument(
                "*/*",
                List.of("a/b", "/", "a/b/c", "//", "/b"),
                List.of("", "ab", "a")),
            new PatternArgument(
                "/*",
                List.of("/", "/a", "/a/b"),
                List.of("", "a", "a/")),
            // ---------- Leading dot is an ordinary character ----------
            new PatternArgument(
                "*rc",
                List.of(".bashrc", "rc", "x.rc"),
                List.of("rc~", ".bashrc~", "")),
            new PatternArgument(
                "?bashrc",
                List.of(".bashrc", "_bashrc"),
                List.of("bashrc", "..bashrc")),
            new PatternArgument(
                "[.]x",
                List.of(".x"),
                List.of("x", "..x", "ax")),
            new PatternArgument(
                ".*",
                List.of(".", "..", ".a", ".a.b"),
                List.of("", "a", "a.b")),
            // ---------- Case sensitivity ----------
            new PatternArgument(
                "Abc",
                List.of("Abc"),
                List.of("abc", "ABC", "aBc")),
            // ---------- Combined features ----------
            new PatternArgument(
                "[a-c]?*[!0-9]",
                List.of("abc", "ab!", "a/xyz", "cZ/Q"),
                List.of("ab1", "a", "dab", "ab5")),
            new PatternArgument(
                "*.[ch]",
                List.of("main.c", "util.h", "a/b/x.c", ".c"),
                List.of("main.cpp", "main.", "main.o", "c")),
            new PatternArgument(
                "file[0-9][0-9].t?t",
                List.of("file01.txt", "file99.tat", "file00.t/t"),
                List.of("file1.txt", "file001.txt", "file01.tt", "File01.txt")),
            new PatternArgument(
                "\\**\\?",
                List.of("*?", "*abc?", "*/?"),
                List.of("?", "*", "abc?", "a*?"))));
  }

  private String assertRegex(String regex, PatternArgument patternArgument) {
    assertRegexMatches(regex, patternArgument.getMatches(), true);
    assertRegexMatches(regex, patternArgument.getMismatches(), false);
    return regex;
  }

  /**
   * @param inputs
   *          Strings to evaluate against {@code regex}.
   * @param expected
   *          Whether {@code inputs} are expected to match {@code regex}.
   */
  private void assertRegexMatches(String regex, List<String> inputs, boolean expected) {
    Predicate<String> matcher = Pattern.compile(regex).asMatchPredicate();
    for (var input : inputs) {
      assertThat(input, matcher.test(input), is(expected));
    }
  }
}
