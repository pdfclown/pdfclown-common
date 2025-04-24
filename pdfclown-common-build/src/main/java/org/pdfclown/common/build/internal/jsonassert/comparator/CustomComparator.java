/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (CustomComparator.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.build.internal.jsonassert.comparator;

import com.github.openjson.JSONException;
import java.util.Arrays;
import java.util.Collection;
import org.pdfclown.common.build.internal.jsonassert.Customization;
import org.pdfclown.common.build.internal.jsonassert.JSONCompareMode;
import org.pdfclown.common.build.internal.jsonassert.JSONCompareResult;
import org.pdfclown.common.build.internal.jsonassert.ValueMatcherException;

public class CustomComparator extends DefaultComparator {
  private final Collection<Customization> customizations;

  public CustomComparator(JSONCompareMode mode, Customization... customizations) {
    super(mode);
    this.customizations = Arrays.asList(customizations);
  }

  @Override
  public void compareValues(String prefix, Object expectedValue, Object actualValue,
      JSONCompareResult result) throws JSONException {
    Customization customization = getCustomization(prefix);
    if (customization != null) {
      try {
        if (!customization.matches(prefix, actualValue, expectedValue, result)) {
          result.fail(prefix, expectedValue, actualValue);
        }
      } catch (ValueMatcherException ex) {
        result.fail(prefix, ex);
      }
    } else {
      super.compareValues(prefix, expectedValue, actualValue, result);
    }
  }

  private Customization getCustomization(String path) {
    for (Customization c : customizations)
      if (c.appliesToPath(path))
        return c;
    return null;
  }
}
