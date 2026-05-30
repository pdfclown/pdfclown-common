/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Comparators.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.temp.util.collect;

import static java.lang.Math.subtractExact;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.unexpected;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import org.pdfclown.common.util.Cloneable;

/**
 * Comparison utilities.
 *
 * @author Stefano Chizzolini
 */
public class Comparators {
  /**
   * Hierarchical type comparator.
   *
   * @author Stefano Chizzolini
   */
  @SuppressWarnings("rawtypes")
  public static class HierarchicalTypeComparator implements Comparator<Class> {
    /**
     * Additional ordering criteria for {@link HierarchicalTypeComparator}.
     *
     * @author Stefano Chizzolini
     */
    public static class Priorities {
      /**
       * Type comparator based on explicit priorities.
       *
       * @author Stefano Chizzolini
       */
      public static class TypePriorityComparator implements Comparator<Class>, Cloneable {
        private int minPriority;
        private int maxPriority;
        private HashMap<Class, Integer> priorities = new HashMap<>();

        private TypePriorityComparator() {
        }

        @Override
        @SuppressWarnings("unchecked")
        public TypePriorityComparator clone() {
          try {
            var ret = (TypePriorityComparator) super.clone();
            ret.priorities = (HashMap<Class, Integer>) ret.priorities.clone();
            return ret;
          } catch (CloneNotSupportedException ex) {
            throw runtime(ex);
          }
        }

        @Override
        public int compare(Class o1, Class o2) {
          return priorities.getOrDefault(o1, 0) - priorities.getOrDefault(o2, 0);
        }

        /**
         * Gets the priority associated to the type.
         *
         * @return {@code 0}, if no priority is associated to {@code type}.
         */
        public int get(Class<?> type) {
          return getOrDefault(type, 0);
        }

        /**
         * Gets the priority associated to the type.
         *
         * @return {@code defaultValue}, if no priority is associated to {@code type}.
         */
        public int getOrDefault(Class<?> type, int defaultValue) {
          return priorities.getOrDefault(type, defaultValue);
        }

        /**
         * Associates a priority to the type.
         */
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public TypePriorityComparator set(int priority, Class<?> type) {
          if (priority < minPriority) {
            subtractExact(priority, maxPriority) /* Checks underflow */;
            subtractExact(maxPriority, priority) /* Checks overflow */;
            minPriority = priority;
          } else if (priority > maxPriority) {
            subtractExact(minPriority, priority) /* Checks underflow */;
            subtractExact(priority, minPriority) /* Checks overflow */;
            maxPriority = priority;
          }
          priorities.put(type, priority);
          return this;
        }

        /**
         * Associates a priority to the types.
         */
        public TypePriorityComparator set(int priority, Class<?>... types) {
          for (var type : types) {
            set(priority, type);
          }
          return this;
        }

        /**
         * Associates a sequence of priorities to the types.
         */
        public TypePriorityComparator setInOrder(int priority, Class<?>... types) {
          for (var type : types) {
            set(priority++, type);
          }
          return this;
        }
      }

      private static final Comparator<Class> COMPARATOR__INTERFACE_PRIORITY = new Comparator<>() {
        @Override
        public int compare(Class o1, Class o2) {
          return interfacePriority(o1) - interfacePriority(o2);
        }

        private int interfacePriority(Class<?> type) {
          return type.isInterface() ? 1 : 0;
        }
      };

      /**
       * Compares types by explicit priority.
       */
      public static TypePriorityComparator explicitPriority() {
        return new TypePriorityComparator();
      }

      /**
       * Compares types prioritizing concrete types over interfaces.
       */
      public static Comparator<Class> interfacePriority() {
        return COMPARATOR__INTERFACE_PRIORITY;
      }
    }

    @SuppressWarnings("unchecked")
    private static final HierarchicalTypeComparator INSTANCE = new HierarchicalTypeComparator(
        ($1, $2) -> {
          // Prioritize specialized types over super types!
          if ($1.isAssignableFrom($2))
            return 1;
          else if ($2.isAssignableFrom($1))
            return -1;
          else
            return 0;
        });

    private final Comparator<Class> base;

    private HierarchicalTypeComparator(Comparator<Class> base) {
      this.base = base;
    }

    @Override
    public int compare(Class o1, Class o2) {
      if (o1 == o2)
        return 0;

      int ret = base.compare(o1, o2);
      if (ret == 0)
        throw unexpected(ret, "unable to decide over type priority between `{}` and `{}`", o1, o2);

      return ret;
    }

    @Override
    @SuppressWarnings("NullableProblems" /* false positive */)
    public HierarchicalTypeComparator thenComparing(Comparator<? super Class> other) {
      return new HierarchicalTypeComparator(base.thenComparing(other));
    }
  }

  /**
   * Compares the {@linkplain Object#toString() string representation of objects} in alphabetic
   * order.
   */
  private static class AlphabeticOrderComparator implements Comparator<Object> {
    static final AlphabeticOrderComparator INSTANCE = new AlphabeticOrderComparator();

    @Override
    public int compare(Object c1, Object c2) {
      return c1.toString().toLowerCase(Locale.ROOT)
          .compareTo(c2.toString().toLowerCase(Locale.ROOT));
    }
  }

  /**
   * Gets a comparator to compare the {@linkplain Object#toString() string representation of
   * objects} in alphabetic order.
   */
  @SuppressWarnings("unchecked")
  public static <T> Comparator<T> alphabetic() {
    return (Comparator<T>) AlphabeticOrderComparator.INSTANCE;
  }

  /**
   * Gets a comparator to compare types hierarchically.
   */
  public static HierarchicalTypeComparator hierarchicalType() {
    return HierarchicalTypeComparator.INSTANCE;
  }
}
