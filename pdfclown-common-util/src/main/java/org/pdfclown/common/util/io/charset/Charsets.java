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
   * Encoding result.
   *
   * @author Stefano Chizzolini
   */
  public sealed interface EncodingResult {
    /**
     * Encoding failure.
     */
    record Failure(boolean malformed, int position, int length) implements EncodingResult {
    }

    /**
     * Encoding success.
     */
    @SuppressWarnings("ArrayRecordComponent" /*
                                              * Array is meant to be immediately consumed and
                                              * discarded, no issue from its mutability
                                              */)
    record Success(byte[] data) implements EncodingResult {
    }
  }

  /**
   * Encodes the given input.
   *
   * @return {@code null}, if the encoding failed.
   */
  @SuppressWarnings("ByteBufferBackingArray" /* ByteBuffer is safe (properly initialized) */)
  public static byte @Nullable [] getBytesOrNull(String input, Charset charset) {
    return doGetBytes(input, charset, $out -> Arrays.copyOf($out.array(), $out.limit()),
        ($in, $result) -> null);
  }

  /**
   * Encodes the given input.
   *
   * @return {@code null}, if the encoding failed.
   */
  @SuppressWarnings("ByteBufferBackingArray" /* ByteBuffer is safe (properly initialized) */)
  public static EncodingResult tryGetBytes(String input, Charset charset) {
    return doGetBytes(input, charset,
        $out -> new EncodingResult.Success(Arrays.copyOf($out.array(), $out.limit())),
        ($in, $result) -> new EncodingResult.Failure($result.isMalformed(), $in.position(),
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

  private Charsets() {
  }
}
