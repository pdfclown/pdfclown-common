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
import static org.pdfclown.common.build.test.assertion.Verifiers.COMBINATION;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.SLASH;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.io.Files.ensureFile;
import static org.pdfclown.common.util.net.Uris.SCHEME__CLASSPATH;
import static org.pdfclown.common.util.net.Uris.SCHEME__FILE;
import static org.pdfclown.common.util.net.Uris.SCHEME__HTTPS;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pdfclown.common.util.__test.BaseTest;
import org.pdfclown.common.util.net.Uris;

/**
 * @author Stefano Chizzolini
 */
class ResourceTest extends BaseTest {
  private static final String NAME_PART__CLASSPATH__JAR_PACKAGE = "build";
  private static final String NAME_PART__CLASSPATH__PACKAGE = "org/pdfclown/common";
  private static final String NAME_PART__FILE__RELATIVE = "conf/";
  private static final String NAME_PART__NON_EXISTENT = "absent";

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
          SCHEME__CLASSPATH + COLON + NAME_PART__CLASSPATH__PACKAGE
              + "/" + NAME_PART__CLASSPATH__JAR_PACKAGE + "/conf/checkstyle/checkstyle-checks.xml",
          // explicit classpath resource, mocked to resolve to JAR URL, nonexistent
          SCHEME__CLASSPATH + COLON + NAME_PART__CLASSPATH__PACKAGE
              + "/" + NAME_PART__CLASSPATH__JAR_PACKAGE + "/" + NAME_PART__NON_EXISTENT
              + "/conf/checkstyle/checkstyle-checks.xml",
          // implicit classpath resource, mocked to resolve to JAR URL
          NAME_PART__CLASSPATH__PACKAGE + "/" + NAME_PART__CLASSPATH__JAR_PACKAGE
              + "/conf/checkstyle/checkstyle-checks.xml",
          // implicit classpath resource, mocked to resolve to JAR URL, nonexistent
          NAME_PART__CLASSPATH__PACKAGE + "/" + NAME_PART__CLASSPATH__JAR_PACKAGE + "/"
              + NAME_PART__NON_EXISTENT + "/conf/checkstyle/checkstyle-checks.xml",
          // explicit classpath resource, mocked to resolve to FILE URL
          SCHEME__CLASSPATH + COLON + NAME_PART__CLASSPATH__PACKAGE
              + "/util/conf/checkstyle/checkstyle-checks.xml",
          // explicit classpath resource, mocked to resolve to FILE URL, nonexistent
          SCHEME__CLASSPATH + COLON + NAME_PART__CLASSPATH__PACKAGE
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
          // filesystem resource, absolute, as URL
          SCHEME__FILE + COLON + SLASH + "/home/myuser/conf/checkstyle/checkstyle-checks.xml",
          // generic URL resource
          SCHEME__HTTPS + COLON
              + "//www.example.io/conf/checkstyle/checkstyle-checks.xml",
          // generic URL resource, nonexistent
          SCHEME__HTTPS + COLON + "//www.example.io/"
              + NAME_PART__NON_EXISTENT + "/conf/checkstyle/checkstyle-checks.xml");

      // Ensure files existence in mocked filesystem!
      for (var name : names) {
        if (name != null
            && !(name.contains(/*
                                * Simulates that paths containing "org/pdfclown/common" don't belong
                                * to the filesystem (so they are treated as classpath resources)
                                */ NAME_PART__CLASSPATH__PACKAGE)
                || name.contains(/*
                                  * Simulates that paths containing the URI scheme separator don't
                                  * belong to the filesystem (so they are treated as URI-based
                                  * (either classpath or web) resources -- the assumption here is
                                  * that the filesystem is Unix-like)
                                  */ S + COLON))) {
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

      //noinspection DataFlowIssue : false positive (result is nullable by contract!)
      COMBINATION.verify(
          (name) -> AbstractResource.of(name, ofClassLoaderMock, fileResolver, fs).orElse(null),
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
