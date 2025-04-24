/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (Configs.java) is part of pdfclown-common-util module in pdfClown Common project
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
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Strings.COLON;
import static org.pdfclown.common.util.Strings.COMMA;
import static org.pdfclown.common.util.Strings.DQUOTE;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.Strings.SEMICOLON;
import static org.pdfclown.common.util.Strings.SPACE;
import static org.pdfclown.common.util.io.Files.urlOf;
import static org.pdfclown.common.util.net.Uris.uri;
import static org.pdfclown.common.util.net.Uris.url;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.stream.Streams;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.net.Uris;

/**
 * Configuration utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Configs {
  /**
   * Arguments collection, suitable for command line and other textual configuration contexts.
   *
   * @author Stefano Chizzolini
   */
  public static class Args {
    protected final List<String> base = new ArrayList<>();

    /**
     * Adds the string representation of the given argument.
     *
     * @param o
     *          ({@link Iterable} is joined in a semicolon-separated string, anything else is
     *          converted to string).
     */
    public Args arg(Object o) {
      if (o instanceof Iterable)
        return arg(toArg((Iterable<?>) o));
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
     */
    public Args args(Iterable<?> ee) {
      ee.forEach(this::arg);
      return this;
    }

    /**
     * Adds the given arguments.
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
   * Classpath resource protocol.
   */
  private static final String URI_SCHEME__CLASSPATH = "classpath";

  private static final String RESOURCE_NAME_PREFIX__CLASSPATH = URI_SCHEME__CLASSPATH + COLON;

  /**
   * Parses the directory corresponding to the given path.
   * <p>
   * Useful to convert textual references to resources (such as those coming from configuration
   * files or command-line options) to their canonical form.
   * </p>
   *
   * @return {@code null}, if the directory does not exist.
   */
  public static @Nullable File parseDir(String s) {
    var ret = parsePath(s);
    return ret.isDirectory() ? ret : null;
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
   * @see #toArg(Iterable, Function)
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
   * Useful to convert textual references to resources (such as those coming from configuration
   * files or command-line options) to their canonical form.
   * </p>
   */
  public static File parsePath(String s) {
    var ret = new File(s);
    try {
      ret = ret.getCanonicalFile();
    } catch (IOException ex) {
      throw runtime(ex);
    }
    return ret;
  }

  /**
   * Parses the {@link URL} corresponding to the given resource name.
   * <p>
   * Useful to convert textual references to resources (such as those coming from configuration
   * files or command-line options) to their canonical form.
   * </p>
   * <p>
   * For more information, see
   * {@linkplain #parseResourceUrl(String, ClassLoader, Function, FileSystem) main overload}.
   * </p>
   *
   * @param name
   *          Resource name (URL, filesystem path or classpath resource (possibly qualified by
   *          {@code "classpath:"} prefix)).
   * @return {@code null}, if the resource corresponding to {@code name} does not exist.
   */
  public static @Nullable URL parseResourceUrl(String name) {
    return parseResourceUrl(name, $ -> new File($).getAbsolutePath());
  }

  /**
   * Parses the {@link URL} corresponding to the given resource name.
   * <p>
   * Useful to convert textual references to resources (such as those coming from configuration
   * files or command-line options) to their canonical form.
   * </p>
   * <p>
   * Supported sources:
   * </p>
   * <ul>
   * <li>classpath (either explicitly defined via URI scheme ({@code "classpath:"} prefix), or
   * automatically detected)</li>
   * <li>filesystem</li>
   * <li>generic URL</li>
   * </ul>
   * <p>
   * Name resolution algorithm:
   * </p>
   * <ol>
   * <li>if {@code name} is prefixed by {@code "classpath:"} (<b>explicit classpath resource</b>),
   * it is resolved through {@code cl} and returned</li>
   * <li>{@code name} is converted through {@code relativeFilePathResolver} to an absolute
   * filesystem path: if its node exists on {@code fs} (<b>filesystem resource</b>), it is
   * returned</li>
   * <li>if {@code name} is an absolute URL (<b>generic URL resource</b>), it is returned</li>
   * <li>otherwise ({@code name} is a relative URL — <b>implicit classpath resource</b>), it is
   * resolved through {@code cl} and returned</li>
   * </ol>
   * <p>
   * In any case, the resolved resource is checked for existence before being returned.
   * </p>
   *
   * @param name
   *          Resource name (URL, filesystem path or classpath resource (possibly qualified by
   *          {@code "classpath:"} prefix)).
   * @param cl
   *          {@link ClassLoader} for classpath resource resolution.
   * @param relativeFilePathResolver
   *          Relative filesystem path resolver. Converts relative paths to their absolute
   *          counterparts.
   * @param fs
   *          Filesystem for file path interpretation.
   * @return {@code null}, if the resource corresponding to {@code name} does not exist.
   */
  @SuppressWarnings("null")
  public static @Nullable URL parseResourceUrl(String name, ClassLoader cl,
      Function<String, String> relativeFilePathResolver, FileSystem fs) {
    // Classpath (explicit)?
    if (name.startsWith(RESOURCE_NAME_PREFIX__CLASSPATH))
      return cl.getResource(name.substring(RESOURCE_NAME_PREFIX__CLASSPATH.length()));

    // Filesystem.
    try {
      var file = fs.getPath(name);
      // Resolve relative file path!
      if (!file.isAbsolute()) {
        file = fs.getPath(relativeFilePathResolver.apply(file.toString()));
      }
      // Filesystem resource exists?
      if (Files.exists(file))
        return urlOf(file);
    } catch (InvalidPathException ex) {
      /*
       * NOOP: `name` is not a file path, so we fall back to generic URL (or implicit classpath
       * resource).
       */
    }

    // URL.
    var uri = uri(name);
    // Generic URL?
    if (uri.isAbsolute()) {
      var ret = url(uri);
      return Uris.exists(ret) ? ret : null;
    }
    // Classpath (implicit, from relative URL).
    else
      return cl.getResource(name);
  }

  /**
   * Parses the {@link URL} corresponding to the given resource name.
   * <p>
   * Useful to convert textual references to resources (such as those coming from configuration
   * files or command-line options) to their canonical form.
   * </p>
   * <p>
   * For more information, see
   * {@linkplain #parseResourceUrl(String, ClassLoader, Function, FileSystem) main overload}.
   * </p>
   *
   * @param name
   *          Resource name (URL, filesystem path or classpath resource (possibly qualified by
   *          {@code "classpath:"} prefix)).
   * @param relativeFilePathResolver
   *          Relative filesystem path resolver. Converts relative paths to their absolute
   *          counterparts.
   * @return {@code null}, if the resource corresponding to {@code name} does not exist.
   */
  public static @Nullable URL parseResourceUrl(String name,
      Function<String, String> relativeFilePathResolver) {
    return parseResourceUrl(name, ClassLoader.getSystemClassLoader(), relativeFilePathResolver,
        FileSystems.getDefault());
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
   * @see #toArg(Iterable)
   */
  public static List<String> parseStrList(String s) {
    return parseList(s).collect(toList());
  }

  /**
   * Converts the given iterable to a semicolon-separated textual argument.
   *
   * @see #parseStrList(String)
   */
  public static String toArg(Iterable<?> o) {
    return toArg(o, Object::toString);
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
  public static <T> String toArg(Iterable<T> o, Function<T, String> mapper) {
    return Streams.of(o)
        .map(mapper)
        .collect(joining(S + SEMICOLON));
  }

  private Configs() {
  }
}
