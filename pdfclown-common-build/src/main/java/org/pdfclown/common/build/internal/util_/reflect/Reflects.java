/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Reflects.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_.reflect;

import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;

import java.lang.StackWalker.StackFrame;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jspecify.annotations.Nullable;

/**
 * Reflection utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Reflects {
  /**
   * Calls the given method on the target object.
   *
   * @param <T>
   *          Return type.
   * @return {@code null}, if failed (NOTE: In case {@code null} is a valid return value, use
   *         {@link #callOrThrow(Object, String, Class[], Object[])} instead).
   */
  public static <T> @Nullable T call(final Object obj, final String methodName,
      Class<?> @Nullable [] paramTypes, Object @Nullable [] args) {
    try {
      return callOrThrow(obj, methodName, paramTypes, args);
    } catch (NoSuchMethodException | IllegalAccessException ex) {
      return null;
    }
  }

  /**
   * Calls the given method on the target object.
   *
   * @param <T>
   *          Return type.
   */
  @SuppressWarnings("unchecked")
  public static <T> @Nullable T callOrThrow(final Object obj, final String methodName,
      Class<?> @Nullable [] paramTypes, Object @Nullable [] args)
      throws NoSuchMethodException, IllegalAccessException {
    try {
      return (T) MethodUtils.invokeExactMethod(obj, methodName, args, paramTypes);
    } catch (InvocationTargetException ex) {
      throw new RuntimeException(ex.getCause());
    }
  }

  /**
   * Gets the calling frame.
   */
  public static StackFrame callerFrame() {
    //noinspection OptionalGetWithoutIsPresent : Exception should NEVER happen.
    return callerFrame($ -> true).get();
  }

  /**
   * Gets the frame chosen walking down the call stack.
   *
   * @param evaluator
   *          Frame chooser.
   * @implNote The call stack looks like this: <pre>
   * ## reflection frames ##
   *    . . .
   *   &lt;caller[x+n-1]&gt;      &lt;-- we are HERE (Reflects.callerFrame(..))
   *    . . .
   *   &lt;caller[x+1]&gt;        &lt;-- Reflects...(..)
   * ## actual frames ##
   *   &lt;caller[x]&gt;          &lt;-- this is YOU
   *   &lt;caller[x-1]&gt;        &lt;-- this is the first frame to evaluate
   *   &lt;caller[x-2]&gt;
   *    . . .
   *   &lt;caller[0]&gt;          &lt;-- this is the last frame to evaluate</pre>
   */
  public static Optional<StackFrame> callerFrame(Predicate<StackFrame> evaluator) {
    var step = new MutableInt(-1);
    return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk($ -> $
        .skip(1) /* Skips the current frame */
        .filter($$ -> {
          switch (step.intValue()) {
            case -1:
              // Skip infrastructural frames!
              if (!$$.getClassName().equals(Reflects.class.getName())) {
                step.increment();
              }
              break;
            case 0:
              // Skip the primary caller!
              step.increment();
              break;
            default:
              if (evaluator.test($$))
                return true;
          }
          return false;
        })
        .findFirst());
  }

  /**
   * Gets a property value from the given object.
   *
   * @param <T>
   *          Return type.
   * @param getter
   *          Method name of the property getter (e.g. "getMyProperty").
   */
  @SuppressWarnings({ "unchecked" })
  public static <T> T get(Object obj, String getter) {
    try {
      return (T) obj.getClass().getMethod(getter, (Class<?>[]) null).invoke(obj);
    } catch (Exception ex) {
      throw runtime(ex);
    }
  }

  /**
   * Gets the method corresponding to the given stack frame.
   */
  public static Method method(StackFrame frame) {
    try {
      return frame.getDeclaringClass().getDeclaredMethod(frame.getMethodName(),
          frame.getMethodType().parameterArray());
    } catch (NoSuchMethodException ex) {
      throw new RuntimeException(ex);
    }
  }

  private Reflects() {
  }
}
