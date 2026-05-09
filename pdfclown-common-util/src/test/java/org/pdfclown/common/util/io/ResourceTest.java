/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ResourceTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.nio.file.Files.newOutputStream;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.pdfclown.common.build.test.assertion.Verifiers.VERIFIER__COMBINATION;
import static org.pdfclown.common.util.Objects.simpleName;
import static org.pdfclown.common.util.Objects.toStringWithValues;
import static org.pdfclown.common.util.function.Functions.to;
import static org.pdfclown.common.util.io.Files.ensureFile;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.zip.ZipOutputStream;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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
    public @NonNull String getName() {
      return name;
    }

    @Override
    public @NonNull URI getUri() {
      return uri;
    }

    @Override
    public String toString() {
      return toStringWithValues(this, sqn, name, uri);
    }
  }

  private static final String NAME_PART__CLASSPATH__JAR_PACKAGE = "build";
  private static final String NAME_PART__CLASSPATH__PACKAGE = "org/pdfclown/common";
  private static final String NAME_PART__FILE__RELATIVE = "conf/";
  private static final String NAME_PART__NON_EXISTENT = "absent";
  private static final String NAME_PART__NON_FILE__PROTOCOL_SEPARATOR = ":";

  private static final String PATH__JAR = "/pdfclown-common-build.jar";

  final MockedStatic<Uris> urisMock = mockStatic(Uris.class, CALLS_REAL_METHODS);
  {
    // Simulates nonexistent URLs.
    urisMock.when(() -> Uris.exists(any()))
        .then($ -> !$.<URL>getArgument(0).getPath().contains(NAME_PART__NON_EXISTENT));
  }

  final ClassLoader ofClassLoaderMock = mock(ClassLoader.class);
  {
    when(ofClassLoaderMock.getResource(org.mockito.ArgumentMatchers.anyString()))
        .then($ -> {
          var name = $.<String>getArgument(0);
          return name.contains(NAME_PART__NON_EXISTENT)
              ? null /* Simulates nonexistent resource */
              : Uris.url((name.contains(NAME_PART__CLASSPATH__JAR_PACKAGE)
                  ? "jar:file:" + PATH__JAR + "!/" /* Simulates JAR URL */
                  : "file:/" /* Simulates FILE URL */)
                  + name);
        });
  }

  @Test
  void of() throws IOException {
    try (var fs = Jimfs.newFileSystem("local", Configuration.unix())) {
      UnaryOperator<Path> fileResolver = $ -> fs.getPath("/home/test/root").resolve($);

      var names = asList(
          null,
          // explicit classpath resource, mocked to resolve to JAR URL
          "classpath" + NAME_PART__NON_FILE__PROTOCOL_SEPARATOR + NAME_PART__CLASSPATH__PACKAGE
              + "/" + NAME_PART__CLASSPATH__JAR_PACKAGE + "/conf/checkstyle/checkstyle-checks.xml",
          // explicit classpath resource, mocked to resolve to JAR URL, nonexistent
          "classpath" + NAME_PART__NON_FILE__PROTOCOL_SEPARATOR + NAME_PART__CLASSPATH__PACKAGE
              + "/" + NAME_PART__CLASSPATH__JAR_PACKAGE + "/" + NAME_PART__NON_EXISTENT
              + "/conf/checkstyle/checkstyle-checks.xml",
          // implicit classpath resource, mocked to resolve to JAR URL
          NAME_PART__CLASSPATH__PACKAGE + "/" + NAME_PART__CLASSPATH__JAR_PACKAGE
              + "/conf/checkstyle/checkstyle-checks.xml",
          // implicit classpath resource, mocked to resolve to JAR URL, nonexistent
          NAME_PART__CLASSPATH__PACKAGE + "/" + NAME_PART__CLASSPATH__JAR_PACKAGE + "/"
              + NAME_PART__NON_EXISTENT + "/conf/checkstyle/checkstyle-checks.xml",
          // explicit classpath resource, mocked to resolve to FILE URL
          "classpath" + NAME_PART__NON_FILE__PROTOCOL_SEPARATOR + NAME_PART__CLASSPATH__PACKAGE
              + "/util/conf/checkstyle/checkstyle-checks.xml",
          // explicit classpath resource, mocked to resolve to FILE URL, nonexistent
          "classpath" + NAME_PART__NON_FILE__PROTOCOL_SEPARATOR + NAME_PART__CLASSPATH__PACKAGE
              + "/util/" + NAME_PART__NON_EXISTENT + "/conf/checkstyle/checkstyle-checks.xml",
          // implicit classpath resource, mocked to resolve to FILE URL
          NAME_PART__CLASSPATH__PACKAGE + "/util/conf/checkstyle/checkstyle-checks.xml",
          // implicit classpath resource, mocked to resolve to FILE URL, nonexistent
          NAME_PART__CLASSPATH__PACKAGE + "/util/" + NAME_PART__NON_EXISTENT
              + "/conf/checkstyle/checkstyle-checks.xml",
          // filesystem resource, absolute
          "/home/myuser/conf/checkstyle/checkstyle-checks.xml",
          // filesystem resource, relative
          NAME_PART__FILE__RELATIVE + "checkstyle/checkstyle-checks.xml",
          // generic URL resource
          "https" + NAME_PART__NON_FILE__PROTOCOL_SEPARATOR
              + "//www.example.io/conf/checkstyle/checkstyle-checks.xml",
          // generic URL resource, nonexistent
          "https" + NAME_PART__NON_FILE__PROTOCOL_SEPARATOR + "//www.example.io/"
              + NAME_PART__NON_EXISTENT + "/conf/checkstyle/checkstyle-checks.xml");

      // Ensure files existence in mocked filesystem!
      for (var name : names) {
        if (name != null
            && !(name.contains(/*
                                * Simulates that paths containing "org/pdfclown/common" don't belong
                                * to the filesystem (so they are treated as classpath resources)
                                */ NAME_PART__CLASSPATH__PACKAGE)
                || name.contains(/*
                                  * Simulates that paths containing the colon symbol don't belong to
                                  * the filesystem (so they are treated as URI-based (either
                                  * classpath or web) resources -- the assumption here is that the
                                  * filesystem is Unix-like)
                                  */ NAME_PART__NON_FILE__PROTOCOL_SEPARATOR))) {
          var file = fs.getPath(name);
          if (name.startsWith(NAME_PART__FILE__RELATIVE)) {
            file = fileResolver.apply(file); /* Simulates the resolution of relative paths */
          }
          ensureFile(file);
        }
      }

      // Ensure JAR file existence in mocked filesystem!
      try (var ignored = new ZipOutputStream(newOutputStream(fs.getPath(PATH__JAR)))) {
        // NOP: empty file.
      }

      VERIFIER__COMBINATION.verify(
          (name) -> to(AbstractResource.of(name, ofClassLoaderMock, fileResolver, fs),
              $ -> new ResourceResult(simpleName($), $.getName(), $.getUri())),
          List.of("name"),
          // name
          names);
    }
  }

  @AfterAll
  void onAllAfter() {
    urisMock.close();
  }
}
