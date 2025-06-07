/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (IndentPrintStreamTest.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings("ALL")
class IndentPrintStreamTest extends BaseTest {
  IndentPrintStream obj;

  @BeforeEach
  void _setUp() {
    obj = new IndentPrintStream();
  }

  @AfterEach
  void _tearDown() {
    obj = null;
  }

  @Test
  void append_CharSequence() {
    obj.indent();
    obj.append("a b\nc d").append("e f");

    assertThat(obj.toDataString(), is(""
        + "  a b\n"
        + "  c de f"));
  }

  @Test
  void append_CharSequence_int_int() {
    obj.indent();
    obj.append("a b\nc d", 3, 6).append("e f");

    assertThat(obj.toDataString(), is(""
        + "\n"
        + "  c e f"));
  }

  @Test
  void append_char() {
    obj.indent();
    obj.append('a').append('\n').append('b');

    assertThat(obj.toDataString(), is(""
        + "  a\n"
        + "  b"));
  }

  @Test
  void getIndent() {
    assertThat(obj.getIndent(), is(0));
  }

  @Test
  void getWrapIndent() {
    assertThat(obj.getWrapIndent(), is(2));
  }

  @Test
  void indent() {
    obj.indent();

    assertThat(obj.getIndent(), is(1));

    obj.indent();

    assertThat(obj.getIndent(), is(2));
    assertThat(obj.toDataString(), is(""));
  }

  @Test
  void print_Object() {
    obj.indent();
    obj.print((Object) null);
    obj.print(Map.entry("key", "value"));

    assertThat(obj.toDataString(), is(""
        + "  nullkey=value"));
  }

  @Test
  void print_String__indent1_noNewLine() {
    obj.indent();
    obj.print("a b");
    obj.print(" c d");

    assertThat(obj.toDataString(), is(""
        + "  a b c d"));
  }

  @Test
  void print_String__indent2_newLine() {
    obj.indent().indent();
    obj.print("a b\n");
    obj.print("c d");
    obj.print("e f\ng h\n\n");
    obj.print("i l");

    assertThat(obj.toDataString(), is(""
        + "    a b\n"
        + "    c de f\n"
        + "    g h\n"
        + "\n"
        + "    i l"));
  }

  @Test
  void print_String__indentDefault_noNewLine() {
    obj.print("a b");
    obj.print(" c d");

    assertThat(obj.toDataString(), is("a b c d"));
  }

  @Test
  void print_boolean() {
    obj.indent();
    obj.print(true);

    assertThat(obj.toDataString(), is("  true"));
  }

  @Test
  void print_char() {
    obj.indent();
    obj.print('a');

    assertThat(obj.toDataString(), is("  a"));
  }

  @Test
  void print_charArray() {
    obj.indent();
    obj.print(new char[] { 'a', 'b', 'c' });

    assertThat(obj.toDataString(), is("  abc"));
  }

  @Test
  void print_double() {
    obj.indent();
    obj.print(1.23e2d);

    assertThat(obj.toDataString(), is("  123.0"));
  }

  @Test
  void print_float() {
    obj.indent();
    obj.print(1.23e2f);

    assertThat(obj.toDataString(), is("  123.0"));
  }

  @Test
  void print_int() {
    obj.indent();
    obj.print(123);

    assertThat(obj.toDataString(), is("  123"));
  }

  @Test
  void print_long() {
    obj.indent();
    obj.print(123L);

    assertThat(obj.toDataString(), is("  123"));
  }

  @Test
  void printf() {
    obj.indent();
    obj.printf("a b%sc d %s", "\n", "e f\n\n");

    assertThat(obj.toDataString(), is(""
        + "  a b\n"
        + "  c d e f\n"
        + "\n"));
  }

  @Test
  void println() {
    obj.indent();
    obj.println();

    assertThat(obj.toDataString(), is("\n"));
  }

  @Test
  void setIndent() {
    obj.indent().indent().indent();
    obj.setIndent(1);

    assertThat(obj.getIndent(), is(1));
  }

  @Test
  void setIndent_negative() {
    obj.indent().indent().indent();
    obj.setIndent(-1);

    assertThat(obj.getIndent(), is(0));
  }

  @Test
  void setWrapIndent() {
    obj.setWrapIndent(1);

    assertThat(obj.getWrapIndent(), is(1));
  }

  @Test
  void setWrapIndent_negative() {
    obj.setWrapIndent(-1);

    assertThat(obj.getWrapIndent(), is(0));
  }

  @Test
  void undent() {
    obj.undent();

    assertThat(obj.getIndent(), is(0));

    obj.setIndent(5);
    obj.undent();

    assertThat(obj.getIndent(), is(4));

    obj.undent();

    assertThat(obj.getIndent(), is(3));
  }

  @Test
  void unwrap__withWrap() {
    var expected = ""
        + "  a b\n"
        + "      c d\n"
        + "  e f\n"
        + "  g h";

    obj.indent();

    // When last line char is NOT newline.
    {
      obj.wrap();
      obj.print("a b\n");
      obj.print("c d");
      obj.unwrap();
      obj.print("e f\n");
      obj.print("g h");

      assertThat(obj.toDataString(), is(expected));
    }

    obj.resetData();

    // When last line char is newline.
    {
      obj.wrap();
      obj.print("a b\n");
      obj.print("c d\n");
      obj.unwrap();
      obj.print("e f\n");
      obj.print("g h");

      assertThat(obj.toDataString(), is(expected));
    }
  }

  @Test
  void unwrap__withoutWrap() {
    obj.indent();
    obj.unwrap();
    obj.print("e f\n");
    obj.print("g h");

    assertThat(obj.toDataString(), is(""
        + "  e f\n"
        + "  g h"));
  }

  @Test
  void wrap__amidLine() {
    obj.indent();
    obj.print("a b\n");
    obj.wrap();
    obj.print("c d");
    obj.print("e f\ng h\n\n");
    obj.print("i j");
    obj.unwrap();
    obj.print("k l\n");
    obj.print("m n");
    obj.print("o p");

    assertThat(obj.toDataString(), is(""
        + "  a b\n"
        + "  c de f\n"
        + "      g h\n"
        + "\n"
        + "      i j\n"
        + "  k l\n"
        + "  m no p"));
  }

  @Test
  void wrap__fromLineStart() {
    obj.indent();
    obj.wrap();
    obj.print("a b\n");
    obj.print("c d");
    obj.print("e f\ng h\n\n");
    obj.print("i j");
    obj.unwrap();
    obj.print("k l\n");
    obj.print("m n");
    obj.print("o p");

    assertThat(obj.toDataString(), is(""
        + "  a b\n"
        + "      c de f\n"
        + "      g h\n"
        + "\n"
        + "      i j\n"
        + "  k l\n"
        + "  m no p"));
  }

  @Test
  void write_byteArray() throws IOException {
    obj.indent();
    obj.write(new byte[] { 'a', 'b', 'c' });

    assertThat(obj.toDataString(), is("abc"));
  }

  @Test
  void write_byteArray_int_int() {
    obj.indent();
    obj.write(new byte[] { 'a', 'b', 'c' }, 1, 1);

    assertThat(obj.toDataString(), is("b"));
  }

  @Test
  void write_int() {
    obj.indent();
    obj.write('a');

    assertThat(obj.toDataString(), is("a"));
  }
}