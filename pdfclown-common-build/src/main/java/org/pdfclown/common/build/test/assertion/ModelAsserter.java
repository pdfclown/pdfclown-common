/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ModelAsserter.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.pdfclown.common.build.internal.util_.Exceptions.runtime;
import static org.pdfclown.common.build.internal.util_.Objects.fqn;
import static org.pdfclown.common.build.internal.util_.Objects.sqnd;
import static org.pdfclown.common.build.internal.util_.Objects.textLiteral;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;
import static org.pdfclown.common.build.internal.util_.io.Files.cognateFile;
import static org.pdfclown.common.build.internal.util_.io.Files.extension;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import org.pdfclown.common.build.internal.util.io.Files;
import org.pdfclown.common.build.test.model.JsonArray;
import org.pdfclown.common.build.test.model.JsonElement;
import org.pdfclown.common.build.test.model.JsonObject;
import org.pdfclown.common.build.test.model.ModelComparator;
import org.pdfclown.common.build.test.model.ModelMapper;
import org.pdfclown.common.build.test.model.ModelMapper.PropertySelector;
import org.pdfclown.common.build.util.io.ResourceNames;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automated model assertions for integration testing.
 * <p>
 * This class enables massive checks over a domain model (actual object) against a resource
 * (expected object) which can be automatically updated. Comparisons are performed through an
 * {@linkplain ModelMapper abstract model}.
 * </p>
 *
 * @param <TMap>
 *          Model mapping type.
 * @param <TMapDiff>
 *          Model comparison mapping type.
 * @param <TDiff>
 *          Model comparison type.
 * @author Stefano Chizzolini
 */
public class ModelAsserter<TMap, TMapDiff, TDiff> extends Asserter {
  private static final Logger log = LoggerFactory.getLogger(ModelAsserter.class);

  protected Supplier<ModelComparator<TDiff, ? extends TMapDiff>> modelComparatorSupplier;
  protected Supplier<ModelMapper<TMapDiff>> modelDiffMapperSupplier;
  protected Supplier<ModelMapper<TMap>> modelMapperSupplier;

  /**
   * @param modelMapperSupplier
   *          Model mapper factory.
   * @param modelDiffMapperSupplier
   *          Model comparison mapper factory.
   * @param modelComparatorSupplier
   *          Model comparator factory.
   */
  public ModelAsserter(Supplier<ModelMapper<TMap>> modelMapperSupplier,
      Supplier<ModelMapper<TMapDiff>> modelDiffMapperSupplier,
      Supplier<ModelComparator<TDiff, ? extends TMapDiff>> modelComparatorSupplier) {
    this.modelMapperSupplier = modelMapperSupplier;
    this.modelDiffMapperSupplier = modelDiffMapperSupplier;
    this.modelComparatorSupplier = modelComparatorSupplier;
  }

  /**
   * Asserts that the difference between objects matches the expected one.
   *
   * @param expectedDiffResourceName
   *          Resource name of the expected object difference in serialized (JSON) form.
   * @param inputObj
   *          Input object.
   * @param outputObj
   *          Output object.
   * @param config
   *          Assertion configuration.
   * @throws AssertionError
   *           if the difference between {@code inputObj} and {@code outputObj} doesn't match the
   *           one loaded from {@code expectedDiffResourceName}.
   * @see Asserter#PARAM_NAME__UPDATE
   */
  public void assertDiffEquals(String expectedDiffResourceName, TDiff inputObj, TDiff outputObj,
      Config config) {
    // Compare the objects!
    List<? extends TMapDiff> diffs = modelComparatorSupplier.get().compare(inputObj, outputObj);

    // Map the comparison to JSON!
    JsonArray actualJsonArray = modelDiffMapperSupplier.get().mapAll(diffs);

    // Check consistency with expected comparison!
    assertEquals(expectedDiffResourceName, actualJsonArray, config);
  }

  /**
   * Asserts that an object matches the expected one.
   *
   * @param expectedObjResourceName
   *          Resource name of the expected object in serialized (JSON) form.
   * @param actualObj
   *          Actual object.
   * @param config
   *          Assertion configuration.
   * @throws AssertionError
   *           if {@code actualObj} doesn't match the one loaded from
   *           {@code expectedObjResourceName}.
   * @see Asserter#PARAM_NAME__UPDATE
   */
  public void assertEquals(String expectedObjResourceName, TMap actualObj, Config config) {
    assertEquals(expectedObjResourceName, actualObj, List.of(), config);
  }

