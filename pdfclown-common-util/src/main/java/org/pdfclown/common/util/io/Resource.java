/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Resource.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static org.pdfclown.common.util.function.Functions.toOrNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Immutable;

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
   * @return Empty, if the resource corresponding to {@code path} does not exist.
   */
  static Optional<PathResource> of(@Nullable Path path) {
    return path(toOrNull(path, Object::toString));
  }

  /**
   * Gets the resource corresponding to a name.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param name
   *          Resource name.
   * @return Empty, if the resource corresponding to {@code name} does not exist.
   */
  static Optional<Resource> of(@Nullable String name) {
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
   * @return Empty, if the resource corresponding to {@code name} does not exist.
   */
  static Optional<Resource> of(@Nullable String name, ClassLoader cl) {
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
   * @return Empty, if the resource corresponding to {@code name} does not exist.
   * @implNote {@code name} is nullable to accommodate methods like
   *           {@link Class#getResource(String)}. Resolution algorithm:
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
   *           <p>
   *           NOTE: In any case, the resolved resource is checked for existence before being
   *           returned.
   *           </p>
   *           <p>
   *           </p>
   */
  static Optional<Resource> of(@Nullable String name, @Nullable ClassLoader cl,
      @Nullable Function<Path, Path> fileResolver) {
    return AbstractResource.of(name, cl, fileResolver, null);
  }

  /**
   * Gets the resource corresponding to a URI.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @return Empty, if the resource corresponding to {@code url} does not exist.
   */
  static Optional<Resource> of(@Nullable URI uri) {
    return of(toOrNull(uri, Object::toString));
  }

  /**
   * Gets the resource corresponding to a URL.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @return Empty, if the resource corresponding to {@code url} does not exist.
   */
  static Optional<Resource> of(@Nullable URL url) {
    return of(toOrNull(url, Object::toString));
  }

  /**
   * Gets the resource corresponding to a name.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param name
   *          Resource name.
   * @return Empty, if the resource corresponding to {@code name} does not exist.
   */
  static Optional<PathResource> path(@Nullable String name) {
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
   * @return Empty, if the resource corresponding to {@code name} does not exist.
   */
  static Optional<PathResource> path(@Nullable String name, ClassLoader cl) {
    return path(name, cl, null);
  }

  static Optional<PathResource> path(@Nullable String name, @Nullable ClassLoader cl,
      @Nullable Function<Path, Path> fileResolver) {
    return of(name, cl, fileResolver).map(PathResource.class::cast);
  }

  /**
   * Canonical representation of this resource.
   */
  String asString();

  /**
   * Original name used to retrieve this resource.
   */
  String getName();

  /**
   * Location of this resource.
   */
  URI getUri();

  /**
   * Opens a connection to this resource.
   */
  default InputStream openStream() throws IOException {
    return getUri().toURL().openStream();
  }
}
