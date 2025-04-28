/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (Files.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.writeString;
import static java.util.stream.Collectors.joining;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Strings.BACKSLASH;
import static org.pdfclown.common.util.Strings.DOT;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.INDEX__NOT_FOUND;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.Strings.SLASH;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.stream.Streams;

/**
 * File utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Files {
  /**
   * Full file extension pattern.
   * <p>
   * A greedy sequence of one or more extensions, each beginning with a dot, followed by a
   * non-digit, followed by one or more characters different from dot (extension separator), slash
   * (Unix directory separator) or back-slash (Windows directory separator).
   * </p>
   */
  private static final Pattern PATTERN__FULL_EXTENSION = Pattern.compile("(\\.\\D[^\\.\\\\/]+)+$");

  public static final String FILE_EXTENSION__EPS = ".eps";
  public static final String FILE_EXTENSION__HTML = ".html";
  public static final String FILE_EXTENSION__JAVA = ".java";
  public static final String FILE_EXTENSION__JPG = ".jpg";
  public static final String FILE_EXTENSION__PDF = ".pdf";
  public static final String FILE_EXTENSION__PNG = ".png";
  public static final String FILE_EXTENSION__SVG = ".svg";
  public static final String FILE_EXTENSION__XML = ".xml";
  public static final String FILE_EXTENSION__XSD = ".xsd";
  public static final String FILE_EXTENSION__XSL = ".xsl";
  public static final String FILE_EXTENSION__ZIP = ".zip";
  public static final String FILE_EXTENSION__JSON_ZIP = ".json" + FILE_EXTENSION__ZIP;

  public static final String PATH_SUPER = S + DOT + DOT;

  /**
   * Gets the simple extension of the given path.
   * <p>
   * Contrary to {@link FilenameUtils#getExtension(String)}, <i>the extension is prefixed by
   * dot</i>.
   * </p>
   *
   * @return Empty, if no extension.
   * @see #fullExtension(String)
   */
  public static String extension(String path) {
    int extensionIndex = FilenameUtils.indexOfExtension(path);
    return extensionIndex != INDEX__NOT_FOUND ? path.substring(extensionIndex) : EMPTY;
  }

  /**
   * Gets the last part of the given path.
   */
  public static String fileName(String path) {
    return FilenameUtils.getName(path);
  }

  /**
   * Converts the given URI to the corresponding path.
   * <p>
   * <i>Contrary to {@link File#File(URI) new File(URI)}, this function supports also <b>relative
   * URIs</b></i>, remedying the limitation of the standard API which rejects URIs missing their
   * scheme.
   * </p>
   */
  public static File fileOf(URI uri) {
    return pathOf(uri).toFile();
  }

  /**
   * Converts the given URL to the corresponding path.
   * <p>
   * <i>Contrary to {@link File#File(URI) new File(URI)}, this function supports also <b>relative
   * URLs</b></i>, remedying the limitation of the standard API which rejects URIs missing their
   * scheme.
   * </p>
   */
  public static File fileOf(URL url) {
    return pathOf(url).toFile();
  }

  /**
   * Gets the full extension of the given path.
   * <p>
   * Any dot-prefixed tailing part which doesn't begin with a digit is included in the extension;
   * therefore, composite extensions (eg, ".tar.gz") are recognized, while version codes are ignored
   * (eg, "commons-io-2.8.0.jar" returns ".jar", NOT ".8.0.jar").
   * </p>
   *
   * @return Empty, if no extension.
   * @see #extension(String)
   */
  public static String fullExtension(String path) {
    Matcher m = PATTERN__FULL_EXTENSION.matcher(path);
    return m.find() ? m.group() : "";
  }

  /**
   * Gets whether the extension of the given path corresponds to the specified one (case
   * insensitive).
   * <p>
   * NOTE: Contrary to {@link FilenameUtils#isExtension(String, String)}, <i>the extension is
   * prefixed by dot and the match is case insensitive</i>.
   * </p>
   *
   * @see #isFullExtension(String, String)
   */
  public static boolean isExtension(final String path, final String extension) {
    return extension(path).equalsIgnoreCase(extension);
  }

  /**
   * Gets whether the full extension of the given path corresponds to the specified one (case
   * insensitive).
   *
   * @see #isExtension(String, String)
   */
  public static boolean isFullExtension(final String path, final String extension) {
    return fullExtension(path).equalsIgnoreCase(extension);
  }

  /**
   * Converts the given URI to the corresponding path.
   * <p>
   * <i>Contrary to {@link Path#of(URI)}, this function supports also <b>relative URIs</b></i>,
   * remedying the limitation of the standard API which rejects URIs missing their scheme.
   * </p>
   */
  public static Path pathOf(URI uri) {
    return pathOf(uri, FileSystems.getDefault());
  }

  /**
   * Converts the given URL to the corresponding path.
   * <p>
   * <i>Contrary to {@link Path#of(URI)}, this function supports also <b>relative URLs</b></i>,
   * remedying the limitation of the standard API which rejects URIs missing their scheme.
   * </p>
   *
   * @throws IllegalArgumentException
   *           if {@code url} is an invalid URI.
   */
  public static Path pathOf(URL url) {
    try {
      return pathOf(url.toURI());
    } catch (URISyntaxException ex) {
      throw wrongArg("url", url, null, ex);
    }
  }

  /**
   * Gets the relative path from the given file to the other one.
   */
  public static String relativePath(File from, File to) {
    if (from.isFile())
      return relativePath(from.getParentFile(), to);
    else if (from.exists() && !from.isDirectory())
      throw wrongArg("from", from, "NOT a directory");

    try {
      return from.getCanonicalFile().toPath().relativize(to.getCanonicalFile().toPath()).toString();
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }

  /**
   * Replaces the {@linkplain #fullExtension(String) full extension} of the given path with the
   * given extension.
   */
  public static String replaceFullExtension(String path, String newExtension) {
    Matcher m = PATTERN__FULL_EXTENSION.matcher(path);
    return (m.find() ? path.substring(0, m.start()) : path) + newExtension;
  }

  /**
   * Cleans the given directory, or creates it if not existing.
   *
   * @return {@code dir}
   */
  public static File resetDir(File dir) throws IOException {
    if (dir.exists()) {
      FileUtils.cleanDirectory(dir);
    } else {
      dir.mkdirs();
    }
    return dir;
  }

  /**
   * Gets the simple base name (ie, the filename without {@linkplain #fullExtension(String) full
   * extension}) of the given path.
   *
   * @see FilenameUtils#getBaseName(String)
   */
  public static String simpleBaseName(String path) {
    String fileName = fileName(path);
    return fileName.substring(0, fileName.length() - fullExtension(fileName).length());
  }

  /**
   * Converts the given path to the corresponding URI.
   * <p>
   * <i>Contrary to {@link File#toURI()}, this function supports also <b>relative URIs</b></i>,
   * remedying the limitation of the standard API which forcibly resolves relative paths as absolute
   * URIs against the current user directory. On the other hand, absolute paths are normalized
   * before being converted.
   * </p>
   */
  public static URI uriOf(File path) {
    return uriOf(path.toPath());
  }

  /**
   * Converts the given path to the corresponding URI.
   * <p>
   * <i>Contrary to {@link Path#toUri()}, this function supports also <b>relative URIs</b></i>,
   * remedying the limitation of the standard API which forcibly resolves relative paths as absolute
   * URIs against the current user directory. On the other hand, absolute paths are normalized
   * before being converted.
   * </p>
   */
  public static URI uriOf(Path path) {
    return path.isAbsolute()
        ? path.normalize().toUri()
        : URI.create(Streams.of(path)
            .map(Path::toString)
            .collect(joining(S + SLASH) /*
                                         * Forces the URI separator instead of the default
                                         * filesystem separator
                                         */));
  }

  /**
   * Converts the given path to the corresponding URL.
   *
   * @throws IllegalArgumentException
   *           if {@code path} is relative.
   */
  public static URL urlOf(File path) {
    // TODO: support relative paths
    return urlOf(path.toPath());
  }

  /**
   * Converts the given path to the corresponding URL.
   *
   * @throws IllegalArgumentException
   *           if {@code path} is relative.
   */
  public static URL urlOf(Path path) {
    // TODO: support relative paths
    try {
      return uriOf(path).toURL();
    } catch (MalformedURLException ex) {
      throw wrongArg("path", path, ex);
    }
  }

  /**
   * Gets the given path without its {@linkplain #extension(String) extension}.
   */
  public static String withoutExtension(String path) {
    int extensionPos = FilenameUtils.indexOfExtension(path);
    return extensionPos >= 0 ? path.substring(0, extensionPos) : path;
  }

  /**
   * Gets the given path without its {@linkplain #fullExtension(String) full extension}.
   */
  public static String withoutFullExtension(String path) {
    return path.substring(0, path.length() - fullExtension(path).length());
  }

  /**
   * Writes the given string to the file, encoded in UTF-8 charset; any non-existent parent
   * directory is automatically created.
   */
  public static void writeTo(File file, CharSequence content) throws IOException {
    writeTo(file.toPath(), content);
  }

  /**
   * Writes the given string to the file, encoded in UTF-8 charset; any non-existent parent
   * directory is automatically created.
   */
  public static void writeTo(Path path, CharSequence content) throws IOException {
    createDirectories(path.getParent());
    writeString(path, content);
  }

  static Path pathOf(URI uri, FileSystem fs) {
    // Absolute URI?
    if (uri.isAbsolute()) {
      if (!uri.getScheme().equalsIgnoreCase("file"))
        throw wrongArg("uri", uri, "MUST be a 'file:' URI");

      var b = new StringBuilder();
      // Windows-like?
      if (fs.getSeparator().equals(S + BACKSLASH)) {
        String s;
        // Host.
        if ((s = uri.getAuthority()) != null) {
          b.append(fs.getSeparator() + fs.getSeparator()).append(uri.getAuthority())
              .append(fs.getSeparator());
        }
        // Path.
        s = uri.getPath();
        {
          // Remove leading slashes!
          int i = 0;
          while (i < s.length() && s.charAt(i) == SLASH) {
            i++;
          }
          if (i > 0) {
            s = s.substring(i);
          }
          b.append(s);
        }
      }
      // Unix-like.
      else {
        String s;
        // Host.
        if ((s = uri.getAuthority()) != null) {
          b.append(fs.getSeparator()).append(s);
        }
        // Path.
        b.append(uri.getPath());
      }
      return fs.getPath(b.toString());
    }
    // Relative URI.
    else
      return fs.getPath(EMPTY, uri.toString().split(S + SLASH));
  }

  private Files() {
  }
}
