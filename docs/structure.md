<!--
  SPDX-FileCopyrightText: 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: CC-BY-SA-4.0
-->

[Project Conventions](conventions.md) >

# Project Structure

The filesystem structure of pdfClown.org projects follows [Maven's Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html).

For clarity, [subproject directory names MUST match the `project/artifactID` element](https://www.sonatype.com/blog/2011/01/maven-tip-project-directories-and-artifact-ids) in their respective pom.xml; the same applies to the `project/name` element:

    <name>${project.artifactId}</name>

