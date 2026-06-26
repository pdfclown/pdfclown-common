/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (package-info.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * Charset utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Charsets {
  /**
   * Coding result.
   *
   * @author Stefano Chizzolini
   */
  public sealed interface CodingResult<R> {
    /**
     * Coding failure.
     */
    record Failure<R>(boolean malformed, int position, int length) implements CodingResult<R> {
    }

    /**
     * Coding success.
     */
    record Success<R>(R value) implements CodingResult<R> {
    }
  }

  /**
   * Encodes the given input.
   *
   * @return {@code null}, if the encoding failed.
   * @see #tryGetBytes(String, Charset)
   */
  @SuppressWarnings("ByteBufferBackingArray" /* ByteBuffer is safe (properly initialized) */)
  public static byte @Nullable [] getBytesOrNull(String input, Charset charset) {
    return doGetBytes(input, charset,
        $out -> Arrays.copyOf($out.array(), $out.limit()),
        ($in, $result) -> null);
  }

  /**
   * Decodes the given input.
   *
   * @return {@code null}, if the encoding failed.
   * @see #tryString(byte[], Charset)
   */
  public static @Nullable String stringOrNull(byte[] input, Charset charset) {
    return doString(input, charset,
        CharBuffer::toString,
        ($in, $result) -> null);
  }

  /**
   * Encodes the given input.
   *
   * @see #getBytesOrNull(String, Charset)
   */
  @SuppressWarnings("ByteBufferBackingArray" /* ByteBuffer is safe (properly initialized) */)
  public static CodingResult<byte[]> tryGetBytes(String input, Charset charset) {
    return doGetBytes(input, charset,
        $out -> new CodingResult.Success<>(Arrays.copyOf($out.array(), $out.limit())),
        ($in, $result) -> new CodingResult.Failure<>($result.isMalformed(), $in.position(),
            $result.length()));
  }

  /**
   * Decodes the given input.
   *
   * @see #stringOrNull(byte[], Charset)
   */
  public static CodingResult<String> tryString(byte[] input, Charset charset) {
    return doString(input, charset,
        $out -> new CodingResult.Success<>($out.toString()),
        ($in, $result) -> new CodingResult.Failure<>($result.isMalformed(), $in.position(),
            $result.length()));
  }

  private static <R extends @Nullable Object> R doGetBytes(String input, Charset charset,
      Function<ByteBuffer, R> onSuccess, BiFunction<CharBuffer, CoderResult, R> onFailure) {
    CharsetEncoder encoder = charset.newEncoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT);

    CharBuffer in = CharBuffer.wrap(input);
    ByteBuffer out = ByteBuffer.allocate((int) (input.length() * encoder.maxBytesPerChar()));

    CoderResult result = encoder.encode(in, out, true);
    if (!result.isError()) {
      result = encoder.flush(out);
    }
    if (result.isError())
      return onFailure.apply(in, result);

    out.flip();
    return onSuccess.apply(out);
  }

  private static <R extends @Nullable Object> R doString(byte[] input, Charset charset,
      Function<CharBuffer, R> onSuccess, BiFunction<ByteBuffer, CoderResult, R> onFailure) {
    CharsetDecoder decoder = charset.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT);

    ByteBuffer in = ByteBuffer.wrap(input);
    CharBuffer out = CharBuffer.allocate((int) (input.length * decoder.maxCharsPerByte()));

    CoderResult result = decoder.decode(in, out, true);
    if (!result.isError()) {
      result = decoder.flush(out);
    }
    if (result.isError())
      return onFailure.apply(in, result);

    out.flip();
    return onSuccess.apply(out);
  }

  private Charsets() {
  }
}
