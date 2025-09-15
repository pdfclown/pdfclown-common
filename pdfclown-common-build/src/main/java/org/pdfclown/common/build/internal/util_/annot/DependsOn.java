/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DependsOn.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_.annot;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Exceptions.wrongArg;
import static org.pdfclown.common.build.internal.util_.Objects.asType;
import static org.pdfclown.common.build.internal.util_.Objects.fqn;
import static org.pdfclown.common.build.internal.util_.Objects.init;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Indicates that the element relies on non-essential dependencies and must therefore be decoupled
 * from main code in case those dependencies may be excluded at runtime.
 *
 * @author Stefano Chizzolini
 * @apiNote To decouple code relying on non-essential dependencies:
 *          <ol>
 *          <li>declare the <b>non-essential dependencies</b>:<pre class="lang-java"><code>
 * public class Config {
 *   public static final String DEPENDENCY__JAVACV = "org.bytedeco:javacv-platform";
 *   static {
 *     DependsOn.Registry.register(DEPENDENCY__JAVACV, "org.bytedeco.javacv.Frame");
 *   }
 *
 *   public static &lt;T&gt; T resolve(
 *       Class&lt;? extends T&gt; dependentType,
 *       Class&lt;? extends T&gt; fallbackType) {
 *     return DependsOn.Registry.resolve(dependentType, fallbackType);
 *   }
 * }</code></pre></li>
 *          <li>define a <b>fall-back implementation</b> of the functionality, which doesn't rely on
 *          non-essential dependencies:<pre class="lang-java"><code>
 * public class MyType {
 *   public String myMethod(. . .) {
 *     . . .
 *   }
 * }</code></pre></li>
 *          <li>define one or more <b>full implementations</b> of the functionality, which rely on
 *          non-essential dependencies:<pre class="lang-java"><code>
 * &#64;DependsOn(DEPENDENCY_JAVACV)
 * public class MyFullType extends MyType {
 *   &#64;Override
 *   public String myMethod(. . .) {
 *     . . .
 *   }
 * }</code></pre></li>
 *          <li>resolve at runtime the <b>best implementation</b>
 *          available:<pre class="lang-java"><code>
 * public class MyType {
 *   public static final MyType INSTANCE = Config.resolve(MyFullType.class, MyType.class);
 *   . . .
 * }</code></pre></li>
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
  class Registry {
    private static final Logger log = LoggerFactory.getLogger(Registry.class);

    private static final Map<String, Boolean> dependables = new HashMap<>();
    private static final Map<String, String> detectFqns = new HashMap<>();

    /**
     * Gets whether the dependency is available.
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
     * Gets whether all the non-essential dependencies of the object are available.
     *
     * @param obj
     *          Either an instance or a class.
     */
    public static boolean isUsable(Object obj) {
      Class<?> dependentType = requireNonNull(asType(obj), "`obj`");
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
     * Registers a non-essential dependency.
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
     * Instantiates the best viable implementation among the types.
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
