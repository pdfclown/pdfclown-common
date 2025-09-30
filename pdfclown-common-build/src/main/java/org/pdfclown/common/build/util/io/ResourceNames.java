/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ResourceNames.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.util.io;

import static org.pdfclown.common.build.internal.util_.Exceptions.wrongArg;
import static org.pdfclown.common.build.internal.util_.Objects.sqnd;
import static org.pdfclown.common.build.internal.util_.Strings.BACKSLASH;
import static org.pdfclown.common.build.internal.util_.Strings.DOT;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;
import static org.pdfclown.common.build.internal.util_.Strings.S;
import static org.pdfclown.common.build.internal.util_.Strings.SLASH;
import static org.pdfclown.common.build.internal.util_.Strings.UNDERSCORE;
import static org.pdfclown.common.build.internal.util_.io.Files.PATH_SUPER;

import java.nio.file.Path;
import org.jspecify.annotations.Nullable;

/**
 * Resource utilities.
 * <p>
 * Names follow Java resource name syntax (slash-separated segments).
 * </p>
 *
 * @author Stefano Chizzolini
 */
public final class ResourceNames {
  /**
   * Gets the absolute name of a resource, resolved according to the base directory.
   *
   * @param file
   *          Resource file.
   * @param baseDir
   *          Root resource directory.
   * @return
   *         <ul>
   *         <li>if {@code file} is absolute and under {@code baseDir}: relativized, converted to
   *         name and prefixed with slash</li>
   *         <li>if {@code file} is relative and under {@code baseDir} (that is, not prefixed with
   *         {@code ..}): converted to name and prefixed with slash</li>
   *         <li>if {@code file} is outside {@code baseDir}: {@code null}</li>
   *         </ul>
   * @see #isAbs(String)
   */
  public static @Nullable String abs(Path file, Path baseDir) {
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

    return normal(SLASH + file.toString());
  }

  /**
   * Ensures the resource name is absolute.
   *
   * @param name
   *          Resource name.
   * @see #isAbs(String)
   */
  public static String abs(String name) {
    return isAbs(name) ? name : SLASH + name;
  }

  /**
   * Gets the fully-qualified name of a resource, resolved according to the base type.
   *
   * @param name
   *          Resource name (either relative or absolute; slash-separated).
   * @param baseType
   *          Context type (to resolve relative {@code name}).
   */
  public static String full(String name, @Nullable Class<?> baseType) {
    return full(name, baseType != null ? baseType.getPackageName() : EMPTY);
  }

  /**
   * Gets the fully-qualified name of a resource, resolved according to the base package.
   *
   * @param name
   *          Resource name (either relative or absolute; slash-separated).
   * @param basePackage
   *          Context package name (to resolve relative {@code name}).
   */
  public static String full(String name, String basePackage) {
    name = normal(name);
    return isAbs(name) || basePackage.isEmpty() ? name
        : name(basePackage.replace(DOT, SLASH), name);
  }

  /**
   * Gets whether the name is absolute (that is, prefixed by {@code /}).
   */
  public static boolean isAbs(String name) {
    return name.startsWith(S + SLASH);
  }

  /**
   * Qualifies a simple resource name prepending the simple name of the base type.
   * <p>
   * For example, if {@code simpleName} is {@code "MyResource"} and {@code baseType}'s FQN is
   * {@code "io.mydomain.myproject.MyOuterClass$MyInnerClass"}, it returns
   * {@code "MyOuterClass.MyInnerClass_MyResource"}.
   * </p>
   *
   * @param simpleName
   *          Simple resource name.
   * @param baseType
   *          Context type (whose qualified simple name is to prepend).
   * @throws IllegalArgumentException
   *           if {@code simpleName} is not a simple resource name.
   */
  public static String local(String simpleName, Class<?> baseType) {
    if (simpleName.indexOf(SLASH) >= 0)
      throw wrongArg("simpleName", simpleName, "INVALID (cannot contain slashes)");

    return sqnd(baseType) + UNDERSCORE + simpleName;
  }

  /**
   * Gets the name corresponding to the concatenation of the parts, normalized.
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
          } else if (part.startsWith(S + SLASH) && i > 0) {
            part = part.substring(1);
            if (part.isEmpty()) {
              continue;
            }
          }

          if (b.length() > 0 && b.charAt(b.length() - 1) != SLASH) {
            b.append(SLASH);
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
    int lastEnd = 0;
    /*
     * Whether current character is on separator boundary (that is, the previous character was a
     * slash, so no contiguous separator is acceptable).
     */
    boolean separated = false;
    for (int i = 0, limit = name.length() - 1; i <= limit; i++) {
      String replacement = null;
      char c = name.charAt(i);
      switch (c) {
        case BACKSLASH:
        case SLASH:
          // Contiguous with previous separator, or trailing non-root?
          if (i > 0
              && (separated /* Contiguous */
                  || i == limit) /* Trailing, non-root */) {
            // Suppress!
            replacement = EMPTY;
          } else if (c == BACKSLASH) {
            // Normalize!
            replacement = S + SLASH;
          }
          separated = true;
          break;
        default:
          separated = false;
          break;
      }
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
    return b != null ? b.append(name.substring(lastEnd)).toString() : name;
  }

  /**
   * Gets the parent of a resource name, normalized.
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
   *          Resource name (either relative or absolute; slash-separated).
   * @return {@code null}, if {@code name} is root (either relative ({@code ""}) or absolute
   *         ({@code "/"})).
   */
  public static @Nullable String parent(String name) {
    /*
     * NOTE: After normalization, no trailing slash other than root is possible.
     */
    name = normal(name);
    int sepIndex = name.lastIndexOf(SLASH);
    switch (sepIndex) {
      case -1:
        return !name.isEmpty()
            ? EMPTY /* Relative root */
            : null /* Relative root's parent */;
      case 0:
        return name.length() > 1
            ? S + SLASH /* Absolute root */
            : null /* Absolute root's parent */;
      default:
        return name.substring(0, sepIndex) /* Intermediate level */;
    }
  }

  /**
   * Gets the absolute path of a resource.
   * <p>
   * {@code name} is resolved according to {@code baseDir}, no matter whether it is relative or
   * absolute. For example, assuming {@code baseDir} is
   * {@code "/home/myusr/Projects/myproj/src/main/resources"}:
   * </p>
   * <ul>
   * <li>in case of a <i>relative</i> {@code name} like {@code "subpath/obj.html"}, it returns
   * {@code "/home/myusr/Projects/myproj/src/main/resources/subpath/obj.html"}</li>
   * <li>in case of an <i>absolute</i> {@code name} like {@code "/html/obj.html"}, it returns
   * {@code "/home/myusr/Projects/myproj/src/main/resources/html/obj.html"}</li>
   * </ul>
   *
   * @param name
   *          Resource name (either relative or absolute; slash-separated).
   * @param baseDir
   *          Root resource directory.
   */
  public static Path path(String name, Path baseDir) {
    //    return path(name, baseDir, EMPTY);
    if ((name = normal(name)).startsWith(S + SLASH)) {
      name = name.substring(1);
    }
    return baseDir.resolve(name);
  }

  private ResourceNames() {
  }
}
