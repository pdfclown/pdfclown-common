/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Inets.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.net;

import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Objects.type;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

/**
 * Internet Protocol-related utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Inets {
  /**
   * Gets whether an address belongs to the local machine.
   */
  public static boolean isLocal(InetAddress address) {
    // Loop back?
    if (address.isLoopbackAddress())
      return true;

    // Any of this machine's own network interfaces?
    try {
      return NetworkInterface.getByInetAddress(address) != null;
    } catch (SocketException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Gets whether an address belongs to a private range.
   * <p>
   * This method remedies the flawed (Java 17) {@link Inet6Address#isSiteLocalAddress()}
   * implementation, still misleadingly sticking to the obsolete site-local block
   * ({@code fec0::/10}) which is effectively public (see also IPv6
   * <a href="https://en.wikipedia.org/wiki/Unique_local_address">Unique Local Address (ULA)</a>).
   * </p>
   * <p>
   * IPv4 <a href="https://en.wikipedia.org/wiki/Carrier-grade_NAT">Carrier-grade NAT</a> (CGN)
   * address block ({@code 100.64/10}) is excluded.
   * </p>
   */
  public static boolean isPrivate(InetAddress address) {
    if (address.isAnyLocalAddress())
      return false;
    else if (address.isLoopbackAddress() /* 127/8 (IPv4); ::1 (IPv6) */
        || address.isLinkLocalAddress() /* 169.254/16 (IPv4); fe80::/10 (IPv6) */)
      return true;
    else if (address instanceof Inet6Address)
      return isInet6UniqueLocal(address) /*
                                          * fc00::/7 -- WARNING: DO NOT use
                                          * `Inet6Address.isSiteLocalAddress`, it does NOT cover
                                          * this and is, on the contrary, dangerously flawed
                                          */;
    else if (address instanceof Inet4Address)
      return address.isSiteLocalAddress() /* 10/8, 172.16/12, 192.168/16 */;
    else
      throw unexpected("address", type(address));
  }

  /**
   * Gets whether an address belongs to the
   * <a href="https://en.wikipedia.org/wiki/Unique_local_address">Unique Local Address (ULA)</a>
   * range.
   * <p>
   * This range ({@code fc00::/7}) superseded the old site-local addresses block ({@code fec0::/10})
   * which in turn was assigned back to regular global unicast; unfortunately, the current (Java 17)
   * {@link Inet6Address#isSiteLocalAddress()} is dangerously misleading, as it still sticks to the
   * obsolete (now public!) block: this method remedies such flaw.
   * </p>
   */
  private static boolean isInet6UniqueLocal(InetAddress address) {
    byte[] b = address.getAddress();
    if (b.length != 16)
      return false;

    return (b[0] & 0xFE) == 0xFC /* fc00::/7 */;
  }

  private Inets() {
  }
}
