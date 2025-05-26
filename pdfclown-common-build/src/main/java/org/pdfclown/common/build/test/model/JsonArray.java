/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JsonArray.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.model;

import org.json.JSONArray;
import org.jspecify.annotations.Nullable;

/**
 * JSON array for domain modeling.
 *
 * @author Stefano Chizzolini
 */
public class JsonArray extends JSONArray implements JsonElement {
  @Override
  public @Nullable JSONArray put(int index, @Nullable Object value) {
    return super.put(index, JsonElement.normValue(value));
  }

  @Override
  public @Nullable JSONArray put(@Nullable Object value) {
    return super.put(JsonElement.normValue(value));
  }
}
