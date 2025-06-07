/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ObjectsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class ObjectsTest extends BaseTest {
  @Test
  @SuppressWarnings("ConstantValue")
  void objOrGet() {
    List<Object> defaultResult = Collections.emptyList();

    List<Object> obj = List.of("test");
    Supplier<List<Object>> defaultSupplier = () -> defaultResult;
    assertThat(Objects.objElseGet(obj, defaultSupplier), is(obj));

    obj = null;
    assertThat(Objects.objElseGet(obj, defaultSupplier), is(defaultResult));
  }

  @Test
  @SuppressWarnings("ConstantValue")
  void objToOrElse() {
    int defaultResult = 0;

    List<Object> obj = List.of("test");
    assertThat(Objects.objToElse(obj, List::size, defaultResult), is(1));

    obj = null;
    assertThat(Objects.objTo(obj, List::size), is(nullValue()));
    assertThat(Objects.objToElse(obj, List::size, defaultResult), is(defaultResult));
  }

  @Test
  @SuppressWarnings("ConstantValue")
  void objToOrGet() {
    int defaultResult = 0;

    List<Object> obj = List.of("test");
    Supplier<Integer> defaultSupplier = () -> defaultResult;
    assertThat(Objects.objToElseGet(obj, List::size, defaultSupplier), is(1));

    obj = null;
    assertThat(Objects.objToElseGet(obj, List::size, defaultSupplier), is(defaultResult));
  }
}
