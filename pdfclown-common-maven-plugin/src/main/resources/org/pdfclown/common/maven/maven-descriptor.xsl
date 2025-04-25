<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  SPDX-FileCopyrightText: 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later
-->
<!--
  This stylesheet prepares MAVEN DESCRIPTOR (ie, metadata embedded in non-'pom' artifacts) for
  distribution, purging any irrelevant content.

  See also <https://central.sonatype.org/publish/requirements/#required-pom-metadata>
-->
<stylesheet
  xmlns="http://www.w3.org/1999/XSL/Transform"
  xmlns:pom="http://maven.apache.org/POM/4.0.0"
  version="1.0">
  <output
    method="xml"
    indent="yes"/>
  <strip-space elements="*"/>
  <param name="header"></param>

  <!-- Root -->
  <template match="/">
    <comment>
      <value-of select="$header"/>
    </comment>
    <apply-templates select="project"/>
  </template>

  <!-- Attributes -->
  <template match="@*">
    <copy>
      <apply-templates select="@*"/>
    </copy>
  </template>

  <!--
    Nodes

    NOTE: Attributes are suppressed (they are typically irrelevant configuration details).
  -->
  <template match="node()">
    <copy>
      <apply-templates select="node()"/>
    </copy>
  </template>

  <!--
    Root element

    This rule commands the whole metadata filtering, as only whitelisted sub-nodes survive.
  -->
  <template match="/project">
    <copy>
      <apply-templates select="@*"/>
      <!--  Whitelist -->
      <apply-templates
        select="modelVersion
        | groupId | artifactId | version
        | name | description | url | inceptionYear | organization
        | licenses
        | developers
        | scm | issueManagement
        | dependencies"/>
    </copy>
  </template>

  <!--
    Blacklist

    NOTE: Because of root element filtering, this list focuses on whitelisted sub-nodes only.
  -->
  <template match="comment() | dependency[scope='test']"/>
</stylesheet>
