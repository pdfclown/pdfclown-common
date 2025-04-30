<!--
  SPDX-FileCopyrightText: 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: CC-BY-SA-4.0
-->

# Third-party Code Reuse

Source code from third-party projects is incorporated according to [REUSE 3.3](https://reuse.software/spec-3.3/) specification.

It may be incorporated either keeping track of its original repository (fork), or not (detached).

Whenever any source code from third-party projects is incorporated, it must be documented as follows:

1. in case of *files from a new third-party project*, add an **entry to the NOTICE.txt file** in the root directory, eg:

      ```
      JSONassert <https://github.com/skyscreamer/JSONassert>
      Copyright 2012-2022 Skyscreamer
      Licensed under Apache License 2.0 <https://www.apache.org/licenses/LICENSE-2.0>
      ```

2. in the source file reusing the third-party code, add a **licensing notice** formatted in accordance with the extent of the incorporation, accompanied by additional information (such as `Source`, `SourceFQN` and `Changes` tags) whenever appropriate:

    - **third-party file** as a project file: append to the file header the third-party copyright notice in its equivalent SPDX representation:

        ```java
        . . . file header . . .

        /*
          SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

          SPDX-License-Identifier: Apache-2.0

          -------------------------------------------------------------------------------------------------
          Changes: `recursivelyCompareJSONArray(..)` modified to handle also . . .
         */

        . . .

        class AbstractComparator {
          . . .
        ```

        NOTE: If the original code has been incorporated as-is (no change other than trivial adaptation), then the `Changes` tag can be omitted.

        Other informative tags will be added based on the relationship between the incorporated file and the original code:

        - the `SourceFQN` tag indicates the original name of the type (or type member — i.e., field or method) in case the incorporation changed it:

          ```java
          . . . file header . . .

          /*
            SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

            SPDX-License-Identifier: Apache-2.0

            -------------------------------------------------------------------------------------------------
            Changes: `recursivelyCompareJSONArray(..)` modified to handle also . . .
           */

          . . .

          // SourceFQN: org.skyscreamer.jsonassert.comparator.AbstractComparator
          class AbstractComparator {
            . . .
          ```

        - the `Source` tag indicates, in case of detached file, its repository-specific permalink:

          ```java
          . . . file header . . .

          /*
            SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

            SPDX-License-Identifier: Apache-2.0

            -------------------------------------------------------------------------------------------------
            Source: https://github.com/skyscreamer/JSONassert/blob/e81c16c59ce0860f97a65d871589ab2337370c4b/src/main/java/org/skyscreamer/jsonassert/comparator/AbstractComparator.java
            Changes: `recursivelyCompareJSONArray(..)` modified to handle also . . .
           */

          . . .

          // SourceFQN: org.skyscreamer.jsonassert.comparator.AbstractComparator
          class AbstractComparator {
            . . .
          ```

            NOTE: In case of detached file, the `SourceFQN` tag will always be indicated (since, by definition, its relationship with the original repository is not documented elsewhere in the project).

    - **third-party code fragment** into a project file: wrap the fragment as an SPDX snippet:

      ```java
      // SPDX-SnippetBegin
      // SPDX-SnippetCopyrightText: © 2016 Foo Ltd
      // SPDX-License-Identifier: LGPL-3.0-or-later
      //
      // Source: https://github.com/foo/bar/blob/e9e7ce933f564da9a0dbbca476bd74a25d6f0663/src/main/java/org/foo/bar/graphics/AnotherClass.java
      // Changes: algorithm X substituted with Y
      // SourceFQN: org.foo.bar.graphics.AnotherClass.myMethod(String)
      . . . third-party code fragment . . .
      // SPDX-SnippetEnd
      ```
