/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (WebResource.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.util.Objects.requireNonNull;

import java.net.URL;

/**
 * Generic web resource.
 *
 * @author Stefano Chizzolini
 */
public class WebResource extends AbstractResource {
  private final URL url;

  WebResource(String name, URL url) {
    super(name);

    this.url = requireNonNull(url, "`url`");
  }

  @Override
  public URL getUrl() {
    return url;
  }
}
