<?xml version="1.0" encoding="UTF-8"?>
<!--
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (google_checks.patch.xsl) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block below this file
  header, listing the main changes you applied to the original source.
-->
<!--
  Transformer to patch `google_checks.xml` resource from `com.puppycrawl.tools:checkstyle`
  dependency with custom settings.

  Nodes from the dependency resource are simply copied, unless they match a patch template.

  BaseFile: https://github.com/checkstyle/checkstyle/blob/7d9e9489b58531edd4447503f694a5bd4f475209/src/main/resources/google_checks.xml

  HISTORY (format: <`BaseFile` commit SHA> (<`com.puppycrawl.tools:checkstyle` version>, <year>)):
  - ebb7998 (14.3.0-14.4.0, 2026): current patch
  - 1d29e97 (10.2, 2022): initial patch

  MAINTENANCE: In order to work properly, anytime `com.puppycrawl.tools:checkstyle` version is
  bumped (see `maven-checkstyle-plugin.puppycrawl.version`), this transformer MUST be updated
  accordingly:
    1. compare the new `google_checks.xml` against `BaseFile`
    2. update the templates here below, according to these rules:
      - every change: precede it with `[CUSTOM]` comment block for documentation
      - ADD change: separate the custom fragment from following ones via `separate` template
      - REMOVE/REPLACE change: suppress the original replaced/removed fragment via `comment-out`
        template
    3. update `BaseFile` permalinks (both here above and in the `CHANGES TO THE ORIGINAL FILE`
      comment element here below)
    4. add a corresponding HISTORY entry here above (HISTORY entries are useful as references inside
      the custom fragments' documentation)

  NOTE:
  - attribute values written in literal result elements double their braces ("{{" and "}}"), because
    a single brace starts an attribute value template in XSLT. So "${{checkstyle.typename.format}}"
    here yields "${checkstyle.typename.format}" in the output
  - the input must be parsed WITHOUT fetching the remote DTD
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes" encoding="UTF-8"
              doctype-public="-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
              doctype-system="https://checkstyle.org/dtds/configuration_1_3.dtd"/>

  <!-- ROOT -->
  <xsl:template match="/">
    <!-- REUSE-IgnoreStart -->
    <xsl:comment>
      <!-- @formatter:off -->
      CHANGES TO THE ORIGINAL FILE

      SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

      SPDX-License-Identifier: MIT-0

      BaseFile: https://github.com/checkstyle/checkstyle/blob/7d9e9489b58531edd4447503f694a5bd4f475209/src/main/resources/google_checks.xml

      Changes: Patched with pdfClown Common's custom settings (see [CUSTOM] tags).
      <!-- @formatter:on -->
    </xsl:comment>
    <!-- REUSE-IgnoreEnd -->

    <xsl:apply-templates/>
  </xsl:template>

  <!-- IDENTITY: copy everything that is not touched below -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- ADD nodes ================================================================================-->

  <!-- module SuppressWithPlainTextCommentFilter, SuppressWithNearbyTextFilter: ADD -->
  <xsl:template match="/module[@name='Checker']/module[@name='SuppressionFilter']">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>

    <xsl:comment>
      [CUSTOM] Hack to suppress checks (such as JavadocTagContinuationIndentation) from PRE and CODE
      tag blocks in Javadoc (hoping they are not nested, otherwise we are gonna be caught by the
      infamous Chomsky hierarchy trap... - BTW, here it is a popular little gem of humor on the
      topic: https://stackoverflow.com/a/1732454 (best quip: "regex-infection wil​l devour your
      HT​ML parser, application and existence for all time like Visual Basic only worse" - cannot
      imagine a grimmer curse XD )).
      (see https://github.com/checkstyle/checkstyle/issues/13048).
    </xsl:comment>
    <module name="SuppressWithPlainTextCommentFilter">
      <property name="offCommentFormat" value="&lt;(pre|code)&gt;"/>
      <property name="onCommentFormat" value="&lt;/(pre|code)&gt;"/>
    </module>
    <module name="SuppressWithNearbyTextFilter">
      <property name="nearbyTextPattern" value="&lt;/(pre|code)&gt;"/>
    </module>
    <xsl:call-template name="separate"/>
  </xsl:template>

  <!-- module AvoidEscapedUnicodeCharacters: ADD property 'allowIfAllCharactersEscaped' -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='AvoidEscapedUnicodeCharacters']">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>

      <xsl:comment>
        [CUSTOM] Allow escaping on Unicode-only literals.
      </xsl:comment>
      <property name="allowIfAllCharactersEscaped" value="true"/>
    </xsl:copy>
  </xsl:template>

  <!-- module FallThrough: ADD property 'reliefPattern' -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='FallThrough']">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>

      <xsl:comment>
        [CUSTOM] Allow unified fall-through tag only.
      </xsl:comment>
      <property name="reliefPattern" value="FALLTHRU"/>
    </xsl:copy>
  </xsl:template>

  <!-- REMOVE nodes =============================================================================-->

  <!-- Checker / property 'severity': REMOVE -->
  <xsl:template match="/module[@name='Checker']/property[@name='severity']">
    <xsl:comment>
      [CUSTOM] Suppressed.
    </xsl:comment>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module OverloadMethodsDeclarationOrder: REMOVE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='OverloadMethodsDeclarationOrder']">
    <xsl:comment>
      [CUSTOM] Allow overloaded methods to split (method scope order takes precedence).
    </xsl:comment>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module VariableDeclarationUsageDistance: REMOVE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='VariableDeclarationUsageDistance']">
    <xsl:comment>
      [CUSTOM] Allow variables to be declared at arbitrary distance from usage.
    </xsl:comment>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module SummaryJavadoc: REMOVE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='SummaryJavadoc']">
    <xsl:comment>
      [CUSTOM] Allow missing Javadoc summary (this check is extremely pedantic, and cannot be
      selectively suppressed by XPath to exclude, for example, constructors which, most of the time,
      are excruciatingly redundant).
    </xsl:comment>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module JavadocParagraph: REMOVE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='JavadocParagraph']">
    <xsl:comment>
      [CUSTOM] Allow relaxed Javadoc paragraph (no need of blank line between paragraphs).
    </xsl:comment>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module MultilineCommentLeadingAsteriskPresence: REMOVE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='MultilineCommentLeadingAsteriskPresence']">
    <xsl:comment>
      [CUSTOM] Suppressed.
    </xsl:comment>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module TextBlockGoogleStyleFormatting: REMOVE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='TextBlockGoogleStyleFormatting']">
    <xsl:comment>
      [CUSTOM] Suppressed.
    </xsl:comment>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module ArrayBracketNoWhitespace: REMOVE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='ArrayBracketNoWhitespace']">
    <xsl:comment>
      [CUSTOM] Allow whitespace between ']' and `}`, for example:
      myMethod(new float[] { a[0], a[1] })
    </xsl:comment>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module OperatorWrap: REMOVE additional instances (such as `OperatorWrapEOL`) -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='OperatorWrap' and not(property[@name='id']/@value='OperatorWrapNL')]">
    <xsl:comment>
      [CUSTOM] Suppress additional OperatorWrap modules (ebb7998 (14.3.0, 2026)) to restore the good
      old behavior (1d29e97 (10.2, 2022)), allowing to split `->` to new line on modern switch cases
      for readability, for example:
      <!-- @formatter:off -->
      return switch (platformID) {
        case //
          PLATFORM_ID__UNICODE, //
          PLATFORM_ID__MICROSOFT //
          -> base.readString(length, UTF_16);
        default //
          -> base.readString(length);
        };
      <!-- @formatter:on -->
    </xsl:comment>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- REPLACE nodes ============================================================================-->

  <!-- module SuppressionFilter / property 'file': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='SuppressionFilter']/property[@name='file']">
    <xsl:comment>
      [CUSTOM] `checkstyle.suppressions.file` maps to `suppressionsLocation` configuration.
    </xsl:comment>
    <property
        name="file"
        value="${{checkstyle.suppressions.file}}"
        default="checkstyle-suppressions.xml"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module LineLength / property 'ignorePattern': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='LineLength']/property[@name='ignorePattern']">
    <xsl:comment>
      [CUSTOM] Ignore `{@link`.
    </xsl:comment>
    <property name="ignorePattern"
              value="^package.*|^import.*|href\s*=\s*&quot;[^&quot;]*&quot;|http://|https://|ftp://|\{{@link"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module SuppressionCommentFilter: REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='SuppressionCommentFilter']">
    <xsl:comment>
      [CUSTOM] Allows free indentation of formatter-tag comments.
      Unfortunately, `onComment` was (checkstyle 10.1) excluded from suppression [1].

      TODO: verify whether current version (checkstyle 14.3.0) solves the issue.

      [1] https://github.com/checkstyle/checkstyle/issues/11504
    </xsl:comment>
    <module name="SuppressionCommentFilter">
      <property name="offCommentFormat" value="@formatter:off"/>
      <property name="onCommentFormat" value="@formatter:on"/>
      <property name="checkFormat" value="CommentsIndentation"/>
    </module>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module NeedBraces / property 'tokens': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='NeedBraces']/property[@name='tokens']">
    <xsl:comment>
      [CUSTOM] Allow missing braces on IF and ELSE occurrences
    </xsl:comment>
    <property name="tokens" value="LITERAL_DO, LITERAL_FOR, LITERAL_WHILE"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module WhitespaceAfter / property 'tokens': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='WhitespaceAfter']/property[@name='tokens']">
    <xsl:comment>
      [CUSTOM] Restore the old behavior (1d29e97 (10.2, 2022)).
    </xsl:comment>
    <property name="tokens" value="COMMA, SEMI, TYPECAST, LITERAL_IF, LITERAL_ELSE, LITERAL_RETURN,
                    LITERAL_WHILE, LITERAL_DO, LITERAL_FOR, LITERAL_FINALLY, DO_WHILE, ELLIPSIS,
                    LITERAL_SWITCH, LITERAL_SYNCHRONIZED, LITERAL_TRY, LITERAL_CATCH, LAMBDA"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module EmptyLineSeparator / property 'tokens': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='EmptyLineSeparator']/property[@name='tokens']">
    <xsl:comment>
      [CUSTOM] Allow non-empty line above package definition, variables and (static and instance)
      initializers.
    </xsl:comment>
    <property name="tokens" value="IMPORT, STATIC_IMPORT, CLASS_DEF, INTERFACE_DEF, ENUM_DEF,
                    METHOD_DEF, CTOR_DEF, RECORD_DEF,
                    COMPACT_CTOR_DEF"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module TypeName / property 'format': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='TypeName']/property[@name='format']">
    <xsl:comment>
      [CUSTOM] Configurable format.
    </xsl:comment>
    <property
        name="format"
        value="${{checkstyle.typename.format}}"
        default="^[A-Z][a-zA-Z0-9]*$"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module ParameterName / property 'format': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='ParameterName']/property[@name='format']">
    <xsl:comment>
      [CUSTOM] Allow any non-capitalized name, with trailing underscore in case of reserved keyword
      conflict.
    </xsl:comment>
    <property name="format" value="^[a-z][a-zA-Z0-9]*[_]?$"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module LambdaParameterName / property 'format': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='LambdaParameterName']/property[@name='format']">
    <xsl:comment>
      [CUSTOM] Allow any non-capitalized, possibly dollar-prefixed name.
    </xsl:comment>
    <property name="format" value="^[a-z$][a-zA-Z0-9$]*$"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module LocalVariableName / property 'format': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='LocalVariableName']/property[@name='format']">
    <xsl:comment>
      [CUSTOM] Allow any non-capitalized name, with trailing underscore in case of reserved keyword
      conflict.
    </xsl:comment>
    <property name="format" value="^[a-z][a-zA-Z0-9]*[_]?$"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module ClassTypeParameterName / property 'format': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='ClassTypeParameterName']/property[@name='format']">
    <xsl:comment>
      [CUSTOM] Allow only single capital letter (e.g., 'K') or T prefix (e.g., 'TMyType').
    </xsl:comment>
    <property name="format" value="(^[A-Z][0-9]?)$|(^T[A-Z][a-zA-Z0-9]+$)"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module RecordComponentName / property 'format': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='RecordComponentName']/property[@name='format']">
    <xsl:comment>
      [CUSTOM] Allow any non-capitalized name.
    </xsl:comment>
    <property name="format" value="^[a-z][a-zA-Z0-9]*$"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module MethodTypeParameterName / property 'format': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='MethodTypeParameterName']/property[@name='format']">
    <xsl:comment>
      [CUSTOM] Allow type parameter names like 'K' or 'TMyType'.
    </xsl:comment>
    <property name="format" value="(^[A-Z][0-9]?)$|(^T[A-Z][a-zA-Z0-9]+$)"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module InterfaceTypeParameterName / property 'format': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='InterfaceTypeParameterName']/property[@name='format']">
    <xsl:comment>
      [CUSTOM] Allow type parameter names like 'K' or 'TMyType'.
    </xsl:comment>
    <property name="format" value="(^[A-Z][0-9]?)$|(^T[A-Z][a-zA-Z0-9]+$)"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module Indentation / property 'braceAdjustment': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='Indentation']/property[@name='braceAdjustment']">
    <xsl:comment>
      [CUSTOM] Allow brace on new line at the same indentation level as previous line.
    </xsl:comment>
    <property name="braceAdjustment" value="0"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module AbbreviationAsWordInName / property 'allowedAbbreviationLength': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='AbbreviationAsWordInName']/property[@name='allowedAbbreviationLength']">
    <xsl:comment>
      [CUSTOM] Allow all short (up to 3) capital letter sequences and explicitly-defined long ones
      in names.
    </xsl:comment>
    <property name="allowedAbbreviationLength" value="2"/>
    <property name="allowedAbbreviations"
              value="${{checkstyle.abbreviationAsWordInName.allowedAbbreviations}}"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module CustomImportOrder / property 'sortImportsInGroupAlphabetically': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='CustomImportOrder']/property[@name='sortImportsInGroupAlphabetically']">
    <xsl:comment>
      [CUSTOM] Allow Eclipse-styled import order (keep synchronized with java.importorder file).
    </xsl:comment>
    <property name="sortImportsInGroupAlphabetically" value="false"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module MissingJavadocMethod / property 'tokens': REPLACE -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='MissingJavadocMethod']/property[@name='tokens']">
    <xsl:comment>
      [CUSTOM] Allow constructors to have no Javadoc (redundant).
    </xsl:comment>
    <property name="tokens" value="METHOD_DEF, ANNOTATION_FIELD_DEF"/>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module LeftCurly: REPLACE -->
  <xsl:template priority="2"
                match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='LeftCurly'][1]">
    <xsl:comment>
      [CUSTOM] Restore the good old module (1d29e97 (10.2, 2022)), to allow left curly braces
      trailing switch cases.
    </xsl:comment>
    <module name="LeftCurly">
      <property name="tokens"
                value="ANNOTATION_DEF, CLASS_DEF, CTOR_DEF, ENUM_CONSTANT_DEF, ENUM_DEF,
                    INTERFACE_DEF, LAMBDA, LITERAL_CASE, LITERAL_CATCH, LITERAL_DEFAULT,
                    LITERAL_DO, LITERAL_ELSE, LITERAL_FINALLY, LITERAL_FOR, LITERAL_IF,
                    LITERAL_SWITCH, LITERAL_SYNCHRONIZED, LITERAL_TRY, LITERAL_WHILE, METHOD_DEF,
                    OBJBLOCK, STATIC_INIT, RECORD_DEF, COMPACT_CTOR_DEF"/>
    </module>
    <xsl:call-template name="comment-out"/>
  </xsl:template>
  <!-- module LeftCurly: REMOVE additional instances (such as `LeftCurlyNl`) -->
  <xsl:template priority="1"
                match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='LeftCurly']">
    <xsl:comment>
      [CUSTOM] Suppress additional LeftCurly modules (ebb7998 (14.3.0, 2026)).
    </xsl:comment>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module GoogleRightCurly: REPLACE with module RightCurly -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='GoogleRightCurly']">
    <xsl:comment>
      [CUSTOM] Allow closing braces to stay alone on IF and ELSE occurrences (this allows condition
      comments aligned to the respective conditional block for better visibility, for example (look
      at `// Null?`):
      <!-- @formatter:off -->
      // Boolean?
      if (token.equals(Keyword.False) || token.equals(Keyword.True)) {
        . . .
      }
      // Null?
      else if (token.equals(Keyword.Null)) {
        . . .
      }
      <!-- @formatter:on -->
    </xsl:comment>
    <module name="RightCurly">
      <property
          name="id"
          value="RightCurlySame"/>
      <property
          name="tokens"
          value="LITERAL_TRY, LITERAL_CATCH, LITERAL_FINALLY,
                    LITERAL_DO"/>
      <xsl:comment>
        &lt;property name="tokens"
        value="LITERAL_TRY, LITERAL_CATCH, LITERAL_FINALLY, LITERAL_IF, LITERAL_ELSE,
        LITERAL_DO"/&gt;
      </xsl:comment>
    </module>
    <module name="RightCurly">
      <property
          name="id"
          value="RightCurlyAlone"/>
      <property
          name="option"
          value="alone"/>
      <property
          name="tokens"
          value="CLASS_DEF, METHOD_DEF, CTOR_DEF, LITERAL_FOR, LITERAL_WHILE, STATIC_INIT,
                    INSTANCE_INIT, ANNOTATION_DEF, ENUM_DEF, INTERFACE_DEF, RECORD_DEF,
                    COMPACT_CTOR_DEF"/>
    </module>
    <module name="SuppressionXpathSingleFilter">
      <xsl:comment>suppression is required till
        https://github.com/checkstyle/checkstyle/issues/7541
      </xsl:comment>
      <property
          name="id"
          value="RightCurlyAlone"/>
      <property
          name="query"
          value="//RCURLY[parent::SLIST[count(./*)=1]
                                     or preceding-sibling::*[last()][self::LCURLY]]"/>
    </module>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module GoogleNonConstantFieldName: REPLACE with module MemberName -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='GoogleNonConstantFieldName']">
    <xsl:comment>
      [CUSTOM] Allow any non-capitalized name, with trailing underscore for special cases (e.g.,
      reserved keyword conflict like "extends").
    </xsl:comment>
    <module name="MemberName">
      <property
          name="format"
          value="^[a-z][a-zA-Z0-9]*[_]?$"/>
      <message
          key="name.invalidPattern"
          value="Member name ''{0}'' must match pattern ''{1}''."/>
    </module>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!-- module GoogleMethodName: REPLACE with module MethodName -->
  <xsl:template
      match="/module[@name='Checker']/module[@name='TreeWalker']/module[@name='GoogleMethodName']">
    <xsl:comment>
      [CUSTOM] Allow underscore-lead name on private methods.
    </xsl:comment>
    <module name="MethodName">
      <property name="format" value="^[a-z_][a-zA-Z0-9]*$"/>
      <property name="applyToPublic" value="false"/>
      <property name="applyToProtected" value="false"/>
      <property name="applyToPackage" value="false"/>
      <message key="name.invalidPattern"
               value="Method name ''{{0}}'' must match pattern ''{{1}}''."/>
    </module>
    <xsl:call-template name="comment-out"/>
  </xsl:template>

  <!--==============================================================================================
    NAMED TEMPLATES
  ===============================================================================================-->

  <!--
    NODE SUPPRESSION

    Useful to comment out existing nodes.
  -->
  <xsl:template name="comment-out">
    <xsl:comment>
      <xsl:apply-templates select="." mode="serialize"/>
    </xsl:comment>
    <xsl:call-template name="separate"/>
  </xsl:template>
  <xsl:template match="*" mode="serialize">
    <xsl:text>&lt;</xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:apply-templates select="@*" mode="serialize"/>
    <xsl:choose>
      <xsl:when test="node()">
        <xsl:text>&gt;</xsl:text>
        <xsl:apply-templates mode="serialize"/>
        <xsl:text>&lt;/</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>&gt;</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>/&gt;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="@*" mode="serialize">
    <xsl:text> </xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:text>="</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>"</xsl:text>
  </xsl:template>
  <xsl:template match="text()" mode="serialize">
    <xsl:value-of select="."/>
  </xsl:template>
  <xsl:template match="comment()" mode="serialize"/>

  <!--
    NODE SEPARATION

    Useful to separate a fragment from the following nodes via empty line.
  -->
  <xsl:template name="separate">
    <xsl:text>&#10;</xsl:text>
  </xsl:template>
</xsl:stylesheet>