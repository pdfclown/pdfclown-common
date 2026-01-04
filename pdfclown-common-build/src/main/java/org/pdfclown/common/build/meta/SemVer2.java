/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SemVer2.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.meta;

import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.HYPHEN;
import static org.pdfclown.common.util.Chars.PLUS;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.regex.Patterns.indexOfMatchFailure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.stream.Streams;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.ArgumentFormatException;
import org.pdfclown.common.util.annot.Derived;
import org.pdfclown.common.util.annot.Immutable;

/**
 * <a href="https://semver.org/spec/v2.0.0.html">Semantic Version 2.0.0</a>.
 *
 * @author Stefano Chizzolini
 */
@Immutable
@SuppressWarnings("rawtypes")
public class SemVer2 extends SemVer<SemVer2> {
  private static final String PATTERN_GROUP__MAJOR = "major";
  private static final String PATTERN_GROUP__METADATA = "metadata";
  private static final String PATTERN_GROUP__MINOR = "minor";
  private static final String PATTERN_GROUP__PATCH = "patch";
  private static final String PATTERN_GROUP__PRERELEASE = "prerelease";

  /**
   * <a href=
   * "https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string">Official
   * Semantic Versioning 2.0 regular expression</a>.
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
   * Versioning 2.0</a>.
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
  public static SemVer2 of(String value) {
    Matcher m = PATTERN__SEM_VER.matcher(value);
    if (!m.find())
      throw new ArgumentFormatException(null, value, indexOfMatchFailure(m));

    return new SemVer2(
        parseInt(m.group(PATTERN_GROUP__MAJOR)),
        parseInt(m.group(PATTERN_GROUP__MINOR)),
        parseInt(m.group(PATTERN_GROUP__PATCH)),
        m.group(PATTERN_GROUP__PRERELEASE),
        m.group(PATTERN_GROUP__METADATA));
  }

  /**
  */
  public static SemVer2 of(int major, int minor, int patch, @Nullable String prerelease,
      @Nullable String metadata) {
    /*
     * NOTE: Validation is based on regex, so we have to serialize the version components, keeping
     * track of their offsets.
     */
    var b = new StringBuilder();
    var offsets = new int[5];
    for (int i = 0; i < 5; i++) {
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
      offsets[i] = b.length();
    }
    try {
      return of(b.toString());
    } catch (ArgumentFormatException ex) {
      for (int i = 0; i < offsets.length; i++) {
        if (offsets[i] > ex.getOffset()) {
          switch (i) {
            case 0:
              throw wrongArg("major", major);
            case 1:
              throw wrongArg("minor", minor);
            case 2:
              throw wrongArg("patch", patch);
            case 3:
              throw wrongArg("prerelease", prerelease);
            case 4:
              throw wrongArg("metadata", metadata);
            default:
              // NOP
          }
        }
      }
      throw unexpected("Invalid offset not matched");
    }
  }

  private final String metadata;
  private transient @Derived @Nullable List<String> metadataFields;
  private transient @Derived @Nullable List<Comparable> prereleaseFields;

  SemVer2(int major, int minor, int patch, @Nullable String prerelease, @Nullable String metadata) {
    super(major, minor, patch, prerelease);
    this.metadata = requireNonNullElse(metadata, EMPTY);
  }

  @Override
  public int compareTo(SemVer2 o) {
    int precedence = precedence(o);
    return precedence == 0 ? this.metadata.compareTo(o.metadata) : precedence;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass())
      return false;

