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

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;
import org.slf4j.event.Level;

/**
 * @author Stefano Chizzolini
 */
class ParamMessageTest extends BaseTest {
  @Test
  void format() {
    assertEquals("Test message 101", ParamMessage.format("{} message {}", "Test", 101));
    assertNotLogged();

    assertEquals("Test message {}", ParamMessage.format("{} message {}", "Test"));
    assertLogged(Level.WARN,
        is("Argument 1 missing for placeholder '{}' (format: '{} message {}')"));
  }

  @Test
  void of() {
    {
      ParamMessage message = ParamMessage.of("Message {}", "construction");
      assertEquals("Message construction", message.getDescription());
      assertNull(message.getCause());
      assertNotLogged();
    }

    {
      final var cause = new RuntimeException();
      ParamMessage message = ParamMessage.of("Message {}", "construction", cause);
      assertEquals("Message construction", message.getDescription());
      assertSame(cause, message.getCause());
      assertNotLogged();
    }

    {
      final var cause = new RuntimeException();
      ParamMessage message = ParamMessage.of("Message {}", "construction", 5334, cause);
      assertEquals("Message construction", message.getDescription());
      assertSame(cause, message.getCause());
      assertLogged(Level.WARN,
          is("Placeholder '{}' missing for argument 1 (format: 'Message {}')"));
    }
  }
}
