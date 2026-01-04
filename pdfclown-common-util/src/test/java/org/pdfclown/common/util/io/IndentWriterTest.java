/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (IndentWriterTest.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.util.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.pdfclown.common.util.io.ThrowWriter.throwing;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

// SourceName: nl.talsmasoftware.umldoclet.rendering.indent.IndentingWriterTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation to pdfclown-common-util)
 */
public class IndentWriterTest extends BaseTest {
  // SourceName: testCloseRethrowingIOExceptions
  @Test
  void close__rethrowingIOException() {
    final var exception = new IOException("I/O exception!");
    try {
      new IndentWriter(throwing(exception), Indent.DEFAULT).close();

      fail("I/O exception expected.");
    } catch (IOException ex) {
      assertThat(ex, is(sameInstance(exception)));
    }
  }

  // SourceName: testCloseRethrowingRuntimeExceptions
  @Test
  void close__rethrowingRuntimeException() {
    final var exception = new RuntimeException("Runtime exception!");
    assertThat(assertThrows(RuntimeException.class, () -> {
      new IndentWriter(throwing(exception), Indent.DEFAULT).close();
    }),
        is(sameInstance(exception)));
  }

  // SourceName: testCloseWrappingCheckedExceptions
  @Test
  void close__wrappingCheckedException() throws IOException {
    final var checkedException = new Exception("Checked exception!");
    try {
      new IndentWriter(throwing(checkedException), Indent.DEFAULT).close();

      fail("Runtime exception expected.");
    } catch (RuntimeException ex) {
      assertThat(ex.getCause(), is(sameInstance(checkedException)));
    }
  }

  @Test
  void indent() {
    var writer = new IndentWriter(new StringWriter(), Indent.DEFAULT);
    var indentWriter = writer.indent();

    assertThat(indentWriter, sameInstance(writer));
    assertThat(indentWriter.getIndent(), is(equalTo(Indent.DEFAULT.increase())));
  }

  // SourceName: testToStringDelegation
  @Test
  void toString__delegation() throws IOException {
    var indentWriter = new IndentWriter(new StringWriter(), Indent.DEFAULT);
    indentWriter.write("first line\n");
    indentWriter.indent();
    indentWriter.write("second line");

    assertThat(indentWriter, hasToString("first line\n    second line"));
  }

  // SourceName: testNoFlushingTerminatingWhitespace
  @Test
  void whitespace__noFlushingTerminating() throws IOException {
    var output = new StringWriter();
    var indentingWriter = new IndentWriter(output, Indent.DEFAULT);
    {
      indentingWriter.write("Some content");
      indentingWriter.whitespace();
      indentingWriter.flush();

      assertThat(output, hasToString("Some content"));
    }
    {
      indentingWriter.write("more content");
      indentingWriter.whitespace();
      indentingWriter.flush();

      assertThat(output, hasToString("Some content more content"));
    }
  }

  // SourceName: testIndent
  @Test
  void withIndent() {
    var writer = new IndentWriter(new StringWriter(), Indent.DEFAULT);
    var indentWriter = writer.withIndent();

    assertThat(indentWriter, not(sameInstance(writer)));
    assertThat(indentWriter.getIndent(), is(equalTo(Indent.DEFAULT.increase())));
  }

  // SourceName: testUnindentFromZero
  @Test
  void withUndent__fromZero() {
    var indentingWriter = new IndentWriter(new StringWriter(), Indent.DEFAULT);

    assertThat(indentingWriter.withUndent(), is(sameInstance(indentingWriter)));
  }

  // SourceName: testWritingFromNonZeroOffset
  @Test
  void write__nonZeroOffset() throws IOException {
    var output = new StringWriter();
    new IndentWriter(output, Indent.DEFAULT).write("1234".toCharArray(), 1, 2);

    assertThat(output, hasToString("23"));
  }
}
