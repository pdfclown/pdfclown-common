/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DynamicMap.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.collect;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Objects.sqn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.XtCloneable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map whose entries are dynamically expanded based on key correlations.
 * <p>
 * Implicit entries are discovered looking for keys related to missing ones, provided by
 * {@link #getRelatedKeysProvider() relatedKeysProvider}; once a related key is found among existing
 * entries, the missing key is mapped to its value, ensuring a match on next
 * {@linkplain #get(Object) requests}.
 * </p>
 * <p>
 * Keys {@linkplain #put(Object, Object) explicitly defined} by users are <b>root keys</b>, whilst
 * <b>dynamic keys</b> are associated to the respective root keys through a chain of parent keys.
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
 * @implSpec Implementers MUST keep {@linkplain #putDynamic(Object, Object, Object) dynamic entries}
 *           apart from {@linkplain #put(Object, Object) user-defined ones}, in order to trace them
 *           back to their respective {@linkplain #getRootKey(Object) roots}.
 */
@SuppressWarnings({ "serial" /* serialization is currently not a concern */, "unchecked" })
public class DynamicMap<K, V> extends HashMap<K, V> {
  /**
   * Provides the elements related to the given one.
   *
   * @param <E>
   *          Element type.
   * @author Stefano Chizzolini
   */
  public abstract static class DynamicProvider<E>
      implements Function<E, Stream<E>>, XtCloneable {
    @Override
    public DynamicProvider<E> clone() {
      try {
        return (DynamicProvider<E>) super.clone();
      } catch (CloneNotSupportedException ex) {
        throw runtime(ex);
      }
    }
  }

  private static final Logger log = LoggerFactory.getLogger(DynamicMap.class);

  private HashMap<K, K> parentKeys = new HashMap<>();
  private DynamicProvider<K> relatedKeysProvider;

  public DynamicMap(DynamicProvider<K> relatedKeysProvider) {
    this.relatedKeysProvider = requireNonNull(relatedKeysProvider, "`relatedKeysProvider`");
  }

  @Override
  public DynamicMap<K, V> clone() {
    var ret = (DynamicMap<K, V>) super.clone();
    {
      ret.relatedKeysProvider = ret.relatedKeysProvider.clone();
      ret.parentKeys = new HashMap<>(ret.parentKeys);
    }
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
    V ret = super.get(key);
    if (ret == null && key != null) {
      final var k = (K) key;

      if (log.isDebugEnabled()) {
        log.debug("Related key SEARCH for {}", sqn(k));
      }

      // Looking for implicit mapping...
      Iterator<K> relatedKeysItr = relatedKeysProvider.apply(k).iterator();
      while (relatedKeysItr.hasNext()) {
        var relatedKey = relatedKeysItr.next();

        if (log.isDebugEnabled()) {
          log.debug("Related key: {}", sqn(relatedKey));
        }

        // Implicit mapping found?
        if ((ret = super.get(relatedKey)) != null) {
          if (log.isDebugEnabled()) {
            log.debug("Related key MATCH for {}: {}", sqn(k), sqn(relatedKey));
          }

          // Make explicit the existing implicit mapping!
          putDynamic(k, ret, relatedKey);
          break;
        }
      }
    }
    return ret;
  }

  @Override
  public final V getOrDefault(@Nullable Object key, V defaultValue) {
    V ret = get(key);
    return ret != null ? ret : defaultValue;
  }

  /**
   * Gets the key the given one was dynamically derived from.
   *
   * @return {@code null}, if {@code key} is root or missing.
   */
  public @Nullable K getParentKey(K key) {
    return parentKeys.get(key);
  }

  /**
   * Gets the root key associated to the given key.
   *
   * @return
   *         <ul>
   *         <li>distinct key, if {@code key} is derived (dynamic mapping)</li>
   *         <li>{@code key}, if it is user-defined (explicit mapping)</li>
   *         <li>{@code null}, if no mapping, neither explicit nor dynamic, exists, even if
   *         {@code key} can potentially be derived from an existing root (only a call to
   *         {@link #get(Object)} triggers its mapping).</li>
   *         </ul>
   */
  public @Nullable K getRootKey(K key) {
    var parent = parentKeys.get(key);
    if (parent == null)
      // `key` is root itself?
      return super.containsKey(key) ? key : null;

    var ret = parent;
    while (true) {
      parent = parentKeys.get(ret);
      if (parent == null)
        return ret;

      ret = parent;
    }
  }

  @Override
  public final void putAll(Map<? extends K, ? extends V> m) {
    for (var entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Root keys.
   */
  public Set<K> rootKeySet() {
    var ret = new HashSet<>(keySet());
    ret.removeAll(parentKeys.keySet());
    return ret;
  }

  /**
   * Provides a sequence of keys related to the given one.
   */
  protected DynamicProvider<K> getRelatedKeysProvider() {
    return relatedKeysProvider;
  }

  /**
   * Associates a key to a value obtained from a related mapping.
   *
   * @param key
   *          New entry key.
   * @param value
   *          New entry value, obtained from {@code parentKey}.
   * @param parentKey
   *          Key whose mapping is reused by {@code key}.
   * @implNote This method purposely delegates to the super implementation of
   *           {@link #put(Object, Object)} in order to keep user-defined mappings apart from
   *           dynamically derived ones.
   */
  protected void putDynamic(K key, V value, K parentKey) {
    parentKeys.put(key, parentKey);

    super.put(key, value);

    if (log.isDebugEnabled()) {
      log.debug("putDynamic: {} from {}", sqn(key), sqn(parentKey));
    }
  }
}
