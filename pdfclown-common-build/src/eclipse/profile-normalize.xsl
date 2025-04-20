<?xml version="1.0" encoding="UTF-8"?>
<!--
  Eclipse profile format normalizer.

  Applies the following rules:
  - no indentation (yet readable through per-element newlines)
  - `setting` elements sorted in ascending order
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output encoding="UTF-8"/>
  <xsl:strip-space elements="*"/>

  <xsl:template match="@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*">
    <xsl:text>&#x0A;</xsl:text>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="*">
        <xsl:sort select="@id" order="ascending"/>
      </xsl:apply-templates>
      <!-- Expand only in case of child elements. -->
      <xsl:if test="count(*) &gt; 0">
        <xsl:text>&#x0A;</xsl:text>
      </xsl:if>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>