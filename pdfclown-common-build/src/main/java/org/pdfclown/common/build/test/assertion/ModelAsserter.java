/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ModelAsserter.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static java.util.Comparator.comparing;
import static org.pdfclown.common.build.internal.temp.util.Objects.fqn;
import static org.pdfclown.common.build.internal.temp.util.Objects.type;
import static org.pdfclown.common.build.internal.temp.util.io.Files.FILE_EXTENSION__JSON;
import static org.pdfclown.common.util.Conditions.requireType;
import static org.pdfclown.common.util.Exceptions.wrongArgOpt;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__ZIP;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pdfclown.common.build.test.model.JsonArray;
import org.pdfclown.common.build.test.model.ModelDiffer;
import org.pdfclown.common.build.test.model.ModelMapper;
import org.pdfclown.common.build.test.model.ModelMapper.PropertySelector;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automated model assertions for integration testing.
 * <p>
 * This class enables massive checks over a domain model (actual object) against a resource
 * (expected object) which can be {@linkplain Asserter#SYSTEM_PROPERTY__UPDATE_EXPECTED
 * automatically updated}. Comparisons are performed through an {@linkplain ModelMapper abstract
 * model}.
 * </p>
 *
 * @param <TMap>
 *          Model mapping type.
 * @param <TMapDiff>
 *          Model difference mapping type.
 * @param <TDiff>
 *          Model difference type.
 * @author Stefano Chizzolini
 */
public class ModelAsserter<TMap, TMapDiff, TDiff> extends ContentAsserter<Object> {
  private static final Logger log = LoggerFactory.getLogger(ModelAsserter.class);

  private static final String FILE_EXTENSION__JSON_ZIP = FILE_EXTENSION__JSON + FILE_EXTENSION__ZIP;

  protected Supplier<ModelDiffer<TDiff, ? extends TMapDiff>> modelDifferSupplier;
  protected Supplier<ModelMapper<TMapDiff>> modelDiffMapperSupplier;
  protected Supplier<ModelMapper<TMap>> modelMapperSupplier;

  /**
   * @param modelMapperSupplier
   *          Model mapper factory.
   * @param modelDiffMapperSupplier
   *          Model difference mapper factory.
   * @param modelDifferSupplier
   *          Model difference collector factory.
   */
  public ModelAsserter(Supplier<ModelMapper<TMap>> modelMapperSupplier,
      Supplier<ModelMapper<TMapDiff>> modelDiffMapperSupplier,
      Supplier<ModelDiffer<TDiff, ? extends TMapDiff>> modelDifferSupplier) {
    this.modelMapperSupplier = modelMapperSupplier;
    this.modelDiffMapperSupplier = modelDiffMapperSupplier;
    this.modelDifferSupplier = modelDifferSupplier;
  }

  /**
   * Asserts that the difference between objects matches the expected one.
   *
   * @param expectedDiffResourceBasename
   *          Resource basename of the expected object difference in serialized (JSON) form. If
   *          relative, it is resolved on the local name of {@link Config#getTest()
   *          config.getTest()}.
   * @param inputObj
   *          Input object.
   * @param outputObj
   *          Output object.
   * @param config
   *          Assertion configuration.
   * @throws AssertionError
   *           if the difference between {@code inputObj} and {@code outputObj} doesn't match that
   *           from {@code expectedDiffResourceBasename}.
   * @see Asserter#SYSTEM_PROPERTY__UPDATE_EXPECTED
   */
  public void assertDiffEquals(String expectedDiffResourceBasename, TDiff inputObj, TDiff outputObj,
      Config config) {
    // Collect the differences between the objects!
    List<? extends TMapDiff> diffs = modelDifferSupplier.get().diff(inputObj, outputObj);

    // Map the differences to JSON!
    JsonArray actualJsonArray = modelDiffMapperSupplier.get().mapAll(diffs);

    // Check consistency with expected differences!
    doAssertEquals(expectedDiffResourceBasename, FILE_EXTENSION__JSON_ZIP, actualJsonArray, config);
  }

