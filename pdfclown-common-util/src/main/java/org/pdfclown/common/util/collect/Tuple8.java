/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Tuple8.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: Copyright 2003-2026 The Apache Software Foundation

  SPDX-License-Identifier: Apache-2.0

  Source: https://github.com/apache/groovy/blob/7831e9cf9e66f20c1b7fdd8f7dfff480db7cbe18/src/main/java/groovy/lang/Tuple8.java

  Changes:
  - constructor visibility reduced to package
  - elements `v*` renamed as `e*`
  - deprecated getters deleted
 */
package org.pdfclown.common.util.collect;

import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Unmodifiable;

/**
 * Octet.
 *
 * @param <E1>
 *          Element 1 type.
 * @param <E2>
 *          Element 2 type.
 * @param <E3>
 *          Element 3 type.
 * @param <E4>
 *          Element 4 type.
 * @param <E5>
 *          Element 5 type.
 * @param <E6>
 *          Element 6 type.
 * @param <E7>
 *          Element 7 type.
 * @param <E8>
 *          Element 8 type.
 * @author Stefano Chizzolini (adaptation to pdfclown-common-util)
 */
@Unmodifiable
public class Tuple8<E1 extends @Nullable Object, E2 extends @Nullable Object,
    E3 extends @Nullable Object, E4 extends @Nullable Object, E5 extends @Nullable Object,
    E6 extends @Nullable Object, E7 extends @Nullable Object, E8 extends @Nullable Object>
    extends Tuple<Object> {
  private final E1 e1;
  private final E2 e2;
  private final E3 e3;
  private final E4 e4;
  private final E5 e5;
  private final E6 e6;
  private final E7 e7;
  private final E8 e8;

  Tuple8(E1 e1, E2 e2, E3 e3, E4 e4, E5 e5, E6 e6, E7 e7, E8 e8) {
    super(e1, e2, e3, e4, e5, e6, e7, e8);

    this.e1 = e1;
    this.e2 = e2;
    this.e3 = e3;
    this.e4 = e4;
    this.e5 = e5;
    this.e6 = e6;
    this.e7 = e7;
    this.e8 = e8;
  }

  public E1 getE1() {
    return e1;
  }

  public E2 getE2() {
    return e2;
  }

  public E3 getE3() {
    return e3;
  }

  public E4 getE4() {
    return e4;
  }

  public E5 getE5() {
    return e5;
  }

  public E6 getE6() {
    return e6;
  }

  public E7 getE7() {
    return e7;
  }

  public E8 getE8() {
    return e8;
  }
}
