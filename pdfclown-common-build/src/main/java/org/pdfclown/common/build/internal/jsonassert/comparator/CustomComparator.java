/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (CustomComparator.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common> (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
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
