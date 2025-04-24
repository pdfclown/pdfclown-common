/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (Reflects.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util.lang;

import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Reflection utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Reflects {
  // SourceFQN: org.pdfclown.common.util.lang.Reflects.callerFrame(..)
  /**
   * Gets the calling frame.
   */
  public static Optional<StackFrame> callerFrame() {
    return callerFrame($ -> true);
  }

  // SourceFQN: org.pdfclown.common.util.lang.Reflects.callerFrame(..)
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

  // SourceFQN: org.pdfclown.common.util.lang.Reflects.method(..)
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
