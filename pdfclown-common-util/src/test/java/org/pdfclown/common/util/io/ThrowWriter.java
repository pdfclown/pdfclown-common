/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ThrowWriter.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  Source: https://github.com/talsma-ict/umldoclet/blob/6a7b2e09126b38e1c49103ba85754c7fdfb01db4/src/test/java/nl/talsmasoftware/umldoclet/rendering/writers/ThrowingWriter.java

  Changes: Adaptation to pdfclown-common-util.
 */
/*
 * Copyright 2016-2022 Talsma ICT
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pdfclown.common.util.io;

import java.io.IOException;
import java.io.Writer;

// SourceName: nl.talsmasoftware.umldoclet.rendering.writers.ThrowingWriter
/**
 * Writer that throws exceptions when writing, flushing or closing.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation to pdfclown-common-util)
 */
final class ThrowWriter extends Writer {
  public static ThrowWriter throwing(Throwable throwable) {
    return new ThrowWriter(throwable);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
    throw (T) t;
  }

  private final Throwable throwable;

  private ThrowWriter(Throwable throwable) {
    this.throwable = throwable;
  }

  @Override
  public void close() throws IOException {
    sneakyThrow(throwable);
    throw (IOException) throwable;
  }

  @Override
  public void flush() throws IOException {
    sneakyThrow(throwable);
    throw (IOException) throwable;
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    sneakyThrow(throwable);
    throw (IOException) throwable;
  }
}
