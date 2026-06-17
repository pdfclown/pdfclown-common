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

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.pdfclown.common.util.Chars.COMMA;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Objects.fqnd;
import static org.pdfclown.common.util.Strings.EMPTY;
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
@SuppressWarnings("TypeParameterUnusedInFormals")
public final class Reflects {
  private static final StackWalker STACK_WALKER =
      StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

  /**
   * Calls a static method on the target class.
   *
   * @throws RuntimeException
   *           if the call failed.
   * @see #tryCall(Class, String, Class[], Object[])
   * @see #get(Class, String, Class[], Object[])
   */
  public static void call(Class<?> type, String methodName) {
    call(type, methodName, null, null);
  }

  /**
   * Calls a static method on the target class.
   *
   * @throws RuntimeException
   *           if the call failed.
   * @see #tryCall(Class, String, Class[], Object[])
   * @see #get(Class, String, Class[], Object[])
   */
  public static void call(Class<?> type, String methodName, Class<?> @Nullable [] paramTypes,
      @Nullable Object @Nullable [] args) {
    getOrNull(type, methodName, paramTypes, args);
  }

  /**
   * Calls a method on the target object.
   *
   * @throws RuntimeException
   *           if the call failed.
   * @see #tryCall(Object, String, Class[], Object[])
   * @see #get(Object, String, Class[], Object[])
   */
  public static void call(Object obj, String methodName) {
    call(obj, methodName, null, null);
  }

