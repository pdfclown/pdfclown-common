pdfClown.org > [Documentation](README.md) >

# Cheatsheets

This document provides a concise reference to useful commands for pdfClown.org projects.

## Maven

### Dependencies

- spot dependency in graph:

    ```shell
    ./mvnw dependency:tree -Dverbose -Dincludes=%GROUP_ID%:%ARTIFACT_ID%
    ```

    where `%GROUP_ID%` is the group ID the searched artifact belongs to, and `%ARTIFACT_ID%` is the searched artifact ID.

### Output

- print output without log noise (`-q -DforceStdout`) — for example:

    ```shell
    ./mvnw org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout
    ```

### Profiles

- list active profiles:

    ```shell
    ./mvnw help:active-profiles
    ```

### Subprojects selection

- select subprojects to build (`-pl`):

    ```shell
    ./mvnw ... -pl .,my-sub
    ```

- select subprojects to build along with their dependencies (`-pl -am`):

    ```shell
    ./mvnw ... -pl .,my-sub -am
    ```

- select subprojects to build along with their dependents (`-pl -amd`):

    ```shell
    ./mvnw ... -pl .,my-sub -amd
    ```

### Wrapper

- initialization (new projects only; regular Maven installation required):

    ```shell
    mvn wrapper:wrapper -Dtype=only-script -Dmaven=%MAVEN_VERSION% -DincludeDebug
    ```

    where `%MAVEN_VERSION%` is the latest version available (e.g., `3.9.12`).

- update:

    ```shell
    ./mvnw wrapper:wrapper -Dmaven=%MAVEN_VERSION%
    ```
