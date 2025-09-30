/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Files.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Strings.BACKSLASH;
import static org.pdfclown.common.util.Strings.DOT;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.Strings.SLASH;
import static org.pdfclown.common.util.Strings.found;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

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
  private static final Pattern PATTERN__FULL_EXTENSION = Pattern.compile("(\\.\\D[^.\\\\/]+)+$");

  public static final String FILE_EXTENSION__CSS = ".css";
  public static final String FILE_EXTENSION__GROOVY = ".groovy";
  public static final String FILE_EXTENSION__HTML = ".html";
  public static final String FILE_EXTENSION__JAVA = ".java";
  public static final String FILE_EXTENSION__JAVASCRIPT = ".js";
  public static final String FILE_EXTENSION__JPG = ".jpg";
  public static final String FILE_EXTENSION__PDF = ".pdf";
  public static final String FILE_EXTENSION__PNG = ".png";
  public static final String FILE_EXTENSION__SVG = ".svg";
  public static final String FILE_EXTENSION__XML = ".xml";
  public static final String FILE_EXTENSION__XSD = ".xsd";
  public static final String FILE_EXTENSION__XSL = ".xsl";
  public static final String FILE_EXTENSION__ZIP = ".zip";

  public static final String PATH_SUPER = S + DOT + DOT;

  /**
   * Gets the age of the file.
   */
  public static Duration age(Path file) {
    return Duration.ofMillis(System.currentTimeMillis() - file.toFile().lastModified());
  }

  /**
   * Gets the base name (that is, the filename without {@linkplain #extension(Path) extension}) of
   * the path.
   *
   * @see #simpleBaseName(Path)
   */
  public static String baseName(Path path) {
    return baseName(path.getFileName().toString());
  }

  /**
   * Gets the base name (that is, the filename without {@linkplain #extension(String) extension}) of
   * the path.
   *
   * @see #simpleBaseName(String)
   */
  public static String baseName(String path) {
    return FilenameUtils.getBaseName(path);
  }

  /**
   * Recursively copies the source directory to the target.
   * <p>
   * NOTE: This method differs from
   * {@link org.apache.commons.io.file.PathUtils#copyDirectory(Path, Path, java.nio.file.CopyOption...)}
   * in its return value.
   * </p>
   *
   * @return {@code targetDir}
   */
  public static Path copyDirectory(Path sourceDir, Path targetDir) throws IOException {
    FileUtils.copyDirectory(sourceDir.toFile(), targetDir.toFile());
    return targetDir;
  }

  /**
   * Gets the simple extension of the path.
   * <p>
   * Contrary to {@link FilenameUtils#getExtension(String)}, the extension is prefixed by dot.
   * </p>
   *
   * @return Empty, if no extension.
   * @see #fullExtension(Path)
   */
  public static String extension(Path path) {
    return extension(path.getFileName().toString());
  }

  /**
   * Gets the simple extension of the path.
   * <p>
   * Contrary to {@link FilenameUtils#getExtension(String)}, the extension is prefixed by dot.
   * </p>
   *
   * @return Empty, if no extension.
   * @see #fullExtension(String)
   */
  public static String extension(String path) {
    int extensionIndex = FilenameUtils.indexOfExtension(path);
    return found(extensionIndex) ? path.substring(extensionIndex) : EMPTY;
  }

  /**
   * Gets the last part of the path.
   */
  public static String filename(Path path) {
    return path.getFileName().toString();
  }

  /**
   * Gets the last part of the path.
   */
  public static String filename(String path) {
    return FilenameUtils.getName(path);
  }

  /**
   * Gets the full extension of the path.
   * <p>
   * Any dot-prefixed tailing part which doesn't begin with a digit is included in the extension;
   * therefore, composite extensions (for example, {@code ".tar.gz"}) are recognized, whilst version
   * codes are ignored (for example, {@code "commons-io-2.8.0.jar"} returns {@code ".jar"}, NOT
   * {@code ".8.0.jar"}).
   * </p>
   *
   * @return Empty, if no extension.
   * @see #extension(Path)
   */
  public static String fullExtension(Path path) {
    return fullExtension(path.getFileName().toString());
  }

  /**
   * Gets the full extension of the path.
   * <p>
   * Any dot-prefixed tailing part which doesn't begin with a digit is included in the extension;
   * therefore, composite extensions (for example, {@code ".tar.gz"}) are recognized, whilst version
   * codes are ignored (for example, {@code "commons-io-2.8.0.jar"} returns {@code ".jar"}, NOT
   * {@code ".8.0.jar"}).
   * </p>
   *
   * @return Empty, if no extension.
   * @see #extension(String)
   */
  public static String fullExtension(String path) {
    Matcher m = PATTERN__FULL_EXTENSION.matcher(path);
    return m.find() ? m.group() : EMPTY;
  }

  /**
   * Gets whether the extension of the path corresponds to the given one (case-insensitive).
   * <p>
   * NOTE: Contrary to {@link FilenameUtils#isExtension(String, String)}, the extension is prefixed
   * by dot and the match is case-insensitive.
   * </p>
   *
   * @see #isFullExtension(Path, String)
   */
  public static boolean isExtension(final Path path, final String extension) {
    return isExtension(path.getFileName().toString(), extension);
  }

  /**
   * Gets whether the extension of the path corresponds to the given one (case-insensitive).
   * <p>
   * NOTE: Contrary to {@link FilenameUtils#isExtension(String, String)}, the extension is prefixed
   * by dot and the match is case-insensitive.
   * </p>
   *
   * @see #isFullExtension(String, String)
   */
  public static boolean isExtension(final String path, final String extension) {
    return extension(path).equalsIgnoreCase(extension);
  }

  /**
   * Gets whether the URI belongs to the {@code file} scheme.
   * <p>
   * NOTE: Undefined scheme is assimilated to the {@code file} scheme.
   * </p>
   */
  public static boolean isFile(URI uri) {
    /*
     * NOTE: Scheme is case-insensitive.
     */
    return uri.getScheme() == null || uri.getScheme().equalsIgnoreCase("file");
  }

  /**
   * Gets whether the full extension of the path corresponds to the given one (case-insensitive).
   *
   * @see #isExtension(Path, String)
   */
  public static boolean isFullExtension(final Path path, final String extension) {
    return isFullExtension(path.getFileName().toString(), extension);
  }

  /**
   * Gets whether the full extension of the path corresponds to the given one (case-insensitive).
   *
   * @see #isExtension(String, String)
   */
  public static boolean isFullExtension(final String path, final String extension) {
    return fullExtension(path).equalsIgnoreCase(extension);
  }

  /**
   * Normalizes the path to its absolute form.
   */
  public static Path normal(Path path) {
    return path.toAbsolutePath().normalize();
  }

  /**
   * Converts the URI to the corresponding path.
   * <p>
   * Contrary to {@link Path#of(URI)}, this function supports also relative URIs, remedying the
   * limitation of the standard API which rejects URIs missing their scheme.
   * </p>
   *
   * @throws IllegalArgumentException
   *           if {@code uri} does not represent a file (that is, its scheme is neither {@code file}
   *           nor undefined).
   */
  public static Path path(URI uri) {
    return path(uri, FileSystems.getDefault());
  }

  /**
   * Converts the URL to the corresponding path.
   * <p>
   * Contrary to {@link Path#of(URI)}, this function supports also relative URLs, remedying the
   * limitation of the standard API which rejects URIs missing their scheme.
   * </p>
   *
   * @throws IllegalArgumentException
   *           if {@code uri} does not represent a file (that is, its scheme is neither {@code file}
   *           nor undefined).
   */
  public static Path path(URL url) {
    try {
      return path(url.toURI());
    } catch (URISyntaxException ex) {
      throw wrongArg("url", url, null, ex);
    }
  }

  /**
   * Gets the relative path from the file to the other one.
   *
   * @throws IllegalArgumentException
   *           if {@code from} does not exist.
   */
  public static Path relativePath(Path from, Path to) {
    if (isRegularFile(from = normal(from))) {
      from = from.getParent();
    }
    if (!isDirectory(from))
      throw wrongArg("from", from, "MUST be a directory");

    return from.relativize(normal(to));
  }

  /**
   * Cleans the directory, or creates it if not existing.
   *
   * @return {@code dir}
   */
  public static Path resetDirectory(Path dir) throws IOException {
    if (exists(dir)) {
      /*
       * IMPORTANT: DO NOT use `PathUtils.cleanDirectory(..)`, as it doesn't delete subdirectories
       * (weird asymmetry!).
       */
      FileUtils.cleanDirectory(dir.toFile());
    } else {
      createDirectories(dir);
    }
    return dir;
  }

  /**
   * Gets the simple base name (that is, the filename without {@linkplain #fullExtension(Path) full
   * extension}) of the path.
   *
   * @see #baseName(Path)
   */
  public static String simpleBaseName(Path path) {
    return simpleBaseName(path.getFileName().toString());
  }

  /**
   * Gets the simple base name (that is, the filename without {@linkplain #fullExtension(String)
   * full extension}) of the path.
   *
   * @see #baseName(String)
   */
  public static String simpleBaseName(String path) {
    String filename = filename(path);
    return filename.substring(0, filename.length() - fullExtension(filename).length());
  }

  /**
   * Gets the size of the file.
   *
   * @return The length (bytes) of the file, or {@code 0} if the file does not exist.
   */
  public static long size(Path path) {
    return path.toFile().length();
  }

  /**
   * Strips the path of its {@linkplain #extension(Path) extension}.
   */
  public static String stripExtension(Path path) {
    return stripExtension(path.toString());
  }

  /**
   * Strips the path of its {@linkplain #extension(String) extension}.
   */
  public static String stripExtension(String path) {
    int extensionPos = FilenameUtils.indexOfExtension(path);
    return extensionPos >= 0 ? path.substring(0, extensionPos) : path;
  }

  /**
   * Strips the path of its {@linkplain #fullExtension(Path) full extension}.
   */
  public static String stripFullExtension(Path path) {
    return stripFullExtension(path.toString());
  }

  /**
   * Strips the path of its {@linkplain #fullExtension(String) full extension}.
   */
  public static String stripFullExtension(String path) {
    Matcher m = PATTERN__FULL_EXTENSION.matcher(path);
    return m.find() ? path.substring(0, m.start()) : path;
  }

  /**
   * @throws IllegalArgumentException
   *           if {@code uri} does not represent a file (that is, its scheme is neither {@code file}
   *           nor undefined).
   */
  static Path path(URI uri, FileSystem fs) {
    // Absolute URI?
    if (uri.isAbsolute()) {
      if (!isFile(uri))
        throw wrongArg("uri", uri, "MUST be a file-schemed URI");

      var b = new StringBuilder();
      // Windows-like?
      if (fs.getSeparator().equals(S + BACKSLASH)) {
        String s;
        // Host.
        if ((s = uri.getAuthority()) != null) {
          b.append(fs.getSeparator()).append(fs.getSeparator()).append(s)
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
      return fs.getPath(EMPTY, uri.toString());
  }

  private Files() {
  }
}
