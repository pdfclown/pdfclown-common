/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (IndentTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.util.io;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

// SourceName: nl.talsmasoftware.umldoclet.rendering.indent.IndentationTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation to pdfclown-common-util)
 */
public class IndentTest extends BaseTest {
  @SuppressWarnings("unchecked")
  private static <S extends Serializable> S deserialize(byte[] bytes) {
    try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      return (S) in.readObject();
    } catch (IOException | ClassNotFoundException ex) {
      throw runtime(ex);
    }
  }

  private static byte[] serialize(Serializable object) {
    try {
      var bos = new ByteArrayOutputStream();
      try (var oos = new ObjectOutputStream(bos)) {
        oos.writeObject(object);
      }
      return bos.toByteArray();
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }

  // SourceName: testDefault
  @Test
  void default_() {
    assertThat("Initially at level 0", Indent.DEFAULT, hasToString(equalTo(EMPTY)));
    assertThat("4 spaces by default", Indent.DEFAULT.increase(), hasToString(equalTo("    ")));
    assertThat(Indent.DEFAULT.increase().decrease(), is(sameInstance(Indent.DEFAULT)));
    assertThat(Indent.DEFAULT.decrease(), is(sameInstance(Indent.DEFAULT)));
  }

  // SourceName: testDeserialization
  @Test
  void deserialization() {
    Indent deserialized;

    deserialized = deserialize(serialize(Indent.DEFAULT));
    assertThat(deserialized, is(sameInstance(Indent.DEFAULT)));

    deserialized = deserialize(serialize(Indent.spaces(4, 3)));
    assertThat(deserialized, is(sameInstance(Indent.spaces(4, 3))));

    deserialized = deserialize(serialize(Indent.tabs(4)));
    assertThat(deserialized, is(sameInstance(Indent.tabs(4))));

    deserialized = deserialize(serialize(Indent.spaces(1, 0)));
    assertThat("Not a constant, other instance", deserialized,
        is(equalTo(Indent.spaces(1, 0))));
  }

  // SourceName: testHashcode
  @Test
  void hashCode_() {
    assertThat(Indent.DEFAULT.hashCode(), is(Indent.DEFAULT.hashCode()));
    assertThat(Indent.spaces(1, 15).hashCode(), is(Indent.spaces(1, 15).hashCode()));
    assertThat(Indent.tabs(28).hashCode(), is(Indent.tabs(28).hashCode()));
  }

  // SourceName: testLenght
  @Test
  void lenght() {
    assertThat(Indent.DEFAULT.length(), is(0));
    assertThat(Indent.DEFAULT.increase().length(), is(4));
    assertThat(Indent.DEFAULT.increase().increase().length(), is(8));
    assertThat(Indent.tabs(5).length(), is(5));
  }

  // SourceName: testNone
  @Test
  void none() {
    assertThat(Indent.NONE, hasToString(equalTo(EMPTY)));
    assertThat(Indent.NONE.increase(), is(sameInstance(Indent.NONE)));
    assertThat(Indent.NONE.decrease(), is(sameInstance(Indent.NONE)));
  }

  @Test
  void spaces() {
    combinationVerifier.verify(
        (width, level) -> Indent.spaces(width, level).toString(),
        List.of("width", "level"),
        // width
        asList(
            0,
            1,
            2,
            3,
            4),
        // level
        asList(
            -1,
            0,
            1,
            2,
            6));
  }

  // SourceName: testDefaultSpaces
  @Test
  void spaces__default() {
    Indent defaultSpaces = Indent.spaces(-1, 0);

    assertThat(defaultSpaces, hasToString(EMPTY));
    assertThat("4 spaces by default", defaultSpaces.increase(), hasToString("    "));
    assertThat(defaultSpaces.increase().decrease(), is(sameInstance(defaultSpaces)));
    assertThat("Negative level becomes 0", Indent.spaces(-1, -1),
        is(sameInstance(defaultSpaces)));
  }

  // SourceName: testSubsequence
  @Test
  void subSequence() {
    assertThat(Indent.DEFAULT.increase().increase().subSequence(3, 6), hasToString("   "));
  }

  // SourceName: testTabs
  @Test
  void tabs() {
    assertThat(Indent.tabs(-1), is(sameInstance(Indent.tabs(0))));
    assertThat(Indent.tabs(0), hasToString(""));
    assertThat(Indent.tabs(1), hasToString("\t"));
    assertThat(Indent.tabs(2), hasToString("\t\t"));
    assertThat(Indent.tabs(6), hasToString("\t\t\t\t\t\t"));
  }
}
