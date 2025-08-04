/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BaseXnum.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Exceptions.wrongState;
import static org.pdfclown.common.util.Objects.objTo;
import static org.pdfclown.common.util.Objects.objToElse;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.UnmodifiableView;
import org.pdfclown.common.util.spi.ServiceProvider;
import org.pdfclown.common.util.spi.XnumProvider;

/**
 * Base {@link Xnum} implementation and manager.
 * <p>
 * Overcomes the major (by-design) limitation of regular enumerations (lack of extensibility)
 * leveraging {@link Xnum}-derived interfaces to combine one or more enumerations (for predefined
 * constants) and a factory (for custom constants) into a unified collection emulating most of
 * {@link Enum} semantics (such as {@link #get(Class, Object)} and {@link #values(Class)}), with the
 * additional benefit of {@link XtEnum} support to meaningful domain-specific
 * {@linkplain XtEnum#getCode() identity codes} for uniform and efficient lookups.
 * </p>
 * <p>
 * Advantages over regular enumerations:
 * </p>
 * <ul>
 * <li><i>multiple regular enumerations can work together in a (almost) seamless way</i>—sure, some
 * syntactic optimization (such as {@code switch} statements) is lost, but that's an inevitable
 * trade-off when trying to juggle multiple enumerations as a whole, alas</li>
 * <li><i>non-{@link XnumType#isSealed() sealed} augmented enumerations are open to custom
 * constants</i> —that may sound abusive (after all, enumerations are supposed to be statically
 * defined on purpose, right?), but some models are semantically open to recurring (constant-like)
 * unknown cases</li>
 * </ul>
 * <p>
 * Each use case is expected to be implemented as an augmented enumeration this way:
 * </p>
 * <ol>
 * <li>derive a specialized <b>augmented enumeration interface</b> from {@link Xnum}</li>
 * <li>for <i>statically-defined (predefined) constants</i>, implement the specialized interface
 * deriving one or more <b>regular enumerations</b> from {@link Enum}</li>
 * <li>for <i>dynamically-defined (custom) constants</i>, implement the specialized interface
 * deriving one <b>pseudo-enumeration</b> from {@link BaseXnum}</li>
 * <li>{@linkplain #register(Class, Class, Predicate, BiFunction, Class) register} the <b>augmented
 * enumeration</b> passing all its implementations under the same specialized interface</li>
 * <li>use the augmented enumeration as a whole (regardless of the number of regular enumerations
 * and custom constants it encompasses), retrieving its constants through
 * {@link #get(Class, Object)} and {@link #values(Class)}</li>
 * </ol>
 * <p>
 * Within the same augmented enumeration, uniqueness and identity stability of the constants are
 * guaranteed:
 * </p>
 * <ul>
 * <li>uniqueness: combining regular enumerations having multiple constants with equivalent
 * {@link XtEnum#getCode() code}s causes an {@link IllegalStateException} to be thrown</li>
 * <li>identity stability: constants associated to equivalent codes are the same constant
 * (therefore, identity operator ({@code ==}) can be safely used to match constants)</li>
 * </ul>
 *
 * @param <K>
 *          Domain-specific identity type.
 * @author Stefano Chizzolini
 */
public abstract class BaseXnum<@NonNull K> implements Xnum<K> {
  /**
   * Augmented enumeration descriptor.
   *
   * @param <E>
   *          Augmented enumeration type.
   * @param <K>
   *          Domain-specific identity type.
   * @author Stefano Chizzolini
   */
  public static class XnumType<E extends Xnum<K>, @NonNull K> {
    private final Class<K> codeType;
    private final Predicate<K> codeValidator;
    private final Map<K, E> constants = new HashMap<>();
    private final BiFunction<K, @NonNull Object, E> constructor;
    private boolean sealed;
    private final Class<E> type;

