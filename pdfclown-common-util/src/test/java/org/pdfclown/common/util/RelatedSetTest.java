/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (RelatedSetTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class RelatedSetTest extends BaseTest {
  @SuppressWarnings("rawtypes")
  static class ClassSet extends RelatedSet<Class> {

    ClassSet() {
      super(new RelatedMapTest.ClassMap());
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

    var classSet2 = classSet.clone();

    /*
     * NOTE: Initially, the clone is supposed to have the same yet distinct mapping as the original
     * map.
     */
    assertThat("Subclass `TreeMap` SHOULD be resolved on clone", classSet2.contains(TreeMap.class),
        is(true));
    assertThat("Subclass `ArrayList` SHOULD be resolved on clone",
        classSet2.contains(ArrayList.class),
        is(true));
    assertThat("Subclass `String` SHOULD NOT be resolved on clone",
        classSet2.contains(String.class),
        is(false));

    /*
     * NOTE: Applying a new mapping to the original set should not affect its clone.
     */
    classSet.add(Object.class);

    assertThat("Subclass `String` SHOULD be resolved", classSet.contains(String.class),
        is(true));
    assertThat("Subclass `String` SHOULD NOT be resolved on clone",
        classSet2.contains(String.class),
        is(false));

    /*
     * NOTE: Applying a new mapping to the clone should not affect the original set.
     */
    classSet2.add(String.class);

    assertThat("Subclass `String` SHOULD be resolved", classSet.contains(String.class),
        is(true));
    assertThat("Subclass `String` SHOULD be resolved on clone", classSet2.contains(String.class),
        is(true));
  }
}