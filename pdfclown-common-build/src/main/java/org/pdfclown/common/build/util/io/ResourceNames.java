/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ResourceNames.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.util.io;

import static org.pdfclown.common.util.Chars.BACKSLASH;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.SLASH;
import static org.pdfclown.common.util.Objects.asType;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.io.Files.PATH_SUPER;

import java.nio.file.Path;
import org.jspecify.annotations.Nullable;

/**
 * Resource name utilities.
 * <p>
 * Within this class, <b>resource names</b> follow the Java resource name syntax: they are
 * <i>concatenations of slash-separated segments</i>. Trailing slash means directory (otherwise,
 * file); leading slash means absolute name (otherwise, relative) — NOTE: the
 * {@linkplain Class#getResource(String) official Java documentation} is misleading, as it states
 * that an absolute resource name "is the portion of the name following the [leading slash]": such
 * definition doesn't make sense, as the lack of leading slash causes the name to be prefixed by a
 * package name (typical behavior of <i>relative</i> names, NOT absolute ones!). The documentation
 * of {@code Class.resolveName(String)} itself falls in contradiction when it says "Add a package
 * name prefix if the name is not absolute. Remove leading [slash] if name is absolute". What the
 * original Java author was evidently intending with the expression "absolute resource name" is the
 * <i>full resource name</i>, NOT the absolute one.
 * </p>
 * <p>
 * All the methods within this class return normalized resource names.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public final class ResourceNames {
  /**
   * Ensures the resource name is absolute.
   *
   * @param name
   *          Resource name.
   * @see #isAbs(CharSequence)
   */
  public static String abs(String name) {
    return isAbs(name = normal(name)) ? name : SLASH + name;
  }

  /**
   * Gets the name of a resource, relative to the base directory.
   *
   * @param file
   *          Resource file.
   * @param baseDir
   *          Resource base directory.
   * @return
   *         <ul>
   *         <li>if {@code file} is absolute and under {@code baseDir}: relativized and converted to
   *         name</li>
   *         <li>if {@code file} is relative and under {@code baseDir} (that is, not prefixed with
   *         {@code ..}): converted to name</li>
   *         <li>if {@code file} is outside {@code baseDir}: {@code null}</li>
   *         </ul>
   * @see #isAbs(CharSequence)
   */
  public static @Nullable String based(Path file, Path baseDir) {
    return based(file, baseDir, false);
  }

  /**
   * Gets the name of a resource, resolved according to the base directory.
   *
   * @param file
   *          Resource file.
   * @param baseDir
   *          Resource base directory.
   * @param abs
   *          Whether the resulting name is {@linkplain #isAbs(CharSequence) absolute}.
   * @return
   *         <ul>
   *         <li>if {@code file} is absolute and under {@code baseDir}: relativized and converted to
   *         name</li>
   *         <li>if {@code file} is relative and under {@code baseDir} (that is, not prefixed with
   *         {@code ..}): converted to name</li>
   *         <li>if {@code file} is outside {@code baseDir}: {@code null}</li>
   *         </ul>
   */
  public static @Nullable String based(Path file, Path baseDir, boolean abs) {
    file = file.normalize();
    if (file.isAbsolute()) {
      baseDir = baseDir.toAbsolutePath().normalize();
      // Absolute file outside `baseDir`?
      if (!file.startsWith(baseDir))
        return null;

      file = baseDir.relativize(file);
    }
    /*
     * Relative file outside `baseDir`?
     *
     * NOTE: By definition, ancestors are outside the resource context rooted in `baseDir`.
     */
    else if (file.getName(0).toString().equals(PATH_SUPER))
      return null;

    var ret = normal(file.toString());
    if (abs) {
      ret = abs(ret);
    }
    return ret;
  }

  /**
   * Gets the name of a resource qualified by the base.
   *
   * @param name
   *          Resource name (either relative or absolute).
   * @param base
   *          Base object, whose package name is prepended in case of relative {@code name} (if
   *          {@code String}, it must be a package name itself).
   * @return
   *         <ul>
   *         <li>{@code name} — if {@code name} is absolute</li>
   *         <li>{@code %BasePackageName%/name} — if {@code name} is relative, where
   *         {@code %BasePackageName%} is the package of {@code base} whose dots are converted to
   *         slashes</li>
   *         </ul>
   */
  public static String based(String name, Object base) {
    return based(name, base, false);
  }

  /**
   * Gets the name of a resource qualified by the base.
   *
   * @param name
   *          Resource name (either relative or absolute).
   * @param base
   *          Base object, whose package name is prepended in case of relative {@code name} (if
   *          {@code String}, it must be a package name itself).
   * @param abs
   *          Whether the resulting name is absolute.
   * @return
   *         <ul>
   *         <li>{@code name} — if {@code name} is absolute</li>
   *         <li>{@code %BasePackageName%/name} — if {@code name} is relative, where
   *         {@code %BasePackageName%} is the package of {@code base} whose dots are converted to
   *         slashes</li>
   *         </ul>
   */
  public static String based(String name, Object base, boolean abs) {
    if (!(isAbs(name = normal(name)))) {
      //noinspection DataFlowIssue : @PolyNull
      name = name((base instanceof String s ? s : asType(base).getPackageName())
          .replace(DOT, SLASH), name);
      if (abs) {
        name = abs(name);
      }
    }
    return name;
  }

  /**
   * Gets whether the name is absolute (that is, prefixed by slash).
   *
   * @implNote For the sake of consistency with the other utilities in this class, which enforce
   *           name normalization, this method accepts also back-slash as separator.
   * @see #isDir(CharSequence)
   */
  public static boolean isAbs(CharSequence name) {
    char c = name.length() > 0 ? name.charAt(0) : 0;
    return c == SLASH || c == BACKSLASH;
  }

  /**
   * Gets whether the name is a directory (that is, suffixed by slash or empty (relative root)).
   *
   * @implNote For the sake of consistency with the other utilities in this class, which enforce
   *           name normalization, this method accepts also back-slash as separator.
   * @see #isAbs(CharSequence)
   */
  public static boolean isDir(CharSequence name) {
    char c = name.length() > 0 ? name.charAt(name.length() - 1) : SLASH;
    return c == SLASH || c == BACKSLASH;
  }

  /**
   * Gets the name corresponding to the concatenation of the parts.
   * <p>
   * NOTE: The semantics of resource name concatenation are different from usual string
   * concatenation, in that <i>the first part commands whether the whole name is absolute or not</i>
   * (for example, if {@code parts} is {@code ["", "/"]}, then the result is {@code ""} (relative
   * root), since {@code parts[0]} is itself relative root, whilst {@code parts[1]} is just a
   * trailing slash, which is suppressed because of normalization).
   * </p>
   *
   * @return Empty (that is, relative root), if {@code parts} is empty.
   */
  public static String name(String... parts) {
    switch (parts.length) {
      case 0:
        return EMPTY;
      case 1:
        return normal(parts[0]);
      default: {
        var b = new StringBuilder();
        for (int i = 0, limit = parts.length - 1; i <= limit; i++) {
          /*
           * NOTE: Normalized part may have single leading slash, but never trailing slash.
           */
          var part = normal(parts[i]);
          if (part.isEmpty()) {
            continue;
          }

          if (i > 0) {
            boolean partAbs = isAbs(part);
            if (isDir(b) == partAbs) {
              /*
               * Merging contiguous separators, or collapsing leading intermediate separator on
               * relative root?
               *
               * Example (merge case): {"part0/", "/part1"} --> "part0/part1"
               *
               * Example (collapse case): {"", "/part1"} --> "part1"
               */
              if (partAbs) {
                part = rel(part);
              }
              /*
               * Missing separator?
               *
               * Example: {"part0", "part1"} --> "part0/part1"
               */
              else {
                b.append(SLASH);
              }
            }
          }
          b.append(part);
        }
        return b.toString();
      }
    }
  }

  /**
   * Normalizes a name.
   * <p>
   * Transformations applied to {@code name}:
   * </p>
   * <ul>
   * <li>backslashes are converted to slashes</li>
   * <li>resulting contiguous slashes are collapsed to single slashes</li>
   * <li>resulting trailing slash is suppressed if non-root</li>
   * </ul>
   */
  public static String normal(String name) {
    StringBuilder b = null;
    /*
     * Whether current character is on separator boundary (that is, the previous character was a
     * slash, so no contiguous separator is acceptable).
     */
    var separated = false;
    int lastEnd = 0;
    for (int i = 0, l = name.length(); i < l; i++) {
      String replacement = null;
      char c = name.charAt(i);
      separated = switch (c) {
        case BACKSLASH, SLASH -> {
          // Contiguous with previous separator?
          if (separated) {
            // Suppress!
            replacement = EMPTY;
          } else if (c == BACKSLASH) {
            // Normalize!
            replacement = S + SLASH;
          }
          yield true;
        }
        default -> false;
      };
      if (replacement != null) {
        if (b == null) {
          b = new StringBuilder();
        }
        if (lastEnd < i) {
          b.append(name, lastEnd, i);
        }
        b.append(replacement);
        lastEnd = i + 1;
      }
    }
    return b != null ? b.append(name, lastEnd, name.length()).toString() : name;
  }

  /**
   * Gets the parent of a resource name.
   * <p>
   * For example:
   * </p>
   * <ul>
   * <li>if {@code name} is {@code "/my/res/html/obj.html"}, returns {@code "/my/res/html"}</li>
   * <li>if {@code name} is {@code "my/res/html/obj.html"}, returns {@code "my/res/html"}</li>
   * <li>if {@code name} is {@code "/my/res/html/"}, returns {@code "/my/res"}</li>
   * <li>if {@code name} is {@code "my/res/html/"}, returns {@code "my/res"}</li>
   * <li>if {@code name} is {@code "/my"}, returns {@code "/"} (absolute root)</li>
   * <li>if {@code name} is {@code "my"}, returns {@code ""} (relative root)</li>
   * <li>if {@code name} is {@code "/"} (absolute root), returns {@code null}</li>
   * <li>if {@code name} is {@code ""} (relative root), returns {@code null}</li>
   * </ul>
   *
   * @param name
   *          Resource name.
   * @return {@code null}, if {@code name} is root (either relative ({@code ""}) or absolute
   *         ({@code "/"})).
   */
  public static @Nullable String parent(String name) {
    /*
     * NOTE: After normalization, no trailing slash other than root is possible.
     */
    int sepPos;
    return switch (sepPos = (name = normal(name)).lastIndexOf(SLASH)) {
      case -1 -> !name.isEmpty()
          ? EMPTY /* Relative root */
          : null; /* Relative root's parent */
      case 0 -> name.length() > 1
          ? S + SLASH /* Absolute root */
          : null; /* Absolute root's parent */
      default -> name.substring(0, sepPos) /* Intermediate level */;
    };
  }

  /**
   * Gets the path of a resource.
   * <p>
   * {@code name} is resolved according to {@code baseDir}; whether {@code name} is relative or
   * absolute makes no difference.
   * </p>
   *
   * @param name
   *          Resource name.
   * @param baseDir
   *          Resource base directory.
   */
  public static Path path(String name, Path baseDir) {
    return baseDir.resolve(rel(name));
  }

  /**
   * Ensures the resource name is relative.
   *
   * @param name
   *          Resource name.
   * @see #isAbs(CharSequence)
   */
  public static String rel(String name) {
    return isAbs(name = normal(name)) ? name.substring(1) : name;
  }

  private ResourceNames() {
  }
}
