/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Securities.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.security;

import static org.pdfclown.common.util.Objects.textLiteral;

import org.apache.commons.lang3.function.FailableBiConsumer;

/**
 * Security utilities.
 *
 * @author Stefano Chizzolini
 */
public class Securities {
  /**
   * Security feature applier.
   *
   * @param <T>
   *          Value type.
   * @author Stefano Chizzolini
   */
  @FunctionalInterface
  public interface FeatureApplier<T> extends FailableBiConsumer<String, T, Exception> {
  }

  // SPDX-SnippetBegin
  // SPDX-SnippetCopyrightText: 2016 Patrick Wright and others
  // SPDX-License-Identifier: LGPL-3.0-only
  //
  // Source: https://github.com/danfickle/openhtmltopdf/blob/780ba564839f1ad5abfa5df12e4aebb9dd6782d2/openhtmltopdf-core/src/main/java/com/openhtmltopdf/resource/XMLResource.java#L131
  // SourceName: com.openhtmltopdf.resource.XMLResource.trySetFeature(..)
  // Changes:
  // - `SetFeature` parameter type replaced by `FeatureApplier`
  // - in case of unsupported feature, fails fast instead of logging
  /**
   * Applies a security feature.
   *
   * @param name
   *          Feature name.
   * @param value
   *          Feature value.
   * @param applier
   *          Applies the feature.
   * @throws SecurityFeatureException
   *           if the feature failed to get applied.
   */
  public static <T> void applyFeature(String name, T value, FeatureApplier<T> applier)
      throws SecurityFeatureException {
    try {
      applier.accept(name, value);
    } catch (Exception ex) {
      throw new SecurityFeatureException("Security feature %s activation FAILED"
          .formatted(textLiteral(name)), ex);
    }
  }
  // SPDX-SnippetEnd
}
