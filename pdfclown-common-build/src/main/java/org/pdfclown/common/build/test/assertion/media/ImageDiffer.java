/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ImageDiffer.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion.media;

import static java.lang.Math.cbrt;
import static java.lang.Math.pow;

import java.awt.image.BufferedImage;
import org.jspecify.annotations.Nullable;

/**
 * Image comparator.
 *
 * @author Stefano Chizzolini
 */
public abstract class ImageDiffer {
  /**
   * Color sample comparator.
   *
   * @author Stefano Chizzolini
   */
  public interface SampleDiffer {
    /**
     * Calculates the squared Euclidean distance between two color samples.
     *
     * @param rgb1
     *          Sample 1.
     * @param rgb2
     *          Sample 2.
     * @return Squared Euclidean distance.
     */
    double diff(int rgb1, int rgb2);

    /**
     * Gets the squared scaled distance corresponding to the given normal.
     *
     * @param normal
     *          Normal distance ({@code [0,1]}).
     * @return Squared scaled distance.
     * @implNote Each color distance algorithm has its own range; this method provides a convenient
     *           transformation to even out implementation details.
     */
    double resolve(double normal);
  }

  /**
   * {@linkplain SampleDiffer Color sample comparator}s.
   *
   * @author Stefano Chizzolini
   */
  public static final class SampleDiffers {
    /**
     * Oklab Euclidean distance.
     * <p>
     * Oklab color space provides superior perceptual uniformity compared to YIQ and older spaces
     * like CIELAB.
     * </p>
     * <p>
     * Slower than {@linkplain #YIQ YIQ-weighted perceptual distance}, but more accurate.
     * </p>
     *
     * @see <a href="https://bottosson.github.io/posts/oklab/">Oklab, a perceptual color space for
     *      image processing — Björn Ottosson</a>
     */
    public static final SampleDiffer OKLAB = new SampleDiffer() {
      // SPDX-SnippetBegin
      // SPDX-SnippetCopyrightText: 2020 Björn Ottosson
      // SPDX-License-Identifier: MIT
      //
      // Source: https://bottosson.github.io/posts/oklab/#converting-from-linear-srgb-to-oklab
      // SourceName: linear_srgb_to_oklab
      // Changes: integrates convertion to linear sRGB (`sRgbToLinear`)
      /**
       * Converts an sRGB sample to Oklab color space.
       *
       * @see <a href=
       *      "https://bottosson.github.io/posts/oklab/#converting-from-linear-srgb-to-oklab">Converting
       *      from linear sRGB to Oklab — Björn Ottosson</a>
       */
      private static double[] rgbToOklab(int rgb) {
        // sRGB -> Linear sRGB (undo sRGB gamma encoding)
        double lr = sRgbToLinear(((rgb >> 16) & 0xFF) / 255d);
        double lg = sRgbToLinear(((rgb >> 8) & 0xFF) / 255d);
        double lb = sRgbToLinear((rgb & 0xFF) / 255d);

        // Linear sRGB -> LMS' (non-linear cone space)
        double l_ = cbrt(0.4122214708 * lr + 0.5363325363 * lg + 0.0514459929 * lb);
        double m_ = cbrt(0.2119034982 * lr + 0.6806995451 * lg + 0.1073969566 * lb);
        double s_ = cbrt(0.0883024619 * lr + 0.2817188376 * lg + 0.6299787005 * lb);

        // LMS' -> Oklab
        return new double[] {
            0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_,
            1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_,
            0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_
        };
      }
      // SPDX-SnippetEnd

      // SPDX-SnippetBegin
      // SPDX-SnippetCopyrightText: 2020 Björn Ottosson
      // SPDX-License-Identifier: MIT
      //
      // Source: https://bottosson.github.io/posts/colorwrong/#what-can-we-do%3F
      // SourceName: f_inv
      /**
       * Converts an sRGB channel value to linear.
       *
       * @param c
       *          ({@code [0,1]}) Normal sRGB channel value.
       * @see <a href="https://bottosson.github.io/posts/colorwrong/#what-can-we-do%3F">Linear
       *      version of sRGB by applying the inverse of the sRGB nonlinear transform function —
       *      Björn Ottosson</a>
       */
      private static double sRgbToLinear(double c) {
        return c >= 0.04045 ? pow((c + 0.055) / 1.055, 2.4) : c / 12.92;
      }
      // SPDX-SnippetEnd

      @Override
      public double diff(int rgb1, int rgb2) {
        // Convert sRGB to Oklab!
        double[] lab1 = rgbToOklab(rgb1);
        double[] lab2 = rgbToOklab(rgb2);

        // Calculate deltas across Oklab channels!
        double dL = lab1[0] - lab2[0];
        double dA = lab1[1] - lab2[1];
        double dB = lab1[2] - lab2[2];

        return dL * dL + dA * dA + dB * dB;
      }

      /**
       * @implNote Pass-through: Oklab range corresponds to normal for standard sRGB gamut.
       */
      @Override
      public double resolve(double normal) {
        return normal;
      }
    };

