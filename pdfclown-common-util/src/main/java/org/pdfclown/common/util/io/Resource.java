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

import static org.pdfclown.common.util.function.Functions.to;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
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
 * <p>
 * Resources are instantiated only if existing.
 * </p>
 *
 * @author Stefano Chizzolini
 */
@Immutable
public interface Resource {
  /**
   * Gets the resource corresponding to the path.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param path
   *          Path.
   * @return {@code null}, if the resource corresponding to {@code path} does not exist.
   */
  static @Nullable PathResource of(@Nullable Path path) {
    return (PathResource) of(to(path, Object::toString));
  }

  /**
   * Gets the resource corresponding to the name.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @param name
   *          Resource name.
   * @return {@code null}, if the resource corresponding to {@code name} does not exist.
   */
  static @Nullable Resource of(@Nullable String name) {
    return of(name, Resource.class.getClassLoader());
  }

  /**
   * Gets the resource corresponding to the name.
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
  static @Nullable Resource of(@Nullable String name, ClassLoader cl) {
    return of(name, cl, Path::toAbsolutePath);
  }

  /**
   * Gets the resource corresponding to the name.
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
   * @throws IllegalArgumentException
   *           if {@code name} is an invalid URL.
   * @implNote Name resolution algorithm:
   *           <ol>
   *           <li><b>[explicit classpath resource]</b> if {@code name} is prefixed by
   *           {@code "classpath:"}, it is resolved through {@code cl} and returned</li>
   *           <li><b>[filesystem resource]</b> if {@code name}, resolved through
   *           {@code fileResolver} to an absolute filesystem path, exists, it is returned</li>
   *           <li><b>[URL resource]</b> if {@code name} is an absolute URI, it is converted to URL
   *           and returned</li>
   *           <li><b>[implicit classpath resource]</b> otherwise, {@code name} is resolved through
   *           {@code cl} and returned</li>
   *           </ol>
   *           <p>
   *           NOTE: In any case, the resolved resource is checked for existence before being
   *           returned.
   *           </p>
   */
  static @Nullable Resource of(@Nullable String name, ClassLoader cl,
      Function<Path, Path> fileResolver) {
    return AbstractResource.of(name, cl, fileResolver, FileSystems.getDefault());
  }

  /**
   * Gets the resource corresponding to the name.
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
  static @Nullable Resource of(@Nullable String name, Function<Path, Path> fileResolver) {
    return of(name, Resource.class.getClassLoader(), fileResolver);
  }

  /**
   * Gets the resource corresponding to the URI.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @return {@code null}, if the resource corresponding to {@code url} does not exist.
   */
  static @Nullable Resource of(@Nullable URI uri) {
    return of(to(uri, Object::toString));
  }

  /**
   * Gets the resource corresponding to the URL.
   * <p>
   * For more information, see {@linkplain #of(String, ClassLoader, Function) main overload}.
   * </p>
   *
   * @return {@code null}, if the resource corresponding to {@code url} does not exist.
   */
  static @Nullable Resource of(@Nullable URL url) {
    return of(to(url, Object::toString));
  }

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
