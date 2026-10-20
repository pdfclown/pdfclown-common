/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Annots.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.annot;

import static java.util.Collections.unmodifiableList;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Objects.type;
import static org.pdfclown.common.util.function.Functions.toElseGet;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Annotation} utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Annots {
  /**
   * Annotation search strategy.
   *
   * @author Stefano Chizzolini
   */
  public enum SearchStrategy {
    /**
     * Searches the given annotated element only.
     */
    DIRECT,
    /**
     * Walks the containment axis, from a given annotated element up to its package.
     */
    CONTAINMENT
  }

  /**
   * Finds all the instances of an annotation in the given context.
   *
   * @param element
   *          Element where to start the search.
   * @param type
   *          Annotation type to search.
   * @param strategy
   *          Search strategy.
   */
  public static <A extends Annotation> @Unmodifiable List<A> findAll(Class<A> type,
      AnnotatedElement element, SearchStrategy strategy) {
    return switch (strategy) {
      case DIRECT -> toElseGet(element.getAnnotation(type), List::of, List::of);
      case CONTAINMENT -> {
        List<A> ret = null;
        while (element != null) {
          A annot = element.getAnnotation(type);
          if (annot != null) {
            if (ret == null) {
              ret = new ArrayList<>();
            }
            ret.add(annot);
          }

          if (element instanceof Method e) {
            element = e.getDeclaringClass();
          } else if (element instanceof Class<?> e) {
            if ((element = e.getEnclosingMethod()) != null
                || (element = e.getEnclosingClass()) != null) {
              continue;
            }

            element = e.getPackage();
          } else if (element instanceof Package) {
            break;
          } else
            throw unexpected(type(element));
        }
        yield ret != null ? unmodifiableList(ret) : List.of();
      }
    };
  }

  private Annots() {
  }
}
