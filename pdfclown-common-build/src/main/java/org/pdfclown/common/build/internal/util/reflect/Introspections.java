/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Introspections.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util.reflect;

import static java.util.Collections.unmodifiableMap;
import static org.pdfclown.common.build.internal.util_.Strings.uncapitalizeGreedy;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Introspection utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Introspections {
  private static final Map<Class<?>, Map<String, PropertyDescriptor>> declaredPropertyDescriptors =
      new HashMap<>();

  private static final int PROPERTY_MODIFIER_MASK__ON = Modifier.PUBLIC;
  private static final int PROPERTY_MODIFIER_MASK__OFF =
      Modifier.ABSTRACT | Modifier.VOLATILE /* BRIDGE */ | Modifier.STATIC;
  private static final int PROPERTY_MODIFIER_MASK =
      PROPERTY_MODIFIER_MASK__ON | PROPERTY_MODIFIER_MASK__OFF;

  /**
   * Retrieves the property descriptors (getters only) of the given type.
   *
   * <p>
   * This is a highly-specialized implementation that works around some limitations of vanilla
   * {@linkplain java.beans.Introspector#getBeanInfo(Class, Class) introspection}:
   * </p>
   * <ul>
   * <li>default methods: because of a notorious bug
   * (<a href="https://bugs.openjdk.org/browse/JDK-8071693">JDK-8071693</a>), at the moment OpenJDK
   * ignores the introspection of default methods, whilst this method supports it;</li>
   * <li>interfaces as stop types: standard introspection works only on classes as stop types,
   * whilst this method supports interfaces too (in such case, it looks for the farthest ancestor
   * class implementing the interface along the inheritance line).</li>
   * </ul>
   *
   * @param type
   *          Type whose properties are to retrieve.
   * @param stopType
   *          Ancestor type (exclusive) at which to stop the traversal of {@code type}'s inheritance
   *          line.
   */
  public static List<PropertyDescriptor> propertyDescriptors(Class<?> type,
      @Nullable Class<?> stopType) throws IntrospectionException {
    if (stopType != null) {
      // Checking that `stopType` is actually an ancestor...
      var superType = type;
      if (stopType.isInterface()) {
        /*
         * Looking for the farthest ancestor class implementing `stopType` interface along the
         * inheritance line...
         */
        Class<?> actualStopType = null;
        while ((superType = superType.getSuperclass()) != null
            && stopType.isAssignableFrom(superType)) {
          actualStopType = superType;
        }
        if (actualStopType != null) {
          stopType = actualStopType;
        } else {
          superType = null;
        }
      } else {
        //noinspection StatementWithEmptyBody
        while ((superType = superType.getSuperclass()) != null && superType != stopType) {
          /* NOOP */
        }
      }
      if (superType == null)
        throw new IntrospectionException(
            String.format("%s is not a superclass of %s", stopType, type));
    }

    return new ArrayList<>(propertyDescriptors(type, interfaces(stopType, new HashSet<>()),
        new LinkedHashMap<>()).values());
  }

  private static Set<Class<?>> interfaces(@Nullable Class<?> type, Set<Class<?>> result) {
    if (type != null) {
      result.add(type);
      for (Class<?> typeInterface : type.getInterfaces()) {
        interfaces(typeInterface, result);
      }
    }
    return result;
  }

  private static Map<String, PropertyDescriptor> propertyDescriptors(Class<?> type,
      Set<Class<?>> stopTypes, Map<String, PropertyDescriptor> result)
      throws IntrospectionException {
    Map<String, PropertyDescriptor> declaredProperties = declaredPropertyDescriptors.get(type);
    if (declaredProperties == null) {
      declaredProperties = new LinkedHashMap<>();
      for (Method method : type.getDeclaredMethods()) {
        if (method.getParameterCount() > 0
            || (method.getModifiers() & PROPERTY_MODIFIER_MASK) != PROPERTY_MODIFIER_MASK__ON) {
          continue;
        }

        String propertyName;
        {
          if (method.getName().startsWith("get")) {
            propertyName = method.getName().substring(3);
          } else if (method.getName().startsWith("is")) {
            propertyName = method.getName().substring(2);
          } else {
            continue;
          }
          propertyName = uncapitalizeGreedy(propertyName);
        }
        declaredProperties.put(propertyName, new PropertyDescriptor(propertyName, method, null));
      }
      for (Class<?> typeInterface : type.getInterfaces()) {
        if (!stopTypes.contains(typeInterface)) {
          propertyDescriptors(typeInterface, stopTypes, declaredProperties);
        }
      }
      declaredPropertyDescriptors.put(type, unmodifiableMap(declaredProperties));
    }
    for (PropertyDescriptor declaredProperty : declaredProperties.values()) {
      if (!result.containsKey(declaredProperty.getName())) {
        result.put(declaredProperty.getName(), declaredProperty);
      }
    }

    Class<?> superType = type.getSuperclass();
    if (superType != null && !stopTypes.contains(superType)) {
      propertyDescriptors(superType, stopTypes, result);
    }

    return result;
  }

  private Introspections() {
  }
}
