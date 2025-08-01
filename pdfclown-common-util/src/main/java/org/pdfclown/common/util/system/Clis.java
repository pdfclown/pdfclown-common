/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Clis.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.system;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.wrap;
import static org.pdfclown.common.util.Strings.BACKSLASH;
import static org.pdfclown.common.util.Strings.COMMA;
import static org.pdfclown.common.util.Strings.DQUOTE;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.Strings.SEMICOLON;
import static org.pdfclown.common.util.Strings.SPACE;
import static org.pdfclown.common.util.Strings.SQUOTE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.stream.Streams;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.io.Resource;

/**
 * Command-line utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Clis {
  /**
   * Simple arguments collection for command line and other configuration contexts.
   *
   * @author Stefano Chizzolini
   */
  public static class Args {
    protected final List<String> base = new ArrayList<>();

    /**
     * Adds the string representation of the given argument.
     *
     * @param o
     *          ({@link Collection} is joined in a semicolon-separated string, anything else is
     *          converted to string).
     */
    public Args arg(Object o) {
      /*
       * IMPORTANT: DO NOT generalize to `Iterable`, as certain classes (like `Path`) get
       * inappropriately split.
       */
      if (o instanceof Collection)
        return arg(listArg((Collection<?>) o));
      else
        return arg(o.toString());
    }

    /**
     * Adds the given argument.
     */
    public Args arg(String s) {
      base.add(s);
      return this;
    }

    /**
     * Adds the given option.
     *
     * @param option
     *          Option name.
     * @param values
     *          Option values (see {@link #arg(Object)}).
     */
    public Args arg(String option, Object... values) {
      arg(requireNonNull(option, "`option`"));
      for (var value : values) {
        arg(value);
      }
      return this;
    }

    /**
     * Adds the given arguments.
     *
     * @param ee
     *          Argument values (see {@link #arg(Object)}).
     */
    public Args args(Iterable<?> ee) {
      ee.forEach(this::arg);
      return this;
    }

    /**
     * Adds the given arguments.
     *
     * @param ee
     *          Argument values (see {@link #arg(Object)}).
     */
    public Args args(Object[] ee) {
      return args(Arrays.asList(ee));
    }

    /**
     * Gets whether this collection contains the given argument.
     */
    public boolean contains(String arg) {
      return base.contains(arg);
    }

    @Override
    public String toString() {
      return base.stream()
          .map($ -> $.contains(S + SPACE) ? wrap($, DQUOTE) : $)
          .collect(joining(S + SPACE));
    }
  }

  /**
   * Converts the given iterable to a semicolon-separated textual argument.
   *
   * @see #parseStrList(String)
   */
  public static String listArg(Iterable<?> o) {
    return listArg(o, Object::toString);
  }

  /**
   * Converts the given iterable to a semicolon-separated textual argument.
   *
   * @param mapper
   *          Maps each element to its textual representation.
   * @see #parseList(String)
   * @implNote Conventionally, list elements in an argument can be concatenated by either comma or
   *           semicolon; the latter is herein applied, as it allows the use of commas (which are
   *           typically more common) within elements.
   */
  public static <T> String listArg(Iterable<T> o, Function<T, String> mapper) {
    return Streams.of(o)
        .map(mapper)
        .collect(joining(S + SEMICOLON));
  }

  /**
   * Parses the given command line.
   * <p>
   * Supports argument (single- and double-) quoting and character escaping.
   * </p>
   */
  public static List<String> parseArgs(final String argsString) {
    var ret = new ArrayList<String>();
    int delimiter = 0;
    var b = new StringBuilder();
    for (int i = 0, l = argsString.length(); i < l; i++) {
      char c = argsString.charAt(i);
      switch (c) {
        case BACKSLASH:
          b.append(argsString.charAt(++i));
          break;
        case SQUOTE:
        case DQUOTE:
          if (delimiter > 0) {
            if (c == delimiter) {
              delimiter = 0;
            } else {
              b.append(c);
            }
          } else {
            delimiter = c;
          }
          break;
        default:
          if (delimiter > 0) {
            b.append(c);
          } else if (Character.isWhitespace(c)) {
            // Argument ended?
            if (b.length() > 0) {
              ret.add(b.toString());

              b.setLength(0);
            }
          } else {
            b.append(c);
          }
      }
    }
    // End last argument!
    if (b.length() > 0) {
      ret.add(b.toString());
    }
    return ret;
  }

  /**
   * Parses the directory corresponding to the given path.
   * <p>
   * Useful to convert textual references to filesystem resources (such as those coming from
   * configuration files or command-line options) to their normalized absolute form.
   * </p>
   *
   * @return {@code null}, if the directory does not exist.
   * @see #parsePath(String)
   */
  public static @Nullable Path parseDir(String s) {
    var ret = parsePath(s);
    return Files.isDirectory(ret) ? ret : null;
  }

  /**
   * Parses the given string as a stream of values.
   * <p>
   * Useful to convert textual lists of references (such as those coming from configuration files or
   * command-line options) to be transformed through {@link Stream}.
   * </p>
   * <p>
   * Values are trimmed and filtered out if empty.
   * </p>
   *
   * @param s
   *          List of semicolon-(or, alternatively, comma-)separated values.
   * @see #parseStrList(String)
   * @see #listArg(Iterable, Function)
   */
  public static Stream<String> parseList(String s) {
    return !s.isEmpty()
        ? Stream.of(s.split(s.contains(S + SEMICOLON) ? S + SEMICOLON : S + COMMA))
            .map(String::trim)
            .filter($ -> !$.isEmpty())
        : Stream.empty();
  }

  /**
   * Parses the given path, no matter whether it exists.
   * <p>
   * Useful to convert textual references to filesystem resources (such as those coming from
   * configuration files or command-line options) to their normalized absolute form.
   * </p>
   *
   * @see #parseDir(String)
   */
  public static Path parsePath(String s) {
    return Path.of(s).toAbsolutePath().normalize();
  }

  /**
   * Parses the resource corresponding to the given name.
   * <p>
   * Useful to convert textual references to resources (such as those coming from configuration
   * files or command-line options).
   * </p>
   * <p>
   * For more information, see {@linkplain #parseResource(String, ClassLoader, Function) main
   * overload}.
   * </p>
   *
   * @param name
   *          Resource name (URL, filesystem path or classpath resource (possibly qualified by
   *          {@code "classpath:"} prefix)).
   * @return {@code null}, if the resource corresponding to {@code name} does not exist.
   */
  public static @Nullable Resource parseResource(String name) {
    return Resource.of(name);
  }

  /**
   * Parses the resource corresponding to the given name.
   * <p>
   * Useful to convert textual references to resources (such as those coming from configuration
   * files or command-line options).
   * </p>
   * <p>
   * Supported sources:
   * </p>
   * <ul>
   * <li>classpath (either explicitly qualified via URI scheme ({@code "classpath:"}), or
   * automatically detected)</li>
   * <li>filesystem</li>
   * <li>generic URL</li>
   * </ul>
   *
   * @param name
   *          Resource name (URL, filesystem path or classpath resource (possibly qualified by
   *          {@code "classpath:"} prefix)).
   * @param cl
   *          {@link ClassLoader} for classpath resource resolution.
   * @param fileResolver
   *          Filesystem path resolver. Converts relative paths to their absolute counterparts.
   * @return {@code null}, if the resource corresponding to {@code name} does not exist.
   */
  public static @Nullable Resource parseResource(String name, ClassLoader cl,
      Function<Path, Path> fileResolver) {
    return Resource.of(name, cl, fileResolver);
  }

  /**
   * Parses the resource corresponding to the given name.
   * <p>
   * Useful to convert textual references to resources (such as those coming from configuration
   * files or command-line options).
   * </p>
   * <p>
   * For more information, see {@linkplain #parseResource(String, ClassLoader, Function) main
   * overload}.
   * </p>
   *
   * @param name
   *          Resource name (URL, filesystem path or classpath resource (possibly qualified by
   *          {@code "classpath:"} prefix)).
   * @param fileResolver
   *          Filesystem path resolver. Converts relative paths to their absolute counterparts.
   * @return {@code null}, if the resource corresponding to {@code name} does not exist.
   */
  public static @Nullable Resource parseResource(String name, Function<Path, Path> fileResolver) {
    return Resource.of(name, fileResolver);
  }

  /**
   * Parses the given string as a list of values.
   * <p>
   * Useful to convert textual lists of values (such as those coming from configuration files or
   * command-line options).
   * </p>
   * <p>
   * Values are trimmed and filtered out if empty.
   * </p>
   *
   * @param s
   *          List of semicolon-(or, alternatively, comma-)separated values.
   * @see #parseList(String)
   * @see #listArg(Iterable)
   */
  public static List<String> parseStrList(String s) {
    return parseList(s).collect(toList());
  }

  private Clis() {
  }
}
