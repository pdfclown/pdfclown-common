## v0.8.0 (2026-05-14)

### BREAKING CHANGE

- `Aggregations::stream` removed
- `Asserter.SYSTEM_PROPERTY__FILES_UPDATE` replaced by `SYSTEM_PROPERTY__UPDATE_EXPECTED`
- `Assertions::assertParameterized` and related API removed
- `Clis` removed from `build` module
- `Objects::basicLiteral` semantics changed
- `org.pdfclown.common.build.util.io.ResourceNames` moved to `org.pdfclown.common.util.io.ResourceNames`
- `Reflects::method` changed return type to `Optional<Method>`
- `RelatedMap` renamed as `DynamicMap`; `RelatedSet` renamed as `DynamicSet`
- `ResourceNames::toTypeName` semantics changed (file extension removed, dotted folder parts not allowed)
- `TestUnit::localName` changed semantics (test unit-specific resources are placed in directory of the same name)
- redefine `Verifier.Namer` as `Verifier.DefaultNamer`; move `Verifier::verifyResponse` to `CallVerifier`

### Feat

- **build**:
  - harmonize `Asserter`s to approval files' naming conventions
  - implement support to ApprovalTests
  - improve `ResourceNames` (class names semantics)
  - improve `Verifier` hierarchy
  - improve test resources layout
  - remove `Clis`
  - replace `Asserter.SYSTEM_PROPERTY__FILES_UPDATE`
- **util**:
  - add `Clis.InclusionFilter` hierarchy
  - add `defaultValue` support to `Systems::getBooleanProperty`
  - improve `Objects` (non-basic literal formatters)
  - improve `Reflects` (optional return types)
  - remove `Aggregations::stream`
  - rename dynamic aggregations
- move `ResourceNames` to `util` module
- replace parametric assertions (`Assertions::assertParameterized`) with approvals (`Verifier`)

### Fix

