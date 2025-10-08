/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ModelMapper.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.model;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Exceptions.wrongArg;
import static org.pdfclown.common.build.internal.util_.Exceptions.wrongState;
import static org.pdfclown.common.build.internal.util_.Objects.ancestors;
import static org.pdfclown.common.build.internal.util_.Objects.fqn;
import static org.pdfclown.common.build.internal.util_.Objects.sqn;
import static org.pdfclown.common.build.internal.util_.Strings.COLON;
import static org.pdfclown.common.build.internal.util_.Strings.S;
import static org.pdfclown.common.build.internal.util_.Strings.SPACE;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.util.reflect.Introspections;
import org.pdfclown.common.build.internal.util_.Objects.HierarchicalTypeComparator;
import org.pdfclown.common.build.internal.util_.Objects.HierarchicalTypeComparator.Priorities.TypePriorityComparator;
import org.pdfclown.common.build.internal.util_.RelatedMap;
import org.pdfclown.common.build.internal.util_.annot.InitNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain object converter to abstract model (JSON format).
 * <p>
 * Domain objects are evaluated as:
 * </p>
 * <ul>
 * <li><b>primary</b> (complex objects), mapped by {@link #map(Object, List, Set, int)} as
 * full-fledged JSON objects</li>
 * <li><b>secondary</b> (components (either properties or aggregations (maps or collections)) of
 * primary objects), mapped by {@link #mapValue(Object, List, Set, int)} as either simple values
 * (such as {@code String}) or primary objects themselves.</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
public class ModelMapper<T> {
  /**
   * Property selector.
   * <p>
   * The selection can be applied in two ways: <b>exclusive</b> (ALL BUT the specified properties)
   * or <b>inclusive</b> (NOTHING BUT the specified properties).
   * </p>
   */
  public static class PropertySelector {
    /**
     * Property selection rule.
     *
     * @author Stefano Chizzolini
     */
    public static class Rule {
      public final int level;
      public final Set<String> properties;

      Rule(int level, Set<String> properties) {
        this.level = level;
        this.properties = properties;
      }

      @Override
      public String toString() {
        return sqn(this) + " {level: " + level + "; properties: " + properties + "}";
      }
    }

    /**
     * Gets the all-but-these-properties selector for the type.
     */
    public static PropertySelector excludeProperties(Class<?> type, String... properties) {
      return excludeProperties(type, 0, properties);
    }

    /**
     * Gets the all-but-these-properties selector for the type.
     *
     * @param level
     *          Exclusion level (that is, selection is applied from this level).
     */
    public static PropertySelector excludeProperties(Class<?> type, int level,
        String... properties) {
      return new PropertySelector(type, true, level, properties);
    }

    /**
     * Gets the nothing-but-these-properties selector for the type.
     */
    public static PropertySelector includeProperties(Class<?> type, String... properties) {
      return includeProperties(type, Integer.MAX_VALUE, properties);
    }

    /**
     * Gets the nothing-but-these-properties selector for the type.
     *
     * @param level
     *          Inclusion level (that is, selection is applied up to this level).
     */
    public static PropertySelector includeProperties(Class<?> type, int level,
        String... properties) {
      return new PropertySelector(type, false, level, properties);
    }

    private boolean exclusive;
    private boolean mutable;
    private final List<Rule> rules;
    private Class<?> type;

    /**
     * Creates a mutable selector.
     */
    public PropertySelector(Class<?> type, boolean exclusive) {
      this.type = requireNonNull(type);
      this.exclusive = exclusive;

      mutable = true;
      rules = new ArrayList<>() /* NOTE: Mutable list */;
    }

    /**
     * Creates an immutable selector.
     */
    public PropertySelector(Class<?> type, boolean exclusive, int level, Set<String> properties) {
      this.type = requireNonNull(type);
      this.exclusive = exclusive;

      rules = List.of(new Rule(level, properties)) /* NOTE: Immutable list */;
    }

    /**
     * Creates an immutable selector.
     */
    public PropertySelector(Class<?> type, boolean exclusive, int level, String... properties) {
      this(type, exclusive, level, Set.of(properties) /* NOTE: Immutable set */);
    }

    /**
     * Creates a mutable selector copying the given one.
     */
    public PropertySelector(PropertySelector source) {
      this(source.getType(), source.isExclusive());

      merge(source);
    }

    /**
     * Property selection rules.
     */
    public List<Rule> getRules() {
      return rules;
    }

    /**
     * Domain type whose properties are selected according to the rules in this selector.
     */
    public Class<?> getType() {
      return type;
    }

    /**
     * Whether the rules in this selector exclude the specified properties, while include all the
     * others; otherwise, they include the specified properties, while exclude all the others.
     */
    public boolean isExclusive() {
      return exclusive;
    }

    /**
     * Whether this selector is mutable.
     */
    public boolean isMutable() {
      return mutable;
    }

    /**
     * Gets whether a property at a level is selected.
     */
    public boolean isSelected(String propertyName, int level) {
      /*
       * NOTE: In order to be selected, all rules MUST be satisfied.
       */
      for (var rule : rules) {
        if (((level < rule.level) != exclusive
            && rule.properties.contains(propertyName)) == exclusive)
          return false;
      }
      return true;
    }

    /**
     * Merges a selector into this one.
     */
    public void merge(PropertySelector other) {
      if (!mutable)
        throw wrongState("Immutable");

      if (other.exclusive != exclusive) {
        exclusive = other.exclusive;
        rules.clear();
      }
      otherLoop: for (var otherRule : other.rules) {
        for (var rule : rules) {
          if (rule.level == otherRule.level) {
            rule.properties.addAll(otherRule.properties);
            continue otherLoop;
          }
        }
        rules.add(new Rule(otherRule.level,
            new HashSet<>(otherRule.properties) /* NOTE: Mutable set */));
      }
      type = other.type /* NOTE: Type updated only after merger succeeded */;
    }

    @Override
    public String toString() {
      return String.format(Locale.ROOT, "%s {type: %s; exclusive: %s; %s}", sqn(this), fqn(type),
          exclusive, rules);
    }
  }

  /**
   * Value mapper.
   *
   * @author Stefano Chizzolini
   */
  @FunctionalInterface
  protected interface ValueMapper {
    @Nullable
    Object map(Object value, List<PropertySelector> selectors,
        Set<Object> visitedObjs, int level);
  }

  /**
   * Value mappers.
   *
   * @author Stefano Chizzolini
   */
  @SuppressWarnings("rawtypes")
  protected static class ValueMapperMap extends RelatedMap<Class, ValueMapper> {
    private static class RelatedKeysProvider extends RelatedMap.RelatedProvider<Class> {
      /**
       * Explicit type priorities.
       */
      private TypePriorityComparator priorities = HierarchicalTypeComparator.Priorities
          .explicitPriority();

      @SuppressWarnings("NotNullFieldNotInitialized")
      private @InitNonNull Function<Class, Iterable<Class>> base;

      @Override
      public Iterable<Class> apply(Class type) {
        return base.apply(type);
      }

      @Override
      public RelatedKeysProvider clone() {
        var ret = (RelatedKeysProvider) super.clone();
        ret.priorities = ret.priorities.clone();
        return ret;
      }

      void init(Set<Class> keys) {
        base = $ -> ancestors($, HierarchicalTypeComparator.get()
            .thenComparing(priorities)
            .thenComparing(HierarchicalTypeComparator.Priorities.interfacePriority())
            .thenComparing(($1, $2) -> {
              int ret;
              var name1 = $1.getName();
              var name2 = $2.getName();

              // Prioritize library-specific types!
              if ((ret = libraryPriority(name1) - libraryPriority(name2)) != 0)
                return ret;

              // Compare arbitrarily (no more relevant aspects to evaluate)!
              return name1.compareTo(name2);
            }), keys, false);
      }
    }

    private static final long serialVersionUID = 1L;

    private static int libraryPriority(String name) {
      return name.startsWith("org.pdfclown.") ? -1 : 0;
    }

    /**
     * Explicitly mapped types.
     * <p>
     * Represents all the mappings not derived from related ones.
     * </p>
     */
    private final Map<ValueMapper, Class> rootTypes = new HashMap<>();

    ValueMapperMap() {
      super(new RelatedKeysProvider());

      ((RelatedKeysProvider) getRelatedKeysProvider()).init(keySet());
    }

    /**
     * Type priorities.
     */
    public TypePriorityComparator getPriorities() {
      return ((RelatedKeysProvider) getRelatedKeysProvider()).priorities;
    }

    @Override
    public @Nullable ValueMapper put(Class key, ValueMapper value) {
      if (!rootTypes.containsKey(value)) {
        rootTypes.put(value, key);
      }
      return super.put(key, value);
    }

    public @Nullable ValueMapper put(Class key, ValueMapper value,
        int priority) {
      getPriorities().set(priority, key);
      return put(key, value);
    }

    @Override
    protected void putRelated(Class relatedKey, Class key, ValueMapper value) {
      put(key, value, getPriorities().get(relatedKey));

      if (log.isDebugEnabled()) {
        log.debug("ValueMapper.putRelated: {} from {}", sqn(key), sqn(relatedKey));
      }
    }
  }

  private static final Logger log = LoggerFactory.getLogger(ModelMapper.class);

  protected @Nullable Comparator<String> keyComparator;
  protected @Nullable Map<Class<?>, PropertySelector> typeSelectors;
  protected final ValueMapperMap valueMappers = new ValueMapperMap();
  {
    valueMappers.put(Object.class, ($obj, $selectors, $visitedObjs, $level) -> {
      if ($obj.getClass().isArray()) {
        var b = new StringBuilder("[");
        $level++;
        for (int i = 0; i < Array.getLength($obj); i++) {
          if (i > 0) {
            b.append(", ");
          }
          b.append(mapValue(Array.get($obj, i), $selectors, $visitedObjs, $level));
        }
        return b.append("]").toString();
      } else
        return $obj.toString();
    });

    valueMappers.put(Iterable.class, this::map);

    valueMappers.put(Map.class, this::map);

    valueMappers.put(Map.Entry.class, ($obj, $selectors, $visitedObjs, $level) -> {
      @SuppressWarnings("rawtypes")
      var entry = (Map.Entry) $obj;
      return entry.getKey() + S + COLON + SPACE
          + mapValue(entry.getValue(), $selectors, $visitedObjs, $level);
    });
  }

  /**
   * @param propertyMapping
   *          Whether object property mapping is enabled.
   * @param keyComparator
   *          {@link JsonObject} key comparator.
   */
  public ModelMapper(boolean propertyMapping, @Nullable Comparator<String> keyComparator) {
    if (propertyMapping) {
      typeSelectors = new HashMap<>();
    }
    this.keyComparator = keyComparator;
  }

  /**
   * Maps an object to its JSON counterpart.
   *
   * @param obj
   *          Object to map.
   * @implNote Because of visited-object tracking, to ensure consistent mappings across multiple
   *           iterations over the same object a strict operation ordering is enforced (properties
   *           are therefore sorted before being processed).
   */
  public JsonObject map(@NonNull T obj) {
    return map(obj, List.of());
  }

  /**
   * Maps an object to its JSON counterpart.
   *
   * @param obj
   *          Object to map.
   * @param selectors
   *          Filters for selective inclusion/exclusion of object properties based on their parent
   *          type. Types outside this collection are fully included by default.
   * @implNote Because of visited-object tracking, to ensure consistent mappings across multiple
   *           iterations over the same object a strict operation ordering is enforced (properties
   *           are therefore sorted before being processed).
   */
  public JsonObject map(@NonNull T obj, List<PropertySelector> selectors) {
    return map(obj, selectors, new HashSet<>(), 0);
  }

  /**
   * Maps objects to their JSON counterparts.
   *
   * @param objs
   *          Objects to map.
   * @implNote Because of visited-object tracking, to ensure consistent mappings across multiple
   *           iterations over the same object a strict operation ordering is enforced (properties
   *           are therefore sorted before being processed).
   */
  public JsonArray mapAll(Collection<? extends T> objs) {
    return mapAll(objs, List.of());
  }

  /**
   * Maps objects to their JSON counterparts.
   *
   * @param objs
   *          Objects to map.
   * @param selectors
   *          Filters for selective inclusion/exclusion of object properties based on their parent
   *          type. Types outside this collection are fully included by default.
   * @implNote Because of visited-object tracking, to ensure consistent mappings across multiple
   *           iterations over the same object a strict operation ordering is enforced (properties
   *           are therefore sorted before being processed).
   */
  public JsonArray mapAll(Collection<? extends T> objs, List<PropertySelector> selectors) {
    var ret = new JsonArray();
    var visitedObjs = new HashSet<>();
    for (var obj : objs) {
      ret.put(map(obj, selectors, visitedObjs, 1));
    }
    return ret;
  }

  /**
   * Maps an object to its JSON counterpart.
   * <p>
   * {@code obj} is added to {@code visitedObjs} to prevent duplicated mappings and infinite loops
   * on circular references: <span class="important">it is caller's responsibility to check
   * {@code visitedObjs} <i>before</i> calling this method in order to fall back to an alternate,
   * reference-like representation</span> (for example, as a PDF reference (say, "14 0 R") instead
   * of its referenced PDF object) in case {@code obj} has already been visited.
   * </p>
   *
   * @param obj
   *          Object to map.
   * @param selectors
   *          Filters for selective inclusion/exclusion of object properties based on their parent
   *          type. Types outside this collection are fully included by default.
   * @param visitedObjs
   *          Already visited objects.
   * @param level
   *          Current nesting level.
   * @throws IllegalArgumentException
   *           if {@code obj} has already been visited (circular references MUST be resolved by the
   *           caller with an alternate, reference-like representation of {@code obj}).
   * @implNote To ensure consistent mappings across multiple sessions over the same object, a strict
   *           transformation ordering is enforced (properties are therefore sorted before being
   *           processed).
   */
  protected JsonObject map(Object obj, List<PropertySelector> selectors, Set<Object> visitedObjs,
      int level) {
    if (visitedObjs.contains(obj))
      throw wrongArg("obj", obj, "ALREADY VISITED");

    visitedObjs.add(obj);

    var ret = new JsonObject(keyComparator);

    PropertySelector objSelector = typeSelectors != null
        ? typeSelectors.computeIfAbsent(obj.getClass(), $ -> {
          /*-
           * NOTE: Applied property selector is the most specific to the object type, according to
           * these rules:
           * - derived types win over their ancestors;
           * - concrete types win over interfaces;
           * - matching selectors are merged (if non-conflicting, the latest is combined with the
           *   other one; otherwise, the latest wins over the other one).
           */
          PropertySelector value = new PropertySelector(Object.class, true);
          for (PropertySelector selector : selectors) {
            if (selector.type.isAssignableFrom($)
                && (value.type.isAssignableFrom(selector.type)
                    || value.type.isInterface())) {
              value.merge(selector);
            }
          }
          return value;
        })
        : null;

    if (log.isDebugEnabled()) {
      log.debug("map(level: {}): {}, {}", level, sqn(obj), objSelector);
    }

    int innerLevel = level + 1;

    // Custom properties.
    mapCustomProperties(obj, objSelector, ret, level);

    // Aggregation properties.
    if (obj instanceof Map) {
      if (objSelector == null || objSelector.isSelected("entries", level)) {
        var jsonEntries = new JsonObject(keyComparator);
        for (var entry : ((Map<?, ?>) obj).entrySet()) {
          jsonEntries.put(entry.getKey().toString(),
              mapValue(entry.getValue(), selectors, visitedObjs, innerLevel));
        }
        ret.put("entries", jsonEntries);
      }
    } else if (obj instanceof Iterable) {
      if (objSelector == null || objSelector.isSelected("items", level)) {
        var jsonItems = new JsonArray();
        for (var e : (Iterable<?>) obj) {
          jsonItems.put(mapValue(e, selectors, visitedObjs, innerLevel));
        }
        ret.put("items", jsonItems);
      }
    }

    // Discovered properties.
    if (objSelector != null) {
      List<PropertyDescriptor> objProperties;
      try {
        objProperties = Introspections.propertyDescriptors(obj.getClass(), null);
        /*
         * NOTE: Properties are sorted before being processed to ensure consistent mappings across
         * multiple sessions over the same object.
         */
        objProperties.sort(Comparator.comparing(PropertyDescriptor::getName));
      } catch (IntrospectionException ex) {
        throw runtime(ex);
      }
      for (var objProperty : objProperties) {
        if (!objSelector.isSelected(objProperty.getName(), level)
            || ret.has(objProperty.getName())) {
          continue;
        }

        var getter = objProperty.getReadMethod();
        if (getter == null) {
          continue;
        }

        Object mappedValue;
        try {
          if (!getter.canAccess(obj)) {
            getter.setAccessible(true);
          }
          mappedValue = mapValue(getter.invoke(obj), selectors, visitedObjs, innerLevel);
        } catch (InvocationTargetException ex) {
          mappedValue = ex.getTargetException().getClass().getName();

          log.warn("Value mapping failed", ex.getTargetException());
        } catch (Exception ex) {
          throw runtime(ex);
        }
        ret.put(objProperty.getName(), mappedValue);
      }
    }
    return ret;
  }

  /**
   * Maps custom properties which don't belong to the actual interface of the object.
   * <p>
   * Such properties are placed before all the other properties.
   * </p>
   *
   * @param obj
   *          Object to map.
   * @param objJson
   *          JSON representation of {@code obj}.
   * @param level
   *          Current nesting level.
   */
  protected void mapCustomProperties(Object obj, @Nullable PropertySelector objSelector,
      JsonObject objJson, int level) {
  }

  /**
   * Maps a value to its JSON counterpart.
   *
   * @param value
   *          Value to map.
   * @param selectors
   *          Filters for selective inclusion/exclusion of object properties based on their parent
   *          type. Types outside this collection are fully included by default.
   * @param visitedObjs
   *          Visited objects to avoid mapping duplications.
   * @param level
   *          Current nesting level.
   */
  protected @Nullable Object mapValue(@Nullable Object value, List<PropertySelector> selectors,
      Set<Object> visitedObjs, int level) {
    if (value == null || (value instanceof CharSequence && ((CharSequence) value).length() == 0))
      return null;

    if (log.isDebugEnabled()) {
      log.debug("mapValue(level: {}): {}", level, sqn(value));
    }

    var valueMapper = valueMappers.get(value.getClass());
    assert valueMapper != null /*
                                * NOTE: By definition, there MUST be a fallback mapper for Object
                                * class, so it cannot be null in any case
                                */;
    return valueMapper.map(value, selectors, visitedObjs, level);
  }
}
