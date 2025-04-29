/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (ClasspathResource.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Strings.SLASH;

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
public class ClasspathResource extends AbstractResource implements PathResource {
  private static final String JAR_ENTRY_SEPARATOR = "!/";

  /*
   * TODO: cache soft refs!
   */
  private static Map<String, FileSystem> fileSystems = new HashMap<>();

  static @Nullable ClasspathResource of(String name, ClassLoader cl) {
    URL url;
    {
      int index = 0;
      if (name.startsWith(URI_SCHEME_PART__CLASSPATH)) {
        index = URI_SCHEME_PART__CLASSPATH.length();
      }
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

  private static FileSystem asFileSystem(Path path) {
    return fileSystems.computeIfAbsent(path.toString(),
        Failable.asFunction($k -> FileSystems.newFileSystem(path, null)));
  }

  private static String jarEntryName(String path) {
    return path.substring(path.indexOf(JAR_ENTRY_SEPARATOR) + JAR_ENTRY_SEPARATOR.length());
  }

  private static String jarFileName(String path) {
    return path.substring(URI_SCHEME_PART__FILE.length(), path.indexOf(JAR_ENTRY_SEPARATOR));
  }

  private final Path path;
  private final URL url;

  protected ClasspathResource(String name, URL url) {
    super(name);

    this.url = requireNonNull(url, "`url`");

    switch (url.getProtocol()) {
      case URI_SCHEME__JAR: {
        Path jarFile = Path.of(jarFileName(url.getPath()));
        String jarEntryName = jarEntryName(url.getPath());
        path = asFileSystem(jarFile).getPath(jarEntryName);
      }
        break;
      case URI_SCHEME__FILE:
        path = Path.of(url.getPath());
        break;
      default:
        throw unexpected("url.protocol", url.getProtocol());
    }
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public URL getUrl() {
    return url;
  }
}
