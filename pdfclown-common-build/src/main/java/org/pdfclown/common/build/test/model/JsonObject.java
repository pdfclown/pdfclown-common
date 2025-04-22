/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (JsonObject.java) is part of pdfclown-common-build module in pdfClown Common project
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

import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import com.github.openjson.JSONStringer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * JSON object for domain modeling.
 *
 * @author Stefano Chizzolini
 */
public class JsonObject
    extends JSONObject
    implements JsonElement {
  private Comparator<String> keyComparator;

  JsonObject(Comparator<String> keyComparator) {
    this.keyComparator = keyComparator != null ? keyComparator : Comparator.naturalOrder();
  }

  @Override
  public JSONObject put(String name, Object value) throws JSONException {
    return super.put(name, JsonElement.normValue(value));
  }

  @Override
  protected void encode(JSONStringer stringer) throws JSONException {
    stringer.object();
    List<String> keys = new ArrayList<>();
    {
      /*
       * NOTE: In order to get a convenient and reproducible output, key sorting is enforced.
       */
      keys().forEachRemaining($ -> keys.add($));
      Collections.sort(keys, keyComparator);
    }
    for (String key : keys) {
      stringer.key(key).value(get(key));
    }
    stringer.endObject();
  }
}
