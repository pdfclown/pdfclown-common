/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

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
import static org.apache.commons.lang3.StringUtils.wrap;
import static org.pdfclown.common.util.Chars.BACKSLASH;
import static org.pdfclown.common.util.Chars.COMMA;
import static org.pdfclown.common.util.Chars.DQUOTE;
import static org.pdfclown.common.util.Chars.MINUS;
import static org.pdfclown.common.util.Chars.PLUS;
import static org.pdfclown.common.util.Chars.SEMICOLON;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Chars.SQUOTE;
import static org.pdfclown.common.util.Exceptions.unsupported;
import static org.pdfclown.common.util.Strings.S;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
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
     * Adds the string representation of the argument.
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
      if (o instanceof Collection<?> c)
        return arg(listArg(c));
      else
        return arg(o.toString());
    }

    /**
     * Adds an argument.
     */
    public Args arg(String s) {
      base.add(s);
      return this;
    }

    /**
     * Adds an option.
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
     * Adds arguments.
     *
     * @param ee
     *          Argument values (see {@link #arg(Object)}).
     */
    public Args args(Iterable<?> ee) {
      ee.forEach(this::arg);
      return this;
    }

    /**
     * Adds arguments.
     *
     * @param ee
     *          Argument values (see {@link #arg(Object)}).
     */
    public Args args(Object[] ee) {
      return args(Arrays.asList(ee));
    }

    /**
     * Gets whether this collection contains the argument.
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
   * Target collection adapter for {@link #parseListIncremental(String, Function, Collection)}.
   *
   * @author Stefano Chizzolini
   */
  public abstract static class ListIncrementalAdapter<E> implements Collection<E> {
    @Override
    public final boolean addAll(Collection<? extends E> c) {
      throw unsupported();
    }

    @Override
    public final boolean contains(Object o) {
      throw unsupported();
    }

    @Override
    public final boolean containsAll(Collection<?> c) {
      throw unsupported();
    }

    @Override
    public final void forEach(Consumer<? super E> action) {
      throw unsupported();
    }

    @Override
    public final boolean isEmpty() {
      throw unsupported();
    }

    @Override
    public final Iterator<E> iterator() {
      throw unsupported();
    }

    @Override
    public final Stream<E> parallelStream() {
      throw unsupported();
    }

    @Override
    public final boolean removeAll(Collection<?> c) {
      throw unsupported();
    }

    @Override
    public final boolean removeIf(Predicate<? super E> filter) {
      throw unsupported();
    }

    @Override
    public final boolean retainAll(Collection<?> c) {
      throw unsupported();
    }

    @Override
    public final int size() {
      throw unsupported();
    }

    @Override
    public final Spliterator<E> spliterator() {
      throw unsupported();
    }

    @Override
    public final Stream<E> stream() {
      throw unsupported();
    }

    @Override
    public final Object[] toArray() {
      throw unsupported();
    }

    @Override
    public final <T> T[] toArray(IntFunction<T[]> generator) {
      throw unsupported();
    }

    @Override
    public final <T> T[] toArray(T[] a) {
      throw unsupported();
    }
  }

  /**
   * Converts an iterable to a semicolon-separated textual argument.
   *
   * @see #parseList(String)
   */
  public static String listArg(Iterable<?> o) {
    return listArg(o, Object::toString);
  }

  /**
   * Converts an iterable to a semicolon-separated textual argument.
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
   * Parses a command line.
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
   * Parses a directory.
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
   * Parses a string as a stream of values.
   * <p>
   * Useful to convert textual lists of references (such as those coming from configuration files or
   * command-line options) to be transformed through {@link Stream}.
   * </p>
   * <p>
   * Values are trimmed and filtered out if empty.
   * </p>
   *
   * @param s
   *          String of comma- (or semicolon-) separated argument values.
   * @see #parseListIncremental(String, Function, Collection)
   * @see #listArg(Iterable, Function)
   */
  public static Stream<String> parseList(String s) {
    return !s.isEmpty()
        ? Stream.of(s.split(s.contains(S + SEMICOLON) ? S + SEMICOLON : S + COMMA))
            .map(String::trim)
            .filter(StringUtils::isNotEmpty)
        : Stream.empty();
  }

  /**
   * Parses a string as elements applied incrementally to the collection.
   * <p>
   * If {@code s} is prefixed by a modifier ({@code '+'} or {@code '-'}), then the elements mapped
   * from the argument values are, respectively, appended or removed to/from {@code target};
   * otherwise, the elements replace the whole {@code target} contents.
   * </p>
   * <p>
   * Useful to convert textual lists of references (such as those coming from configuration files or
   * command-line options) providing the flexibility to either modify the existing state or redefine
   * it from scratch.
   * </p>
   *
   * @param s
   *          String of comma- (or semicolon-) separated argument values. It can be prefixed by a
   *          modifier, as described above.
   * @param transformer
   *          Maps to elements the argument values from {@code s} as split by
   *          {@link #parseList(String)}.
   * @param target
   *          Collection the mapped elements are applied to. Use {@link ListIncrementalAdapter} for
   *          custom mapping of the modifications.
   * @param <E>
   *          Element type.
   * @param <C>
   *          Target collection type.
   * @return {@code target}
   * @see #parseList(String)
   * @see #listArg(Iterable, Function)
   */
  public static <E, C extends Collection<E>> C parseListIncremental(String s,
      Function<Stream<String>, Stream<E>> transformer, C target) {
    if (!s.isEmpty()) {
      final char modifier;
      {
        char c = s.charAt(0);
        switch (c) {
          case PLUS:
          case MINUS:
            modifier = c;
            s = s.substring(1);
            break;
          default:
            target.clear();
            modifier = PLUS;
        }
      }
      transformer
          .apply(parseList(s))
          .forEach($ -> {
            if (modifier == PLUS) {
              target.add($);
            } else {
              target.remove($);
            }
          });
    } else {
      target.clear();
    }
    return target;
  }

  /**
   * Parses a path, no matter whether it exists.
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
   * {@jada.reuseDoc} Parses the resource corresponding to the name.
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
   * {@jada.reuseDoc END}
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
   * {@jada.doc} Parses the resource corresponding to the name.
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
   * {@jada.doc END}
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
   * {@jada.reuseDoc} Parses the resource corresponding to the name.
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
   * {@jada.reuseDoc END}
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

  private Clis() {
  }
}
