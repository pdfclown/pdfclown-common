/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Uris.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.net;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.indexOfDifference;
import static org.pdfclown.common.util.Chars.SLASH;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Objects.INDEX__NOT_FOUND;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.io.Files.PATH_SUPER;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.stream.Streams;

/**
 * URI-related utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Uris {
  /**
   * {@code classpath} resource protocol.
   */
  public static final String SCHEME__CLASSPATH = "classpath";
  /**
   * <a href="https://en.wikipedia.org/wiki/File_URI_scheme">{@code file}</a> scheme.
   */
  public static final String SCHEME__FILE = "file";
  /**
   * {@link java.net.JarURLConnection jar} resource protocol.
   */
  public static final String SCHEME__JAR = "jar";

  /**
   * Checks whether the resource exists.
   */
  public static boolean exists(URL url) {
    try {
      URLConnection c = url.openConnection();
      if (c instanceof HttpURLConnection hc) {
        hc.setRequestMethod("HEAD") /* Avoids body transfer on response */;
        return hc.getResponseCode() == HttpURLConnection.HTTP_OK;
      } else {
        c.connect();
        return true;
      }
    } catch (IOException ex) {
      // NOP
    }
    return false;
  }

  /**
   * Gets the relative URI from the URI to the other one.
   * <p>
   * This method remedies {@link URI#relativize(URI)} limitations, since the latter cannot
   * relativize a target path if the source is a subpath (<cite>"if the path of this URI is not a
   * prefix of the path of the given URI, then the given URI is returned."</cite>) — for example,
   * </p>
   * <pre class="lang-java"><code>
   * URI.create("https://example.io/path/from.html")
   *     .relativize(URI.create("https://example.io/path/way/longer/to.html"))</code></pre>
   * <p>
   * weirdly returns
   * </p>
   * <pre>
   * https://example.io/path/way/longer/to.html</pre>
   * <p>
   * instead of the canonical
   * </p>
   * <pre>
   * way/longer/to.html</pre>
   */
  public static URI relativize(URI from, URI to) {
    if (from.isOpaque() || to.isOpaque()
        || !Strings.CI.equals(from.getScheme(), to.getScheme())
        || !Strings.CI.equals(from.getAuthority(), to.getAuthority()))
      return to;

    // Normalize paths!
    from = from.normalize();
    to = to.normalize();

    String fromPath = from.getPath();
    String toPath = to.getPath();

    // Find raw common path segment!
    int index = indexOfDifference(fromPath, toPath);
    // Same URI?
    if (index == INDEX__NOT_FOUND)
      /*
       * NOTE: The relative URI of the same URI is empty.
       */
      return URI.create(EMPTY);
    // Both relative URIs, without common chunk?
    else if (index == 0 && from.getScheme() == null) {
      /*
       * Mutually-incompatible relative URIs (one of them is rooted)?
       *
       * NOTE: If one of the relative URIs is rooted (that is, with a leading slash, kinda local
       * absolute), then they cannot be related to each other, and `to` must be returned as-is.
       */
      if (fromPath.charAt(index) == SLASH || toPath.charAt(index) == SLASH)
        return to;
    }

    // Get distinct subpath start at last common directory!
    index = fromPath.lastIndexOf(SLASH, index) + 1;
    return URI.create(
        (PATH_SUPER + SLASH).repeat(countMatches(fromPath.substring(index), SLASH))
            + toPath.substring(index));
  }

  /**
   * Gets the URI corresponding to a path.
   * <p>
   * Contrary to {@link Path#toUri()}, this method supports also <b>relative URIs</b>, remedying the
   * limitation of the standard API which forcibly resolves relative paths as absolute URIs against
   * the current user directory. On the other hand, absolute paths are normalized before being
   * converted.
   * </p>
   */
  public static URI uri(Path path) {
    return path.isAbsolute()
        ? path.normalize().toUri()
        : URI.create(Streams.of(path)
            .map(Path::toString)
            .collect(joining(S + SLASH) /*
                                         * Forces the URI separator instead of the default
                                         * filesystem separator
                                         */));
  }

  /**
   * Gets the URI corresponding to a string.
   *
   * @throws IllegalArgumentException
   *           if {@code uri} is invalid.
   */
  public static URI uri(String uri) {
    return URI.create(uri);
  }

  /**
   * Gets the URI corresponding to a URL.
   *
   * @throws IllegalArgumentException
   *           if {@code url} is invalid.
   */
  public static URI uri(URL url) {
    try {
      return url.toURI();
    } catch (URISyntaxException ex) {
      throw wrongArg("url", url, null, ex);
    }
  }

  /**
   * Gets the URL corresponding to the path.
   *
   * @throws IllegalArgumentException
   *           if {@code path} is relative.
   */
  public static URL url(Path path) {
    return url(uri(path));
  }

  /**
   * Gets the URL corresponding to a string.
   *
   * @throws IllegalArgumentException
   *           if {@code url} is invalid.
   */
  public static URL url(String url) {
    return url(uri(url));
  }

  /**
   * Gets the URL corresponding to a URI.
   *
   * @throws IllegalArgumentException
   *           if {@code uri} is invalid.
   */
  public static URL url(URI uri) {
    try {
      return uri.toURL();
    } catch (MalformedURLException ex) {
      throw wrongArg("uri", uri, null, ex);
    }
  }

  private Uris() {
  }
}
