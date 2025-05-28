<!--
  SPDX-FileCopyrightText: 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: CC-BY-SA-4.0
-->

# Building and Testing

This document describes how to set up your development environment to build and test pdfClown.org projects.

## Prerequisites

- **JDK 11** (for source code compilation)

- **JDK 17+** (for Maven execution)

- **[Maven toolchains configuration](https://maven.apache.org/guides/mini/guide-using-toolchains.html)** for JDK 11

    If the file `${user.home}/.m2/toolchains.xml` is missing, generate it via CLI:

      mvn toolchains:generate-jdk-toolchains-xml

    It should contain a `toolchain` entry for JDK 11, like this:

    ```xml
    <toolchain>
      <type>jdk</type>
      <provides>
        <version>11</version>
        . . .
    ```

- **[nodeJS](https://nodejs.org/en)** [[installation instructions](https://nodejs.org/en/download)]

    This is required by the [Prettier](https://prettier.io/) formatter integrated in [Spotless](https://github.com/diffplug/spotless/tree/main/plugin-maven) Maven plugin.
- **[pre-commit](https://pre-commit.com/)** [[installation instructions](https://pre-commit.com/#install)]
- **[commitizen](https://commitizen-tools.github.io/commitizen/)** [[installation instructions](https://commitizen-tools.github.io/commitizen/#requirements)]
- **[REUSE tool](https://reuse.software/)** [[installation instructions](https://reuse.readthedocs.io/en/latest/readme.html#install)]

## Setup

1. install **commit validation hooks**:

        pre-commit install

    Installed hooks (activated whenever a commit is submitted):

    - [commit-check](https://github.com/commit-check/commit-check) — triggers `check-branch`, aborting the new commit if [branch validation](https://conventional-branch.github.io/) failed
    - [commitizen](https://commitizen-tools.github.io/commitizen/getting_started/#integration-with-pre-commit) — triggers `cz check`, aborting the new commit if [message validation](https://www.conventionalcommits.org/en/v1.0.0/) failed
    - [REUSE](https://reuse.software/dev/#pre-commit-hook) — triggers `reuse lint`, aborting the new commit if [REUSE validation](https://reuse.software/spec-3.3/) failed

    To update the hooks to latest version:

        pre-commit autoupdate

## Building

In order to speed up frequent building operations, heavier goals are skipped by default and can be activated via profiles.

Common CLI operations:

- local installation:

    - basic build (without integration tests, sources jar nor javadoc jar):

            mvn install

    - full build (with integration tests, sources jar and javadoc jar):

            mvn install -Pverify,install

- publishing on Maven Central:

        mvn deploy -Pverify,release

See [pdfclown-common-base](../pdfclown-common-base/pom.xml) for a basic cheatsheet.