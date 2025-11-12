/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TestEnvironment.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static java.util.Comparator.comparing;

import java.nio.file.Path;
import java.util.Objects;
import org.apache.commons.lang3.stream.Streams;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.system.ProjectDirId;
import org.pdfclown.common.build.util.io.ResourceNames;

/**
 * Test environment.
 * <h4>Filesystem</h4>
 * <p>
 * On the filesystem, the test environment covers test resources (both on
 * {@linkplain #resourcePath(String) target} and {@linkplain #resourceSrcPath(String) source}
 * sides), {@linkplain #typeSrcPath(Class) type sources} (both main and test kinds) and
 * {@linkplain #outputPath(String) output files}. The test environment is focused on test execution,
 * so any filesystem reference must be intended on the target side unless explicitly stated.
 * </p>
 * <h5>File object names</h5>
 * <p>
 * Within the test environment, filesystem objects are addressed by <b>names</b> similar to
 * {@linkplain Class#getResource(String) Java resource names} (see
 * {@link ResourceNames#path(String, Path)}) and are rooted in their respective
 * {@linkplain #dir(ProjectDirId) base directories}; <b>relative names</b> are based on the
 * subdirectory local to the current test environment.
 * </p>
 * <table border="1">
 * <caption>Name case summary</caption>
 * <tr>
 * <th rowspan="2">Name case</th>
 * <th colspan="4">Example</th>
 * </tr>
 * <tr>
 * <th>Test environment</th>
 * <th>Base directory</th>
 * <th>Name</th>
 * <th>Path</th>
 * </tr>
 * <tr>
 * <td>Absolute</td>
 * <td>org.myproject.system.MyObjectTest</td>
 * <td>target/test-classes</td>
 * <td>/images/myresource.jpg</td>
 * <td>target/test-classes/images/myresource.jpg</td>
 * </tr>
 * <tr>
 * <td>Absolute root</td>
 * <td>org.myproject.system.MyObjectTest</td>
 * <td>target/test-classes</td>
 * <td>/</td>
 * <td>target/test-classes</td>
 * </tr>
 * <tr>
 * <td>Relative</td>
 * <td>org.myproject.system.MyObjectTest</td>
 * <td>target/test-classes</td>
 * <td>images/myresource.jpg</td>
 * <td>target/test-classes/org/myproject/system/images/myresource.jpg</td>
 * </tr>
 * <tr>
 * <td>Relative root</td>
 * <td>org.myproject.system.MyObjectTest</td>
 * <td>target/test-classes</td>
 * <td>(empty)</td>
 * <td>target/test-classes/org/myproject/system</td>
 * </tr>
 * </table>
 *
 * @author Stefano Chizzolini
 */
public interface TestEnvironment {
  /**
   * Gets the base directory corresponding to an ID.
   */
  Path dir(ProjectDirId id);

  /**
   * Gets the absolute path of an output file (no matter whether it exists).
   *
   * @param name
   *          Output name (either absolute or relative to this environment).
   */
  Path outputPath(String name);

  /**
   * Resolves the absolute name of a file.
   *
   * @return {@code null}, if {@code file} is outside the test space.
   */
  default @Nullable String resolveName(Path file) {
    return Streams.of(ProjectDirId.values())
        .map($ -> ResourceNames.based(file, dir($), true))
        .filter(Objects::nonNull)
        .min(comparing(String::length) /* Keeps the most specific name */)
        .orElse(null);
  }

  /**
   * Gets the absolute path of a test resource on target side (no matter whether it exists).
   *
   * @param name
   *          Resource name (either absolute or relative to this environment).
   * @see #resourceSrcPath(String)
   */
  Path resourcePath(String name);

  /**
   * Gets the absolute path of a test resource on source side (no matter whether it exists).
   *
   * @param name
   *          Resource name (either absolute or relative to this environment).
   * @see #resourcePath(String)
   */
  Path resourceSrcPath(String name);

  /**
   * Gets the absolute path of a type on source side (MUST exist either in test or main sources).
   *
   * @param type
   *          Type.
   * @throws RuntimeException
   *           if file not found.
   */
  Path typeSrcPath(Class<?> type);
}