    /**
     * @param type
     *          Interface type.
     * @param codeType
     *          Domain-specific identity.
     * @param codeValidator
     *          Domain-specific identity validator (in case of new constant candidates).
     * @param constructor
     *          Element constructor.
     */
    private XnumType(Class<@NonNull E> type, Class<@NonNull K> codeType,
        @Nullable Predicate<K> codeValidator, BiFunction<K, @NonNull Object, E> constructor) {
      this.type = requireNonNull(type);
      this.codeType = requireNonNull(codeType);
      this.codeValidator = requireNonNullElseGet(codeValidator, () -> $ -> true);
      this.constructor = requireNonNull(constructor);
    }

    /**
     * Adds to this augmented enumeration the constants associated to the given enumeration.
     *
     * @param enumType
     *          Enum type.
     * @return Self.
     * @throws IllegalArgumentException
     *           If {@code enumType} is not an enumeration or its {@link Xnum}-derived interface is
     *           different from {@link #getType() type}.
     * @throws IllegalStateException
     *           If any of the constants associated to {@code enumType} collides with those already
     *           associated to this augmented enumeration.
     */
    @SuppressWarnings("null")
    public XnumType<E, K> addAll(Class<? extends E> enumType) {
      if (!enumType.isEnum())
        throw wrongArg("enumType", enumType, "MUST be an enum");
      else if (getType(enumType) != type)
        throw wrongArg("enumType", enumType, "MUST implement {} subinterface", type);

      for (E constant : enumType.getEnumConstants()) {
        K code = constant.getCode();
        if (constants.containsKey(code))
          throw wrongState("Constant already exists for '{}' code ({}): {}", code, constant,
              constants.get(code));

        constants.put(code, constant);
      }
      return this;
    }

    /**
     * Gets the constant associated to the given code.
     *
     * @param code
     *          {@linkplain XtEnum#getCode() Domain-specific identity value}.
     * @return According to the following priority:
     *         <ol>
     *         <li>existing constant</li>
     *         <li>new custom constant (if {@code code} is not {@code null} and this augmented
     *         enumeration is not {@link #isSealed() sealed})</li>
     *         <li>{@code null}</li>
     *         </ol>
     * @throws IllegalArgumentException
     *           If {@code code} is invalid.
     */
    @SuppressWarnings("null")
    public @Nullable E get(@Nullable K code) {
      var ret = constants.get(code);
      if (ret == null) {
        if (code == null || sealed)
          return null;
        else if (!codeValidator.test(code))
          throw wrongArg("code", code);

        constants.put(code, ret = constructor.apply(code, GUARD));
      }
      return ret;
    }

    /**
     * {@linkplain XtEnum#getCode() Domain-specific identity} type associated to this augmented
     * enumeration.
     */
    public Class<K> getCodeType() {
      return codeType;
    }

    /**
     * All the constants associated to this augmented enumeration.
     */
    public Map<K, E> getConstants() {
      return Collections.unmodifiableMap(constants);
    }

    /**
     * {@link Xnum}-derived interface type associated to this augmented enumeration.
     */
    public Class<E> getType() {
      return type;
    }

    /**
     * Whether no more constants can be added to this augmented enumeration.
     */
    public boolean isSealed() {
      return sealed;
    }

    /**
     * Avoids further constants to be added to this augmented enumeration.
     */
    public void seal() {
      sealed = true;
    }

    @SuppressWarnings("unchecked")
    private Class<E> getType(Class<? extends E> implType) {
      if (implType.isInterface())
        throw wrongArg("implType", implType, "MUST be an implementation");

      for (Class<?> typeInterface : implType.getInterfaces()) {
        if (Xnum.class.isAssignableFrom(typeInterface)) {
          if (typeInterface == Xnum.class)
            throw wrongArg("implType", implType, "MUST implement a subinterface of {}", Xnum.class);

          return (Class<E>) typeInterface;
        }
      }
      throw wrongArg("implType", implType, "MUST extend {}", Xnum.class);
    }
  }

  /**
   * {@linkplain BaseXnum#BaseXnum( Object, Object) Constructor} guard.
   * <p>
   * Ensures only internal calls are valid.
   * </p>
   */
  private static final Object GUARD = new Object();

  @SuppressWarnings("rawtypes")
  private static final Map<Class, BaseXnum.XnumType> types = new HashMap<>();