    /**
     * YIQ-weighted perceptual distance.
     * <p>
     * Faster than {@linkplain #OKLAB Oklab Euclidean distance}, but less accurate.
     * </p>
     *
     * @see <a href=
     *      "https://riaa.uaem.mx/xmlui/bitstream/handle/20.500.12055/91/progmat222010Measuring.pdf?sequence=1">Measuring
     *      perceived color difference using YIQ NTSC transmission color space in mobile
     *      applications — Yuriy Kotsarenko, Fernando Ramos</a>
     */
    public static final SampleDiffer YIQ = new SampleDiffer() {
      @Override
      public double diff(int rgb1, int rgb2) {
        // Calculate deltas across RGB channels!
        int dR = ((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF);
        int dG = ((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF);
        int dB = (rgb1 & 0xFF) - (rgb2 & 0xFF);

        /*
         * Convert RGB deltas to YIQ!
         *
         * NOTE: Standard NTSC transmission coefficients.
         */
        double dY = dR * 0.2989 + dG * 0.5870 + dB * 0.1140;
        double dI = dR * 0.5959 - dG * 0.2744 - dB * 0.3216;
        double dQ = dR * 0.2115 - dG * 0.5229 + dB * 0.3114;

        /*
         * Calculate weighted Euclidean distance!
         *
         * NOTE: Y (Luminance) is heavily weighted (~0.5) because human contrast sensitivity depends
         * mostly on brightness.
         */
        return dY * dY * 0.5053 + dI * dI * 0.3558 + dQ * dQ * 0.1389;
      }

      @Override
      public double resolve(double normal) {
        return 35215 /*-
                      * Maximum squared YIQ distance (as implemented in
                      * <https://github.com/mapbox/pixelmatch/blob/9faed09302aaecec130b4ce0e8505d5ed5221393/index.js#L58>)
                      */
            * normal * normal /* Squared normal distance */;
      }
    };

    /**
     * Raw, non-perceptual distance.
     * <p>
     * Very fast, but fragile.
     * </p>
     */
    public static final SampleDiffer RAW = new SampleDiffer() {
      @Override
      public double diff(int rgb1, int rgb2) {
        // Calculate deltas across RGB channels!
        int dR = ((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF);
        int dG = ((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF);
        int dB = (rgb1 & 0xFF) - (rgb2 & 0xFF);

        return dR * dR + dG * dG + dB * dB;
      }

      @Override
      public double resolve(double normal) {
        return 0xFF * 0xFF * 3 /*
                                * Maximum squared raw distance (squared component magnitude times
                                * component count)
                                */
            * normal * normal /* Squared normal distance */;
      }
    };

    private SampleDiffers() {
    }
  }

  /**
   * Builds the diff image of the given ones.
   *
   * @return {@code null}, if no difference is found.
   * @throws org.pdfclown.common.util.ArgumentException
   *           if either {@code expectedImage} or {@code actualImage} are incompatible.
   */
  public abstract @Nullable BufferedImage diff(BufferedImage actualImage,
      BufferedImage expectedImage);
}
