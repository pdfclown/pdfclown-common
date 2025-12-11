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

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.pdfclown.common.build.internal.util_.Objects.init;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;

/**
 * Indicates that the annotated element depends on {@linkplain Dependency optional dependencies}.
 * <p>
 * Callers MUST catch {@link NoClassDefFoundError} and pass it to
 * {@link org.pdfclown.common.build.internal.util_.Exceptions#missingClass(Collection, NoClassDefFoundError)},
 * along with the dependencies of the called element. <span class="important">Since callers become
 * transitively dependent on those dependencies, they MUST annotate themselves with
 * {@link Requires}, in order to document their own dependencies.</span>
 * </p>
 *
 * @author Stefano Chizzolini
 * @see Requires
 * @apiNote Usage example:
 *          <ol>
 *          <li>define an enum declaring the optional dependencies:<pre class="lang-java"><code>
 * public enum Dependency implements DependsOn.Dependency {
 *   JAVACV("org.bytedeco:javacv-platform", "org.bytedeco.javacv.Frame");
 *
 *   public static final String ID__JAVACV = "org.bytedeco:javacv-platform";
 *
 *   private final String fqn;
 *   private final String id;
 *
 *   Dependency(String id, String fqn) {
 *     this.id = id;
 *     this.fqn = fqn;
 *   }
 *
 *   &#64;Override
 *   public String getFqn() {
 *     return fqn;
 *   }
 *
 *   &#64;Override
 *   public String getId() {
 *     return id;
 *   }
 * }</code></pre>
 *          <p>
 *          NOTE: The dependency ID ({@code "org.bytedeco:javacv-platform"}) has to be declared
 *          twice because annotations allow only compile-time field types.
 *          </p>
 *          </li>
 *          <li>associate the optional dependencies to dependent
 *          elements:<pre class="lang-java" data-line="1"><code>
 * import org.bytedeco.javacv.FFmpegFrameGrabber;
 *
 * <span style="background-color:yellow;color:black;">&#64;DependsOn(Dependency.ID__JAVACV)</span>
 * public final Videos {
 *   public static BufferedImage frameImage(InputStream videoStream, double frameTime) {
 *     try (var grabber = new FFmpegFrameGrabber(videoStream)) {
 *       . . .
 *     }
 *   }
 * }</code></pre></li>
 *          <li>handle thrown {@link NoClassDefFoundError} on
 *          call:<pre class="lang-java" data-line="6,12"><code>
 * import static org.pdfclown.common.util.Exceptions.missingClass;
 *
* public Appearances {
*   <span style="background-color:yellow;color:black;">&#64;Requires(Dependency.ID__JAVACV)</span>
*   public static Image playbackAltImage(InputStream videoStream, double frameTime, Size size) {
*     try {
*       var frameImage = Image.of(Videos.frameImage(videoStream, frameTime));
*       . . .
*     } catch (NoClassDefFoundError ex) {
*       <span style=
"background-color:yellow;color:black;">throw missingClass(Dependency.JAVACV, ex);</span>
*     }
*   }
* }</code></pre></li>
 *          </ol>
 * @implNote Since the timing of linking is implementation-specific (a JVM implementation may choose
 *           to resolve each symbolic reference in a class or interface individually, only when it
 *           is used (<em>lazy</em> or <em>late resolution</em>), or to resolve them all at once
 *           while the class is being verified (<em>static resolution</em>) {@biblio.spec JLS:25
 *           12.3}), the granularity of this annotation is limited to
 *           {@linkplain java.lang.annotation.ElementType#TYPE type declaration}.
 */
@Documented
@Retention(RUNTIME)
@Target({ PACKAGE, TYPE })
public @interface DependsOn {
  /**
   * Optional dependency (that is, required at compile time only, like in JPMS
   * {@code requires static}).
   *
   * @author Stefano Chizzolini
   */
  interface Dependency {
    /**
     * Fully-qualified name of the class used at runtime to detect whether this dependency is
     * available.
     * <p>
     * For the purpose, it should be present across all the supported versions of this dependency.
     * </p>
     */
    String getFqn();

    /**
     * Dependency identifier.
     *
     * @return (format: <code>"groupId:artifactId"</code>)
     */
    String getId();

    /**
     * Whether this dependency is present in the classpath.
     */
    default boolean isAvailable() {
      return init(getFqn());
    }
  }

  /**
   * Optional dependencies.
   *
   * @return (format: <code>"groupId:artifactId"</code>)
   */
  String[] value();
}
