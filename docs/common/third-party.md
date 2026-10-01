pdfClown.org > [Documentation](README.md) > [Project Conventions](conventions.md) >

# Third-Party Code Reuse

<!-- REUSE-IgnoreStart -->

> [!IMPORTANT]
> To enforce the conventions described herein, committers have to set up the [**commit validation hooks**](building.md#setup).

Source code from third-party projects is incorporated according to [REUSE 3.3](https://reuse.software/spec-3.3/) specification.

It may be incorporated either keeping track of its original repository (fork), or not (detached).

Whenever any source code from third-party projects is incorporated, it MUST be documented as follows:

1. in case of **files from a new third-party project**:
   1. verify its **license requirements** and comply with them — for example, Apache License 2.0 [requires](https://infra.apache.org/licensing-howto.html) to propagate copyright notices through the `NOTICE` file: in such case, add a corresponding entry to the `NOTICE.txt` file in the root directory.
   2. add an **entry to the `CREDITS.txt` file** in the root directory — for example:

      ```
      JSONassert <https://github.com/skyscreamer/JSONassert>
      Copyright 2012-2022 Skyscreamer
      License Apache-2.0 <https://spdx.org/licenses/Apache-2.0>
      ```

2. in the source file reusing the third-party code, add a **licensing notice** formatted in accordance with the extent of the incorporation, accompanied by additional information (such as `Source`, `SourceName` and `Changes` tags — see here below) whenever appropriate:

    - **third-party file** (as a new project file):
        1. if the third-party file contains a copyright notice with traditional license boilerplate, insert the corresponding **`SPDX-License-Identifier` tag** just below the copyright statement, and leave the original text unaltered — for example:
             ```java
             . . . file header . . .
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
             ```
        2. prepend to the original copyright notice a new comment block specifying the **additional information** (NOTE: `SourceName` tag is always adjacent to the corresponding code element) — for example:

              ```java
              . . . file header . . .
              /*
                Source: https://github.com/skyscreamer/JSONassert/blob/e81c16c59ce0860f97a65d871589ab2337370c4b/src/main/java/org/skyscreamer/jsonassert/comparator/AbstractComparator.java

                Changes: `recursivelyCompareJSONArray(..)` modified to handle also X Y Z.
               */
              /*
               *  Copyright 2003-2026 The Apache Software Foundation
               *
               *  SPDX-License-Identifier: Apache-2.0
               *
               *  Licensed to the Apache Software Foundation (ASF) under one
               *  or more contributor license agreements.  See the NOTICE file
               *  distributed with this work for additional information
               *  . . .
               */

              // SourceName: org.skyscreamer.jsonassert.comparator.AbstractComparator
              class AbstractComparator {
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

   **Additional tags**:

   - `Source`: specifies the *permalink of the file in the original repository*, in case of detached file.

   - `SourceName`: specifies the *original name of the code element (type, field, method, etc)* in case of detached file, or if the incorporation changed it. When specified, the name shall be as terse as possible: simple name, unless disambiguation requires full qualification — for example, the source name of top-level classes in detached files shall always be fully qualified.

   - `Changes`: specifies *relevant differences between the local file and its source*.

3. run `reuse lint` to check whether the license metadata of the project is valid.

<!-- REUSE-IgnoreEnd -->