  /**
   * Asserts that an object matches the expected one.
   *
   * @param expectedObjResourceBasename
   *          Resource basename of the expected object in serialized (JSON) form. If relative, it is
   *          resolved on the local name of {@link Config#getTest() config.getTest()}.
   * @param actualObj
   *          Actual object.
   * @param config
   *          Assertion configuration.
   * @throws AssertionError
   *           if {@code actualObj} doesn't match the one loaded from
   *           {@code expectedObjResourceBasename}.
   * @see Asserter#SYSTEM_PROPERTY__UPDATE_EXPECTED
   */
  public void assertEquals(String expectedObjResourceBasename, TMap actualObj, Config config) {
    assertEquals(expectedObjResourceBasename, actualObj, List.of(), config);
  }

  /**
   * Asserts that an object matches the expected one.
   *
   * @param expectedObjResourceBasename
   *          Resource basename of the expected object in serialized (JSON) form. If relative, it is
   *          resolved on the local name of {@link Config#getTest() config.getTest()}.
   * @param actualObj
   *          Actual object.
   * @param objSelectors
   *          Property selectors for the object.
   * @param config
   *          Assertion configuration.
   * @throws AssertionError
   *           if {@code actualObj} doesn't match the one loaded from
   *           {@code expectedObjResourceBasename}.
   * @see Asserter#SYSTEM_PROPERTY__UPDATE_EXPECTED
   */
  public void assertEquals(String expectedObjResourceBasename, TMap actualObj,
      List<PropertySelector> objSelectors, Config config) {
    /*
     * Property selector consolidation.
     *
     * NOTE: Multiple elements may be associated to the same type; since the assertion algorithm
     * peeks just a single element to filter a given type, it is necessary to normalize them before
     * submitting.
     */
    {
      var targetObjSelectors = new ArrayList<>(objSelectors) /* NOTE: Preserves original order */;
      var prevRef = new PropertySelector[1];
      objSelectors.stream()
          .sorted(comparing($ -> $.getType().getName()))
          .forEachOrdered($ -> {
            var prev = prevRef[0];
            if (prev != null && $.getType() == prev.getType()) {
              // Same type.
              // Merge?
              if ($.isExclusive() == prev.isExclusive()) {
                int pos = targetObjSelectors.indexOf(prev);
                if (!prev.isMutable()) {
                  prevRef[0] = prev = new PropertySelector(prev);
                }
                prev.merge($);
                targetObjSelectors.set(pos, prev);
                targetObjSelectors.remove($);

                log.debug("objSelectors: {} properties MERGED", fqn($.getType()));
              }
              // Drop older.
              else {
                targetObjSelectors.remove(prev);
                prevRef[0] = $;
              }
            } else {
              prevRef[0] = $;
            }
          });
      objSelectors = targetObjSelectors;
    }

    // Map the object to JSON!
    var actualJsonObj = modelMapperSupplier.get().map(actualObj, objSelectors);

    // Check consistency with expected object!
    doAssertEquals(expectedObjResourceBasename, FILE_EXTENSION__JSON_ZIP, actualJsonObj, config);
  }

  @Override
  protected void doAssertEquals(Object expectedContent, Object actualContent) {
    if (expectedContent instanceof JSONObject expectedObject) {
      JSONAssert.assertEquals(expectedObject,
          requireType(actualContent, JSONObject.class, "actualContent"), true);
    } else if (expectedContent instanceof JSONArray expectedArray) {
      JSONAssert.assertEquals(expectedArray,
          requireType(actualContent, JSONArray.class, "actualContent"), true);
    } else
      throw wrongArgOpt("expectedContent", type(expectedContent), null,
          Set.of(JSONObject.class, JSONArray.class));
  }

  @Override
  protected Logger getLog() {
    return log;
  }

  /**
   * @return Either {@link JSONObject} or {@link JSONArray}.
   */
  @Override
  protected Object readContent(Path file) throws IOException {
    return JSONParser.parseJSON(doReadStringContent(file));
  }

  /**
   * @param content
   *          Either {@link JSONObject} or {@link JSONArray}.
   */
  @Override
  protected void writeContent(Path file, Object content) throws IOException {
    doWriteStringContent(file, content.toString());
  }
}
