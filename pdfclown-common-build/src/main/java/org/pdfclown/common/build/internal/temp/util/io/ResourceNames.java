/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ResourceNames.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.temp.util.io;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Chars.BACKSLASH;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.SLASH;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Objects.asType;
import static org.pdfclown.common.util.Objects.found;
import static org.pdfclown.common.util.Objects.fqn;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.io.Files.PATH_SUPER;

import java.nio.file.Path;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.io.Files;

/**
 * Resource name utilities.
 * <p>
 * Within this class, <b>resource names</b> follow the Java resource name syntax: they are
 * <i>concatenations of slash-separated segments</i>. A resource name without leading slash is
 * <b>relative</b>, whilst with it is <b>absolute</b> — NOTE: the
 * {@linkplain Class#getResource(String) official Java documentation} is misleading, as it states
 * that an absolute resource name "is the portion of the name following the [leading slash]": such
 * definition doesn't make sense, as the lack of leading slash causes the name to be prefixed by a
 * package name (typical behavior of <i>relative</i> names, NOT absolute ones!). The documentation
 * of {@code Class.resolveName(String)} itself falls in contradiction when it says "Add a package
 * name prefix if the name is not absolute. Remove leading [slash] if name is absolute". What the
 * original Java author was evidently intending with the initial expression "absolute resource name"
 * is the <i>full resource name</i>, NOT the absolute one.
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
   * Gets the name of a resource, rooted in a base directory.
   *
   * @param file
   *          Resource file.
   * @param baseDir
   *          Resource base directory.
   * @return
   *         <ul>
   *         <li>if {@code file} is absolute and under {@code baseDir}: relativized and converted to
   *         absolute resource name</li>
   *         <li>if {@code file} is relative and under {@code baseDir} (that is, NOT prefixed with
   *         {@value Files#PATH_SUPER}): converted to absolute resource name</li>
   *         <li>if {@code file} is outside {@code baseDir}: {@code null}</li>
   *         </ul>
   * @throws NullPointerException
   *           if {@code file} is absolute and {@code baseDir} is undefined.
   */
  public static @Nullable String fromPath(Path file, @Nullable Path baseDir) {
    file = file.normalize();
    if (file.isAbsolute()) {
      baseDir = requireNonNull(baseDir, "`baseDir`").toAbsolutePath().normalize();
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

    return abs(file.toString());
  }

  /**
   * Gets the absolute name of a resource qualified by a base.
   *
   * @param name
   *          Resource name (either relative or absolute).
   * @param base
   *          Object whose package name is prepended in case of relative {@code name} (if
   *          {@code String}, it must be a package name).
   * @return
   *         <ul>
   *         <li>if {@code name} is absolute: {@code name}</li>
   *         <li>if {@code name} is relative: {@code /%BASE_PACKAGE_NAME%/name}, where
   *         {@code BASE_PACKAGE_NAME} is the slash-separated {@linkplain Class#getPackageName()
   *         fully-qualified class package name} of {@code base}</li>
   *         </ul>
   * @see #relBased(String, Object)
   * @see #localName(String, Object)
   */
  public static String absBased(String name, Object base) {
    return abs(relBased(name, base));
  }

  /**
   * Gets the absolute name of a resource local to an object.
   *
   * @param name
   *          Resource name (either relative or absolute).
   * @param obj
   *          Object whose type name is prepended in case of relative {@code name}.
   * @return
   *         <ul>
   *         <li>if {@code name} is absolute: {@code name}</li>
   *         <li>if {@code name} is relative:
   *         <code>"/%OBJ_PACKAGE_NAME%/%OBJ_TYPE_SQN%/%name%"</code>, where
   *         {@code OBJ_PACKAGE_NAME} is the slash-separated {@linkplain Class#getPackageName()
   *         fully-qualified class package name} of {@code obj}, and {@code OBJ_TYPE_SQN} is the
   *         {@linkplain org.pdfclown.common.util.Objects#sqn(Object) simply-qualified class name}
   *         of {@code obj}</li>
   *         </ul>
   * @see #absBased(String, Object)
   */
  public static String localName(String name, Object obj) {
    return isAbs(name) ? normal(name) : name(fromType(obj), name);
  }

  /**
   * Gets the absolute abstract resource name of an object.
   * <p>
   * This is a <i>purely syntactic conversion from class to resource namespaces</i>, where dot
   * separators are replaced by slashes, and a slash is prefixed; consequently, the result does NOT
   * make any assumption on its actual resource type and no file extension is included (for example,
   * {@code fromType("")} returns {@code "/java/util/String"}, NEITHER
   * {@code "/java/util/String.class"} NOR {@code "/java/util/String.java"}).
   * </p>
   *
   * @see #fromTypeName(String)
   */
  public static String fromType(Object obj) {
    return fromTypeName(fqn(requireNonNull(obj, "`obj`")));
  }

  /**
   * Gets the absolute abstract resource name corresponding to a type name.
   * <p>
   * This is a <i>purely syntactic conversion from class to resource namespaces</i>, where dot
   * separators are replaced by slashes, and a slash is prefixed; consequently, the result does NOT
   * make any assumption on its actual resource type and no file extension is included (for example,
   * {@code fromTypeName("java.util.String")} returns {@code "/java/util/String"}, NEITHER
   * {@code "/java/util/String.class"} NOR {@code "/java/util/String.java"}).
   * </p>
   *
   * @see #fromType(Object)
   * @see #toTypeName(String)
   */
  public static String fromTypeName(String typeName) {
    return abs(requireNonNull(typeName, "`typeName`").replace(DOT, SLASH));
  }

  /**
   * Gets the type name corresponding to a resource name.
   * <p>
   * Slash separators are replaced by dots, while leading slash and file extension are removed.
   * </p>
   *
   * @param name
   *          Resource name.
   * @throws IllegalArgumentException
   *           if {@code name} contains any dot inside folder parts, as they conflict with
   *           corresponding package syntax — in particular, the resulting package/class name could
   *           not reverse to the initial resource name and any relative resource name
   *           {@linkplain Class#getResource(String) resolved} on it would point to a different
   *           resource folder than the initial one (for example,
   *           {@code "/my/internal.scripts/conf"} would return {@code "my.internal.scripts.conf"},
   *           whose classes would resolve relative names to {@code "/my/internal/scripts/conf"}
   *           instead of initial {@code "/my/internal.scripts/conf"}).
   * @see #fromTypeName(String)
   */
  public static String toTypeName(String name) {
    name = rel(name);
    int dotPos = name.indexOf(DOT);
    if (found(dotPos)) {
      // Forbid dots inside folder parts!
      if (name.lastIndexOf(SLASH) > dotPos)
        throw wrongArg("name", name, "Dots NOT allowed inside folder parts");

      // Remove file extension!
      name = name.substring(0, dotPos);
    }
    return name.replace(SLASH, DOT);
  }

  /**
   * Gets the relative name of a resource qualified by a base.
   *
   * @param name
   *          Resource name (either relative or absolute).
   * @param base
   *          Object whose package name is prepended in case of relative {@code name} (if
   *          {@code String}, it must be a package name).
   * @return
   *         <ul>
   *         <li>if {@code name} is absolute: {@code name}</li>
   *         <li>if {@code name} is relative: {@code %BASE_PACKAGE_NAME%/name}, where
   *         {@code BASE_PACKAGE_NAME} is the slash-separated {@linkplain Class#getPackageName()
   *         fully-qualified class package name} of {@code base}</li>
   *         </ul>
   */
  public static String relBased(String name, Object base) {
    //noinspection DataFlowIssue : @PolyNull
    return isAbs(name = normal(name)) ? name
        : name(rel(fromTypeName(requireNonNull(base, "`base`") instanceof String s ? s
            : asType(base).getPackageName())), name);
  }

  /**
   * Gets whether the name is absolute (that is, prefixed by slash).
   *
   * @implNote For the sake of consistency with the other utilities in this class, which enforce
   *           name normalization, this method accepts also back-slash as separator.
   */
  public static boolean isAbs(CharSequence name) {
    char c = !name.isEmpty() ? name.charAt(0) : 0;
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
    return switch (parts.length) {
      case 0 -> EMPTY;
      case 1 -> normal(parts[0]);
      default -> {
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
            var partBeginsWithSeparator = isAbs(part);
            var mainIsEmptyOrEndsWithSeparator = b.isEmpty() || b.charAt(b.length() - 1) == SLASH;
            if (mainIsEmptyOrEndsWithSeparator == partBeginsWithSeparator) {
              /*
               * Merging contiguous separators (or collapsing leading intermediate separator on
               * relative root)?
               *
               * Example (merge case): ["part0/" "/part1"] --> "part0/part1"
               *
               * Example (collapse case): ["" "/part1"] --> "part1"
               */
              if (partBeginsWithSeparator) {
                part = rel(part);
              }
              /*
               * Missing separator.
               *
               * Example: ["part0" "part1"] --> "part0/part1"
               */
              else {
                b.append(SLASH);
              }
            }
          }
          b.append(part);
        }
        yield b.toString();
      }
    };
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
   * Gets the path corresponding to a resource name.
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
  public static Path toPath(String name, Path baseDir) {
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
