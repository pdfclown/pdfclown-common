pdfClown.org > [Documentation](README.md) > [Project Conventions](conventions.md) >

# Third-Party Content Reuse

<!-- REUSE-IgnoreStart -->

> [!IMPORTANT]
> To enforce the conventions described herein, committers have to set up the [**commit validation hooks**](building.md#setup).

Contents from third-party projects are incorporated according to [REUSE 3.3](https://reuse.software/spec-3.3/) specification.

They may be incorporated either keeping track of their original repository (fork), or not (detached).

> [!WARNING]
> *DO NOT incorporate code covered by licenses incompatible with the project's own license!*

Whenever any content from third-party projects is incorporated, it MUST be documented as follows:

1. in case of **files from a new third-party project**:
   1. **verify its license requirements** and comply with them — for example, Apache License 2.0 [requires](https://infra.apache.org/licensing-howto.html) to propagate copyright notices through the `NOTICE` file: in such case, add a corresponding entry to the `NOTICE.txt` file in the root directory.

   2. **add an entry to the `THIRD-PARTY-NOTICES.txt` file** in the root directory — for example:

       ```
       Component:  Apache Groovy
       Copyright:  2003-2026 The Apache Software Foundation
       License:    Apache-2.0
       Homepage:   https://github.com/apache/groovy
       ```
       Each entry can have multiple `Copyright` fields, according to the actual attributions from third-party source files incorporated in the project.

2. **mark third-party content** with appropriate copyright and licensing information:
   - in case of source file reusing **third-party code**, add copyright and licensing information formatted in accordance with the extent of the incorporation, accompanied by additional information (such as `Source`, `SourceName` and `Changes` tags — see here below) whenever appropriate:

       - **third-party file** (as a new project file):
           1. **original copyright notice**: if the third-party file contains a copyright notice with traditional license boilerplate, insert the corresponding **`SPDX-License-Identifier` tag** just below the copyright statement, and *leave the original text unaltered* — for example:
                ```java
                /*
                *  Copyright 2003-2026 The Apache Software Foundation
                *
                *  SPDX-License-Identifier: Apache-2.0
                *
                *  Licensed to the Apache Software Foundation (ASF) under one
                *  or more contributor license agreements.  See the NOTICE file
                *  distributed with this work for additional information
                *  regarding copyright ownership.  The ASF licenses this file
                *  to you under the Apache License, Version 2.0 (the
                *  "License"); you may not use this file except in compliance
                *  with the License.  You may obtain a copy of the License at
                *
                *    http://www.apache.org/licenses/LICENSE-2.0
                *
                *  Unless required by applicable law or agreed to in writing,
                *  software distributed under the License is distributed on an
                *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
                *  KIND, either express or implied.  See the License for the
                *  specific language governing permissions and limitations
                *  under the License.
                */
                . . . derivative copyright notice . . .
                ```
              2. **derivative copyright notice**: append after the original copyright notice a new comment block specifying copyright and licensing information of the derivative project, along with additional information (NOTE: `SourceName` tag is always adjacent to the corresponding code element) — for example:

                    ```java
                    . . . original copyright notice . . .
                    /*
                      CHANGES TO THE ORIGINAL FILE

                      SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

                      SPDX-License-Identifier: LGPL-3.0-only

                      This file (Tuple.java) is part of pdfclown-common-util module in pdfClown Common project
                      <https://github.com/pdfclown/pdfclown-common>

                      DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
                      this file, you MUST add your own copyright notice in a separate comment block below this file
                      header, listing the main changes you applied to the original source.

                      Source: https://github.com/apache/groovy/blob/7831e9cf9e66f20c1b7fdd8f7dfff480db7cbe18/src/main/java/groovy/lang/Tuple.java

                      Changes:
                      - factory methods reduced to 9 elements maximum
                      - constructor visibility reduced to package
                      - equality comparison simplified
                    */

                    // SourceName: groovy.lang.Tuple
                    public class Tuple<E extends @Nullable Object> extends AbstractList<E> implements Comparable<Tuple<E>>, RandomAccess {
                      . . .
                    ```

        - **third-party code fragment** (into an existing project file): wrap the fragment as an SPDX snippet — for example:

            ```java
            // SPDX-SnippetBegin
            // SPDX-SnippetCopyrightText: 2016 Foo Ltd
            // SPDX-License-Identifier: LGPL-3.0-only
            //
            // Source: https://github.com/foo/bar/blob/e9e7ce933f564da9a0dbbca476bd74a25d6f0663/src/main/java/org/foo/bar/graphics/AnotherClass.java
            // SourceName: org.foo.bar.graphics.AnotherClass.myMethod(String)
            // Changes: algorithm X substituted with Y
            . . . third-party code fragment . . .
            // SPDX-SnippetEnd
            ```

   - in case of **third-party resource file** (such as image, document, etc.), add copyright and licensing information, accompanied by additional information (such as `Source`, `SourceName` and `Changes` tags — see here below) whenever appropriate, in one of the following ways:
      - [sidecar `.license` file](https://reuse.software/faq/#uncommentable-file): this is the recommended way, especially for formats (such as images) that cannot hold comment headers
      - comment header (same as third-party source code files, see here above): this may be a viable alternative for textual formats like XML/HTML
      - [bulk annotation](https://reuse.software/faq/#bulk-license): useful in case of whole directories from a single third party. WARNING: *the matching scope should be as narrow as possible*, since bulk annotation may silently apply wrong copyright and licensing information to files accidentally slipping inside it, resulting in false negatives to the `reuse lint` command

   **Additional tags**:

   - `Source`: specifies the *permalink of the file in the original repository*, in case of detached file.

   - `Source-Page`: specifies the *permalink of the page holding a reference to the original location of the third-party file (`Source`)*. Typically, third-party content is structured in SCM-based repositories, so there is no need of this tag; sometimes, however, `Source` represents an unstable link, or it is outright unavailable because of dynamic resolution, so this tag provides a corresponding page from which the source can still be indirectly retrieved.

   - `SourceName`: specifies the *original name of the code element (type, field, method, etc)* in case of detached file, or if the incorporation changed it. When specified, the name shall be as terse as possible: simple name, unless disambiguation requires full qualification — for example, the source name of top-level classes in detached files shall always be fully qualified.

   - `Changes`: specifies *relevant differences between the local file and its source*.

3. [**verify copyright and licensing declaration compliance**](building.md#other-checks)
<!-- REUSE-IgnoreEnd -->
