/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (RelatedSet.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_;

import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import org.pdfclown.common.build.internal.util_.RelatedMap.RelatedProvider;

/**
 * Set whose matches are dynamically expanded based on element correlations.
 * <p>
 * Implicit matches are discovered looking for elements related to missing ones, provided by
 * {@link #getRelatedProvider() relatedProvider}; once a related element is found, the missing
 * element is mapped to its value, ensuring a match on next requests.
 * </p>
 * <p>
 * Useful, for example, in case of hierarchical sets, like {@link Class}: adding a certain class,
 * all its subclasses will be matched — an ordinary set would match only the class explicitly added
 * to the set.
 * </p>
 *
 * @param <E>
 *          Element type.
 * @author Stefano Chizzolini
 */
public class RelatedSet<E> extends AbstractSet<E>
    implements Set<E>, Cloneable {
  /**
   * Dummy value to associate in {@link #base} with an element of this set.
   */
  private static final Object VALUE = new Object();

  private RelatedMap<E, Object> base;

  public RelatedSet(RelatedMap<E, Object> base) {
    this.base = base;
  }

  public RelatedSet(RelatedProvider<E> relatedProvider) {
    this(new RelatedMap<>(relatedProvider));
  }

  @Override
  public boolean add(E e) {
    return base.put(e, VALUE) == null;
  }

  @Override
  public void clear() {
    base.clear();
  }

  @Override
  public RelatedSet<E> clone() {
    try {
      @SuppressWarnings("unchecked")
      var ret = (RelatedSet<E>) super.clone();
      ret.base = base.clone();
      return ret;
    } catch (CloneNotSupportedException ex) {
      throw runtime(ex);
    }
  }

  @Override
  public boolean contains(Object o) {
    return base.containsKey(o);
  }

  @Override
  public boolean isEmpty() {
    return base.isEmpty();
  }

  @Override
  public Iterator<E> iterator() {
    return base.keySet().iterator();
  }

  @Override
  public boolean remove(Object o) {
    return base.remove(o) == VALUE;
  }

  @Override
  public int size() {
    return base.size();
  }

  /**
   * Provides a sequence of elements related to the given one.
   */
  protected RelatedProvider<E> getRelatedProvider() {
    return base.getRelatedKeysProvider();
  }
}
