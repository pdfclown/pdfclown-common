[Documentation](README.md) >

# Building and Testing

This document describes how to set up your development environment to build and test pdfClown.org projects.

## Prerequisites

- **JDK 17+** (Maven execution)

- **JDK 17** (source code compilation)

- **[Maven toolchains configuration](https://maven.apache.org/guides/mini/guide-using-toolchains.html)** for JDK 17 (source code compilation)

    If the file `${user.home}/.m2/toolchains.xml` is missing, generate it via CLI:

    ```shell
    ./mvnw toolchains:generate-jdk-toolchains-xml
    ```

    It should contain a `toolchain` entry for JDK 17, like this:

    ```xml
    <toolchain>
      <type>jdk</type>
      <provides>
        <version>17</version>
        . . .
    ```

- **[Node.js](https://nodejs.org/en)** [[installation instructions](https://nodejs.org/en/download)]

    This is required by the [Prettier](https://prettier.io/) formatter integrated in [Spotless](https://github.com/diffplug/spotless/tree/main/plugin-maven) Maven plugin.
- **[pre-commit](https://pre-commit.com/)** [[installation instructions](https://pre-commit.com/#install)]
- **[commitizen](https://commitizen-tools.github.io/commitizen/)** [[installation instructions](https://commitizen-tools.github.io/commitizen/#requirements)]
- **[REUSE tool](https://reuse.software/)** [[installation instructions](https://reuse.readthedocs.io/en/latest/readme.html#install)]

## Setup

1. [fork](https://help.github.com/articles/fork-a-repo/) the project, then clone your fork and configure its upstream:

    ```shell
    # Clone your fork into the current directory!
    git clone https://github.com/%YOUR_USERNAME%/pdfclown-common.git
    # Enter into the cloned directory!
    cd pdfclown-common
    # Add the original repository as "upstream" remote!
    git remote add upstream https://github.com/pdfclown/pdfclown-common.git
    ```

    To get the latest changes from upstream:

    ```shell
    git checkout main
    git pull upstream main
    ```

2. check the **[prerequisites](#prerequisites)** are satisfied

3. install **commit validation hooks**:

    ```shell
    pre-commit install -t pre-commit -t commit-msg
    ```

    Installed hooks (activated whenever a commit is submitted):

    - [commit-check](https://github.com/commit-check/commit-check) — triggers `check-branch`, aborting the new commit if [branch validation](maintenance.md#branches) failed
    - [commitizen](https://commitizen-tools.github.io/commitizen/getting_started/#integration-with-pre-commit) — triggers `cz check`, aborting the new commit if [message validation](maintenance.md#commits) failed
    - [REUSE](https://reuse.software/dev/#pre-commit-hook) — triggers `reuse lint`, aborting the new commit if [REUSE validation](https://reuse.software/spec-3.3/) failed

    To update the hooks to latest version:

    ```shell
    pre-commit autoupdate
    ```

4. that's all! :tada: Now you are ready to build (see next section) — happy development!

## Building

> [!NOTE]
> The projects are based on the [Maven build system](https://maven.apache.org/index.html); since they include [Maven Wrapper](https://maven.apache.org/tools/wrapper/) (`mvnw` (Unix)/`mvnw.cmd` (Windows) command), builds work out of the box (no setup needed).

Common build operations via CLI (see also the comparison table here below):

- installation:

    - basic (without javadoc):

        ```shell
        ./mvnw install
        ```

    - full (with javadoc):

        ```shell
        ./mvnw install -Pfull
        ```

    - fast (without javadoc and integration tests):

        ```shell
        ./mvnw install -Pfast
        ```

    - fastest (without javadoc, integration tests and unit tests):

        ```shell
        ./mvnw install -Pfast+
        ```

- testing:

    - basic (unit tests only):

        ```shell
        ./mvnw test
        ```

    - full (unit tests and integration tests):

        ```shell
        ./mvnw verify
        ```

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