/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ClasspathResource.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.SLASH;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Objects.INDEX__NOT_FOUND;
import static org.pdfclown.common.util.Objects.isSameType;
import static org.pdfclown.common.util.net.Uris.SCHEME__CLASSPATH;
import static org.pdfclown.common.util.net.Uris.SCHEME__FILE;
import static org.pdfclown.common.util.net.Uris.SCHEME__JAR;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.function.Failable;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Immutable;

/**
 * Classpath resource.
 * <p>
 * Can be either a <b>simple file</b> (in case of filesystem resources, typical of IDE debugging
 * environments) or an <b>entry in an artifact jar</b> (typical of ordinary execution environments).
 * </p>
 * <p>
 * <b>Directories</b> are transparently handled no matter whether they are plain filesystem nodes or
 * jar entries: they can be {@linkplain Files#newDirectoryStream(Path) listed} and recursively
 * {@linkplain Files#walkFileTree(Path, java.nio.file.FileVisitor) walked}. The only limitation is
 * the impossibility to {@linkplain Path#resolve(Path) directly resolve} relativized walked files
 * into the physical filesystem, as they belong to a separate filesystem (otherwise a
 * {@link ProviderMismatchException} is thrown); nonetheless, the workaround is pretty simple:
 * {@linkplain Path#resolve(String) resolve the string representation} of the walked file instead.
 * </p>
 *
 * @author Stefano Chizzolini
 */
@Immutable
public class ClasspathResource extends AbstractResource implements PathResource {
  /**
   * JAR URL separator (see {@link java.net.JarURLConnection}).
   */
  private static final String JAR_URL_SEPARATOR = "!/";

  /*
   * TODO: cache soft refs!
   */
  private static final Map<String, FileSystem> fileSystems = new HashMap<>();

  static @Nullable ClasspathResource of(String name, ClassLoader cl) {
    URL url;
    {
      int index = name.indexOf(COLON);
      // Strip classpath scheme!
      if (index != INDEX__NOT_FOUND && name.substring(0, index).equals(SCHEME__CLASSPATH)) {
        index++;
      } else {
        index = 0;
      }
      // Strip leading slash!
      if (name.charAt(index) == SLASH) {
        index++;
      }
      if (index > 0) {
        name = name.substring(index);
      }
      url = cl.getResource(name);
    }
    return url != null ? new ClasspathResource(name, url) : null;
  }

  @SuppressWarnings("RedundantCast" /*
                                     * NOTE: Since Java 13, `ClassLoader` cast is mandatory because
                                     * of overload ambiguity
                                     */)
  private static FileSystem asFileSystem(Path path) {
    return fileSystems.computeIfAbsent(path.toString(),
        Failable.asFunction($k -> FileSystems.newFileSystem(path, (ClassLoader) null)));
  }

  private static String jarEntryName(String path) {
    return path.substring(path.indexOf(JAR_URL_SEPARATOR) + JAR_URL_SEPARATOR.length());
  }

  private static String jarFileName(String path) {
    return path.substring((SCHEME__FILE + COLON).length(), path.indexOf(JAR_URL_SEPARATOR));
  }

  private final Path path;
  private final URI uri;

  protected ClasspathResource(String name, URL url) {
    super(name);

    try {
      this.uri = requireNonNull(url, "`url`").toURI();
    } catch (URISyntaxException ex) {
      throw runtime(ex);
    }

    switch (uri.getScheme()) {
      case SCHEME__JAR: {
        /*
         * NOTE: We have to access `URL.path` instead of `URI.path`, as the latter returns `null`
         * since URI treats JAR protocol as opaque (that is, non-hierarchical).
         */
        Path jarFile = Path.of(jarFileName(url.getPath()));
        String jarEntryName = jarEntryName(url.getPath());
        path = asFileSystem(jarFile).getPath(jarEntryName);
      }
        break;
      case SCHEME__FILE:
        path = Path.of(url.getPath());
        break;
      default:
        throw unexpected("uri.scheme", uri.getScheme());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    else if (!isSameType(this, o))
      return false;

    var that = (ClasspathResource) o;
    return this.uri.equals(that.uri);
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public URI getUri() {
    return uri;
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }

  @Override
  public String toString() {
    return uri.toString();
  }
}
