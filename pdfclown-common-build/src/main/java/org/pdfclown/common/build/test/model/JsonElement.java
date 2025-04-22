/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (JsonElement.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common> (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.build.test.model;

import static org.pdfclown.common.build.internal.util.Objects.PATTERN_GROUP__CLASS_FQN;
import static org.pdfclown.common.build.internal.util.Objects.PATTERN__TO_STRING__DEFAULT;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import java.util.regex.Matcher;
import org.jspecify.annotations.Nullable;

/**
 * JSON element for domain modeling.
 * <p>
 * Common ancestor to org.json objects, overcoming the limitations of the original implementation
 * (awkwardly, org.json's flat class hierarchy makes impossible to check objects against a common
 * ancestor — see also <a href="https://github.com/google/gson">Gson</a>).
 * </p>
 *
 * @author Stefano Chizzolini
 */
public interface JsonElement {
  /**
   * Ensures proper value definition.
   *
   * <p>
   * There are two kinds of values:
   * </p>
   * <ul>
   * <li>domain objects ({@code JsonElement})</li>
   * <li>ancillary objects (any other object)</li>
   * </ul>
   *
   * @param value
   * @return {@link JsonElement}s are passed through; any other object is converted to string
   *         enforcing its stability across executions.
   */
  static @Nullable Object normValue(Object value) {
    if ((value instanceof JSONArray || value instanceof JSONObject)
        && !(value instanceof JsonElement))
      throw new IllegalArgumentException(
          String.format("JSON values MUST implement %s", JsonElement.class));

    if (value instanceof JsonElement)
      return value;
    else if (value != null) {
      String ret = value.toString();
      {
        /*
         * NOTE: Ancillary objects don't need a full serialization, just a bare minimum, convenient
         * object representation. Unfortunately, the hashcode-bound default Object.toString()
         * implementation doesn't guarantee stability across executions, so it must be purged. This
         * is NOT a perfect solution as its API explicitly stipulates that overriding
         * implementations are not required to stick to stable outputs, yet it should be enough in
         * most cases (heuristics, alas!).
         */
        Matcher matcher = PATTERN__TO_STRING__DEFAULT.matcher(ret);
        if (matcher.find()) {
          var b = new StringBuilder();
          do {
            matcher.appendReplacement(b,
                Matcher.quoteReplacement(matcher.group(PATTERN_GROUP__CLASS_FQN)));
          } while (matcher.find());
          matcher.appendTail(b);
          ret = b.toString();
        }
      }
      return ret;
    } else
      return null;
  }
}
