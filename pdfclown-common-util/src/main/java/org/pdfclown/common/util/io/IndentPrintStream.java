/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (IndentPrintStream.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.LF;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.Strings.SPACE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.InitNonNull;

/**
 * Indentation-capable {@link java.io.PrintStream PrintStream}.
 * <p>
 * Indentation is based on the repetition of the given {@linkplain #getIndentSymbol() symbol}
 * according to its {@linkplain #getIndent() current level}. Lines can also be wrapped with
 * {@linkplain #getWrapIndent() additional indentation} embracing them between {@link #wrap()} and
 * {@link #unwrap()} calls.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class IndentPrintStream extends XtPrintStream {
  private static final int INDENT__DEFAULT = 0;
  private static final String INDENT_SYMBOL__DEFAULT = S + SPACE + SPACE;
  private static final int WRAP_INDENT__DEFAULT = 2;

  private static final int INDENT__MAX = 32;

  private static final int WRAP_STATUS__INACTIVE = 0;
  private static final int WRAP_STATUS__PENDING = 1;
  private static final int WRAP_STATUS__ACTIVE = 2;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull String indentSymbol;

  /**
   * Indentation string.
   */
  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull String indentChunk;
  /**
   * Indentation cache.
   * <p>
   * Avoids repeated string concatenation, storing requested instances for reuse.
   * </p>
   */
  private final List<String> indentChunks = new ArrayList<>();
  /**
   * Whether a new line has just begun.
   * <p>
   * Indentation shall be applied on next print.
   * </p>
   */
  private boolean lineStarting = true;
  /**
   * Whether the current line belongs to the last paragraph rather than a new one.
   */
  private int wrapStatus = WRAP_STATUS__INACTIVE;
  /**
   * Indentation string for wrapped lines.
   */
  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull String wrapIndentChunk;

  /**
   * New instance backed by {@link ByteArrayOutputStream}, with {@link StandardCharsets#UTF_8 UTF_8}
   * encoding charset and 2-space indentation.
   */
  public IndentPrintStream() {
    this(INDENT_SYMBOL__DEFAULT);
  }

  /**
   * New instance backed by the given stream, with {@link StandardCharsets#UTF_8 UTF_8} encoding
   * charset and 2-space indentation.
   *
   * @param out
   *          Backing stream.
   */
  public IndentPrintStream(OutputStream out) {
    this(out, INDENT_SYMBOL__DEFAULT);
  }

  /**
   * New instance backed by the given stream, with {@link StandardCharsets#UTF_8 UTF_8} encoding
   * charset and the given indentation symbol.
   *
   * @param out
   *          Backing stream.
   * @param indentSymbol
   *          String representation of an indentation step.
   */
  public IndentPrintStream(OutputStream out, String indentSymbol) {
    super(out);

    setIndentSymbol(indentSymbol);
  }

  /**
   * New instance backed by the given stream, with {@link StandardCharsets#UTF_8 UTF_8} encoding
   * charset and 2-space indentation.
   *
   * @param out
   *          Backing stream.
   * @param autoFlush
   *          Whether the output buffer will be flushed whenever a byte array is written, one of the
   *          {@code println} methods is invoked, or a newline character or byte ({@code '\n'}) is
   *          written.
   */
  public IndentPrintStream(OutputStream out, boolean autoFlush) {
    super(out, autoFlush);

    setIndentSymbol(INDENT_SYMBOL__DEFAULT);
  }

  /**
   * New instance backed by the given stream, with 2-space indentation.
   *
   * @param out
   *          Backing stream.
   * @param autoFlush
   *          Whether the output buffer will be flushed whenever a byte array is written, one of the
   *          {@code println} methods is invoked, or a newline character or byte ({@code '\n'}) is
   *          written.
   * @param charset
   *          Encoding charset.
   */
  public IndentPrintStream(OutputStream out, boolean autoFlush, Charset charset) {
    this(out, autoFlush, charset, INDENT_SYMBOL__DEFAULT);
  }

  /**
   * New instance backed by the given stream, with the given indentation symbol.
   *
   * @param out
   *          Backing stream.
   * @param autoFlush
   *          Whether the output buffer will be flushed whenever a byte array is written, one of the
   *          {@code println} methods is invoked, or a newline character or byte ({@code '\n'}) is
   *          written.
   * @param charset
   *          Encoding charset.
   * @param indentSymbol
   *          String representation of an indentation step.
   */
  public IndentPrintStream(OutputStream out, boolean autoFlush, Charset charset,
      String indentSymbol) {
    super(out, autoFlush, charset);

    setIndentSymbol(indentSymbol);
  }

  /**
   * New instance backed by {@link ByteArrayOutputStream}, with {@link StandardCharsets#UTF_8 UTF_8}
   * encoding charset and the given indentation symbol.
   *
   * @param indentSymbol
   *          String representation of an indentation step.
   */
  public IndentPrintStream(String indentSymbol) {
    super();

    setIndentSymbol(indentSymbol);
  }

  @Override
  public IndentPrintStream append(CharSequence csq) {
    return (IndentPrintStream) super.append(csq);
  }

  @Override
  public IndentPrintStream append(CharSequence csq, int start, int end) {
    return (IndentPrintStream) super.append(csq, start, end);
  }

  @Override
  public IndentPrintStream append(char c) {
    return (IndentPrintStream) super.append(c);
  }

  /**
   * Current indentation level.
   */
  public int getIndent() {
    return indentChunk.length() / indentSymbol.length();
  }

  /**
   * String representation of an indentation step.
   */
  public String getIndentSymbol() {
    return indentSymbol;
  }

  /**
   * Additional indentation level for wrapped lines.
   */
  public int getWrapIndent() {
    return wrapIndentChunk.length() / indentSymbol.length();
  }

  /**
   * Adds a level to the {@linkplain #getIndent() current indentation}.
   *
   * @see #undent()
   */
  public IndentPrintStream indent() {
    return setIndent(getIndent() + 1);
  }

  @Override
  public void print(@Nullable Object obj) {
    print(Objects.toString(obj));
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note that newlines, as identified by {@link String#lines()}, are considered for indentation
   * purposes.
   * </p>
   */
  @Override
  public void print(@Nullable String s) {
    if (s != null) {
      String[] lines = s.split("\\r?\\n", -1 /* Preserves trailing empty lines */);
      for (int i = 0, last = lines.length - 1; i <= last; i++) {
        var line = lines[i];
        if (!line.isEmpty()) {
          ensurePrintIndent();
          super.print(line);
        }
        if (i < last) {
          println();
        }
      }
    } else {
      //noinspection ConstantValue -- retain `s` reference for consistency
      super.print(s);
    }
  }

  @Override
  public void print(boolean b) {
    ensurePrintIndent();
    super.print(b);
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note that if the character is {@code '\n'}, it is considered newline for indentation purposes;
   * any other newline-related character (such as {@code '\r'}) is opaque (just printed, without
   * influence on indentation).
   * </p>
   */
  @Override
  public void print(char c) {
    ensurePrintIndent();
    super.print(c);

    if (c == LF) {
      lineStarting = true;
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note that newlines, as identified by {@link String#lines()}, are considered for indentation
   * purposes.
   * </p>
   */
  @Override
  public void print(char[] s) {
    print(new String(s));
  }

  @Override
  public void print(double d) {
    ensurePrintIndent();
    super.print(d);
  }

  @Override
  public void print(float f) {
    ensurePrintIndent();
    super.print(f);
  }

  @Override
  public void print(int i) {
    ensurePrintIndent();
    super.print(i);
  }

  @Override
  public void print(long l) {
    ensurePrintIndent();
    super.print(l);
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note that newlines, as identified by {@link String#lines()}, are considered for indentation
   * purposes.
   * </p>
   */
  @Override
  @SuppressWarnings("RedundantStringFormatCall" /* String is resolved BEFORE being indented */)
  public IndentPrintStream printf(Locale locale, String format, Object... args) {
    print(String.format(locale, format, args));
    return this;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note that newlines, as identified by {@link String#lines()}, are considered for indentation
   * purposes.
   * </p>
   */
  @Override
  @SuppressWarnings("RedundantStringFormatCall" /* String is resolved BEFORE being indented */)
  public IndentPrintStream printf(String format, Object... args) {
    print(String.format(format, args));
    return this;
  }

  @Override
  public void println() {
    super.println();

    lineStarting = true;
  }

  /**
   * @throws IllegalStateException
   *           if the backing stream is not {@link ByteArrayOutputStream}.
   */
  @Override
  public void resetData() {
    super.resetData();

    lineStarting = true;
  }

  /**
   * Sets {@link #getIndent() indent}.
   *
   * @see #indent()
   */
  public IndentPrintStream setIndent(int value) {
    indentChunk = getIndentChunk(max(0, value));
    return this;
  }

  /**
   * Sets {@link #getIndentSymbol() indentSymbol}.
   */
  public IndentPrintStream setIndentSymbol(String value) {
    if (!requireNonNull(value).equals(indentSymbol)) {
      int indent;
      int wrapIndent;
      //noinspection ConstantValue -- `@InitNonNull indentChunk`
      if (indentChunk != null) {
        indent = getIndent();
        wrapIndent = getWrapIndent();
      } else {
        indent = INDENT__DEFAULT;
        wrapIndent = WRAP_INDENT__DEFAULT;
      }

      indentSymbol = value;

      // Reset cache!
      indentChunks.clear();
      indentChunks.add(EMPTY);

      // Update current indentations!
      setIndent(indent);
      setWrapIndent(wrapIndent);
    }
    return this;
  }

  /**
   * Sets {@link #getWrapIndent() wrapIndent}.
   * <p>
   * Typically called on initialization only.
   * </p>
   *
   * @see #setIndent(int)
   */
  public IndentPrintStream setWrapIndent(int value) {
    wrapIndentChunk = getIndentChunk(max(0, value));
    return this;
  }

  /**
   * Removes a level from the {@linkplain #getIndent() current indentation}.
   *
   * @see #indent()
   */
  public void undent() {
    setIndent(getIndent() - 1);
  }

  /**
   * Restores normal indentation after {@link #wrap()} was called.
   */
  public void unwrap() {
    if (wrapStatus == WRAP_STATUS__INACTIVE)
      return;
    else if (wrapStatus == WRAP_STATUS__ACTIVE && !lineStarting) {
      println();
    }
    wrapStatus = WRAP_STATUS__INACTIVE;
  }

  /**
   * Applies {@linkplain #getWrapIndent() additional indentation} to subsequent line breaks, until
   * {@link #unwrap()} is called.
   */
  public IndentPrintStream wrap() {
    /*
     * NOTE: If line start is pending, then wrapping must be delayed after it, otherwise the very
     * beginning of the line will be affected (line wrapping is about the newlines *after* the
     * initial line).
     */
    wrapStatus = lineStarting ? WRAP_STATUS__PENDING : WRAP_STATUS__ACTIVE;
    return this;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note also that the bytes is simply passed-through as opaque data, without any indentation; in
   * particular, no newline detection effort is applied. If indentation is required, convert them to
   * {@code String} and pass the latter instead.
   * </p>
   */
  @Override
  public void write(byte[] buf) throws IOException {
    super.write(buf);
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note also that the bytes is simply passed-through as opaque data, without any indentation; in
   * particular, no newline detection effort is applied. If indentation is required, convert them to
   * {@code String} and pass the latter instead.
   * </p>
   */
  @Override
  public void write(byte[] buf, int off, int len) {
    super.write(buf, off, len);
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note also that the byte is simply passed-through as opaque data, without any indentation; in
   * particular, no newline detection effort is applied. If indentation is required, convert it to
   * {@code char} (or {@code String}, depending on its encoding) and pass the latter instead.
   * </p>
   */
  @Override
  public void write(int b) {
    super.write(b);
  }

  private void ensurePrintIndent() {
    if (lineStarting) {
      super.print(indentChunk);
      switch (wrapStatus) {
        case WRAP_STATUS__INACTIVE:
          // NOP
          break;
        case WRAP_STATUS__PENDING:
          wrapStatus = WRAP_STATUS__ACTIVE;
          break;
        case WRAP_STATUS__ACTIVE:
          super.print(wrapIndentChunk);
          break;
        default:
          throw unexpected(wrapStatus);
      }
      lineStarting = false;
    }
  }

  private String getIndentChunk(int level) {
    String ret;
    if (level < indentChunks.size())
      ret = indentChunks.get(level);
    else if (level == indentChunks.size()) {
      indentChunks.add(ret = indentChunks.get(level - 1) + indentSymbol);
    } else {
      if (level > INDENT__MAX)
        throw wrongArg("level", level, "limit EXCEEDED ({})", INDENT__MAX);

      var b = new StringBuilder(indentChunks.get(indentChunks.size() - 1));
      while (level >= indentChunks.size()) {
        indentChunks.add(b.append(indentSymbol).toString());
      }
      ret = b.toString();
    }
    return ret;
  }
}