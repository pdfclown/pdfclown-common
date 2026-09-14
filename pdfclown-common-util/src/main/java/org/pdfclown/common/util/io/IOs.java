/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (IOs.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * I/O utilities.
 *
 * @author Stefano Chizzolini
 */
public final class IOs {
  /**
   * Wraps an output stream.
   */
  public static XtOutputStream outputStream(OutputStream base) {
    if (base instanceof XtOutputStream out)
      return out;
    else if (base instanceof ByteArrayOutputStream out)
      return new XtOutputStream(out) {
        @Override
        public long getCount() {
          return ((ByteArrayOutputStream) out).size();
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
          out.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
          out.write(b);
        }
      };
    else
      return new XtOutputStream(base) {
        private long count = 0;

        @Override
        public long getCount() {
          return count;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
          out.write(b, off, len);
          count += len;
        }

        @Override
        public void write(int b) throws IOException {
          out.write(b);
          count++;
        }
      };
  }

  private IOs() {
  }
}
