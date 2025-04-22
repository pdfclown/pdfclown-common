/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (DependsOn.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common> (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.util.lang;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Objects.asType;
import static org.pdfclown.common.util.Objects.fqn;
import static org.pdfclown.common.util.Objects.init;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Indicates the element relies on non-essential dependencies and must therefore be decoupled from
 * main code in case those dependencies may be excluded at runtime.
 *
 * @author Stefano Chizzolini
 * @apiNote To decouple code relying on non-essential dependencies:
 *          <ol>
 *          <li>declare the <b>non-essential dependencies</b>:<pre>
 *public class Config {
 *  public static final String DEPENDENCY__JAVACV = "org.bytedeco:javacv-platform";
 *  static {
 *    DependsOn.Registry.register(DEPENDENCY__JAVACV, "org.bytedeco.javacv.Frame");
 *  }
 *
 *  {@code public static <T> T resolve(
 *      Class<? extends T> dependentType,
 *      Class<? extends T> fallbackType)} {
 *    return DependsOn.Registry.resolve(dependentType, fallbackType);
 *  }
 *}</pre></li>
 *          <li>define a <b>fall-back implementation</b> of the functionality, which doesn't rely on
 *          non-essential dependencies:<pre>
 *public class MyType {
 *  public String myMethod(. . .) {
 *    . . .
 *  }
 *}</pre></li>
 *          <li>define one or more <b>full implementations</b> of the functionality, which rely on
 *          non-essential dependencies:<pre>
 *{@code @}DependsOn(DEPENDENCY_JAVACV)
 *public class MyFullType extends MyType {
 *  {@code @}Override
 *  public String myMethod(. . .) {
 *    . . .
 *  }
 *}</pre></li>
 *          <li>resolve at runtime the <b>best implementation</b> available:<pre>
 *public class MyType {
 *  public static final MyType INSTANCE = Config.resolve(MyFullType.class, MyType.class);
 *  . . .
 *}</pre></li>
 *          </ol>
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, CONSTRUCTOR, METHOD })
public @interface DependsOn {
  /**
   * Non-essential dependency registry.
   *
   * @author Stefano Chizzolini
   */
  static class Registry {
    private static final Logger log = LoggerFactory.getLogger(Registry.class);

    private static final Map<String, Boolean> dependables = new HashMap<>();
    private static final Map<String, String> detectFqns = new HashMap<>();

    /**
     * Gets whether the given dependency is available.
     *
     * @param dependency
     *          Dependency ({@code groupId:artifactId}).
     */
    public static boolean isDependable(String dependency) {
      return dependables.computeIfAbsent(requireNonNull(dependency), $ -> {
        String detectFqn = detectFqns.get($);
        if (detectFqn == null)
          throw wrongArg("dependency", dependency, "UNKNOWN (registration required)");

        var ret = init(detectFqn);
        if (!ret) {
          log.warn("Dependency UNAVAILABLE: {}", dependency);
        }
        return ret;
      });
    }

    /**
     * Gets whether all the non-essential dependencies of the given object are available.
     *
     * @param obj
     *          Either an instance or a class.
     */
    public static boolean isUsable(Object obj) {
      Class<?> dependentType = asType(obj);
      var annot = dependentType.getDeclaredAnnotation(DependsOn.class);
      if (annot != null) {
        for (String dependency : annot.value()) {
          boolean dependable = isDependable(dependency);
          if (!dependable) {
            if (log.isDebugEnabled()) {
              log.debug("{}: '{}' dependency MISSING", fqn(dependentType), dependency);
            }
            return false;
          }
        }
      }
      return true;
    }

    /**
     * Registers the given non-essential dependency.
     *
     * @param dependency
     *          Dependency ({@code groupId:artifactId}).
     * @param detectFqn
     *          Fully-qualified name of the class to use for the detection of {@code dependency}
     *          artifact availability at runtime (a highly-stable class within the dependency
     *          artifact which is available across the artifact versions).
     */
    public static void register(String dependency, String detectFqn) {
      detectFqns.put(dependency, detectFqn);
    }

    /**
     * Instantiates the best viable implementation among the given types.
     *
     * @param <T>
     *          Interface.
     * @param dependentTypes
     *          Implementation types, ordered by priority; the last type should be a fall-back
     *          implementation without non-essential dependencies.
     */
    @SafeVarargs
    public static <T> T resolve(Class<? extends T>... dependentTypes) {
      for (var dependentType : dependentTypes) {
        if (isUsable(dependentType)) {
          try {
            return dependentType.getConstructor().newInstance();
          } catch (Exception ex) {
            throw runtime(ex);
          }
        }
      }
      throw runtime("No viable type");
    }
  }

  /**
   * Dependencies (each represented as {@code "%groupId%:%artifactId%"}).
   */
  @NonNull
  String[] value();
}