  private static final List<@NonNull XnumProvider> providers =
      ServiceProvider.discover(XnumProvider.class);

  /**
   * Gets whether the constant corresponding to the given code exists.
   *
   * @param <E>
   *          Augmented enumeration type.
   * @param <K>
   *          Domain-specific identity type.
   * @param type
   *          Augmented enumeration type.
   * @param code
   *          Domain-specific identity.
   */
  public static <E extends Xnum<K>, K> boolean contains(Class<E> type, @Nullable K code) {
    return objToElse(get(type), $ -> $.constants.containsKey(code), false);
  }

  /**
   * Gets the augmented enumeration type associated to the given interface.
   *
   * @param <E>
   *          Augmented enumeration type.
   * @param <K>
   *          Domain-specific identity type.
   * @param type
   *          Augmented enumeration type.
   */
  @SuppressWarnings("unchecked")
  public static <E extends Xnum<K>, @NonNull K> @Nullable XnumType<E, K> get(Class<E> type) {
    var ret = types.get(requireNonNull(type));
    if (ret == null) {
      /*
       * NOTE: Each provider may register additional implementations of the same type, so we have to
       * call all of them.
       */
      for (XnumProvider provider : providers) {
        provider.load(type);
      }
      ret = types.get(type);
    }
    return ret;
  }

  /**
   * Gets the constant corresponding to the given code.
   *
   * @param <E>
   *          Augmented enumeration type.
   * @param <K>
   *          Domain-specific identity type.
   * @param type
   *          Augmented enumeration type.
   * @param code
   *          Domain-specific identity.
   */
  public static <E extends Xnum<K>, K> @Nullable E get(Class<E> type, @Nullable K code) {
    return objTo(get(type), $ -> $.get(code));
  }

  /**
   * Gets the constants associated to the given type.
   *
   * @param <E>
   *          Augmented enumeration type.
   * @param <K>
   *          Domain-specific identity type.
   * @param type
   *          Augmented enumeration type.
   */
  public static <E extends Xnum<K>,
      K> @Nullable @UnmodifiableView Collection<E> values(Class<E> type) {
    /*
     * NOTE: The unmodifiable collection guarantees that changes to the backing collections are
     * visible to it too.
     */
    return objTo(get(type), $ -> unmodifiableCollection($.constants.values()));
  }

  /**
   * Registers the given {@linkplain Xnum augmented enumeration}.
   * <p>
   * Its constants are gathered in a map common to all the types implementing the same
   * {@link Xnum}-derived interface.
   * </p>
   *
   * @param <E>
   *          Augmented enumeration type.
   * @param <K>
   *          Domain-specific identity type.
   * @param type
   *          Interface type.
   * @param codeType
   *          Domain-specific identity type.
   * @param codeValidator
   *          Domain-specific identity validator (in case of new constant candidates).
   * @param constructor
   *          Element constructor.
   * @param enumType
   *          Enumeration type.
   */
  protected static <E extends Xnum<K>, @NonNull K> XnumType<E, K> register(Class<@NonNull E> type,
      Class<@NonNull K> codeType, @Nullable Predicate<K> codeValidator,
      BiFunction<K, @NonNull Object, E> constructor, Class<? extends E> enumType) {
    if (types.containsKey(type))
      throw wrongArg("type", type, "Interface already registered");

    var ret = new XnumType<>(type, codeType, codeValidator, constructor).addAll(enumType);
    types.put(ret.type, ret);
    return ret;
  }

  private final K code;

  /**
   * <span class="warning">(For internal use only)</span> Call {@link #get(Class, Object)} instead.
   */
  protected BaseXnum(K code, Object guard) {
    if (guard != GUARD)
      throw wrongArg("guard", null, "Manual instantiation forbidden: call {}.valueOf(..) instead",
          getClass());

    this.code = requireNonNull(code);
  }

  @Override
  public K getCode() {
    return code;
  }

  @Override
  public String name() {
    return code.toString();
  }

  @Override
  public String toString() {
    return name();
  }
}
