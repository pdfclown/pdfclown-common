/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (IndentTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.util.io;

import static java.util.Arrays.asList;
import static java.util.List.of;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.util.Aggregations.entry;
import static org.pdfclown.common.util.Exceptions.runtime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.util.__test.BaseTest;

// SourceName: nl.talsmasoftware.umldoclet.rendering.indent.IndentationTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation to pdfclown-common-util)
 */
public class IndentTest extends BaseTest {
  static Stream<Arguments> spaces() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // width[0]: 0
            // [1] level[0]: -1
            "",
            // [2] level[1]: 0
            "",
            // [3] level[2]: 1
            "",
            // [4] level[3]: 2
            "",
            // [5] level[4]: 6
            "",
            //
            // width[1]: 1
            // [6] level[0]: -1
            "",
            // [7] level[1]: 0
            "",
            // [8] level[2]: 1
            " ",
            // [9] level[3]: 2
            "  ",
            // [10] level[4]: 6
            "      ",
            //
            // width[2]: 2
            // [11] level[0]: -1
            "",
            // [12] level[1]: 0
            "",
            // [13] level[2]: 1
            "  ",
            // [14] level[3]: 2
            "    ",
            // [15] level[4]: 6
            "            ",
            //
            // width[3]: 3
            // [16] level[0]: -1
            "",
            // [17] level[1]: 0
            "",
            // [18] level[2]: 1
            "   ",
            // [19] level[3]: 2
            "      ",
            // [20] level[4]: 6
            "                  ",
            //
            // width[4]: 4
            // [21] level[0]: -1
            "",
            // [22] level[1]: 0
            "",
            // [23] level[2]: 1
            "    ",
            // [24] level[3]: 2
            "        ",
            // [25] level[4]: 6
            "                        "),
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

  // SourceName: testSpacesWidth*
  @ParameterizedTest
  @MethodSource
  void spaces(Expected<String> expected, int width, int level) {
    assertParameterizedOf(
        () -> Indent.spaces(width, level).toString(),
        expected,
        () -> new ExpectedGeneration(of(
            entry("width", width),
            entry("level", level))));
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
