/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (IndentPrintWriter.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0

  Source: https://github.com/talsma-ict/umldoclet/blob/6a7b2e09126b38e1c49103ba85754c7fdfb01db4/src/main/java/nl/talsmasoftware/umldoclet/rendering/indent/IndentingPrintWriter.java

  Changes: Contrary to the original implementation, indentation can also be changed in existing
  instances; consequently, immutable methods (`indent`, `unindent`) have been renamed (`withIndent`,
  `withUndent`), whilst their names have switched to mutable methods (`indent`, `undent`).
 */
package org.pdfclown.common.util.io;

import java.io.PrintWriter;
import org.jspecify.annotations.Nullable;

// SourceName: nl.talsmasoftware.umldoclet.rendering.indent.IndentingPrintWriter
/**
 * Indentation-capable text writer.
 * <p>
 * Indentation is triggered by any newline character, not just calls to {@link #println()} methods.
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation to pdfclown-common-util)
 */
public class IndentPrintWriter extends PrintWriter {
  // SourceName: wrap
  /**
   * Augments the appendable with indenting capabilities.
   *
   * @param indent
   *          Initial indentation ({@code null}, for {@linkplain Indent#DEFAULT default
   *          indentation}).
   */
  public static IndentPrintWriter of(Appendable base /* SourceName: delegate */,
      @Nullable Indent indent /* SourceName: indentation */) {
    return base instanceof IndentPrintWriter writer
        ? writer.withIndent(indent)
        : new IndentPrintWriter(base, indent);
  }

  protected IndentPrintWriter(Appendable base /* SourceName: writer */,
      @Nullable Indent indent /* SourceName: indentation */) {
    super(IndentWriter.of(base, indent));
  }

  @Override
  public IndentPrintWriter append(CharSequence csq) {
    return (IndentPrintWriter) super.append(csq);
  }

  @Override
  public IndentPrintWriter append(CharSequence csq, int start, int end) {
    return (IndentPrintWriter) super.append(csq, start, end);
  }

  @Override
  public IndentPrintWriter append(char c) {
    return (IndentPrintWriter) super.append(c);
  }

  // SourceName: getIndentation
  /**
   * Indentation.
   */
  public Indent getIndent() {
    return getBase().getIndent();
  }

  /**
   * Indentation level.
   */
  public int getLevel() {
    return getIndent().getLevel();
  }

  /**
   * Increases the {@linkplain #getIndent() indentation}.
   *
   * @return Self.
   */
  public IndentPrintWriter indent() {
    getBase().indent();
    return this;
  }

  /**
   * Appends a newline.
   *
   * @return Self.
   * @see #println()
   */
  public IndentPrintWriter newline() {
    super.println();
    return this;
  }

  /**
   * Sets {@linkplain #getIndent() indentation}.
   *
   * @return Self.
   */
  public IndentPrintWriter setIndent(Indent value) {
    getBase().setIndent(value);
    return this;
  }

  /**
   * Sets {@link #getLevel() level}.
   *
   * @return Self.
   */
  public IndentPrintWriter setLevel(int value) {
    return setIndent(getIndent().withLevel(value));
  }

  @Override
  public String toString() {
    return out.toString();
  }

  /**
   * Decreases the {@linkplain #getIndent() indentation}.
   *
   * @return Self.
   */
  public IndentPrintWriter undent() {
    getBase().undent();
    return this;
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
  public IndentPrintWriter whitespace() {
    getBase().whitespace();
    return this;
  }

  /*
   * SourceName: indent
   *
   * NOTE: The original name was semantically ambiguous, as the method returns `IndentPrintWriter`
   * just like, say `append(..)`, despite it creates a new instance without mutating anything of the
   * original one, contrary to the latter which returns the same instance after mutating its backing
   * data buffer.
   */
  /**
   * Gets a new instance with increased indentation.
   */
  public IndentPrintWriter withIndent() {
    return withIndent(getIndent().increase());
  }

  /*
   * SourceName: unindent
   *
   * NOTE: (see `withIndent()`).
   */
  /**
   * Gets an instance with decreased indentation.
   */
  public IndentPrintWriter withUndent() {
    return withIndent(getIndent().decrease());
  }

  // SourceName: getDelegate
  /**
   * Backing writer.
   */
  protected IndentWriter getBase() {
    return (IndentWriter) out;
  }

  // SourceName: withIndentation
  private IndentPrintWriter withIndent(@Nullable Indent value) {
    return value != null && !value.equals(getIndent())
        ? new IndentPrintWriter(out, value)
        : this;
  }
}
