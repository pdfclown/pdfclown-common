/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (RelatedMapTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.pdfclown.common.util.Objects.superTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.Objects.HierarchicalTypeComparator;
import org.pdfclown.common.util.Objects.HierarchicalTypeComparator.Priorities.TypePriorityComparator;
import org.pdfclown.common.util.__test.BaseTest;
import org.pdfclown.common.util.annot.InitNonNull;

/**
 * @author Stefano Chizzolini
 */
class RelatedMapTest extends BaseTest {
  /**
   * Based on {@link org.pdfclown.common.build.test.model.ModelMapper}.{@code ValueMapperMap}.
   */
  @SuppressWarnings("rawtypes")
  static class ClassMap extends RelatedMap<Class, Object> {
    private static class RelatedKeysProvider extends RelatedMap.RelatedProvider<Class> {
      /**
       * Explicit type priorities.
       */
      private TypePriorityComparator priorities =
          HierarchicalTypeComparator.Priorities.explicitPriority();

      @SuppressWarnings("NotNullFieldNotInitialized")
      private @InitNonNull Function<Class, Iterable<Class>> base;

      @Override
      public Iterable<Class> apply(Class type) {
        return base.apply(type);
      }

      @Override
      public RelatedKeysProvider clone() {
        var ret = (RelatedKeysProvider) super.clone();
        ret.priorities = ret.priorities.clone();
        return ret;
      }

      void init(Set<Class> keys) {
        base = $ -> superTypes($, HierarchicalTypeComparator.get()
            .thenComparing(priorities)
            .thenComparing(HierarchicalTypeComparator.Priorities.interfacePriority())
            .thenComparing(($1, $2) -> {
              int ret;
              var name1 = $1.getName();
              var name2 = $2.getName();

              // Prioritize library-specific types!
              if ((ret = libraryPriority(name1) - libraryPriority(name2)) != 0)
                return ret;

              // Compare arbitrarily (no more relevant aspects to evaluate)!
              return name1.compareTo(name2);
            }), keys, false);
      }
    }

    private static final long serialVersionUID = 1L;

    private static int libraryPriority(String name) {
      return name.startsWith("org.pdfclown.") ? -1 : 0;
    }

    /**
     * Explicitly mapped types.
     * <p>
     * Represents all the mappings not derived from related ones.
     * </p>
     */
    private final Map<Object, Class> rootTypes = new HashMap<>();

    ClassMap() {
      super(new RelatedKeysProvider());

      ((RelatedKeysProvider) getRelatedKeysProvider()).init(keySet());
    }

    /**
     * Type priorities.
     */
    public TypePriorityComparator getPriorities() {
      return ((RelatedKeysProvider) getRelatedKeysProvider()).priorities;
    }

    @Override
    public @Nullable Object put(Class key, Object value) {
      if (!rootTypes.containsKey(value)) {
        rootTypes.put(value, key);
      }
      return super.put(key, value);
    }

    public @Nullable Object put(Class key, Object value,
        int priority) {
      getPriorities().set(priority, key);
      return put(key, value);
    }

    @Override
    protected void putRelated(Class relatedKey, Class key, Object value) {
      put(key, value, getPriorities().get(relatedKey));
    }
  }

  @Test
  @SuppressWarnings("DataFlowIssue")
  void _main() {
    var classMap = new ClassMap();

    String mapClassValue = Map.class.getName();
    classMap.put(Map.class, mapClassValue);
    String collectionClassValue = Collection.class.getName();
    classMap.put(Collection.class, collectionClassValue);

    assertThat("Subclass `TreeMap` SHOULD be resolved", classMap.get(TreeMap.class),
        is(mapClassValue));
    assertThat("Subclass `ArrayList` SHOULD be resolved", classMap.get(ArrayList.class),
        is(collectionClassValue));
    assertThat("Subclass `String` SHOULD NOT be resolvable", classMap.get(String.class),
        is(nullValue()));

    var classMap2 = classMap.clone();

    /*
     * NOTE: Initially, the clone is supposed to have the same yet distinct mapping as the original
     * map.
     */
    assertThat("Subclass `TreeMap` SHOULD be resolved on clone", classMap2.get(TreeMap.class),
        is(mapClassValue));
    assertThat("Subclass `ArrayList` SHOULD be resolved on clone", classMap2.get(ArrayList.class),
        is(collectionClassValue));
    assertThat("Subclass `String` SHOULD NOT be resolvable on clone", classMap2.get(String.class),
        is(nullValue()));

    /*
     * NOTE: Applying a new mapping to the original map should not affect its clone.
     */
    String objectClassValue = Object.class.getName();
    classMap.put(Object.class, objectClassValue);

    assertThat("Subclass `String` SHOULD be resolved", classMap.get(String.class),
        is(objectClassValue));
    assertThat("Subclass `String` SHOULD NOT be resolvable on clone", classMap2.get(String.class),
        is(nullValue()));

    /*
     * NOTE: Applying a new mapping to the clone should not affect the original map.
     */
    String stringClassValue = String.class.getName();
    classMap2.put(String.class, stringClassValue);

    assertThat("Subclass `String` SHOULD be resolved", classMap.get(String.class),
        is(objectClassValue));
    assertThat("Subclass `String` SHOULD be resolved on clone", classMap2.get(String.class),
        is(stringClassValue));
  }
}