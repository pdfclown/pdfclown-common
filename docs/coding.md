[Documentation](README.md) > [Project Conventions](conventions.md) >

# Coding Conventions

## Comments

### Javadoc

#### Headings

For classic (HTML-based) Javadoc, use `<h4>` or lower tags.

RATIONALE: headings within Javadoc comments are a controversial topic: the [official specification](https://docs.oracle.com/en/java/javase/11/docs/specs/doc-comment-spec.html) discourages their use, as they may interfere with the standard page structure; Javadoc tool's doclint even prohibits certain levels, but [it hasn't been consistent across JDK versions](https://bugs.openjdk.org/browse/JDK-8223552) — the consensus seems to avoid level 3 and higher.

## Tests

### Naming

- **Test packages**:

    - **containing primary testing code**: `mainCodePackage`

      Same package as the corresponding main-code package — dead obvious.

      Example:
      <pre>
      // test package for `org.example.myapp.util` main-code package
      package org.example.myapp.util;</pre>

    - **containing ancillary testing code**: `mainCodePackage '._' ancillarySubPackagePart ('.' subPackage)*`

      Ancillary testing code is every piece of code (sample objects, mocks specific to a tested part, ...) which is NOT intended for reuse within the current testing suite. SHALL be prefixed by single underscore (to avoid ambiguities with main-code packages).

      Example:
      <pre>
      // test package for issues of the project with `org.example.myapp` as root package
      package org.example.myapp._issues;</pre>

    - **containing internal testing harness functionality**: `mainCodePackage '.__test' ('.' subPackage)*`

      Internal testing harness functionality is every piece of code (utilities, models, shared mocks, ...) which is intended for reuse within the current testing suite. SHALL be prefixed by double underscore (to avoid ambiguities with `*.test` packages containing public testing harness functionality — see next item).

      Example:
      <pre>
      // test package for internal testing harness functionality of the project with
      // `org.example.myapp` as root package
      package org.example.myapp.__test;</pre>

    - **containing public testing harness functionality**: `mainCodePackage '.test' ('.' subPackage)*`

      Public testing harness functionality is every piece of code (utilities, models, shared mocks, ...) which is intended for reuse outside the current testing suite, exported in a test-jar artifact.

      Example:
      <pre>
      // test package for public testing harness functionality of the project with
      // `org.example.myapp` as root package
      package org.example.myapp.test;</pre>

- **Test classes**:

    - **Unit tests**: `TestedClassName 'Test'`

      Example:
      <pre>
      // unit tests on class `PrintStream`
      class PrintStreamTest { . . .</pre>

    - **Integration tests**: `TestedClassName 'IT'`

      Example:
      <pre>
      // integration tests on class `PrintStream`
      class PrintStreamIT { . . .</pre>

- **Test methods**:
    - **on API method**

      Methods testing the behavior of specific API methods SHALL be named as-is, without any prefix; in case of ambiguity, they SHALL specify their signature as suffix.

      - main case: `apiMethodName ('_' ArgumentType)*`

        Example:
        <pre>
        // main test on method `PrintStream.append(CharSequence, int, int)`
        @Test
        void append_CharSequence_int_int() { . . .</pre>

      - special cases: `apiMethodName ('_' ArgumentType)* '__' specialCase ('_' specialCasePart)*`

        Special test cases on an API method SHALL be named separating the test case with double underscore (to avoid ambiguity with method signature). `specialCasePart` SHALL be a subcase or a descriptive suffix of the test case.

        Example:
        <pre>
        // failure test on method `PrintStream.append(CharSequence, int, int)`
        @Test
        void append_CharSequence_int_int__indexOutOfBounds() { . . .</pre>

    - **on API class**: `'_' testCase ('_' testCasePart)*`

      Methods testing the behavior across a class SHALL be prefixed by single underscore (to make them grouped via code formatter and stick out in code editors). `testCasePart` SHALL be either a subcase or a descriptive suffix of the test case.

      Example:
      <pre>
      // test on a fixed issue about the tested class
      @Test
      void _issue123_missingNewLine() { . . .</pre>

RATIONALE: avoid ambiguity between primary test-code elements (directly associated to tested main-code elements) and secondary test-code elements (NOT directly associated to any main-code element).
