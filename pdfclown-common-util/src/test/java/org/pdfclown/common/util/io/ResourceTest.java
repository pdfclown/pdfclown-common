/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ResourceTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.pdfclown.common.build.test.assertion.Assertions.Argument.qnamed;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.build.test.assertion.Matchers.has;
import static org.pdfclown.common.util.Objects.literal;
import static org.pdfclown.common.util.Objects.sqn;
import static org.pdfclown.common.util.Objects.toStringWithValues;
import static org.pdfclown.common.util.net.Uris.uri;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.util.Objects;
import org.pdfclown.common.util.__test.BaseTest;
import org.pdfclown.common.util.net.Uris;

/**
 * @author Stefano Chizzolini
 */
class ResourceTest extends BaseTest {
  static class ResourceResult implements Resource {
    final String name;
    final String sqn;
    final URI uri;

    ResourceResult(String sqn, String name, URI uri) {
      this.sqn = sqn;
      this.name = name;
      this.uri = uri;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public URI getUri() {
      return uri;
    }

    @Override
    public String toString() {
      return toStringWithValues(this, sqn, name, uri);
    }
  }

  static Stream<Arguments> of() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] name[0]: null
            null,
            // [2] name[1]: "classpath:org/pdfclown/common/build/conf/checkstyle/checkstyle-checks.xml"
            new ResourceResult("ClasspathResource",
                "org/pdfclown/common/build/conf/checkstyle/checkstyle-checks.xml",
                uri("jar:file:/pdfclown-common-build.jar!/org/pdfclown/common/build/conf/checkstyle/checkstyle-checks.xml")),
            // [3] name[2]: "classpath:org/pdfclown/common/build/absent/conf/checkstyle/checkstyle-checks.xml"
            null,
            // [4] name[3]: "org/pdfclown/common/build/conf/checkstyle/checkstyle-checks.xml"
            new ResourceResult("ClasspathResource",
                "org/pdfclown/common/build/conf/checkstyle/checkstyle-checks.xml",
                uri("jar:file:/pdfclown-common-build.jar!/org/pdfclown/common/build/conf/checkstyle/checkstyle-checks.xml")),
            // [5] name[4]: "org/pdfclown/common/build/absent/conf/checkstyle/checkstyle-checks.xml"
            null,
            // [6] name[5]: "classpath:org/pdfclown/common/util/conf/checkstyle/checkstyle-checks.xml"
            new ResourceResult("ClasspathResource",
                "org/pdfclown/common/util/conf/checkstyle/checkstyle-checks.xml",
                uri("file:/org/pdfclown/common/util/conf/checkstyle/checkstyle-checks.xml")),
            // [7] name[6]: "classpath:org/pdfclown/common/util/absent/conf/checkstyle/checkstyle-checks.xml"
            null,
            // [8] name[7]: "org/pdfclown/common/util/conf/checkstyle/checkstyle-checks.xml"
            new ResourceResult("ClasspathResource",
                "org/pdfclown/common/util/conf/checkstyle/checkstyle-checks.xml",
                uri("file:/org/pdfclown/common/util/conf/checkstyle/checkstyle-checks.xml")),
            // [9] name[8]: "org/pdfclown/common/util/absent/conf/checkstyle/checkstyle-checks.xml"
            null,
            // [10] name[9]: "/home/myuser/conf/checkstyle/checkstyle-checks.xml"
            new ResourceResult("FileResource", "/home/myuser/conf/checkstyle/checkstyle-checks.xml",
                uri("jimfs://local/home/myuser/conf/checkstyle/checkstyle-checks.xml")),
            // [11] name[10]: "conf/checkstyle/checkstyle-checks.xml"
            new ResourceResult("FileResource", "conf/checkstyle/checkstyle-checks.xml",
                uri("jimfs://local/home/test/root/conf/checkstyle/checkstyle-checks.xml")),
            // [12] name[11]: "https://www.example.io/conf/checkstyle/checkstyle-checks.xml"
            new ResourceResult("WebResource",
                "https://www.example.io/conf/checkstyle/checkstyle-checks.xml",
                uri("https://www.example.io/conf/checkstyle/checkstyle-checks.xml")),
            // [13] name[12]: "https://www.example.io/absent/conf/checkstyle/checkstyle-checks.xml"
            null),
        // name
        asList(
            null,
            qnamed("explicit classpath resource, mocked to resolve to JAR URL",
                "classpath:org/pdfclown/common/build/conf/checkstyle/checkstyle-checks.xml"),
            qnamed("explicit classpath resource, mocked to resolve to JAR URL, inexistent",
                "classpath:org/pdfclown/common/build/absent/conf/checkstyle/checkstyle-checks.xml"),
            qnamed("implicit classpath resource, mocked to resolve to JAR URL",
                "org/pdfclown/common/build/conf/checkstyle/checkstyle-checks.xml"),
            qnamed("implicit classpath resource, mocked to resolve to JAR URL, inexistent",
                "org/pdfclown/common/build/absent/conf/checkstyle/checkstyle-checks.xml"),
            qnamed("explicit classpath resource, mocked to resolve to FILE URL",
                "classpath:org/pdfclown/common/util/conf/checkstyle/checkstyle-checks.xml"),
            qnamed("explicit classpath resource, mocked to resolve to FILE URL, inexistent",
                "classpath:org/pdfclown/common/util/absent/conf/checkstyle/checkstyle-checks.xml"),
            qnamed("implicit classpath resource, mocked to resolve to FILE URL",
                "org/pdfclown/common/util/conf/checkstyle/checkstyle-checks.xml"),
            qnamed("implicit classpath resource, mocked to resolve to FILE URL, inexistent",
                "org/pdfclown/common/util/absent/conf/checkstyle/checkstyle-checks.xml"),
            qnamed("filesystem resource, absolute",
                "/home/myuser/conf/checkstyle/checkstyle-checks.xml"),
            qnamed("filesystem resource, relative",
                "conf/checkstyle/checkstyle-checks.xml"),
            qnamed("generic URL resource",
                "https://www.example.io/conf/checkstyle/checkstyle-checks.xml"),
            qnamed("generic URL resource, inexistent",
                "https://www.example.io/absent/conf/checkstyle/checkstyle-checks.xml")));
  }

  final MockedStatic<FileSystems> fileSystemsMock;
  {
    /*
     * NOTE: Called before mocking `FileSystems`, as it relies on its real implementation (sic!).
     */
    var defaultFsMock = Jimfs.newFileSystem("local", Configuration.unix());

    fileSystemsMock = mockStatic(FileSystems.class);
    var fileSystemMock = mock(FileSystem.class);
    {
      /*
       * Relays to default mocked filesystem.
       */
      when(fileSystemMock.getPath(any())).then($ -> Path.of($.<String>getArgument(0)));
    }
    //noinspection resource
    fileSystemsMock
        .when(() -> FileSystems.newFileSystem(any(Path.class), isNull(ClassLoader.class)))
        .thenReturn(fileSystemMock);
    fileSystemsMock.when(FileSystems::getDefault).thenReturn(defaultFsMock);
  }

  final MockedStatic<Files> filesMock = mockStatic(Files.class, CALLS_REAL_METHODS);
  {
    filesMock.when(() -> Files.exists(any(Path.class), any(LinkOption[].class)))
        .then($ -> {
          var path = $.<Path>getArgument(0).toString();
          return !(path.contains("org/pdfclown/common") /*
                                                         * Simulates that paths containing
                                                         * "org/pdfclown/common" don't belong to the
                                                         * filesystem (so they are treated as
                                                         * classpath resources)
                                                         */
              || path.contains(":") /*
                                     * Simulates that paths containing the colon symbol don't belong
                                     * to the filesystem (so they are treated as URI-based resources
                                     * -- the assumption here is that the filesystem is Unix-like)
                                     */);
        });
  }

  final MockedStatic<Uris> urisMock = mockStatic(Uris.class, CALLS_REAL_METHODS);
  {
    /*
     * Simulates that URLs containing "absent" are inexistent.
     */
    urisMock.when(() -> Uris.exists(any()))
        .then($ -> !$.<URL>getArgument(0).getPath().contains("absent"));
  }

  final ClassLoader ofClassLoaderMock = mock(ClassLoader.class);
  {
    when(ofClassLoaderMock.getResource(org.mockito.ArgumentMatchers.anyString()))
        .then($ -> {
          var name = $.<String>getArgument(0);
          return name.contains("absent")
              // Simulates inexistent resource (in case its path contains "absent").
              ? null
              : Uris.url((name.contains("build")
                  // Simulates JAR URL (in case its path contains "build").
                  ? "jar:file:/pdfclown-common-build.jar!/"
                  // Simulates FILE URL.
                  : "file:/")
                  + name);
        });
  }

  @ParameterizedTest
  @MethodSource
  void of(Expected<Resource> expected, @Nullable String name) {
    assertParameterizedOf(
        () -> Resource.of(name, ofClassLoaderMock, $ -> Path.of("/home/test/root").resolve($)),
        expected.match($ -> {
          var e = (ResourceResult) $;
          return Matchers.allOf(
              has("typename", Objects::sqn, is(e.sqn)),
              has("name", Resource::getName, is(e.name)),
              has("uri", Resource::getUri, is(e.uri)));
        }),
        () -> new ExpectedGeneration<Resource>(name)
            .setParamNames("name")
            .setExpectedSourceCodeGenerator($ -> "new ResourceResult(%s, %s, uri(%s))".formatted(
                literal(sqn($)), literal($.getName()), literal($.getUri())))
            .setMaxArgCommentLength(100)
            .setOut(System.err) /*
                                 * IMPORTANT: DO NOT remove `out` redirection, otherwise the
                                 * interaction of the test harness with the filesystem will cause
                                 * malfunctions (mind the filesystem is mocked in this test unit!).
                                 */);
  }

  @AfterAll
  void onAllAfter() {
    fileSystemsMock.close();
    filesMock.close();
    urisMock.close();
  }
}
