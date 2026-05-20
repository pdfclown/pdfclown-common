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

import static java.util.stream.Collectors.joining;
import static org.pdfclown.common.util.Chars.COMMA;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Objects.fqnd;
import static org.pdfclown.common.util.Strings.NULL;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.function.Functions.toElse;

import java.lang.StackWalker.StackFrame;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.Objects;

/**
 * Reflection utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Reflects {
  private static final StackWalker STACK_WALKER =
      StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

  /**
   * Calls the method on the target object.
   *
   * @param <T>
   *          Return type.
   * @return Value returned by the call ({@code null}, if failed — to disambiguate the case where
   *         {@code null} is a valid return value, use
   *         {@link #callOrThrow(Object, String, Class[], Object[])} instead)).
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
   * Gets the class of the caller who invoked the method that invoked this one.
   *
   * @see #callerFrame()
   */
  public static Class<?> callerClass() {
    return STACK_WALKER.getCallerClass();
  }

  /**
   * Gets the stack frame of the caller who invoked the method that invoked this one.
   * <p>
   * {@link StackFrame#getDeclaringClass()} is supported.
   * </p>
   *
   * @see #callerClass()
   * @see #stackFrame(Predicate)
   */
  public static StackFrame callerFrame() {
    return stackFrame($ -> true).orElseThrow();
  }

  /**
   * Calls the method on the target object.
   *
   * @param <T>
   *          Return type.
   * @throws RuntimeException
   *           if the call fails.
   */
  @SuppressWarnings("unchecked")
  public static <T extends @Nullable Object> T callOrThrow(final Object obj,
      final String methodName, Class<?> @Nullable [] paramTypes, Object @Nullable [] args)
      throws NoSuchMethodException, IllegalAccessException {
    try {
      return (T) MethodUtils.invokeExactMethod(obj, methodName, args, paramTypes);
    } catch (InvocationTargetException ex) {
      throw runtime("Call to `{}.{}({})` FAILED", fqnd(obj), methodName, toElse(paramTypes,
          $ -> Arrays.stream($).map(Objects::literal).collect(joining(S + COMMA + SPACE)), NULL),
          ex.getCause());
    }
  }

  /**
   * Gets a property value from the object.
   *
   * @param <T>
   *          Return type.
   * @param getter
   *          Method name of the property getter (for example "getMyProperty").
   * @throws RuntimeException
   *           if the call fails.
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
   * Gets the method corresponding to the stack frame.
   */
  public static Optional<Method> method(StackFrame frame) {
    try {
      return Optional.of(frame.getDeclaringClass().getDeclaredMethod(frame.getMethodName(),
          frame.getMethodType().parameterArray()));
    } catch (NoSuchMethodException ex) {
      return Optional.empty();
    }
  }

  /**
   * Gets the fully-qualified method name corresponding to the stack frame.
   */
  public static String methodFqn(StackFrame frame) {
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
        // Skip incidental frames!
        .dropWhile($$ -> $$.getDeclaringClass() == Reflects.class)
        // Skip current frame!
        .skip(1)
        .filter(selector)
        .findFirst());
  }

  private Reflects() {
  }
}
