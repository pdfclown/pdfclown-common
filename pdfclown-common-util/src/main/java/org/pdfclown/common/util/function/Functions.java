/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Functions.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.function;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.function.FailablePredicate;
import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.function.FailableSupplier;
import org.jspecify.annotations.Nullable;

/**
 * Function utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Functions {
  /**
   * Performs an action on an object, if defined.
   *
   * @param <T>
   *          Object type.
   * @param obj
   *          Object to consume.
   * @param action
   *          Action to perform.
   * @return {@code obj}
   * @apiNote Useful for fluent chaining, when {@code obj} needs to be passed downstream after
   *          applying {@code action}; same as {@link java.util.Optional#ifPresent(Consumer)}.
   * @see #ifPresentTry(Object, FailableConsumer)
   * @see #peek(Object, Consumer)
   */
  public static <T> @Nullable T ifPresent(@Nullable T obj, Consumer<? super T> action) {
    if (obj != null) {
      action.accept(obj);
    }
    return obj;
  }

  /**
   * Quietly performs an action on an object, if defined.
   *
   * @param <T>
   *          Object type.
   * @param obj
   *          Object to consume.
   * @param action
   *          Action to perform.
   * @return {@code obj}
   * @apiNote Useful for fluent chaining, when {@code obj} needs to be passed downstream after
   *          applying {@code action}; same as {@link java.util.Optional#ifPresent(Consumer)},
   *          except {@code action} exceptions are muted.
   * @see #ifPresent(Object, Consumer)
   * @see #peekQuietly(Object, FailableConsumer)
   */
  public static <T> @Nullable T ifPresentTry(@Nullable T obj, FailableConsumer<T, ?> action) {
    if (obj != null) {
      try {
        action.accept(obj);
      } catch (Throwable ex) {
        //NOP
      }
    }
    return obj;
  }

  /**
   * Performs an action on an object.
   *
   * @param <T>
   *          Object type.
   * @param obj
   *          Object to consume.
   * @param action
   *          Action to perform.
   * @return {@code obj}
   * @throws NullPointerException
   *           if {@code obj} is undefined (to avoid it, use {@link #ifPresent(Object, Consumer)}
   *           instead).
   * @apiNote Useful for fluent chaining, when {@code obj} needs to be passed downstream after
   *          applying {@code action}; same as {@link java.util.stream.Stream#peek(Consumer)},
   *          except {@code action} is executed immediately and unconditionally.
   * @see #peekQuietly(Object, FailableConsumer)
   * @see #ifPresent(Object, Consumer)
   */
  public static <T> T peek(T obj, Consumer<? super T> action) {
    action.accept(requireNonNull(obj, "obj"));
    return obj;
  }

  /**
   * Quietly performs an action on an object.
   *
   * @param <T>
   *          Object type.
   * @param obj
   *          Object to consume.
   * @param action
   *          Action to perform.
   * @return {@code obj}
   * @throws NullPointerException
   *           if {@code obj} is undefined (to avoid it, use
   *           {@link #ifPresentTry(Object, FailableConsumer)} instead).
   * @apiNote Useful for fluent chaining, when {@code obj} needs to be passed downstream after
   *          applying {@code action}; same as {@link java.util.stream.Stream#peek(Consumer)},
   *          except {@code action} is executed immediately and its exceptions are muted.
   * @see #peek(Object, Consumer)
   * @see #ifPresentTry(Object, FailableConsumer)
   */
  public static <T> T peekQuietly(T obj, FailableConsumer<T, ?> action) {
    requireNonNull(obj, "obj");
    try {
      action.accept(obj);
    } catch (Throwable ex) {
      //NOP
    }
    return obj;
  }

  /**
   * Quietly runs an operation.
   *
   * @param operation
   *          Operation to execute.
   */
  public static void quietly(FailableRunnable<?> operation) {
    quietly(operation, null);
  }

  /**
   * Quietly runs an operation.
   *
   * @param operation
   *          Operation to execute.
   * @param exceptionHandler
   *          Handles the exceptions thrown by {@code operation}.
   */
  public static void quietly(FailableRunnable<?> operation,
      @Nullable Consumer<Throwable> exceptionHandler) {
    try {
      operation.run();
    } catch (Throwable ex) {
      if (exceptionHandler != null) {
        exceptionHandler.accept(ex);
      }
    }
  }

  /**
   * Tests an object, if defined.
   * <p>
   * Complementary to {@link #testElseTrue(Object, Predicate)}.
   * </p>
   *
   * @param <T>
   *          Object type.
   * @param obj
   *          Object to test.
   * @param predicate
   *          Predicate to apply.
   * @return {@code true}, if {@code obj} is defined and {@code predicate} is true itself.
   * @see #tryTest(Object, FailablePredicate)
   */
  public static <T> boolean test(@Nullable T obj, Predicate<? super T> predicate) {
    return obj != null && predicate.test(obj);
  }

  /**
   * Tests an object, if defined.
   * <p>
   * Complementary to {@link #test(Object, Predicate)}.
   * </p>
   *
   * @param <T>
   *          Object type.
   * @param obj
   *          Object to test.
   * @param predicate
   *          Predicate to apply.
   * @return {@code true}, if {@code obj} is undefined or {@code predicate} is true itself.
   * @see #tryTestElseTrue(Object, FailablePredicate)
   */
  public static <T> boolean testElseTrue(@Nullable T obj, Predicate<? super T> predicate) {
    return obj == null || predicate.test(obj);
  }

  /**
   * Maps an object.
   *
   * @param <T>
   *          Object type.
   * @param <R>
   *          Result type.
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   * @throws NullPointerException
   *           if the mapping is undefined (to avoid it, use {@link #toOrNull(Object, Function)}
   *           instead).
   * @see #toOrNull(Object, Function)
   */
  public static <T, R> R to(T obj, Function<? super T, ? extends R> mapper) {
    return requireNonNull(toOrNull(obj, mapper));
  }

  /**
   * Maps an object.
   *
   * @param <T>
   *          Object type.
   * @param <R>
   *          Result type.
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   * @param defaultResult
   *          Result if {@code obj} or {@code mapper}'s result are undefined.
   * @see #tryToElse(Object, FailableFunction, Object)
   */
  public static <T, R> R toElse(@Nullable T obj, Function<? super T, ? extends @Nullable R> mapper,
      R defaultResult) {
    return requireNonNullElse(toOrNull(obj, mapper), defaultResult);
  }

  /**
   * Maps an object.
   *
   * @param <T>
   *          Object type.
   * @param <R>
   *          Result type.
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   * @param defaultSupplier
   *          Result supplier if {@code obj} or {@code mapper}'s result are undefined.
   * @throws NullPointerException
   *           if the mapping is undefined (to avoid it, use
   *           {@link #toElseGetOrNull(Object, Function, Supplier)} instead).
   * @see #toElseGetOrNull(Object, Function, Supplier)
   * @see #tryToElseGet(Object, FailableFunction, Supplier)
   */
  public static <T, R> R toElseGet(@Nullable T obj,
      Function<? super T, ? extends @Nullable R> mapper, Supplier<? extends R> defaultSupplier) {
    return requireNonNull(toElseGetOrNull(obj, mapper, defaultSupplier));
  }

  /**
   * Maps an object.
   *
   * @param <T>
   *          Object type.
   * @param <R>
   *          Result type.
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   * @param defaultSupplier
   *          Result supplier if {@code obj} or {@code mapper}'s result are undefined.
   * @see #toElseGet(Object, Function, Supplier)
   * @see #tryToElseGetOrNull(Object, FailableFunction, Supplier)
   */
  public static <T, R> @Nullable R toElseGetOrNull(@Nullable T obj,
      Function<? super T, ? extends @Nullable R> mapper,
      Supplier<? extends @Nullable R> defaultSupplier) {
    R ret;
    return (ret = toOrNull(obj, mapper)) != null ? ret : defaultSupplier.get();
  }

  /**
   * Maps an object.
   *
   * @param <T>
   *          Object type.
   * @param <R>
   *          Result type.
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   * @see #to(Object, Function)
   * @see #tryTo(Object, FailableFunction)
   */
  public static <T, R> @Nullable R toOrNull(@Nullable T obj,
      Function<? super T, ? extends @Nullable R> mapper) {
    return obj != null ? mapper.apply(obj) : null;
  }

  /**
   * Quietly gets a result from a supplier.
   *
   * @param <R>
   *          Result type.
   * @param supplier
   *          Result supplier.
   * @return Result of {@code supplier}, or {@code null} if failed.
   */
  public static <R> @Nullable R tryGet(FailableSupplier<? extends @Nullable R, ?> supplier) {
    try {
      return supplier.get();
    } catch (Throwable ex) {
      return null;
    }
  }

  /**
   * Quietly gets a result from a supplier.
   *
   * @param <R>
   *          Result type.
   * @param supplier
   *          Result supplier.
   * @param defaultResult
   *          Result in case {@code supplier} fails or its result is undefined.
   * @return Result of {@code supplier}, if not {@code null}; otherwise, {@code defaultResult}.
   */
  public static <R> R tryGetElse(FailableSupplier<? extends @Nullable R, ?> supplier,
      R defaultResult) {
    return requireNonNullElse(tryGet(supplier), defaultResult);
  }

  /**
   * Quietly tests an object, if defined.
   * <p>
   * Complementary to {@link #tryTestElseTrue(Object, FailablePredicate)}.
   * </p>
   *
   * @param obj
   *          Object to test.
   * @param predicate
   *          Predicate to apply.
   * @return {@code true}, if {@code obj} is defined and {@code predicate} is true itself.
   * @see #test(Object, Predicate)
   */
  public static <T> boolean tryTest(@Nullable T obj, FailablePredicate<? super T, ?> predicate) {
    return doTryTestElse(obj, predicate, false);
  }

  /**
   * Quietly tests an object, if defined.
   * <p>
   * Complementary to {@link #tryTest(Object, FailablePredicate)}.
   * </p>
   *
   * @param obj
   *          Object to test.
   * @param predicate
   *          Predicate to apply.
   * @return {@code true}, if {@code obj} is undefined or {@code predicate} is true itself.
   * @see #testElseTrue(Object, Predicate)
   */
  public static <T> boolean tryTestElseTrue(@Nullable T obj,
      FailablePredicate<? super T, ?> predicate) {
    return doTryTestElse(obj, predicate, true);
  }

  /**
   * Quietly maps an object.
   *
   * @param <T>
   *          Object type.
   * @param <R>
   *          Result type.
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   * @see #toOrNull(Object, Function)
   */
  public static <T, R> @Nullable R tryTo(@Nullable T obj,
      FailableFunction<? super T, ? extends @Nullable R, ?> mapper) {
    if (obj != null) {
      try {
        return mapper.apply(obj);
      } catch (Throwable ex) {
        // NOP
      }
    }
    return null;
  }

  /**
   * Quietly maps an object.
   *
   * @param <T>
   *          Object type.
   * @param <R>
   *          Result type.
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   * @param defaultResult
   *          Result if {@code obj} or {@code mapper}'s result are undefined.
   * @see #toElse(Object, Function, Object)
   */
  public static <T, R> R tryToElse(@Nullable T obj,
      FailableFunction<? super T, ? extends @Nullable R, ?> mapper, R defaultResult) {
    return requireNonNullElse(tryTo(obj, mapper), defaultResult);
  }

  /**
   * Quietly maps an object.
   *
   * @param <T>
   *          Object type.
   * @param <R>
   *          Result type.
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   * @param defaultSupplier
   *          Result supplier if {@code obj} or {@code mapper}'s result are undefined.
   * @throws NullPointerException
   *           if the mapping is undefined (to avoid it, use
   *           {@link #tryToElseGetOrNull(Object, FailableFunction, Supplier)} instead).
   * @see #tryToElseGetOrNull(Object, FailableFunction, Supplier)
   * @see #toElseGet(Object, Function, Supplier)
   */
  public static <T, R> R tryToElseGet(@Nullable T obj,
      FailableFunction<? super T, ? extends @Nullable R, ?> mapper,
      Supplier<? extends R> defaultSupplier) {
    return requireNonNull(tryToElseGetOrNull(obj, mapper, defaultSupplier));
  }

  /**
   * Quietly maps an object.
   *
   * @param <T>
   *          Object type.
   * @param <R>
   *          Result type.
   * @param obj
   *          Object to map.
   * @param mapper
   *          Object mapping function.
   * @param defaultSupplier
   *          Result supplier if {@code obj} or {@code mapper}'s result are undefined.
   * @see #tryToElseGet(Object, FailableFunction, Supplier)
   * @see #toElseGetOrNull(Object, Function, Supplier)
   */
  public static <T, R> @Nullable R tryToElseGetOrNull(@Nullable T obj,
      FailableFunction<? super T, ? extends @Nullable R, ?> mapper,
      Supplier<? extends @Nullable R> defaultSupplier) {
    R ret;
    return (ret = tryTo(obj, mapper)) != null ? ret : defaultSupplier.get();
  }

  private static <T> boolean doTryTestElse(@Nullable T obj,
      FailablePredicate<? super T, ?> predicate, boolean defaultResult) {
    if (obj != null) {
      try {
        return predicate.test(obj);
      } catch (Throwable ex) {
        return false;
      }
    }
    return defaultResult;
  }

  private Functions() {
  }
}
