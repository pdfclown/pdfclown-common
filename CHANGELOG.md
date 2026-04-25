## v0.7.0 (2026-04-25)

### BREAKING CHANGE

- `ResourceNames::isDir` removed
- `averageLineLength` parameter replaced by `maxLength`;
`marker` parameter length included in `maxLength`
- `Objects` utilities `tryGet` and `tryGetElse` moved to
`Functions`
- `Objects` utilities redefined:
- `objDo` moved to `Functions::let`
- `objElseGet` renamed as `elseGet`
- `objTo` moved to `Functions::to`
- `objToElse` moved to `Functions::toElse`
- `objToElseGet` moved to `Functions::toElseGet`
- `objToElseGetNonNull` moved to `Functions::toElseGetNonNull`
- `quiet` renamed as `quietly`
- `Fluent` removed
- `Objects::objCast` renamed as `tryCast`
- `ProjectPathResolver`, `MavenPathResolver`
parameterless constructors removed
- `Strings::indexOfElse` and `Strings::lastIndexOfElse`
changed `defaultIndex` parameter semantics (`Strings::STR_LENGTH`,
instead of out-of-bound value, to return string length);
`Strings::strToInteger` removed
- `IndentPrintWriter` removed (use `IndentWriter`
instead)
- `TestUnit` API redefined (`getTestName` renamed as
`getTestLabel`; `getTestMethodName` renamed as `getTestName`)
- `Objects` API redefined (`any` renamed as `anyThat` to
emphasize functional use; `Locale`-related methods removed (`locale`,
`localeNorm`) in favor of Apache Commons Lang3 equivalents
(`LocaleUtils::toLocale`); `superTypes` return type changed to
`Stream<Class>` to harmonize with other type hierarchy-related methods)

### Feat

- **build**: improve `ResourceNames`
- **util**: change `Strings::abbreviateMultiline` semantics
- **util**: move functional utilities to `Functions`/2
- **util**: move functional utilities to `Functions`
- **util**: remove `Fluent`
- **util**: improve `Objects`
- **build**: improve `TestUnit`
- **util**: improve `Strings`
- **util**: improve `Objects`
- **util**: improve `IndentWriter`
- **util**: consolidate indentation-capable writers
- **build**: improve `TestUnit`
- **util**: improve `Objects`

### Fix