- **deps**:
  - bump com.github.javaparser:javaparser-core from 3.28.0 to 3.28.1 (#101)
  - bump groovy.version from 5.0.5 to 5.0.6 (#99)
  - bump jada.version from 0.3.0 to 0.4.1
  - bump org.apache.logging.log4j:log4j-bom from 2.25.4 to 2.26.0 (#98)
  - bump org.pdfclown:pdfclown-common-util from 0.6.0 to 0.7.0
- **util**:
  - fix `Objects::toStringStable`
  - generalize `Patterns::globToRegex`
  - improve `ResourceNames::fromPath`

### Refactor

- **build**:
  - consolidate `CallVerifier` hierarchy
- **util**:
  - improve `Objects` (nullness annotations)

## v0.7.0 (2026-04-25)

### BREAKING CHANGE

- `averageLineLength` parameter replaced by `maxLength`; `marker` parameter length included in `maxLength`
- `Fluent` removed
- `IndentPrintWriter` removed (use `IndentWriter` instead)
- `Objects::objCast` renamed as `tryCast`
- `Objects` API redefined (`any` renamed as `anyThat` to emphasize functional use; `Locale`-related methods removed (`locale`, `localeNorm`) in favor of Apache Commons Lang3 equivalents (`LocaleUtils::toLocale`); `superTypes` return type changed to `Stream<Class>` to harmonize with other type hierarchy-related methods)
- `Objects` utilities `tryGet` and `tryGetElse` moved to `Functions`
- `Objects` utilities redefined:
  - `objDo` moved to `Functions::let`
  - `objElseGet` renamed as `elseGet`
  - `objTo` moved to `Functions::to`
  - `objToElse` moved to `Functions::toElse`
  - `objToElseGet` moved to `Functions::toElseGet`
  - `objToElseGetNonNull` moved to `Functions::toElseGetNonNull`
  - `quiet` renamed as `quietly`
- `ProjectPathResolver`, `MavenPathResolver` parameterless constructors removed
- `ResourceNames::isDir` removed
- `Strings::indexOfElse` and `Strings::lastIndexOfElse` changed `defaultIndex` parameter semantics (`Strings::STR_LENGTH`, instead of out-of-bound value, to return string length); `Strings::strToInteger` removed
- `TestUnit` API redefined (`getTestName` renamed as `getTestLabel`; `getTestMethodName` renamed as `getTestName`)

### Feat

- **build**:
  - improve `ResourceNames`
  - improve `TestUnit`
- **util**:
  - change `Strings::abbreviateMultiline` semantics
  - consolidate indentation-capable writers
  - improve `IndentWriter`
  - improve `Objects`
  - improve `Strings`
  - move functional utilities to `Functions`
  - move functional utilities to `Functions`/2
  - remove `Fluent`

### Fix

- **build**:
  - fix `Assertions.ExpectedGeneration`
  - fix `ModelMapper` on null key
  - improve `Asserter` hierarchy on assertion error
  - improve `checkstyle-checks`
  - initialize `LogManager` on test startup
  - update Jada bibliography
- **deps**:
  - bump com.diffplug.spotless:spotless-maven-plugin from 3.2.1 to 3.3.0 (#82)
  - bump com.diffplug.spotless:spotless-maven-plugin from 3.3.0 to 3.4.0 (#87)
  - bump commons-io:commons-io from 2.21.0 to 2.22.0 (#94)
  - bump groovy.version from 5.0.4 to 5.0.5 (#90)
  - bump io.github.git-commit-id:git-commit-id-maven-plugin from 9.0.2 to 10.0.0 (#91)
  - bump maven.version from 3.9.12 to 3.9.14 (#85)
  - bump maven.version from 3.9.14 to 3.9.15 (#92)
  - bump net.bytebuddy:byte-buddy from 1.18.5 to 1.18.7 (#83)
  - bump net.bytebuddy:byte-buddy from 1.18.7 to 1.18.8 (#89)
  - bump org.apache.logging.log4j:log4j-bom from 2.25.3 to 2.25.4 (#88)
  - bump org.apache.maven.plugins:maven-shade-plugin from 3.6.1 to 3.6.2 (#84)
  - bump org.jsoup:jsoup from 1.22.1 to 1.22.2 (#95)
- **util**:
  - fix `Strings::abbreviateMultiline`
  - improve `Enums`
  - improve aggregation nullness declarations
- clean up nullness annotations

### Refactor

- **build**:
  - remove temporary pdfclown-common-util code
- normalize the codebase
- specify `ExpectedGeneration` type parameter

## v0.6.0 (2026-03-03)

### BREAKING CHANGE

- - `ResourceNames.based(Path,...)` renamed as `fromPath`
  - `ResourceNames::path` renamed as `toPath`
  - `TestEnvironment::resolveName` renamed as `name`
- `indexOfElseEnd` replaced by `indexOfElse`.
- `ParamMessage.format(String, Object[], int)` removed.
- failure message mapping API redefined in `Assertions`.

### Feat

- **build**:
  - add `log.level` system property
  - add `Mocks::mockFileSystems`
  - consolidate resource names support (`ResourceNames`)
  - improve parameterized tests portability and automation (`Assertions`)
  - improve resource resolution (`TestUnit::getTestResourcePath`)
- **util**:
  - add `Objects::enclosingTypes`
  - consolidate `Xmls::toString` overloads
  - improve `Objects`
  - improve `Strings`
  - update fluent collections

### Fix

- **build**:
  - improve Javadoc page layout
  - normalize assertion system properties
  - update javadoc bibliographic entries (CSS 2.2)
- **deps**:
  - bump com.diffplug.spotless:spotless-maven-plugin from 3.1.0 to 3.2.0 (#68)
  - bump com.diffplug.spotless:spotless-maven-plugin from 3.2.0 to 3.2.1 (#69)
  - bump groovy.version from 5.0.3 to 5.0.4 (#65)
  - bump net.bytebuddy:byte-buddy from 1.18.4 to 1.18.5 (#71)
  - bump org.apache.maven.plugins:maven-dependency-plugin from 3.9.0 to 3.10.0 (#72)
  - bump org.apache.maven.plugins:maven-failsafe-plugin from 3.5.4 to 3.5.5 (#77)
  - bump org.apache.maven.plugins:maven-surefire-plugin from 3.5.4 to 3.5.5 (#76)
  - bump org.codehaus.gmavenplus:gmavenplus-plugin from 4.2.1 to 4.3.0 (#66)
  - bump org.codehaus.gmavenplus:gmavenplus-plugin from 4.3.0 to 4.3.1 (#73)
  - bump org.codehaus.mojo:xml-maven-plugin from 1.2.0 to 1.2.1 (#67)
  - bump org.pdfclown:jada-maven-plugin from 0.2.0 to 0.2.2 (#64)
  - bump org.pdfclown:jada-maven-plugin from 0.2.2 to 0.3.0 (#79)
  - bump org.pdfclown:pdfclown-common-util from 0.4.0 to 0.5.0
- **util**:
  - consolidate `Throwable` argument (`ParamMessage`)
  - improve `Objects::toString...` methods

## v0.5.0 (2026-01-17)

### BREAKING CHANGE

- `Builds` API redefined.
- `org.pdfclown.common.build.meta` replaced by `org.pdfclown.common.util.meta`.
- `Processes` API redefined.
- `ReleaseManager` constructor redefined.

### Feat

- **build**:
  - add support to `onRelease` script hook
- **util**:
  - add support to interactive shell execution (`Processes`)

### Fix

- **build**:
  - improve `Builds`
  - support local maven executable resolution (`Builds::classpath`)
- **deps**:
  - bump net.bytebuddy:byte-buddy from 1.18.3 to 1.18.4 (#63)
  - bump org.pdfclown:pdfclown-common-util from 0.2.2 to 0.4.0
- **super**:
  - fix Javadoc links
- **util**:
  - improve `Processes`

## v0.4.0 (2026-01-11)

### BREAKING CHANGE

- `SemVer` renamed to `SemVer2`, its API redefined.
- `ServiceProvider` API breaks compatibility because of changed return types.
- API references to shaded pdfclown-common-util (`org.pdfclown.common.build.util`) have been remapped to `org.pdfclown.common.build.shaded.util`.
- Execution identifiers redefined:
  - default-test-javadoc-> generate-test-javadoc-jar
  - execute-jar-init -> init-build-info
  - flatten -> flatten-pom
  - get-git-commit-info -> init-git-commit-info
  - help-mojo-generate -> generate-help-mojo
  - integration-test -> run-integration-test
  - javadoc-jar-generate -> generate-javadoc-jar
  - mojo-descriptor-generate -> generate-plugin-descriptor
  - sisu-index-generate -> generate-sisu-index
  - sources-attach -> generate-sources-jar
  - sources-checkstyle -> check-sources-style

### Feat

- **build**:
  - define `LogCaptorProvider` (SPI)
- **util**:
  - add support to `Appendable` for `Objects::toStringWith...`
  - add support to `byte[]` stream (`IntStream`)
  - add support to unsigned `byte[]` stream (`IntStream`)
  - consolidate `ServiceProvider`
- expand versioning metadata model

### Fix

- **build**:
  - improve javadoc table rows contrast
  - simplify pdfclown-common-util shade mapping
- **deps**:
  - bump com.github.javaparser:javaparser-core from 3.27.1 to 3.28.0 (#59)
  - bump com.puppycrawl.tools:checkstyle from 12.3.0 to 12.3.1 (#56)
  - bump org.jsoup:jsoup from 1.21.2 to 1.22.1 (#55)
  - bump org.owasp:dependency-check-maven from 12.1.9 to 12.2.0 (#61)
  - bump org.sonatype.central:central-publishing-maven-plugin from 0.9.0 to 0.10.0 (#57)
- **super**:
  - improve POM (execution identifiers)
- **util**:
  - improve `Object::toString`-related utilities

### Refactor

- **util**:
  - clean up code (`ServiceProvider`)
- normalize `Object::toString` implementations
- normalize derived fields

## v0.3.0 (2025-12-30)

### BREAKING CHANGE

- old API references to the temporary bootstrap copy of pdfclown-common-util (`org.pdfclown.common.build.internal.util_`) have been remapped to `org.pdfclown.common.build.util`.

### Fix

- **build**:
  - restore default javadoc table row styles
  - shade pdfclown-common-util
- **deps**:
  - bump maven.version from 3.9.11 to 3.9.12 (#51)
  - bump net.bytebuddy:byte-buddy from 1.18.2 to 1.18.3 (#50)
  - bump org.pdfclown:jada-maven-plugin from 0.1.2 to 0.1.3 (#53)
  - bump org.pdfclown:jada-maven-plugin from 0.1.3 to 0.2.0 (#54)
- **super**:
  - fix `MANIFEST.MF` generation

## v0.2.2 (2025-12-24)

### Fix

- improve `internal-pom` profile

## v0.2.1 (2025-12-22)

### Fix

- **deps**:
  - bump jada.version from 0.1.0 to 0.1.2

## v0.2.0 (2025-12-22)

### Feat

- add Jada to enhance Javadoc

## v0.1.1 (2025-12-22)

### Fix

- **build**:
  - fix default release sequence (`DEPLOY_SCM_CHECKOUT` step)
  - fix default release sequence (`DEPLOY` step)
- **deps**:
  - bump log4j.version from 2.25.2 to 2.25.3 (#49)

## v0.1.0 (2025-12-20)
