/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PageImageDiffer.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion.media;

import static org.pdfclown.common.build.internal.temp.util.ArgumentException.ARG_VALUE__OMITTED;
import static org.pdfclown.common.build.internal.temp.util.Conditions.requireWithinNormal;
import static org.pdfclown.common.build.internal.temp.util.Exceptions.wrongArg;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;

/**
 * Page image comparator.
 * <p>
 * {@linkplain #diff(BufferedImage, BufferedImage) Comparison} between actual and expected images is
 * performed in two stages:
 * </p>
 * <ol>
 * <li>pixel-level {@linkplain #getSampleDiffer() Euclidean distance}
 * {@linkplain #getSampleDiffTolerance() thresholding} (to spot raw differences)</li>
 * <li>cluster density check (to weed out noise due to isolated, random rendering artifacts)</li>
 * </ol>
 *
 * @author Stefano Chizzolini
 */
public class PageImageDiffer extends ImageDiffer {
  /**
   * Image type used to represent images in memory.
   * <p>
   * Each pixel is represented by a {@linkplain BufferedImage#TYPE_INT_RGB packed big-endian integer
   * word without alpha component}.
   * </p>
   * <p>
   * <span class="important">IMPORTANT: In order to provide robust and stable image comparison, all
   * codec operations MUST stick to this data layout</span>.
   * </p>
   */
  public static final int IMAGE_TYPE = BufferedImage.TYPE_INT_RGB;

  /**
   * Color to mark unexpected new pixel (that is, present in the actual image only).
   */
  private static final int SAMPLE_DIFF_COLOR__ADDED = Color.GREEN.getRGB();
  /**
   * Color to mark missing old pixel (that is, absent in the actual image).
   */
  private static final int SAMPLE_DIFF_COLOR__REMOVED = Color.RED.getRGB();

  private static final int DIFF_NEIGHBOR_THRESHOLD = 4;

