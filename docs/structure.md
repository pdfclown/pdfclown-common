<!--
  SPDX-FileCopyrightText: 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: CC-BY-SA-4.0
-->

[Project Conventions](conventions.md) >

# Project Structure

The filesystem structure of pdfClown.org projects follows [Maven's Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html).

For clarity, subproject directory names MUST match the `project/artifactID` element in their respective pom.xml; the same applies to the `project/name` element:

    <name>${project.artifactId}</name>

