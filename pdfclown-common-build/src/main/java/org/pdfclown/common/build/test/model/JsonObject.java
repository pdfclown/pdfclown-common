/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JsonObject.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.model;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.jspecify.annotations.Nullable;

/**
 * JSON object for domain modeling.
 *
 * @author Stefano Chizzolini
 */
public class JsonObject extends JSONObject implements JsonElement {
  // SPDX-SnippetBegin
  // SPDX-SnippetCopyrightText: NONE
  // SPDX-License-Identifier: MIT-0
  //
  // Source: https://github.com/stleary/JSON-java/blob/82a02d879e9177105bb248a10cad1f18844b7964/src/main/java/org/json/JSONObject.java
  // SourceName: org.json.JSONObject.NUMBER_PATTERN
  /**
   * Regular Expression Pattern that matches JSON Numbers. This is primarily used for output to
   * guarantee that we are always writing valid JSON.
   */
  private static final Pattern NUMBER_PATTERN =
      Pattern.compile("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?");
  // SPDX-SnippetEnd

  // SPDX-SnippetBegin
  // SPDX-SnippetCopyrightText: NONE
  // SPDX-License-Identifier: MIT-0
  //
  // Source: ibid.
  // SourceName: org.json.JSONObject.indent(Writer, int)
  private static void indent(Writer writer, int indent) throws IOException {
    for (int i = 0; i < indent; i += 1) {
      writer.write(' ');
    }
  }
  // SPDX-SnippetEnd

  // SPDX-SnippetBegin
  // SPDX-SnippetCopyrightText: NONE
  // SPDX-License-Identifier: MIT-0
  //
  // Source: ibid.
  // SourceName: org.json.JSONObject.writeValue(Writer, Object, int, int)
  // Changes: null value flaw fixed.
  @SuppressWarnings("UnusedReturnValue")
  private static Writer writeValue(Writer writer, @Nullable Object value,
      int indentFactor, int indent) throws JSONException, IOException {
    if (value == null || value.equals("null")) {
      writer.write("null");
    } else if (value instanceof JSONString) {
      Object o;
      try {
        o = ((JSONString) value).toJSONString();
      } catch (Exception e) {
        throw new JSONException(e);
      }
      writer.write(o != null ? o.toString() : quote(value.toString()));
    } else if (value instanceof Number) {
      // not all Numbers may match actual JSON Numbers. that is fractions or Imaginary
      final String numberAsString = numberToString((Number) value);
      if (NUMBER_PATTERN.matcher(numberAsString).matches()) {
        writer.write(numberAsString);
      } else {
        // The Number value is not a valid JSON number.
        // Instead, we will quote it as a string
        quote(numberAsString, writer);
      }
    } else if (value instanceof Boolean) {
      writer.write(value.toString());
    } else if (value instanceof Enum<?>) {
      writer.write(quote(((Enum<?>) value).name()));
    } else if (value instanceof JSONObject) {
      ((JSONObject) value).write(writer, indentFactor, indent);
    } else if (value instanceof JSONArray) {
      ((JSONArray) value).write(writer, indentFactor, indent);
    } else if (value instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) value;
      new JSONObject(map).write(writer, indentFactor, indent);
    } else if (value instanceof Collection) {
      Collection<?> coll = (Collection<?>) value;
      new JSONArray(coll).write(writer, indentFactor, indent);
    } else if (value.getClass().isArray()) {
      new JSONArray(value).write(writer, indentFactor, indent);
    } else {
      quote(value.toString(), writer);
    }
    return writer;
  }
  // SPDX-SnippetEnd

  private final Comparator<String> keyComparator;

  JsonObject(@Nullable Comparator<String> keyComparator) {
    this.keyComparator = keyComparator != null ? keyComparator : Comparator.naturalOrder();
  }

  @Override
  public JSONObject put(String name, @Nullable Object value) {
    return super.put(name, JsonElement.normValue(value));
  }

  // SPDX-SnippetBegin
  // SPDX-SnippetCopyrightText: NONE
  // SPDX-License-Identifier: MIT-0
  //
  // Source: ibid.
  // SourceName: org.json.JSONObject.write(Writer, int, int)
  // Changes: sort entries by key for serialization predictability.
  @Override
  public Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
    try {
      writer.write('{');

      final int length = this.length();
      if (length == 1) {
        final Entry<String, ?> entry = this.entrySet().iterator().next();
        final String key = entry.getKey();
        writer.write(quote(key));
        writer.write(':');
        if (indentFactor > 0) {
          writer.write(' ');
        }
        try {
          writeValue(writer, entry.getValue(), indentFactor, indent);
        } catch (Exception e) {
          throw new JSONException("Unable to write JSONObject value for key: " + key, e);
        }
      } else if (length > 0) {
        final int newIndent = indent + indentFactor;
        boolean needsComma = false;
        /*
         * NOTE: In order to get a convenient and reproducible output, key sorting is enforced.
         */
        for (String key : keySet().stream().sorted(keyComparator).toArray(String[]::new)) {
          if (needsComma) {
            writer.write(',');
          }
          if (indentFactor > 0) {
            writer.write('\n');
          }
          indent(writer, newIndent);
          writer.write(quote(key));
          writer.write(':');
          if (indentFactor > 0) {
            writer.write(' ');
          }
          try {
            writeValue(writer, get(key), indentFactor, newIndent);
          } catch (Exception e) {
            throw new JSONException("Unable to write JSONObject value for key: " + key, e);
          }
          needsComma = true;
        }
        if (indentFactor > 0) {
          writer.write('\n');
        }
        indent(writer, indent);
      }
      writer.write('}');
    } catch (IOException exception) {
      throw new JSONException(exception);
    }
    return writer;
  }
  // SPDX-SnippetEnd
}
