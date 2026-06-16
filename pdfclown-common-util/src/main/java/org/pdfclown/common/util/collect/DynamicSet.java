/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DynamicSet.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.collect;

import static org.pdfclown.common.util.Exceptions.runtime;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.XtCloneable;
import org.pdfclown.common.util.collect.DynamicMap.DynamicProvider;

/**
 * Set whose elements are dynamically expanded based on element correlations.
 * <p>
 * Implicit elements are discovered looking for elements related to missing ones, provided by
 * {@link #getRelatedElementsProvider() relatedElementsProvider}; once a related element is found,
 * the missing element is added, ensuring a match on next {@linkplain #contains(Object) requests}.
 * </p>
 * <p>
 * Elements {@linkplain #add(Object) explicitly defined} by users are <b>root elements</b>, whilst
 * <b>dynamic elements</b> are associated to the respective roots through a chain of parents.
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
public class DynamicSet<E> extends AbstractSet<E>
    implements Set<E>, XtCloneable {
  /**
   * Dummy value to associate in {@link #base} with an element of this set.
   */
  private static final Object VALUE = new Object();

  private DynamicMap<E, Object> base;

  public DynamicSet(DynamicMap<E, Object> base) {
    this.base = base;
  }

  public DynamicSet(DynamicProvider<E> relatedElementsProvider) {
    this(new DynamicMap<>(relatedElementsProvider));
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
  public DynamicSet<E> clone() {
    try {
      @SuppressWarnings("unchecked")
      var ret = (DynamicSet<E>) super.clone();
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

  /**
   * Gets the element the given one was dynamically derived from.
   *
   * @return {@code null}, if {@code e} is root or missing.
   */
  public @Nullable E getParent(E e) {
    return base.getParentKey(e);
  }

  /**
   * Gets the root element associated to the given element.
   *
   * @return
   *         <ul>
   *         <li>distinct element, if {@code e} is derived (dynamic mapping)</li>
   *         <li>{@code e}, if it is user-defined (explicit mapping)</li>
   *         <li>{@code null}, if no mapping, neither explicit nor dynamic, exists, even if
   *         {@code e} can potentially be derived from an existing root (only a call to
   *         {@link #contains(Object)} triggers its mapping).</li>
   *         </ul>
   */
  public @Nullable E getRoot(E e) {
    return base.getRootKey(e);
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
  @SuppressWarnings("ReferenceEquality")
  public boolean remove(Object o) {
    return base.remove(o) == VALUE;
  }

  /**
   * Root elements.
   */
  public Set<E> rootSet() {
    return base.rootKeySet();
  }

  @Override
  public int size() {
    return base.size();
  }

  /**
   * Provides a sequence of elements related to the given one.
   */
  protected DynamicProvider<E> getRelatedElementsProvider() {
    return base.getRelatedKeysProvider();
  }
}
