/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (XtPrintStream.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util.io;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

// SourceFQN: org.pdfclown.common.util.io.XtPrintStream
/**
 * Extended {@link java.io.PrintStream PrintStream}.
 * <p>
 * Improves the original implementation:
 * </p>
 * <ul>
 * <li>{@link StandardCharsets#UTF_8 UTF_8} (rather than the platform's default encoding charset of
 * the original implementation) is the default encoding charset</li>
 * <li>{@link ByteArrayOutputStream} is the default backing stream, and its data is manageable
 * directly from this interface (see {@link #toDataString()}, {@link #resetData()}), sparing users
 * from repetitive boilerplate code</li>
 * <li>derivative {@code println(..)} methods are forced to call their base component methods
 * (namely, {@code print(..)} and {@code println()}), ensuring the behavior of the latter propagates
 * to the former (on the contrary, the original contract left room to under-the-hood optimizations
 * which disrupt the consistency between equivalent calls to derivative methods and to their base
 * components)</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
public class XtPrintStream extends java.io.PrintStream {
  /**
   * New instance backed by {@link ByteArrayOutputStream}, with {@link StandardCharsets#UTF_8 UTF_8}
   * encoding charset.
   */
  public XtPrintStream() {
    this(new ByteArrayOutputStream(), true);
  }

  /**
   * New instance backed by the given stream, with {@link StandardCharsets#UTF_8 UTF_8} encoding
   * charset.
   *
   * @param out
   *          Backing stream.
   */
  public XtPrintStream(OutputStream out) {
    this(out, false);
  }

  /**
   * New instance backed by the given stream, with {@link StandardCharsets#UTF_8 UTF_8} encoding
   * charset.
   *
   * @param out
   *          Backing stream.
   * @param autoFlush
   *          Whether the output buffer will be flushed whenever a byte array is written, one of the
   *          {@code println} methods is invoked, or a newline character or byte ({@code '\n'}) is
   *          written.
   */
  public XtPrintStream(OutputStream out, boolean autoFlush) {
    this(out, autoFlush, UTF_8);
  }

  /**
   * New instance backed by the given stream, with the given encoding charset.
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
  public XtPrintStream(OutputStream out, boolean autoFlush, Charset charset) {
    super(out, autoFlush, charset);
  }

  /**
   * Prints an object, then terminates the line.
   * <p>
   * This method calls at first {@link String#valueOf(Object)} to get the printed object's string
   * value, then {@link #println(String)}.
   * </p>
   */
  @Override
  public void println(Object obj) {
    println(String.valueOf(obj));
  }

  /**
   * Prints a string, then terminates the line.
   * <p>
   * This method calls {@link #print(String)}, then {@link #println()}.
   * </p>
   */
  @Override
  public void println(String s) {
    print(s);
    println();
  }

  /**
   * Prints a boolean, then terminates the line.
   * <p>
   * This method calls {@link #print(boolean)}, then {@link #println()}.
   * </p>
   */
  @Override
  public void println(boolean b) {
    print(b);
    println();
  }

  /**
   * Prints a character, then terminates the line.
   * <p>
   * This method calls {@link #print(char)}, then {@link #println()}.
   * </p>
   */
  @Override
  public void println(char c) {
    print(c);
    println();
  }

  /**
   * Prints an array of characters, then terminates the line.
   * <p>
   * This method calls {@link #print(char[])}, then {@link #println()}.
   * </p>
   */
  @Override
  public void println(char[] s) {
    print(s);
    println();
  }

  /**
   * Prints a double, then terminates the line.
   * <p>
   * This method calls {@link #print(double)}, then {@link #println()}.
   * </p>
   */
  @Override
  public void println(double d) {
    print(d);
    println();
  }

  /**
   * Prints a float, then terminates the line.
   * <p>
   * This method calls {@link #print(float)}, then {@link #println()}.
   * </p>
   */
  @Override
  public void println(float f) {
    print(f);
    println();
  }

  /**
   * Prints an integer, then terminates the line.
   * <p>
   * This method calls {@link #print(int)}, then {@link #println()}.
   * </p>
   */
  @Override
  public void println(int i) {
    print(i);
    println();
  }

  /**
   * Prints a long, then terminates the line.
   * <p>
   * This method calls {@link #print(long)}, then {@link #println()}.
   * </p>
   */
  @Override
  public void println(long l) {
    print(l);
    println();
  }

  /**
   * Resets the backing stream, so that all accumulated output is discarded and already allocated
   * buffer space can be reused.
   *
   * @throws IllegalStateException
   *           if the backing stream is not {@link ByteArrayOutputStream}.
   */
  public void resetData() {
    baseArrayStream().reset();
  }

  /**
   * Gets the printed contents, decoded via {@link StandardCharsets#UTF_8 UTF_8} charset.
   *
   * @throws IllegalStateException
   *           if the backing stream is not {@link ByteArrayOutputStream}.
   */
  public String toDataString() {
    return toDataString(UTF_8);
  }

  /**
   * Gets the printed contents.
   *
   * @param charset
   *          Decoding charset.
   * @throws IllegalStateException
   *           if the backing stream is not {@link ByteArrayOutputStream}.
   * @see ByteArrayOutputStream#toString(Charset)
   */
  public String toDataString(Charset charset) {
    return baseArrayStream().toString(charset);
  }

  /**
   * @throws IllegalStateException
   *           if the backing stream is not {@link ByteArrayOutputStream}.
   */
  private ByteArrayOutputStream baseArrayStream() {
    if (!(out instanceof ByteArrayOutputStream))
      throw new IllegalStateException("Backing stream is NOT `ByteArrayOutputStream`");

    return (ByteArrayOutputStream) out;
  }
}
