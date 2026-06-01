/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (AbstractResource.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Chars.SLASH;
import static org.pdfclown.common.util.Objects.opt;
import static org.pdfclown.common.util.Objects.toStringWithValues;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.internal.Internals.ROOT_PACKAGE;
import static org.pdfclown.common.util.io.Files.path;
import static org.pdfclown.common.util.net.Uris.SCHEME__CLASSPATH;
import static org.pdfclown.common.util.net.Uris.SCHEME__FILE;
import static org.pdfclown.common.util.net.Uris.scheme;
import static org.pdfclown.common.util.net.Uris.url;
import static org.pdfclown.common.util.reflect.Reflects.stackFrame;

import java.io.IOError;
import java.lang.StackWalker.StackFrame;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Immutable;
import org.pdfclown.common.util.net.Uris;

/**
 * {@link Resource} base implementation.
 *
 * @author Stefano Chizzolini
 */
@Immutable
public abstract class AbstractResource implements Resource {
  /**
   * (see {@link Resource#of(String, ClassLoader, Function)})
   */
  static Optional<Resource> of(@Nullable String name, @Nullable ClassLoader cl,
      @Nullable Function<Path, Path> fileResolver, @Nullable FileSystem fs) {
    if (name == null)
      return opt(null);

    if (fs == null) {
      fs = FileSystems.getDefault();
    }

    var scheme = scheme(requireNonNull(name, "`name`"));
    switch (scheme) {
      case SCHEME__CLASSPATH -> {
        // [explicit classpath resource]
        return ofClasspath(name.substring(scheme.length() + 1), cl, fs);
      }
      case SCHEME__FILE -> {
        // [filesystem resource]
        try {
          Path file = path(new URI(name), fs);
          return opt(Files.exists(file) ? new FileResource(name, file) : null);
        } catch (URISyntaxException ex) {
          // NOP
        }
      }
      default -> {
        // NOP
      }
    }

    try {
      Path file = fs.getPath(name);
      if (!file.isAbsolute()) {
        if (fileResolver == null) {
          fileResolver = Path::toAbsolutePath;
        }
        file = fileResolver.apply(file);
      }
      if (Files.exists(file))
        // [filesystem resource]
        return opt(new FileResource(name, file));
    } catch (InvalidPathException | IOError ex) {
      // NOP
    }

    try {
      var uri = new URI(name);
      if (uri.isAbsolute())
        // [URL resource]
        return opt(Uris.exists(url(uri)) ? new WebResource(name, uri) : null);
    } catch (URISyntaxException ex) {
      // NOP
    }

    // [implicit classpath resource]
    return ofClasspath(name, cl, fs);
  }

  /**
   * @param name
   *          Resource name WITHOUT scheme.
   * @implNote Leading slash is stripped from {@code name} (see also
   *           {@link Class#getResource(String)}).
   */
  private static Optional<Resource> ofClasspath(String name, @Nullable ClassLoader cl,
      FileSystem fs) {
    if (cl == null) {
      cl = stackFrame($ -> !$.getClassName()
          .startsWith(ROOT_PACKAGE) /* Ignores any intermediate caller within this library */)
              .map(StackFrame::getDeclaringClass)
              .orElseThrow().getClassLoader();
    }

    URL url;
    {
      // Strip leading slash!
      if (name.startsWith(S + SLASH)) {
        name = name.substring(1);
      }

      url = cl.getResource(name);
    }
    return opt(url != null ? new ClasspathResource(name, url, fs) : null);
  }

  private final String name;

  protected AbstractResource(String name) {
    this.name = requireNonNull(name, "`name`");
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return toStringWithValues(this, getName(), getUri());
  }
}
