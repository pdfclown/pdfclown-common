<!--
  SPDX-FileCopyrightText: 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later
-->

# Third-party Code Reuse

Source code from third-party projects is incorporated according to [REUSE 3.3](https://reuse.software/spec-3.3/) specification.

It may be incorporated either keeping track of its original repository (fork), or independently (detached).

Whenever any source code from third-party projects is incorporated, it must be documented as follows:

1. in case of *files from a new third-party project*, add an entry to the "Third-party software" section of the COPYING.md file in the root project, eg:

      ```
      - JSONassert

        Source: <https://github.com/skyscreamer/JSONassert><br/>
        License: [Apache License 2.0](LICENSES/Apache-2.0.txt)
      ```

2. in case of *files bound to a new repository*, add an entry to the "Fork reconciliation" section of the project's README.md, eg:

    <table border="1">
    <tr>
    <td><b>Local package</b></td>
    <td><b>Upstream package</b></td>
    <td><b>Upstream commit*</b></td>
    <td><b>Upstream VCS</b></td>
    </tr>
    <tr><td><code>org.pdfclown.common.build.internal.jsonassert</code></td><td><a href="https://github.com/skyscreamer/JSONassert">org.skyscreamer.jsonassert</a></td><td><a href="https://github.com/skyscreamer/JSONassert/commit/7414e901af11c559bc553e5bb8e12b99a57d1c1c">7414e901af11c559bc553e5bb8e12b99a57d1c1c</a> (2022-07-11 18:50:49+0530)</td><td>git</td>
    </tr>
    </table>

    [*] Latest commit reconciled

3. in the source file reusing the third-party code, add a corresponding licensing notice formatted in accordance with the extent of the incorporation and accompanied by an informative block (containing tags like `Changes`, `SourceFQN`, etc):

    - **third-party file**: append to the file header the third-party copyright notice in its equivalent SPDX representation:

        ```
        . . . file header . . .
        /*
          SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

          SPDX-License-Identifier: Apache-2.0
         */

        /*
          Changes: `recursivelyCompareJSONArray(..)` modified to handle also XY
         */
        class AbstractComparator {
          . . .
        ```

        NOTE: If the original code has been incorporated as-is (no change other than trivial adaptation), then the "<code>Changes</code>" tag can be omitted. Other informative tags will be added based on the relationship with the original code:

        - in case of **repository-bound file**, *if the class name of the file has changed* (eg, the original root package indicated among the forks in README.md is `org.skyscreamer.jsonassert` and the original class name is `org.skyscreamer.jsonassert.comparator.AbstractComparator` (relative package `comparator`), whilst its incorporated class name is `org.pdfclown.common.build.internal.jsonassert.comp.AbstractComparator` (root package `org.pdfclown.common.build.internal.jsonassert`, relative package `comp`)), then the `SourceFQN` tag will specify its original fully qualified name:

          ```
          . . . file header . . .
          /*
            SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

            SPDX-License-Identifier: Apache-2.0
           */

          /*
            SourceFQN: org.skyscreamer.jsonassert.comparator.AbstractComparator
            Changes: `recursivelyCompareJSONArray(..)` modified to handle also XY
           */
          class AbstractComparator {
            . . .
          ```

        - in case of **detached file**, the `Source` tag will specify its repository-specific permalink:

          ```
          . . . file header . . .
          /*
            SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

            SPDX-License-Identifier: Apache-2.0
           */

          /*
            SourceFQN: org.skyscreamer.jsonassert.comparator.AbstractComparator
            Source: https://github.com/skyscreamer/JSONassert/blob/e81c16c59ce0860f97a65d871589ab2337370c4b/src/main/java/org/skyscreamer/jsonassert/comparator/AbstractComparator.java
            Changes: `recursivelyCompareJSONArray(..)` modified to handle also XY
           */
          class AbstractComparator {
            . . .
          ```

    - **third-party code fragment** into a project file: wrap the fragment as an SPDX snippet:

      ```
      // SPDX-SnippetBegin
      // SPDX-SnippetCopyrightText: © 2016 Foo Ltd
      // SPDX-License-Identifier: LGPL-3.0-or-later
      //
      // SourceFQN: org.foo.bar.graphics.AnotherClass.myMethod(String)
      // Source: https://github.com/foo/bar/blob/e9e7ce933f564da9a0dbbca476bd74a25d6f0663/src/main/java/org/foo/bar/graphics/AnotherClass.java
      // Changes: algorithm X substituted with Y
      . . .
      // SPDX-SnippetEnd
      ```