  /**
   * Gets the raw, non-weighted intensity of the given RGB color.
   *
   * @return {@code [0, 255 * 3]}
   */
  private static int colorIntensity(int rgb) {
    return ((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF);
  }

  private double diffTolerance = 0;
  private SampleDiffer sampleDiffer = SampleDiffers.OKLAB;
  private double sampleDiffTolerance = .02;

  /**
   * {@inheritDoc}
   * <p>
   * The resulting image shows only the mismatching pixels, based on their relative intensity (green
   * if unexpected new pixel, red if missing old pixel), against a black background.
   * </p>
   */
  @Override
  public @Nullable BufferedImage diff(BufferedImage actualImage, BufferedImage expectedImage) {
    if (actualImage.getType() != IMAGE_TYPE || expectedImage.getType() != IMAGE_TYPE)
      throw wrongArg(actualImage.getType() != IMAGE_TYPE ? "actualImage" : "expectedImage",
          ARG_VALUE__OMITTED, "type MUST be {} -- see `BufferedImage.TYPE_*`", IMAGE_TYPE);

    int imageWidth = expectedImage.getWidth();
    int imageHeight = expectedImage.getHeight();
    if (actualImage.getWidth() != imageWidth || actualImage.getHeight() != imageHeight)
      throw wrongArg("actualImage", ARG_VALUE__OMITTED, "size MUST be the same as `expectedImage` "
          + "-- {}x{}", imageWidth, imageHeight);

    BufferedImage ret = null;
    int sampleDiffCount = 0;
    {
      int[] retData = ArrayUtils.EMPTY_INT_ARRAY /* Just to make NullAway happy */;

      /*
       * 1. Building the diff mask...
       *
       * NOTE: Each sample is evaluated for divergence with the diffing algorithm against the noise
       * threshold.
       */
      {
        final Raster actualRaster = actualImage.getRaster();
        final Raster expectedRaster = expectedImage.getRaster();
        final var actualRowData = new int[imageWidth];
        final var expectedRowData = new int[imageWidth];
        final double sampleDiffThreshold = sampleDiffer.resolve(sampleDiffTolerance);
        int retOffset = 0;
        for (int y = 0; y < imageHeight; y++) {
          actualRaster.getDataElements(0, y, imageWidth, 1, actualRowData);
          expectedRaster.getDataElements(0, y, imageWidth, 1, expectedRowData);
          for (int i = 0; i < imageWidth; i++) {
            // Actual sample diverges?
            if (sampleDiffer.diff(actualRowData[i], expectedRowData[i]) > sampleDiffThreshold) {
              if (ret == null) {
                ret = new BufferedImage(imageWidth, imageHeight, IMAGE_TYPE);
                retData = ((DataBufferInt) ret.getRaster().getDataBuffer()).getData();
              }

              /*
               * NOTE: Mimicking a subtractive-color surface, brighter colors approximate to the
               * white of a blank page, whilst dimmer colors approximate to the black of an
               * impressed page. Therefore, an actual sample dimmer than expected is considered
               * added information; conversely, an actual sample brighter than expected is
               * considered removed information.
               */
              retData[retOffset] =
                  colorIntensity(actualRowData[i]) < colorIntensity(expectedRowData[i])
                      ? SAMPLE_DIFF_COLOR__ADDED
                      : SAMPLE_DIFF_COLOR__REMOVED;
            }
            retOffset++;
          }
        }
      }
      /*
       * 2. Filtering noise out of the diff mask...
       *
       * NOTE: Each diff sample is evaluated against its neighbors, to weed out random, isolated,
       * noisy samples (false positives).
       */
      if (ret != null) {
        for (int y = 2; y < imageHeight - 2; y++) {
          for (int x = 2, limit = imageWidth - 2; x < limit; x++) {
            if (retData[y * imageWidth + x] == 0) {
              continue;
            }

            // Evaluating diff density over a 5x5 cluster...
            int diffNeighborCount = 0;
            neighborhoodLoop: for (int ny = -2; ny <= 2; ny++) {
              for (int nx = -2, nLimit = 2; nx <= nLimit; nx++) {
                // Diff density beyond noise level?
                if (retData[(y + ny) * imageWidth + (x + nx)] != 0
                    && ++diffNeighborCount >= DIFF_NEIGHBOR_THRESHOLD) {
                  sampleDiffCount++;
                  break neighborhoodLoop;
                }
              }
            }
          }
        }
      }
    }
    return sampleDiffCount / (double) (imageWidth * imageHeight) > diffTolerance ? ret : null;
  }

  /**
   * Maximum overall divergence acceptable.
   * <p>
   * Represents the {@linkplain #getSampleDiffTolerance() divergent samples} count threshold beyond
   * which images are considered different.
   * </p>
   *
   * @return {@code [0,1]}
   */
  public double getDiffTolerance() {
    return diffTolerance;
  }

  /**
   * Sample comparator.
   */
  public SampleDiffer getSampleDiffer() {
    return sampleDiffer;
  }

  /**
   * Maximum sample divergence acceptable.
   * <p>
   * Represents the normalized color distance threshold beyond which samples are considered
   * different.
   * </p>
   *
   * @return {@code [0,1]}
   */
  public double getSampleDiffTolerance() {
    return sampleDiffTolerance;
  }

  /**
   * Sets {@link #getDiffTolerance() diffTolerance}.
   */
  public PageImageDiffer setDiffTolerance(double value) {
    diffTolerance = requireWithinNormal(value);
    return this;
  }

  /**
   * Sets {@link #getSampleDiffer() sampleDiffer}.
   */
  public PageImageDiffer setSampleDiffer(SampleDiffer value) {
    sampleDiffer = value;
    return this;
  }

  /**
   * Sets {@link #getSampleDiffTolerance() sampleDiffTolerance}.
   */
  public PageImageDiffer setSampleDiffTolerance(double value) {
    sampleDiffTolerance = requireWithinNormal(value);
    return this;
  }
}
