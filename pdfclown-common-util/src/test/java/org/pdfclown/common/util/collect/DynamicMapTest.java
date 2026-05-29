/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (RelatedMapTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.collect;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.pdfclown.common.util.Objects.superTypes;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.Objects.HierarchicalTypeComparator;
import org.pdfclown.common.util.Objects.HierarchicalTypeComparator.Priorities.TypePriorityComparator;
import org.pdfclown.common.util.__test.BaseTest;
import org.pdfclown.common.util.annot.InitNonNull;

/**
 * @author Stefano Chizzolini
 */
class DynamicMapTest extends BaseTest {
  /**
   * Based on {@link org.pdfclown.common.build.test.model.ModelMapper}.{@code ValueMapperMap}.
   */
  @SuppressWarnings("rawtypes")
  static class ClassMap extends DynamicMap<Class, Object> {
    private static class DynamicKeysProvider extends DynamicProvider<Class> {
      /**
       * Explicit type priorities.
       */
      private TypePriorityComparator priorities = HierarchicalTypeComparator.Priorities
          .explicitPriority();

      private @InitNonNull Function<Class, Stream<Class>> base;

      @Override
      public Stream<Class> apply(Class type) {
        return base.apply(type);
      }

      @Override
      public DynamicKeysProvider clone() {
        var ret = (DynamicKeysProvider) super.clone();
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

    private static int libraryPriority(String name) {
      return name.startsWith("org.pdfclown.") ? -1 : 0;
    }

    ClassMap() {
      super(new DynamicKeysProvider());

      ((DynamicKeysProvider) getRelatedKeysProvider()).init(keySet());
    }

    /**
     * Type priorities.
     * <p>
     * Override any other criteria to sort {@linkplain #getRelatedKeysProvider() related keys}.
     * </p>
     */
    public TypePriorityComparator getPriorities() {
      return ((DynamicKeysProvider) getRelatedKeysProvider()).priorities;
    }

    /**
     * @param priority
     *          {@linkplain #getPriorities() Type priority}.
     */
    public @Nullable Object put(Class key, Object value, int priority) {
      getPriorities().set(priority, key);

      return put(key, value);
    }

    @Override
    protected void putDynamic(Class key, Object value, Class parentKey) {
      var priorities = getPriorities();
      priorities.set(priorities.get(parentKey), key);

      super.putDynamic(key, value, parentKey);
    }
  }

  @Test
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

    // getParentKey
    assertThat("Root key SHOULD return null parent", classMap.getParentKey(Map.class),
        is(nullValue()));
    assertThat("Root key SHOULD return null parent", classMap.getParentKey(Collection.class),
        is(nullValue()));
    assertThat("Derived key SHOULD return parent", classMap.getParentKey(TreeMap.class),
        is(AbstractMap.class));
    assertThat("Derived key SHOULD return parent", classMap.getParentKey(ArrayList.class),
        is(AbstractList.class));
    assertThat("Derivable-yet-not-mapped key SHOULD return null parent",
        classMap.getParentKey(HashMap.class),
        is(nullValue()));

    // getRootKey
    assertThat("Root key SHOULD return itself", classMap.getRootKey(Map.class),
        is(Map.class));
    assertThat("Root key SHOULD return itself", classMap.getRootKey(Collection.class),
        is(Collection.class));
    assertThat("Derived key SHOULD return its root", classMap.getRootKey(TreeMap.class),
        is(Map.class));
    assertThat("Derived key SHOULD return its root", classMap.getRootKey(ArrayList.class),
        is(Collection.class));
    assertThat("Derivable-yet-not-mapped key SHOULD return null root",
        classMap.getRootKey(HashMap.class),
        is(nullValue()));

    // rootKeySet
    assertThat(classMap.rootKeySet(), containsInAnyOrder(Map.class, Collection.class));

    // clone
    var classMapClone = classMap.clone();

    /*
     * NOTE: Initially, the clone is supposed to have the same yet distinct mapping as the original
     * map.
     */
    assertThat("Subclass `TreeMap` SHOULD be resolved on clone", classMapClone.get(TreeMap.class),
        is(mapClassValue));
    assertThat("Subclass `ArrayList` SHOULD be resolved on clone",
        classMapClone.get(ArrayList.class),
        is(collectionClassValue));
    assertThat("Subclass `String` SHOULD NOT be resolvable on clone",
        classMapClone.get(String.class),
        is(nullValue()));

    assertThat(classMapClone.rootKeySet(), containsInAnyOrder(Map.class, Collection.class));

    /*
     * NOTE: Applying a new mapping to the original map should not affect its clone.
     */
    String objectClassValue = Object.class.getName();
    classMap.put(Object.class, objectClassValue);

    assertThat("Subclass `String` SHOULD be resolved", classMap.get(String.class),
        is(objectClassValue));
    assertThat("Subclass `String` SHOULD NOT be resolvable on clone",
        classMapClone.get(String.class),
        is(nullValue()));

    assertThat("Subclass `String` SHOULD have parent", classMap.getParentKey(String.class),
        is(Object.class));
    assertThat("Subclass `String` SHOULD NOT have parent on clone",
        classMapClone.getParentKey(String.class),
        is(nullValue()));

    assertThat("Subclass `String` SHOULD have root", classMap.getRootKey(String.class),
        is(Object.class));
    assertThat("Subclass `String` SHOULD NOT have root on clone",
        classMapClone.getRootKey(String.class),
        is(nullValue()));

    assertThat(classMap.rootKeySet(),
        containsInAnyOrder(Object.class, Map.class, Collection.class));
    assertThat(classMapClone.rootKeySet(), containsInAnyOrder(Map.class, Collection.class));

    /*
     * NOTE: Applying a new mapping to the clone should not affect the original map.
     */
    String stringClassValue = String.class.getName();
    classMapClone.put(String.class, stringClassValue);

    assertThat("Subclass `String` SHOULD be resolved", classMap.get(String.class),
        is(objectClassValue));
    assertThat("Subclass `String` SHOULD be resolved on clone", classMapClone.get(String.class),
        is(stringClassValue));

    assertThat("Subclass `String` SHOULD have parent", classMap.getParentKey(String.class),
        is(Object.class));
    assertThat("Subclass `String` (root) SHOULD NOT have parent on clone",
        classMapClone.getParentKey(String.class),
        is(nullValue()));

    assertThat("Subclass `String` SHOULD have root", classMap.getRootKey(String.class),
        is(Object.class));
    assertThat("Subclass `String` (root) SHOULD have itself as root on clone",
        classMapClone.getRootKey(String.class),
        is(String.class));

    assertThat(classMap.rootKeySet(),
        containsInAnyOrder(Object.class, Map.class, Collection.class));
    assertThat(classMapClone.rootKeySet(),
        containsInAnyOrder(String.class, Map.class, Collection.class));
  }
}