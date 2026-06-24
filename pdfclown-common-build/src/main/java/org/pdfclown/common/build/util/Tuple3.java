/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Tuple3.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: Copyright 2003-2026 The Apache Software Foundation

  SPDX-License-Identifier: Apache-2.0

  Source: https://github.com/apache/groovy/blob/7831e9cf9e66f20c1b7fdd8f7dfff480db7cbe18/src/main/java/groovy/lang/Tuple3.java

  Changes:
  - constructor visibility reduced to package
  - elements `v*` renamed as `e*`
  - deprecated getters deleted
 */
package org.pdfclown.common.build.util;

import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Unmodifiable;

/**
 * Triplet.
 *
 * @param <E1>
 *          Element 1 type.
 * @param <E2>
 *          Element 2 type.
 * @param <E3>
 *          Element 3 type.
 * @author Stefano Chizzolini (adaptation to pdfclown-common-build)
 */
@Unmodifiable
@SuppressWarnings("unchecked")
public class Tuple3<E1 extends @Nullable Object, E2 extends @Nullable Object,
    E3 extends @Nullable Object> extends Tuple<Object> {
  Tuple3(E1 e1, E2 e2, E3 e3) {
    super(e1, e2, e3);
  }

  public E1 getE1() {
    return (E1) get(0);
  }

  public E2 getE2() {
    return (E2) get(1);
  }

  public E3 getE3() {
    return (E3) get(2);
  }
}
