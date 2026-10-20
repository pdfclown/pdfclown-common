/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TestEnvironment.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static java.nio.file.Files.exists;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.build.internal.temp.util.Exceptions.missing;
import static org.pdfclown.common.build.internal.temp.util.Exceptions.wrongArg;
import static org.pdfclown.common.build.internal.temp.util.Objects.asTopLevelType;
import static org.pdfclown.common.build.internal.temp.util.io.Files.FILE_EXTENSION__JAVA;

import java.nio.file.Path;
import java.util.Objects;
import org.apache.commons.lang3.stream.Streams;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.temp.util.io.ResourceNames;
import org.pdfclown.common.build.system.ProjectDirId;

/**
 * {@link Test} environment.
 * <h4>Filesystem</h4>
 * <p>
 * On the filesystem, the test environment covers test resources (both on
 * {@linkplain #resourcePath(String) target} and {@linkplain #resourceSrcPath(String) source}
 * sides), {@linkplain #typeSrcPath(Class) type sources} (both production and test kinds) and
 * {@linkplain #outputPath(String) output files}. The test environment is focused on test execution,
 * so any filesystem reference must be intended on the target side unless explicitly stated.
 * </p>
 * <h5>File object names</h5>
 * <p>
 * Within the test environment, filesystem objects are addressed by <b>names</b> similar to
 * {@linkplain Class#getResource(String) Java resource names} (see
 * {@link ResourceNames#toPath(String, Path)}), and rooted in their respective
 * {@linkplain #dir(ProjectDirId) base directories}.
 * </p>
 * <p>
 * <b>Relative names</b> are based on the subdirectory {@linkplain #localName(String) local to the
 * current test environment}, which corresponds to the fully-qualified package name of the test
 * class, followed by the {@linkplain org.pdfclown.common.util.Objects#sqn(Object) simply-qualified
 * test class name} (to better organize test-specific files).
 * </p>
 * <b>Shared names</b> (<code>@/</code>-prefixed) are based on a common subdirectory under the root
 * directory of the project where to place resources referenced by multiple subproject as an
 * alternative to test-jar packaging (useful in case of large files which are only consumed
 * locally).
 * </p>
 * <table border="1">
 * <caption>Name case summary (based on Maven's Standard Directory Layout [*])</caption>
 * <tr>
 * <th>Name case</th>
 * <th>Name</th>
 * <th>Base directory</th>
 * <th>Test environment</th>
 * <th>Path</th>
 * </tr>
 * <tr>
 * <td>Absolute root</td>
 * <td><code>/</code></td>
 * <td>{@link #resourcePath(String) target/test-classes}</td>
 * <td rowspan="7"><code>org.myproject.system.MyObjectTest</code></td>
 * <td><code>target/test-classes</code></td>
 * </tr>
 * <tr>
 * <td>Absolute</td>
 * <td><code>/images/myresource.jpg</code></td>
 * <td>{@link #resourcePath(String) target/test-classes}</td>
 * <td><code>target/test-classes/images/myresource.jpg</code></td>
 * </tr>
 * <tr>
 * <td rowspan="2">Relative root</td>
 * <td rowspan="2">(empty)</td>
 * <td>{@link #resourcePath(String) target/test-classes}</td>
 * <td><code>target/test-classes/org/myproject/system/MyObjectTest</code></td>
 * </tr>
 * <tr>
 * <td>{@link #outputPath(String) target/test-output}</td>
 * <td><code>target/test-output/org/myproject/system/MyObjectTest</code></td>
 * </tr>
 * <tr>
 * <td rowspan="2">Relative</td>
 * <td rowspan="2"><code>images/myresource.jpg</code></td>
 * <td>{@link #resourcePath(String) target/test-classes}</td>
 * <td><code>target/test-classes/org/myproject/system/MyObjectTest/images/myresource.jpg</code></td>
 * </tr>
 * <tr>
 * <td>{@link #outputPath(String) target/test-output}</td>
 * <td><code>target/test-output/org/myproject/system/MyObjectTest/images/myresource.jpg</code></td>
 * </tr>
 * <tr>
 * <td>Shared</td>
 * <td><code>@/images/myresource.jpg</code></td>
 * <td>{@link #resourcePath(String) %ROOT%/src/test/shared-resources}</td>
 * <td><code>%ROOT%/src/test/shared-resources/images/myresource.jpg</code></td>
 * </tr>
 * </table>
 * <p>
 * [*] The actual <b>project directory layout</b> is implementation-specific (see also
 * {@link org.pdfclown.common.build.system.ProjectPathResolver}).
 * </p>
 *
 * @author Stefano Chizzolini
 */
