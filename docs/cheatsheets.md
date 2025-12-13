[Documentation](README.md) >

# Cheatsheets

This document provides a concise reference to useful commands for pdfClown.org projects.

## Maven

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
    mvn wrapper:wrapper -Dtype=only-script -Dmaven=3.9.11 -DincludeDebug
    ```

- update:

    ```shell
    ./mvnw wrapper:wrapper -Dmaven=x.x.x
    ```
