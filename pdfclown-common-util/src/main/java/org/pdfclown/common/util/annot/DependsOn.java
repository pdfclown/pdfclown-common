/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DependsOn.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.annot;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Objects.init;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.pdfclown.common.util.XtEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Indicates that the annotated element depends on optional dependencies (that is, required at
 * compile time only, not runtime — same as JPMS {@code requires static}).
 * <p>
 * IMPORTANT: This annotation has documentation purposes only; if the declared dependencies aren't
 * available, the associated element will still brutally fail upon call, throwing an obscure
 * exception. <span class="important">In order to gracefully fail, the annotated element is expected
 * to declare the {@link NoClassDefFoundError} among its thrown exceptions in Javadoc, so its
 * consumers can catch and process that error through
 * {@link org.pdfclown.common.util.Exceptions#missingClass(Dependency, NoClassDefFoundError)}.</span>
 * </p>
 *
 * @author Stefano Chizzolini
 * @apiNote Usage example:
 *          <ol>
 *          <li>define an enum declaring the optional dependencies:<pre class="lang-java"><code>
 * public enum Dependency implements DependsOn.Dependency {
 *   JAVACV("org.bytedeco:javacv-platform", "org.bytedeco.javacv.Frame");
 *
 *   public static final String CODE__JAVACV = "org.bytedeco:javacv-platform";
 *
 *   private final String code;
 *
 *   Dependency(String code, String fqn) {
 *     DependsOn.Registry.register(this.code = code, fqn);
 *   }
 *
 *   &#64;Override
 *   public String getCode() {
 *     return code;
 *   }
 * }</code></pre>
 *          <p>
 *          NOTE: The dependency code ({@code "org.bytedeco:javacv-platform"}) has to be declared
 *          twice because annotations allow only compile-time field types; at the same time,
 *          {@link org.pdfclown.common.util.Exceptions#missingClass(Dependency, NoClassDefFoundError)}
 *          needs a runtime type in order to trigger dependency registration upon reference (no
 *          assumption on application- or framework-level bootstrap mechanisms can be made here, so
 *          this is the simplest common solution possible).
 *          </p>
 *          </li>
 *          <li>associate the optional dependencies to dependent elements:
 *          <ul>
 *          <li>at method level (if the functionality depending on optional dependencies is specific
 *          to a method): <pre class="lang-java" data-line="4"><code>
 * public final Videos {
 *   . . .
 *
 *   <span style=
"background-color:yellow;color:black;">&#64;DependsOn(Dependency.CODE__JAVACV)</span>
 *   public static BufferedImage frameImage(InputStream videoStream, double frameTime) {
 *     . . .
 *   }
 * }</code></pre></li>
 *          <li>at class level (if the functionality of the class as a whole depends on optional
 *          dependencies): <pre class="lang-java" data-line="1"><code>
 * <span style="background-color:yellow;color:black;">&#64;DependsOn(Dependency.CODE__JAVACV)</span>
 * public final Videos {
 *   . . .
 *
 *   public static BufferedImage frameImage(InputStream videoStream, double frameTime) {
 *     . . .
 *   }
 * }</code></pre></li>
 *          </ul>
 *          </li>
 *          <li>manage thrown {@link NoClassDefFoundError}:
 *          <ul>
 *          <li>if the associated method contains references to types belonging to optional
 *          dependencies, <b>declare the error among its thrown exceptions</b> in
 *          Javadoc:<pre class="lang-java" data-line="5-6"><code>
* public final Videos {
*   . . .
*
*   &#47;**
*    <span style="background-color:yellow;color:black;">* &#64;throws NoClassDefFoundError
*    *           if {&#64;value Dependency#CODE__JAVACV} dependency is missing.</span>
*    *&#47;
*   &#64;DependsOn(Dependency.CODE__JAVACV)
*   public static BufferedImage frameImage(InputStream videoStream, double frameTime) {
*     var grabber = new FFmpegFrameGrabber(videoStream);
*     . . .
*   }
* }</code></pre></li>
 *          <li>if the associated method doesn't contain references to types belonging to optional
 *          dependencies, <b>catch the error if declared among the exceptions thrown by called
 *          methods</b>:<pre class="lang-java" data-line="12"><code>
 * import static org.pdfclown.common.util.Exceptions.missingClass;
 *
* public Appearances {
*   . . .
*
*   &#64;DependsOn(Dependency.CODE__JAVACV)
*   public static Image playbackAltImage(InputStream videoStream, double frameTime, Size size) {
*     . . .
*     try {
*       frameImage = Image.of(Videos.frameImage(videoStream, frameTime));
*     } catch (NoClassDefFoundError ex) {
*       <span style=
"background-color:yellow;color:black;">throw missingClass(Dependency.JAVACV, ex);</span>
*     }
*   }
* }</code></pre></li>
 *          </ul>
 *          </li>
 *          </ol>
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, CONSTRUCTOR, METHOD })
public @interface DependsOn {
  /**
   * Optional dependency.
   *
   * @author Stefano Chizzolini
   */
  interface Dependency extends XtEnum<String> {
    default boolean isAvailable() {
      return Registry.isDependable(getCode());
    }
  }

  /**
   * Optional dependencies registry.
   *
   * @author Stefano Chizzolini
   */
  class Registry {
    private static final Logger log = LoggerFactory.getLogger(Registry.class);

    private static final Map<String, Boolean> dependables = new HashMap<>();

    /**
     * Gets whether the optional dependency is available.
     *
     * @param dependencyId
     *          (see {@link #register(String, String)})
     * @throws IllegalArgumentException
     *           if {@code dependencyId} is unknown (that is, not among the
     *           {@linkplain #register(String, String) registered} ones).
     */
    public static boolean isDependable(String dependencyId) {
      var ret = dependables.get(dependencyId);
      if (ret == null)
        throw wrongArg("dependencyId", requireNonNull(dependencyId, "`dependencyId`"),
            "UNKNOWN (registration required)");

      return ret;
    }

    /**
     * Registers an optional dependency.
     *
     * @param dependencyId
     *          (typically represented as {@code "groupId:artifactId"})
     * @param fqn
     *          Fully-qualified name of the class used at runtime to detect whether the dependency
     *          is available; for the purpose, it should be present across all the supported
     *          versions.
     */
    public static void register(String dependencyId, String fqn) {
      requireNonNull(dependencyId, "`dependencyId`");

      var dependable = init(fqn);
      if (dependable) {
        log.debug("Optional dependency FOUND: {}", dependencyId);
      } else {
        log.debug("Optional dependency MISSING: {} ({})", dependencyId, fqn);
      }
      dependables.put(dependencyId, dependable);
    }
  }

  /**
   * Optional dependencies (each represented as {@code "groupId:artifactId"}).
   */
  @NonNull
  String[] value();
}
