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

import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.net.Uris.SCHEME__FILE;
import static org.pdfclown.common.util.net.Uris.SCHEME__JAR;
import static org.pdfclown.common.util.net.Uris.isLocalFileSystem;
import static org.pdfclown.common.util.net.Uris.scheme;
import static org.pdfclown.common.util.net.Uris.uri;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.function.Failable;
import org.pdfclown.common.util.annot.Immutable;
import org.pdfclown.common.util.net.Uris;
import org.pdfclown.common.util.net.Uris.JarUrl;

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
 * the impossibility to {@linkplain Path#resolve(Path) directly resolve} walked files into the
 * physical filesystem, as they belong to a separate filesystem (otherwise a
 * {@link ProviderMismatchException} is thrown); nonetheless, the workaround is pretty simple:
 * {@linkplain Path#resolve(String) resolve the string representation} of the walked files instead.
 * </p>
 *
 * @author Stefano Chizzolini
 */
@Immutable
public class ClasspathResource extends AbstractResource implements PathResource {
  private static final Map<String, FileSystem> fileSystems = new HashMap<>();

  private static FileSystem asFileSystem(Path path) {
    return fileSystems.computeIfAbsent(path.toString(),
        Failable.asFunction($k -> FileSystems.newFileSystem(path, (ClassLoader) null)));
  }

  private final Path path;
  private final URI uri;

  /**
   * @throws org.pdfclown.common.util.ArgumentException
   *           if {@code url} is not local or its scheme is incompatible (supported:
   *           {@value Uris#SCHEME__FILE}, {@value Uris#SCHEME__JAR}).
   */
  protected ClasspathResource(String name, URL url, FileSystem fs) {
    super(name);

    this.uri = uri(url);

    if (!isLocalFileSystem(uri))
      /*
       * NOTE: A `jar:` URL (for example, returned via `URLClassLoader`) may be remote
       * ("jar:https://...!/...") even though the `classpath:` syntax it was resolved from looks
       * purely local.
       */
      throw wrongArg("url", url, "only local filesystem allowed");

    switch (scheme(uri)) {
      case SCHEME__JAR -> {
        var jarUrl = JarUrl.of(url);
        Path jarFile = fs.getPath(jarUrl.jarFileUrl().getPath());
        FileSystem jarFS = asFileSystem(jarFile);
        path = jarUrl.entryName() != null
            ? jarFS.getPath(jarUrl.entryName())
            : jarFS.getRootDirectories().iterator().next();
      }
      case SCHEME__FILE -> path = fs.getPath(url.getPath());
      default -> throw wrongArg("uri.scheme", uri.getScheme());
    }
  }

  @Override
  public String asString() {
    return uri.toString();
  }

  /**
   * @implNote Marked as final to enforce equivalence symmetry.
   */
  @Override
  public final boolean equals(Object o) {
    return this == o || (o instanceof ClasspathResource that
        && this.uri.equals(that.uri));
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public URI getUri() {
    return uri;
  }

  /**
   * @implNote Marked as final to enforce equivalence symmetry.
   */
  @Override
  public final int hashCode() {
    return uri.hashCode();
  }
}
