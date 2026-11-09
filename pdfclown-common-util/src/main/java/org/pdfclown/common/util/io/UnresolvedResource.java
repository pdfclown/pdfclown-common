/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UnresolvedResource.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block below this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.missingFile;
import static org.pdfclown.common.util.Exceptions.wrongState;
import static org.pdfclown.common.util.Objects.textLiteral;
import static org.pdfclown.common.util.Objects.toStringWithValues;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.function.Function;
import org.pdfclown.common.util.annot.Immutable;

/**
 * Unresolved resource.
 *
 * @author Stefano Chizzolini
 * @implNote <a href="https://en.wikipedia.org/wiki/Null_object_pattern">Null object</a>.
 *           <p>
 *           This implementation covers all the derivative interfaces of {@link Resource} (namely,
 *           {@link PathResource}) in order to fit any demand (for example, see the cast in the
 *           implementation of {@link Resource#path(String, ClassLoader, Function)}).
 *           </p>
 */
@Immutable
final class UnresolvedResource implements PathResource {
  private final String name;

  UnresolvedResource(String name) {
    this.name = requireNonNull(name, "`name`");
  }

  @Override
  public String asString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof UnresolvedResource that
        && this.name.equals(that.name));
  }

  @Override
  public boolean exists() {
    return false;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Path getPath() {
    throw wrongState("Resource {} UNRESOLVED", textLiteral(name));
  }

  @Override
  public URI getUri() {
    throw wrongState("Resource {} UNRESOLVED", textLiteral(name));
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public InputStream openStream() throws IOException {
    throw missingFile(name, "Resource UNRESOLVED");
  }

  @Override
  public String toString() {
    return toStringWithValues(this, getName());
  }
}
