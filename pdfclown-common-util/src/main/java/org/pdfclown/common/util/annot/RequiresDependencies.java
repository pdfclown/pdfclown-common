/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (RequiresDependencies.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.annot;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.pdfclown.common.util.Conditions.requireElement;
import static org.pdfclown.common.util.Objects.toStringWithValues;
import static org.pdfclown.common.util.Objects.tryInit;
import static org.pdfclown.common.util.reflect.Reflects.callerFrame;
import static org.pdfclown.common.util.reflect.Reflects.stackMethod;
import static org.pdfclown.common.util.reflect.Reflects.traceClass;

import java.lang.StackWalker.StackFrame;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.pdfclown.common.util.Conditions;
import org.pdfclown.common.util.Exceptions;
import org.pdfclown.common.util.annot.Annots.SearchStrategy;
import org.pdfclown.common.util.spi.DependencyProvider;
import org.pdfclown.common.util.spi.ServiceProvider;

/**
 * Indicates that the annotated element depends on {@linkplain Dependency optional dependencies}.
 * <p>
 * Annotated {@linkplain ElementType#TYPE types} and {@linkplain ElementType#PACKAGE packages}
 * cascade their optional dependencies to contained type members
 * ({@linkplain ElementType#CONSTRUCTOR constructors} and {@linkplain ElementType#METHOD methods});
 * such members shall not be directly annotated and shall have no responsibility to handle the
 * optional dependencies, as it is upon their callers. In other words, <span class="important">the
 * annotated type members have the responsibility to handle the optional dependencies only if the
 * members they call are not directly annotated themselves</span>. The responsible type members
 * either proactively {@linkplain Conditions#requireDependencies() check} the dependencies for
 * availability, or lazily {@linkplain Exceptions#missingDependencies(LinkageError) catch}
 * {@code LinkageError}. Both delegation to responsible type members and lazy catch assume that no
 * undesirable side effect may result from partial execution; otherwise, the responsibility bounces
 * back to the calling members and to proactive checks, respectively.
 * </p>
 * <p>
 * NOTE: <span class="important">If the optional dependencies are non-essential to the
 * implementation of an element, it shall not be annotated</span>; instead, it shall
 * {@linkplain Support#isAvailable(Collection) evaluate} whether the dependencies are available, and
 * branch accordingly, using a fallback in case of absence. If a dependent module promotes optional
 * dependencies to runtime dependencies, its code shall obviously neither be annotated nor evaluate
 * dependency availability.
 * </p>
 * <p>
 * IMPORTANT: Since the time of linking is implementation-specific (a JVM may choose to resolve each
 * symbolic reference in a class or interface individually, only when it is used (<b>lazy
 * resolution</b>), or to resolve them all at once while the class is being verified (<b>static
 * resolution</b>) {@biblio.spec JLS:25 12.3}), <span class="important">the code which references
 * types from optional dependencies MUST be isolated in top-level or static inner classes</span>;
 * mixing regular code and optional references will cause fatal errors.
 * </p>
 *
 * @author Stefano Chizzolini
 * @apiNote Useful when decoupling optional functionalities to separate modules is not feasible.
 *          <p>
 *          Usage example:
 *          </p>
 *          <ol>
 *          <li>define the optional dependencies:<pre class="lang-java" data-line="4"><code>
 * import org.pdfclown.common.util.annot.RequiresDependencies.Dependency;
 * import org.pdfclown.common.util.spi.DependencyProvider;
 *
 * public final class Dependencies <span style=
"background-color:yellow;color:black;">extends DependencyProvider</span> {
 *   public static final String ID__JAVACV = "org.bytedeco:javacv-platform";
 *   public static final String ID__JSOUP = "org.jsoup:jsoup";
 *
 *   public static final Dependency JAVACV = new Dependency(ID__JAVACV, "org.bytedeco.javacv.Frame");
 *   public static final Dependency JSOUP = new Dependency(ID__JSOUP, "org.jsoup.Jsoup");
 * }</code></pre>
 *          <p>
 *          {@code Dependencies} MUST extend {@link DependencyProvider} and register itself via
 *          {@linkplain java.util.ServiceLoader SPI} mechanism.
 *          </p>
 *          <p>
 *          NOTE: The dependency identifiers have to be declared as static constants because
 *          annotations allow only compile-time field types as values.
 *          </p>
 *          </li>
 *          <li>declare the dependencies required by elements at implementation
 *          sites:<pre class="lang-java" data-line="3"><code>
 * import org.pdfclown.common.util.annot.RequiresDependencies;
 *
 * <span style=
"background-color:yellow;color:black;">&#64;RequiresDependencies(Dependencies.ID__JAVACV)</span>
 * public final class Videos {
 *   public static BufferedImage frameImage(InputStream videoStream, double frameTime) {
 *     try (var grabber = new FFmpegFrameGrabber(videoStream)) {
 *       . . .
 *     }
 *   }
 * }</code></pre>
 *          <p>
 *          NOTE: In this example the annotated element is a type, so dependency handling is upon
 *          the callers of {@code frameImage}.
 *          </p>
 *          </li>
 *          <li>handle the dependencies at call sites:
 *          <ul>
 *          <li>essential dependencies:
 *          <ul>
 *          <li>proactive check: <pre class="lang-java" data-line="6,8"><code>
 * import static org.pdfclown.common.util.Conditions.requireDependencies;
 *
 * import org.pdfclown.common.util.annot.RequiresDependencies;
 *
 * public final class Appearances {
 *   <span style=
"background-color:yellow;color:black;">&#64;RequiresDependencies(Dependencies.ID__JAVACV)</span>
 *   public static Image playbackAltImage(InputStream videoStream, double frameTime, Size size) {
 *     <span style="background-color:yellow;color:black;">requireDependencies();</span>
 *
 *     var frameImage = Image.of(Videos.frameImage(videoStream, frameTime));
 *     . . .
 *   }
 * }</code></pre>
 *          <p>
 *          NOTE: {@code requireDependencies} method does not cache dependency probing; it is
 *          recommended to guard the check in a boolean condition that is skipped after first
 *          execution, like this:
 *          </p>
 *          <pre class="lang-java" data-line="6,10-13"><code>
 * import static org.pdfclown.common.util.Conditions.requireDependencies;
 *
 * import org.pdfclown.common.util.annot.RequiresDependencies;
 *
 * public final class Appearances {
 *   <span style=
"background-color:yellow;color:black;">private static boolean dependencyChecked;</span>
 *
 *   &#64;RequiresDependencies(Dependencies.ID__JAVACV)
 *   public static Image playbackAltImage(InputStream videoStream, double frameTime, Size size) {
 *     <span style="background-color:yellow;color:black;">if (!dependencyChecked) {
 *       requireDependencies();
 *       dependencyChecked = true;
 *     }</span>
 *
 *     var frameImage = Image.of(Videos.frameImage(videoStream, frameTime));
 *     . . .
 *   }
 * }</code></pre></li>
 *          <li>lazy catch: <pre class="lang-java" data-line="6,12"><code>
 * import static org.pdfclown.common.util.Exceptions.missingDependencies;
 *
 * import org.pdfclown.common.util.annot.RequiresDependencies;
 *
 * public class Appearances {
 *   <span style=
"background-color:yellow;color:black;">&#64;RequiresDependencies(Dependencies.ID__JAVACV)</span>
 *   public static Image playbackAltImage(InputStream videoStream, double frameTime, Size size) {
 *     try {
 *       var frameImage = Image.of(Videos.frameImage(videoStream, frameTime));
 *       . . .
 *     } catch (LinkageError ex) {
 *       <span style="background-color:yellow;color:black;">throw missingDependencies(ex);</span>
 *     }
 *   }
 * }</code></pre></li>
 *          </ul>
 *          </li>
 *          <li>non-essential dependencies: <pre class="lang-java" data-line="4,7"><code>
 * import static org.pdfclown.common.util.annot.RequiresDependencies.Support.isAvailable;
 *
 * public final class Appearances {
 *   <span style=
"background-color:yellow;color:black;">private static boolean javacvAvailable = isAvailable(Set.of(Dependencies.JAVACV));</span>
 *
 *   public static Image playbackAltImage(InputStream videoStream, double frameTime, Size size) {
 *     <span style="background-color:yellow;color:black;">if (javacvAvailable)</span> {
 *       var frameImage = Image.of(Videos.frameImage(videoStream, frameTime));
 *       . . .
 *     }
 *     // Fallback.
 *     else {
 *       . . .
 *     }
 *   }
 * }</code></pre></li>
 *          </ul>
 *          </li>
 *          </ol>
 */
@Documented
@Retention(RUNTIME)
@Target({ PACKAGE, TYPE, CONSTRUCTOR, METHOD })
public @interface RequiresDependencies {
  /**
   * Optional dependency (that is, required at compile time only, like in JPMS
   * {@code requires static}).
   *
   * @apiNote For usage examples, see {@link RequiresDependencies}.
   * @author Stefano Chizzolini
   */
  @Immutable
  class Dependency {
    /**
     * Registered dependencies by {@link Dependency#getId() ID}.
     */
    private static final Map<String, Dependency> registry = new HashMap<>();

    private final String id;
    private final String probe;

    public Dependency(String id, String probe) {
      this.id = id;
      this.probe = probe;

      registry.put(id, this);
    }

    /**
     * Dependency identifier.
     *
     * @return (format: <code>"groupId:artifactId"</code>)
     */
    public String getId() {
      return id;
    }

    /**
     * Fully-qualified name of the class used at runtime to detect whether this dependency is
     * available.
     * <p>
     * For the purpose, it should be present across all the supported versions of this dependency.
     * </p>
     */
    public String getProbe() {
      return probe;
    }

    /**
     * Whether this dependency is present in the classpath.
     */
    public boolean isAvailable() {
      return tryInit(getProbe()) != null;
    }

    @Override
    public String toString() {
      return toStringWithValues(this, id);
    }
  }

  /**
   * {@link Dependency} utilities.
   *
   * @author Stefano Chizzolini
   */
  final class Support {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Set.of(
        StackWalker.Option.RETAIN_CLASS_REFERENCE,
        StackWalker.Option.SHOW_REFLECT_FRAMES /* Ensures frames can match trace */));

    static {
      // Trigger dependency registration!
      ServiceProvider.discover(DependencyProvider.class).forEach($ -> {
        /*
         * NOP: Just forces all providers to get loaded, which in turn trigger dependency
         * registration.
         */
      });
    }

    /**
     * Gets the dependencies of the caller who invoked this method.
     */
    public static Collection<Dependency> dependencies() {
      return dependencies(callerFrame());
    }

    /**
     * Gets the dependencies of an element.
     */
    public static Collection<Dependency> dependencies(AnnotatedElement element) {
      return dependencies(element, SearchStrategy.CONTAINMENT);
    }

    /**
     * Gets the dependencies of an element.
     */
    public static Collection<Dependency> dependencies(AnnotatedElement element,
        SearchStrategy strategy) {
      return Annots.findAll(RequiresDependencies.class, element, strategy).stream()
          .map(RequiresDependencies::value)
          .flatMap(Arrays::stream)
          .distinct()
          .map($ -> requireElement(Dependency.registry.get($), $, "dependency"))
          .toList();
    }

    /**
     * Gets the dependencies of the method at a frame.
     */
    public static Collection<Dependency> dependencies(StackFrame frame) {
      return dependencies(stackMethod(frame).orElseThrow(), SearchStrategy.CONTAINMENT);
    }

    /**
     * Gets whether the dependencies of the caller who invoked this method are available.
     */
    public static boolean isAvailable() {
      return isAvailable(dependencies());
    }

    /**
     * Gets whether the dependencies of an element are available.
     */
    public static boolean isAvailable(AnnotatedElement element) {
      return isAvailable(dependencies(element));
    }

    /**
     * Gets whether the given dependencies are available.
     */
    public static boolean isAvailable(Collection<Dependency> dependencies) {
      for (Dependency d : dependencies) {
        if (!d.isAvailable())
          return false;
      }
      return true;
    }

    /**
     * Gets whether the dependencies of the method at a frame are available.
     */
    public static boolean isAvailable(StackFrame frame) {
      return isAvailable(dependencies(frame));
    }

    /**
     * Detects missing dependencies from a linkage error.
     */
    public static Collection<Dependency> missingDependencies(LinkageError error) {
      /*
       * Align stack trace to live stack!
       *
       * NOTE: Since the stack trace has no information to safely infer the class loader needed to
       * resolve each trace element class, we have to find the common limit between stack trace and
       * live stack (where they don't match anymore), whose class will be the anchor to start the
       * resolution chain across the remaining stack trace (for example, if this method is invoked
       * from the catch site of `error`, the anchor corresponds to the class of that stack frame).
       */
      StackTraceElement[] trace = error.getStackTrace();
      Class<?> callerClass = null;
      int i = trace.length - 1;
      {
        StackFrame[] live = STACK_WALKER.walk(
            $ -> $
                .dropWhile(
                    $$ -> $$.getDeclaringClass() == Support.class /* Skips incidental frames */)
                .toArray(StackFrame[]::new));
        for (int j = live.length - 1; i >= 0 && j >= 0
            && trace[i].getClassName().equals(live[j].getClassName())
            && trace[i].getMethodName().equals(live[j].getMethodName()); i--, j--) {
          callerClass = live[j].getDeclaringClass();
        }
        if (callerClass == null) {
          callerClass = live[0].getDeclaringClass() /* Caller frame class as fallback */;
        }
      }

      // Find dependencies across the stack trace!
      var dependencies = new HashSet<Dependency>();
      for (; i >= 0; i--) {
        Class<?> traceClass = traceClass(trace[i], callerClass, error);
        if (traceClass == null) {
          continue;
        }

        callerClass = traceClass;
        try {
          String traceMethodName = trace[i].getMethodName();
          var overloading = false;
          for (Method method : traceClass.getDeclaredMethods()) {
            if (!method.getName().equals(traceMethodName)) {
              continue;
            }

            /*
             * NOTE: The stack trace model doesn't provide enough coordinates to identify the method
             * overload corresponding to the frame, so we have to harvest all of them. For the sake
             * of efficiency, only the first overload has its containment axis traversed; the
             * subsequent ones are searched for their direct annotations only.
             */
            dependencies.addAll(dependencies(method, overloading ? SearchStrategy.DIRECT
                : SearchStrategy.CONTAINMENT));
            overloading = true;
          }
          /*
           * NOTE: As methods depending on optional dependencies are expected to be annotated
           * (directly or indirectly), as soon as we find an annotated method, that's enough.
           */
          if (!dependencies.isEmpty()) {
            break;
          }
        } catch (LinkageError ignored) {
          /*
           * NOP: Reflection over trace classes which may depend on missing dependencies is expected
           * to fail.
           */
        }
      }
      return dependencies.stream()
          .filter(Predicate.not(Dependency::isAvailable))
          .toList();
    }

    private Support() {
    }
  }

  /**
   * Required {@linkplain Dependency dependencies}.
   *
   * @return (see {@link Dependency#getId()})
   */
  String[] value();
}
