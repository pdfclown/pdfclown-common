/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Mocks.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.function.Supplier;
import org.mockito.MockedStatic;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;

/**
 * Object-mocking utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Mocks {
  /**
   * Mocks {@link FileSystems}.
   * <p>
   * NOTE: Once mocked, all file system I/O is redirected to the fake system, disrupting regular
   * operations. Therefore, <span class="important">it MUST be {@linkplain MockedStatic#close()
   * closed} as soon as possible.</span>
   * </p>
   *
   * @apiNote When using {@link Assertions#assertParameterized(Object, Expected, Supplier)},
   *          {@link org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration#setParamNames(String...)
   *          paramNames} MUST be specified and the source code output MUST be
   *          {@linkplain org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration#setOut(Appendable)
   *          redirected} in order to work around the file system disruption — for
   *          example:<pre class="lang-java" data-line="15-16"><code>
   * final MockedStatic&lt;FileSystems&gt; fileSystemsMock=Mocks.mockFileSystems();
   *
   * &#64;AfterAll
   * void onAllAfter() {
   *   fileSystemsMock.close();
   * }
   *
   * &#64;ParameterizedTest
   * &#64;MethodSource
   * void resourcePath(Expected&lt;Path&gt; expected, String name) {
   *   assertParameterizedOf(
   *       () -&gt; sampleTest.getEnv().resourcePath(name),
   *       expected,
   *       () -&gt; new ExpectedGeneration&lt;&gt;(name)
   *           .setParamNames("name")
   *           .setOut(System.err) &#47;*
   *                                * IMPORTANT: DO NOT remove `out` redirection, otherwise the
   *                                * interaction of the test harness with the filesystem will cause
   *                                * malfunctions (mind the filesystem is mocked in this test unit!)
   *                                *&#47;);
   * }
   *
   * static Stream&lt;Arguments&gt; resourcePath() {
   *   return argumentsStream(
   *       cartesian().&lt;String&gt;composeExpectedConverter(Path::of),
   *       // expected
   *       asList( . . .),
   *       // name
   *       asList( . . .));
   * }</code></pre>
   */
  public static MockedStatic<FileSystems> mockFileSystems() {
    final MockedStatic<FileSystems> ret;

    /*
     * NOTE: Called before mocking `FileSystems`, as it relies on its real implementation (sic!).
     */
    var defaultFsMock = Jimfs.newFileSystem("local", Configuration.unix());

    ret = mockStatic(FileSystems.class);
    var fileSystemMock = mock(FileSystem.class);
    {
      /*
       * Relays to default mocked filesystem.
       */
      when(fileSystemMock.getPath(any())).then($ -> Path.of($.<String>getArgument(0)));
    }
    //noinspection resource
    ret
        .when(() -> FileSystems.newFileSystem(any(Path.class), isNull(ClassLoader.class)))
        .thenReturn(fileSystemMock);
    ret.when(FileSystems::getDefault).thenReturn(defaultFsMock);

    return ret;
  }

  private Mocks() {
  }
}
