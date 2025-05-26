/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Resource.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static org.pdfclown.common.util.io.AbstractResource.URI_SCHEME_PART__CLASSPATH;
import static org.pdfclown.common.util.net.Uris.uri;
import static org.pdfclown.common.util.net.Uris.url;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.net.Uris;

/**
 * Resource.
 * <p>
 * Unifies the handling of disparate kinds of resources (classpath, filesystem, web) to simplify
 * their management.
 * </p>
 * <p>
 * Resources are instantiated only if existing.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public interface Resource {
  /**
   * Gets the resource corresponding to the given file.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param file
   *          File.
   * @return {@code null}, if the resource corresponding to {@code file} does not exist.
   */
  public static @Nullable PathResource of(File file) {
    return (PathResource) of(file.toString());
  }

  /**
   * Gets the resource corresponding to the given path.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param path
   *          Path.
   * @return {@code null}, if the resource corresponding to {@code path} does not exist.
   */
  public static @Nullable PathResource of(Path path) {
    return (PathResource) of(path.toString());
  }

  /**
   * Gets the resource corresponding to the given name.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param name
   *          Resource name.
   * @return {@code null}, if the resource corresponding to {@code name} does not exist.
   */
  public static @Nullable Resource of(String name) {
    return of(name, Resource.class.getClassLoader());
  }

  /**
   * Gets the resource corresponding to the given name.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param name
   *          Resource name.
   * @param cl
   *          Class loader for resource lookup.
   * @return {@code null}, if the resource corresponding to {@code name} does not exist.
   */
  public static @Nullable Resource of(String name, ClassLoader cl) {
    return of(name, cl, Path::toAbsolutePath);
  }

  /**
   * Gets the resource corresponding to the given name.
   * <p>
   * Supported resource types:
   * </p>
   * <ul>
   * <li>classpath (either explicitly qualified via URI scheme ({@code "classpath:"}), or
   * automatically detected)</li>
   * <li>filesystem</li>
   * <li>generic URL</li>
   * </ul>
   *
   * @param name
   *          Resource name.
   * @param cl
   *          Class loader for resource lookup.
   * @param fileResolver
   *          Filesystem path resolver. Converts relative paths to their absolute counterparts.
   * @return {@code null}, if the resource corresponding to {@code name} does not exist.
   * @implNote Name resolution algorithm:
   *           <ol>
   *           <li>[<b>explicit classpath resource</b>] if {@code name} is prefixed by
   *           {@code "classpath:"}, it is resolved through {@code cl} and returned</li>
   *           <li>[<b>filesystem resource</b>] {@code name} is converted through
   *           {@code fileResolver} to an absolute filesystem path: if its node exists on
   *           {@code fs}, it is returned</li>
   *           <li>[<b>generic URL resource</b>] if {@code name} is an absolute URL, it is
   *           returned</li>
   *           <li>[<b>implicit classpath resource</b>] otherwise ({@code name} is a relative URL),
   *           it is resolved through {@code cl} and returned</li>
   *           </ol>
   *           <p>
   *           NOTE: In any case, the resolved resource is checked for existence before being
   *           returned.
   *           </p>
   */
  public static @Nullable Resource of(String name, ClassLoader cl,
      Function<Path, Path> fileResolver) {
    if (name.startsWith(URI_SCHEME_PART__CLASSPATH))
      // [explicit classpath resource]
      return ClasspathResource.of(name, cl);

    try {
      var file = Path.of(name);
      if (!file.isAbsolute()) {
        file = fileResolver.apply(file);
      }
      if (Files.exists(file))
        // [filesystem resource]
        return new FileResource(name, file);
    } catch (InvalidPathException | IOError ex) {
      /*
       * NOOP: `name` is not a file path, so we fall back to generic URL (or implicit classpath
       * resource).
       */
    }

    var uri = uri(name);
    if (uri != null && uri.isAbsolute()) {
      var ret = url(uri);
      // [generic URL resource]
      return ret != null && Uris.exists(ret) ? new WebResource(name, ret) : null;
    }

    // [implicit classpath resource]
    return ClasspathResource.of(name, cl);
  }

  /**
   * Gets the resource corresponding to the given name.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param name
   *          Resource name.
   * @param fileResolver
   *          Filesystem path resolver. Converts relative paths to their absolute counterparts.
   * @return {@code null}, if the resource corresponding to {@code name} does not exist.
   */
  public static @Nullable Resource of(String name, Function<Path, Path> fileResolver) {
    return of(name, Resource.class.getClassLoader(), fileResolver);
  }

  /**
   * Gets the resource corresponding to the given URL.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param url
   *          URL.
   * @return {@code null}, if the resource corresponding to {@code url} does not exist.
   */
  public static @Nullable Resource of(URL url) {
    return of(url.toString());
  }

  /**
   * Original name used to retrieve this resource.
   */
  String getName();

  /**
   * Location of this resource.
   */
  URL getUrl();

  /**
   * Opens a connection to this resource.
   *
   * @throws IOException
   */
  default InputStream openStream() throws IOException {
    return getUrl().openStream();
  }
}
