/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (RelatedMap.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Objects.sqn;

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
 * {@link #getRelatedKeysProvider() relatedKeysProvider}; once a related key is found, the missing
 * key is mapped to its value, ensuring a match on next requests.
 * </p>
 * <p>
 * Useful, for example, in case of maps keyed hierarchically, like {@link Class}: adding an entry
 * for a certain class, all its subclasses will match the same entry value — an ordinary map would
 * match only the class explicitly associated to the entry.
 * </p>
 *
 * @param <K>
 *          Key type.
 * @param <V>
 *          Value type.
 * @author Stefano Chizzolini
 * @implSpec Implementers should keep {@linkplain #putRelated(Object, Object, Object) implicit,
 *           automatically-derived mappings} distinct from {@linkplain #put(Object, Object)
 *           explicit, user-defined ones} — this is useful for tracing entries back to their
 *           respective root assignments.
 */
public class RelatedMap<K, V> extends HashMap<K, V> {
  /**
   * Provides the elements related to the given one.
   *
   * @param <E>
   *          Element type.
   * @author Stefano Chizzolini
   */
  public abstract static class RelatedProvider<E>
      implements Function<E, Iterable<E>>, Cloneable {
    @Override
    public RelatedProvider<E> clone() {
      try {
        //noinspection unchecked
        return (RelatedProvider<E>) super.clone();
      } catch (CloneNotSupportedException ex) {
        throw runtime(ex);
      }
    }
  }

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(RelatedMap.class);

  private RelatedProvider<K> relatedKeysProvider;

  public RelatedMap(RelatedProvider<K> relatedKeysProvider) {
    this.relatedKeysProvider = requireNonNull(relatedKeysProvider, "`relatedKeysProvider`");
  }

  @Override
  public RelatedMap<K, V> clone() {
    @SuppressWarnings("unchecked")
    var ret = (RelatedMap<K, V>) super.clone();
    ret.relatedKeysProvider = ret.relatedKeysProvider.clone();
    return ret;
  }

  @Override
  public boolean containsKey(Object key) {
    return get(key) != null;
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
      @SuppressWarnings("unchecked")
      final var k = (K) key;
      Iterator<K> relatedKeysItr = relatedKeysProvider.apply(k).iterator();

      if (log.isDebugEnabled()) {
        log.debug("Related key SEARCH for {}", sqn(k));
      }

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
          break;
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
   * Provides a sequence of keys related to the given one.
   */
  protected RelatedProvider<K> getRelatedKeysProvider() {
    return relatedKeysProvider;
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
