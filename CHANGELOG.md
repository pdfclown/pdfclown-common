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
