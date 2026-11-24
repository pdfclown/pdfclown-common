pdfClown.org > [Documentation](README.md) > [Project Conventions](conventions.md) >

# Third-Party Content Reuse

<!-- REUSE-IgnoreStart -->

> [!IMPORTANT]
> To enforce the conventions described herein, committers have to set up the [**commit validation hooks**](building.md#setup).

Contents from third-party projects are incorporated according to [REUSE 3.3](https://reuse.software/spec-3.3/) specification.

They may be incorporated either keeping track of their original repository (forked), or not (detached).

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
       Module:     pdfclown-common-build
       Module:     pdfclown-common-util
       AssetType:  SOURCE_CODE
       ```

       Tags:
       - `AssetType` (repeatable: {`OBJECT_CODE`|`RESOURCE`|`SOURCE_CODE`}): specifies what *kind of third-party asset* was reused.
       - `Component`: specifies the *name of a third-party project* whose assets are reused in the project.
       - `Copyright`: reproduces the *copyright notice* declared by the third-party project.
       - `License`: specifies the *SPDX identifier of the license* applied to the third-party project (for example, `Apache-2.0`), or a `LicenseRef-<custom-name>` identifier in case of custom license.
       - `LicenseSource`: specifies the *URL to the original license full text*, in case of custom license.
       - `Module` (repeatable): specifies a *project module* reusing the component.

2. **mark third-party content** with appropriate copyright and licensing information, accompanied by source coordinates (either `Component` tag (if forked) or `Source` tag (if detached), and `SourceName`) and other additional information (such as `Changes` — see here below) whenever appropriate:
   - in case of source file reusing **third-party code**, add copyright and licensing information formatted in accordance with the extent of the incorporation:

       - **third-party code file** (as a new project file):
           1. **original copyright notice**: *leave the original text from the third-party file unaltered*, and append the corresponding `SPDX-License-Identifier` tag just after its comment block, along with source coordinates — for example:
               ```java
               /*
                *  Copyright 2003-2026 The Apache Software Foundation
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
               // SPDX-License-Identifier: Apache-2.0
               // Component: Apache Groovy @ 0d40fc0
               // SourceName: groovy.lang.Tuple

               . . . derivative copyright notice . . .
               ```
           2. **derivative copyright notice**: append after the original copyright notice, separated by a blank line, a new comment block specifying copyright and licensing information of the derivative project, along with `Changes` tag — for example:

               ```java
               . . . original copyright notice . . .

               /*
                 CHANGES TO THE ORIGINAL FILE

                 SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

                 SPDX-License-Identifier: LGPL-3.0-or-later

                 This file (Tuple.java) is part of pdfclown-common-util module in pdfClown Common project
                 <https://github.com/pdfclown/pdfclown-common>

                 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
                 this file, you MUST add your own copyright notice in a separate comment block below this file
                   header, listing the main changes you applied to the original source.

                 Changes:
                 - factory methods reduced to 9 elements maximum
                 - constructor visibility reduced to package
                 - equality comparison simplified
                */

               public class Tuple<E extends @Nullable Object> extends AbstractList<E> implements Comparable<Tuple<E>>, RandomAccess {
                 . . .
               ```

        - **third-party code fragment** (into an existing project file): wrap the fragment as an SPDX snippet, and provide the same information as described here above for third-party code file — for example:

            ```java
            // SPDX-SnippetBegin
            // SPDX-SnippetCopyrightText: original ApprovalTests.Java authors and contributors
            // SPDX-License-Identifier: Apache-2.0
            // Source: https://github.com/approvals/ApprovalTests.Java/blob/e3ac34f79e430e4dc44c1ecfd70499467d9e96d5/approvaltests/src/main/java/org/approvaltests/approvers/FileApprover.java
            // SourceName: org.approvaltests.approvers.FileApprover
            //
            // CHANGES TO THE ORIGINAL SNIPPET
            // SPDX-SnippetCopyrightText: 2025-2026 Stefano Chizzolini and contributors
            // SPDX-License-Identifier: LGPL-3.0-or-later
            // Changes: `approved` file resolution based on actual received file instead of regular
            //          `org.approvaltests.namer.ApprovalNamer.getApprovedFile(String)`.
            private static class Approver implements ApprovalApprover {
            . . . third-party code fragment . . .
            // SPDX-SnippetEnd
            ```

   - in case of **third-party resource file** (such as image, document, etc.), add copyright and licensing information, accompanied by source coordinates and other additional information (such as `Changes` tag — see here below), in one of the following ways:
      - [sidecar `.license` file](https://reuse.software/faq/#uncommentable-file): this is the recommended way, especially for formats (such as images) that cannot hold comment headers
      - comment header (same as third-party source code files, see here above): this may be a viable alternative for textual formats like XML/HTML
      - [bulk annotation](https://reuse.software/faq/#bulk-license): useful in case of vendored directories from a single third party. WARNING: *the matching scope should be as narrow as possible*, since bulk annotation may silently apply wrong copyright and licensing information to files inadvertently slipping inside it, resulting in false negatives to the `reuse lint` command

   **Additional tags**:

   - `Changes`: specifies *relevant differences between the local file and its source*.

   - `Component`: specifies the *component name* (as declared in `THIRD-PARTY-NOTICES.txt` file) and the *source commit* in the corresponding original repository, in case of forked file — for example: `Component: Apache Groovy @ 0d40fc0`

   - `Source`: specifies the *permalink of the file in the original repository*, in case of detached file. When such permalink is unavailable, use `SourcePage` instead.

   - `SourceName`: specifies the *original name of the code element* (type, field, method, etc.). Top-level classes MUST always be accompanied by the corresponding fully-qualified original name; nested elements (inner classes and members) shall specify their original name only in case of change.

   - `SourcePage`: specifies the *permalink of the page holding a reference to the original location of the third-party file*, as an alternative to unavailable `Source`. Typically, third-party content is structured in SCM-based repositories, so there is no need of this tag; sometimes, however, `Source` represents an unstable link, or it is outright unavailable because of dynamic resolution, so this tag provides a landing page from which the source can still be indirectly retrieved.

3. [**verify copyright and licensing declaration compliance**](building.md#other-checks)

## FAQ

### What SPDX identifier to use for license information in case of assets under Public Domain?

Public domain is a legal status rather than a license; the appropriate identifier depends on what determined such legal status:

- the material is in the public domain **by virtue of law** (copyright expired, government work, ineligible for copyright): use `CC-PDM-1.0`.
- the author explicitly chose a **standard license** (`CC0-1.0`, `Unlicense`, etc.) — use that license, as-is.
- the author **informally stated in a notice** that the material is under public domain — create a custom identifier (`LicenseRef-<custom-name>`) and place the original notice in the corresponding file under `/LICENSES` directory in the project, along with the permalink to the original notice.

<!-- REUSE-IgnoreEnd -->
