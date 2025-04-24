/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (AggregationsTest.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
public class AggregationsTest extends BaseTest {
  @Test
  public void _addAll_array() {
    var obj = new ArrayList<>();
    Aggregations.addAll(obj, new Object[] { "A", "B" });
    assertThat(obj.size(), is(2));
    assertThat(obj.get(0), is("A"));
    assertThat(obj.get(1), is("B"));

    Aggregations.addAll(obj, 1, new Object[] { "C", "D", "E" });
    assertThat(obj.size(), is(5));
    assertThat(obj.get(0), is("A"));
    assertThat(obj.get(1), is("C"));
    assertThat(obj.get(2), is("D"));
    assertThat(obj.get(3), is("E"));
    assertThat(obj.get(4), is("B"));
  }

  @Test
  public void _place() {
    var obj = new ArrayList<>();
    obj.add("A");
    obj.add("B");
    obj.add("C");
    assertThat(obj.size(), is(3));

    /*
     * List upper-bound expansion.
     */
    Aggregations.place(obj, 4, "D");
    assertThat(obj.size(), is(5));
    assertThat(obj.get(3), is(nullValue()));
    assertThat(obj.get(4), is("D"));

    /*
     * List lower-bound expansion.
     */
    Aggregations.place(obj, -2, "E");
    assertThat(obj.size(), is(7));
    assertThat(obj.get(0), is("E"));
    assertThat(obj.get(1), is(nullValue()));
    assertThat(obj.get(2), is("A"));
    assertThat(obj.get(4), is("C"));
    assertThat(obj.get(5), is(nullValue()));
    assertThat(obj.get(6), is("D"));
  }
}
