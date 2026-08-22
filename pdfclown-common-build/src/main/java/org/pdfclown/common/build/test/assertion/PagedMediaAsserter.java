/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PagedMediaAsserter.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static java.lang.Math.cbrt;
import static java.lang.Math.pow;
import static java.nio.file.Files.exists;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_INT_ARRAY;
import static org.pdfclown.common.build.internal.temp.util.ArgumentException.ARG_VALUE__OMITTED;
import static org.pdfclown.common.build.internal.temp.util.Conditions.requireWithinNormal;
import static org.pdfclown.common.build.internal.temp.util.Exceptions.failedIO;
import static org.pdfclown.common.build.internal.temp.util.Exceptions.wrongArg;
import static org.pdfclown.common.build.internal.temp.util.Strings.EMPTY;
import static org.pdfclown.common.build.internal.temp.util.Strings.S;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.SPACE;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Function;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import org.jspecify.annotations.Nullable;

/**
 * Automated paged-media rendering assertions.
 * <p>
 * This class enables checks over the pages of a document rendered as raster images.
 * </p>
 * <p>
 * {@linkplain #diff(BufferedImage, BufferedImage) Comparison} between actual and expected pages is
 * performed in two stages: pixel-level {@linkplain #getSampleDiffer() Euclidean distance}
 * {@linkplain #getSampleDiffTolerance() thresholding} to spot raw differences, then cluster density
 * check to weed out noise due to isolated, random rendering artifacts. The resulting diff image is
 * saved to the same directory as the actual document, along with the corresponding page images for
 * manual evaluation. Expected page images are permanently stored as PNG files among test resources,
 * along with the corresponding expected document. An expected document and its expected page images
 * are updated only if enabled via {@value Asserter#SYSTEM_PROPERTY__UPDATE_EXPECTED} system
 * property.
 * </p>
 * <p>
 * See {@link Asserter} for further information.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public abstract class PagedMediaAsserter<A extends PagedMediaAsserter.PagedMediaAdapter>
    extends Asserter {
  /**
   * {@link PagedMediaAsserter} error message builder.
   *
   * @author Stefano Chizzolini
   */
  public static class ErrorMessageBuilder extends Asserter.ErrorMessageBuilder {
    @Override
    public ErrorMessageBuilder append(Object obj) {
      return (ErrorMessageBuilder) super.append(obj);
    }

    @Override
    public ErrorMessageBuilder append(String text) {
      return (ErrorMessageBuilder) super.append(text);
    }

    @Override
    public ErrorMessageBuilder error(String text) {
      return (ErrorMessageBuilder) super.error(text);
    }

    /**
     * Begins a new page-related error entry.
     */
    public ErrorMessageBuilder pageError(int pageIndex) {
      return error("Page" + SPACE).append(pageIndex).append(S + COLON + SPACE);
    }
  }

  /**
   * Color sample difference algorithm.
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
   * {@linkplain SampleDiffer Color sample difference algorithm}s.
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
   * Document adapter for {@link PagedMediaAsserter}.
   *
   * @author Stefano Chizzolini
   */
  protected abstract static class PagedMediaAdapter implements AutoCloseable {
    private final Path file;

    protected PagedMediaAdapter(Path file) {
      this.file = file;
    }

    @Override
    public abstract void close() throws IOException;

    /**
     * Document file.
     */
    public Path getFile() {
      return file;
    }

    /**
     * Page count.
     */
    public abstract int getPageCount();

    /**
     * Renders a document page.
     *
     * @param index
     *          Page index.
     */
    public abstract BufferedImage renderPage(int index) throws IOException;
  }

  /**
   * Image type used to represent page images in memory.
   * <p>
   * Each pixel is represented by a {@linkplain BufferedImage#TYPE_INT_RGB packed big-endian integer
   * word without alpha component}.
   * </p>
   * <p>
   * <span class="important">IMPORTANT: In order to provide robust and stable image comparison, all
   * codec operations MUST stick to this data layout</span>.
   * </p>
   */
  protected static final int IMAGE_TYPE = BufferedImage.TYPE_INT_RGB;

  private static final String IMAGE_FORMAT = "png";

  private static final int DIFF_NEIGHBOR_THRESHOLD = 4;

  /**
   * Color to mark unexpected new pixel (that is, present in the actual image only).
   */
  private static final int SAMPLE_DIFF_COLOR__ADDED = Color.GREEN.getRGB();
  /**
   * Color to mark missing old pixel (that is, absent in the actual image).
   */
  private static final int SAMPLE_DIFF_COLOR__REMOVED = Color.RED.getRGB();

  /**
   * Gets the raw, non-weighted intensity of the given RGB color.
   *
   * @return {@code [0, 255 * 3]}
   */
  private static int colorIntensity(int rgb) {
    return ((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF);
  }

  private double diffTolerance = 0;
  private final Function<Path, ? extends A> documentLoader;
  private SampleDiffer sampleDiffer = SampleDiffers.OKLAB;
  private double sampleDiffTolerance = .02;

  protected PagedMediaAsserter(Function<Path, ? extends A> documentLoader) {
    this.documentLoader = documentLoader;
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
   * Sample difference algorithm to {@linkplain #diff(BufferedImage, BufferedImage) use}.
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
  public PagedMediaAsserter<A> setDiffTolerance(double value) {
    diffTolerance = requireWithinNormal(value);
    return this;
  }

  /**
   * Sets {@link #getSampleDiffer() sampleDiffer}.
   */
  public PagedMediaAsserter<A> setSampleDiffer(SampleDiffer value) {
    sampleDiffer = value;
    return this;
  }

  /**
   * Sets {@link #getSampleDiffTolerance() sampleDiffTolerance}.
   */
  public PagedMediaAsserter<A> setSampleDiffTolerance(double value) {
    sampleDiffTolerance = requireWithinNormal(value);
    return this;
  }

  /**
   * Builds the diff image of the given ones.
   * <p>
   * The resulting image shows only the mismatching pixels, based on their relative intensity (green
   * if unexpected new pixel, red if missing old pixel), against a black background.
   * </p>
   *
   * @return {@code null}, if no difference is found.
   * @throws org.pdfclown.common.util.ArgumentException
   *           if either {@code expectedImage} or {@code actualImage} are incompatible.
   */
  protected @Nullable BufferedImage diff(BufferedImage actualImage,
      BufferedImage expectedImage) {
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
      int[] retData = EMPTY_INT_ARRAY /* Just to make NullAway happy */;

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
   * Gets the ancillary image file corresponding to the given document page.
   *
   * @param baseFile
   *          Base file path to derive the image file path from, in the same directory.
   */
  protected Path getImageFile(Path baseFile, int pageIndex, String qualifier) {
    return baseFile.resolveSibling(baseFile.getFileName().toString() + DOT + pageIndex
        + (!qualifier.isEmpty() ? S + DOT + qualifier : EMPTY)
        + DOT + IMAGE_FORMAT);
  }

  /**
   * Loads the paged media from a file.
   */
  protected A loadDocument(Path file) {
    return documentLoader.apply(file);
  }

  /**
   * Loads main image from the given file.
   *
   * @return {@code null}, if {@code file} doesn't exist.
   */
  protected @Nullable BufferedImage readImage(Path file) throws IOException {
    if (!exists(file))
      return null;

    try (ImageInputStream in = ImageIO.createImageInputStream(
        file.toFile() /*
                       * IMPORTANT: ImageIO supports only `java.io.File`, NOT `java.nio.file.Path`
                       */)) {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
      if (!readers.hasNext())
        throw failedIO("Reader MISSING: {}", file);

      var reader = readers.next();
      try {
        reader.setInput(in, true, true);

        ImageReadParam param = reader.getDefaultReadParam();
        {
          param.setDestinationType(ImageTypeSpecifier.createFromBufferedImageType(IMAGE_TYPE));
        }
        return reader.read(0, param);
      } finally {
        reader.dispose();
      }
    }
  }

  /**
   * Writes a raster image suitable for stable comparison.
   *
   * @param file
   *          Image file to write to.
   * @param image
   *          Image content.
   * @implNote The image is written in lossless PNG format.
   */
  protected void writeImage(Path file, BufferedImage image) throws IOException {
    ImageIO.write(image, IMAGE_FORMAT, file.toFile());
  }

  /**
   * Writes the mismatch report for a document page.
   *
   * @param pageIndex
   *          Index of the mismatching page in {@code actualDocumentFile}.
   * @param actualDocumentFile
   *          Actual document file, used by this method to derive image file paths in the same
   *          directory.
   */
  protected void writePageMismatch(int pageIndex, Path actualDocumentFile, BufferedImage diffImage,
      BufferedImage expectedPageImage, BufferedImage actualPageImage, ErrorMessageBuilder errors) {
    errors.pageError(pageIndex).append("MISMATCH");

    try {
      // Save diff image!
      writeImage(getImageFile(actualDocumentFile, pageIndex, "DIFF"), diffImage);

      // Save expected page image!
      writeImage(getImageFile(actualDocumentFile, pageIndex, "EXPECTED"), expectedPageImage);

      // Save actual page image!
      writeImage(getImageFile(actualDocumentFile, pageIndex, "ACTUAL"), actualPageImage);

      getLog().info("Unexpected page image {} saved to {}", pageIndex,
          getImageFile(actualDocumentFile, pageIndex, "*"));
    } catch (Exception ex1) {
      getLog().warn("Unexpected page image {} save FAILED at {}", pageIndex,
          getImageFile(actualDocumentFile, pageIndex, "*"), ex1);
    }
  }
}
