/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (RelativeMap.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util;

import static org.pdfclown.common.build.internal.util_.Objects.sqn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map whose matches are dynamically expanded based on key correlations.
 * <p>
 * Implicit matches are discovered looking for keys related to missing ones, provided by
 * {@link #relatedKeysProvider}; once a related key is found, the missing key is mapped to its
 * value, ensuring a match on next request.
 * </p>
 * <p>
 * Useful, eg, in case of maps keyed hierarchically, like {@link Class}: adding an entry for a
 * certain class, all its subclasses will match the same entry value — an ordinary map would match
 * only the class explicitly associated to the entry.
 * </p>
 *
 * @author Stefano Chizzolini
 * @implSpec Implementers should keep {@linkplain #putRelated(Object, Object, Object) implicit,
 *           automatically-derived mappings} distinct from {@linkplain #put(Object, Object)
 *           explicit, user-defined ones} — this is useful for tracing entries back to their
 *           respective root assignments.
 */
public class RelativeMap<K, V> extends HashMap<K, V> {
  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(RelativeMap.class);

  /**
   * Provides a sequence of keys related to the given one.
   */
  protected @Nullable Function<K, Iterator<K>> relatedKeysProvider;

  public RelativeMap() {
  }

  public RelativeMap(@Nullable Function<K, Iterator<K>> relatedKeysProvider) {
    this.relatedKeysProvider = relatedKeysProvider;
  }

  /**
   * @param relatedKeysProvider
   *          Related-keys provider (given a key, provides a sequence of alternatives to find a
   *          match).
   * @param m
   *          Map whose mappings are to be copied to this map.
   */
  public RelativeMap(@Nullable Function<K, Iterator<K>> relatedKeysProvider,
      Map<? extends K, ? extends V> m) {
    this(relatedKeysProvider);

    putAll(m);
  }

  /**
   * {@inheritDoc}
   * <p>
   * If a perfect/explicit (primary) match is missing, a related/implicit (secondary) match is
   * searched traversing the related keys.
   * </p>
   */
  @Override
  public @Nullable V get(@Nullable Object key) {
    // Query explicit mapping!
    var ret = super.get(key);
    if (ret == null && key != null) {
      if (relatedKeysProvider == null)
        throw new IllegalStateException("`relatedKeysProvider` undefined");

      @SuppressWarnings("unchecked")
      final var k = (K) key;
      Iterator<K> relatedKeysItr = relatedKeysProvider.apply(k);

      if (log.isDebugEnabled()) {
        log.debug("Related key SEARCH for {}", sqn(k));
      }

      var inheritKeys = new ArrayList<K>();
      while (relatedKeysItr.hasNext()) {
        var relatedKey = relatedKeysItr.next();

        if (log.isDebugEnabled()) {
          log.debug("Related key: {}", sqn(relatedKey));
        }

        // Query implicit mapping!
        ret = super.get(relatedKey);
        if (ret != null) {
          if (log.isDebugEnabled()) {
            log.debug("Related key MATCH for {}: {}", sqn(k), sqn(relatedKey));
          }

          // Make explicit the successful implicit mapping!
          putRelated(relatedKey, k, ret);

          // Apply the implicit mapping to all the higher-priority related keys!
          for (var inheritKey : inheritKeys) {
            putRelated(relatedKey, inheritKey, ret);
          }
          break;
        } else {
          inheritKeys.add(relatedKey);
        }
      }
    }
    return ret;
  }

  @Override
  public final V getOrDefault(@Nullable Object key, V defaultValue) {
    var ret = get(key);
    return ret != null ? ret : defaultValue;
  }

  @Override
  public final void putAll(Map<? extends K, ? extends V> m) {
    for (var entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Associates a key to a value obtained from a related mapping.
   *
   * @param relatedKey
   *          Key whose mapping is reused by {@code key}.
   * @param key
   *          New entry key.
   * @param value
   *          New entry value (obtained from {@code relatedKey}).
   * @implNote This method purposely delegates to the original (super) implementation of
   *           {@link #put(Object, Object)} in order to keep explicit, user-added mappings distinct
   *           from implicit, automatically derived ones.
   */
  protected void putRelated(K relatedKey, K key, V value) {
    super.put(key, value);
  }
}
