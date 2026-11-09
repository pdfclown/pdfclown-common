/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Resource.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block below this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Immutable;

/**
 * Resource.
 * <p>
 * Unifies the handling of disparate kinds of resources (classpath, filesystem, web) to simplify
 * their management.
 * </p>
 *
 * @author Stefano Chizzolini
 */
@Immutable
public interface Resource {
  /**
   * Gets the resource corresponding to a path.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param path
   *          Path.
   */
  static PathResource of(Path path) {
    return path(path.toString());
  }

  /**
   * Gets the resource corresponding to a name.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param name
   *          Resource name.
   */
  static Resource of(String name) {
    return of(name, null, null);
  }

  /**
   * Gets the resource corresponding to a name.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param name
   *          Resource name.
   * @param cl
   *          Class loader for resource lookup.
   */
  static Resource of(String name, ClassLoader cl) {
    return of(name, cl, null);
  }

  /**
   * Gets the resource corresponding to a name.
   * <p>
   * Supported resource types:
   * </p>
   * <ul>
   * <li>classpath (either explicitly qualified via URI scheme ({@code "classpath:"}), or
   * automatically detected)</li>
   * <li>filesystem (either explicitly qualified via URI scheme ({@code "file:"}), or automatically
   * detected)</li>
   * <li>generic URL</li>
   * </ul>
   *
   * @param name
   *          Resource name.
   * @param cl
   *          Class loader for resource lookup. If undefined, the caller's class loader is used.
   * @param fileResolver
   *          Resolves relative filesystem paths to their absolute representation. If undefined,
   *          they are resolved against the filesystem default directory (current working
   *          directory).
   * @implNote Resolution algorithm:
   *           <ol>
   *           <li><b>[explicit classpath resource]</b> if {@code name} is prefixed by
   *           {@code "classpath:"}, it is resolved through {@code cl} and returned</li>
   *           <li><b>[filesystem resource]</b> if {@code name} is prefixed by {@code "file:"}, it
   *           is resolved through {@code fileResolver} to an absolute filesystem path and
   *           returned</li>
   *           <li><b>[filesystem resource]</b> if {@code name}, resolved through
   *           {@code fileResolver} to an absolute filesystem path, exists, it is returned</li>
   *           <li><b>[URL resource]</b> if {@code name} is an absolute URL, it is returned</li>
   *           <li><b>[implicit classpath resource]</b> otherwise, {@code name} is resolved through
   *           {@code cl} and returned</li>
   *           </ol>
   */
  static Resource of(String name, @Nullable ClassLoader cl,
      @Nullable Function<Path, Path> fileResolver) {
    return AbstractResource.of(name, cl, fileResolver, null);
  }

  /**
   * Gets the resource corresponding to a URI.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   */
  static Resource of(URI uri) {
    return of(uri.toString());
  }

  /**
   * Gets the resource corresponding to a URL.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   */
  static Resource of(URL url) {
    return of(url.toString());
  }

  /**
   * Gets the resource corresponding to a name.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param name
   *          Resource name.
   */
  static PathResource path(String name) {
    return path(name, null, null);
  }

  /**
   * Gets the resource corresponding to a name.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param name
   *          Resource name.
   * @param cl
   *          Class loader for resource lookup.
   */
  static PathResource path(String name, ClassLoader cl) {
    return path(name, cl, null);
  }

  /**
   * Gets the resource corresponding to a name.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param name
   *          Resource name.
   * @param cl
   *          Class loader for resource lookup.
   * @param fileResolver
   *          Resolves relative filesystem paths to their absolute representation. If undefined,
   *          they are resolved against the filesystem default directory (current working
   *          directory).
   */
  static PathResource path(String name, @Nullable ClassLoader cl,
      @Nullable Function<Path, Path> fileResolver) {
    return (PathResource) of(name, cl, fileResolver);
  }

  /**
   * Canonical representation of this resource.
   */
  String asString();

  /**
   * Whether this resource exists.
   */
  boolean exists();

  /**
   * Original name used to retrieve this resource.
   */
  String getName();

  /**
   * Location of this resource.
   *
   * @throws IllegalStateException
   *           if this resource does not {@linkplain #exists() exist}.
   */
  URI getUri();

  /**
   * Opens a connection to this resource.
   *
   * @throws FileNotFoundException
   *           if this resource does not {@linkplain #exists() exist}.
   * @throws IOException
   *           if the connection failed.
   */
  default InputStream openStream() throws IOException {
    return getUri().toURL().openStream();
  }
}