    var that = (SemVer2) o;
    return this.major == that.major && this.minor == that.minor && this.patch == that.patch
        && Objects.equals(this.prerelease, that.prerelease)
        && Objects.equals(this.metadata, that.metadata);
  }

  @Override
  public String getMetadata() {
    return metadata;
  }

  @Override
  public List<String> getMetadataFields() {
    if (metadataFields == null) {
      metadataFields = metadata.isEmpty() ? List.of() : List.of(metadata.split("\\."));
    }
    return metadataFields;
  }

  @Override
  public List<Comparable> getPrereleaseFields() {
    if (prereleaseFields == null) {
      prereleaseFields = prerelease.isEmpty() ? List.of()
          : Streams.of(prerelease.split("\\."))
              .map(this::parsePrereleaseField)
              .collect(toUnmodifiableList());
    }
    return prereleaseFields;
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor, patch, prerelease, metadata);
  }

  /**
   * {@inheritDoc}
   * <p>
   * If {@code id} == {@link Id#PRERELEASE} and its last field is non-numeric, an additional numeric
   * field is initialized to 1 — for example, {@code "1.0.0-alpha"} --&gt; {@code "1.0.0-alpha.1"}.
   * </p>
   *
   * @throws IllegalArgumentException
   *           if {@code id} is {@link Id#METADATA}.
   * @see #with(Id, Comparable)
   */
  @Override
  public SemVer2 next(Id id) {
    return switch (id) {
      case MAJOR -> new SemVer2(major + 1, 0, 0, EMPTY, EMPTY);
      case MINOR -> new SemVer2(major, minor + 1, 0, EMPTY, EMPTY);
      case PATCH -> new SemVer2(major, minor, patch + 1, EMPTY, EMPTY);
      case PRERELEASE -> new SemVer2(major, minor, patch, buildPrerelease(nextPrerelease()), EMPTY);
      case METADATA -> throw wrongArg("id", id);
    };
  }

  /**
   * Converts this version to the target type.
   * <p>
   * Converting to {@link SemVer1} implies the replacement of pre-release field dots with hyphens
   * (normalized form according to
   * <a href="https://maven.apache.org/pom.html#Version_Order_Specification">Maven's Version Order
   * Specification</a> — see also {@link SemVer1#getPrereleaseFields()}); such form isn't
   * reversible, as {@code SemVer1}'s syntax makes impossible to tell original {@code SemVer2}'s
   * dot-separated fields apart from actual hyphen-separated qualifiers.
   * </p>
   *
   * @param type
   *          Target type.
   */
  @SuppressWarnings("unchecked")
  public <U extends SemVer<U>> U to(Class<U> type) {
    if (type == SemVer1.class)
      return (U) SemVer1.of(major, minor, patch,
          getPrereleaseFields().stream().map(Object::toString).collect(joining(S + HYPHEN)));
    else if (type == SemVer2.class)
      return (U) this;
    else
      throw unexpected(type);
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

  @Override
  public SemVer2 with(Id id, Comparable value) {
    return switch (id) {
      case MAJOR -> new SemVer2((Integer) value, 0, 0, EMPTY, EMPTY);
      case MINOR -> new SemVer2(major, (Integer) value, 0, EMPTY, EMPTY);
      case PATCH -> new SemVer2(major, minor, (Integer) value, EMPTY, EMPTY);
      case PRERELEASE -> new SemVer2(major, minor, patch, (String) value, EMPTY);
      case METADATA -> new SemVer2(major, minor, patch, prerelease, (String) value);
    };
  }

  /**
   * {@inheritDoc}
   * <p>
   * The existing suffix is replaced with the given one — for example:
   * {@code "1.2.5-alpha.1.snapshot"} with {@code fieldIndex} == {@code 1} and {@code value} ==
   * {@code 8} --&gt; {@code "1.2.5-alpha.8"}.
   * </p>
   */
  @Override
  public SemVer2 withPrereleaseSuffix(int fieldIndex, Comparable value) {
    if (fieldIndex < 0) {
      fieldIndex = getPrereleaseFields().size();
    }
    var fields = new ArrayList<>(getPrereleaseFields().subList(0, fieldIndex));
    fields.add(value);
    return new SemVer2(major, minor, patch, buildPrerelease(fields), EMPTY);
  }

  private String buildPrerelease(List<Comparable> fields) {
    return fields.stream().map(Object::toString).collect(joining(S + DOT));
  }
}
