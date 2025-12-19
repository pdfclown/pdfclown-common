# Contributing to pdfClown Common

These guidelines are meant to help you contribute to the project in the most pleasant, hassle-free way: if you have any doubt or suggestion about this documentation, don't hesitate to open an issue.

## Bug reports

In case the code in the repository doesn't seem to work as expected, please follow these guidelines:

1. **check if the issue has already been reported** — use the [GitHub Issues](https://github.com/pdfclown/pdfclown-common/issues) search

2. **check if the issue has already been fixed** — try to reproduce it using the latest unreleased codebase, in any of the following alternatives:

   - [snapshot artifacts](https://github.com/pdfclown/pdfclown-common/blob/main/docs/common/usage.md#snapshot-artifacts) (pre-built)
   - [source code](https://github.com/pdfclown/pdfclown-common/blob/main/docs/building.md#setup) (to build): check out the relevant branch (`main` for current development, or `r/`-prefixed (e.g., `r/1.5`) for release maintenance) in the repository and [build it](https://github.com/pdfclown/pdfclown-common/blob/main/docs/building.md#building) by yourself

3. **isolate the problem** — reduce your case to the bare minimum which still demonstrates the problem (ideally, create a test case)

4. **be as concise and detailed as possible in your report** — write only the essential, and don't leave out anything useful to trap the bug

## Feature requests

In case the code in the repository lacks a functionality, please follow these guidelines:

1. **provide as much detail and context as possible** — this way, the project's developers can fully evaluate whether your request fits with the scope and aims of the project
2. (optional) **include your solution proposal** — discussing it before you embark in a pull request avoids you waste your time (see next section for further information).

## Pull requests

In case you want to contribute improvements to the code in the repository, please follow these guidelines:

1. **set up your fork** — see ["Building"](https://github.com/pdfclown/pdfclown-common/blob/main/docs/building.md#setup) for step-by-step instructions
2. in your fork, **create the PR branch** where to commit your changes
   -  the branch name MUST follow the corresponding [convention](https://github.com/pdfclown/pdfclown-common/blob/main/docs/common/maintenance.md#branches)
3. **remain focused in scope** — avoid that your pull request contains unrelated commits
4. **adhere to the [coding conventions](https://github.com/pdfclown/pdfclown-common/blob/main/docs/common/coding.md)** used throughout the project

   > [!TIP]
   > Code is automatically formatted via Maven build, so it's best you disable your IDE formatting in order to avoid conflicts. If you want just to refresh the code format without recompiling:
   >
   > ```shell
   > ./mvnw spotless:apply
   > ```

5. **write tests** — *the tests ensure that each method, class, etc. does what it is expected to do according to its specification*. This is critically important when other changes are made to ensure that existing code is not broken (no regression). Just as important, whenever someone is new to a section of code, they should be able to read the tests to get a thorough understanding of what it does and why.

   - if you are **fixing a bug**, you should add tests to ensure that your code has actually fixed the bug, to specify/describe what the code is doing, and to ensure the bug doesn't happen again (you may need to change existing tests if they were inaccurate)
   - if you are **adding a feature**, you should add tests to specify/describe what the code is doing, and to ensure future changes won't alter its behavior

6. **update the documentation** — the documentation has to be updated for users to know that things have been changed
7. **execute a full build** — run a full installation before submitting your pull request, to ensure your changes build successfully:
   ```shell
   ./mvnw install -Pfull
   ```
8. **submit your pull request** — see ["Creating a pull request from a fork"](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request-from-a-fork)
   - the title of your pull request MUST follow the corresponding [convention](https://github.com/pdfclown/pdfclown-common/blob/main/docs/common/maintenance.md#pull-requests)

> [!IMPORTANT]
> Before embarking on any significant pull request (e.g., implementing features, refactoring code, etc.), *please ask first* submitting a feature request with your proposal (otherwise you risk spending a lot of time working on something that the project’s developers might not want to merge into the project...).

## License

By contributing your code, you agree to license your contribution under the [GNU Lesser General Public License (LGPL) version 3.0](https://github.com/pdfclown/pdfclown-common/blob/main/LICENSE.txt) (`LGPL-3.0-only`).

By contributing to the documentation, you agree to license your contribution under the [Creative Commons Attribution-ShareAlike 4.0 License](https://github.com/pdfclown/pdfclown-common/blob/main/LICENSES/CC-BY-SA-4.0.txt) (`CC-BY-SA-4.0`).

## Attribution

This document was inspired by:
- [Contributing to Bootstrap](https://github.com/twbs/bootstrap/blob/e170268b3c021cdb2eec09df6f4ae6db3664a21a/.github/CONTRIBUTING.md)
- [Mermaid Contributing Guide](https://github.com/mermaid-js/mermaid/blob/7b167cf3318766b14011ba4636fa90211015a442/packages/mermaid/src/docs/community/contributing.md)