<!--
  SPDX-FileCopyrightText: 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: CC-BY-SA-4.0
-->

# Building and Testing

This document describes how to set up your development environment to build and test pdfClown.org projects.

## Prerequisites

- **JDK 11** (for source code compilation)

- **JDK 17+** (for Maven execution)

- **[Maven toolchains configuration](https://maven.apache.org/guides/mini/guide-using-toolchains.html)** for JDK 11 [[sample toolchains.xml](toolchains.xml)]

- **[pre-commit](https://pre-commit.com/)** [[installation instructions](https://pre-commit.com/#install)]

## Setup

1. install [REUSE](https://reuse.software/spec-3.3/) pre-commit hook:

        pre-commit install

    This hook automatically runs `reuse lint` on every commit, preventing your commit from going through if it doesn't pass REUSE validation.

## Building

In order to speed up frequent building operations, heavier goals are skipped by default and can be activated via **profiles**. For the sake of intuitiveness and overall consistency, all manually-activated profiles are named after their relevant phase (eg, `"install"` profile for `install` phase; `"deploy"` profile for `deploy` phase, instead of the more customary `"release"`; and so on).

Common CLI operations:

- local installation:

    - basic build (without integration tests, sources jar nor javadoc jar):

            mvn install

    - full build (with integration tests, sources jar and javadoc jar):

            mvn install -Pverify,install

- publishing on Maven Central:

        mvn deploy -Pverify,deploy

See [pdfclown-common-base](../pdfclown-common-base/pom.xml) for a basic cheatsheet.