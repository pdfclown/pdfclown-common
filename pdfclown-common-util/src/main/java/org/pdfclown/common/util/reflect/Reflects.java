/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Reflects.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.reflect;

import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.function.Functions.toOrNull;

import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

/**
 * Reflection utilities.
 *
 * @author Stefano Chizzolini
 */
@SuppressWarnings("TypeParameterUnusedInFormals")
public final class Reflects {
  private static final StackWalker STACK_WALKER = StackWalker.getInstance(
      StackWalker.Option.RETAIN_CLASS_REFERENCE);

  /**
   * Gets the stack frame of the caller who invoked the method that invoked this one.
   * <p>
   * {@link StackFrame#getDeclaringClass()} is supported.
   * </p>
   *
   * @see #stackFrame(Predicate)
   */
  public static StackFrame callerFrame() {
    return stackFrame($ -> true).orElseThrow();
  }

  /**
   * Gets the fully-qualified method name.
   */
  public static String fqn(Method method) {
    return method.getDeclaringClass().getName() + DOT + method.getName();
  }

  /**
   * Gets the fully-qualified method name corresponding to a stack frame.
   */
  public static String fqn(StackFrame frame) {
    return frame.getClassName() + DOT + frame.getMethodName();
  }

  /**
   * Selects a frame walking down the call stack.
   * <p>
   * {@link StackFrame#getDeclaringClass()} is supported.
   * </p>
   * <p>
   * The call stack looks like this:
   * </p>
   * <pre>
   * ## INCIDENTAL FRAMES ##
   *   frame[x+n]    &lt;-- we are HERE (Reflects.stackFrame(Predicate))
   *   . . .         &lt;-- Reflects...(...)
   *   frame[x+1]    &lt;-- Reflects...(...)
   *   frame[x]      &lt;-- this is YOU (current frame)
   * ## SELECTABLE FRAMES ##
   *   frame[x-1]    &lt;-- this is the first frame to evaluate (caller frame)
   *   frame[x-2]
   *   . . .
   *   frame[0]      &lt;-- this is the last frame to evaluate</pre>
   *
   * @param selector
   *          Evaluates frames for selection. As soon as {@code true}, the walk stops.
   * @see #callerFrame()
   */
  public static Optional<StackFrame> stackFrame(Predicate<StackFrame> selector) {
    return STACK_WALKER.walk($ -> $
        .dropWhile($$ -> $$.getDeclaringClass() == Reflects.class /* Skips incidental frames */)
        .skip(1 /* Skips current frame */)
        .filter(selector)
        .findFirst());
  }

  /**
   * Gets the method corresponding to a stack frame.
   */
  public static Optional<Method> stackMethod(StackFrame frame) {
    try {
      return Optional.of(frame.getDeclaringClass().getDeclaredMethod(frame.getMethodName(),
          frame.getMethodType().parameterArray()));
    } catch (NoSuchMethodException ex) {
      return Optional.empty();
    }
  }

  /**
   * Gets the class corresponding to a trace element.
   *
   * @param caller
   *          Class which called {@code traceElement}. The initial caller should be obtained from
   *          the live call stack, at the corresponding call site; subsequent callers, within the
   *          reversely ordered stack trace, should be resolved iteratively through this method.
   * @param throwable
   *          Throwable that {@code traceElement} {@linkplain Throwable#getStackTrace() belongs to}.
   * @implNote Since the trace element doesn't provide the corresponding class loader, the class is
   *           resolved trying this sequence:
   *           <ol>
   *           <li>{@code caller}'s class loader</li>
   *           <li>{@code traceElement}'s module class loader</li>
   *           <li>current thread's {@linkplain Thread#getContextClassLoader() context class
   *           loader}</li>
   *           <li>{@code throwable}'s class loader</li>
   *           </ol>
   */
  public static @Nullable Class<?> traceClass(StackTraceElement traceElement, Class<?> caller,
      Throwable throwable) {
    ClassLoader loader;
    int i = 0;
    loop: while (true) {
      switch (i++) {
        case 0 -> loader = caller.getClassLoader();
        case 1 -> {
          /*
           * WARNING: We have to distinguish between `null` class loader (corresponding to
           * bootstrap) and missing module, so the two CANNOT conflate into a single mapping
           * expression.
           */
          Module module = toOrNull(traceElement.getModuleName(),
              $ -> ModuleLayer.boot().findModule($).orElse(null));
          if (module == null) {
            continue;
          }

          loader = module.getClassLoader();
        }
        case 2 -> {
          loader = Thread.currentThread().getContextClassLoader();
          if (loader == null /* unset */) {
            continue;
          }
        }
        case 3 -> loader = throwable.getClass().getClassLoader();
        default -> {
          break loop;
        }
      }
      try {
        return Class.forName(traceElement.getClassName(), false, loader);
      } catch (ClassNotFoundException | LinkageError ignored) {
        // NOP
      }
    }
    return null;
  }

  private Reflects() {
  }
}
