/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (XtOutputStream.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Extended output stream.
 *
 * @author Stefano Chizzolini
 * @see IOs#outputStream(OutputStream)
 */
public abstract class XtOutputStream extends FilterOutputStream {
  protected XtOutputStream(OutputStream out) {
    super(out);
  }

  /**
   * Number of bytes written.
   */
  public abstract long getCount();

  @Override
  public abstract void write(byte[] b, int off, int len) throws IOException;

  @Override
  public abstract void write(int b) throws IOException;
}