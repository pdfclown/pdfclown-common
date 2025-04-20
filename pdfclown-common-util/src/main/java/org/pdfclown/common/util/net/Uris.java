/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (Uris.java) is part of pdfclown-common-util module in pdfClown Common project (this
  Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.util.net;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.indexOfDifference;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.INDEX__NOT_FOUND;
import static org.pdfclown.common.util.Strings.SLASH;
import static org.pdfclown.common.util.io.Files.PATH_SUPER;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.lang.PolyNull;

/**
 * Network-related utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Uris {
  /**
   * Checks whether the given resource exists.
   */
  public static boolean exists(URL url) {
    try {
      URLConnection c = url.openConnection();
      if (c instanceof HttpURLConnection) {
        var hc = (HttpURLConnection) c;
        hc.setRequestMethod("HEAD") /* Avoids body transfer on response */;
        return hc.getResponseCode() == HttpURLConnection.HTTP_OK;
      } else {
        c.connect();
        return true;
      }
    } catch (IOException ex) {
      return false;
    }
  }

  /**
   * Gets the relative URI from the given URI to the other one.
   * <p>
   * This method remedies {@link URI#relativize(URI)} limitations, since the latter cannot
   * relativize a target path if the source is a subpath (<cite>"if the path of this URI is not a
   * prefix of the path of the given URI, then the given URI is returned."</cite>), eg
   * </p>
   * <pre>
   * URI.create("https://example.io/path/from.html").relativize(
   *    URI.create("https://example.io/path/way/longer/to.html"))</pre>
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
  public static URI relativeUri(URI from, URI to) {
    if (from.isOpaque() || to.isOpaque()
        || !equalsIgnoreCase(from.getScheme(), to.getScheme())
        || !Objects.equals(from.getAuthority(), to.getAuthority()))
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
       * NOTE: If one of the relative URIs is rooted (ie, with a leading slash, kinda local
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
   * Creates the given URI.
   *
   * @param uri
   *          URI
   * @throws IllegalArgumentException
   *           If {@code uri} is illegal.
   */
  public static @PolyNull @Nullable URI uri(@PolyNull @Nullable String uri) {
    return uri != null ? URI.create(uri) : null;
  }

  /**
   * Converts the given URL to URI.
   *
   * @param url
   *          URL.
   * @throws IllegalArgumentException
   *           If {@code url} is illegal.
   */
  public static @PolyNull @Nullable URI uri(@PolyNull @Nullable URL url) {
    try {
      return url != null ? url.toURI() : null;
    } catch (URISyntaxException ex) {
      throw wrongArg("url", url, null, ex);
    }
  }

  /**
   * Converts the given URI to URL.
   *
   * @param uri
   *          URI
   * @throws IllegalArgumentException
   *           If {@code uri} is illegal.
   */
  public static @PolyNull @Nullable URL url(@PolyNull @Nullable URI uri) {
    try {
      return uri != null ? uri.toURL() : null;
    } catch (MalformedURLException ex) {
      throw wrongArg("uri", uri, null, ex);
    }
  }

  private Uris() {
  }
}
