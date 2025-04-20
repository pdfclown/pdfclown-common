/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (ParamMessageTest.java) is part of pdfclown-common-util module in pdfClown Common
  project (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;
import org.slf4j.event.Level;

/**
 * @author Stefano Chizzolini
 */
public class ParamMessageTest extends BaseTest {
  @Test
  public void _format() {
    assertEquals("Test message 101", ParamMessage.format("{} message {}", "Test", 101));
    assertNotLogged();

    assertEquals("Test message {}", ParamMessage.format("{} message {}", "Test"));
    assertLogged(Level.WARN,
        is("Argument 1 missing for placeholder '{}' (format: '{} message {}')"));
  }

  @Test
  public void _of() {
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
