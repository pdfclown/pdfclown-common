/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

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
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.SLASH;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Objects.INDEX__NOT_FOUND;
import static org.pdfclown.common.util.Objects.textLiteral;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.Strings.indexOfElse;
import static org.pdfclown.common.util.Strings.lcase;
import static org.pdfclown.common.util.function.Functions.toOrNull;
import static org.pdfclown.common.util.io.Files.PATH_SUPER;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Objects;
import org.apache.commons.lang3.stream.Streams;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.Strings;

/**
 * URI-related utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Uris {
  /**
   * {@linkplain #SCHEME__JAR JAR} URL.
   * <p>
   * Syntax: <code>jar:{jarFileUri}!/{entryName}</code>
   * </p>
   *
   * @param url
   *          Full URL.
   * @param jarFileUrl
   *          Jar file component.
   * @param entryName
   *          Entry component.
   * @author Stefano Chizzolini
   */
  public record JarUrl(URL url, URL jarFileUrl, @Nullable String entryName) {
    public static JarUrl of(URL url) {
      if (!scheme(url).equals(SCHEME__JAR))
        throw wrongArg("url", url, "scheme MUST be `{}`", SCHEME__JAR);

      JarURLConnection jarUrl;
      try {
        jarUrl = (JarURLConnection) url.openConnection();
      } catch (IOException ex) {
        throw runtime(ex);
      }

      return new JarUrl(url, jarUrl.getJarFileURL(), jarUrl.getEntryName());
    }
  }

  /**
   * {@code classpath} resource protocol.
   */
  public static final String SCHEME__CLASSPATH = "classpath";
  /**
   * <a href="https://en.wikipedia.org/wiki/File_URI_scheme">{@code file}</a> scheme.
   */
  public static final String SCHEME__FILE = "file";
  /**
   * Unencrypted <a href="https://en.wikipedia.org/wiki/HTTP">Hypertext Transfer Protocol
   * ({@code HTTP})</a> scheme.
   */
  public static final String SCHEME__HTTP = "http";
  /**
   * <a href="https://en.wikipedia.org/wiki/HTTPS">Hypertext Transfer Protocol Secure
   * ({@code HTTPS})</a> scheme.
   */
  public static final String SCHEME__HTTPS = "https";

  /**
   * {@link java.net.JarURLConnection jar} resource protocol.
   */
  public static final String SCHEME__JAR = "jar";

  /**
   * Gets the effective port number of the URI.
   *
   * @return {@link URI#getPort()} if defined, otherwise {@link URL#getDefaultPort()}.
   * @throws RuntimeException
   *           if {@code uri} is malformed.
   */
  public static int effectivePort(URI uri) {
    try {
      return uri.getPort() >= 0 ? uri.getPort() : uri.toURL().getDefaultPort();
    } catch (MalformedURLException ex) {
      throw runtime(ex);
    }
  }

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
   * Ensures the URI is flattened to its bare form.
   * <p>
   * Supported schemes: {@value #SCHEME__CLASSPATH}, {@value #SCHEME__FILE}, {@value #SCHEME__HTTP},
   * {@value #SCHEME__HTTPS}, {@value #SCHEME__JAR}. <span class="important">Any other scheme is
   * rejected</span>.
   * </p>
   *
   * @return Flattened {@code uri}.
   * @throws org.pdfclown.common.util.ArgumentException
   *           if the scheme of {@code uri} is not supported.
   * @apiNote Useful to uncover opaque URIs.
   */
  @SuppressWarnings("ReferenceEquality")
  public static URI flatten(final URI uri) {
    var ret = uri;
    while (true) {
      String scheme = scheme(ret);
      switch (scheme) {
        case //
            SCHEME__CLASSPATH, //
            SCHEME__FILE, //
            SCHEME__HTTP, //
            SCHEME__HTTPS -> {
          return ret;
        }
        case SCHEME__JAR -> {
          if (ret != uri)
            throw wrongArg("uri", uri, "nested JAR URLs NOT SUPPORTED");

          ret = uri(JarUrl.of(url(ret)).jarFileUrl());
        }
        default -> throw wrongArg("uri", uri, "{} scheme NOT SUPPORTED", textLiteral(scheme));
      }
    }
  }

  /**
   * Gets the host of a URI.
   *
   * @return (lower-case) Empty, if no host is specified.
   * @implNote Contrary to {@link URI#getHost()}, this method returns empty on undefined host to
   *           ease handling.
   */
  public static String host(URI uri) {
    return normalComponent(uri.getHost());
  }

  /**
   * Gets the host of a URL.
   *
   * @return (lower-case) Empty, if no host is specified.
   */
  public static String host(URL url) {
    return normalComponent(url.getHost());
  }

  /**
   * Gets whether a URI belongs to the local machine.
   */
  public static boolean isLocal(final URI uri) {
    URI realUri = flatten(uri);

    InetAddress[] addresses;
    try {
      addresses = InetAddress.getAllByName(realUri.getHost());
    } catch (UnknownHostException ex) {
      throw runtime("Host resolution of {} FAILED", realUri, ex);
    }
    for (var address : addresses) {
      if (!Inets.isLocal(address))
        return false;
    }
    return addresses.length > 0;
  }

  /**
   * Gets whether a URI belongs to the local filesystem.
   * <p>
   * The local filesystem comprises resources accessed in the local machine directly (via
   * {@value #SCHEME__FILE} scheme) or through the classpath (via {@value #SCHEME__CLASSPATH}
   * scheme); <span class="important">in the latter case, it is caller's responsibility to verify
   * the actual {@link ClassLoader} the path is resolved against</span>.
   * </p>
   */
  public static boolean isLocalFileSystem(final URI uri) {
    URI realUri = flatten(uri);
    return switch (scheme(realUri)) {
      case //
          SCHEME__CLASSPATH, //
          SCHEME__FILE -> isLocal(realUri);
      default -> false;
    };
  }

  /**
   * Gets whether the port number of the URI is the default of its scheme.
   *
   * @throws RuntimeException
   *           if {@code uri} is malformed.
   */
  public static boolean isPortDefault(URI uri) {
    try {
      return uri.getPort() < 0 || uri.getPort() == uri.toURL().getDefaultPort();
    } catch (MalformedURLException ex) {
      throw runtime(ex);
    }
  }

  /**
   * Gets whether the URI belongs to a {@linkplain Inets#isPrivate(InetAddress) private} address.
   *
   * @throws RuntimeException
   *           if DNS resolution failed.
   */
  public static boolean isPrivate(final URI uri) {
    URI realUri = flatten(uri);
    try {
      return Inets.isPrivate(InetAddress.getByName(realUri.getHost()));
    } catch (UnknownHostException ex) {
      throw runtime("Host resolution of {} FAILED", realUri, ex);
    }
  }

  /**
   * Normalizes a URI, converting to lower case its case-insensitive components.
   *
   * @return Normalized {@code uri}.
   */
  public static URI normalize(URI uri) {
    uri = uri.normalize();
    String scheme = toOrNull(uri.getScheme(), Strings::lcase);
    String host = toOrNull(uri.getHost(), Strings::lcase);
    try {
      return Objects.equals(scheme, uri.getScheme()) && Objects.equals(host, uri.getHost()) ? uri
          : new URI(scheme, uri.getUserInfo(), host, uri.getPort(), uri.getPath(), uri.getQuery(),
              uri.getFragment());
    } catch (URISyntaxException ex) {
      throw runtime(ex);
    }
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
    // Not hierarchical?
    if (from.isOpaque() || to.isOpaque())
      return to;

    // Normalize schemes, hosts and paths!
    from = normalize(from);
    to = normalize(to);

    // Not the same resource context?
    if (!Objects.equals(from.getScheme(), to.getScheme())
        || !Objects.equals(from.getAuthority(), to.getAuthority()))
      return to;

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
   * Gets the scheme of a URI.
   * <p>
   * No syntactic check is applied to {@code uri}.
   * </p>
   *
   * @return (lower-case) Empty, if no scheme is specified.
   */
  public static String scheme(String uri) {
    return lcase(uri.substring(0, indexOfElse(uri, COLON, 0)));
  }

  /**
   * Gets the scheme of a URI.
   *
   * @return (lower-case) Empty, if no scheme is specified.
   * @implNote Contrary to {@link URI#getScheme()}, this method returns empty on undefined scheme to
   *           ease handling.
   */
  public static String scheme(URI uri) {
    return normalComponent(uri.getScheme());
  }

  /**
   * Gets the scheme of a URL.
   *
   * @return (lower-case) Empty, if no scheme is specified.
   */
  public static String scheme(URL url) {
    return normalComponent(url.getProtocol());
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

  private static String normalComponent(@Nullable String value) {
    return value != null ? lcase(value) : EMPTY;
  }

  private Uris() {
  }
}
