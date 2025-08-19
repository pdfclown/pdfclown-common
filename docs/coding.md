<!--
  SPDX-FileCopyrightText: 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: CC-BY-SA-4.0
-->

[Project Conventions](conventions.md) >

# Coding Conventions

## Tests

### Naming

The rationale behind these naming conventions is to *avoid ambiguity between primary test-code elements (directly associated to tested main-code elements) and secondary test-code elements (NOT directly associated to any main-code element*).

- **Test packages**:

    - **containing primary testing code**: `mainCodePackage`

      Same package as the corresponding main-code package — dead obvious.

      Example:
      <pre>
      // test package for `org.example.myapp` main-code package
      package org.example.myapp;</pre>

    - **containing ancillary testing code**: `mainCodePackage '._' testSpecificSubPackagePart`

      Ancillary testing code is every piece of code (sample objects, mocks specific to a tested part, etc.) which is NOT intended for reuse across a testing suite.

      Subpackage SHALL be prefixed by an underscore (to make it stick out in code editors and file listings).

      Example:
      <pre>
      // test package for issues of the project with `org.example.myapp` as root package
      package org.example.myapp._issues;</pre>

    - **containing internal testing harness functionality**: `mainCodePackage '.__' testHarnessSubPackagePart`

      Internal testing harness functionality is every piece of code (utilities, models, shared mocks, etc.) which is intended for reuse across a testing suite.

      Subpackage SHALL be prefixed by double underscore (to make it stick out in code editors and file listings).

      Example:
      <pre>
      // test package containing testing utilities for the project with `org.example.myapp` as root package
      package org.example.myapp.__test;</pre>

    - **containing public testing harness functionality**: `mainCodePackage '.test.' testHarnessSubPackagePart`

      Public testing harness functionality is every piece of code (utilities, models, shared mocks, etc.) which is intended for reuse outside the current module and exported in test-jar artifacts.

- **Test classes**:

    - **Unit tests**: `TestedClassName 'Test'`

      Example:
      <pre>
      // unit tests for class `PrintStream`
      class PrintStreamTest { . . .</pre>

    - **Integration tests**: `TestedClassName 'IT'`

      Example:
      <pre>
      // integration tests for class `PrintStream`
      class PrintStreamIT { . . .</pre>

- **Test methods**:
    - **Unit tests**:
      - **on API method**

        Methods testing the behavior of specific API methods SHALL be named as-is, without any prefix; in case of ambiguity, they SHALL specify their signature as suffix.

          - main case: `apiMethodName ( '_' ArgumentType )*`

            Example:
            <pre>
            // main test for method `PrintStream.append(CharSequence, int, int)`
            @Test
            void append_CharSequence_int_int() { . . .</pre>
          - special cases:
            `apiMethodName ( '_' ArgumentType )* '__' specialCase ( '_' specialCasePart )*`

            Special test cases on an API method SHALL be named separating the name of the test case with a double underscore (to avoid ambiguity with method signature).
            `specialCasePart` SHALL be a subcase or a descriptive suffix of the test case.

            Example:
            <pre>
            // failure test for method `PrintStream.append(CharSequence, int, int)`
            @Test
            void append_CharSequence_int_int__indexOutOfBounds() { . . .</pre>
      - **on API class**: `'_' testCase ( '_' testCasePart )*`

        Methods testing the behavior across a class SHALL be prefixed by an underscore (to make them grouped via code formatter and stick out in code editors).
        `testCasePart`
        SHALL be either a subcase or a descriptive suffix of the test case.

        Example:
        <pre>
        // test for a fixed issue about the tested class
        @Test
        void _issue123_missingNewLine() { . . .</pre>

    - **Integration tests**: `testCase ( '_' testCasePart )*`

      Methods testing the behavior of an API class on integration SHALL NOT be prefixed by underscores (since there are no tests on API members like in unit testing, no ambiguity is possible).
