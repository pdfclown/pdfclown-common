/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (RelatedSetTest.java) is part of pdfclown-common-util module in pdfClown Common project
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

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class DynamicSetTest extends BaseTest {
  @SuppressWarnings("rawtypes")
  static class ClassSet extends DynamicSet<Class> {
    ClassSet() {
      super(new DynamicMapTest.ClassMap());
    }
  }

  @Test
  void _main() {
    var classSet = new ClassSet();

    classSet.add(Map.class);
    classSet.add(Collection.class);

    assertThat("Subclass `TreeMap` SHOULD be resolved", classSet.contains(TreeMap.class),
        is(true));
    assertThat("Subclass `ArrayList` SHOULD be resolved", classSet.contains(ArrayList.class),
        is(true));
    assertThat("Subclass `String` SHOULD NOT be resolved", classSet.contains(String.class),
        is(false));

    // getParent
    assertThat("Root element SHOULD return null parent", classSet.getParent(Map.class),
        is(nullValue()));
    assertThat("Root element SHOULD return null parent", classSet.getParent(Collection.class),
        is(nullValue()));
    assertThat("Derived element SHOULD return parent", classSet.getParent(TreeMap.class),
        is(AbstractMap.class));
    assertThat("Derived element SHOULD return parent", classSet.getParent(ArrayList.class),
        is(AbstractList.class));
    assertThat("Derivable-yet-not-mapped element SHOULD return null parent",
        classSet.getParent(HashMap.class),
        is(nullValue()));

    // getRoot
    assertThat("Root element SHOULD return itself", classSet.getRoot(Map.class),
        is(Map.class));
    assertThat("Root element SHOULD return itself", classSet.getRoot(Collection.class),
        is(Collection.class));
    assertThat("Derived element SHOULD return its root", classSet.getRoot(TreeMap.class),
        is(Map.class));
    assertThat("Derived element SHOULD return its root", classSet.getRoot(ArrayList.class),
        is(Collection.class));
    assertThat("Derivable-yet-not-mapped element SHOULD return null root",
        classSet.getRoot(HashMap.class),
        is(nullValue()));

    // rootSet
    assertThat(classSet.rootSet(), containsInAnyOrder(Map.class, Collection.class));

    // clone
    var classSetClone = classSet.clone();

    /*
     * NOTE: Initially, the clone is supposed to have the same yet distinct mapping as the original
     * map.
     */
    assertThat("Subclass `TreeMap` SHOULD be resolved on clone",
        classSetClone.contains(TreeMap.class),
        is(true));
    assertThat("Subclass `ArrayList` SHOULD be resolved on clone",
        classSetClone.contains(ArrayList.class),
        is(true));
    assertThat("Subclass `String` SHOULD NOT be resolved on clone",
        classSetClone.contains(String.class),
        is(false));

    assertThat(classSetClone.rootSet(), containsInAnyOrder(Map.class, Collection.class));

    /*
     * NOTE: Applying a new mapping to the original set should not affect its clone.
     */
    classSet.add(Object.class);

    assertThat("Subclass `String` SHOULD be resolved", classSet.contains(String.class),
        is(true));
    assertThat("Subclass `String` SHOULD NOT be resolved on clone",
        classSetClone.contains(String.class),
        is(false));

    assertThat("Subclass `String` SHOULD have parent", classSet.getParent(String.class),
        is(Object.class));
    assertThat("Subclass `String` SHOULD NOT have parent on clone",
        classSetClone.getParent(String.class),
        is(nullValue()));

    assertThat("Subclass `String` SHOULD have root", classSet.getRoot(String.class),
        is(Object.class));
    assertThat("Subclass `String` SHOULD NOT have root on clone",
        classSetClone.getRoot(String.class),
        is(nullValue()));

    assertThat(classSet.rootSet(), containsInAnyOrder(Object.class, Map.class, Collection.class));
    assertThat(classSetClone.rootSet(), containsInAnyOrder(Map.class, Collection.class));

    /*
     * NOTE: Applying a new mapping to the clone should not affect the original set.
     */
    classSetClone.add(String.class);

    assertThat("Subclass `String` SHOULD be resolved", classSet.contains(String.class),
        is(true));
    assertThat("Subclass `String` SHOULD be resolved on clone",
        classSetClone.contains(String.class),
        is(true));

    assertThat("Subclass `String` SHOULD have parent", classSet.getParent(String.class),
        is(Object.class));
    assertThat("Subclass `String` (root) SHOULD NOT have parent on clone",
        classSetClone.getParent(String.class),
        is(nullValue()));

    assertThat("Subclass `String` SHOULD have root", classSet.getRoot(String.class),
        is(Object.class));
    assertThat("Subclass `String` (root) SHOULD have itself as root on clone",
        classSetClone.getRoot(String.class),
        is(String.class));

    assertThat(classSet.rootSet(), containsInAnyOrder(Object.class, Map.class, Collection.class));
    assertThat(classSetClone.rootSet(),
        containsInAnyOrder(String.class, Map.class, Collection.class));
  }
}