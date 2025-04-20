/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (Reflects.java) is part of pdfclown-common-util module in pdfClown Common project (this
  Program).

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

import static org.pdfclown.common.util.Exceptions.runtime;

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
   * Gets the calling frame.
   */
  public static Optional<StackFrame> callerFrame() {
    return callerFrame($ -> true);
  }

  /**
   * Gets the frame chosen walking down the call stack.
   *
   * @param evaluator
   *          Frame chooser.
   * @implNote The call stack looks like this: <pre>{@code
   *## reflection frames ##
   *   . . .
   *  <caller[x+n-1]>      <-- we are HERE (Reflects.callerFrame(..))
   *   . . .
   *  <caller[x+1]>        <-- Reflects...(..)
   *## actual frames ##
   *  <caller[x]>          <-- this is YOU
   *  <caller[x-1]>        <-- this is the first frame to evaluate
   *  <caller[x-2]>
   *   . . .
   *  <caller[0]>          <-- this is the last frame to evaluate}</pre>
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
   * Calls the given method on the target object.
   *
   * @param <T>
   *          Return type.
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
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
   * Gets a property value from the given object.
   *
   * @param <T>
   *          Return type.
   * @param obj
   * @param getter
   *          Method name of the property getter (eg "getMyProperty").
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
