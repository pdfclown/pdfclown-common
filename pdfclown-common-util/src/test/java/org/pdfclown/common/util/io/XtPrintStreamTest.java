/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (XtPrintStreamTest.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings({ "DataFlowIssue", "ConstantValue", "UnnecessaryCallToStringValueOf" })
class XtPrintStreamTest extends BaseTest {
  @Spy
  XtPrintStream obj;

  @BeforeEach
  void _setUp() {
    obj = Mockito.spy(new XtPrintStream());
  }

  @AfterEach
  void _tearDown() {
    obj = null;
  }

  @Test
  void println() {
    obj.println();

    verify(obj).println();
    verifyNoMoreInteractions(obj);

    assertThat(obj.toDataString(), is("\n"));
  }

  @Test
  void println_Object() {
    var arg = (Object) null;

    obj.println(arg);

    verify(obj).println(arg);
    verify(obj).println(String.valueOf(arg));
    verify(obj).print(String.valueOf(arg));
    verify(obj).println();
    verifyNoMoreInteractions(obj);

    assertThat(obj.toDataString(), is(arg + "\n"));
  }

  @Test
  void println_String() {
    var arg = "abc";

    obj.println(arg);

    verify(obj).println(arg);
    verify(obj).print(arg);
    verify(obj).println();
    verifyNoMoreInteractions(obj);

    assertThat(obj.toDataString(), is(arg + "\n"));
  }

  @Test
  void println_boolean() {
    var arg = true;

    obj.println(arg);

    verify(obj).println(arg);
    verify(obj).print(arg);
    verify(obj).println();
    verifyNoMoreInteractions(obj);

    assertThat(obj.toDataString(), is(arg + "\n"));
  }

  @Test
  void println_char() {
    var arg = 'a';

    obj.println(arg);

    verify(obj).println(arg);
    verify(obj).print(arg);
    verify(obj).println();
    verifyNoMoreInteractions(obj);

    assertThat(obj.toDataString(), is(arg + "\n"));
  }

  @Test
  void println_charArray() {
    var arg = new char[] { 'a', 'b', 'c' };

    obj.println(arg);

    verify(obj).println(arg);
    verify(obj).print(arg);
    verify(obj).println();
    verifyNoMoreInteractions(obj);

    assertThat(obj.toDataString(), is(String.valueOf(arg) + "\n"));
  }

  @Test
  void println_double() {
    var arg = 1.23d;

    obj.println(arg);

    verify(obj).println(arg);
    verify(obj).print(arg);
    verify(obj).println();
    verifyNoMoreInteractions(obj);

    assertThat(obj.toDataString(), is(arg + "\n"));
  }

  @Test
  void println_float() {
    var arg = 1.23f;

    obj.println(arg);

    verify(obj).println(arg);
    verify(obj).print(arg);
    verify(obj).println();
    verifyNoMoreInteractions(obj);

    assertThat(obj.toDataString(), is(arg + "\n"));
  }

  @Test
  void println_int() {
    var arg = 123;

    obj.println(arg);

    verify(obj).println(arg);
    verify(obj).print(arg);
    verify(obj).println();
    verifyNoMoreInteractions(obj);

    assertThat(obj.toDataString(), is(arg + "\n"));
  }

  @Test
  void println_long() {
    var arg = 123L;

    obj.println(arg);

    verify(obj).println(arg);
    verify(obj).print(arg);
    verify(obj).println();
    verifyNoMoreInteractions(obj);

    assertThat(obj.toDataString(), is(arg + "\n"));
  }

  @Test
  void resetData() {
    assertThat(obj.toDataString(), is(""));

    obj.resetData();

    assertThat(obj.toDataString(), is(""));

    obj.print(true);

    assertThat(obj.toDataString(), is("true"));

    obj.resetData();

    assertThat(obj.toDataString(), is(""));
  }

  @Test
  void toDataString() {
    obj.print(true);

    assertThat(obj.toDataString(), is("true"));
  }
}