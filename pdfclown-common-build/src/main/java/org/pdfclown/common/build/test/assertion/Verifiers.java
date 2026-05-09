/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Verifiers.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static org.pdfclown.common.build.internal.temp.util.Objects.simpleName;
import static org.pdfclown.common.build.internal.temp.util.Objects.stableLiteral;
import static org.pdfclown.common.util.Objects.literal;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.net.Uris.uri;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.temp.util.Objects;

/**
 * {@link Verifier} utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Verifiers {
  private static final Pattern PATTERN__EXCEPTION_MESSAGE__STABLE = Pattern.compile(
      "\\s*\\(.+ are in module \\S+ of loader \\S+\\)");

  /**
   * Formatter normalizing the string representation of objects for test reproducibility.
   * <p>
   * Strips the string representation of its unstable parts, which may change across executions or
   * systems — see {@link Objects#stableLiteral(Object)}.
   * </p>
   */
  public static final Function<@Nullable Object, String> FORMATTER__BASIC = Objects::stableLiteral;

  /**
   * Formatter normalizing exception messages for test reproducibility.
   * <p>
   * Strips the message of its unstable parts, which may change across executions or systems.
   * Supported cases:
   * </p>
   * <ul>
   * <li>types referencing their containing module and loader
   * (<code>"\s*\(.+ are in module \S+ of loader \S+\)"</code>)</li>
   * </ul>
   */
  public static final UnaryOperator<@Nullable String> FORMATTER__EXCEPTION_MESSAGE__BASIC =
      $ -> $ != null ? stableLiteral(PATTERN__EXCEPTION_MESSAGE__STABLE.matcher($)
          .replaceAll(EMPTY)) : null;

  /**
   * Exception formatter {@linkplain #FORMATTER__EXCEPTION_MESSAGE__BASIC normalizing messages} for
   * test reproducibility.
   */
  public static final Function<Throwable, String> FORMATTER__EXCEPTION__BASIC =
      exceptionFormatter(FORMATTER__EXCEPTION_MESSAGE__BASIC);

  //------------------------------------------------------------------------------------------------
  // WARNING: DO NOT move the existing fields across this line (initialization order hazard!).
  //------------------------------------------------------------------------------------------------

  public static final CombinationVerifier VERIFIER__COMBINATION = new CombinationVerifier();
  public static final FileVerifier VERIFIER__FILE = new FileVerifier();
  public static final SimpleVerifier VERIFIER__SIMPLE = new SimpleVerifier();
  public static final TupleVerifier VERIFIER__TUPLE = new TupleVerifier();

  /**
   * Creates an exception formatter.
   */
  public static Function<Throwable, String> exceptionFormatter(
      UnaryOperator<@Nullable String> messageFormatter) {
    return $ -> "<<%s: %s>>".formatted(simpleName($), messageFormatter.apply($.getMessage()));
  }

  /**
   * Creates an exception formatter for {@linkplain #pathExceptionMessageFormatter(Path) path
   * normalization} based on the given directory.
   */
  public static Function<Throwable, String> pathExceptionFormatter(Path baseDir) {
    return exceptionFormatter(FORMATTER__EXCEPTION_MESSAGE__BASIC
        .compose(pathExceptionMessageFormatter(baseDir))::apply);
  }

  /**
   * Creates an exception message formatter for path normalization based on the given directory.
   * <p>
   * Paths are treated as opaque strings, without applying any syntactic normalization; those
   * matching {@code baseDir} are relativized to ensure test reproducibility.
   * </p>
   * <p>
   * <span class="important">IMPORTANT: This specialized formatter is intended for
   * {@linkplain Function#andThen(Function) composition} with other formatters such as
   * {@link #FORMATTER__EXCEPTION_MESSAGE__BASIC}.</span>
   * </p>
   */
  public static UnaryOperator<@Nullable String> pathExceptionMessageFormatter(Path baseDir) {
    Pattern baseDirPattern = Pattern.compile("\"%s.(.+?)\""
        .formatted(Pattern.quote(baseDir.toString())));
    return $ -> {
      /*
       * NOTE: Here we normalize paths in the failure message purging their system-specific project
       * directory in order to make them portable across the testing systems.
       */
      var messageBuilder = new StringBuilder();
      var m = baseDirPattern.matcher($);
      while (m.find()) {
        m.appendReplacement(messageBuilder, literal(uri(m.group(1))));
      }
      m.appendTail(messageBuilder);
      return messageBuilder.toString();
    };
  }

  /**
   * Creates a path formatter.
   */
  public static Function<@Nullable Object, String> pathFormatter(Path baseDir) {
    return $ -> literal(uri(baseDir.relativize((Path) $)));
  }

  /**
   * Creates a path verifier from the given one.
   */
  @SuppressWarnings("unchecked")
  public static <T extends CallVerifier> T pathVerifier(T verifier, Path baseDir) {
    return (T) verifier
        .withOutputFormatter(pathFormatter(baseDir))
        .withExceptionFormatter(pathExceptionFormatter(baseDir));
  }
}
