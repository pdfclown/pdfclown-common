pdfClown.org > [Documentation](README.md) >

# Building

This document describes how to set up your development environment to build and test pdfClown.org projects.

## Prerequisites

- **JDK 21** (Maven toolchain)

    Once JDK 21 is installed, the corresponding [Maven toolchain MUST be configured](https://maven.apache.org/guides/mini/guide-using-toolchains.html): if the file `${user.home}/.m2/toolchains.xml` is missing or doesn't include an entry for JDK 21, generate it via CLI:

    ```shell
    ./mvnw toolchains:generate-jdk-toolchains-xml
    ```

    It should contain a `toolchain` entry for JDK 21, that MUST be included in `${user.home}/.m2/toolchains.xml`, like this:

    ```xml
    <toolchain>
      <type>jdk</type>
      <provides>
        <version>21</version>
        . . .
    ```
- **JDK 17** (source code target)
- **[Node.js](https://nodejs.org/en)** [[installation instructions](https://nodejs.org/en/download)]

    This is required by the [Prettier](https://prettier.io/) formatter integrated in [Spotless](https://github.com/diffplug/spotless/tree/main/plugin-maven) Maven plugin.
- **[pre-commit](https://pre-commit.com/)** [[installation instructions](https://pre-commit.com/#install)]
- **[commitizen](https://commitizen-tools.github.io/commitizen/)** [[installation instructions](https://commitizen-tools.github.io/commitizen/#requirements)]
- **[REUSE tool](https://reuse.software/)** [[installation instructions](https://reuse.readthedocs.io/en/latest/readme.html#install)]

## Setup

1. [fork](https://help.github.com/articles/fork-a-repo/) the pdfClown.org project of your choice (in this example, `pdfclown-common`), then clone your fork and configure its upstream:

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

2. check the **[prerequisites](#prerequisites)** are satisfied (including those specific to the pdfClown.org project of your choice)

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

4. apply any other step specific to the pdfClown.org project of your choice

## Building

> [!NOTE]
> pdfClown.org projects are based on the [Maven build system](https://maven.apache.org/index.html); since they include [Maven Wrapper](https://maven.apache.org/tools/wrapper/) (`mvnw` command), builds work out of the box (no setup needed).

### Installation

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

<table>
<tr>
<th></th>
<th width="20%">Code processing (transformation and validation)</th>
<th width="20%">Unit testing</th>
<th width="20%">Integration testing</th>
<th width="20%">Javadoc</th>
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
</table>

> [!IMPORTANT]
> By default, **compilation linting** (`@lint` profile) is active to enforce code quality through static checks provided by javac and [Error Prone](https://errorprone.info/) (a popular javac plugin). To ensure a thorough validation, force a full rebuild:
>
> ```shell
> ./mvnw clean (compile|install)
> ```
>
> Despite its usefulness, in specific circumstances compilation linting may become inconvenient:
>
> - IDEs like IntelliJ IDEA, which integrate their builds with the underlying Maven configuration, may be disrupted by the `@lint` profile and fail to compile with obscure messages like "java: Compilation failed: internal java compiler error": in such cases, disable the `@lint` profile within the IDE (in IntelliJ IDEA, look under the Profiles node in Maven view: after unchecking the profile, click "Reload All Maven Projects" command)
> - on CLI, to disable the `@lint` profile, use `skipLint` system property, like so:
>
>   ```shell
>   ./mvnw (compile|install) -DskipLint
>   ```
>
> Moreover, to apply linting so it emits warnings without failing (for example, to test alternative JDK versions on CI), use `lint.lenient`, like so:
>
> ```shell
> ./mvnw (compile|install) -Dlint.lenient
> ```
> IMPORTANT: *Disabling lint enforcement on CLI can only be a temporary local trick*, as in any case CI enforces compiler checks on every push to the project repository — before committing PRs, ensure your source code passes compilation linting (along with pre-compilation linting (Spotless formatting and Checkstyle static checks))!

### Testing

- **basic** (unit tests only):

    ```shell
    ./mvnw test
    ```

- **full** (unit tests and integration tests):

    ```shell
    ./mvnw verify
    ```

- **selective**:

  - unit tests (specified tests only):

      ```shell
      ./mvnw test -pl %MODULE% -Dtest=%TEST%
      ```

      where:

      - %MODULE% is the project module containing the tests (e.g., `pdfclown-common-util`)
      - %TEST% is the unit test selector, a single identifier or a comma-separated list of identifiers, each expressed in any of the following alternatives:
          - test class (e.g., `StringsTest`) — to run all its tests
          - test method (e.g., `StringsTest#replaceString`) — to run only the specified test

  - integration tests (specified tests only):

      ```shell
      ./mvnw verify -pl %MODULE% -Dtest=%IT%
      ```

      where:

      - %MODULE% is the project module containing the tests (e.g., `pdfclown-common-util`)
      - %IT% is the integration test selector, a single identifier or a comma-separated list of identifiers, each expressed in any of the following alternatives:
        - test class (e.g., `LayoutIT`) — to run all its tests
        - test method (e.g., `LayoutIT#createDocument`) — to run only the specified test

<table>
<tr>
<th></th>
<th width="20%">Code processing (transformation and validation)</th>
<th width="20%">Unit testing</th>
<th width="20%">Integration testing</th>
<th width="20%">Javadoc</th>
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

> [!TIP]
> **Test resources** are automatically managed by the testing harness ([pdfclown-common-build](https://javadoc.io/doc/org.pdfclown/pdfclown-common-build/0.8.0/org/pdfclown/common/build/test/package-summary.html)). In case of test failures:
>
> 1. **evaluate the differences between actual results and expected ones**: besides the standard JUnit messages, several ([`org.pdfclown.common.build.test.assertion.Verifier`](https://javadoc.io/doc/org.pdfclown/pdfclown-common-build/0.8.0/org/pdfclown/common/build/test/assertion/Verifier.html)-based) unit test results can be inspected for comparison via diff dialogs:
>    ```shell
>    ./mvnw test -Dtest.reporter.enabled
>    ```
>    while several ([`org.pdfclown.common.build.test.assertion.Asserter`](https://javadoc.io/doc/org.pdfclown/pdfclown-common-build/0.8.0/org/pdfclown/common/build/test/assertion/Asserter.html)-based) integration tests report failure details in `%MODULE%/target/test-logs/pdfclown/assertion.log`, along with instructions to resolve them
> 2. in case of false positive (that is, valid differences caused by actual changes in the codebase), **update expected test resources**:
>    - **manually**, using diff dialogs in case of `Verifier`-based unit tests (see here above)
>    - **automatically**, using `test.expected.update` system property, like so:
>      ```shell
>      ./mvnw (test|verify) -Dtest.expected.update
>      ```

> [!TIP]
> By default, **test logs** (`%MODULE%/target/test-logs`) are filtered at WARN level; to adjust this threshold, use `log.level` system property, like so:
>
> ```shell
> ./mvnw (test|verify) -Dlog.level=INFO
> ```

> [!TIP]
> To **debug tests**, attach Maven process to an IDE after entering this:
>
> ```shell
> ./mvnwDebug (test|verify) -DforkCount=0
> ```

### Other Checks

- verify **dependency vulnerabilities**:

    ```shell
    ./mvnw verify -Pverify-extra
    ```

- verify **copyright and licensing declarations compliance** (see <https://reuse.software>):

    ```shell
    reuse lint
    ```
