/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (FieldComparisonFailure.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.build.internal.jsonassert;

/**
 * Models a failure when comparing two fields.
 */
public class FieldComparisonFailure {
  private final String _field;
  private final Object _expected;
  private final Object _actual;

  public FieldComparisonFailure(String field, Object expected, Object actual) {
    this._field = field;
    this._expected = expected;
    this._actual = actual;
  }

  public Object getActual() {
    return _actual;
  }

  public Object getExpected() {
    return _expected;
  }

  public String getField() {
    return _field;
  }
}
