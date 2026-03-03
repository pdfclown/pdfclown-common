/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JavaParsers.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.util.source;

import static org.pdfclown.common.util.Objects.enclosingTypes;
import static org.pdfclown.common.util.Objects.opt;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import java.util.Optional;
import org.pdfclown.common.util.Ref;

/**
 * Java source code utilities.
 *
 * @author Stefano Chizzolini
 */
public final class JavaParsers {
  /**
   * Gets the source type corresponding to a class.
   *
   * @param type
   *          Class corresponding to the source type to match.
   * @param source
   *          Compilation unit containing the source type.
   * @throws RuntimeException
   *           if {@code source} does not contain {@code type}.
   */
  public static Optional<TypeDeclaration<?>> sourceType(Class<?> type, CompilationUnit source) {
    var sourceTypeRef = new Ref<TypeDeclaration<?>>(source.getPrimaryType().orElseThrow());
    if (type.getEnclosingClass() == null)
      return opt(sourceTypeRef.get().getNameAsString().equals(type.getSimpleName())
          ? sourceTypeRef.get()
          : null);

    enclosingTypes(type, true).forEachOrdered($ -> {
      if (sourceTypeRef.isEmpty())
        return;
      else if ($.getEnclosingClass() == null) {
        if (!sourceTypeRef.get().getNameAsString().equals($.getSimpleName())) {
          sourceTypeRef.clear();
        }
        return;
      }

      for (var member : sourceTypeRef.get().getMembers()) {
        if (member.isClassOrInterfaceDeclaration()
            && member.asClassOrInterfaceDeclaration().getNameAsString()
                .equals($.getSimpleName())) {
          sourceTypeRef.set(member.asClassOrInterfaceDeclaration());
          return;
        }
      }
      sourceTypeRef.clear();
    });
    return opt(sourceTypeRef.get());
  }

  private JavaParsers() {
  }
}
