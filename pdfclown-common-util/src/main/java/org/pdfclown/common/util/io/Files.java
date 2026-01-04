/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

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
import static java.nio.file.Files.walkFileTree;
import static java.util.Collections.binarySearch;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.FilenameUtils.indexOfExtension;
import static org.apache.commons.io.file.PathUtils.fileContentEquals;
import static org.pdfclown.common.util.Chars.BACKSLASH;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.SLASH;
import static org.pdfclown.common.util.Exceptions.missingPath;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Objects.INDEX__NOT_FOUND;
import static org.pdfclown.common.util.Objects.found;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.file.AccumulatorPathVisitor;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.LazyNonNull;
import org.pdfclown.common.util.annot.Unmodifiable;

/**
 * File utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Files {
  /**
   * File tree comparison.
   *
   * @author Stefano Chizzolini
   */
  public static final class Diff {
    /**
     * File status.
     *
     * @author Stefano Chizzolini
     */
    public enum FileStatus {
      /**
       * Only in the first file tree.
       */
      DIR1_ONLY,
      /**
       * Same content in both the file trees.
       */
      SAME,
      /**
       * Different content in the file trees.
       */
      DIFFERENT,
      /**
       * Only in the second file tree.
       */
      DIR2_ONLY
    }

    private final Path dir1;
    private final List<Path> dir1Files;
    private final Path dir2;
    private final List<Path> dir2Files;

    private @LazyNonNull @Nullable List<Path> diffFiles;
    private @LazyNonNull @Nullable List<Path> structureDiffFiles;
    private @LazyNonNull @Nullable Boolean structureSame;

    Diff(Path dir1, List<Path> dir1Files, Path dir2, List<Path> dir2Files) {
      this.dir1 = dir1;
      this.dir1Files = unmodifiableList(dir1Files);
      this.dir2 = dir2;
      this.dir2Files = unmodifiableList(dir2Files);
    }

    /**
     * File content difference between {@linkplain #getDir1() first} and {@linkplain #getDir2()
     * second} file tree.
     * <p>
     * Includes also the edge cases of files existing only on one side of the comparison.
     * Consequently, the file content difference is a superset of the
     * {@linkplain #getStructureDiffFiles() file structure difference}.
     * </p>
     * <p>
     * Use {@link #status(Path)} to tell them apart.
     * </p>
     *
     * @see #getStructureDiffFiles()
     */
    @Unmodifiable
    public List<Path> getDiffFiles() {
      if (diffFiles == null) {
        final var diffFiles = new ArrayList<Path>();
        {
          // dir1-only or different files.
          for (var file : dir1Files) {
            try {
              if (!fileContentEquals(dir1.resolve(file), dir2.resolve(file))) {
                diffFiles.add(file);
              }
            } catch (IOException ex) {
              throw runtime(ex);
            }
          }
          // dir2-only.
          final var dir2OnlyFiles = new ArrayList<>(dir2Files);
          {
            dir2OnlyFiles.removeAll(dir1Files);
          }
          diffFiles.addAll(dir2OnlyFiles);

          Collections.sort(diffFiles);
        }
        this.diffFiles = unmodifiableList(diffFiles);
      }
      return diffFiles;
    }

    /**
     * Base directory of the first file tree.
     */
    public Path getDir1() {
      return dir1;
    }

    /**
     * First tree files (relative paths).
     */
    @Unmodifiable
    public List<Path> getDir1Files() {
      return dir1Files;
    }

    /**
     * Base directory of the second file tree.
     */
    public Path getDir2() {
      return dir2;
    }

    /**
     * Second tree files (relative paths).
     */
    @Unmodifiable
    public List<Path> getDir2Files() {
      return dir2Files;
    }

    /**
     * File structure difference between {@linkplain #getDir1() first} and {@linkplain #getDir2()
     * second} file trees.
     * <p>
     * Corresponds to the files existing only on one side of the comparison: use
     * {@link #status(Path)} to tell them apart.
     * </p>
     *
     * @see #getDiffFiles()
     */
    @Unmodifiable
    public List<Path> getStructureDiffFiles() {
      if (structureDiffFiles == null) {
        if (isStructureSame()) {
          structureDiffFiles = List.of();
        } else {
          final var structureDiffFiles = new ArrayList<>(dir2Files);
          {
            // Retain dir2-only files!
            structureDiffFiles.removeAll(dir1Files);

            // Retain dir1-only files!
            final var dir1OnlyFiles = new ArrayList<>(dir1Files);
            {
              dir1OnlyFiles.removeAll(dir2Files);
            }
            structureDiffFiles.addAll(dir1OnlyFiles);

            Collections.sort(structureDiffFiles);
          }
          this.structureDiffFiles = unmodifiableList(structureDiffFiles);
        }
      }
      return structureDiffFiles;
    }

    /**
     * Whether {@linkplain #getDir1() first} and {@linkplain #getDir2() second} file trees have the
     * same content.
     * <p>
     * {@link #getDiffFiles() diffFiles} provide the full list of differences.
     * </p>
     *
     * @see #isStructureSame()
     */
    public boolean isSame() {
      if (!isStructureSame())
        return false;
      else
        return getDiffFiles().isEmpty();
    }

    /**
     * Whether {@linkplain #getDir1() first} and {@linkplain #getDir2() second} file trees have the
     * same structure.
     * <p>
     * {@link #getStructureDiffFiles() structureDiffFiles} provides the full list of differences.
     * </p>
     *
     * @see #isSame()
     */
    public boolean isStructureSame() {
      if (structureSame == null) {
        structureSame = dir1Files.size() == dir2Files.size()
            && dir1Files.equals(dir2Files);
      }
      return structureSame;
    }

    /**
     * Gets the status of a file within this comparison.
     * <p>
     * NOTE: Files created or removed afterward are ignored.
     * </p>
     *
     * @param file
     *          File inside the {@linkplain #getDir1() first} or {@linkplain #getDir2() second} file
     *          tree.
     * @throws IllegalArgumentException
     *           if {@code file} is outside the file trees of this comparison.
     */
    public FileStatus status(Path file) {
      if (file.isAbsolute()) {
        if (file.startsWith(dir1)) {
          file = dir1.relativize(file);
        } else if (file.startsWith(dir2)) {
          file = dir2.relativize(file);
        } else
          throw wrongArg("file", file, "MUST be inside {} or {}", dir1, dir2);
      }

      if (found(binarySearch(getDiffFiles(), file))) {
        return found(binarySearch(dir1Files, file))
            ? found(binarySearch(dir2Files, file))
                ? FileStatus.DIFFERENT
                : FileStatus.DIR1_ONLY
            : FileStatus.DIR2_ONLY;
      } else if (found(binarySearch(dir1Files, file)))
        return FileStatus.SAME;
      else
        throw wrongArg("file", file, "MUST exist inside {} or {}", dir1, dir2);
    }
  }

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

  /**
   * Parent directory symbol.
   */
  public static final String PATH_SUPER = S + DOT + DOT;

  /**
   * Gets the age of the file.
   */
  public static Duration age(Path file) {
    return Duration.ofMillis(System.currentTimeMillis() - file.toFile().lastModified());
  }

  /**
   * Gets the base name (that is, the filename without {@linkplain #extension(Path) extension}) of a
   * file.
   */
  public static String baseName(Path file) {
    return baseName(file, false);
  }

  /**
   * Gets the base name (that is, the filename without {@linkplain #extension(Path, boolean)
   * extension}) of a file.
   *
   * @param simple
   *          Whether to strip the filename of its full extension (obtaining its simple base name)
   *          rather than its simple one (obtaining its full base name).
   */
  public static String baseName(Path file, boolean simple) {
    return baseName(file.getFileName().toString(), simple);
  }

  /**
   * Gets the base name (that is, the filename without {@linkplain #extension(String) extension}) of
   * a file.
   */
  public static String baseName(String file) {
    return baseName(file, false);
  }

  /**
   * Gets the base name (that is, the filename without {@linkplain #extension(String, boolean)
   * extension}) of a file.
   *
   * @param simple
   *          Whether to strip the filename of its full extension (obtaining its simple base name)
   *          rather than its simple one (obtaining its full base name).
   */
  public static String baseName(String file, boolean simple) {
    String filename = filename(file);
    int extensionPos = simple ? indexOfFullExtension(filename) : indexOfExtension(filename);
    return (found(extensionPos) ? filename.substring(0, extensionPos) : filename);
  }

  /**
   * Replaces the simple extension of a file with a suffix.
   * <p>
   * For example, if {@code baseFile} is {@code "mypath/myFile.html"} and {@code suffix} is
   * {@code "_something.txt"}, the result is {@code "mypath/myFile_something.txt"}.
   * </p>
   */
  public static Path cognateFile(Path baseFile, String suffix) {
    return cognateFile(baseFile, suffix, false);
  }

  /**
   * Replaces the extension of a file with a suffix.
   * <p>
   * For example, if {@code baseFile} is {@code "mypath/myFile.html"} and {@code suffix} is
   * {@code "_something.txt"}, the result is {@code "mypath/myFile_something.txt"}.
   * </p>
   *
   * @param full
   *          Whether to replace the full extension rather than the simple one with the suffix.
   */
  public static Path cognateFile(Path baseFile, String suffix, boolean full) {
    return baseFile.resolveSibling(baseName(baseFile, full) + suffix);
  }

  /**
   * Replaces the simple extension of a file with a suffix.
   * <p>
   * For example, if {@code baseFile} is {@code "mypath/myFile.html"} and {@code suffix} is
   * {@code "_something.txt"}, the result is {@code "mypath/myFile_something.txt"}.
   * </p>
   */
  public static String cognateFile(String baseFile, String suffix) {
    return cognateFile(baseFile, suffix, false);
  }

  /**
   * Replaces the extension of a file with a suffix.
   * <p>
   * For example, if {@code baseFile} is {@code "mypath/myFile.html"} and {@code suffix} is
   * {@code "_something.txt"}, the result is {@code "mypath/myFile_something.txt"}.
   * </p>
   *
   * @param full
   *          Whether to replace the full extension rather than the simple one with the suffix.
   */
  public static String cognateFile(String baseFile, String suffix, boolean full) {
    int extensionPos = full ? indexOfFullExtension(baseFile) : indexOfExtension(baseFile);
    return (found(extensionPos) ? baseFile.substring(0, extensionPos) : baseFile) + suffix;
  }

  /**
   * Recursively copies the source directory to the target.
   * <p>
   * NOTE: This method differs from
   * {@link org.apache.commons.io.file.PathUtils#copyDirectory(Path, Path, java.nio.file.CopyOption...)}
   * in its return value: if you don't need path counters, use this one.
   * </p>
   *
   * @return {@code targetDir}
   */
  public static Path copyDirectory(Path sourceDir, Path targetDir) throws IOException {
    FileUtils.copyDirectory(sourceDir.toFile(), targetDir.toFile());
    return targetDir;
  }

  /**
   * Compares file trees by structure and content.
   * <p>
   * <b>File structure</b> comprises all filesystem nodes (both directories and files); <b>file
   * content</b> comprises all the data inside file nodes.
   * </p>
   *
   * @param dir1
   *          Base directory of the first file tree to compare.
   * @param dir2
   *          Base directory of the second file tree to compare.
   * @throws FileNotFoundException
   *           if {@code dir1} or {@code dir2} is not an existing directory.
   * @throws IOException
   *           if file access failed.
   */
  public static Diff diff(Path dir1, Path dir2) throws IOException {
    if (!isDirectory(requireNonNull(dir1, "`dir1`")))
      throw missingPath(dir1);
    else if (!isDirectory(requireNonNull(dir2, "`dir2`")))
      throw missingPath(dir2);

    AccumulatorPathVisitor visit1 = collectFiles(dir1);
    AccumulatorPathVisitor visit2 = collectFiles(dir2);
    return new Diff(
        dir1, visit1.relativizeFiles(dir1, true, null),
        dir2, visit2.relativizeFiles(dir2, true, null));
  }

  /**
   * Gets the simple extension of a file.
   * <p>
   * Contrary to {@link FilenameUtils#getExtension(String)}, the extension is prefixed by dot.
   * </p>
   *
   * @return Empty, if no extension.
   */
  public static String extension(Path file) {
    return extension(file, false);
  }

  /**
   * Gets the extension of a file.
   * <p>
   * Any dot-prefixed tailing part which doesn't begin with a digit is included in the extension;
   * therefore, composite extensions (for example, {@code ".tar.gz"}) are recognized, whilst version
   * codes are ignored (for example, {@code "commons-io-2.8.0.jar"} returns {@code ".jar"}, NOT
   * {@code ".8.0.jar"}).
   * </p>
   *
   * @param full
   *          Whether to retrieve the full extension rather than the simple one.
   * @return Empty, if no extension.
   */
  public static String extension(Path file, boolean full) {
    return extension(file.getFileName().toString(), full);
  }

  /**
   * Gets the simple extension of a file.
   * <p>
   * Contrary to {@link FilenameUtils#getExtension(String)}, the extension is prefixed by dot.
   * </p>
   *
   * @return Empty, if no extension.
   */
  public static String extension(String file) {
    return extension(file, false);
  }

  /**
   * Gets the extension of a file.
   * <p>
   * Any dot-prefixed tailing part which doesn't begin with a digit is included in the extension;
   * therefore, composite extensions (for example, {@code ".tar.gz"}) are recognized, whilst version
   * codes are ignored (for example, {@code "commons-io-2.8.0.jar"} returns {@code ".jar"}, NOT
   * {@code ".8.0.jar"}).
   * </p>
   *
   * @param full
   *          Whether to retrieve the full extension rather than the simple one.
   * @return Empty, if no extension.
   */
  public static String extension(String file, boolean full) {
    int extensionPos = full ? indexOfFullExtension(file) : indexOfExtension(file);
    return found(extensionPos) ? file.substring(extensionPos) : EMPTY;
  }

  /**
   * Gets the last part of a path.
   */
  public static String filename(Path path) {
    return path.getFileName().toString();
  }

  /**
   * Gets the last part of a path.
   */
  public static String filename(String path) {
    return FilenameUtils.getName(path);
  }

  /**
   * Gets whether the simple extension of a file corresponds to the given one (case-insensitive).
   * <p>
   * NOTE: Contrary to {@link FilenameUtils#isExtension(String, String)}, the extension is prefixed
   * by dot and the match is case-insensitive.
   * </p>
   */
  public static boolean isExtension(final Path file, final String extension) {
    return isExtension(file, extension, false);
  }

  /**
   * Gets whether the extension of a file corresponds to the given one (case-insensitive).
   */
  public static boolean isExtension(final Path file, final String extension, boolean full) {
    return isExtension(file.getFileName().toString(), extension, full);
  }

  /**
   * Gets whether the simple extension of a file corresponds to the given one (case-insensitive).
   * <p>
   * NOTE: Contrary to {@link FilenameUtils#isExtension(String, String)}, the extension is prefixed
   * by dot and the match is case-insensitive.
   * </p>
   */
  public static boolean isExtension(final String file, final String extension) {
    return isExtension(file, extension, false);
  }

  /**
   * Gets whether the extension of a file corresponds to the given one (case-insensitive).
   */
  public static boolean isExtension(final String file, final String extension, boolean full) {
    return extension(file, full).equalsIgnoreCase(extension);
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
   * Gets the relative path from a file to the other one.
   * <p>
   * Contrary to {@link Path#relativize(Path)}, this method relativizes {@code to} according to the
   * directory of {@code from} (that is, if {@code from} is a file, its parent is picked instead).
   * Consequently, {@code from} MUST exist.
   * </p>
   *
   * @throws IllegalArgumentException
   *           if {@code from} does not exist.
   */
  public static Path relativize(Path from, Path to) {
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
   * Gets the size of the file.
   *
   * @return The length (bytes) of the file, or {@code 0} if the file does not exist.
   */
  public static long size(Path path) {
    return path.toFile().length();
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

  private static AccumulatorPathVisitor collectFiles(Path dir) throws IOException {
    var ret = AccumulatorPathVisitor.withLongCounters();
    walkFileTree(dir, ret);
    return ret;
  }

  private static int indexOfFullExtension(String file) {
    Matcher m = PATTERN__FULL_EXTENSION.matcher(file);
    return m.find() ? m.start() : INDEX__NOT_FOUND;
  }

  private Files() {
  }
}