  /**
   * Asserts that an object matches the expected one.
   *
   * @param expectedObjResourceName
   *          Resource name of the expected object in serialized (JSON) form.
   * @param actualObj
   *          Actual object.
   * @param objSelectors
   *          Property selectors for the object.
   * @param config
   *          Assertion configuration.
   * @throws AssertionError
   *           if {@code actualObj} doesn't match the one loaded from
   *           {@code expectedObjResourceName}.
   * @see Asserter#PARAM_NAME__UPDATE
   */
  public void assertEquals(String expectedObjResourceName, TMap actualObj,
      List<PropertySelector> objSelectors, Config config) {
    // Property selector consolidation.
    /*
     * NOTE: Multiple elements may be associated to the same type; since the assertion algorithm
     * peeks just a single element to filter a given type, it is necessary to normalize them before
     * submitting.
     */
    {
      var targetObjSelectors = new ArrayList<>(objSelectors) /* NOTE: Preserves original order */;
      var prevRef = new PropertySelector[1];
      objSelectors.stream()
          .sorted(Comparator.comparing($ -> $.getType().getName()))
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
    assertEquals(expectedObjResourceName, actualJsonObj, config);
  }

  /**
   * Asserts that an element matches the expected one.
   *
   * @param expectedJsonResourceName
   *          Resource name of the expected object in serialized (JSON) form.
   * @param actualJsonElement
   *          Actual object.
   * @param config
   *          Assertion configuration.
   * @throws AssertionError
   *           if {@code actualJsonElement} doesn't match the one loaded from
   *           {@code expectedJsonResourceName}.
   * @see Asserter#PARAM_NAME__UPDATE
   */
  protected void assertEquals(final String expectedJsonResourceName,
      final JsonElement actualJsonElement, final Config config) {
    final String expectedJsonResourceFqn = ResourceNames.based(
        expectedJsonResourceName, config.getTest(), true);
    final Path expectedJsonFile = config.getEnv().resourcePath(expectedJsonResourceFqn);

    final String testId = getTestId(config, () -> expectedJsonResourceFqn);

    try {
      boolean built = false;
      while (true) {
        try {
          String expectedJson = requireNonNull(Files.readString(expectedJsonFile), EMPTY);
          if (actualJsonElement instanceof JsonObject) {
            JSONAssert.assertEquals(expectedJson, (JsonObject) actualJsonElement, true);
          } else {
            JSONAssert.assertEquals(expectedJson, (JsonArray) actualJsonElement, true);
          }
          break;
        } catch (AssertionError | FileNotFoundException ex) {
          // Unrecoverable?
          if (built || !isUpdatable()) {
            Path actualJsonOutFile = null;
            if (exists(expectedJsonFile)) {
              actualJsonOutFile = config.getEnv().outputPath(
                  cognateFile(expectedJsonResourceFqn,
                      "_UNEXPECTED" + extension(expectedJsonResourceFqn, true), true));
              try {
                // Save unexpected actual object!
                createDirectories(actualJsonOutFile.getParent());
                Files.writeString(actualJsonOutFile, actualJsonElement.toString());

                log.info("Model sample {}: unexpected actual object saved to {} "
                    + "(expected object is at {})", textLiteral(expectedJsonResourceFqn),
                    textLiteral(actualJsonOutFile), textLiteral(expectedJsonFile));
              } catch (Exception ex1) {
                log.warn("Model sample {}: unexpected actual object save FAILED: {}",
                    textLiteral(expectedJsonResourceFqn), textLiteral(actualJsonOutFile), ex1);
              }
            }

            evalAssertionError(testId, ex.getMessage(), expectedJsonFile, actualJsonOutFile);
          }

          // Assertion model rebuilding.
          {
            /*
             * NOTE: In case of explicit model build request, the actual object is saved into the
             * (either mismatching or missing) expected resource (at both source and target
             * locations).
             */
            built = true;

            log.info("REBUILDING assertion model {} because of {}",
                textLiteral(expectedJsonResourceFqn), sqnd(ex));

            writeExpectedFile(expectedJsonResourceFqn, $ -> {
              try {
                Files.writeString($, actualJsonElement.toString());
              } catch (IOException ex1) {
                throw runtime(ex1);
              }
            }, config);
          }
        }
      }
    } catch (Exception ex) {
      fail(ex);
    }
  }

  @Override
  protected Logger getLog() {
    return log;
  }
}
