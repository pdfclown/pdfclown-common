pdfClown.org > [Documentation](README.md) >

# Usage

This document describes how to use pdfClown.org projects in your own projects.

All the examples are based on the Maven build system.

According to your needs, you can choose among these alternatives:

- **release artifacts** — for normal usage
- **snapshot artifacts** — for experimental usage

## Release artifacts

Normally, user projects are expected to consume the release versions of pdfClown.org projects as dependencies via Maven Central repository.

For the purpose, put the following declaration in the `dependencies` section of your `pom.xml` (replace `%ARTIFACT_ID%` with the identifier of the intended module (for example, `pdfclown-common-util`), and `%VERSION%` with the release version of your choice (see the "Releases" section of the project on GitHub)):

```xml
<dependency>
  <groupId>org.pdfclown</groupId>
  <artifactId>%ARTIFACT_ID%</artifactId>
  <version>%VERSION%</version>
</dependency>
```

## Snapshot artifacts

In case you want to give a try to the latest, unreleased implementation of pdfClown.org projects, you can consume SNAPSHOT dependencies (updated on a daily basis) via Maven Central Portal Snapshots repository.

For the purpose:

1. add Maven Central Portal Snapshots repository to your global configuration (`~/.m2/settings.xml`):

    ```xml
    <settings>
      . . .
      <profiles>
        . . .
        <profile>
          <id>central-snapshots</id>
          <repositories>
            <repository>
              <id>central-portal-snapshots</id>
              <name>Central Portal Snapshots</name>
              <url>https://central.sonatype.com/repository/maven-snapshots/</url>
              <releases>
                <enabled>false</enabled>
              </releases>
              <snapshots>
                <enabled>true</enabled>
                <checksumPolicy>fail</checksumPolicy>
              </snapshots>
            </repository>
          </repositories>
        </profile>
      </profiles>

      <activeProfiles>
        <activeProfile>central-snapshots</activeProfile>
      </activeProfiles>
    </settings>
    ```

2. put the following declaration in the `dependencies` section of your `pom.xml` (replace `%ARTIFACT_ID%` with the identifier of the intended module (for example, `pdfclown-common-util`), and `%VERSION%` with the current SNAPSHOT version (in the project, see `revision` parameter at `.mvn/maven.config`))):

   ```xml
   <dependency>
       <groupId>org.pdfclown</groupId>
       <artifactId>%ARTIFACT_ID%</artifactId>
       <version>%VERSION%</version>
   </dependency>
   ```

> [!TIP]
> Snapshot artifacts sometimes are managed less robustly than stable ones by IDEs (for example, IntelliJ IDEA may miss the Javadoc jar when asked to download the dependency sources); to remedy such annoyances, go to the command line and force Maven to download the missing artifact:
>
> ```shell
> ./mvnw dependency:get -Dclassifier=javadoc -Dartifact=org.pdfclown:%ARTIFACT_ID%:%VERSION%
> ```
>
> Once downloaded, it should be just a matter of refreshing the Maven projects inside your IDE.

Alternatively, you can build the SNAPSHOT artifacts by yourself: see ["Building"](building.md) (in such case, you obviously don't need the Maven Central Portal Snapshots repository).
