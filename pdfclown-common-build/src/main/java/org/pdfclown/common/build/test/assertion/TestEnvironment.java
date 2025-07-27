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

import java.nio.file.Path;
import java.util.Objects;
import org.apache.commons.lang3.stream.Streams;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.util.io.ResourceNames;

/**
 * Test environment.
 * <h2>Filesystem</h2>
 * <p>
 * On the filesystem, the test environment covers test resources (both on
 * {@linkplain #resourcePath(String) target} and {@linkplain #resourceSrcPath(String) source}
 * sides), {@linkplain #typeSrcPath(Class) type sources} (both main and test kinds) and
 * {@linkplain #outputPath(String) output files}. The test environment is focused on test execution,
 * so any filesystem reference must be intended on the target side unless explicitly stated.
 * </p>
 * <h3>File object names</h3>
 * <p>
 * Within the test environment, filesystem objects are addressed by <b>names</b> similar to
 * {@linkplain Class#getResource(String) Java resource names} (see
 * {@link ResourceNames#path(String, Path, Class)}) and are rooted in their respective
 * {@linkplain #dir(DirId) base directories}; <b>relative names</b> are based on the subdirectory
 * local to the current test environment.
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
   * Identifier of a base directory within the test environment.
   *
   * @author Stefano Chizzolini
   */
  enum DirId {
    /**
     * Base directory of test output (typically, {@code "target/test-output"}).
     */
    OUTPUT,
    /**
     * Base directory of test resources' target (typically, {@code "target/test-classes"}).
     */
    RESOURCE,
    /**
     * Base directory of test resources' source (typically, {@code "src/test/resources"}).
     */
    RESOURCE_SRC,
    /**
     * Base directory of test types' source (typically, {@code "src/test/java"}).
     */
    TYPE_SRC,
    /**
     * Base directory of main types' source (typically, {@code "src/main/java"}).
     */
    MAIN_TYPE_SRC
  }

  /**
   * Gets the base directory corresponding to the given identifier.
   */
  Path dir(DirId id);

  /**
   * Gets the absolute path of the given output (no matter whether it exists).
   *
   * @param name
   *          Output file name (either absolute or relative to this environment).
   */
  Path outputPath(String name);

  /**
   * Resolves the absolute name of the given file.
   *
   * @return {@code null}, if {@code file} is outside the integration space.
   */
  default @Nullable String resolveName(Path file) {
    return Streams.of(DirId.values())
        .map($ -> ResourceNames.absName(file, dir($)))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  /**
   * Gets the absolute path of the given resource on target side (no matter whether it exists).
   *
   * @param name
   *          Resource name (either absolute or relative to this environment).
   * @see #resourceSrcPath(String)
   */
  Path resourcePath(String name);

  /**
   * Gets the absolute path of the given resource on source side (no matter whether it exists).
   *
   * @param name
   *          Resource name (either absolute or relative to this environment).
   * @see #resourcePath(String)
   */
  Path resourceSrcPath(String name);

  /**
   * Gets the absolute path of the given type on source side (MUST exist either in test or main
   * sources).
   *
   * @param type
   *          Type.
   * @throws RuntimeException
   *           if file not found.
   */
  Path typeSrcPath(Class<?> type);
}