public abstract class TestEnvironment {
  private static final String RESOURCE_NAME_PREFIX__SHARED = "@/";

  private final Test test;

  protected TestEnvironment(Test test) {
    this.test = test;
  }

  /**
   * Gets a base directory.
   */
  public abstract Path dir(ProjectDirId id);

  /**
   * Resolves a name local to this test environment.
   *
   * @return
   *         <ul>
   *         <li>if {@code name} is absolute: {@code name}</li>
   *         <li>if {@code name} is relative: <code>"/%TEST_PACKAGE%/%TEST_SQN%/%name%"</code>,
   *         where {@code TEST_PACKAGE} is the slash-separated {@linkplain Class#getPackageName()
   *         fully-qualified class package name} of the test class, and {@code TEST_SQN} is the
   *         {@linkplain org.pdfclown.common.util.Objects#sqn(Object) simply-qualified class name}
   *         of the test class</li>
   *         </ul>
   * @throws IllegalArgumentException
   *           if {@code name} represents a shared resource.
   */
  public String localName(String name) {
    checkModuleBasedName(name);

    return ResourceNames.localName(name, test);
  }

  /**
   * Resolves the absolute name of a file.
   *
   * @return {@code null}, if {@code file} is outside the test space.
   */
  public @Nullable String name(Path file) {
    return Streams.of(ProjectDirId.values())
        .map($ -> {
          var ret = ResourceNames.fromPath(file, dir($));
          return ret != null && $ == ProjectDirId.TEST_RESOURCE_SHARED ? "@" + ret : ret;
        })
        .filter(Objects::nonNull)
        .min(comparingInt(String::length) /* Keeps the most specific name */).orElse(null);
  }

  /**
   * Gets the absolute path of an output file.
   *
   * @param name
   *          Output name (either absolute or relative to this test environment).
   * @throws IllegalArgumentException
   *           if {@code name} represents a shared resource.
   */
  public Path outputPath(String name) {
    checkModuleBasedName(name);

    return ResourceNames.toPath(localName(name), dir(ProjectDirId.TEST_OUTPUT));
  }

  /**
   * Gets the absolute path of a test resource on target side.
   *
   * @param name
   *          Resource name (absolute, or relative to this test environment, or shared).
   * @see #resourceSrcPath(String)
   */
  public Path resourcePath(String name) {
    return doResourcePath(name, ProjectDirId.TEST_TARGET);
  }

  /**
   * Gets the absolute path of a test resource on source side.
   *
   * @param name
   *          Resource name (absolute, or relative to this test environment, or shared).
   * @see #resourcePath(String)
   */
  public Path resourceSrcPath(String name) {
    return doResourcePath(name, ProjectDirId.TEST_RESOURCE_SOURCE);
  }

  /*
   * TODO: expose `org.pdfclown.common.util.ElementNotFoundException` instead of `RuntimeException`
   * once available.
   */
  /**
   * Gets the absolute path of a type on source side (either test or production).
   *
   * @param type
   *          Type.
   * @throws RuntimeException
   *           if file not found.
   */
  public Path typeSrcPath(Class<?> type) {
    var name = ResourceNames.absBased(
        requireNonNull(asTopLevelType(type), "`type`").getSimpleName() + FILE_EXTENSION__JAVA,
        type);
    Path ret;
    if (exists(ret = ResourceNames.toPath(name, dir(ProjectDirId.TEST_TYPE_SOURCE)))
        || exists(ret = ResourceNames.toPath(name, dir(ProjectDirId.MAIN_TYPE_SOURCE))))
      return ret;
    else
      throw missing(type, "source file", "search paths: {}, {}",
          dir(ProjectDirId.TEST_TYPE_SOURCE), dir(ProjectDirId.MAIN_TYPE_SOURCE));
  }

  /**
   * Checks the resource name is local to the test module.
   *
   * @throws IllegalArgumentException
   *           if {@code name} represents a shared resource.
   */
  protected void checkModuleBasedName(String name) {
    if (name.startsWith(RESOURCE_NAME_PREFIX__SHARED))
      throw wrongArg("name", name, "Shared resource name NOT allowed in this kind of path");
  }

  private Path doResourcePath(String name, ProjectDirId moduleDirId) {
    var shared = name.startsWith(RESOURCE_NAME_PREFIX__SHARED);
    if (shared) {
      name = name.substring(RESOURCE_NAME_PREFIX__SHARED.length());
    }
    return ResourceNames.toPath(shared ? name : localName(name),
        dir(shared ? ProjectDirId.TEST_RESOURCE_SHARED : moduleDirId));
  }
}
