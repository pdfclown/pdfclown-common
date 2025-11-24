/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SemVer.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.meta;

import static java.lang.Integer.parseInt;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.pdfclown.common.build.internal.util_.Chars.DOT;
import static org.pdfclown.common.build.internal.util_.Chars.HYPHEN;
import static org.pdfclown.common.build.internal.util_.Chars.PLUS;
import static org.pdfclown.common.build.internal.util_.Exceptions.unexpected;
import static org.pdfclown.common.build.internal.util_.Exceptions.wrongArg;
import static org.pdfclown.common.build.internal.util_.Strings.EMPTY;
import static org.pdfclown.common.build.internal.util_.Strings.S;
import static org.pdfclown.common.build.internal.util_.Strings.isUInteger;
import static org.pdfclown.common.build.internal.util_.regex.Patterns.indexOfMatchFailure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.internal.util_.ArgumentFormatException;
import org.pdfclown.common.build.internal.util_.annot.Immutable;
import org.pdfclown.common.build.internal.util_.annot.LazyNonNull;

/**
 * <a href="https://semver.org/spec/v2.0.0.html">Semantic Version (2.0.0)</a>.
 *
 * @author Stefano Chizzolini
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Immutable
public class SemVer implements Comparable<SemVer> {
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

  private static final String PATTERN_GROUP__MAJOR = "major";
  private static final String PATTERN_GROUP__METADATA = "metadata";
  private static final String PATTERN_GROUP__MINOR = "minor";
  private static final String PATTERN_GROUP__PATCH = "patch";
  private static final String PATTERN_GROUP__PRERELEASE = "prerelease";

  /**
   * <a href=
   * "https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string">Official
   * Semantic Versioning 2.0.0 regular expression</a>.
   */
  private static final Pattern PATTERN__SEM_VER = Pattern.compile("""
      ^\
      (?<%s>0|[1-9]\\d*)\\.\
      (?<%s>0|[1-9]\\d*)\\.\
      (?<%s>0|[1-9]\\d*)\
      (?:-(?<%s>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)\
      (?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?\
      (?:\\+(?<%s>[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?\
      $""".formatted(
      PATTERN_GROUP__MAJOR,
      PATTERN_GROUP__MINOR,
      PATTERN_GROUP__PATCH,
      PATTERN_GROUP__PRERELEASE,
      PATTERN_GROUP__METADATA));

  /**
   * Checks whether the version conforms to <a href="https://semver.org/spec/v2.0.0.html">Semantic
   * Versioning 2.0.0</a>.
   *
   * @return {@code version}.
   * @throws IllegalArgumentException
   *           if {@code version} is not a valid semantic version.
   */
  public static String check(String version) {
    Matcher m = PATTERN__SEM_VER.matcher(version);
    if (!m.find())
      throw wrongArg("version", version);

    return version;
  }

  /**
   * @throws ArgumentFormatException
   *           if {@code value} is not a valid semantic version.
   */
  public static SemVer of(String value) {
    Matcher m = PATTERN__SEM_VER.matcher(value);
    if (!m.find())
      throw new ArgumentFormatException(null, value, indexOfMatchFailure(m));

    return new SemVer(
        parseInt(m.group(PATTERN_GROUP__MAJOR)),
        parseInt(m.group(PATTERN_GROUP__MINOR)),
        parseInt(m.group(PATTERN_GROUP__PATCH)),
        m.group(PATTERN_GROUP__PRERELEASE),
        m.group(PATTERN_GROUP__METADATA));
  }

  /**
  */
  public static SemVer of(int major, int minor, int patch, @Nullable String prerelease,
      @Nullable String metadata) {
    /*
     * NOTE: Validation is centralized to regex, so we have to serialize the version components.
     */
    var b = new StringBuilder();
    var offsets = new ArrayList<Integer>(5);
    for (int i = 0; i < 5; i++) {
      offsets.add(b.length());
      switch (i) {
        case 0:
          b.append(major).append(DOT);
          break;
        case 1:
          b.append(minor).append(DOT);
          break;
        case 2:
          b.append(patch);
          break;
        case 3:
          if (!isEmpty(prerelease)) {
            b.append(HYPHEN).append(prerelease);
          }
          break;
        case 4:
          if (!isEmpty(metadata)) {
            b.append(PLUS).append(metadata);
          }
          break;
        default:
          throw unexpected(i);
      }
    }
    try {
      return of(b.toString());
    } catch (ArgumentFormatException ex) {
      for (int i = 0; i < offsets.size(); i++) {
        if (offsets.get(i) > ex.getOffset()) {
          switch (i - 1) {
            case 0:
              throw wrongArg("major", major);
            case 1:
              throw wrongArg("minor", minor);
            case 2:
              throw wrongArg("patch", patch);
            case 3:
              throw wrongArg("prerelease", prerelease);
            default:
              throw unexpected(i - 1);
          }
        }
      }
      throw wrongArg("metadata", metadata);
    }
  }

  private final int major;
  private final String metadata;
  private transient @LazyNonNull @Nullable List<String> metadataFields;
  private final int minor;
  private final int patch;
  private final String prerelease;
  private transient @LazyNonNull @Nullable List<Comparable> prereleaseFields;

  SemVer(int major, int minor, int patch, @Nullable String prerelease, @Nullable String metadata) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.prerelease = requireNonNullElse(prerelease, EMPTY);
    this.metadata = requireNonNullElse(metadata, EMPTY);
  }

  /**
   * {@inheritDoc}
   * <p>
   * <span class="important">IMPORTANT: DO NOT use this method for semantic versioning comparison,
   * use {@link #precedence(SemVer)} instead</span> (this method produces a strict ordering, whilst
   * semantic versioning ignores {@linkplain #getMetadata() build metadata} when determining version
   * precedence).
   * </p>
   */
  @Override
  public int compareTo(SemVer o) {
    int precedence = precedence(o);
    return precedence == 0 ? this.metadata.compareTo(o.metadata) : precedence;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass())
      return false;

    SemVer that = (SemVer) o;
    return this.major == that.major && this.minor == that.minor && this.patch == that.patch
        && Objects.equals(this.prerelease, that.prerelease)
        && Objects.equals(this.metadata, that.metadata);
  }

  /**
   * Gets the value corresponding to an identifier.
   */
  public Comparable get(Id id) {
    return switch (id) {
      case MAJOR -> major;
      case MINOR -> minor;
      case PATCH -> patch;
      case PRERELEASE -> prerelease;
      case METADATA -> metadata;
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
    return metadata;
  }

  /**
   * {@linkplain #getMetadata() Build metadata} fields.
   */
  public @Nullable List<String> getMetadataFields() {
    if (metadataFields == null) {
      metadataFields = metadata.isEmpty() ? List.of() : List.of(metadata.split("\\."));
    }
    return metadataFields;
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
   * @return Each field can be either a {@link String} (non-numeric, for example {@code "alpha"}) or
   *         an {@link Integer} (numeric, for example {@code 1}).
   */
  @SuppressWarnings("rawtypes")
  public List<Comparable> getPrereleaseFields() {
    if (prereleaseFields == null) {
      if (prerelease.isEmpty()) {
        prereleaseFields = List.of();
      } else {
        String[] rawFields = prerelease.split("\\.");
        var prereleaseFields = new ArrayList<Comparable>(rawFields.length);
        for (var rawField : rawFields) {
          /*
           * NOTE: It is fundamental to check unsigned integer, as a leading hyphen may represent an
           * opaque, non-numeric character only.
           */
          prereleaseFields.add(isUInteger(rawField) ? parseInt(rawField) : rawField);
        }
        this.prereleaseFields = unmodifiableList(prereleaseFields);
      }
    }
    return prereleaseFields;
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor, patch, prerelease, metadata);
  }

  /**
   * Whether this version is stable, that is not a pre-release.
   */
  public boolean isStable() {
    return prerelease.isEmpty();
  }

  /**
   * Creates a semantic version incrementing the given identifier.
   * <p>
   * Less-significant identifiers are reset ({@code 0} for numeric, otherwise empty) — for example:
   * {@code "1.2.5-alpha"} with {@code id} == {@link Id#MINOR} --&gt; {@code "1.3.0"}.
   * </p>
   * <p>
   * If {@code id} == {@link Id#PRERELEASE} and its last field is non-numeric, an additional numeric
   * field is initialized to 1 — for example, {@code "1.0.0-alpha"} --&gt; {@code "1.0.0-alpha.1"}.
   * </p>
   *
   * @throws IllegalArgumentException
   *           if {@code id} is {@link Id#METADATA}.
   * @see #with(Id, Comparable)
   */
  public SemVer next(Id id) {
    switch (id) {
      case MAJOR:
        return new SemVer(major + 1, 0, 0, EMPTY, EMPTY);
      case MINOR:
        return new SemVer(major, minor + 1, 0, EMPTY, EMPTY);
      case PATCH:
        return new SemVer(major, minor, patch + 1, EMPTY, EMPTY);
      case PRERELEASE:
        if (isStable())
          throw wrongArg("id", id, "Stable version cannot increment undefined pre-release");

        int fieldCount = getPrereleaseFields().size();
        var lastField = getPrereleaseFields().get(fieldCount - 1);
        if (lastField instanceof Integer i) {
          lastField = i + 1;
        } else {
          lastField = 1;
          fieldCount++;
        }
        var newFields = new ArrayList<Comparable>(fieldCount);
        newFields.addAll(getPrereleaseFields());
        if (getPrereleaseFields().size() == fieldCount) {
          newFields.set(fieldCount - 1, lastField);
        } else {
          newFields.add(lastField);
        }
        return new SemVer(major, minor, patch,
            newFields.stream().map(Object::toString).collect(joining(S + DOT)), EMPTY);
      case METADATA:
        throw wrongArg("id", id);
      default:
        throw unexpected(id);
    }
  }

  /**
   * Compares this object with the given one for
   * <a href="https://semver.org/spec/v2.0.0.html#spec-item-11">Semantic Versioning 2.0.0
   * precedence</a>.
   *
   * @see #compareTo(SemVer)
   */
  public int precedence(SemVer o) {
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

  @Override
  public String toString() {
    var b = new StringBuilder().append(major).append(DOT).append(minor).append(DOT).append(patch);
    if (!prerelease.isEmpty()) {
      b.append(HYPHEN).append(prerelease);
    }
    if (!metadata.isEmpty()) {
      b.append(PLUS).append(metadata);
    }
    return b.toString();
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
  public SemVer with(Id id, Comparable value) {
    return switch (id) {
      case MAJOR -> new SemVer((Integer) value, 0, 0, EMPTY, EMPTY);
      case MINOR -> new SemVer(major, (Integer) value, 0, EMPTY, EMPTY);
      case PATCH -> new SemVer(major, minor, (Integer) value, EMPTY, EMPTY);
      case PRERELEASE -> new SemVer(major, minor, patch, (String) value, EMPTY);
      case METADATA -> new SemVer(major, minor, patch, prerelease, (String) value);
    };
  }
}