  /**
   * Calls a method on the target object.
   *
   * @throws RuntimeException
   *           if the call failed.
   * @see #tryCall(Object, String, Class[], Object[])
   * @see #get(Object, String, Class[], Object[])
   */
  public static void call(Object obj, String methodName, Class<?> @Nullable [] paramTypes,
      @Nullable Object @Nullable [] args) {
    getOrNull(obj, methodName, paramTypes, args);
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
   * Instantiates a class.
   *
   * @param <T>
   *          Return type.
   * @throws RuntimeException
   *           if the instantiation failed.
   * @see #tryCreate(Class)
   */
  public static <T> T create(Class<T> type) {
    return create(type, null, null);
  }

  /**
   * Instantiates a class.
   *
   * @param <T>
   *          Return type.
   * @throws RuntimeException
   *           if the instantiation failed.
   * @see #tryCreate(Class, Class[], Object[])
   */
  public static <T> T create(Class<T> type, Class<?> @Nullable [] paramTypes,
      @Nullable Object @Nullable [] args) {
    try {
      return type.getDeclaredConstructor(paramTypes).newInstance(args);
    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException
        | InstantiationException ex) {
      throw invocationException(type, "<init>", paramTypes, ex);
    }
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
   * Calls a static method on the target class, returning its result.
   *
   * @param <T>
   *          Return type.
   * @throws NullPointerException
   *           if the call returned {@code null}.
   * @throws RuntimeException
   *           if the call failed.
   * @see #getOrNull(Class, String, Class[], Object[])
   * @see #tryGet(Class, String, Class[], Object[])
   * @see #call(Class, String, Class[], Object[])
   */
  public static <T> T get(Class<?> type, String methodName) {
    return get(type, methodName, null, null);
  }

  /**
   * Calls a static method on the target class, returning its result.
   *
   * @param <T>
   *          Return type.
   * @throws NullPointerException
   *           if the call returned {@code null}.
   * @throws RuntimeException
   *           if the call failed.
   * @see #getOrNull(Class, String, Class[], Object[])
   * @see #tryGet(Class, String, Class[], Object[])
   * @see #call(Class, String, Class[], Object[])
   */
  public static <T> T get(Class<?> type, String methodName, Class<?> @Nullable [] paramTypes,
      @Nullable Object @Nullable [] args) {
    return requireNonNull(getOrNull(type, methodName, paramTypes, args));
  }

  /**
   * Calls a method on the target object, returning its result.
   *
   * @param <T>
   *          Return type.
   * @throws NullPointerException
   *           if the call returned {@code null}.
   * @throws RuntimeException
   *           if the call failed.
   * @see #getOrNull(Object, String, Class[], Object[])
   * @see #tryGet(Object, String, Class[], Object[])
   * @see #call(Object, String, Class[], Object[])
   */
  public static <T> T get(Object obj, String methodName) {
    return get(obj, methodName, null, null);
  }

  /**
   * Calls a method on the target object, returning its result.
   *
   * @param <T>
   *          Return type.
   * @throws NullPointerException
   *           if the call returned {@code null}.
   * @throws RuntimeException
   *           if the call failed.
   * @see #getOrNull(Object, String, Class[], Object[])
   * @see #tryGet(Object, String, Class[], Object[])
   * @see #call(Object, String, Class[], Object[])
   */
  public static <T> T get(Object obj, String methodName, Class<?> @Nullable [] paramTypes,
      @Nullable Object @Nullable [] args) {
    return requireNonNull(getOrNull(obj, methodName, paramTypes, args));
  }

  /**
   * Calls a static method on the target class, returning its result.
   *
   * @param <T>
   *          Return type.
   * @throws RuntimeException
   *           if the call failed.
   * @see #get(Class, String, Class[], Object[])
   * @see #tryGet(Class, String, Class[], Object[])
   * @see #call(Class, String, Class[], Object[])
   */
  public static <T> @Nullable T getOrNull(Class<?> type, String methodName) {
    return getOrNull(type, methodName, null, null);
  }

  /**
   * Calls a static method on the target class, returning its result.
   *
   * @param <T>
   *          Return type.
   * @throws RuntimeException
   *           if the call failed.
   * @see #get(Class, String, Class[], Object[])
   * @see #tryGet(Class, String, Class[], Object[])
   * @see #call(Class, String, Class[], Object[])
   */
  @SuppressWarnings("unchecked")
  public static <T> @Nullable T getOrNull(Class<?> type, String methodName,
      Class<?> @Nullable [] paramTypes, @Nullable Object @Nullable [] args) {
    try {
      return (T) MethodUtils.invokeExactStaticMethod(type, methodName, args, paramTypes);
    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
      throw invocationException(type, methodName, paramTypes, ex);
    }
  }

  /**
   * Calls a method on the target object, returning its result.
   *
   * @param <T>
   *          Return type.
   * @throws RuntimeException
   *           if the call failed.
   * @see #get(Object, String, Class[], Object[])
   * @see #tryGet(Object, String, Class[], Object[])
   * @see #call(Object, String, Class[], Object[])
   */
  public static <T> @Nullable T getOrNull(Object obj, String methodName) {
    return getOrNull(obj, methodName, null, null);
  }

  /**
   * Calls a method on the target object, returning its result.
   *
   * @param <T>
   *          Return type.
   * @throws RuntimeException
   *           if the call failed.
   * @see #get(Object, String, Class[], Object[])
   * @see #tryGet(Object, String, Class[], Object[])
   * @see #call(Object, String, Class[], Object[])
   */
  @SuppressWarnings("unchecked")
  public static <T> @Nullable T getOrNull(Object obj, String methodName,
      Class<?> @Nullable [] paramTypes, @Nullable Object @Nullable [] args) {
    try {
      return (T) MethodUtils.invokeExactMethod(obj, methodName, args, paramTypes);
    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
      throw invocationException(obj, methodName, paramTypes, ex);
    }
  }

  /**
   * Gets the method corresponding to a signature.
   */
  public static Optional<Method> method(Class<?> type, String methodName,
      Class<?> @Nullable... paramTypes) {
    try {
      return Optional.of(type.getMethod(methodName, paramTypes));
    } catch (NoSuchMethodException ex) {
      return Optional.empty();
    }
  }

  /**
   * Gets the method corresponding to a stack frame.
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

  /**
   * Safely calls a static method on the target class.
   *
   * @return Whether the call succeeded.
   * @see #call(Class, String, Class[], Object[])
   * @see #get(Class, String, Class[], Object[])
   */
  public static boolean tryCall(Class<?> type, String methodName) {
    return tryCall(type, methodName, null, null);
  }

  /**
   * Safely calls a static method on the target class.
   *
   * @return Whether the call succeeded.
   * @see #call(Class, String, Class[], Object[])
   * @see #get(Class, String, Class[], Object[])
   */
  public static boolean tryCall(Class<?> type, String methodName, Class<?> @Nullable [] paramTypes,
      @Nullable Object @Nullable [] args) {
    try {
      call(type, methodName, paramTypes, args);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  /**
   * Safely calls a method on the target object.
   *
   * @return Whether the call succeeded.
   * @see #call(Object, String, Class[], Object[])
   * @see #get(Object, String, Class[], Object[])
   */
  public static boolean tryCall(Object obj, String methodName) {
    return tryCall(obj, methodName, null, null);
  }

  /**
   * Safely calls a method on the target object.
   *
   * @return Whether the call succeeded.
   * @see #call(Object, String, Class[], Object[])
   * @see #get(Object, String, Class[], Object[])
   */
  public static boolean tryCall(Object obj, String methodName, Class<?> @Nullable [] paramTypes,
      @Nullable Object @Nullable [] args) {
    try {
      call(obj, methodName, paramTypes, args);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  /**
   * Safely instantiates a class.
   *
   * @param <T>
   *          Return type.
   * @return {@code null}, if failed.
   * @see #create(Class)
   */
  public static <T> @Nullable T tryCreate(Class<T> type) {
    return tryCreate(type, null, null);
  }

  /**
   * Safely instantiates a class.
   *
   * @param <T>
   *          Return type.
   * @return {@code null}, if failed.
   * @see #create(Class, Class[], Object[])
   */
  public static <T> @Nullable T tryCreate(Class<T> type, Class<?> @Nullable [] paramTypes,
      @Nullable Object @Nullable [] args) {
    try {
      return create(type, paramTypes, args);
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Safely calls a static method on the target class, returning its result.
   *
   * @param <T>
   *          Return type.
   * @return Value returned by the call ({@code null}, if failed — to disambiguate the case where
   *         {@code null} is a valid return value, use {@link #getOrNull(Class, String)} instead).
   */
  public static <T> @Nullable T tryGet(Class<?> type, String methodName) {
    return tryGet(type, methodName, null, null);
  }

  /**
   * Safely calls a static method on the target class, returning its result.
   *
   * @param <T>
   *          Return type.
   * @return Value returned by the call ({@code null}, if failed — to disambiguate the case where
   *         {@code null} is a valid return value, use
   *         {@link #getOrNull(Class, String, Class[], Object[])} instead).
   */
  public static <T> @Nullable T tryGet(Class<?> type, String methodName,
      Class<?> @Nullable [] paramTypes, @Nullable Object @Nullable [] args) {
    try {
      return getOrNull(type, methodName, paramTypes, args);
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Safely calls a method on the target object, returning its result.
   *
   * @param <T>
   *          Return type.
   * @return Value returned by the call ({@code null}, if failed — to disambiguate the case where
   *         {@code null} is a valid return value, use {@link #getOrNull(Object, String)} instead).
   */
  public static <T> @Nullable T tryGet(Object obj, String methodName) {
    return tryGet(obj, methodName, null, null);
  }

  /**
   * Safely calls a method on the target object, returning its result.
   *
   * @param <T>
   *          Return type.
   * @return Value returned by the call ({@code null}, if failed — to disambiguate the case where
   *         {@code null} is a valid return value, use
   *         {@link #getOrNull(Object, String, Class[], Object[])} instead).
   */
  public static <T> @Nullable T tryGet(Object obj, String methodName,
      Class<?> @Nullable [] paramTypes, @Nullable Object @Nullable [] args) {
    try {
      return getOrNull(obj, methodName, paramTypes, args);
    } catch (Exception ex) {
      return null;
    }
  }

  private static RuntimeException invocationException(Object obj, String methodName,
      Class<?> @Nullable [] paramTypes, Exception ex) {
    return runtime("Invocation to `{}.{}({})` FAILED", fqnd(obj), methodName, toElse(paramTypes,
        $ -> Arrays.stream($).map(Objects::literal).collect(joining(S + COMMA + SPACE)), EMPTY),
        ex instanceof InvocationTargetException ? ex.getCause() : ex);
  }

  private Reflects() {
  }
}
