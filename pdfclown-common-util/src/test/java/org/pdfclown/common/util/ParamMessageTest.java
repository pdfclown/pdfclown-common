/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ParamMessageTest.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.pdfclown.common.build.test.assertion.Matchers.matchesEvent;
import static org.pdfclown.common.util.Strings.EMPTY;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.invocation.InvocationOnMock;
import org.pdfclown.common.build.test.assertion.LogCaptor;
import org.pdfclown.common.util.ParamMessage.Formatter;
import org.pdfclown.common.util.__test.BaseTest;
import org.slf4j.event.Level;

/**
 * @author Stefano Chizzolini
 */
class ParamMessageTest extends BaseTest {
  @RegisterExtension
  static LogCaptor logged = LogCaptor.of(ParamMessage.class);

  @Test
  void format() {
    assertThat(ParamMessage.format(ParamMessage.ARG + " message " + ParamMessage.ARG, "ARG0", 99),
        is("ARG0 message 99"));

    assertThat(logged.noEvent(), is(true));
  }

  @Test
  void format__argumentMissing() {
    assertThat(ParamMessage.format(ParamMessage.ARG + " message " + ParamMessage.ARG, "ARG0"),
        is("ARG0 message {}"));

    assertThat(logged.getEvents(), contains(matchesEvent(Level.WARN,
        "Argument 1 missing for placeholder \"{}\" (format: \"{} message {}\")", null)));
  }

  @Test
  void format__placeholderMissing() {
    assertThat(ParamMessage.format(ParamMessage.ARG + " message", "ARG0", 99), is("ARG0 message"));

    assertThat(logged.getEvents(), contains(matchesEvent(Level.WARN,
        "Placeholder \"{}\" missing for argument 1 (format: \"{} message\")", null)));
  }

  /**
   * Tests that {@link ParamMessage#format(Formatter, String, Object[], int)
   * ParamMessage.format(..)} is called from {@link ParamMessage#of(String, Object...)
   * ParamMessage.of(..)} — this spares us from redundant formatting tests on the latter.
   */
  @Test
  void of__verifyFormat() {
    try (var staticMock = mockStatic(ParamMessage.class)) {
      staticMock
          /* base ParamMessage.format(..) overload (ALL other format calls end up here) */
          .when(() -> ParamMessage.format(any(), any(), any(Object[].class), any(Integer.class)))
          .thenReturn(EMPTY);
      staticMock
          .when(() -> ParamMessage.of(any(), any()))
          .then(InvocationOnMock::callRealMethod);
      staticMock
          .when(() -> ParamMessage.of(any(), any(), any()))
          .then(InvocationOnMock::callRealMethod);

      ParamMessage.of("Message " + ParamMessage.ARG, "construction");

      staticMock.verify(() -> ParamMessage.format(ParamMessage.FORMATTER,
          "Message " + ParamMessage.ARG, new Object[] { "construction" }, 1));
    }
  }

  @Test
  void of__withCause() {
    final var cause = new RuntimeException();
    ParamMessage message = ParamMessage.of("Message " + ParamMessage.ARG, "construction", cause);

    assertThat(message.getDescription(), is("Message construction"));
    assertThat(message.getCause(), sameInstance(cause));
    assertThat(logged.noEvent(), is(true));
  }

  @Test
  void of__withoutCause() {
    ParamMessage message = ParamMessage.of("Message " + ParamMessage.ARG, "construction");

    assertThat(message.getDescription(), is("Message construction"));
    assertThat(message.getCause(), is(nullValue()));
    assertThat(logged.noEvent(), is(true));
  }
}
