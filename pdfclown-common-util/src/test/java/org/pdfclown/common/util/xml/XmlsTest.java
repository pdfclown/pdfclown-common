/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (XmlsTest.java) is part of pdfclown-common-util module in pdfClown Common project
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
package org.pdfclown.common.util.xml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.Test;
import org.pdfclown.common.util.__test.BaseTest;
import org.w3c.dom.ProcessingInstruction;

/**
 * @author Stefano Chizzolini
 */
public class XmlsTest extends BaseTest {
  @Test
  public void _getPseudoAttributes() {
    var pi = mock(ProcessingInstruction.class);
    {
      when(pi.getTarget()).thenReturn("xml-stylesheet");
      when(pi.getData()).thenReturn(
          "href='single-col.css'"
              + " media = \"all and (max-width: 30em)\""
              + " title =\"Ada's default style\"");
    }
    Map<String, String> attrs = Xmls.getPseudoAttributes(pi);

    assertThat(attrs.size(), equalTo(3));
    assertThat(attrs, hasEntry("href", "single-col.css"));
    assertThat(attrs, hasEntry("media", "all and (max-width: 30em)"));
    assertThat(attrs, hasEntry("title", "Ada's default style"));
  }
}
