/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (IndentWriter.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0

  Source: https://github.com/talsma-ict/umldoclet/blob/6a7b2e09126b38e1c49103ba85754c7fdfb01db4/src/main/java/nl/talsmasoftware/umldoclet/rendering/indent/IndentingWriter.java

  Changes: Contrary to the original implementation, indentation can also be changed in existing
  instances; consequently, immutable methods (`indent`, `unindent`) have been renamed (`withIndent`,
  `withUndent`), whilst their names have switched to mutable methods (`indent`, `undent`).
 */
package org.pdfclown.common.util.io;

import static java.lang.Character.isWhitespace;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Strings.SPACE;
import static org.pdfclown.common.util.Strings.isEOL;

import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jspecify.annotations.Nullable;

// SourceName: nl.talsmasoftware.umldoclet.rendering.indent.IndentingWriter
/**
 * Indentation-capable writer.
 * <p>
 * Indentation is triggered by newline characters.
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation to pdfclown-common-util)
 */
public class IndentWriter extends Writer {
  // SourceName: wrap
  /**
   * Augments the given appendable with indenting capabilities.
   *
   * @param indent
   *          Initial indentation ({@code null}, for {@linkplain Indent#DEFAULT default
   *          indentation}).
   */
  public static IndentWriter of(Appendable base /* SourceName: delegate */,
      @Nullable Indent indent /* SourceName: indentation */) {
    return base instanceof IndentWriter
        ? ((IndentWriter) base).withIndent(indent)
        : new IndentWriter(base, indent);
  }

  // SourceName: delegate
  private final Appendable base;
  private Indent indent;
  private char lastWritten;
  // SourceName: addWhitespace
  private final AtomicBoolean whitespaceWritable = new AtomicBoolean(false);

  protected IndentWriter(Appendable base /* SourceName: delegate */, @Nullable Indent indent) {
    this(base, indent, '\n', false);
  }

  private IndentWriter(Appendable base /* SourceName: delegate */,
      @Nullable Indent indent /* SourceName: indentation */, char lastWritten,
      boolean whitespaceWritable /* SourceName: addWhitespace */) {
    super(requireNonNull(base, "`base`"));

    this.base = base;
    this.indent = requireNonNullElse(indent, Indent.DEFAULT);
    this.lastWritten = lastWritten;
    this.whitespaceWritable.set(whitespaceWritable);
  }

  @Override
  public void close() throws IOException {
    if (base instanceof AutoCloseable) {
      try {
        ((AutoCloseable) base).close();
      } catch (IOException ex) {
        throw ex;
      } catch (Exception ex) {
        throw runtime(ex);
      }
    }
  }

  @Override
  public void flush() throws IOException {
    if (base instanceof Flushable) {
      ((Flushable) base).flush();
    }
  }

  // SourceName: getIndentation
  /**
   * Indentation.
   */
  public Indent getIndent() {
    return indent;
  }

  /**
   * Increases the {@linkplain #getIndent() indentation}.
   *
   * @return Self.
   */
  public IndentWriter indent() {
    return setIndent(indent.increase());
  }

  /**
   * Sets {@linkplain #getIndent() indentation}.
   *
   * @return Self.
   */
  public IndentWriter setIndent(Indent value) {
    indent = value;
    return this;
  }

  @Override
  public String toString() {
    return base.toString();
  }

  /**
   * Decreases the {@linkplain #getIndent() indentation}.
   *
   * @return Self.
   */
  public IndentWriter undent() {
    return setIndent(indent.decrease());
  }

  /**
   * Ensures at least one whitespace character between the last character and the next.
   * <p>
   * This method avoids to append a whitespace character if the last character was a whitespace
   * character itself; the whitespace character will also not be written until other characters need
   * to be written, thus preventing trailing whitespace.
   * </p>
   *
   * @return Self.
   */
  public IndentWriter whitespace() {
    whitespaceWritable.set(true);
    return this;
  }

  // SourceName: indent
  /**
   * Gets a new instance with increased indentation.
   */
  public IndentWriter withIndent() {
    return withIndent(indent.increase());
  }

  // SourceName: withIndentation
  /**
   * Gets an instance with the given indentation.
   */
  public IndentWriter withIndent(@Nullable Indent value) {
    return value != null && !indent.equals(value)
        ? new IndentWriter(base, value, lastWritten, whitespaceWritable.get())
        : this;
  }

  // SourceName: unindent
  /**
   * Gets an instance with decreased indentation.
   */
  public IndentWriter withUndent() {
    return withIndent(indent.decrease());
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    if (len > 0) {
      char c = cbuf[off];
      synchronized (lock) {
        if (whitespaceWritable.compareAndSet(true, false) && !isWhitespace(lastWritten)
            && !isWhitespace(c)) {
          base.append(SPACE);
          lastWritten = SPACE;
        }
        for (int i = 0; i < len; i++) {
          c = cbuf[off + i];
          if (isEOL(lastWritten) && !isEOL(c)) {
            base.append(indent);
          }
          base.append(c);
          lastWritten = c;
        }
      }
    }
  }
}
