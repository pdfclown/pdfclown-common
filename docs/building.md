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

- **[Node.js](https://nodejs.org/en)** [[installation instructions](https://nodejs.org/en/download)]

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

Common build operations via CLI (see also the comparison table here below):

- installation:

    - basic (without javadoc):

            mvn install

    - full (with javadoc):

            mvn install -Pfull

    - fast (without javadoc and integration tests):

            mvn install -Pfast

    - fastest (without javadoc, integration tests and unit tests):

            mvn install -Pfast+

- testing:

    - basic (unit tests only):

            mvn test

    - full (unit tests and integration tests):

            mvn verify

<table>
<tr>
<th></th>
<th width="20%">Code processing (transformation and validation)</th>
<th width="20%">Unit testing</th>
<th width="20%">Integration testing</th>
<th width="20%">Javadoc</th>
</tr>
<tr>
<th colspan="5">installation</th>
</tr>
<tr>
<th>Full</th>
<td>:white_check_mark:</td>
<td>:white_check_mark:</td>
<td>:white_check_mark:</td>
<td>:white_check_mark:</td>
</tr>
<tr>
<th>Basic</th>
<td>:white_check_mark:</td>
<td>:white_check_mark:</td>
<td>:white_check_mark:</td>
<td></td>
</tr>
<tr>
<th>Fast</th>
<td>:white_check_mark:</td>
<td>:white_check_mark:</td>
<td></td>
<td></td>
</tr>
<tr>
<th>Fastest</th>
<td>:white_check_mark:</td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<th colspan="5">Testing</th>
</tr>
<tr>
<th>Full</th>
<td>:white_check_mark:</td>
<td>:white_check_mark:</td>
<td>:white_check_mark:</td>
<td></td>
</tr>
<tr>
<th>Basic</th>
<td>:white_check_mark:</td>
<td>:white_check_mark:</td>
<td></td>
<td></td>
</tr>
</table>

See [pdfclown-common-base](../pdfclown-common-base/pom.xml) for a basic cheatsheet.