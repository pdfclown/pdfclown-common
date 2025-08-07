/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (IndentPrintWriterTest.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.util.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.pdfclown.common.util.Strings.EOL;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

// SourceName: nl.talsmasoftware.umldoclet.rendering.indent.IndentingPrintWriterTest
/**
 * Tests for the intenting print writer. By unit-testing this class, we effectively also test the
 * IndentingWriter implementation that provides the actual indenting functionality.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation to pdfclown-common-util)
 */
@SuppressWarnings("ConcatenationWithEmptyString")
public class IndentPrintWriterTest extends BaseTest {
  // SourceName: SettableIndentingPrintWriter
  /**
   * {@link IndentPrintWriter} that allows tests to set its {@code out} field.
   */
  private static class SettableIndentPrintWriter extends IndentPrintWriter {
    private SettableIndentPrintWriter(Appendable writer, Indent indentation) {
      super(writer, indentation);
    }

    private void setOut(Writer out) {
      super.out = out;
    }
  }

  static void clear(StringWriter target) {
    target.getBuffer().delete(0, target.getBuffer().length());
  }

  // SourceName: testIndentModifiedUnderlyingWriter
  @Test
  void indent__modifiedUnderlyingWriter() {
    final var output = new StringWriter();
    var writer = new SettableIndentPrintWriter(output, Indent.DEFAULT);
    writer.setOut(output);

    assertThrows(ClassCastException.class, writer::indent);
  }

  // SourceName: testIndentingPrintWriterNullWriter
  @Test
  void indent__printWriterNullWriter() {
    NullPointerException exception = assertThrows(NullPointerException.class, () -> {
      new IndentPrintWriter(null, Indent.DEFAULT);
    });
    assertThat("Exception message", exception.getMessage(), notNullValue());
  }

  // SourceName: testIndentingWithNewlinesWithinString
  @Test
  void indent__withNewlinesWithinString() {
    var target = new StringWriter();
    IndentPrintWriter.of(target, null)
        .indent()
        .append("text").newline()
        .append("plus a test" + EOL + "with contained newline")
        .flush();

    assertThat(target, hasToString(equalTo(""
        + "    text" + EOL
        + "    plus a test" + EOL
        + "    with contained newline")));
  }

  // SourceName: testWhitespaceIoeByUnderlyingWriter
  @Test
  void whitespace__ioExceptionByUnderlyingWriter() {
    var writer = new SettableIndentPrintWriter(new StringWriter(), Indent.DEFAULT);
    writer.setOut(ThrowWriter.throwing(new IOException("Buffer is full!")));

    assertThrows(RuntimeException.class, writer::whitespace);
  }

  // SourceName: testWhitespaceRendering
  @Test
  void whitespace__rendering() {
    var target = new StringWriter();
    IndentPrintWriter.of(target, null)
        .whitespace().whitespace()
        .indent().whitespace().append("Text ending in whitespace ").whitespace().append("!")
        .newline()
        .whitespace().whitespace().append("Whitespace on beginning of line.")
        .flush();

    assertThat(target, hasToString(equalTo(""
        + "    Text ending in whitespace !" + EOL
        + "    Whitespace on beginning of line.")));
  }

  // SourceName: testWhitespaceRenderingAfterNewlines
  @Test
  void whitespace__renderingAfterNewlines() {
    var output = new StringWriter();
    final IndentPrintWriter writer = IndentPrintWriter.of(output, Indent.DEFAULT);

    writer.append('\n').whitespace().append('-').flush();
    assertThat(output, hasToString(equalTo("\n-")));

    clear(output);

    writer.append('\r').whitespace().append('-').flush();
    assertThat(output, hasToString(equalTo("\r-")));
  }

  // SourceName: testWhitespaceRenderingBeforeNewlines
  @Test
  void whitespace__renderingBeforeNewlines() {
    var output = new StringWriter();
    final IndentPrintWriter writer = IndentPrintWriter.of(output, Indent.DEFAULT);

    writer.append('-').whitespace().append('\n').flush();
    assertThat(output, hasToString(equalTo("-\n")));

    clear(output);

    writer.append('-').whitespace().append('\r').flush();
    assertThat(output, hasToString(equalTo("-\r")));
  }
}
