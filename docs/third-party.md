[Documentation](README.md) > [Project Conventions](conventions.md) >

# Third-Party Code Reuse

<!-- REUSE-IgnoreStart -->

> [!IMPORTANT]
> To enforce the conventions described herein, committers have to set up the [**commit validation hooks**](building.md#setup).

Source code from third-party projects is incorporated according to [REUSE 3.3](https://reuse.software/spec-3.3/) specification.

It may be incorporated either keeping track of its original repository (fork), or not (detached).

Whenever any source code from third-party projects is incorporated, it must be documented as follows:

1. in case of *files from a new third-party project*, add an **entry to the NOTICE.txt file** in the root directory, for example:

      ```
      JSONassert <https://github.com/skyscreamer/JSONassert>
      Copyright 2012-2022 Skyscreamer
      Licensed under Apache License 2.0 <https://www.apache.org/licenses/LICENSE-2.0>
      ```

2. in the source file reusing the third-party code, add a **licensing notice** formatted in accordance with the extent of the incorporation, accompanied by additional information (such as `Source`, `SourceName` and `Changes` tags — see down below) whenever appropriate:

    - **third-party file** (as a new project file): append to the file header the third-party copyright notice in its equivalent SPDX representation — for example:

         ```java
         . . . file header . . .

         /*
           SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

           SPDX-License-Identifier: Apache-2.0

           Source: https://github.com/skyscreamer/JSONassert/blob/e81c16c59ce0860f97a65d871589ab2337370c4b/src/main/java/org/skyscreamer/jsonassert/comparator/AbstractComparator.java

           Changes: `recursivelyCompareJSONArray(..)` modified to handle also . . .
          */

         . . .

         // SourceName: org.skyscreamer.jsonassert.comparator.AbstractComparator
         class AbstractComparator {
           . . .
         ```

     - **third-party code fragment** (into an existing project file): wrap the fragment as an SPDX snippet — for example:

         ```java
         // SPDX-SnippetBegin
         // SPDX-SnippetCopyrightText: © 2016 Foo Ltd
         // SPDX-License-Identifier: LGPL-3.0-only
         //
         // Source: https://github.com/foo/bar/blob/e9e7ce933f564da9a0dbbca476bd74a25d6f0663/src/main/java/org/foo/bar/graphics/AnotherClass.java
         // SourceName: org.foo.bar.graphics.AnotherClass.myMethod(String)
         // Changes: algorithm X substituted with Y
         . . . third-party code fragment . . .
         // SPDX-SnippetEnd
         ```

   **Additional tags**:

   - `Source`: specifies its *permalink in the original repository*, in case of detached file.

   - `SourceName`: specifies the *original name of the type, or type member (that is, field or method)*, in case of detached file or if the incorporation changed it.

   - `Changes`: specifies *relevant differences between the local file and its source*.

     NOTE: If the original code has been incorporated as-is (no change other than trivial adaptation), then the `Changes` tag SHALL be omitted.

<!-- REUSE-IgnoreEnd -->
