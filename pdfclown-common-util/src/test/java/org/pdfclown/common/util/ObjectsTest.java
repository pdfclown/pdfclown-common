/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.util.Aggregations.entry;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
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
  static class UnmodifiableCollection<E> implements Collection<E>, Serializable {
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
  static class UnmodifiableList<E> extends UnmodifiableCollection<E> implements List<E> {
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

  /**
   * @implNote Generated as {@code expected} source code within {@link #toLiteralString ()}.
   */
  private static final List<String> LITERAL_STRINGS = asList(
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
      "\"Text with:\\n- \\\"quoted content\\\"\\n- newlines\"",
      // [10] obj[9]: "测试文本"
      "\"测试文本\"",
      // [11] obj[10]: String
      "String",
      // [12] obj[11]: java.util.stream.Stream
      "java.util.stream.Stream",
      // [13] obj[12]: org.pdfclown.common.util.Strings
      "org.pdfclown.common.util.Strings",
      // [14] obj[13]: [one, two]
      "[one, two]");

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
      "ToStringObject{myprop:AAA}",
      // Simple type name nested in an attribute.
      "myprop:List<ToStringObject>",
      // Simple type name nested in an attribute.
      "myprop:ToStringObject");

  private static final List<@Nullable Object> QN_OBJS = asList(
      null,
      .5,
      'a',
      "a string",
      String.class,
      Map.Entry.class);

  private static final List<@Nullable String> QN_TYPENAMES = unmodifiableList(
      (List<@Nullable String>) QN_OBJS.stream()
          .map($ -> $ != null ? ($ instanceof Class ? (Class<?>) $ : $.getClass()).getName() : null)
          .collect(Collectors.toCollection(ArrayList::new)));

  static Stream<Arguments> fqn_Object() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] obj[0]: null
            "null",
            // [2] obj[1]: 0.5
            "java.lang.Double",
            // [3] obj[2]: 'a'
            "java.lang.Character",
            // [4] obj[3]: "a string"
            "java.lang.String",
            // [5] obj[4]: String
            "java.lang.String",
            // [6] obj[5]: java.util.Map$Entry
            "java.util.Map$Entry"),
        // obj
        QN_OBJS);
  }

  static Stream<Arguments> fqnd_Object() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] obj[0]: null
            "null",
            // [2] obj[1]: 0.5
            "java.lang.Double",
            // [3] obj[2]: 'a'
            "java.lang.Character",
            // [4] obj[3]: "a string"
            "java.lang.String",
            // [5] obj[4]: String
            "java.lang.String",
            // [6] obj[5]: java.util.Map$Entry
            "java.util.Map.Entry"),
        // obj
        QN_OBJS);
  }

  static Stream<Arguments> fqnd_String() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] typename[0]: null
            "null",
            // [2] typename[1]: "java.lang.Double"
            "java.lang.Double",
            // [3] typename[2]: "java.lang.Character"
            "java.lang.Character",
            // [4] typename[3]: "java.lang.String"
            "java.lang.String",
            // [5] typename[4]: "java.lang.String"
            "java.lang.String",
            // [6] typename[5]: "java.util.Map$Entry"
            "java.util.Map.Entry"),
        // obj
        QN_TYPENAMES);
  }

  static Stream<Arguments> fromLiteralString() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] s[0]: "null"
            null,
            // [2] s[1]: "1234"
            1234,
            // [3] s[2]: "1.987"
            1.987F,
            // [4] s[3]: "1.5E-4"
            1.5E-4F,
            // [5] s[4]: "true"
            true,
            // [6] s[5]: "'\\''"
            '\'',
            // [7] s[6]: "'\"'"
            '"',
            // [8] s[7]: "\"\""
            "",
            // [9] s[8]: "\"Text with:\\n- \\\"quoted content\\\"\\n- . . ."
            "Text with:\n- \"quoted content\"\n- newlines",
            // [10] s[9]: "\"测试文本\""
            "测试文本",
            // [11] s[10]: "String"
            "String",
            // [12] s[11]: "java.util.stream.Stream"
            "java.util.stream.Stream",
            // [13] s[12]: "org.pdfclown.common.util.Strings"
            "org.pdfclown.common.util.Strings",
            // [14] s[13]: "[one, two]"
            "[one, two]"),
        // s
        LITERAL_STRINGS);
  }

  static Stream<Arguments> sqn_Object() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] obj[0]: null
            "null",
            // [2] obj[1]: 0.5
            "Double",
            // [3] obj[2]: 'a'
            "Character",
            // [4] obj[3]: "a string"
            "String",
            // [5] obj[4]: String
            "String",
            // [6] obj[5]: java.util.Map$Entry
            "Map$Entry"),
        // obj
        QN_OBJS);
  }

  static Stream<Arguments> sqn_String() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] typename[0]: null
            "null",
            // [2] typename[1]: "java.lang.Double"
            "Double",
            // [3] typename[2]: "java.lang.Character"
            "Character",
            // [4] typename[3]: "java.lang.String"
            "String",
            // [5] typename[4]: "java.lang.String"
            "String",
            // [6] typename[5]: "java.util.Map$Entry"
            "Map$Entry"),
        // obj
        QN_TYPENAMES);
  }

  static Stream<Arguments> sqnd_Object() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] obj[0]: null
            "null",
            // [2] obj[1]: 0.5
            "Double",
            // [3] obj[2]: 'a'
            "Character",
            // [4] obj[3]: "a string"
            "String",
            // [5] obj[4]: String
            "String",
            // [6] obj[5]: java.util.Map$Entry
            "Map.Entry"),
        // obj
        QN_OBJS);
  }

  static Stream<Arguments> sqnd_String() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] typename[0]: null
            "null",
            // [2] typename[1]: "java.lang.Double"
            "Double",
            // [3] typename[2]: "java.lang.Character"
            "Character",
            // [4] typename[3]: "java.lang.String"
            "String",
            // [5] typename[4]: "java.lang.String"
            "String",
            // [6] typename[5]: "java.util.Map$Entry"
            "Map.Entry"),
        // obj
        QN_TYPENAMES);
  }

  static Stream<Arguments> toLiteralString() {
    return argumentsStream(
        cartesian(),
        // expected
        LITERAL_STRINGS,
        // obj
        asList(
            null,
            1_234,
            1.987,
            1.5e-4,
            true,
            '\'',
            '"',
            EMPTY,
            "Text with:\n- \"quoted content\"\n- newlines",
            "测试文本",
            String.class,
            Stream.class,
            Strings.class,
            of("one", "two")));
  }

  static Stream<Arguments> toQualifiedString() {
    return argumentsStream(
        cartesian()
            .<String>composeArgConverter(0, $ -> $ != null ? new ToStringObject($) : null),
        // expected
        asList(
            // [1] obj[0]: null
            "null",
            // [2] obj[1]: "ToStringObject"
            "ToStringObject",
            // [3] obj[2]: "ToStringObject myprop=something"
            "ToStringObject myprop=something",
            // [4] obj[3]: "ObjectsTest$ToStringObject"
            "ObjectsTest$ToStringObject",
            // [5] obj[4]: "ObjectsTest$ToStringObject myprop: something"
            "ObjectsTest$ToStringObject myprop: something",
            // [6] obj[5]: "ObjectsTest.ToStringObject myprop: something else"
            "ObjectsTest.ToStringObject myprop: something else",
            // [7] obj[6]: "org.pdfclown.common.util.ObjectsTest$ToStringObject"
            "org.pdfclown.common.util.ObjectsTest$ToStringObject",
            // [8] obj[7]: "org.pdfclown.common.util.ObjectsTest.ToStringObject something"
            "org.pdfclown.common.util.ObjectsTest.ToStringObject something",
            // [9] obj[8]: "ToStringObjects"
            "ObjectsTest.ToStringObject {ToStringObjects}",
            // [10] obj[9]: "org.something.ToStringObject"
            "ObjectsTest.ToStringObject {org.something.ToStringObject}",
            // [11] obj[10]: "ToStringObject{myprop:AAA}"
            "ToStringObject{myprop:AAA}",
            // [12] obj[11]: "myprop:List<ToStringObject>"
            "ObjectsTest.ToStringObject {myprop:List<ToStringObject>}",
            // [13] obj[12]: "myprop:ToStringObject"
            "ObjectsTest.ToStringObject {myprop:ToStringObject}"),
        // obj
        TO_STRINGS);
  }

  static Stream<Arguments> toSqnQualifiedString() {
    return argumentsStream(
        cartesian()
            .<String>composeArgConverter(0, $ -> $ != null ? new ToStringObject($) : null),
        // expected
        asList(
            // [1] obj[0]: null
            "null",
            // [2] obj[1]: "ToStringObject"
            "ObjectsTest.ToStringObject",
            // [3] obj[2]: "ToStringObject myprop=something"
            "ObjectsTest.ToStringObject myprop=something",
            // [4] obj[3]: "ObjectsTest$ToStringObject"
            "ObjectsTest.ToStringObject",
            // [5] obj[4]: "ObjectsTest$ToStringObject myprop: something"
            "ObjectsTest.ToStringObject myprop: something",
            // [6] obj[5]: "ObjectsTest.ToStringObject myprop: something else"
            "ObjectsTest.ToStringObject myprop: something else",
            // [7] obj[6]: "org.pdfclown.common.util.ObjectsTest$ToStringObject"
            "ObjectsTest.ToStringObject",
            // [8] obj[7]: "org.pdfclown.common.util.ObjectsTest.ToStringObject something"
            "ObjectsTest.ToStringObject something",
            // [9] obj[8]: "ToStringObjects"
            "ObjectsTest.ToStringObject {ToStringObjects}",
            // [10] obj[9]: "org.something.ToStringObject"
            "ObjectsTest.ToStringObject",
            // [11] obj[10]: "ToStringObject{myprop:AAA}"
            "ObjectsTest.ToStringObject {myprop:AAA}",
            // [12] obj[11]: "myprop:List<ToStringObject>"
            "ObjectsTest.ToStringObject {myprop:List<ToStringObject>}",
            // [13] obj[12]: "myprop:ToStringObject"
            "ObjectsTest.ToStringObject {myprop:ToStringObject}"),
        // obj
        TO_STRINGS);
  }

  @Test
  void ancestors() {
    var actual = Objects.ancestors(UnmodifiableList.class,
        Objects.HierarchicalTypeComparator.get()
            .thenComparing(Objects.HierarchicalTypeComparator.Priorities.explicitPriority()
                .set(999, Serializable.class))
            .thenComparing(Objects.HierarchicalTypeComparator.Priorities.interfacePriority()
                .reversed()));

    assertThat(actual, contains(
        List.class,
        UnmodifiableCollection.class,
        Collection.class,
        Iterable.class,
        Serializable.class,
        Object.class));
  }

  @ParameterizedTest
  @MethodSource
  void fqn_Object(Expected<String> expected, @Nullable Object obj) {
    assertParameterizedOf(
        () -> Objects.fqn(obj),
        expected,
        () -> new ExpectedGeneration(of(
            entry("obj", obj))));
  }

  @ParameterizedTest
  @MethodSource
  void fqnd_Object(Expected<String> expected, @Nullable Object obj) {
    assertParameterizedOf(
        () -> Objects.fqnd(obj),
        expected,
        () -> new ExpectedGeneration(of(
            entry("obj", obj))));
  }

  @ParameterizedTest
  @MethodSource
  void fqnd_String(Expected<String> expected, @Nullable String typename) {
    assertParameterizedOf(
        () -> Objects.fqnd(typename),
        expected,
        () -> new ExpectedGeneration(of(
            entry("typename", typename))));
  }

  @ParameterizedTest
  @MethodSource
  void fromLiteralString(Expected<@Nullable Object> expected, @Nullable String s) {
    assertParameterizedOf(
        () -> Objects.fromLiteralString(s),
        expected,
        () -> new ExpectedGeneration(of(
            entry("s", s))));
  }

  @Test
  void objDo() {
    var obj = new MutableObject<>();
    Objects.objDo(obj, $ -> $.setValue("RESULT"));

    assertThat(obj.getValue(), is("RESULT"));
  }

  @Test
  @SuppressWarnings("ConstantValue")
  void objElseGet() {
    List<Object> defaultResult = Collections.emptyList();

    List<Object> obj = of("test");
    Supplier<List<Object>> defaultSupplier = () -> defaultResult;
    assertThat(Objects.objElseGet(obj, defaultSupplier), is(obj));

    obj = null;
    assertThat(Objects.objElseGet(obj, defaultSupplier), is(defaultResult));
  }

  @Test
  @SuppressWarnings("ConstantValue")
  void objToElse() {
    int defaultResult = 0;

    List<Object> obj = of("test");
    assertThat(Objects.objToElse(obj, List::size, defaultResult), is(1));

    obj = null;
    assertThat(Objects.objTo(obj, List::size), is(nullValue()));
    assertThat(Objects.objToElse(obj, List::size, defaultResult), is(defaultResult));
  }

  @Test
  @SuppressWarnings("ConstantValue")
  void objToElseGet() {
    int defaultResult = 0;

    List<Object> obj = of("test");
    Supplier<Integer> defaultSupplier = () -> defaultResult;
    assertThat(Objects.objToElseGet(obj, List::size, defaultSupplier), is(1));

    obj = null;
    assertThat(Objects.objToElseGet(obj, List::size, defaultSupplier), is(defaultResult));
  }

  /**
   * NOTE: {@link Objects#quiet(FailableConsumer, Object)} can NEVER fail per-se; if the given
   * object fails on called operation, that's part of its regular execution.
   */
  @Test
  void quiet_FailableConsumer() {
    var ref = new MutableObject<>();
    var obj = new AutoCloseable() {
      @Override
      public void close() throws Exception {
        ref.setValue("DONE");
        throw new IllegalStateException("FAILED");
      }
    };
    var ret = Objects.quiet(AutoCloseable::close, obj);

    assertThat(ret, sameInstance(obj));
    assertThat(ref.getValue(), is("DONE"));
  }

  /**
   * NOTE: {@link Objects#quiet(FailableConsumer, Object, Consumer)} can NEVER fail per-se; if the
   * given object fails on called operation, that's part of its regular execution.
   */
  @Test
  void quiet_FailableConsumer_Consumer() {
    var obj = new AutoCloseable() {
      @Override
      public void close() throws Exception {
        throw new IllegalStateException("FAILED");
      }
    };
    var exceptionRef = new MutableObject<Throwable>();
    var ret = Objects.quiet(AutoCloseable::close, obj, exceptionRef::setValue);

    assertThat(ret, sameInstance(obj));
    assertThat(exceptionRef.getValue(), instanceOf(IllegalStateException.class));
    assertThat(exceptionRef.getValue().getMessage(), is("FAILED"));
  }

  /**
   * NOTE: {@link Objects#quiet(org.apache.commons.lang3.function.FailableRunnable)} can NEVER fail
   * per-se; if the given operation fails on call, that's part of its regular execution.
   */
  @Test
  void quiet_FailableRunnable() {
    var ref = new MutableObject<>();
    Objects.quiet(() -> {
      ref.setValue("DONE");
      throw new IllegalStateException("FAILED");
    });

    assertThat(ref.getValue(), is("DONE"));
  }

  /**
   * NOTE: {@link Objects#quiet(org.apache.commons.lang3.function.FailableRunnable)} can NEVER fail
   * per-se; if the given operation fails on call, that's part of its regular execution.
   */
  @Test
  void quiet_FailableRunnable_Consumer() {
    var exceptionRef = new MutableObject<Throwable>();
    Objects.quiet(() -> {
      throw new IllegalStateException("FAILED");
    }, exceptionRef::setValue);

    assertThat(exceptionRef.getValue(), instanceOf(IllegalStateException.class));
    assertThat(exceptionRef.getValue().getMessage(), is("FAILED"));
  }

  @ParameterizedTest
  @MethodSource
  void sqn_Object(Expected<String> expected, @Nullable Object obj) {
    assertParameterizedOf(
        () -> Objects.sqn(obj),
        expected,
        () -> new ExpectedGeneration(of(
            entry("obj", obj))));
  }

  @ParameterizedTest
  @MethodSource
  void sqn_String(Expected<String> expected, @Nullable String typename) {
    assertParameterizedOf(
        () -> Objects.sqn(typename),
        expected,
        () -> new ExpectedGeneration(of(
            entry("typename", typename))));
  }

  @ParameterizedTest
  @MethodSource
  void sqnd_Object(Expected<String> expected, @Nullable Object obj) {
    assertParameterizedOf(
        () -> Objects.sqnd(obj),
        expected,
        () -> new ExpectedGeneration(of(
            entry("obj", obj))));
  }

  @ParameterizedTest
  @MethodSource
  void sqnd_String(Expected<String> expected, @Nullable String typename) {
    assertParameterizedOf(
        () -> Objects.sqnd(typename),
        expected,
        () -> new ExpectedGeneration(of(
            entry("typename", typename))));
  }

  @ParameterizedTest
  @MethodSource
  void toLiteralString(Expected<Object> expected, @Nullable Object obj) {
    assertParameterizedOf(
        () -> Objects.toLiteralString(obj),
        expected,
        () -> new ExpectedGeneration(of(
            entry("obj", obj))));
  }

  @ParameterizedTest
  @MethodSource
  void toQualifiedString(Expected<String> expected, Object obj) {
    assertParameterizedOf(
        () -> Objects.toQualifiedString(obj),
        expected,
        () -> new ExpectedGeneration(of(
            entry("obj", obj))).setMaxArgCommentLength(100));
  }

  @ParameterizedTest
  @MethodSource
  void toSqnQualifiedString(Expected<String> expected, Object obj) {
    assertParameterizedOf(
        () -> Objects.toSqnQualifiedString(obj),
        expected,
        () -> new ExpectedGeneration(of(
            entry("obj", obj))).setMaxArgCommentLength(100));
  }

  @Test
  void tryGetElse__fail() {
    var ret = Objects.tryGetElse(() -> {
      throw new IllegalStateException("FAILED");
    }, "ALT");

    assertThat(ret, is("ALT"));
  }

  @Test
  void tryGetElse__ok() {
    var ret = Objects.tryGetElse(() -> "RESULT", "ALT");

    assertThat(ret, is("RESULT"));
  }

  @Test
  void tryGet__fail() {
    var ret = Objects.tryGet(() -> {
      throw new IllegalStateException("FAILED");
    });

    assertThat(ret, is(nullValue()));
  }

  @Test
  void tryGet__ok() {
    var ret = Objects.tryGet(() -> "RESULT");

    assertThat(ret, is("RESULT"));
  }
}
