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

/**
 * {@link Resource} base implementation.
 *
 * @author Stefano Chizzolini
 */
public abstract class AbstractResource implements Resource {
  /**
   * "{@code classpath}" resource protocol.
   */
  static final String URI_SCHEME__CLASSPATH = "classpath";
  /**
   * "{@code file}" resource protocol.
   */
  static final String URI_SCHEME__FILE = "file";
  /**
   * "{@code jar}" resource protocol.
   */
  static final String URI_SCHEME__JAR = "jar";

  static final String URI_SCHEME_PART__CLASSPATH = URI_SCHEME__CLASSPATH + COLON;
  static final String URI_SCHEME_PART__FILE = URI_SCHEME__FILE + COLON;

  private final String name;

  protected AbstractResource(String name) {
    this.name = requireNonNull(name, "`name`");
  }

  @Override
  public String getName() {
    return name;
  }
}
