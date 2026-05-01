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
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.net.Uris.SCHEME__CLASSPATH;
import static org.pdfclown.common.util.net.Uris.url;

import java.io.IOError;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
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
  static @Nullable Resource of(@Nullable String name, ClassLoader cl,
      Function<Path, Path> fileResolver, FileSystem fs) {

    if (name == null)
      return null;
    else if (name.startsWith(SCHEME__CLASSPATH + COLON))
      // [explicit classpath resource]
      return ClasspathResource.of(name, cl, fs);

    try {
      var file = fs.getPath(name);
      if (!file.isAbsolute()) {
        file = fileResolver.apply(file);
      }
      if (Files.exists(file))
        // [filesystem resource]
        return new FileResource(name, file);
    } catch (InvalidPathException | IOError ex) {
      // FALLTHRU
    }

    try {
      URI uri = new URI(name);
      if (uri.isAbsolute())
        // [URL resource]
        return Uris.exists(url(uri)) ? new WebResource(name, uri) : null;
    } catch (URISyntaxException ex) {
      // FALLTHRU
    }

    // [implicit classpath resource]
    return ClasspathResource.of(name, cl, fs);
  }

  private final String name;

  protected AbstractResource(String name) {
    this.name = requireNonNull(name, "`name`");
  }

  @Override
  public String getName() {
    return name;
  }
}
