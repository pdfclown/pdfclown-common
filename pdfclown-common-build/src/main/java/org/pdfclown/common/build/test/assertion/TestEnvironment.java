/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (TestEnvironment.java) is part of pdfclown-common-build module in pdfClown Common
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
package org.pdfclown.common.build.test.assertion;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import org.apache.commons.lang3.stream.Streams;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.util.Resources;

/**
 * Test environment.
 * <h2>Filesystem</h2>
 * <p>
 * On the filesystem, the test environment covers test resources (both on
 * {@linkplain #resourceFile(String) target} and {@linkplain #resourceSrcFile(String) source}
 * sides), {@linkplain #typeSrcFile(Class) type sources} (both main and test kinds) and
 * {@linkplain #outputFile(String) output files}. The test environment is focused on test execution,
 * so any filesystem reference must be intended on the target side unless explicitly stated.
 * </p>
 * <h3>File object names</h3>
 * <p>
 * Within the test environment, filesystem objects are addressed by <b>names</b> similar to
 * {@linkplain Class#getResource(String) Java resource names} (see
 * {@link Resources#path(String, Path, Class)}) and are rooted in their respective
 * {@linkplain #dirPath(DirId) base directories}; <b>relative names</b> are based on the
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
   * Identifier of a base directory within the test environment.
   *
   * @author Stefano Chizzolini
   */
  public enum DirId {
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
  default File dir(DirId id) {
    return dirPath(id).toFile();
  }

  /**
   * Gets the base directory corresponding to the given identifier.
   */
  Path dirPath(DirId id);

  /**
   * Gets the absolute path of the given output (no matter whether it exists).
   *
   * @param name
   *          Output file name (either absolute or relative to this environment).
   */
  default File outputFile(String name) {
    return outputPath(name).toFile();
  }

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
  default @Nullable String resolveName(File file) {
    return resolveName(file.toPath());
  }

  /**
   * Resolves the absolute name of the given file.
   *
   * @return {@code null}, if {@code file} is outside the integration space.
   */
  default @Nullable String resolveName(Path file) {
    return Streams.of(DirId.values())
        .map($ -> Resources.absName(file, dirPath($)))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  /**
   * Gets the absolute path of the given resource on target side (no matter whether it exists).
   *
   * @param name
   *          Resource name (either absolute or relative to this environment).
   * @see #resourceSrcFile(String)
   */
  default File resourceFile(String name) {
    return resourcePath(name).toFile();
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
   * @see #resourceFile(String)
   */
  default File resourceSrcFile(String name) {
    return resourceSrcPath(name).toFile();
  }

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
  default File typeSrcFile(Class<?> type) {
    return typeSrcPath(type).toFile();
  }

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
