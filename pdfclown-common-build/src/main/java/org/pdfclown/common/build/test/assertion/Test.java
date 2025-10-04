/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Test.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static org.pdfclown.common.build.internal.util_.Conditions.requireNonNullElseThrow;
import static org.pdfclown.common.build.internal.util_.Exceptions.wrongArg;
import static org.pdfclown.common.build.internal.util_.Objects.sqn;
import static org.pdfclown.common.build.internal.util_.Strings.UNDERSCORE;
import static org.pdfclown.common.build.internal.util_.io.Files.filename;

import org.pdfclown.common.build.util.io.ResourceNames;

/**
 * Test unit.
 *
 * @author Stefano Chizzolini
 */
public interface Test {
  /**
   * Test environment.
   */
  TestEnvironment getEnv();

  /**
   * Qualifies a resource name prepending the simple name of this test unit to its filename.
   * <p>
   * Useful for referencing resources specific to this test unit.
   * </p>
   * <p>
   * For example, if the fully-qualified name of this test unit is
   * {@code "io.mydomain.myproject.MyOuterClassIT"} and {@code name} is
   * {@code "my/path/MyResource"}, it returns {@code "my/path/MyOuterClassIT_MyResource"}.
   * </p>
   *
   * @param name
   *          Resource name.
   */
  default String subName(String name) {
    return ResourceNames.name(requireNonNullElseThrow(ResourceNames.parent(name),
        () -> wrongArg("name", name, "MUST NOT be root")),
        sqn(this) + UNDERSCORE + filename(name));
  }
}
