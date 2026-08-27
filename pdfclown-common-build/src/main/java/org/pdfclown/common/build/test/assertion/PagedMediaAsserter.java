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

import static java.nio.file.Files.exists;
import static org.pdfclown.common.build.internal.temp.util.Exceptions.failedIO;
import static org.pdfclown.common.build.internal.temp.util.Strings.EMPTY;
import static org.pdfclown.common.build.internal.temp.util.Strings.S;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.SPACE;

import java.awt.image.BufferedImage;
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
import org.pdfclown.common.build.test.assertion.media.ImageDiffer;
import org.pdfclown.common.build.test.assertion.media.PageImageDiffer;

/**
 * Paged-media rendering asserter.
 * <p>
 * Checks the pages of a document rendered as raster images.
 * </p>
 * <p>
 * If the {@linkplain #diff(BufferedImage, BufferedImage) comparison} between actual and expected
 * pages mismatches, the resulting diff image is saved for manual evaluation to the same directory
 * as the actual document, along with the corresponding page images.
 * </p>
 * <p>
 * Expected page images may optionally be permanently stored as
 * {@linkplain #getImageFile(Path, int, String) PNG files} among test resources (useful when the
 * renderer itself is under testing), along with the corresponding expected document. An expected
 * document and its expected page images are updated only if enabled via
 * {@value Asserter#SYSTEM_PROPERTY__UPDATE_EXPECTED} system property.
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
   * For more information, see {@link PageImageDiffer#IMAGE_TYPE}.
   * </p>
   */
  protected static final int IMAGE_TYPE = PageImageDiffer.IMAGE_TYPE;

  private static final String IMAGE_FORMAT = "png";

  private final Function<Path, ? extends A> documentLoader;
  private final PageImageDiffer imageDiffer;

  protected PagedMediaAsserter(Function<Path, ? extends A> documentLoader) {
    this(documentLoader, new PageImageDiffer());
  }

  protected PagedMediaAsserter(Function<Path, ? extends A> documentLoader,
      PageImageDiffer imageDiffer) {
    this.documentLoader = documentLoader;
    this.imageDiffer = imageDiffer;
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
    return imageDiffer.getDiffTolerance();
  }

  /**
   * Sample comparator.
   */
  public ImageDiffer.SampleDiffer getSampleDiffer() {
    return imageDiffer.getSampleDiffer();
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
    return imageDiffer.getSampleDiffTolerance();
  }

  /**
   * Sets {@link #getDiffTolerance() diffTolerance}.
   */
  public PagedMediaAsserter<A> setDiffTolerance(double value) {
    imageDiffer.setDiffTolerance(value);
    return this;
  }

  /**
   * Sets {@link #getSampleDiffer() sampleDiffer}.
   */
  public PagedMediaAsserter<A> setSampleDiffer(ImageDiffer.SampleDiffer value) {
    imageDiffer.setSampleDiffer(value);
    return this;
  }

  /**
   * Sets {@link #getSampleDiffTolerance() sampleDiffTolerance}.
   */
  public PagedMediaAsserter<A> setSampleDiffTolerance(double value) {
    imageDiffer.setSampleDiffTolerance(value);
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
  protected final @Nullable BufferedImage diff(BufferedImage actualImage,
      BufferedImage expectedImage) {
    return imageDiffer.diff(actualImage, expectedImage);
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
