/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ObjectsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.List.of;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.pdfclown.common.build.test.assertion.Verifiers.COMBINATION;
import static org.pdfclown.common.build.test.assertion.Verifiers.TUPLE;
import static org.pdfclown.common.build.util.Tuple.tuple;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.collect.Aggregations.map;
import static org.pdfclown.common.util.collect.Comparators.hierarchicalType;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;
import org.pdfclown.common.util.collect.Comparators.HierarchicalTypeComparator;
import org.pdfclown.common.util.collect.XtList;
import org.pdfclown.common.util.function.FunctionsTest;
import org.pdfclown.common.util.system.Clis;
import org.pdfclown.common.util.xml.Xmls;
import org.pdfclown.common.util.xml.Xmls.DocumentFactoryProfile;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings("Convert2MethodRef")
class ObjectsTest extends BaseTest {
  /**
   * Simulates an object with arbitrary {@link #toString()}.
   */
  static class ToStringObject {
    final String toString;

    public ToStringObject(String toString) {
      this.toString = toString;
    }

    @Override
    public String toString() {
      return toString;
    }
  }

  /**
   * Simulating {@code java.util.Collections.UnmodifiableCollection}.
   */
  @SuppressWarnings({ "NullableProblems", "DataFlowIssue" })
  static class UnmodifiableCollection<E> implements Collection<E>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public boolean add(E e) {
      return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
      return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean contains(Object o) {
      return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      return false;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public Iterator<E> iterator() {
      return null;
    }

    @Override
    public boolean remove(Object o) {
      return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      return false;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public Object[] toArray() {
      return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
      return null;
    }
  }

  /**
   * Simulating {@code java.util.Collections.UnmodifiableList}.
   */
  @SuppressWarnings({ "NullableProblems", "DataFlowIssue" })
  static class UnmodifiableList<E> extends UnmodifiableCollection<E> implements List<E> {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public void add(int index, E element) {
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
      return false;
    }

    @Override
    public E get(int index) {
      return null;
    }

    @Override
    public int indexOf(Object o) {
      return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
      return 0;
    }

    @Override
    public ListIterator<E> listIterator() {
      return null;
    }

    @Override
    public ListIterator<E> listIterator(int index) {
      return null;
    }

    @Override
    public E remove(int index) {
      return null;
    }

    @Override
    public E set(int index, E element) {
      return null;
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
      return of();
    }
  }

  private static final List<String> TO_STRINGS = asList(
      null,
      // Simple type name (Class.getSimpleName()).
      "ToStringObject",
      // Simple type name (Class.getSimpleName()), with attributes.
      "ToStringObject myprop=something",
      // SQN.
      "ObjectsTest$ToStringObject",
      // SQN, with attributes.
      "ObjectsTest$ToStringObject myprop: something",
      // SQND, with attributes.
      "ObjectsTest.ToStringObject myprop: something else",
      // FQN (Class.getName()).
      "org.pdfclown.common.util.ObjectsTest$ToStringObject",
      // FQND, with attributes.
      "org.pdfclown.common.util.ObjectsTest.ToStringObject something",
      // Wrong simple type name.
      "ToStringObjects",
      // Wrong FQN.
      "org.something.ToStringObject",
      // Simple type name, with attributes.
      "ToStringObject[myprop=AAA]",
      // Simple type name nested in an attribute.
      "myprop=List<ToStringObject>",
      // Simple type name nested in an attribute.
      "myprop=ToStringObject");

  private static final List<@Nullable Object> QN_OBJS = asList(
      null,
      .5,
      'a',
      "a string",
      String.class,
      Map.Entry.class);

  private static final List<@Nullable String> QN_TYPENAMES = unmodifiableList(
      (List<@Nullable String>) QN_OBJS.stream()
          .map($ -> $ != null ? ($ instanceof Class<?> c ? c : $.getClass()).getName() : null)
          .collect(Collectors.toCollection(ArrayList::new)));

  @Test
  void elseGet() {
    COMBINATION.verify(
        (obj, supplier) -> Objects.elseGet(obj, supplier),
        List.of("obj", "supplier"),
        // obj
        FunctionsTest.OBJS,
        // supplier
        FunctionsTest.SUPPLIERS);
  }

  @Test
  void fqn_Object() {
    COMBINATION.verify(
        (obj) -> Objects.fqn(obj),
        List.of("obj"),
        // obj
        QN_OBJS);
  }

  @Test
  void fqnd_Object() {
    COMBINATION.verify(
        (obj) -> Objects.fqnd(obj),
        List.of("obj"),
        // obj
        QN_OBJS);
  }

  @Test
  void fqnd_String() {
    COMBINATION.verify(
        (typename) -> Objects.fqnd(typename),
        List.of("typename"),
        // typename
        QN_TYPENAMES);
  }

  /**
   * @implNote TODO: Anytime the expected result of this test changes, it should be manually copied
   *           to {@link #parseLiteral()} as {@code s} argument list.
   */
  @Test
  void literal() {
    COMBINATION.verify(
        (obj) -> Objects.literal(obj),
        List.of("obj"),
        // obj
        asList(
            null,
            // integer
            1_234,
            // single-precision floating-point number
            1.987f,
            // double-precision floating-point number
            1.987,
            // double-precision floating-point number
            1.5e-4,
            // special single-precision floating-point number
            Float.NaN,
            Float.NEGATIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            // special double-precision floating-point number
            Double.NaN,
            Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            // boolean
            true,
            // char
            '\'',
            '"',
            // string
            EMPTY,
            // string with whitespace
            "Text with:\n- \"quoted content\"\n- newlines",
            // string with characters outside the BMP
            "测试文本",
            // class
            String.class,
            Stream.class,
            Strings.class,
            // generic object
            of("one", "two")));
  }

  @Test
  void parseLiteral() {
    COMBINATION.verify(
        (s) -> Objects.parseLiteral(s),
        List.of("s"),
        // s (see implementation note)
        asList(
            // [1] obj[0]: null
            "null",
            // [2] obj[1]: 1234
            "1234",
            // [3] obj[2]: 1.987
            "1.987",
            // [4] obj[3]: 1.5E-4
            "1.5E-4",
            // [5] obj[4]: true
            "true",
            // [6] obj[5]: '\''
            "'\\''",
            // [7] obj[6]: '"'
            "'\"'",
            // [8] obj[7]: ""
            "\"\"",
            // [9] obj[8]: "Text with:\n- \"quoted content\"\n- newlines"
            "\"Text with:\\n"
                + "- \\\"quoted content\\\"\\n"
                + "- newlines\"",
            // [10] obj[9]: "测试文本"
            "\"测试文本\"",
            // [11] obj[10]: String
            "String",
            // [12] obj[11]: java.util.stream.Stream
            "java.util.stream.Stream",
            // [13] obj[12]: org.pdfclown.common.util.Strings
            "org.pdfclown.common.util.Strings",
            // [14] obj[13]: "[one, two]"
            "\"[one, two]\""));
  }

  @Test
  void pkg_Object() {
    COMBINATION.verify(
        (obj) -> Objects.pkg(obj),
        List.of("obj"),
        // obj
        QN_OBJS);
  }

  @Test
  void pkg_String() {
    COMBINATION.verify(
        (typename) -> Objects.pkg(typename),
        List.of("typename"),
        // typename
        QN_TYPENAMES);
  }

  @Test
  void sfqn_Object() {
    COMBINATION.verify(
        (obj) -> Objects.sfqn(obj),
        List.of("obj"),
        // obj
        QN_OBJS);
  }

  @Test
  void sfqn_String() {
    COMBINATION.verify(
        (typename) -> Objects.sfqn(typename),
        List.of("typename"),
        // typename
        QN_TYPENAMES);
  }

  @Test
  void sfqnd_Object() {
    COMBINATION.verify(
        (obj) -> Objects.sfqnd(obj),
        List.of("obj"),
        // obj
        QN_OBJS);
  }

  @Test
  void sfqnd_String() {
    COMBINATION.verify(
        (typename) -> Objects.sfqnd(typename),
        List.of("typename"),
        // typename
        QN_TYPENAMES);
  }

  @Test
  void simpleName_Object() {
    COMBINATION.verify(
        (obj) -> Objects.simpleName(obj),
        List.of("obj"),
        // obj
        QN_OBJS);
  }

  @Test
  void simpleName_String() {
    COMBINATION.verify(
        (typename) -> Objects.simpleName(typename),
        List.of("typename"),
        // typename
        QN_TYPENAMES);
  }

  @Test
  void sqn_Object() {
    COMBINATION.verify(
        (obj) -> Objects.sqn(obj),
        List.of("obj"),
        // obj
        QN_OBJS);
  }

  @Test
  void sqn_String() {
    COMBINATION.verify(
        (typename) -> Objects.sqn(typename),
        List.of("typename"),
        // typename
        QN_TYPENAMES);
  }

  @Test
  void sqnd_Object() {
    COMBINATION.verify(
        (obj) -> Objects.sqnd(obj),
        List.of("obj"),
        // obj
        QN_OBJS);
  }

  @Test
  void sqnd_String() {
    COMBINATION.verify(
        (typename) -> Objects.sqnd(typename),
        List.of("typename"),
        // typename
        QN_TYPENAMES);
  }

  @Test
  @SuppressWarnings("rawtypes")
  void subTypes() {
    try (var types = Objects.types(Objects.class.getClassLoader())) {
      Stream<Class<? extends List>> subTypesStream = Objects.subTypes(List.class, types);
      List<Class<? extends List>> subTypes = subTypesStream.collect(toList());

      assertThat("SHOULD contain concrete classes", subTypes, hasItem(ArrayList.class));
      assertThat("SHOULD contain abstract classes", subTypes, hasItem(AbstractList.class));
      assertThat("SHOULD contain interfaces", subTypes, hasItem(XtList.class));
    }
  }

  @Test
  void superTypes() {
    var actual = Objects.superTypes(UnmodifiableList.class, hierarchicalType()
        .thenComparing(HierarchicalTypeComparator.Priorities.explicitPriority()
            .set(999, Serializable.class))
        .thenComparing(HierarchicalTypeComparator.Priorities.interfacePriority()
            .reversed()));

    /*
     * NOTE: `containsInRelativeOrder(..)` is needed as newer Java versions may introduce further
     * super types than Java 11.
     */
    assertThat(actual.toList(), containsInRelativeOrder(
        List.class,
        UnmodifiableCollection.class,
        Collection.class,
        Iterable.class,
        Serializable.class,
        Object.class));
  }

  @Test
  void toQualifiedString() {
    COMBINATION.verify(
        (obj) -> Objects.toQualifiedString(obj),
        List.of("obj"),
        // obj
        TO_STRINGS.stream().map($ -> $ != null ? new ToStringObject($) : null).toList());
  }

  @Test
  void toSqnQualifiedString() {
    COMBINATION.verify(
        (obj) -> Objects.toSqnQualifiedString(obj),
        List.of("obj"),
        // obj
        TO_STRINGS.stream().map($ -> $ != null ? new ToStringObject($) : null).toList());
  }

  @Test
  void toStringStable() {
    COMBINATION
        .verify(
            (obj) -> Objects.toStringStable(obj),
            List.of("obj"),
            // obj
            asList(
                // null object
                null,
                // single-precision floating-point number with limited decimal places
                -0.12345f,
                // double-precision floating-point number with limited decimal places
                -0.12345,
                // single-precision floating-point number with exceeding decimal places and rounding to floor
                -0.1234567891234f,
                // double-precision floating-point number with exceeding decimal places and rounding to floor
                -0.1234567891234,
                // single-precision floating-point number with exceeding decimal places and rounding to ceiling
                -0.1234567891567f,
                // double-precision floating-point number with exceeding decimal places and rounding to ceiling
                -0.1234567891567,
                // tiny single-precision floating-point number
                -0.0000123456789f,
                // tiny double-precision floating-point number
                -0.0000123456789,
                // single-precision floating-point number clipped to integer
                100.000000000001f,
                // double-precision floating-point number clipped to integer
                100.000000000001,
                // integer
                100,
                // array
                new double[] { 1234, -0.123456789, Double.NaN, Float.NEGATIVE_INFINITY },
                // collection
                List.of("yellow", "green", "blue", "red", "pink", "magenta"),
                // set
                Set.of("yellow", "green", "blue", "red", "pink", "magenta"),
                // map
                map()
                    .with("yellow", null)
                    .with("green", 1)
                    .with("blue", -0.123456789)
                    .with("black", Double.POSITIVE_INFINITY)
                    .with("white", Float.NaN)
                    .with("red", '2')
                    .with("pink", true)
                    .with("magenta", Set.of("yellow", "green", "blue", "red", "pink", "magenta")),
                // any other object (hexadecimal hash code suffix)
                new ToStringObject("MyObject@16fdec90")));
  }

  @Test
  void toStringWithProperties() {
    TUPLE.verify(
        (obj, properties) -> Objects.toStringWithProperties(obj, properties),
        List.of("obj", "properties"),
        List.of(
            tuple(Object.class, new Object[] { URI.create("https://www.example.io"), "Blue" }),
            tuple(Clis.Args.class, new Object[] { "adapter", Clis.ListIncrementalAdapter.class }),
            tuple(Xmls.XPath.class,
                new Object[] { "profile", DocumentFactoryProfile.COMPACT, "level", 11 })));
  }

  @Test
  void toStringWithValues() {
    TUPLE.verify(
        (obj, features) -> Objects.toStringWithValues(obj, features),
        List.of("obj", "features"),
        List.of(
            tuple(Object.class, new Object[] { URI.create("https://www.example.io"), "Blue" }),
            tuple(Clis.Args.class, new Object[] { Clis.ListIncrementalAdapter.class }),
            tuple(Xmls.XPath.class,
                new Object[] { true, "Yellow", DocumentFactoryProfile.COMPACT })));
  }
}