- **deps**: bump org.jsoup:jsoup from 1.22.1 to 1.22.2 (#95)
- **deps**: bump commons-io:commons-io from 2.21.0 to 2.22.0 (#94)
- **build**: update Jada bibliography
- **deps**: bump maven.version from 3.9.14 to 3.9.15 (#92)
- **build**: improve `Asserter` hierarchy on assertion error
- **build**: fix `ModelMapper` on null key
- **util**: fix `Strings::abbreviateMultiline`
- **build**: improve `checkstyle-checks`
- **build**: initialize `LogManager` on test startup
- **util**: improve aggregation nullness declarations
- **deps**: bump io.github.git-commit-id:git-commit-id-maven-plugin from 9.0.2 to 10.0.0 (#91)
- **deps**: bump groovy.version from 5.0.4 to 5.0.5 (#90)
- **deps**: bump net.bytebuddy:byte-buddy from 1.18.7 to 1.18.8 (#89)
- **util**: improve `Enums`
- **deps**: bump org.apache.logging.log4j:log4j-bom from 2.25.3 to 2.25.4 (#88)
- **build**: fix `Assertions.ExpectedGeneration`
- clean up nullness annotations
- **deps**: bump com.diffplug.spotless:spotless-maven-plugin from 3.3.0 to 3.4.0 (#87)
- **deps**: bump maven.version from 3.9.12 to 3.9.14 (#85)
- **deps**: bump org.apache.maven.plugins:maven-shade-plugin from 3.6.1 to 3.6.2 (#84)
- **deps**: bump net.bytebuddy:byte-buddy from 1.18.5 to 1.18.7 (#83)
- **deps**: bump com.diffplug.spotless:spotless-maven-plugin from 3.2.1 to 3.3.0 (#82)

### Refactor

- normalize the codebase
- specify `ExpectedGeneration` type parameter
- **build**: remove temporary pdfclown-common-util code

## v0.6.0 (2026-03-03)

### BREAKING CHANGE

- failure message mapping API redefined in `Assertions`.
- `indexOfElseEnd` replaced by `indexOfElse`.
- - `ResourceNames.based(Path,...)` renamed as `fromPath`
- `ResourceNames::path` renamed as `toPath`
- `TestEnvironment::resolveName` renamed as `name`
- `ParamMessage.format(String, Object[], int)` removed.

### Feat

- **build**: consolidate resource names support (`ResourceNames`)
- **build**: improve parameterized tests portability and automation (`Assertions`)
- **build**: add `Mocks::mockFileSystems`
- **util**: add `Objects::enclosingTypes`
- **util**: improve `Objects`
- **util**: improve `Strings`
- **build**: improve resource resolution (`TestUnit::getTestResourcePath`)
- **build**: add `log.level` system property
- **util**: update fluent collections
- **util**: consolidate `Xmls::toString` overloads

### Fix

- **deps**: bump org.pdfclown:jada-maven-plugin from 0.2.2 to 0.3.0 (#79)
- **deps**: bump org.apache.maven.plugins:maven-failsafe-plugin from 3.5.4 to 3.5.5 (#77)
- **deps**: bump org.apache.maven.plugins:maven-surefire-plugin from 3.5.4 to 3.5.5 (#76)
- **util**: consolidate `Throwable` argument (`ParamMessage`)
- **util**: improve `Objects::toString...` methods
- **build**: normalize assertion system properties
- **build**: update javadoc bibliographic entries (CSS 2.2)
- **deps**: bump org.codehaus.gmavenplus:gmavenplus-plugin from 4.3.0 to 4.3.1 (#73)
- **deps**: bump org.apache.maven.plugins:maven-dependency-plugin from 3.9.0 to 3.10.0 (#72)
- **deps**: bump net.bytebuddy:byte-buddy from 1.18.4 to 1.18.5 (#71)
- **deps**: bump com.diffplug.spotless:spotless-maven-plugin from 3.2.0 to 3.2.1 (#69)
- **deps**: bump com.diffplug.spotless:spotless-maven-plugin from 3.1.0 to 3.2.0 (#68)
- **deps**: bump org.codehaus.mojo:xml-maven-plugin from 1.2.0 to 1.2.1 (#67)
- **deps**: bump org.codehaus.gmavenplus:gmavenplus-plugin from 4.2.1 to 4.3.0 (#66)
- **deps**: bump groovy.version from 5.0.3 to 5.0.4 (#65)
- **deps**: bump org.pdfclown:jada-maven-plugin from 0.2.0 to 0.2.2 (#64)
- **build**: improve Javadoc page layout
- **deps**: bump org.pdfclown:pdfclown-common-util from 0.4.0 to 0.5.0

## v0.5.0 (2026-01-17)

### BREAKING CHANGE

- `ReleaseManager` constructor redefined.
- `Builds` API redefined.
- `Processes` API redefined.
- `org.pdfclown.common.build.meta` replaced by
`org.pdfclown.common.util.meta`.

### Feat

- **util**: add support to interactive shell execution (`Processes`)
- **build**: add support to `onRelease` script hook

### Fix

- **deps**: bump net.bytebuddy:byte-buddy from 1.18.3 to 1.18.4 (#63)
- **build**: support local maven executable resolution (`Builds::classpath`)
- **build**: improve `Builds`
- **util**: improve `Processes`
- **super**: fix Javadoc links
- **deps**: bump org.pdfclown:pdfclown-common-util from 0.2.2 to 0.4.0

## v0.4.0 (2026-01-11)

### BREAKING CHANGE

- Execution identifiers redefined
- API references to shaded pdfclown-common-util
  (`org.pdfclown.common.build.util`) have been remapped to
  `org.pdfclown.common.build.shaded.util`.
- `SemVer` renamed to `SemVer2`, its API redefined.
- `ServiceProvider` API changed return types.

### Feat

- **util**: add support to unsigned `byte[]` stream (`IntStream`)
- **util**: add support to `Appendable` for `Objects::toStringWith...`
- **util**: add support to `byte[]` stream (`IntStream`)
- expand versioning metadata model
- **util**: consolidate `ServiceProvider`
- **build**: define `LogCaptorProvider` (SPI)

### Fix

- **super**: improve POM (execution identifiers)
- **deps**: bump org.owasp:dependency-check-maven from 12.1.9 to 12.2.0 (#61)
- **deps**: bump com.github.javaparser:javaparser-core from 3.27.1 to 3.28.0 (#59)
- **deps**: bump org.sonatype.central:central-publishing-maven-plugin from 0.9.0 to 0.10.0 (#57)
- **build**: simplify pdfclown-common-util shade mapping
- **util**: improve `Object::toString`-related utilities
- **build**: improve javadoc table rows contrast
- **deps**: bump com.puppycrawl.tools:checkstyle from 12.3.0 to 12.3.1 (#56)
- **deps**: bump org.jsoup:jsoup from 1.21.2 to 1.22.1 (#55)

### Refactor

- normalize `Object::toString` implementations
- normalize derived fields
- **util**: clean up code (`ServiceProvider`)

## v0.3.0 (2025-12-30)

### BREAKING CHANGE

- old API references to the temporary bootstrap copy of
pdfclown-common-util (`org.pdfclown.common.build.internal.util_`) have
been remapped to `org.pdfclown.common.build.util`.

### Fix

- **super**: fix `MANIFEST.MF` generation
- **build**: shade pdfclown-common-util
- **deps**: bump org.pdfclown:jada-maven-plugin from 0.1.3 to 0.2.0 (#54)
- **deps**: bump maven.version from 3.9.11 to 3.9.12 (#51)
- **deps**: bump net.bytebuddy:byte-buddy from 1.18.2 to 1.18.3 (#50)
- **deps**: bump org.pdfclown:jada-maven-plugin from 0.1.2 to 0.1.3 (#53)
- **build**: restore default javadoc table row styles

## v0.2.2 (2025-12-24)

### Fix

- improve `internal-pom` profile

## v0.2.1 (2025-12-22)

### Fix

- **deps**: bump jada.version from 0.1.0 to 0.1.2

## v0.2.0 (2025-12-22)

### Feat

- add Jada to enhance Javadoc

## v0.1.1 (2025-12-22)

### Fix

- **build**: fix default release sequence (`DEPLOY_SCM_CHECKOUT` step)
- **build**: fix default release sequence (`DEPLOY` step)
- **deps**: bump log4j.version from 2.25.2 to 2.25.3 (#49)

## v0.1.0 (2025-12-20)
