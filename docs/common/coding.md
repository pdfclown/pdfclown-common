pdfClown.org > [Documentation](README.md) > [Project Conventions](conventions.md) >

# Coding Conventions

## Comments

### Javadoc

#### Headings

For classic (HTML-based) Javadoc, use `<h4>` or lower tags.

RATIONALE: headings within Javadoc comments are a controversial topic: the [official specification](https://docs.oracle.com/en/java/javase/11/docs/specs/doc-comment-spec.html) discourages their use, as they may interfere with the standard page structure; Javadoc tool's doclint even prohibits certain levels, but [it hasn't been consistent across JDK versions](https://bugs.openjdk.org/browse/JDK-8223552) — the consensus seems to avoid level 3 and higher.

## API

### Equivalence (`equals`+`hashcode`)

The `equals` method is notoriously plagued by semantic ambiguities due to the conflation of conflicting purposes into the same API: besides its general contract specified by [`Object`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Object.html) (stable, strong equivalence tied to the `hashcode` method), [`Collection`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Collection.html) specifies a looser contract (unstable, weak equivalence independent of the `hashcode` method) which makes the latter unsuitable for hash-based uses.

Consequently, *`equals` and `hashcode` methods MUST be overridden if and only if the class is immutable and not derived from `Collection`*. If such class is non-final (weakly immutable), it MUST use `instanceof` operator instead of `getClass` method for instance comparison in order to preserve the [Liskov Substitution Principle](https://en.wikipedia.org/wiki/Liskov_substitution_principle); moreover, it MUST enforce equivalence symmetry by declaring both `equals` and `hashcode` as final methods, accompanied for the sake of clarity by the following Javadoc note:

```java
/**
 * @implNote Marked as final to enforce equivalence symmetry.
 */
```

## Tests

### Naming

- **test packages**:

    - **containing primary test code**: `mainCodePackage`

      Same package as the corresponding main-code package — dead obvious.

      Example:
      <pre>
      // test package for `org.example.myapp.util` main-code package
      package org.example.myapp.util;</pre>

    - **containing class-specific ancillary test code**: `mainCodePackage DOT testClassName UNDERSCORE (DOT subPackage)*`

      Class-specific ancillary test code is every piece of code (sample objects, mocks, ...) which *pertains exclusively to a test class*. Its package SHALL be suffixed by single underscore to avoid collision with the corresponding class.

      Example:
      <pre>
      // test package for ancillary test code of integration test class `MyClassIT`
      package org.example.myapp.MyClassIT_;</pre>

    - **containing generic ancillary test code**: `mainCodePackage DOT UNDERSCORE ancillarySubPackagePart (DOT subPackage)*`

      Generic ancillary test code is every piece of code (sample objects, mocks, ...) which is *NOT specific to a test class yet NOT intended for reuse*. Its package SHALL be prefixed by single underscore to avoid ambiguities with main-code packages.

      Example:
      <pre>
      // test package for issues of a project with `org.example.myapp` as root package
      package org.example.myapp._issues;</pre>

    - **containing internal test harness**: `mainCodePackage DOT UNDERSCORE UNDERSCORE 'test' (DOT subPackage)*`

      Internal test harness is every piece of code (utilities, models, mocks, ...) which is intended for *reuse within the current test suite*. Its package SHALL be prefixed by double underscore to avoid ambiguities with `*.test` packages containing the public test harness.

      Example:
      <pre>
      // test package for internal test harness of a project with `org.example.myapp` as root package
      package org.example.myapp.__test;</pre>

    - **containing public test harness**: `mainCodePackage DOT 'test' (DOT subPackage)*`

      Public test harness is every piece of code (utilities, models, mocks, ...) which is intended for *reuse outside the current test suite, exported in a test-jar artifact*.

      Example:
      <pre>
      // test package for public test harness of the project with `org.example.myapp` as root package
      package org.example.myapp.test;</pre>

- **test classes**:

    - **unit tests**: `TestedClassName 'Test'`

      Example:
      <pre>
      // unit tests on class `PrintStream`
      class PrintStreamTest { . . .</pre>

    - **integration tests**: `TestedClassName 'IT'`

      Example:
      <pre>
      // integration tests on class `PrintStream`
      class PrintStreamIT { . . .</pre>

- **test methods**:
    - **unit tests**:
      - **testing an API method**

        Methods testing the behavior of specific API methods SHALL be named as-is; in case of collision with the test class API itself (for example, `equals`), they SHALL be suffixed by an underscore; in case of ambiguity, they SHALL be suffixed by their argument types:

        - **main case**: `apiMethodName (UNDERSCORE ArgumentType)*`

          Example:
          <pre>
          // main test on method `PrintStream.append(CharSequence, int, int)`
          @Test
          void append_CharSequence_int_int() { . . .</pre>

        - **special cases**: `apiMethodName (UNDERSCORE ArgumentType)* UNDERSCORE UNDERSCORE specialCase (UNDERSCORE specialCasePart)*`

          Special test cases on an API method SHALL be named separating the test case with double underscore (to avoid ambiguity with method signature). `specialCasePart` SHALL be a subcase or a descriptive suffix of the test case.

          Example:
          <pre>
          // failure test on method `PrintStream.append(CharSequence, int, int)`
          @Test
          void append_CharSequence_int_int__indexOutOfBounds() { . . .</pre>

      - **testing an API class**: `UNDERSCORE testCase (UNDERSCORE testCasePart)*`

        Methods testing other behaviors within a class SHALL be prefixed by single underscore (to make them stick out from tests on API methods). `testCasePart` SHALL be a subcase or a descriptive suffix of the test case.

        Example:
        <pre>
        // test on a fixed issue about the tested class
        @Test
        void _issue123_missingNewLine() { . . .</pre>

    - **integration tests**: `testCase (UNDERSCORE testCasePart)*`

      Methods testing the behavior of a class as a whole SHALL be named with optional `testCasePart` which SHALL be a subcase or a descriptive suffix of the test case.

      Example:
        <pre>
        // test on a task involving the tested class
        @Test
        void execute() { . . .</pre>


RATIONALE: avoid ambiguity between primary test-code elements (directly associated to tested main-code elements) and secondary test-code elements (NOT directly associated to any main-code element).
