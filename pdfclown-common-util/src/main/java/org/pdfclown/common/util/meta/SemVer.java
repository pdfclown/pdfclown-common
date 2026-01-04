/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SemVer.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.meta;

import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNullElse;
import static org.pdfclown.common.util.Exceptions.wrongState;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.isUInteger;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * <a href="https://semver.org/">Semantic version</a>.
 *
 * @param <T>
 *          Semantic version type.
 * @author Stefano Chizzolini
 */
@SuppressWarnings("rawtypes")
public abstract class SemVer<T extends SemVer<T>> implements Version<T>, Comparable<T> {
  /**
   * Version identifier.
   *
   * @author Stefano Chizzolini
   */
  public enum Id {
    MAJOR,
    MINOR,
    PATCH,
    PRERELEASE,
    METADATA
  }

  protected final int major;
  protected final int minor;
  protected final int patch;
  protected final String prerelease;

  protected SemVer(int major, int minor, int patch, @Nullable String prerelease) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.prerelease = requireNonNullElse(prerelease, EMPTY);
  }

  /**
   * {@inheritDoc}
   * <p>
   * <span class="important">IMPORTANT: DO NOT use this method for version comparison, use
   * {@link #precedence(SemVer)} instead</span> (this method produces a strict ordering, whilst
   * versioning rules may ignore certain version parts when determining version precedence).
   * </p>
   */
  @Override
  public int compareTo(T o) {
    return precedence(o);
  }

  /**
   * Gets the value corresponding to an identifier.
   */
  public Comparable get(Id id) {
    return switch (id) {
      case MAJOR -> getMajor();
      case MINOR -> getMinor();
      case PATCH -> getPatch();
      case PRERELEASE -> getPrerelease();
      case METADATA -> getMetadata();
    };
  }

  /**
   * Major identifier (<i>backward-incompatible</i> API).
   */
  public int getMajor() {
    return major;
  }

  /**
   * Build metadata.
   * <p>
   * <i>Ignored when determining version precedence</i> (that is, two versions differing only in the
   * build metadata have the same precedence).
   * </p>
   */
  public String getMetadata() {
    return EMPTY;
  }

  /**
   * {@linkplain #getMetadata() Build metadata} fields.
   */
  public List<String> getMetadataFields() {
    return List.of();
  }

  /**
   * Minor identifier (<i>backward-compatible</i> API).
   */
  public int getMinor() {
    return minor;
  }

  /**
   * Patch identifier (<i>bug fixes</i>, backward-compatible API).
   */
  public int getPatch() {
    return patch;
  }

  /**
   * Pre-release identifier.
   * <p>
   * If non-empty, indicates that the version is <i>unstable</i> and might not satisfy the intended
   * compatibility requirements as denoted by its associated normal version.
   * </p>
   */
  public String getPrerelease() {
    return prerelease;
  }

  /**
   * {@linkplain #getPrerelease() Pre-release} fields.
   *
   * @return Each field can be either a {@link String} (non-numeric field, for example
   *         {@code "alpha"}) or an {@link Integer} (numeric field, for example {@code 1}).
   */
  public abstract List<Comparable> getPrereleaseFields();

  @Override
  public boolean isRegular() {
    return prerelease.isEmpty();
  }

  /**
   * Creates a semantic version incrementing the given identifier.
   * <p>
   * Less-significant identifiers are reset ({@code 0} for numeric, otherwise empty) — for example:
   * {@code "1.2.5-alpha"} with {@code id} == {@link Id#MINOR} --&gt; {@code "1.3.0"}.
   * </p>
   *
   * @throws IllegalArgumentException
   *           if {@code id} is {@link Id#METADATA}.
   * @see #with(Id, Comparable)
   */
  public abstract T next(Id id);

  /**
   * @implNote Applies <a href="https://semver.org/spec/v2.0.0.html#spec-item-11">Semantic
   *           Versioning 2.0.0</a> algorithm, as it represents a superset of the specification.
   * @see #compareTo(SemVer)
   */
  @Override
  public int precedence(T o) {
    /*
     * [RULE 11.1] Precedence MUST be calculated by separating the version into major, minor, patch
     * and pre-release identifiers in that order (Build metadata does not figure into precedence).
     *
     * [RULE 11.2] Precedence is determined by the first difference when comparing each of these
     * identifiers from left to right as follows: major, minor, and patch versions are always
     * compared numerically.
     */
    int ret;
    if ((ret = this.major - o.major) != 0)
      // [11.2] First difference.
      return ret;

    if ((ret = this.minor - o.minor) != 0)
      // [11.2] First difference.
      return ret;

    if ((ret = this.patch - o.patch) != 0)
      // [11.2] First difference.
      return ret;

    /*
     * [RULE 11.3] When major, minor, and patch are equal, a pre-release version has lower
     * precedence than a normal version.
     */
    if (this.prerelease.isEmpty())
      return o.prerelease.isEmpty()
          ? 0 /* [11.2] First difference (none) */
          : 1 /* [11.3] Only the other version has pre-release, hence lower precedence */;
    else if (o.prerelease.isEmpty())
      // [11.3] Only this version has pre-release, hence lower precedence.
      return -1;

    /*
     * [RULE 11.4] Precedence for two pre-release versions with the same major, minor, and patch
     * version MUST be determined by comparing each dot separated identifier from left to right
     * until a difference is found as follows:
     *
     * [RULE 11.4.1] Identifiers consisting of only digits are compared numerically.
     *
     * [RULE 11.4.2] Identifiers with letters or hyphens are compared lexically in ASCII sort order.
     *
     * [RULE 11.4.3] Numeric identifiers always have lower precedence than non-numeric identifiers.
     *
     * [RULE 11.4.4] A larger set of pre-release fields has a higher precedence than a smaller set,
     * if all of the preceding identifiers are equal.
     */
    var thisPrereleaseFields = this.getPrereleaseFields();
    var oPrereleaseFields = o.getPrereleaseFields();
    for (int i = 0; i < thisPrereleaseFields.size(); i++) {
      if (oPrereleaseFields.size() == i)
        // [11.4.4] This pre-release has a larger set of fields.
        return -1;
      else if (thisPrereleaseFields.get(i).getClass() == oPrereleaseFields.get(i).getClass()) {
        // [11.4.1][11.4.2] Comparable fields (either numerical or lexical comparison).
        if ((ret = thisPrereleaseFields.get(i).compareTo(oPrereleaseFields.get(i))) != 0)
          // [11.2] First difference.
          return ret;
      } else
        // [11.4.3] The numeric pre-release has lower precedence.
        return thisPrereleaseFields.get(i) instanceof Integer ? -1 : 1;
    }
    // [11.4.4] The other pre-release has a larger set of fields.
    return thisPrereleaseFields.size() - oPrereleaseFields.size();
  }

  /**
   * Creates a semantic version with the given identifier.
   * <p>
   * Less-significant identifiers are reset ({@code 0} for numeric, otherwise empty) — for example:
   * {@code "1.2.5-alpha"} with {@code id} == {@link Id#MINOR} and {@code value} == {@code 8} --&gt;
   * {@code "1.8.0"}.
   * </p>
   *
   * @throws ClassCastException
   *           if {@code value} is incompatible with the identifier.
   * @see #next(Id)
   */
  public abstract T with(Id id, Comparable value);

  /**
   * Creates a semantic version with the given pre-release suffix.
   *
   * @param fieldIndex
   *          Index of the field where to truncate the existing pre-release before appending
   *          {@code value} ({@code -1}, to retain all the fields).
   */
  public abstract T withPrereleaseSuffix(int fieldIndex, Comparable value);

  /**
   * Gets the next pre-release.
   *
   * @return Pre-release fields.
   */
  protected List<Comparable> nextPrerelease() {
    if (isRegular())
      throw wrongState("Regular version cannot increment undefined pre-release");

    int fieldCount = getPrereleaseFields().size();
    var lastField = getPrereleaseFields().get(fieldCount - 1);
    if (lastField instanceof Integer v) {
      lastField = v + 1;
    } else {
      lastField = 1;
      fieldCount++;
    }
    var ret = new ArrayList<Comparable>(fieldCount);
    ret.addAll(getPrereleaseFields());
    if (getPrereleaseFields().size() == fieldCount) {
      ret.set(fieldCount - 1, lastField);
    } else {
      ret.add(lastField);
    }
    return ret;
  }

  /**
   * Parses a pre-release field.
   *
   * @return {@link Integer}, if {@code rawValue} consists of digits only; {@link String},
   *         otherwise.
   */
  protected Comparable<?> parsePrereleaseField(String rawValue) {
    /*
     * NOTE: It is fundamental to check unsigned integer, as a leading hyphen would represent an
     * opaque, non-numeric field.
     */
    return isUInteger(rawValue) ? parseInt(rawValue) : rawValue;
  }
}
