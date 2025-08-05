/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (AbstractResource.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Strings.COLON;
import static org.pdfclown.common.util.net.Uris.SCHEME__CLASSPATH;
import static org.pdfclown.common.util.net.Uris.SCHEME__FILE;

import org.pdfclown.common.util.annot.Immutable;
import org.pdfclown.common.util.net.Uris;

/**
 * {@link Resource} base implementation.
 *
 * @author Stefano Chizzolini
 */
@Immutable
public abstract class AbstractResource implements Resource {
  private final String name;

  protected AbstractResource(String name) {
    this.name = requireNonNull(name, "`name`");
  }

  @Override
  public String getName() {
    return name;
  }
}
