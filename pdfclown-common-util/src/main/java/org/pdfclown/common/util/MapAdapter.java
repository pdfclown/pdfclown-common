/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (MapAdapter.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.unsupported;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Map adapter.
 * <p>
 * Allows type parameter restriction to subtypes of the base map counterparts. Useful whenever the
 * type parameters of the base map must be constrained to a subset of their original domain.
 * </p>
 *
 * @param <K>
 *          Key type.
 * @param <V>
 *          Value type.
 * @author Stefano Chizzolini
 */
public class MapAdapter<K, V> implements XtMap<K, V> {
  private final Map<? super K, ? super V> base;

  public MapAdapter(Map<? super K, ? super V> base) {
    this.base = requireNonNull(base);
  }

  @Override
  public void clear() {
    base.clear();
  }

  @Override
  public boolean containsKey(@Nullable Object key) {
    return base.containsKey(key);
  }

  @Override
  public boolean containsValue(@Nullable Object value) {
    return base.containsValue(value);
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return new Set<>() {
      @Override
      public boolean add(@Nullable Entry<K, V> e) {
        throw unsupported();
      }

      @Override
      public boolean addAll(Collection<? extends Entry<K, V>> c) {
        throw unsupported();
      }

      @Override
      public void clear() {
        MapAdapter.this.base.clear();
      }

      @Override
      public boolean contains(@Nullable Object o) {
        return MapAdapter.this.base.entrySet().contains(o);
      }

      @Override
      public boolean containsAll(Collection<?> c) {
        return MapAdapter.this.base.entrySet().containsAll(c);
      }

      @Override
      public boolean isEmpty() {
        return MapAdapter.this.isEmpty();
      }

      @Override
      public Iterator<Entry<K, V>> iterator() {
        return new Iterator<>() {
          final Iterator<?> base = MapAdapter.this.base.entrySet().iterator();

          @Override
          public boolean hasNext() {
            return base.hasNext();
          }

          @Override
          @SuppressWarnings({ "unchecked", "null" })
          public Entry<K, V> next() {
            var baseEntry = (Map.Entry<?, ?>) base.next();
            return new AbstractMap.SimpleImmutableEntry<>((K) baseEntry.getKey(),
                (V) baseEntry.getValue());
          }
        };
      }

      @Override
      public boolean remove(@Nullable Object o) {
        return MapAdapter.this.base.entrySet().remove(o);
      }

      @Override
      public boolean removeAll(Collection<?> c) {
        return MapAdapter.this.base.entrySet().removeAll(c);
      }

      @Override
      public boolean retainAll(Collection<?> c) {
        return MapAdapter.this.base.entrySet().retainAll(c);
      }

      @Override
      public int size() {
        return MapAdapter.this.size();
      }

      @Override
      public Object[] toArray() {
        return MapAdapter.this.base.entrySet().toArray();
      }

      @Override
      public <T> T[] toArray(T[] a) {
        return MapAdapter.this.base.entrySet().toArray(a);
      }
    };
  }

  @Override
  @SuppressWarnings("unchecked")
  public V get(@Nullable Object key) {
    return (V) base.get(key);
  }

  @Override
  public boolean isEmpty() {
    return base.isEmpty();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<K> keySet() {
    return new Set<>() {
      @Override
      public boolean add(K e) {
        throw unsupported();
      }

      @Override
      public boolean addAll(Collection<? extends K> c) {
        throw unsupported();
      }

      @Override
      public void clear() {
        MapAdapter.this.base.clear();
      }

      @Override
      public boolean contains(@Nullable Object o) {
        //noinspection RedundantCollectionOperation
        return MapAdapter.this.base.keySet().contains(o);
      }

      @Override
      public boolean containsAll(Collection<?> c) {
        return MapAdapter.this.base.keySet().containsAll(c);
      }

      @Override
      public boolean isEmpty() {
        return MapAdapter.this.isEmpty();
      }

      @Override
      public Iterator<K> iterator() {
        return new Iterator<>() {
          final Iterator<? super K> base = MapAdapter.this.base.keySet().iterator();

          @Override
          public boolean hasNext() {
            return base.hasNext();
          }

          @Override
          public K next() {
            return (K) base.next();
          }
        };
      }

      @Override
      public boolean remove(@Nullable Object o) {
        //noinspection RedundantCollectionOperation
        return MapAdapter.this.base.keySet().remove(o);
      }

      @Override
      public boolean removeAll(Collection<?> c) {
        return MapAdapter.this.base.keySet().removeAll(c);
      }

      @Override
      public boolean retainAll(Collection<?> c) {
        return MapAdapter.this.base.keySet().retainAll(c);
      }

      @Override
      public int size() {
        return MapAdapter.this.size();
      }

      @Override
      public Object[] toArray() {
        return MapAdapter.this.base.keySet().toArray();
      }

      @Override
      public <T> T[] toArray(T[] a) {
        return MapAdapter.this.base.keySet().toArray(a);
      }
    };
  }

  @Override
  @SuppressWarnings("unchecked")
  public V put(K key, V value) {
    return (V) base.put(key, value);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    base.putAll(m);
  }

  @Override
  @SuppressWarnings("unchecked")
  public V remove(@Nullable Object key) {
    return (V) base.remove(key);
  }

  @Override
  public int size() {
    return base.size();
  }

  @Override
  public Collection<V> values() {
    return new Collection<>() {
      @Override
      public boolean add(V e) {
        throw unsupported();
      }

      @Override
      public boolean addAll(Collection<? extends V> c) {
        throw unsupported();
      }

      @Override
      public void clear() {
        MapAdapter.this.base.clear();
      }

      @Override
      public boolean contains(@Nullable Object o) {
        return MapAdapter.this.containsValue(o);
      }

      @Override
      public boolean containsAll(Collection<?> c) {
        return MapAdapter.this.base.values().containsAll(c);
      }

      @Override
      public boolean isEmpty() {
        return MapAdapter.this.isEmpty();
      }

      @Override
      public Iterator<V> iterator() {
        return new Iterator<>() {
          final Iterator<? super V> base = MapAdapter.this.base.values().iterator();

          @Override
          public boolean hasNext() {
            return base.hasNext();
          }

          @Override
          @SuppressWarnings("unchecked")
          public V next() {
            return (V) base.next();
          }
        };
      }

      @Override
      public boolean remove(@Nullable Object o) {
        return MapAdapter.this.base.values().remove(o);
      }

      @Override
      public boolean removeAll(Collection<?> c) {
        return MapAdapter.this.base.values().removeAll(c);
      }

      @Override
      public boolean retainAll(Collection<?> c) {
        return MapAdapter.this.base.values().retainAll(c);
      }

      @Override
      public int size() {
        return MapAdapter.this.size();
      }

      @Override
      public Object[] toArray() {
        return MapAdapter.this.base.values().toArray();
      }

      @Override
      public <T> T[] toArray(T[] a) {
        return MapAdapter.this.base.values().toArray(a);
      }
    };
  }
}
