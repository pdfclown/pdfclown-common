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
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.pdfclown.common.build.internal.temp.util.function.Functions.toElse;
import static org.pdfclown.common.util.Bytes.BYTE_ARRAY__EMPTY;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.LF;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Chars.UNDERSCORE;
import static org.pdfclown.common.util.Exceptions.failedIO;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.unsupported;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.io.Files.cognateFile;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.function.Function;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.function.Failable;
import org.jspecify.annotations.Nullable;

/**
 * Paged-media rendering asserter.
 * <p>
 * Checks the pages of a document rendered as raster images.
 * </p>
 * <p>
 * For efficiency, comparison between actual and expected pages is primarily done through page image
 * checksums; in case of mismatch, a pixel-level comparison is applied and the resulting
 * {@linkplain #buildDiffImage(BufferedImage, BufferedImage) diff image} is saved to the same
 * directory as the actual document, along with the corresponding page images. Expected checksums
 * are permanently stored among test resources as a list of hexadecimal values in a textual file
 * (whose name extension is {@code ".page-checksums"}), along with the corresponding expected
 * document. Both expected checksums and expected page images are rebuilt only if enabled via
 * {@value Asserter#SYSTEM_PROPERTY__UPDATE_EXPECTED} system property.
 * </p>
 * <p>
 * During a comparison, assertion errors are progressively appended to a single message; in the end,
 * if the message is not empty, it is logged as a
 * {@link org.pdfclown.common.build.system.LogManager#MARKER__VERBOSE verbose} error entry, then
 * wrapped into an {@link AssertionError} in shortened form and thrown.
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
   * List of page checksums.
   *
   * @author Stefano Chizzolini
   */
  protected static class ChecksumList extends AbstractList<byte[]> {
    private final @Nullable String[] base;

    public ChecksumList(int size) {
      this(new String[size]);
    }

    ChecksumList(@Nullable String[] base) {
      this.base = base;
    }

    /**
     * @return Empty, if undefined.
     */
    @Override
    public byte[] get(int index) {
      //noinspection DataFlowIssue : false positive null
      return toElse(base[index], Failable.asFunction(Hex::decodeHex), BYTE_ARRAY__EMPTY);
    }

    /**
     * @return {@code null} (old value is ignored)
     */
    @Override
    public byte @Nullable [] set(int index, byte[] element) {
      base[index] = encodeHexString(element, false);
      return null;
    }

    @Override
    public int size() {
      return base.length;
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
   * Image type used to represent page images in memory (each pixel is represented by a
   * little-endian 3-byte word — no alpha channel).
   * <p>
   * <span class="important">IMPORTANT: In order to provide robust and stable image comparison, all
   * codec operations MUST stick to this data layout</span>.
   * </p>
   */
  protected static final int IMAGE_TYPE = BufferedImage.TYPE_3BYTE_BGR;

  private static final String FILE_EXTENSION__CHECKSUMS = DOT + "page-checksums";

  private static final String IMAGE_FORMAT = "png";

  /**
   * Gets the ancillary image file corresponding to the given document page.
   *
   * @param baseFile
   *          Base file path to derive the image file path from.
   */
  protected static Path imageFile(Path baseFile, int pageIndex, String qualifier) {
    return cognateFile(baseFile, S + UNDERSCORE + UNDERSCORE + pageIndex
        + (!qualifier.isEmpty() ? S + DOT + qualifier : EMPTY)
        + DOT + IMAGE_FORMAT);
  }

  /**
   * Loads main image from the given file.
   *
   * @return {@code null} if {@code file} doesn't exist.
   */
  protected static @Nullable BufferedImage readImage(Path file) throws IOException {
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

  private static int colorIntensity(int rgb) {
    return ((rgb >> 16 & 0x0ff) + (rgb >> 8 & 0x0ff) + (rgb & 0x0ff)) / 3;
  }

  private final Function<Path, ? extends A> documentLoader;

  private final ThreadLocal<MessageDigest> digest = ThreadLocal.withInitial(
      () -> {
        try {
          return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
          throw runtime(ex);
        }
      });

  protected PagedMediaAsserter(Function<Path, ? extends A> documentLoader) {
    this.documentLoader = documentLoader;
  }

  /**
   * New instance inheriting the document loader from another one.
   */
  protected PagedMediaAsserter(PagedMediaAsserter<? extends A> base) {
    this(base.documentLoader);
  }

  /**
   * Generates the checksum of an image.
   *
   * @implNote MD5 is used as hashing algorithm for its balance between file corruption detection
   *           and speed.
   */
  protected byte[] buildChecksum(BufferedImage image) {
    MessageDigest digest = this.digest.get();
    digest.reset();

    DataBuffer dataBuffer = image.getRaster().getDataBuffer();
    if (!(dataBuffer instanceof DataBufferByte d))
      throw unsupported("Wrong DataBuffer type: {} (should be {})", dataBuffer.getClass(),
          DataBufferByte.class);

    return digest.digest(d.getData());
  }

  /**
   * Builds the diff image of the given ones.
   * <p>
   * The resulting image shows only the mismatching pixels, based on their relative intensity (green
   * if expected-pixel prevalence, otherwise red), against a black background.
   * </p>
   *
   * @return {@code null}, if no difference is found.
   * @throws org.pdfclown.common.util.ArgumentException
   *           if {@code expectedImage} and {@code actualImage} have mismatching sizes.
   */
  protected @Nullable BufferedImage buildDiffImage(BufferedImage expectedImage,
      BufferedImage actualImage) {
    int imageWidth = expectedImage.getWidth();
    int imageHeight = expectedImage.getHeight();
    if (actualImage.getWidth() != imageWidth || actualImage.getHeight() != imageHeight)
      throw wrongArg("actualImage", null, "MUST have the same size as expectedImage "
          + "({}x{} instead of {}x{})", imageWidth, imageHeight, actualImage.getWidth(),
          actualImage.getHeight());

    BufferedImage ret = null;
    byte[] retData = BYTE_ARRAY__EMPTY /* Just to make NullAway happy */;
    int diffExpectedRgb = Color.GREEN.getRGB();
    int diffActualRgb = Color.RED.getRGB();
    int offset = 0;
    for (int y = 0; y < imageHeight; y++) {
      for (int x = 0; x < imageWidth; x++) {
        int actualRgb = actualImage.getRGB(x, y);
        int expectedRgb = expectedImage.getRGB(x, y);
        if (actualRgb != expectedRgb) {
          /*
           * NOTE: IMAGE_TYPE is 3BYTE_BGR (each pixel is represented by a little-endian 3-byte
           * word).
           */
          if (ret == null) {
            ret = new BufferedImage(expectedImage.getWidth(), expectedImage.getHeight(),
                IMAGE_TYPE);
            retData = ((DataBufferByte) ret.getRaster().getDataBuffer()).getData();
          }

          int diffRgb = colorIntensity(expectedRgb) > colorIntensity(actualRgb)
              ? diffActualRgb
              : diffExpectedRgb;
          retData[offset++] = (byte) (diffRgb & 0x0ff);
          retData[offset++] = (byte) (diffRgb >> 8 & 0x0ff);
          retData[offset++] = (byte) (diffRgb >> 16 & 0x0ff);
        } else {
          offset += 3;
        }
      }
    }
    return ret;
  }

  /**
   * Loads the paged media from a file.
   */
  protected A loadDocument(Path file) {
    return documentLoader.apply(file);
  }

  /**
   * Reads an expected checksums resource.
   *
   * @param documentResourceName
   *          Document resource whose checksums file (named appending {@code ".page-checksums"} to
   *          it) is to read.
   * @param config
   *          Assertion configuration.
   */
  protected ChecksumList readExpectedChecksums(String documentResourceName, Config config)
      throws IOException {
    return readExpectedFile(documentResourceName + FILE_EXTENSION__CHECKSUMS,
        $ -> new ChecksumList(Files.readString($).split("\\n")), config);
  }

  /**
   * Writes an expected checksums resource.
   * <p>
   * After written to source, the resource is also copied to the target side in order to synchronize
   * ongoing tests.
   * </p>
   *
   * @param documentResourceName
   *          Document resource whose checksums file (named appending {@code ".page-checksums"} to
   *          it) is to write.
   * @param config
   *          Assertion configuration.
   */
  protected void writeExpectedChecksums(String documentResourceName, ChecksumList checksums,
      Config config) throws IOException {
    writeExpectedFile(documentResourceName + FILE_EXTENSION__CHECKSUMS, Failable.asConsumer(
        $ -> Files.writeString($, String.join(S + LF, checksums.base),
            exists($) ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.CREATE)),
        config);
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
   *          Actual document file, used by this method to derive image file paths.
   */
  protected void writePageMismatch(int pageIndex, Path actualDocumentFile, BufferedImage diffImage,
      BufferedImage expectedPageImage, BufferedImage actualPageImage, ErrorMessageBuilder errors) {
    errors.pageError(pageIndex).append("MISMATCH");

    try {
      // Save diff image!
      writeImage(imageFile(actualDocumentFile, pageIndex, "DIFF"), diffImage);

      // Save expected page image!
      writeImage(imageFile(actualDocumentFile, pageIndex, "EXPECTED"), expectedPageImage);

      // Save actual page image!
      writeImage(imageFile(actualDocumentFile, pageIndex, "ACTUAL"), actualPageImage);

      getLog().info("Unexpected page image {} saved to {}", pageIndex,
          imageFile(actualDocumentFile, pageIndex, "*"));
    } catch (Exception ex1) {
      getLog().warn("Unexpected page image {} save FAILED at {}", pageIndex,
          imageFile(actualDocumentFile, pageIndex, "*"), ex1);
    }
  }
}
